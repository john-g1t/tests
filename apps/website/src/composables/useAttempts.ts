import { ref, computed } from 'vue';
import { attemptsApi } from '@/api/attempt';
import type {
  TestAttempt,
  AttemptProgress,
  AttemptAnswer,
} from '@/types/types';

export function useAttempts() {
  /* -------------------- state -------------------- */

  const attempts = ref<TestAttempt[]>([]);
  const currentAttempt = ref<AttemptProgress | null>(null);
  const attemptDetails = ref<{
    attempt: TestAttempt;
    answers: AttemptAnswer[];
  } | null>(null);

  const isLoading = ref(false);
  const error = ref<string | null>(null);

  /* -------------------- guards -------------------- */

  let starting = false;
  let submitting = false;
  let finishing = false;

  /* -------------------- computed -------------------- */

  const timeRemaining = computed(
    () => currentAttempt.value?.timeRemaining ?? 0
  );

  /* -------------------- actions -------------------- */

  const startAttempt = async (testId: number): Promise<number> => {
    if (starting) throw new Error('Attempt already starting');
    starting = true;
    isLoading.value = true;
    error.value = null;

    try {
      const res = await attemptsApi.startAttempt(testId);
      return res.attemptId;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      starting = false;
      isLoading.value = false;
    }
  };

  const fetchAttemptProgress = async (
    attemptId: number
  ): Promise<AttemptProgress> => {
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
  ): Promise<void> => {
    if (submitting) return;
    submitting = true;
    isLoading.value = true;
    error.value = null;

    try {
      await attemptsApi.submitAnswer(attemptId, data);
      await fetchAttemptProgress(attemptId);
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      submitting = false;
      isLoading.value = false;
    }
  };

  const finishAttempt = async (
    attemptId: number
  ): Promise<{
    score: number;
    maxScore: number;
    percentage: number;
    passed: boolean;
  }> => {
    if (finishing) throw new Error('Attempt already finishing');
    finishing = true;
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
      finishing = false;
      isLoading.value = false;
    }
  };

  const fetchUserAttempts = async (
    userId: number,
    params?: { page?: number; limit?: number; testId?: number }
  ): Promise<void> => {
    isLoading.value = true;
    error.value = null;

    try {
      const res = await attemptsApi.getUserAttempts(userId, params);
      attempts.value = res.attempts;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchAttemptDetails = async (
    attemptId: number
  ): Promise<{
    attempt: TestAttempt;
    answers: AttemptAnswer[];
  }> => {
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

  /* -------------------- exports -------------------- */

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
