import { api } from './client';
import type { Test, TestStatistics, PaginatedResponse } from '@/types/types';

export const testsApi = {
  createTest: (data: {
    title: string;
    description: string;
    timeLimit: number;
    maxAttempts: number;
    startTime: string;
    endTime: string;
  }) => api.post<{ testId: number }>('/tests', data),

  getAllTests: (params?: {
    page?: number;
    limit?: number;
    active?: boolean;
    creatorId?: number;
    search?: string;
  }) => {
    const query = new URLSearchParams(
      params as Record<string, string>
    ).toString();
    return api.get<{ tests: Test[] } & PaginatedResponse<Test>>(
      `/tests${query ? `?${query}` : ''}`
    );
  },

  getTestById: (id: number) => api.get<Test>(`/tests/${id}`),

  updateTest: (
    id: number,
    data: {
      title?: string;
      description?: string;
      timeLimit?: number;
      maxAttempts?: number;
      startTime?: string;
      endTime?: string;
    }
  ) => api.put<null>(`/tests/${id}`, data),

  deactivateTest: (id: number) => api.delete<null>(`/tests/${id}`),

  getTestStatistics: (id: number) =>
    api.get<TestStatistics>(`/tests/${id}/statistics`),
};
