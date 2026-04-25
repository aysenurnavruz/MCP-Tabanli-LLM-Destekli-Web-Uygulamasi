import api from "../utils/axiosInstance";
import type { PageResponse } from "./chats";

export type DocumentItem = {
  id: number;
  originalFilename: string;
  contentType: string | null;
  sizeBytes: number;
  createdAt: string;
};

export async function uploadDocument(file: File) {
  const form = new FormData();
  form.append("file", file);

  const { data } = await api.post<DocumentItem>("/api/documents/upload", form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

export async function listDocuments(page = 0, size = 20) {
  const { data } = await api.get<PageResponse<DocumentItem>>("/api/documents", {
    params: { page, size },
  });
  return data;
}