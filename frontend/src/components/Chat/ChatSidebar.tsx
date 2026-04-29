import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
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
  Settings,
  UserCircle2,
} from "lucide-react";

import { Button } from "@/components/ui/button";
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
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  SidebarSeparator,
  useSidebar,
} from "@/components/ui/sidebar";

import type { Chat } from "@/api/chats";
import { clearTokens } from "@/store/authStore";

const CHATS_SECTION_OPEN_KEY = "sidebarChatsSectionOpen";

interface ChatSidebarProps {
  chats: Chat[];
  activeChatId: number | null;
  setActiveChatId: (id: number) => void;
  chatsLoading: boolean;
  uploading?: boolean;
  onPickFile?: () => void;
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
        <div className="flex size-7 shrink-0 items-center justify-center rounded-md bg-zinc-800 text-zinc-100">
          <Hexagon className="size-4" />
        </div>
        {open ? <span className="truncate text-sm font-semibold text-zinc-100">AYEL PROJE</span> : null}
      </button>

      {open ? (
        <Button
          size="icon-sm"
          variant="ghost"
          onClick={toggleSidebar}
          className="text-zinc-200 hover:bg-zinc-800 hover:text-white"
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
  uploading = false,
  onPickFile,
}: ChatSidebarProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { open } = useSidebar();
  const [isChatsOpen, setIsChatsOpen] = useState(() => {
    const raw = localStorage.getItem(CHATS_SECTION_OPEN_KEY);
    if (raw === null) return true;
    return raw === "true";
  });

  useEffect(() => {
    localStorage.setItem(CHATS_SECTION_OPEN_KEY, String(isChatsOpen));
  }, [isChatsOpen]);

  const onLogout = () => {
    clearTokens();
    navigate("/login");
  };

  return (
    <Sidebar collapsible="icon" className="border-r border-zinc-800 bg-black text-zinc-100">
      <SidebarHeader>
        <SidebarHeaderComponent />
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          {open ? (
            <Button
              type="button"
              variant="ghost"
              onClick={() => onPickFile?.()}
              disabled={uploading || !onPickFile}
              className="mb-2 w-full justify-start gap-2 text-zinc-100 hover:bg-zinc-800"
            >
              <Plus className="size-4" />
              <span>Yeni Sohbet (PDF Yukle)</span>
            </Button>
          ) : (
            <SidebarMenu>
              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => onPickFile?.()}
                  tooltip="Yeni Sohbet (PDF Yukle)"
                  className="text-zinc-200 hover:bg-zinc-800 hover:text-white"
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
                  className="text-zinc-200 hover:bg-zinc-800 hover:text-white"
                >
                  <MessageSquare className="size-4" />
                  <span>Sohbetler</span>
                  {isChatsOpen ? <ChevronDown className="ml-auto size-4" /> : <ChevronRight className="ml-auto size-4" />}
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          ) : (
            <SidebarMenu>
              <SidebarMenuItem>
                <SidebarMenuButton
                  tooltip="Sohbetler"
                  className="text-zinc-200 hover:bg-zinc-800 hover:text-white"
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
                  Yukleniyor
                </div>
              ) : null}

              {chats.map((chat) => (
                <SidebarMenuItem key={chat.id}>
                  <SidebarMenuButton
                    isActive={chat.id === activeChatId}
                    onClick={() => setActiveChatId(chat.id)}
                    tooltip={chat.title || "Yeni Sohbet"}
                    className="text-zinc-200 hover:bg-zinc-800 hover:text-white data-[active=true]:bg-zinc-800 data-[active=true]:text-white"
                  >
                    <span>{chat.title || "Yeni Sohbet"}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}

              {!chatsLoading && chats.length === 0 && open ? (
                <div className="px-2 py-2 text-xs text-zinc-500">Henuz sohbet yok</div>
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
                  className="text-zinc-200 hover:bg-zinc-800 hover:text-white data-[active=true]:bg-zinc-800 data-[active=true]:text-white"
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
                  className="text-zinc-200 hover:bg-zinc-800 hover:text-white"
                >
                  <UserCircle2 className="size-4" />
                  <span>Profil</span>
                </SidebarMenuButton>
              </DropdownMenuTrigger>

              <DropdownMenuContent
                side={open ? "top" : "right"}
                align={open ? "start" : "end"}
                className="w-44 border-zinc-800 bg-zinc-900 text-zinc-100"
              >
                <DropdownMenuItem
                  onClick={() => navigate("/settings")}
                  className="gap-2 focus:bg-zinc-800 focus:text-zinc-100"
                >
                  <Settings className="size-4" />
                  Ayarlar
                </DropdownMenuItem>
                <DropdownMenuSeparator className="bg-zinc-800" />
                <DropdownMenuItem
                  onClick={onLogout}
                  className="gap-2 focus:bg-zinc-800 focus:text-zinc-100"
                >
                  <LogOut className="size-4" />
                  Cikis Yap
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
