import { ref } from 'vue';
import { testsApi } from '@/api/test';
import type { Test, TestStatistics } from '@/types/types';

export function useTests() {
  const tests = ref<Test[]>([]);
  const currentTest = ref<Test | null>(null);
  const statistics = ref<TestStatistics | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const pagination = ref({
    total: 0,
    page: 1,
    limit: 20,
    totalPages: 0,
  });

  const fetchTests = async (params?: {
    page?: number;
    limit?: number;
    active?: boolean;
    creatorId?: number;
    search?: string;
  }) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await testsApi.getAllTests(params);
      tests.value = response.tests;
      pagination.value = {
        total: response.total,
        page: response.page,
        limit: response.limit,
        totalPages: response.totalPages,
      };
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchTestById = async (id: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      const test = await testsApi.getTestById(id);
      currentTest.value = test;
      return test;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const createTest = async (data: {
    title: string;
    description: string;
    timeLimit: number;
    maxAttempts: number;
    startTime: string;
    endTime: string;
  }) => {
    isLoading.value = true;
    error.value = null;
    try {
      const result = await testsApi.createTest(data);
      return result.testId;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const updateTest = async (id: number, data: Partial<Test>) => {
    isLoading.value = true;
    error.value = null;
    try {
      await testsApi.updateTest(id, data);
      if (currentTest.value?.id === id) {
        currentTest.value = { ...currentTest.value, ...data };
      }
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const deactivateTest = async (id: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      await testsApi.deactivateTest(id);
      tests.value = tests.value.filter((t: Test) => t.id !== id);
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchTestStatistics = async (id: number) => {
    isLoading.value = true;
    error.value = null;
    try {
      const stats = await testsApi.getTestStatistics(id);
      statistics.value = stats;
      return stats;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  return {
    tests,
    currentTest,
    statistics,
    isLoading,
    error,
    pagination,
    fetchTests,
    fetchTestById,
    createTest,
    updateTest,
    deactivateTest,
    fetchTestStatistics,
  };
}
