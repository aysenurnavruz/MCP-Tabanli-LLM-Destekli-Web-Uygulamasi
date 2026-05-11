import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import {
  ChevronDown,
  ChevronRight,
  FolderOpen,
  Hexagon,
  Loader2,
  LogOut,
  MessageSquare,
  PanelLeftClose,
  Plus,
  Search,
  Settings,
  Trash2,
  UserCircle2,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  SidebarSeparator,
  useSidebar,
} from "@/components/ui/sidebar";

import type { Chat } from "@/api/chats";
import { useAuth } from "@/hooks/useAuth";
import { normalizeSearchValue } from "@/utils/search";

const CHATS_SECTION_OPEN_KEY = "sidebarChatsSectionOpen";

interface ChatSidebarProps {
  chats: Chat[];
  activeChatId: number | null;
  setActiveChatId: (id: number) => void;
  chatsLoading: boolean;
  chatsError?: string | null;
  onRefreshChats?: () => Promise<void> | void;
  uploading?: boolean;
  onPickFile?: () => void;
  onDeleteChat?: (chatId: number) => Promise<boolean> | boolean;
}

function SidebarHeaderComponent() {
  const { toggleSidebar, open } = useSidebar();

  return (
    <div className="flex items-center justify-between gap-2 px-2 py-1">
      <button
        type="button"
        onClick={() => {
          if (!open) toggleSidebar();
        }}
        className="flex min-w-0 items-center gap-2"
      >
        <div className="flex size-7 shrink-0 items-center justify-center rounded-md bg-zinc-900 text-white dark:bg-zinc-800 dark:text-zinc-100">
          <Hexagon className="size-4" />
        </div>
        {open ? (
          <span className="truncate text-sm font-semibold text-zinc-900 dark:text-zinc-100">
            SYNORA
          </span>
        ) : null}
      </button>

      {open ? (
        <Button
          size="icon-sm"
          variant="ghost"
          onClick={toggleSidebar}
          className="text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800 dark:hover:text-white"
        >
          <PanelLeftClose className="size-4" />
        </Button>
      ) : null}
    </div>
  );
}

