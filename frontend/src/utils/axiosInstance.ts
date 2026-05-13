import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from "@/store/authStore";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;
    const isTokenEndpoint = originalRequest?.url
      ? ["/api/auth/login", "/api/auth/register", "/api/auth/logout", "/api/auth/refresh"].some((path) =>
          originalRequest.url?.startsWith(path)
        )
      : false;

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isTokenEndpoint) {
      const refreshToken = getRefreshToken();

      if (refreshToken) {
        try {
          originalRequest._retry = true;

          const { data } = await axios.post<{ accessToken: string; refreshToken: string }>(
            `${API_BASE_URL}/api/auth/refresh`,
            { refreshToken }
          );

          setTokens(data.accessToken, data.refreshToken);
          originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
          return api(originalRequest);
        } catch {
          clearTokens();
        }
      } else {
        clearTokens();
      }

      if (!window.location.pathname.includes("/login")) {
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default api;
