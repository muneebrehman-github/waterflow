import { apiClient, ApiEnvelope } from './client';

export type ShopStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'REJECTED' | 'SUSPENDED' | 'TERMINATED';

export interface Shop {
  id: number;
  name: string;
  description: string | null;
  ownerName: string;
  email: string;
  phone: string;
  address: string | null;
  status: ShopStatus;
  rejectionReason: string | null;
  suspensionReason: string | null;
  createdAt: string;
}

export async function listShops(status?: ShopStatus): Promise<Shop[]> {
  const response = await apiClient.get<ApiEnvelope<Shop[]>>('/admin/shops', {
    params: status ? { status } : undefined,
  });
  return response.data.data;
}

export async function approveShop(id: number): Promise<Shop> {
  const response = await apiClient.post<ApiEnvelope<Shop>>(`/admin/shops/${id}/approve`);
  return response.data.data;
}

export async function rejectShop(id: number, reason: string): Promise<Shop> {
  const response = await apiClient.post<ApiEnvelope<Shop>>(`/admin/shops/${id}/reject`, { reason });
  return response.data.data;
}

export async function suspendShop(id: number, reason: string): Promise<Shop> {
  const response = await apiClient.post<ApiEnvelope<Shop>>(`/admin/shops/${id}/suspend`, { reason });
  return response.data.data;
}

export async function reinstateShop(id: number): Promise<Shop> {
  const response = await apiClient.post<ApiEnvelope<Shop>>(`/admin/shops/${id}/reinstate`);
  return response.data.data;
}
