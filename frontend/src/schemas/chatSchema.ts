import { z } from "zod";

export const chatMessageSchema = z.object({
  chatId: z.number().int().positive("Geçerli bir sohbet seçin"),
  content: z
    .string()
    .trim()
    .min(1, "Mesaj boş olamaz")
    .max(4000, "Mesaj çok uzun"),
});

export const createChatSchema = z.object({
  documentId: z.number().int().positive("Geçerli bir doküman seçin"),
  title: z
    .string()
    .trim()
    .min(1, "Başlık zorunlu")
    .max(120, "Başlık çok uzun")
    .optional(),
});

export const chatSearchSchema = z.object({
  query: z.string().trim().max(100, "Arama çok uzun").optional(),
});

export type ChatMessageValues = z.infer<typeof chatMessageSchema>;
export type CreateChatValues = z.infer<typeof createChatSchema>;
export type ChatSearchValues = z.infer<typeof chatSearchSchema>;