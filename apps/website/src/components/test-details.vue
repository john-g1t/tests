<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useTests } from '@/composables/useTest';
import { useAttempts } from '@/composables/useAttempts';
import TakeTestView from '@/components/take-test-view.vue';

const props = defineProps<{
  testId: number;
}>();

const emit = defineEmits<{
  back: [];
}>();

const { currentTest, statistics, fetchTestById, fetchTestStatistics } = useTests();
const { startAttempt } = useAttempts();

const showingAttempt = ref(false);
const currentAttemptId = ref<number | null>(null);

onMounted(async () => {
  await fetchTestById(props.testId);
  await fetchTestStatistics(props.testId);
});

const handleStartTest = async () => {
  const attemptId = await startAttempt(props.testId);
  currentAttemptId.value = attemptId;
  showingAttempt.value = true;
};

const handleAttemptFinished = () => {
  showingAttempt.value = false;
  currentAttemptId.value = null;
};
</script>

<template>
  <div v-if="showingAttempt && currentAttemptId">
    <TakeTestView :attempt-id="currentAttemptId" @finished="handleAttemptFinished" />
  </div>

  <div v-else-if="currentTest" class="test-details">
    <button @click="emit('back')" class="btn-link">← Back to tests</button>

    <div class="details-card">
      <div class="details-header">
        <div>
          <h2>{{ currentTest.title }}</h2>
          <p>{{ currentTest.description }}</p>
        </div>
        <span :class="['badge', currentTest.isActive ? 'badge-success' : 'badge-inactive']">
          {{ currentTest.isActive ? 'Active' : 'Inactive' }}
        </span>
      </div>

      <div class="details-grid">
        <div>
          <h3>Test Details</h3>
          <dl>
            <dt>Time Limit:</dt>
            <dd>{{ Math.floor(currentTest.timeLimit / 60) }} minutes</dd>
            <dt>Max Attempts:</dt>
            <dd>{{ currentTest.maxAttempts }}</dd>
            <dt>Available:</dt>
            <dd>
              {{ new Date(currentTest.startTime).toLocaleDateString() }} -
              {{ new Date(currentTest.endTime).toLocaleDateString() }}
            </dd>
          </dl>
        </div>

        <div v-if="statistics">
          <h3>Statistics</h3>
          <dl>
            <dt>Total Attempts:</dt>
            <dd>{{ statistics.totalAttempts }}</dd>
            <dt>Average Score:</dt>
            <dd>{{ statistics.averageScore.toFixed(1) }}%</dd>
            <dt>Pass Rate:</dt>
            <dd>{{ statistics.passRate.toFixed(1) }}%</dd>
          </dl>
        </div>
      </div>

      <button
        @click="handleStartTest"
        :disabled="!currentTest.isActive"
        class="btn btn-primary btn-block btn-lg"
      >
        Start Test
      </button>
    </div>
  </div>
</template>

<style scoped>
.test-details {
  max-width: 900px;
  margin: 0 auto;
}

.test-details > .btn-link {
  margin-bottom: 1rem;
}

.details-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.details-header h2 {
  margin: 0 0 0.5rem 0;
}

.details-header p {
  margin: 0;
  color: #666;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.details-grid h3 {
  margin-top: 0;
}

dl {
  margin: 0;
}

dt {
  color: #666;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

dd {
  margin: 0 0 0.5rem 0;
  font-weight: 500;
}

.btn-lg {
  padding: 1rem;
  font-size: 1.1rem;
}
</style>
