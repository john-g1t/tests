<script setup lang="ts">
import { ref } from 'vue';
import { useTests } from '@/composables/useTest';

const emit = defineEmits<{
  created: [];
}>();

const { createTest, isLoading, error } = useTests();

const form = ref({
  title: '',
  description: '',
  timeLimit: 60,
  maxAttempts: 3,
  startTime: '',
  endTime: '',
});

const handleSubmit = async () => {
  try {
    await createTest({
      ...form.value,
      timeLimit: form.value.timeLimit * 60,
      startTime: new Date(form.value.startTime).toISOString(),
      endTime: new Date(form.value.endTime).toISOString(),
    });
    emit('created');
  } catch (e) {
    // Error handled by composable
  }
};
</script>

<template>
  <div class="form-container">
    <h2>Create New Test</h2>

    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>Title</label>
        <input v-model="form.title" type="text" required />
      </div>

      <div class="form-group">
        <label>Description</label>
        <textarea v-model="form.description" required rows="4" />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Time Limit (minutes)</label>
          <input v-model.number="form.timeLimit" type="number" required min="1" />
        </div>

        <div class="form-group">
          <label>Max Attempts</label>
          <input v-model.number="form.maxAttempts" type="number" required min="1" />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Start Time</label>
          <input v-model="form.startTime" type="datetime-local" required />
        </div>

        <div class="form-group">
          <label>End Time</label>
          <input v-model="form.endTime" type="datetime-local" required />
        </div>
      </div>

      <div v-if="error" class="error">{{ error }}</div>

      <button type="submit" :disabled="isLoading" class="btn btn-primary btn-block">
        {{ isLoading ? 'Creating...' : 'Create Test' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.form-container {
  max-width: 700px;
  margin: 0 auto;
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-container h2 {
  margin-bottom: 1.5rem;
}
</style>
