import { api } from './client';
import type { Question, AnswerOption } from '@/types/types';

export const questionsApi = {
  addQuestion: (
    testId: number,
    data: {
      text: string;
      answerType: 'MULTIPLE_CHOICE' | 'TEXT' | 'NUMERIC';
      maxPoints: number;
    }
  ) => api.post<{ questionId: number }>(`/tests/${testId}/questions`, data),

  getQuestions: (testId: number) =>
    api.get<{ questions: Question[] }>(`/tests/${testId}/questions`),

  updateQuestion: (
    id: number,
    data: {
      text?: string;
      answerType?: 'MULTIPLE_CHOICE' | 'TEXT' | 'NUMERIC';
      maxPoints?: number;
    }
  ) => api.put<null>(`/questions/${id}`, data),

  deleteQuestion: (id: number) => api.delete<null>(`/questions/${id}`),

  addAnswerOption: (
    questionId: number,
    data: { optionText: string; score: number }
  ) => api.post<{ optionId: number }>(`/questions/${questionId}/options`, data),

  getAnswerOptions: (questionId: number) =>
    api.get<{ options: AnswerOption[] }>(`/questions/${questionId}/options`),

  updateAnswerOption: (
    id: number,
    data: { optionText?: string; score?: number }
  ) => api.put<null>(`/options/${id}`, data),

  deleteAnswerOption: (id: number) => api.delete<null>(`/options/${id}`),
};
