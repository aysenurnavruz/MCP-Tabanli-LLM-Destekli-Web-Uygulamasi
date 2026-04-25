import { useCallback, useEffect, useState } from "react";
import { createChat, listChats, type Chat } from "../api/chats";

const ACTIVE_CHAT_KEY = "activeChatId";

function parseError(err: unknown) {
  const e = err as { response?: { data?: { message?: string } }; message?: string };
  return e?.response?.data?.message || e?.message || "Bir hata oluştu.";
}

export function useChats() {
  const [chats, setChats] = useState<Chat[]>([]);
  const [activeChatId, setActiveChatIdState] = useState<number | null>(() => {
    const raw = localStorage.getItem(ACTIVE_CHAT_KEY);
    if (!raw) return null;
    const parsed = Number(raw);
    return Number.isNaN(parsed) ? null : parsed;
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const setActiveChatId = useCallback((id: number | null) => {
    setActiveChatIdState(id);
    if (id === null) {
      localStorage.removeItem(ACTIVE_CHAT_KEY);
      return;
    }
    localStorage.setItem(ACTIVE_CHAT_KEY, String(id));
  }, []);

  const refreshChats = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const page = await listChats();
      setChats(page.content);
      setActiveChatIdState((prev) => {
        if (page.content.length === 0) {
          localStorage.removeItem(ACTIVE_CHAT_KEY);
          return null;
        }

        const hasActive = prev !== null && page.content.some((c) => c.id === prev);
        const next = hasActive ? prev : page.content[0].id;

        if (next === null) {
          localStorage.removeItem(ACTIVE_CHAT_KEY);
        } else {
          localStorage.setItem(ACTIVE_CHAT_KEY, String(next));
        }

        return next;
      });
    } catch (err) {
      setError(parseError(err));
    } finally {
      setLoading(false);
    }
  }, []);

  const createNewChat = useCallback(async (opts?: { documentId?: number; title?: string }) => {
    setError("");
    try {
      const created = await createChat({
        documentId: opts?.documentId,
        title: opts?.title ?? "Yeni Sohbet",
      });
      setChats((prev) => [created, ...prev]);
      setActiveChatId(created.id);
      return created;
    } catch (err) {
      setError(parseError(err));
      return null;
    }
  }, [setActiveChatId]);

  useEffect(() => {
    refreshChats();
  }, [refreshChats]);

  return {
    chats,
    activeChatId,
    setActiveChatId,
    loading,
    error,
    refreshChats,
    createNewChat,
  };
}