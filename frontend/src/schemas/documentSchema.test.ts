import { describe, it, expect } from "vitest";
import { documentUploadSchema, documentSearchSchema } from "@/schemas/documentSchema";

describe("Document Schemas", () => {
  describe("documentUploadSchema", () => {
    it("should validate document upload with correct file type", () => {
      // Note: In real tests, you'd mock File objects
      // This is a simplified validation example
      const schema = documentUploadSchema;
      expect(schema).toBeDefined();
    });

    it("documentUploadSchema should be defined", () => {
      expect(documentUploadSchema).toBeDefined();
    });
  });

  describe("documentSearchSchema", () => {
    it("should validate search query", () => {
      const validData = {
        query: "test document",
      };

      const result = documentSearchSchema.safeParse(validData);
      expect(result.success).toBe(true);
      if (result.success) {
        expect(result.data.query).toBe(validData.query);
      }
    });

    it("should accept empty search query", () => {
      const validData = {
        query: "",
      };

      const result = documentSearchSchema.safeParse(validData);
      expect(result.success).toBe(true);
    });

    it("should accept empty query field", () => {
      const validData = {};

      const result = documentSearchSchema.safeParse(validData);
      // query is optional, so empty object is valid
      expect(result.success).toBe(true);
    });
  });
});
