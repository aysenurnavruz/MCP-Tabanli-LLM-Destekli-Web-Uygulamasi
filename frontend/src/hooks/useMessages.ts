import { useCallback, useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { listMessages, sendMessage, type Message, type MessageWithCitations } from "../api/chats";
import { getApiErrorMessage } from "../utils/apiError";
import { queryKeys } from "../lib/queryKeys";

type OptimisticMessage = Message & {
  optimistic?: boolean;
};

type ChatMessage = MessageWithCitations | OptimisticMessage;

function createClientMessageId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }

  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function createOptimisticUserMessage(content: string): OptimisticMessage {
  return {
    id: -Date.now(),
    role: "USER",
    status: "CREATED",
    content,
    createdAt: new Date().toISOString(),
    optimistic: true,
  };
}

function createOptimisticAssistantMessage(): OptimisticMessage {
  return {
    id: -(Date.now() + 1),
    role: "ASSISTANT",
    status: "STREAMING",
    content: "Yazıyor...",
    createdAt: new Date().toISOString(),
    optimistic: true,
  };
}

export function useMessages(chatId: number | null) {
  const queryClient = useQueryClient();

  // citations are attached locally so they survive refetches during the same chat session
  const [citationByMessageId, setCitationByMessageId] = useState<Record<number, NonNullable<MessageWithCitations["citations"]>>>({});

  useEffect(() => {
    setCitationByMessageId({});
  }, [chatId]);

  const messagesQuery = useQuery({
    queryKey: queryKeys.messages(chatId),
    queryFn: () => listMessages(chatId as number),
    enabled: chatId !== null,
  });

  const sendMutation = useMutation({
    mutationFn: async (content: string) => {
      if (!chatId) {
        throw new Error("Aktif sohbet yok.");
      }

      return sendMessage(chatId, {
        content: content.trim(),
        clientMessageId: createClientMessageId(),
      });
    },
    onMutate: async (content) => {
      if (chatId === null) {
        return null;
      }

      await queryClient.cancelQueries({
        queryKey: queryKeys.messages(chatId),
      });

      const previous = queryClient.getQueryData<{
        content: Message[];
      }>(queryKeys.messages(chatId));

      const optimisticUserMessage = createOptimisticUserMessage(content.trim());
      const optimisticAssistantMessage = createOptimisticAssistantMessage();

      queryClient.setQueryData<{
        content: OptimisticMessage[];
        totalPages: number;
        totalElements: number;
        size: number;
        number: number;
        first: boolean;
        last: boolean;
      }>(queryKeys.messages(chatId), (current) => {
        if (!current) {
          return {
            content: [optimisticUserMessage, optimisticAssistantMessage],
            totalPages: 1,
            totalElements: 2,
            size: 50,
            number: 0,
            first: true,
            last: true,
          };
        }

        return {
          ...current,
          content: [
            ...current.content,
            optimisticUserMessage,
            optimisticAssistantMessage,
          ],
          totalElements: current.totalElements + 2,
        };
      });

      return { previous };
    },
    onError: (_err, _content, context) => {
      if (!chatId) return;

      if (context?.previous) {
        queryClient.setQueryData(queryKeys.messages(chatId), context.previous);
      }
    },
    onSuccess: (data) => {
      if (!chatId) return;

      queryClient.setQueryData(queryKeys.messages(chatId), (current: any) => {
        if (!current) {
          return {
            content: [
              data.userMessage,
              ...(data.assistantMessage ? [data.assistantMessage] : []),
            ],
            totalPages: 1,
            totalElements: data.assistantMessage ? 2 : 1,
            size: 50,
            number: 0,
            first: true,
            last: true,
          };
        }

        const filtered = (current.content ?? []).filter(
          (m: OptimisticMessage) => !m.optimistic
        );

        return {
          ...current,
          content: [
            ...filtered,
            data.userMessage,
            ...(data.assistantMessage ? [data.assistantMessage] : []),
          ],
        };
      });

      const assistantMessage = data.assistantMessage;

      if (assistantMessage && data.citations?.length) {
        setCitationByMessageId((current) => ({
          ...current,
          [assistantMessage.id]: data.citations,
        }));
      }

      queryClient.invalidateQueries({
        queryKey: queryKeys.messages(chatId),
      });
    },
  });

  const refreshMessages = useCallback(async () => {
    await messagesQuery.refetch();
  }, [messagesQuery]);

  const send = useCallback(
    async (content: string) => {
      if (!chatId || !content.trim()) return false;

      sendMutation.reset();

      try {
        await sendMutation.mutateAsync(content);
        return true;
      } catch {
        return false;
      }
    },
    [chatId, sendMutation]
  );

  return {
    messages: ((messagesQuery.data?.content ?? []) as Message[]).map((message) => ({
      ...message,
      citations: message.role === "ASSISTANT" ? citationByMessageId[message.id] : undefined,
    })) as ChatMessage[],
    loading: messagesQuery.isLoading || messagesQuery.isFetching,
    sending: sendMutation.isPending,
    error:
      getApiErrorMessage(messagesQuery.error, "Mesajlar yüklenemedi.") ||
      getApiErrorMessage(sendMutation.error, "Mesaj gönderilemedi."),
    refreshMessages,
    send,
  };
}