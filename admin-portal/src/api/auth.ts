import { apiClient, ApiEnvelope } from './client';

export type Role = 'SUPER_ADMIN' | 'SHOP_OWNER' | 'STAFF' | 'DELIVERY_BOY' | 'CUSTOMER';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  fullName: string;
  role: Role;
  tenantId: number | null;
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const response = await apiClient.post<ApiEnvelope<AuthResponse>>('/auth/login', { email, password });
  return response.data.data;
}

export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post('/auth/logout', { refreshToken });
}
