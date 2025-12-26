<script setup lang="ts">
import { ref } from 'vue';
import { useAuth } from '@/composables/useAuth';

const { login, error, isLoading } = useAuth();

const email = ref('');
const password = ref('');

const handleSubmit = async () => {
  try {
    await login(email.value, password.value);
  } catch (e) {
    // Error handled by composable
  }
};
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-4">
    <div>
      <label>Email</label>
      <input
        v-model="email"
        type="text"
        required
      />
    </div>
    
    <div>
      <label>Password</label>
      <input
        v-model="password"
        type="password"
        required
      />
    </div>
    
    <div v-if="error" class="text-red-600 text-sm">
      {{ error }}
    </div>
    
    <button
      type="submit"
      :disabled="isLoading"
      @click="handleSubmit"
    >
      {{ isLoading ? 'Loading...' : 'Login' }}
    </button>
  </form>
</template>
