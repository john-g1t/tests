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
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.container {
  width: 95%;
  padding: 2rem 1rem;
  flex: 1;
  overflow: scroll;
  align-self: center;
}
</style>
