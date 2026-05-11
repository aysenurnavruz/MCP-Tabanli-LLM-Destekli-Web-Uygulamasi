import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { uploadDocument, type DocumentItem } from "../api/documents";
import { getApiErrorMessage } from "../utils/apiError";
import { queryKeys } from "../lib/queryKeys";

export function useUpload() {
  const queryClient = useQueryClient();
  const [document, setDocument] = useState<DocumentItem | null>(null);

  const uploadMutation = useMutation({
    mutationFn: uploadDocument,
    onSuccess: (doc) => {
      setDocument(doc);
      queryClient.invalidateQueries({ queryKey: queryKeys.documents });
    },
  });

  const upload = async (file: File) => {
    uploadMutation.reset();

    try {
      const doc = await uploadMutation.mutateAsync(file);
      return doc;
    } catch {
      return null;
    }
  };

  return {
    uploading: uploadMutation.isPending,
    error: getApiErrorMessage(uploadMutation.error, "Dosya yüklenemedi."),
    document,
    upload,
  };
}