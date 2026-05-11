import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  loginApi,
  logoutApi,
  registerApi,
  type LoginRequest,
  type RegisterRequest,
} from "../api/auth";
import { getApiErrorMessage } from "../utils/apiError";
import { setTokens } from "../store/authStore";

export function useAuth() {
  const queryClient = useQueryClient();

  const loginMutation = useMutation({
    mutationFn: loginApi,
    onSuccess: (res) => {
      setTokens(res.accessToken, res.refreshToken);
    },
  });

  const registerMutation = useMutation({
    mutationFn: registerApi,
    onSuccess: (res) => {
      setTokens(res.accessToken, res.refreshToken);
    },
  });

  const logoutMutation = useMutation({
    mutationFn: logoutApi,
    onSuccess: () => {
      queryClient.clear();
    },
  });

  const login = async (payload: LoginRequest) => {
    loginMutation.reset();
    try {
      await loginMutation.mutateAsync(payload);
      return true;
    } catch {
      return false;
    }
  };

  const register = async (payload: RegisterRequest) => {
    registerMutation.reset();
    try {
      await registerMutation.mutateAsync(payload);
      return true;
    } catch {
      return false;
    }
  };

  const logout = async () => {
    logoutMutation.reset();
    await logoutMutation.mutateAsync();
  };

  return {
    loading:
      loginMutation.isPending ||
      registerMutation.isPending ||
      logoutMutation.isPending,
    error:
      getApiErrorMessage(loginMutation.error, "Beklenmeyen bir hata oluştu.", {
        unauthorizedMessage: "E-posta veya şifre hatalı.",
      }) ||
      getApiErrorMessage(registerMutation.error, "Beklenmeyen bir hata oluştu.") ||
      getApiErrorMessage(logoutMutation.error, "Beklenmeyen bir hata oluştu."),
    login,
    register,
    logout,
  };
}