import { describe, it, expect } from "vitest";
import { chatMessageSchema, createChatSchema, chatSearchSchema } from "@/schemas/chatSchema";

describe("Chat Schemas", () => {
  describe("chatMessageSchema", () => {
    it("should validate correct chat message", () => {
      const validData = {
        chatId: 1,
        content: "Hello, this is a test message",
      };

      const result = chatMessageSchema.safeParse(validData);
      expect(result.success).toBe(true);
      if (result.success) {
        expect(result.data.content).toBe(validData.content);
      }
    });

    it("should reject empty message", () => {
      const invalidData = {
        chatId: 1,
        content: "",
      };

      const result = chatMessageSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });

    it("should reject missing content", () => {
      const invalidData = {
        chatId: 1,
      };

      const result = chatMessageSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });
  });

  describe("createChatSchema", () => {
    it("should reject chat creation without documentId", () => {
      const invalidData = {};

      const result = createChatSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });

    it("should validate chat creation with documentId", () => {
      const validData = {
        documentId: 123,
      };

      const result = createChatSchema.safeParse(validData);
      expect(result.success).toBe(true);
      if (result.success) {
        expect(result.data.documentId).toBe(123);
      }
    });

    it("should reject non-numeric documentId", () => {
      const invalidData = {
        documentId: "not-a-number",
      };

      const result = createChatSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });
  });

  describe("chatSearchSchema", () => {
    it("should validate search query", () => {
      const validData = {
        query: "test search",
      };

      const result = chatSearchSchema.safeParse(validData);
      expect(result.success).toBe(true);
      if (result.success) {
        expect(result.data.query).toBe(validData.query);
      }
    });

    it("should accept empty search query", () => {
      const validData = {
        query: "",
      };

      const result = chatSearchSchema.safeParse(validData);
      expect(result.success).toBe(true);
    });
  });
});
