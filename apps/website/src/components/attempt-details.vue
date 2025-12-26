<script setup lang="ts">
import { onMounted } from 'vue';
import { useAttempts } from '@/composables/useAttempts';

const props = defineProps<{
  attemptId: number;
}>();

const emit = defineEmits<{
  close: [];
}>();

const { attemptDetails, fetchAttemptDetails } = useAttempts();

onMounted(async () => {
  await fetchAttemptDetails(props.attemptId);
});
</script>

<template>
  <div v-if="attemptDetails">
    <button @click="emit('close')" class="btn-link">← Back to attempts</button>

    <div class="details-card">
      <h2>Attempt Details</h2>

      <div class="summary">
        <div>
          <div class="summary-label">Started</div>
          <div>{{ new Date(attemptDetails.attempt.startTime).toLocaleString() }}</div>
        </div>
        <div>
          <div class="summary-label">Ended</div>
          <div>
            {{ attemptDetails.attempt.endTime ? new Date(attemptDetails.attempt.endTime).toLocaleString() : 'N/A' }}
          </div>
        </div>
        <div>
          <div class="summary-label">Final Score</div>
          <div class="final-score">{{ attemptDetails.attempt.score }}</div>
        </div>
      </div>

      <h3>Answers</h3>

      <div class="answers-list">
        <div
          v-for="(answer, index) in attemptDetails.answers"
          :key="index"
          class="answer-card"
          :class="{ correct: answer.isCorrect, incorrect: !answer.isCorrect }"
        >
          <div class="answer-header">
            <h4>{{ answer.questionText }}</h4>
            <div class="answer-score">
              <div>{{ answer.scoreEarned }} / {{ answer.maxScore }}</div>
              <div :class="answer.isCorrect ? 'correct-label' : 'incorrect-label'">
                {{ answer.isCorrect ? '✓ Correct' : '✗ Incorrect' }}
              </div>
            </div>
          </div>

          <div class="answer-content">
            <div>
              <div class="answer-label">Your Answer:</div>
              <div>{{ answer.userAnswerText || 'N/A' }}</div>
            </div>

            <div v-if="!answer.isCorrect">
              <div class="answer-label">Correct Answer:</div>
              <div class="correct-answer">{{ answer.correctAnswerText }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.btn-link {
  margin-bottom: 1rem;
}

.details-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.details-card h2 {
  margin: 0 0 1.5rem 0;
}

.details-card h3 {
  margin: 2rem 0 1rem 0;
}

.summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.summary-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.25rem;
}

.final-score {
  font-size: 2rem;
  font-weight: bold;
  color: #007bff;
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.answer-card {
  border: 2px solid;
  border-radius: 8px;
  padding: 1.5rem;
}

.answer-card.correct {
  border-color: #28a745;
  background: #f0f9f3;
}

.answer-card.incorrect {
  border-color: #dc3545;
  background: #fef5f6;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.answer-header h4 {
  margin: 0;
  flex: 1;
}

.answer-score {
  text-align: right;
}

.answer-score > div:first-child {
  font-weight: bold;
  margin-bottom: 0.25rem;
}

.correct-label {
  color: #28a745;
  font-size: 0.9rem;
}

.incorrect-label {
  color: #dc3545;
  font-size: 0.9rem;
}

.answer-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.answer-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.25rem;
}

.correct-answer {
  color: #28a745;
  font-weight: 500;
}
</style>
