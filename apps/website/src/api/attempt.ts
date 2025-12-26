import { api } from './client';
import type {
  TestAttempt,
  AttemptProgress,
  AttemptAnswer,
  PaginatedResponse,
} from '@/types/types';

export const attemptsApi = {
  startAttempt: (testId: number) =>
    api.post<{ attemptId: number; startTime: string; timeLimit: number }>(
      '/attempts/start',
      { testId }
    ),

  submitAnswer: (
    attemptId: number,
    data:
      | { questionId: number; answerId: number }
      | { questionId: number; answerText: string }
  ) => api.post<null>(`/attempts/${attemptId}/answers`, data),

  getAttemptProgress: (attemptId: number) =>
    api.get<AttemptProgress>(`/attempts/${attemptId}`),

  finishAttempt: (attemptId: number) =>
    api.post<{
      score: number;
      maxScore: number;
      percentage: number;
      passed: boolean;
    }>(`/attempts/${attemptId}/finish`),

  getUserAttempts: (
    userId: number,
    params?: { page?: number; limit?: number; testId?: number }
  ) => {
    const query = new URLSearchParams(
      params as Record<string, string>
    ).toString();
    return api.get<{ attempts: TestAttempt[] } & PaginatedResponse<TestAttempt>>(
      `/attempts/user/${userId}${query ? `?${query}` : ''}`
    );
  },

  getAttemptDetails: (attemptId: number) =>
    api.get<{ attempt: TestAttempt; answers: AttemptAnswer[] }>(
      `/attempts/${attemptId}/details`
    ),
};