export function ChatSidebar({
  chats,
  activeChatId,
  setActiveChatId,
  chatsLoading,
  chatsError,
  onRefreshChats,
  uploading = false,
  onPickFile,
  onDeleteChat,
}: ChatSidebarProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { open } = useSidebar();
  const { logout } = useAuth();

  const [isChatsOpen, setIsChatsOpen] = useState(() => {
    const raw = localStorage.getItem(CHATS_SECTION_OPEN_KEY);
    if (raw === null) return true;
    return raw === "true";
  });

  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    localStorage.setItem(CHATS_SECTION_OPEN_KEY, String(isChatsOpen));
  }, [isChatsOpen]);

  const filteredChats = chats.filter((chat) =>
    normalizeSearchValue(chat.title ?? "").includes(normalizeSearchValue(searchQuery))
  );

  const onLogout = async () => {
    await logout();
    navigate("/login");
  };

  const handleDeleteChat = async (chatId: number) => {
    const confirmed = window.confirm("Bu sohbeti silmek istiyor musun?");
    if (!confirmed) return;

    const ok = await onDeleteChat?.(chatId);
    if (ok) {
      toast.success("Sohbet silindi");
      const remaining = chats.filter((chat) => chat.id !== chatId);
      if (remaining.length > 0) {
        setActiveChatId(remaining[0].id);
      }
    } else {
      toast.error("Sohbet silinirken hata oluştu");
    }
  };

  return (
    <Sidebar collapsible="icon" className="border-r border-zinc-200 dark:border-zinc-800 bg-white dark:bg-black text-zinc-900 dark:text-zinc-100">
      <SidebarHeader>
        <SidebarHeaderComponent />
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          {open ? (
            <div className="px-2 pb-2">
              <div className="relative">
                <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-zinc-400" />
                <Input
                  type="text"
                  placeholder="Sohbet ara..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="h-9 pl-9 border-zinc-300 bg-white text-zinc-900 placeholder-zinc-500 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
                />
              </div>
            </div>
          ) : null}

          {open ? (
            <Button
              type="button"
              variant="ghost"
              onClick={() => onPickFile?.()}
              disabled={uploading || !onPickFile}
              className="mb-2 w-full justify-start gap-2 text-zinc-900 hover:bg-zinc-100 dark:text-zinc-100 dark:hover:bg-zinc-800"
            >
              <Plus className="size-4" />
              <span>Yeni Sohbet (PDF Yükle)</span>
            </Button>
          ) : (
            <SidebarMenu>
              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => onPickFile?.()}
                  tooltip="Yeni Sohbet (PDF Yukle)"
                  className="text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800 dark:hover:text-white"
                >
                  <Plus className="size-4" />
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          )}

          {open ? (
            <SidebarMenu>
              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => setIsChatsOpen((prev) => !prev)}
                  tooltip="Sohbetler"
                  className="text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800 dark:hover:text-white"
                >
                  <MessageSquare className="size-4" />
                  <span>Sohbetler</span>
                  {isChatsOpen ? (
                    <ChevronDown className="ml-auto size-4" />
                  ) : (
                    <ChevronRight className="ml-auto size-4" />
                  )}
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          ) : (
            <SidebarMenu>
              <SidebarMenuItem>
                <SidebarMenuButton
                  tooltip="Sohbetler"
                  className="text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800 dark:hover:text-white"
                >
                  <MessageSquare className="size-4" />
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          )}

          <SidebarGroupContent className={isChatsOpen && open ? "block" : "hidden"}>
            <SidebarMenu>
              {chatsLoading ? (
                <div className="flex items-center gap-2 px-2 py-2 text-xs text-muted-foreground">
                  <Loader2 className="size-3 animate-spin" />
                  Yükleniyor
                </div>
              ) : null}

              {!chatsLoading && chatsError ? (
                <div className="px-2 py-2 text-xs text-red-500 dark:text-red-400 space-y-2">
                  <p>{chatsError}</p>
                  {onRefreshChats ? (
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      onClick={() => void onRefreshChats()}
                      className="h-7 w-full border-zinc-300 text-zinc-700 hover:bg-zinc-100 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800"
                    >
                      Yeniden dene
                    </Button>
                  ) : null}
                </div>
              ) : null}

              {filteredChats.map((chat) => (
                <SidebarMenuItem key={chat.id}>
                  <div className="flex items-center gap-1">
                    <SidebarMenuButton
                      isActive={chat.id === activeChatId}
                      onClick={() => setActiveChatId(chat.id)}
                      tooltip={chat.title || "Yeni Sohbet"}
                      className="flex-1 text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900 data-[active=true]:bg-zinc-100 data-[active=true]:text-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800 dark:hover:text-white dark:data-[active=true]:bg-zinc-800 dark:data-[active=true]:text-white"
                    >
                      <span>{chat.title || "Yeni Sohbet"}</span>
                    </SidebarMenuButton>

                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          type="button"
                          size="icon-sm"
                          variant="ghost"
                          className="text-zinc-500 hover:bg-zinc-100 hover:text-zinc-900 dark:text-zinc-400 dark:hover:bg-zinc-800 dark:hover:text-zinc-100"
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent
                        side="right"
                        align="start"
                        className="border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100"
                      >
                        <DropdownMenuItem
                          onClick={() => handleDeleteChat(chat.id)}
                          className="gap-2 focus:bg-zinc-800 focus:text-zinc-100"
                        >
                          <Trash2 className="size-4" />
                          Sohbeti Sil
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </SidebarMenuItem>
              ))}

              {!chatsLoading && chats.length === 0 && open ? (
                <div className="px-2 py-2 text-xs text-zinc-500">Henüz sohbet yok</div>
              ) : null}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarMenuItem>
                <SidebarMenuButton
                  isActive={location.pathname === "/documents"}
                  onClick={() => navigate("/documents")}
                  tooltip="Dokumanlar"
                  className="text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900 data-[active=true]:bg-zinc-100 data-[active=true]:text-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800 dark:hover:text-white dark:data-[active=true]:bg-zinc-800 dark:data-[active=true]:text-white"
                >
                  <FolderOpen className="size-4" />
                  <span>Dokumanlar</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarSeparator />

      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <SidebarMenuButton
                  tooltip="Profil"
                  className="text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900 dark:text-zinc-200 dark:hover:bg-zinc-800 dark:hover:text-white"
                >
                  <UserCircle2 className="size-4" />
                  <span>Profil</span>
                </SidebarMenuButton>
              </DropdownMenuTrigger>

              <DropdownMenuContent
                side={open ? "top" : "right"}
                align={open ? "start" : "end"}
                className="w-44 border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100"
              >
                <DropdownMenuItem
                  onClick={() => navigate("/settings")}
                  className="gap-2 focus:bg-zinc-100 focus:text-zinc-900 dark:focus:bg-zinc-800 dark:focus:text-zinc-100"
                >
                  <Settings className="size-4" />
                  Ayarlar
                </DropdownMenuItem>
                <DropdownMenuSeparator className="bg-zinc-200 dark:bg-zinc-800" />
                <DropdownMenuItem
                  onClick={onLogout}
                  className="gap-2 focus:bg-zinc-100 focus:text-zinc-900 dark:focus:bg-zinc-800 dark:focus:text-zinc-100"
                >
                  <LogOut className="size-4" />
                  Çıkış Yap
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>

      <SidebarRail />
    </Sidebar>
  );
}