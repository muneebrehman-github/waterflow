import axios from 'axios';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081/api/v1';

export const apiClient = axios.create({ baseURL: API_BASE_URL });

apiClient.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem('spgg_access_token');
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

export interface ApiEnvelope<T> {
  success: boolean;
  message: string | null;
  data: T;
  timestamp: string;
}

export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiEnvelope<unknown> | undefined;
    if (data?.message) return data.message;
    return error.message;
  }
  return 'Something went wrong. Please try again.';
}
