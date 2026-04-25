import api from "../utils/axiosInstance";

export type ChangePasswordRequest = {
  currentPassword: string;
  newPassword: string;
};

export async function getCurrentUserEmail() {
  const { data } = await api.get<string>("/api/test/me");
  return data;
}

export async function changePassword(payload: ChangePasswordRequest) {
  const { data } = await api.post("/api/auth/change-password", payload);
  return data;
}
