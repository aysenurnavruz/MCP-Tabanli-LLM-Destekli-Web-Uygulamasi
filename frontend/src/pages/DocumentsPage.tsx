import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { FileText, Loader2 } from "lucide-react";

import { useDocuments } from "@/hooks/useDocuments";
import { formatDate } from "@/utils/formatDate";

function DocumentsPage() {
  const navigate = useNavigate();
  const { documents, loading, error } = useDocuments();

  const sortedDocuments = useMemo(
    () => [...documents].sort((a, b) => b.createdAt.localeCompare(a.createdAt)),
    [documents]
  );

  return (
    <div className="min-h-svh flex flex-col bg-zinc-950 text-zinc-100">
        <header className="h-14 border-b border-zinc-800 px-4 flex items-center">
          <p className="text-sm font-semibold truncate">Dokumanlar</p>
        </header>

        <section className="flex-1 overflow-auto">
          <div className="mx-auto w-full max-w-4xl px-4 py-6 space-y-3">
            {loading ? (
              <p className="text-sm text-zinc-400 flex items-center gap-2">
                <Loader2 className="size-4 animate-spin" />
                Dokumanlar yukleniyor
              </p>
            ) : null}

            {error ? <p className="text-sm text-red-400">{error}</p> : null}

            {!loading && !error && sortedDocuments.length === 0 ? (
              <p className="text-sm text-zinc-400">Kayitli dokuman yok.</p>
            ) : null}

            {sortedDocuments.map((doc) => (
              <button
                key={doc.id}
                type="button"
                onClick={() => navigate(`/chat?documentId=${doc.id}`)}
                className="w-full rounded-xl border border-zinc-800 bg-zinc-900 px-4 py-3 text-left transition hover:bg-zinc-800"
              >
                <div className="flex items-center gap-3">
                  <FileText className="size-4 text-zinc-300" />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-zinc-100">
                      {doc.originalFilename}
                    </p>
                    <p className="mt-1 text-xs text-zinc-400">
                      {formatDate(doc.createdAt)}
                    </p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </section>
    </div>
  );
}

export default DocumentsPage;
