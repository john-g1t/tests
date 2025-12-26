<script setup lang="ts">
import { onMounted, ref, computed, onUnmounted } from 'vue';
import { useAttempts } from '@/composables/useAttempts';
import { questionsApi } from '@/api/question';
import type { Question, AnswerOption } from '@/types/types';

const props = defineProps<{
  attemptId: number;
}>();

const emit = defineEmits<{
  finished: [];
}>();

const { currentAttempt, fetchAttemptProgress, submitAnswer, finishAttempt } = useAttempts();

const questions = ref<Question[]>([]);
const answerOptions = ref<Record<number, AnswerOption[]>>({});
const currentQuestionIndex = ref(0);
const selectedAnswerId = ref<number | null>(null);
const textAnswer = ref('');
const timerInterval = ref<number | null>(null);

const currentQuestion = computed(() => questions.value[currentQuestionIndex.value]);

const isAnswered = computed(() => {
  if (!currentQuestion.value) return false;
  return currentAttempt.value?.answeredQuestions.includes(currentQuestion.value.id) || false;
});

const formattedTimeRemaining = computed(() => {
  const seconds = currentAttempt.value?.timeRemaining || 0;
  const minutes = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${minutes}:${secs.toString().padStart(2, '0')}`;
});

const isTimeCritical = computed(() => {
  return currentAttempt.value && currentAttempt.value.timeRemaining < 300;
});

onMounted(async () => {
  await fetchAttemptProgress(props.attemptId);
  
  if (currentAttempt.value) {
    const response = await questionsApi.getQuestions(currentAttempt.value.testId);
    questions.value = response.questions;
    
    for (const question of questions.value) {
      if (question.answerType === 'MULTIPLE_CHOICE') {
        const options = await questionsApi.getAnswerOptions(question.id);
        answerOptions.value[question.id] = options.options;
      }
    }
  }
  
  timerInterval.value = window.setInterval(async () => {
    await fetchAttemptProgress(props.attemptId);
    
    if (currentAttempt.value && currentAttempt.value.timeRemaining <= 0) {
      await handleFinish();
    }
  }, 1000);
});

onUnmounted(() => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
  }
});

const handleSubmitAnswer = async () => {
  if (!currentQuestion.value) return;
  
  try {
    if (currentQuestion.value.answerType === 'MULTIPLE_CHOICE') {
      if (selectedAnswerId.value) {
        await submitAnswer(props.attemptId, {
          questionId: currentQuestion.value.id,
          answerId: selectedAnswerId.value,
        });
      }
    } else {
      await submitAnswer(props.attemptId, {
        questionId: currentQuestion.value.id,
        answerText: textAnswer.value,
      });
    }
    
    selectedAnswerId.value = null;
    textAnswer.value = '';
    
    if (currentQuestionIndex.value < questions.value.length - 1) {
      currentQuestionIndex.value++;
    }
  } catch (e) {
    console.error('Failed to submit answer:', e);
  }
};

const handleFinish = async () => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
  }
  
  try {
    const result = await finishAttempt(props.attemptId);
    alert(`Test completed! Score: ${result.score}/${result.maxScore} (${result.percentage.toFixed(1)}%)`);
    emit('finished');
  } catch (e) {
    console.error('Failed to finish attempt:', e);
  }
};

const goToQuestion = (index: number) => {
  currentQuestionIndex.value = index;
  selectedAnswerId.value = null;
  textAnswer.value = '';
};

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
    selectedAnswerId.value = null;
    textAnswer.value = '';
  }
};

const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--;
    selectedAnswerId.value = null;
    textAnswer.value = '';
  }
};
</script>

<template>
  <div class="take-test">
    <div class="test-header">
      <h2>Taking Test</h2>
      <div class="timer" :class="{ critical: isTimeCritical }">
        Time Remaining: {{ formattedTimeRemaining }}
      </div>
    </div>

    <div class="progress-bar">
      <div class="progress-info">
        Question {{ currentQuestionIndex + 1 }} of {{ questions.length }}
      </div>
      <div class="progress">
        <div
          class="progress-fill"
          :style="{ width: `${((currentQuestionIndex + 1) / questions.length) * 100}%` }"
        />
      </div>
    </div>

    <div v-if="currentQuestion" class="question-section">
      <h3>{{ currentQuestion.text }}</h3>
      <div class="points-badge">{{ currentQuestion.maxPoints }} points</div>

      <div v-if="currentQuestion.answerType === 'MULTIPLE_CHOICE'" class="options">
        <label
          v-for="option in answerOptions[currentQuestion.id]"
          :key="option.id"
          class="option"
          :class="{ selected: selectedAnswerId === option.id }"
        >
          <input
            v-model="selectedAnswerId"
            type="radio"
            :value="option.id"
            :disabled="isAnswered"
          />
          <span>{{ option.optionText }}</span>
        </label>
      </div>

      <div v-else-if="currentQuestion.answerType === 'TEXT'">
        <textarea
          v-model="textAnswer"
          :disabled="isAnswered"
          rows="6"
          placeholder="Enter your answer..."
        />
      </div>

      <div v-else-if="currentQuestion.answerType === 'NUMERIC'">
        <input
          v-model="textAnswer"
          :disabled="isAnswered"
          type="number"
          placeholder="Enter numeric answer..."
        />
      </div>

      <div v-if="isAnswered" class="answered-badge">✓ Already answered</div>
    </div>

    <div class="actions">
      <button
        @click="previousQuestion"
        :disabled="currentQuestionIndex === 0"
        class="btn"
      >
        Previous
      </button>

      <div class="actions-right">
        <button
          v-if="!isAnswered"
          @click="handleSubmitAnswer"
          :disabled="currentQuestion?.answerType === 'MULTIPLE_CHOICE' ? !selectedAnswerId : !textAnswer"
          class="btn btn-primary"
        >
          Submit Answer
        </button>

        <button
          v-if="currentQuestionIndex < questions.length - 1"
          @click="nextQuestion"
          class="btn"
        >
          Next
        </button>

        <button
          v-if="currentQuestionIndex === questions.length - 1"
          @click="handleFinish"
          class="btn btn-success"
        >
          Finish Test
        </button>
      </div>
    </div>

    <div class="question-nav">
      <h4>Question Navigator</h4>
      <div class="nav-grid">
        <button
          v-for="(q, index) in questions"
          :key="q.id"
          @click="goToQuestion(index)"
          :class="[
            'nav-btn',
            { 
              current: index === currentQuestionIndex,
              answered: currentAttempt?.answeredQuestions.includes(q.id)
            }
          ]"
        >
          {{ index + 1 }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.take-test {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.test-header h2 {
  margin: 0;
}

.timer {
  font-size: 1.1rem;
  font-weight: 600;
}

.timer.critical {
  color: #dc3545;
}

.progress-bar {
  margin-bottom: 2rem;
}

.progress-info {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.5rem;
}

.progress {
  height: 8px;
  background: #e9e9e9;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #007bff;
  transition: width 0.3s;
}

.question-section {
  margin-bottom: 2rem;
}

.question-section h3 {
  margin: 0 0 1rem 0;
}

.points-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #e9e9e9;
  border-radius: 4px;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.option {
  display: flex;
  align-items: center;
  padding: 1rem;
  border: 2px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.option:hover {
  background: #f9f9f9;
}

.option.selected {
  border-color: #007bff;
  background: #e7f3ff;
}

.option input[type="radio"] {
  margin-right: 0.75rem;
}

.answered-badge {
  margin-top: 1rem;
  color: #28a745;
  font-weight: 600;
}

.actions {
  display: flex;
  justify-content: space-between;
  padding-top: 1.5rem;
  border-top: 1px solid #e9e9e9;
  margin-bottom: 2rem;
}

.actions-right {
  display: flex;
  gap: 0.5rem;
}

.question-nav {
  padding-top: 1.5rem;
  border-top: 1px solid #e9e9e9;
}

.question-nav h4 {
  margin: 0 0 1rem 0;
}

.nav-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #e9e9e9;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.nav-btn.current {
  background: #007bff;
  color: white;
}

.nav-btn.answered {
  background: #d4edda;
  color: #155724;
}
</style>
