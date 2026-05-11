import { z } from "zod";

export const documentUploadSchema = z.object({
  file: z
    .instanceof(File, { message: "Dosya seçmelisin" })
    .refine((file) => file.type === "application/pdf", {
      message: "Sadece PDF dosyası yükleyebilirsin",
    })
    .refine((file) => file.size <= 20 * 1024 * 1024, {
      message: "Dosya boyutu en fazla 20 MB olabilir", //burayı elife sor bakalım 20 mb sınırladık ama belki gerek yoktur 
    }),
});

export const documentSearchSchema = z.object({
  query: z.string().trim().max(100, "Arama çok uzun").optional(),
});

export type DocumentUploadValues = z.infer<typeof documentUploadSchema>;
export type DocumentSearchValues = z.infer<typeof documentSearchSchema>;