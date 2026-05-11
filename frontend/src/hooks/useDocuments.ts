import { useCallback } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { deleteDocument, listDocuments } from "../api/documents";
import { getApiErrorMessage } from "../utils/apiError";
import { queryKeys } from "../lib/queryKeys";

export function useDocuments() {
  const queryClient = useQueryClient();

  const documentsQuery = useQuery({
    queryKey: queryKeys.documents,
    queryFn: () => listDocuments(),
  });

  const deleteDocumentMutation = useMutation({
    mutationFn: deleteDocument,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.documents });
      queryClient.invalidateQueries({ queryKey: queryKeys.chats });
    },
  });

  const refreshDocuments = useCallback(async () => {
    await documentsQuery.refetch();
  }, [documentsQuery]);

  const removeDocument = useCallback(
    async (documentId: number) => {
      deleteDocumentMutation.reset();
      try {
        await deleteDocumentMutation.mutateAsync(documentId);
        return true;
      } catch {
        return false;
      }
    },
    [deleteDocumentMutation]
  );

  return {
    documents: documentsQuery.data?.content ?? [],
    loading: documentsQuery.isLoading || documentsQuery.isFetching,
    error:
      getApiErrorMessage(documentsQuery.error, "Dokümanlar yüklenemedi.") ||
      getApiErrorMessage(deleteDocumentMutation.error, "Doküman silinemedi."),
    refreshDocuments,
    removeDocument,
  };
}