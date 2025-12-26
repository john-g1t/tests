<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuth } from './composables/useAuth';
import AppHeader from '@/components/app-header.vue';
import AuthPage from '@/components/auth-page.vue';
import Dashboard from '@/components/dashboard-view.vue';

const { isAuthenticated, fetchCurrentUser } = useAuth();

onMounted(async () => {
  await fetchCurrentUser();
});
</script>

<template>
  <div class="app">
    <AppHeader v-if="isAuthenticated" />
    <main class="container">
      <AuthPage v-if="!isAuthenticated" />
      <Dashboard v-else />
    </main>
  </div>
</template>

<style>
.app {
  min-height: 100vh;
  background: #f5f5f5;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}
</style>
