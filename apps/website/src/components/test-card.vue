<script setup lang="ts">
import type { Test } from '@/types/types';

defineProps<{
  test: Test;
}>();

const formatDate = (date: string) => {
  return new Intl.DateTimeFormat('en-GB', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(Number(date) * 1000))
};

const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60);
  return `${minutes} min`;
};
</script>

<template>
  <div class="test-card">
    <div class="test-card-header">
      <div>
        <h3>{{ test.title }}</h3>
        <p>{{ test.description }}</p>
      </div>
      <span :class="['badge', test.isActive ? 'badge-success' : 'badge-inactive']">
        {{ test.isActive ? 'Active' : 'Inactive' }}
      </span>
    </div>

    <div class="test-card-details">
      <div>
        <span class="label">Time Limit:</span>
        <span>{{ formatTime(test.timeLimit) }}</span>
      </div>
      <div>
        <span class="label">Max Attempts:</span>
        <span>{{ test.maxAttempts }}</span>
      </div>
      <div>
        <span class="label">Available:</span>
        <span>{{ formatDate(test.startTime) }} - {{ formatDate(test.endTime) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.test-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.test-card:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.test-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.test-card h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
}

.test-card p {
  margin: 0;
  color: #666;
}

.test-card-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  font-size: 0.9rem;
}

.label {
  color: #666;
  margin-right: 0.5rem;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 500;
}

.badge-success {
  background: #d4edda;
  color: #155724;
}

.badge-inactive {
  background: #e9e9e9;
  color: #666;
}
</style>