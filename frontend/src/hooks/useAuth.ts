import { useState } from "react";
import { loginApi, logoutApi, registerApi, type LoginRequest, type RegisterRequest } from "../api/auth";
import { setTokens } from "../store/authStore";

function getErrorMessage(err: unknown): string {
  const maybe = err as {
    response?: { data?: { message?: string } };
    message?: string;
  };
  return (
    maybe?.response?.data?.message ||
    maybe?.message ||
    "Beklenmeyen bir hata oluştu."
  );
}

export function useAuth() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const login = async (payload: LoginRequest) => {
    setLoading(true);
    setError("");
    try {
      const res = await loginApi(payload);
      setTokens(res.accessToken, res.refreshToken);
      return true;
    } catch (err) {
      setError(getErrorMessage(err));
      return false;
    } finally {
      setLoading(false);
    }
  };

  const register = async (payload: RegisterRequest) => {
    setLoading(true);
    setError("");
    try {
      const res = await registerApi(payload);
      setTokens(res.accessToken, res.refreshToken);
      return true;
    } catch (err) {
      setError(getErrorMessage(err));
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    await logoutApi();
  };

  return {
    loading,
    error,
    login,
    register,
    logout,
  };
}
