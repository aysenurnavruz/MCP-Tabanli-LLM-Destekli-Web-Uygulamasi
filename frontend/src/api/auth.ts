import api from "../utils/axiosInstance";
import { clearTokens, getRefreshToken } from "../store/authStore";

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  email: string;
  password: string;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
};

export async function loginApi(payload: LoginRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/api/auth/login", payload);
  return data;
}

export async function registerApi(payload: RegisterRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/api/auth/register", payload);
  return data;
}

export async function refreshApi(refreshToken: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/api/auth/refresh", { refreshToken });
  return data;
}

export async function logoutApi(): Promise<void> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    clearTokens();
    return;
  }

  try {
    await api.post("/api/auth/logout", { refreshToken });
  } finally {
    clearTokens();
  }
}
