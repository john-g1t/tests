import { computed, ref } from 'vue';
import { usersApi } from '@/api/user';
import type { User } from '@/types/types';

const currentUser = ref<User | null>(null);
const isLoading = ref(false);
const error = ref<string | null>(null);

export function useAuth() {
  const isAuthenticated = computed(() => currentUser.value !== null);
  const isAdmin = computed(() => currentUser.value?.role === 'ADMIN');

  const login = async (email: string, password: string) => {
    isLoading.value = true;
    error.value = null;
    try {
      const user = await usersApi.login({ email, password });
      currentUser.value = user;
      return user;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const register = async (data: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }) => {
    isLoading.value = true;
    error.value = null;
    try {
      await usersApi.register(data);
      return await login(data.email, data.password);
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const logout = async () => {
    isLoading.value = true;
    try {
      await usersApi.logout();
      currentUser.value = null;
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchCurrentUser = async () => {
    isLoading.value = true;
    try {
      const user = await usersApi.getCurrentUser();
      currentUser.value = user;
      return user;
    } catch (e) {
      currentUser.value = null;
      return null;
    } finally {
      isLoading.value = false;
    }
  };

  const changePassword = async (oldPassword: string, newPassword: string) => {
    isLoading.value = true;
    error.value = null;
    try {
      await usersApi.changePassword({ oldPassword, newPassword });
    } catch (e) {
      error.value = (e as Error).message;
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  return {
    currentUser: computed(() => currentUser.value),
    isAuthenticated,
    isAdmin,
    isLoading: computed(() => isLoading.value),
    error: computed(() => error.value),
    login,
    register,
    logout,
    fetchCurrentUser,
    changePassword,
  };
}
