import { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";

import { useChats } from "@/hooks/useChats";
import { useMessages } from "@/hooks/useMessages";
import { useUpload } from "@/hooks/useUpload";
import { useDocuments } from "@/hooks/useDocuments";

import { ChatContent } from "@/components/Chat/ChatContent";
import { ChatStartScreen } from "@/components/Chat/ChatStartScreen";

function ChatPage() {
  const [searchParams] = useSearchParams();
  const [input, setInput] = useState("");
  const fileRef = useRef<HTMLInputElement | null>(null);

  const { uploading, error: uploadError, upload } = useUpload();
  const {
    loading: docsLoading,
    error: docsError,
    refreshDocuments,
  } = useDocuments();

  const {
    chats,
    activeChatId,
    setActiveChatId,
    loading: chatsLoading,
    error: chatsError,
    createNewChat,
  } = useChats();

  const {
    messages,
    loading: messagesLoading,
    error: messagesError,
    send,
  } = useMessages(activeChatId);

  const chatIdRaw = searchParams.get("chatId");
  const requestedChatId = chatIdRaw ? Number(chatIdRaw) : null;
  const hasRequestedChatId = requestedChatId !== null && !Number.isNaN(requestedChatId);
  const hasForceStart = searchParams.get("start") === "1";

  const selectedDocumentIdRaw = searchParams.get("documentId");
  const selectedDocumentId = selectedDocumentIdRaw ? Number(selectedDocumentIdRaw) : null;
  const hasDocumentFilter = selectedDocumentId !== null && !Number.isNaN(selectedDocumentId);

  const visibleChats = useMemo(
    () =>
      hasDocumentFilter
        ? chats.filter((c) => c.documentId === selectedDocumentId)
        : chats,
    [chats, hasDocumentFilter, selectedDocumentId]
  );

  const activeChat = useMemo(
    () => visibleChats.find((c) => c.id === activeChatId) ?? null,
    [visibleChats, activeChatId]
  );

  useEffect(() => {
    if (hasForceStart) {
      if (activeChatId !== null) {
        setActiveChatId(null);
      }
      return;
    }

    if (visibleChats.length === 0) {
      return;
    }

    if (hasRequestedChatId) {
      const requested = visibleChats.find((c) => c.id === requestedChatId);
      if (requested && activeChatId !== requested.id) {
        setActiveChatId(requested.id);
        return;
      }
    }

    const hasActive = activeChatId !== null && visibleChats.some((c) => c.id === activeChatId);
    if (!hasActive) {
      setActiveChatId(visibleChats[0].id);
    }
  }, [
    activeChatId,
    hasForceStart,
    hasRequestedChatId,
    requestedChatId,
    setActiveChatId,
    visibleChats,
  ]);

  const isBootstrapping = chatsLoading || docsLoading;
  const hasActiveChat = Boolean(activeChat);
  const shouldShowStartScreen =
    !isBootstrapping && (hasForceStart || (!hasActiveChat && !hasDocumentFilter));

  const onPickFile = () => fileRef.current?.click();

  const onFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.type !== "application/pdf") {
      alert("Lutfen PDF dosyasi sec.");
      e.target.value = "";
      return;
    }

    const doc = await upload(file);
    if (doc) {
      refreshDocuments();
      await createNewChat({
        documentId: doc.id,
        title: `PDF: ${doc.originalFilename}`,
      });
    }

    e.target.value = "";
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!activeChatId) {
      return;
    }

    const content = input.trim();
    if (!content) return;

    setInput("");
    const ok = await send(content);
    if (!ok) setInput(content);
  };

  return (
    <div className="min-h-svh flex flex-col bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100">
      <input
        ref={fileRef}
        type="file"
        accept="application/pdf"
        className="hidden"
        onChange={onFileChange}
      />

        {!isBootstrapping && !shouldShowStartScreen && hasActiveChat ? (
          <header className="h-14 border-b border-zinc-800 px-4 flex items-center">
            <p className="text-sm font-semibold truncate">{activeChat?.title || "Sohbet"}</p>
          </header>
        ) : null}

        {isBootstrapping || shouldShowStartScreen ? (
          <ChatStartScreen
            isBootstrapping={isBootstrapping}
            uploading={uploading}
            chatsError={chatsError}
            docsError={docsError}
            uploadError={uploadError}
            onPickFile={onPickFile}
          />
        ) : hasDocumentFilter && !hasActiveChat ? (
          <div className="flex-1 flex items-center justify-center px-6">
            <div className="w-full max-w-xl rounded-2xl border border-zinc-800 bg-zinc-900 p-8 text-center">
              <p className="text-sm text-zinc-300">
                Bu dokuman icin henuz sohbet bulunamadi.
              </p>
            </div>
          </div>
        ) : (
          <ChatContent
            messages={messages}
            messagesLoading={messagesLoading}
            messagesError={messagesError}
            chatsError={chatsError}
            input={input}
            setInput={setInput}
            hasActiveChat={hasActiveChat}
            uploadError={uploadError}
            onSubmit={onSubmit}
          />
        )}
    </div>
  );
}

export default ChatPage;