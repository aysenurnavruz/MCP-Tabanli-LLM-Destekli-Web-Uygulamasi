import { useState } from "react";
import { uploadDocument, type DocumentItem } from "../api/documents";

function parseError(err: unknown) {
  const e = err as { response?: { data?: { message?: string } }; message?: string };
  return e?.response?.data?.message || e?.message || "Dosya yüklenemedi.";
}

export function useUpload() {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [document, setDocument] = useState<DocumentItem | null>(null);

  const upload = async (file: File) => {
    setUploading(true);
    setError("");
    try {
      const doc = await uploadDocument(file);
      setDocument(doc);
      return doc;
    } catch (err) {
      setError(parseError(err));
      return null;
    } finally {
      setUploading(false);
    }
  };

  return {
    uploading,
    error,
    document,
    upload,
  };
}