<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useTests } from '@/composables/useTest';
import TestCard from '@/components/test-card.vue';

const emit = defineEmits<{
  viewTest: [id: number];
}>();

const { tests, isLoading, error, pagination, fetchTests } = useTests();
const searchQuery = ref('');
const activeOnly = ref(false);

onMounted(() => {
  loadTests();
});

const loadTests = () => {
  fetchTests({
    page: pagination.value.page,
    search: searchQuery.value || '',
    active: activeOnly.value || '',
  });
};

const changePage = (page: number) => {
  fetchTests({
    page,
    search: searchQuery.value || undefined,
    active: activeOnly.value || undefined,
  });
};
</script>

<template>
  <div>
    <div class="filters">
      <input
        v-model="searchQuery"
        @input="loadTests"
        type="text"
        placeholder="Search tests..."
      />
      <label class="checkbox-label">
        <input v-model="activeOnly" @change="loadTests" type="checkbox" />
        Active only
      </label>
    </div>

    <div v-if="isLoading" class="loading">Loading tests...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="tests.length === 0" class="empty">No tests found</div>

    <div v-else class="tests-list">
      <TestCard
        v-for="test in tests"
        :key="test.id"
        :test="test"
        @click="emit('viewTest', test.id)"
      />
    </div>

    <div v-if="pagination.totalPages > 1" class="pagination">
      <button
        v-for="page in pagination.totalPages"
        :key="page"
        @click="changePage(page)"
        :class="{ active: page === pagination.page }"
      >
        {{ page }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.filters input[type="text"] {
  flex: 1;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: white;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.loading, .empty {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.tests-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 2rem;
}

.pagination button {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
}

.pagination button:hover {
  background: #f5f5f5;
}

.pagination button.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}
</style>
