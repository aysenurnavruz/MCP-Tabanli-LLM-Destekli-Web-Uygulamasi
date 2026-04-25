import { Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { formatDate } from "@/utils/formatDate";

interface ChatContentProps {
  messages: any[];
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
                    <div
                      className={`w-fit max-w-2xl rounded-xl px-4 py-3 border ${
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
                  </div>
                );
              })}
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
