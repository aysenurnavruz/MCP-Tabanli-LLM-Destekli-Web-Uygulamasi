import { useCallback, useEffect, useState } from "react";
import { listMessages, sendMessage, type Message } from "../api/chats";

function parseError(err: unknown) {
  const e = err as { response?: { data?: { message?: string } }; message?: string };
  return e?.response?.data?.message || e?.message || "Bir hata oluştu.";
}

function createClientMessageId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export function useMessages(chatId: number | null) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");

  const refreshMessages = useCallback(async () => {
    if (!chatId) {
      setMessages([]);
      return;
    }

    setLoading(true);
    setError("");
    try {
      const page = await listMessages(chatId);
      setMessages(page.content);
    } catch (err) {
      setError(parseError(err));
    } finally {
      setLoading(false);
    }
  }, [chatId]);

  const send = useCallback(
    async (content: string) => {
      if (!chatId || !content.trim()) return false;

      setSending(true);
      setError("");
      try {
        const data = await sendMessage(chatId, {
          content: content.trim(),
          clientMessageId: createClientMessageId(),
        });

        setMessages((prev) => {
          const next = [...prev, data.userMessage];
          if (data.assistantMessage) next.push(data.assistantMessage);
          return next;
        });

        return true;
      } catch (err) {
        setError(parseError(err));
        return false;
      } finally {
        setSending(false);
      }
    },
    [chatId]
  );

  useEffect(() => {
    refreshMessages();
  }, [refreshMessages]);

  return {
    messages,
    loading,
    sending,
    error,
    refreshMessages,
    send,
  };
}