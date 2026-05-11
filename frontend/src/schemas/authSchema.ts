import { z } from "zod";

const emailSchema = z
  .string()
  .trim()
  .min(1, "E-posta zorunlu")
  .email("Geçerli bir e-posta girin");

const passwordSchema = z
  .string()
  .min(1, "Şifre zorunlu")
  .min(6, "Şifre en az 6 karakter olmalı");

export const loginSchema = z.object({
  email: emailSchema,
  password: passwordSchema,
});

export const registerSchema = z.object({
  email: emailSchema,
  password: passwordSchema,
});

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegisterFormValues = z.infer<typeof registerSchema>;