<script setup lang="ts">
import { ref } from 'vue';
import LoginForm from '@/components/login-form.vue';
import RegisterForm from '@/components/register-form.vue';

const showLogin = ref(true);
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>{{ showLogin ? 'Login' : 'Register' }}</h2>
      
      <LoginForm v-if="showLogin" />
      <RegisterForm v-else />
      
      <div class="auth-toggle">
        <button @click="showLogin = !showLogin" class="btn-link">
          {{ showLogin ? "Don't have an account? Register" : 'Already have an account? Login' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  margin-top: 4rem;
}

.auth-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 400px;
}

.auth-card h2 {
  text-align: center;
  margin-bottom: 1.5rem;
}

.auth-toggle {
  margin-top: 1.5rem;
  text-align: center;
}
</style>
