import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FileText, Loader2, RefreshCw, Search, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { useChats } from "@/hooks/useChats";
import { useDocuments } from "@/hooks/useDocuments";
import { formatDate } from "@/utils/formatDate";
import { normalizeSearchValue } from "@/utils/search";
import { getApiErrorMessage } from "@/utils/apiError";
import { Input } from "@/components/ui/input";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { reprocessDocument } from "@/api/documents";

function DocumentsPage() {
  const navigate = useNavigate();
  const { documents, loading, error, removeDocument } = useDocuments();
  const { createNewChat } = useChats();
  const [searchQuery, setSearchQuery] = useState("");
  const [reprocessingId, setReprocessingId] = useState<number | null>(null);

  const sortedDocuments = useMemo(
    () => [...documents].sort((a, b) => b.createdAt.localeCompare(a.createdAt)),
    [documents]
  );

  const filteredDocuments = useMemo(
    () =>
      sortedDocuments.filter((doc) =>
        normalizeSearchValue(doc.originalFilename ?? "").includes(normalizeSearchValue(searchQuery))
      ),
    [sortedDocuments, searchQuery]
  );

  const handleDeleteDocument = async (documentId: number) => {
    const confirmed = window.confirm("Bu dokümanı silmek istiyor musun?");
    if (!confirmed) return;

    const ok = await removeDocument(documentId);
    if (ok) {
      toast.success("Doküman silindi");
    } else {
      toast.error("Doküman silinirken hata oluştu");
    }
  };

  const handleReprocessDocument = async (documentId: number, originalFilename: string) => {
    const confirmed = window.confirm("Bu dokümanı yeniden işleyip yeni sohbet açmak istiyor musun?");
    if (!confirmed) return;

    setReprocessingId(documentId);
    try {
      await reprocessDocument(documentId);

      const created = await createNewChat({
        documentId,
        title: `PDF: ${originalFilename}`,
      });

      if (created) {
        toast.success("Doküman yeniden işlendi ve yeni sohbet açıldı");
        navigate(`/chat?chatId=${created.id}`);
      } else {
        toast.error("Sohbet açılamadı");
      }
    } catch (err) {
      toast.error(getApiErrorMessage(err, "Doküman yeniden işlenemedi") ?? "Doküman yeniden işlenemedi");
    } finally {
      setReprocessingId(null);
    }
  };

  return (
    <div className="min-h-svh flex flex-col bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100">
      <header className="h-14 border-b border-zinc-200 dark:border-zinc-800 px-4 flex items-center justify-between">
        <p className="text-sm font-semibold truncate">Dokumanlar</p>
        <div className="w-48">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-zinc-400" />
            <Input
              type="text"
              placeholder="Dokuman ara..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="h-8 pl-9 border-zinc-300 bg-white text-zinc-900 placeholder-zinc-500 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
            />
          </div>
        </div>
      </header>

      <section className="flex-1 overflow-auto">
        <div className="mx-auto w-full max-w-4xl px-4 py-6 space-y-3">
          {loading ? (
            <p className="text-sm text-zinc-600 dark:text-zinc-400 flex items-center gap-2">
              <Loader2 className="size-4 animate-spin" />
              Dokumanlar yukleniyor
            </p>
          ) : null}

          {error ? <p className="text-sm text-red-500 dark:text-red-400">{error}</p> : null}

          {!loading && !error && filteredDocuments.length === 0 && documents.length === 0 ? (
            <p className="text-sm text-zinc-600 dark:text-zinc-400">Kayitli dokuman yok.</p>
          ) : null}

          {!loading && !error && filteredDocuments.length === 0 && documents.length > 0 ? (
            <p className="text-sm text-zinc-600 dark:text-zinc-400">Aramaniza uygun dokuman bulunamadi.</p>
          ) : null}

          {filteredDocuments.map((doc) => (
            <div
              key={doc.id}
              className="flex items-center gap-2 rounded-xl border border-zinc-200 bg-white px-4 py-3 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <button
                    type="button"
                    className="flex min-w-0 flex-1 items-center gap-3 text-left cursor-pointer hover:opacity-80 transition-opacity"
                  >
                    <FileText className="size-4 text-zinc-500 dark:text-zinc-300" />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium text-zinc-900 dark:text-zinc-100">
                        {doc.originalFilename}
                      </p>
                      <p className="mt-1 text-xs text-zinc-600 dark:text-zinc-400">
                        {formatDate(doc.createdAt)}
                      </p>
                    </div>
                  </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent
                  side="bottom"
                  align="end"
                  sideOffset={8}
                  className="min-w-52 border-zinc-200 bg-white p-1.5 text-zinc-900 shadow-lg dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100"
                >
                 
                  <DropdownMenuItem
                    onClick={() => handleReprocessDocument(doc.id, doc.originalFilename)}
                    className="gap-2 rounded-md px-2 py-2.5 focus:bg-zinc-100 focus:text-zinc-900 dark:focus:bg-zinc-800 dark:focus:text-zinc-100"
                    disabled={reprocessingId === doc.id}
                  >
                    <RefreshCw className={`size-4 ${reprocessingId === doc.id ? "animate-spin" : ""}`} />
                    Yeni Sohbet Aç
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => handleDeleteDocument(doc.id)}
                    className="gap-2 rounded-md px-2 py-2.5 text-red-500 focus:bg-red-50 focus:text-red-600 dark:text-red-400 dark:focus:bg-zinc-800 dark:focus:text-red-300"
                  >
                    <Trash2 className="size-4" />
                    Dokümanı Sil
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

export default DocumentsPage;