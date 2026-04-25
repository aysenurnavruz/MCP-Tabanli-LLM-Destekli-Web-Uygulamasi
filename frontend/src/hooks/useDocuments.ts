import { useCallback, useEffect, useState } from "react";
import { listDocuments, type DocumentItem } from "../api/documents";

function parseError(err: unknown) {
  const e = err as { response?: { data?: { message?: string } }; message?: string };
  return e?.response?.data?.message || e?.message || "Dokumanlar yuklenemedi.";
}

export function useDocuments() {
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const refreshDocuments = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const page = await listDocuments();
      setDocuments(page.content);
    } catch (err) {
      setError(parseError(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshDocuments();
  }, [refreshDocuments]);

  return {
    documents,
    loading,
    error,
    refreshDocuments,
  };
}
