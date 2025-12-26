import { api } from './client';
import type { UserStatistics } from '@/types/types';

export const statisticsApi = {
  getGlobalStatistics: () =>
    api.get<{
      totalUsers: number;
      totalTests: number;
      totalAttempts: number;
      completedAttempts: number;
      averageScore: number;
      mostPopularTests: Array<{
        testId: number;
        title: string;
        attemptCount: number;
      }>;
    }>('/statistics/global'),

  getUserStatistics: (userId: number) =>
    api.get<UserStatistics>(`/statistics/user/${userId}`),
};
