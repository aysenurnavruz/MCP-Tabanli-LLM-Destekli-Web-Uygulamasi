export const queryKeys = {
  auth: ["auth"] as const,
  chats: ["chats"] as const,
  messages: (chatId: number | null) => ["messages", chatId ?? "none"] as const,
  documents: ["documents"] as const,
};