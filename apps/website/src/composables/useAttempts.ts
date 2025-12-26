import { ref, computed } from 'vue';
import { attemptsApi } from '@/api/attempt';
import type { TestAttempt, AttemptProgress, AttemptAnswer } from '@/types/types';

export function useAttempts() {
  const attempts = ref<TestAttempt[]>([]);
  const currentAttempt = ref<AttemptProgress | null>(null);
  const attemptDetails = ref<{
    attempt: TestAttempt;
    answers: AttemptAnswer[];
  } | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  const timeRemaining = computed(() => currentAttempt.value?.timeRemaining || 0);

  const startAttempt = async (testId: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      const result = await attemptsApi.startAttempt(testId);
      return result.attemptId;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchAttemptProgress = async (attemptId: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      const progress = await attemptsApi.getAttemptProgress(attemptId);
      currentAttempt.value = progress;
      return progress;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const submitAnswer = async (
    attemptId: number,
    data:
      | { questionId: number; answerId: number }
      | { questionId: number; answerText: string }
  ) => {
    isLoading.value = true;
    error.value = null;
    try {
      await attemptsApi.submitAnswer(attemptId, data);
      await fetchAttemptProgress(attemptId);
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const finishAttempt = async (attemptId: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      const result = await attemptsApi.finishAttempt(attemptId);
      currentAttempt.value = null;
      return result;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchUserAttempts = async (
    userId: number,
    params?: { page?: number; limit?: number; testId?: number }
  ) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await attemptsApi.getUserAttempts(userId, params);
      attempts.value = response.attempts;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchAttemptDetails = async (attemptId: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      const details = await attemptsApi.getAttemptDetails(attemptId);
      attemptDetails.value = details;
      return details;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  return {
    attempts,
    currentAttempt,
    attemptDetails,
    timeRemaining,
    isLoading,
    error,
    startAttempt,
    fetchAttemptProgress,
    submitAnswer,
    finishAttempt,
    fetchUserAttempts,
    fetchAttemptDetails,
  };
}
