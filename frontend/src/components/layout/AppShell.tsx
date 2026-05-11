import { useRef } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";

import { ChatSidebar } from "@/components/Chat/ChatSidebar";
import { useChats } from "@/hooks/useChats";
import { useDocuments } from "@/hooks/useDocuments";
import { useUpload } from "@/hooks/useUpload";

function AppShell() {
  const navigate = useNavigate();
  const location = useLocation();
  const fileRef = useRef<HTMLInputElement | null>(null);

  const {
    chats,
    loading: chatsLoading,
    error: chatsError,
    refreshChats,
    createNewChat,
    removeChat,
  } = useChats();
  const { refreshDocuments } = useDocuments();
  const { uploading, upload } = useUpload();

  const activeChatId = (() => {
    if (!location.pathname.startsWith("/chat")) {
      return null;
    }
    const chatIdRaw = new URLSearchParams(location.search).get("chatId");
    const chatId = chatIdRaw ? Number(chatIdRaw) : null;
    return chatId !== null && !Number.isNaN(chatId) ? chatId : null;
  })();

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
      const created = await createNewChat({
        documentId: doc.id,
        title: `PDF: ${doc.originalFilename}`,
      });
      if (created) {
        navigate(`/chat?chatId=${created.id}`);
      } else {
        navigate(`/chat?documentId=${doc.id}`);
      }
    }

    e.target.value = "";
  };

  return (
    <SidebarProvider defaultOpen={true}>
      <input
        ref={fileRef}
        type="file"
        accept="application/pdf"
        className="hidden"
        onChange={onFileChange}
      />

      <ChatSidebar
        chats={chats}
        activeChatId={activeChatId}
        setActiveChatId={(chatId) => navigate(`/chat?chatId=${chatId}`)}
        chatsLoading={chatsLoading}
        chatsError={chatsError}
        onRefreshChats={refreshChats}
        uploading={uploading}
        onPickFile={onPickFile}
        onDeleteChat={removeChat}
      />

      <SidebarInset className="min-h-svh bg-background text-foreground">
        <Outlet />
      </SidebarInset>
    </SidebarProvider>
  );
}

export default AppShell;