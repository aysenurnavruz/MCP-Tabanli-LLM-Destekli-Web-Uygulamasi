import { useCallback, useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type Chat, createChat, deleteChat, listChats } from "../api/chats";
import { getApiErrorMessage } from "../utils/apiError";
import { queryKeys } from "../lib/queryKeys";

const ACTIVE_CHAT_KEY = "activeChatId";
const CACHED_CHATS_KEY = "cachedChats";

function readCachedChats(): Chat[] {
  const raw = localStorage.getItem(CACHED_CHATS_KEY);
  if (!raw) return [];

  try {
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) return [];
    return parsed as Chat[];
  } catch {
    return [];
  }
}

export function useChats() {
  const queryClient = useQueryClient();

  const chatsQuery = useQuery({
    queryKey: queryKeys.chats,
    queryFn: () => listChats(),
  });

  const [cachedChats, setCachedChats] = useState<Chat[]>(() => readCachedChats());

  const [activeChatId, setActiveChatIdState] = useState<number | null>(() => {
    const raw = localStorage.getItem(ACTIVE_CHAT_KEY);
    if (!raw) return null;
    const parsed = Number(raw);
    return Number.isNaN(parsed) ? null : parsed;
  });

  const setActiveChatId = useCallback((id: number | null) => {
    setActiveChatIdState(id);
    if (id === null) {
      localStorage.removeItem(ACTIVE_CHAT_KEY);
      return;
    }
    localStorage.setItem(ACTIVE_CHAT_KEY, String(id));
  }, []);

  useEffect(() => {
    const next = chatsQuery.data?.content;
    if (!next) return;

    setCachedChats(next);
    localStorage.setItem(CACHED_CHATS_KEY, JSON.stringify(next));
  }, [chatsQuery.data]);

  const createChatMutation = useMutation({
    mutationFn: (payload: { documentId?: number; title?: string }) => createChat(payload),
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.chats });
      setActiveChatId(created.id);
    },
  });

  const deleteChatMutation = useMutation({
    mutationFn: deleteChat,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.chats });
      if (activeChatId !== null) {
        queryClient.invalidateQueries({ queryKey: queryKeys.messages(activeChatId) });
      }
    },
  });

  useEffect(() => {
    const chats = chatsQuery.data?.content ?? cachedChats;

    setActiveChatIdState((prev) => {
      if (chats.length === 0) {
        localStorage.removeItem(ACTIVE_CHAT_KEY);
        return null;
      }

      const hasActive = prev !== null && chats.some((c) => c.id === prev);
      const next = hasActive ? prev : chats[0].id;

      if (next === null) {
        localStorage.removeItem(ACTIVE_CHAT_KEY);
      } else {
        localStorage.setItem(ACTIVE_CHAT_KEY, String(next));
      }

      return next;
    });
  }, [cachedChats, chatsQuery.data]);

  const chats = chatsQuery.data?.content ?? cachedChats;

  const refreshChats = useCallback(async () => {
    await chatsQuery.refetch();
  }, [chatsQuery]);

  const createNewChat = useCallback(
    async (opts?: { documentId?: number; title?: string }) => {
      createChatMutation.reset();
      try {
        const created = await createChatMutation.mutateAsync({
          documentId: opts?.documentId,
          title: opts?.title ?? "Yeni Sohbet",
        });
        return created;
      } catch {
        return null;
      }
    },
    [createChatMutation]
  );

  const removeChat = useCallback(
    async (chatId: number) => {
      deleteChatMutation.reset();
      try {
        await deleteChatMutation.mutateAsync(chatId);
        if (activeChatId === chatId) {
          setActiveChatId(null);
        }
        return true;
      } catch {
        return false;
      }
    },
    [activeChatId, deleteChatMutation, setActiveChatId]
  );

  return {
    chats,
    activeChatId,
    setActiveChatId,
    loading: chatsQuery.isLoading || chatsQuery.isFetching,
    error:
      getApiErrorMessage(chatsQuery.error, "Sohbetler yüklenemedi.") ||
      getApiErrorMessage(createChatMutation.error, "Sohbet oluşturulamadı.") ||
      getApiErrorMessage(deleteChatMutation.error, "Sohbet silinemedi."),
    refreshChats,
    createNewChat,
    removeChat,
  };
}