<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useAttempts } from '@/composables/useAttempts';
import { useAuth } from '@/composables/useAuth';
import AttemptDetails from '@/components/attempt-details.vue';

const { attempts, fetchUserAttempts } = useAttempts();
const { currentUser } = useAuth();

const selectedAttemptId = ref<number | null>(null);

onMounted(async () => {
  if (currentUser.value) {
    await fetchUserAttempts(currentUser.value.id);
  }
});

const viewDetails = (attemptId: number) => {
  selectedAttemptId.value = attemptId;
};

const closeDetails = () => {
  selectedAttemptId.value = null;
};
</script>

<template>
  <div>
    <AttemptDetails
      v-if="selectedAttemptId"
      :attempt-id="selectedAttemptId"
      @close="closeDetails"
    />

    <div v-else>
      <h2>My Attempts</h2>

      <div v-if="attempts.length === 0" class="empty">
        You haven't taken any tests yet
      </div>

      <div v-else class="attempts-list">
        <div
          v-for="attempt in attempts"
          :key="attempt.id"
          class="attempt-card"
          @click="viewDetails(attempt.id)"
        >
          <div class="attempt-info">
            <h3>{{ attempt.testTitle }}</h3>
            <div class="attempt-dates">
              <div>Started: {{ new Date(attempt.startTime).toLocaleString() }}</div>
              <div v-if="attempt.endTime">
                Ended: {{ new Date(attempt.endTime).toLocaleString() }}
              </div>
            </div>
          </div>
          <div class="attempt-score">
            <div v-if="attempt.isFinished && attempt.score !== null" class="score">
              {{ attempt.score }}
            </div>
            <div v-else class="badge badge-warning">In Progress</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
h2 {
  margin-bottom: 1.5rem;
}

.empty {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.attempts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.attempt-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: box-shadow 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.attempt-card:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.attempt-info h3 {
  margin: 0 0 0.5rem 0;
}

.attempt-dates {
  font-size: 0.9rem;
  color: #666;
}

.attempt-score {
  text-align: right;
}

.score {
  font-size: 2rem;
  font-weight: bold;
  color: #007bff;
}

.badge-warning {
  background: #fff3cd;
  color: #856404;
}
</style>
