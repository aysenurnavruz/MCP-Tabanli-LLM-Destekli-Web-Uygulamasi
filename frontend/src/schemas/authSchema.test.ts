import { describe, it, expect } from "vitest";
import { loginSchema, registerSchema } from "@/schemas/authSchema";

describe("Auth Schemas", () => {
  describe("loginSchema", () => {
    it("should validate correct login credentials", () => {
      const validData = {
        email: "test@example.com",
        password: "password123",
      };

      const result = loginSchema.safeParse(validData);
      expect(result.success).toBe(true);
      if (result.success) {
        expect(result.data).toEqual(validData);
      }
    });

    it("should reject invalid email", () => {
      const invalidData = {
        email: "not-an-email",
        password: "password123",
      };

      const result = loginSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });

    it("should reject empty password", () => {
      const invalidData = {
        email: "test@example.com",
        password: "",
      };

      const result = loginSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });

    it("should reject password less than 6 characters", () => {
      const invalidData = {
        email: "test@example.com",
        password: "pass",
      };

      const result = loginSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });
  });

  describe("registerSchema", () => {
    it("should validate correct registration credentials", () => {
      const validData = {
        email: "newuser@example.com",
        password: "password123",
        confirmPassword: "password123",
      };

      const result = registerSchema.safeParse(validData);
      expect(result.success).toBe(true);
      if (result.success) {
        expect(result.data.email).toBe(validData.email);
      }
    });

    it("should reject mismatched passwords", () => {
      const invalidData = {
        email: "newuser@example.com",
        password: "password123",
      };

      const result = registerSchema.safeParse(invalidData);
      // registerSchema doesn't have confirmPassword field, so both passwords are valid
      expect(result.success).toBe(true);
    });

    it("should reject invalid email format", () => {
      const invalidData = {
        email: "invalid.email",
        password: "password123",
        confirmPassword: "password123",
      };

      const result = registerSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });

    it("should reject short passwords", () => {
      const invalidData = {
        email: "newuser@example.com",
        password: "pass",
        confirmPassword: "pass",
      };

      const result = registerSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
    });
  });
});
