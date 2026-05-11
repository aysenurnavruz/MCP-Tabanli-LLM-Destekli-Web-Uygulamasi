import { Loader2, Plus, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";

interface ChatStartScreenProps {
  isBootstrapping: boolean;
  uploading: boolean;
  chatsError: string | null;
  docsError: string | null;
  uploadError: string | null;
  onPickFile: () => void;
}

export function ChatStartScreen({
  isBootstrapping,
  uploading,
  chatsError,
  docsError,
  uploadError,
  onPickFile,
}: ChatStartScreenProps) {
  return (
    <div className="flex-1 flex items-center justify-center px-6">
      {isBootstrapping ? (
        <div className="w-full max-w-xl rounded-3xl border border-border bg-card/80 p-8 text-center shadow-sm">
          <p className="text-sm text-muted-foreground flex items-center justify-center gap-2">
            <Loader2 className="size-4 animate-spin" />
            Sohbetler ve dokumanlar yukleniyor...
          </p>
        </div>
      ) : (
        <div className="w-full max-w-xl rounded-3xl border border-border bg-card/80 p-8 text-center shadow-sm">
          <div className="mx-auto mb-4 flex size-14 items-center justify-center rounded-2xl bg-primary/10 text-primary">
            <Sparkles className="size-7" />
          </div>
          <h1 className="text-2xl font-semibold">Yeni sohbet başlatmak istiyorsan</h1>
          <p className="mt-3 text-sm text-muted-foreground">
            Önce bir PDF seç. Sohbetler sadece yüklediğin PDF üzerinden açılır.
          </p>
          <div className="mt-6 flex justify-center gap-3">
            <Button onClick={onPickFile} disabled={uploading}>
              <Plus className="size-4" />
              {uploading ? "Yükleniyor..." : "PDF Seç ve Başlat"}
            </Button>
          </div>
          {chatsError ? (
            <p className="mt-4 text-sm text-destructive">{chatsError}</p>
          ) : null}
          {docsError ? (
            <p className="mt-2 text-sm text-destructive">{docsError}</p>
          ) : null}
          {uploadError ? (
            <p className="mt-4 text-sm text-destructive">{uploadError}</p>
          ) : null}
        </div>
      )}
    </div>
  );
}
