import { useEffect, useRef } from "react";
import { Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { formatDate } from "@/utils/formatDate";
import type { MessageWithCitations } from "@/api/chats";

interface ChatContentProps {
  messages: MessageWithCitations[];
  messagesLoading: boolean;
  messagesError: string | null;
  chatsError: string | null;
  input: string;
  setInput: (value: string) => void;
  hasActiveChat: boolean;
  uploadError: string | null;
  onSubmit: (e: React.FormEvent) => void;
}

export function ChatContent({
  messages,
  messagesLoading,
  messagesError,
  chatsError,
  input,
  setInput,
  hasActiveChat,
  uploadError,
  onSubmit,
}: ChatContentProps) {
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!hasActiveChat) return;

    bottomRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [hasActiveChat, messages, messagesLoading]);

  return (
    <>
      {hasActiveChat ? (
        <>
          <section className="flex-1 overflow-auto">
            <div className="mx-auto max-w-3xl px-4 pt-4 pb-0 space-y-3">
              {chatsError ? (
                <p className="text-sm text-destructive">{chatsError}</p>
              ) : null}

              {messagesError ? (
                <p className="text-sm text-destructive">{messagesError}</p>
              ) : null}

              {messagesLoading ? (
                <p className="text-sm text-muted-foreground flex items-center gap-2">
                  <Loader2 className="size-4 animate-spin" />
                  Mesajlar yükleniyor
                </p>
              ) : null}

              {!messagesLoading && messages.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                  İlk mesajı göndererek başlayabilirsin.
                </p>
              ) : null}

              {messages.map((m) => {
                const isUser = m.role === "USER";

                return (
                  <div
                    key={m.id}
                    className={`flex ${isUser ? "justify-end" : "justify-start"}`}
                  >
                    <div className="w-fit max-w-2xl space-y-2">
                      <div
                        className={`rounded-xl px-4 py-3 border ${
                          isUser
                            ? "bg-primary text-primary-foreground border-transparent"
                            : "bg-card text-card-foreground"
                        }`}
                      >
                        <p className="whitespace-pre-wrap text-sm">{m.content}</p>
                        <p
                          className={`text-[11px] mt-2 ${
                            isUser ? "text-primary-foreground/70" : "text-muted-foreground"
                          }`}
                        >
                          {formatDate(m.createdAt)}
                        </p>
                      </div>

                      {!isUser && m.citations?.length ? (
                        <div className="ml-1 max-w-2xl rounded-lg border border-dashed border-zinc-300 bg-zinc-50 px-3 py-2 text-xs text-zinc-700 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-300">
                          <p className="mb-2 text-[11px] italic text-zinc-500 dark:text-zinc-400">
                            Kaynaklar
                          </p>
                          <div className="space-y-2">
                            {m.citations.map((citation, index) => {
                              const pageLabel =
                                citation.pageStart && citation.pageEnd
                                  ? citation.pageStart === citation.pageEnd
                                    ? `Sayfa ${citation.pageStart}`
                                    : `Sayfa ${citation.pageStart}-${citation.pageEnd}`
                                  : "Sayfa bilgisi yok";

                              return (
                                <div key={`${m.id}-${citation.chunkId}-${index}`} className="space-y-1">
                                  <p className="font-medium text-zinc-800 dark:text-zinc-200">
                                    {pageLabel}
                                  </p>
                                  {citation.preview ? (
                                    <p className="italic text-zinc-600 dark:text-zinc-400">
                                      {citation.preview}
                                    </p>
                                  ) : null}
                                </div>
                              );
                            })}
                          </div>
                        </div>
                      ) : null}
                    </div>
                  </div>
                );
              })}
              <div ref={bottomRef} />
            </div>
          </section>

          <form
            onSubmit={onSubmit}
            className="sticky bottom-0 z-20 bg-background"
          >
            {uploadError ? (
              <p className="mb-2 text-xs text-destructive">{uploadError}</p>
            ) : null}

            <div className="mx-auto w-full max-w-3xl px-4 py-4">
              <Input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Mesajını yaz..."
                disabled={!hasActiveChat}
                className="rounded-lg"
              />
            </div>
          </form>
        </>
      ) : null}
    </>
  );
}
