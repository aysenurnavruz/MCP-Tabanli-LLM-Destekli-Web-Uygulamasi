import api from "../utils/axiosInstance";

export type PageResponse<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
};

export type Chat = {
  id: number;
  documentId: number | null;
  title: string | null;
  createdAt: string;
  updatedAt: string;
};

export type Message = {
  id: number;
  role: "USER" | "ASSISTANT";
  status: "CREATED" | "STREAMING" | "COMPLETED" | "FAILED";
  content: string;
  createdAt: string;
};

export type Citation = {
  chunkId: number;
  chunkIndex: number;
  pageStart: number | null;
  pageEnd: number | null;
  startOffset: number | null;
  endOffset: number | null;
  score: number;
  preview: string;
};

export type MessageWithCitations = Message & {
  citations?: Citation[];
};

export type SendMessageResponse = {
  userMessage: Message;
  assistantMessage: Message | null;
  citations: Citation[];
};

export async function listChats(page = 0, size = 20) {
  const { data } = await api.get<PageResponse<Chat>>("/api/chats", {
    params: { page, size, sort: "updatedAt,desc" },
  });
  return data;
}

export async function createChat(payload: { documentId?: number; title?: string }) {
  const { data } = await api.post<Chat>("/api/chats", payload);
  return data;
}

export async function deleteChat(chatId: number) {
  await api.delete(`/api/chats/${chatId}`);
}

export async function listMessages(chatId: number, page = 0, size = 50) {
  const { data } = await api.get<PageResponse<Message>>(`/api/chats/${chatId}/messages`, {
    params: { page, size, sort: "createdAt,asc" },
  });
  return data;
}

export async function sendMessage(
  chatId: number,
  payload: { content: string; clientMessageId?: string }
) {
  const { data } = await api.post<SendMessageResponse>(`/api/chats/${chatId}/messages`, payload);
  return data;
}