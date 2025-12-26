<!-- components/AppHeader.vue -->
<script setup lang="ts">
import { useAuth } from '../composables/useAuth';

const { currentUser, logout } = useAuth();

const handleLogout = async () => {
  await logout();
};
</script>

<template>
  <header class="header">
    <div class="header-container">
      <h1>Test Platform</h1>
      <div class="header-user">
        <span>{{ currentUser?.firstName }} {{ currentUser?.lastName }}</span>
        <button @click="handleLogout" class="btn btn-danger">Logout</button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 1rem;
}
</style>
