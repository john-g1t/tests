import { api } from './client';
import type { User, PaginatedResponse } from '@/types/types';

export const usersApi = {
  register: (data: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }) => api.post<{ userId: number }>('/users/register', data),

  login: (data: { email: string; password: string }) =>
    api.post<User>('/users/login', data),

  logout: () => api.post<null>('/users/logout'),

  getCurrentUser: () => api.get<User>('/users/me'),

  getAllUsers: (params?: {
    page?: number;
    limit?: number;
    search?: string;
  }) => {
    const query = new URLSearchParams(
      params as Record<string, string>
    ).toString();
    return api.get<{ users: User[] } & PaginatedResponse<User>>(
      `/users${query ? `?${query}` : ''}`
    );
  },

  getUserById: (id: number) => api.get<User>(`/users/${id}`),

  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    api.put<null>('/users/password', data),
};
