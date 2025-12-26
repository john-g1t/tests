<script setup lang="ts">
import { ref } from 'vue';
import TestsList from '@/components/tests-list.vue';
import CreateTestForm from '@/components/create-test-form.vue';
import TestDetails from '@/components/test-details.vue';
import UserAttempts from '@/components/user-attempts.vue';

type View = 'tests' | 'create-test' | 'test-details' | 'my-attempts';

const currentView = ref<View>('tests');
const selectedTestId = ref<number | null>(null);

const showView = (view: View, testId?: number) => {
  currentView.value = view;
  if (testId) {
    selectedTestId.value = testId;
  }
};
</script>

<template>
  <div>
    <nav class="nav-tabs">
      <button
        @click="showView('tests')"
        :class="{ active: currentView === 'tests' }"
      >
        All Tests
      </button>
      <button
        @click="showView('create-test')"
        :class="{ active: currentView === 'create-test' }"
      >
        Create Test
      </button>
      <button
        @click="showView('my-attempts')"
        :class="{ active: currentView === 'my-attempts' }"
      >
        My Attempts
      </button>
    </nav>

    <TestsList
      v-if="currentView === 'tests'"
      @view-test="(id) => showView('test-details', id)"
    />
    
    <CreateTestForm
      v-if="currentView === 'create-test'"
      @created="showView('tests')"
    />
    
    <TestDetails
      v-if="currentView === 'test-details' && selectedTestId"
      :test-id="selectedTestId"
      @back="showView('tests')"
    />
    
    <UserAttempts v-if="currentView === 'my-attempts'" />
  </div>
</template>

<style scoped>
.nav-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
}

.nav-tabs button {
  padding: 0.75rem 1.5rem;
  border: none;
  background: white;
  cursor: pointer;
  border-radius: 4px;
  font-size: 1rem;
  transition: background 0.2s;
}

.nav-tabs button:hover {
  background: #e9e9e9;
}

.nav-tabs button.active {
  background: #007bff;
  color: white;
}
</style>
