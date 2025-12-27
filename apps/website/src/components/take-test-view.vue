<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch } from 'vue';
import { useAttempts } from '@/composables/useAttempts';
import { questionsApi } from '@/api/question';
import type { Question, AnswerOption } from '@/types/types';

type AnswerType = 'single_choice' | 'multiple_choice' | 'text';

const props = defineProps<{ attemptId: number }>();
const emit = defineEmits<{ finished: [] }>();

const {
  currentAttempt,
  fetchAttemptProgress,
  submitAnswer,
  finishAttempt,
} = useAttempts();

/* -------------------- state -------------------- */

const questions = ref<Question[]>([]);
const answerOptions = ref<Record<number, AnswerOption[]>>({});
const answeredSet = ref<Set<number>>(new Set());

// Track current answer only for the active question
const currentAnswerIds = ref<number[]>([]);
const currentAnswerText = ref('');

const currentQuestionIndex = ref(0);
const isLoading = ref(true);
const hasFinished = ref(false);

/* -------------------- timer -------------------- */

const localTimeRemaining = ref(0);
let timer: number | null = null;

/* -------------------- computed -------------------- */

const currentQuestion = computed(() => questions.value[currentQuestionIndex.value]);

const isAnswered = computed(() =>
  currentQuestion.value ? answeredSet.value.has(currentQuestion.value.id) : false
);

/**
 * PROXY COMPUTED:
 * This solves the "radio vs checkbox" v-model conflict.
 * It forces radio selections to be stored in our array, enabling the submit button.
 */
const selectedIds = computed<(number | undefined)[]>({
  get: () => currentAnswerIds.value,
  
  set: (val) => {
    // 1. Ensure val is an array (v-model sometimes passes a single value for radio)
    const incoming = Array.isArray(val) ? val : [val];
    
    // 2. Filter to strictly numbers using a Type Guard
    const cleanVal = incoming.filter((v): v is number => typeof v === 'number');

    if (currentQuestion.value?.answerType === 'single_choice') {
      // Radio logic: only keep the most recent selection
      if (cleanVal.length === 0) {
        currentAnswerIds.value = [];
        return;
      } else {
        let last = cleanVal[cleanVal.length - 1]!;
        currentAnswerIds.value = [last];
      }
    } else {
      // Checkbox logic: keep the whole array
      currentAnswerIds.value = cleanVal;
    }
  }
});

const formattedTime = computed(() => {
  const m = Math.floor(localTimeRemaining.value / 60);
  const s = localTimeRemaining.value % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
});

const isTimeCritical = computed(() => localTimeRemaining.value < 300);

const progressPercent = computed(() =>
  questions.value.length ? (answeredSet.value.size / questions.value.length) * 100 : 0
);

const canFinish = computed(() => answeredSet.value.size === questions.value.length);

/* -------------------- sync & reset -------------------- */

watch(() => currentAttempt.value?.answeredQuestions, (list) => {
  if (list) answeredSet.value = new Set(list);
}, { immediate: true });

// Reset local answer state when the question changes
watch(currentQuestionIndex, () => {
  currentAnswerIds.value = [];
  currentAnswerText.value = '';
});

/* -------------------- lifecycle -------------------- */

onMounted(async () => {
  try {
    const progress = await fetchAttemptProgress(props.attemptId);
    if (!progress) return;

    localTimeRemaining.value = progress.timeRemaining;

    const qRes = await questionsApi.getQuestions(progress.testId);
    questions.value = qRes.questions;

    // Load options for all choice questions up front
    await Promise.all(
      questions.value.map(async (q) => {
        if (q.answerType === 'single_choice' || q.answerType === 'multiple_choice') {
          const o = await questionsApi.getAnswerOptions(q.id);
          answerOptions.value[q.id] = o.options ?? [];
        }
      })
    );

    startTimer();
  } finally {
    isLoading.value = false;
  }
});

onUnmounted(() => { if (timer) clearInterval(timer); });

/* -------------------- logic -------------------- */

function startTimer() {
  timer = window.setInterval(async () => {
    if (hasFinished.value) return;
    if (localTimeRemaining.value > 0) {
      localTimeRemaining.value--;
    } else {
      await handleFinish();
    }
  }, 1000);
}

async function handleSubmitAnswer() {
  if (!currentQuestion.value || isAnswered.value) return;

  const q = currentQuestion.value;
  let payload: any;

  if (q.answerType === 'single_choice') {
    payload = { questionId: q.id, answerId: currentAnswerIds.value[0] };
  } else if (q.answerType === 'multiple_choice') {
    payload = { questionId: q.id, answerIds: currentAnswerIds.value };
  } else {
    payload = { questionId: q.id, answerText: currentAnswerText.value };
  }

  await submitAnswer(props.attemptId, payload);
  answeredSet.value.add(q.id);

  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
  }
}

async function handleFinish() {
  if (hasFinished.value) return;
  hasFinished.value = true;
  if (timer) clearInterval(timer);
  const result = await finishAttempt(props.attemptId);
  alert(`Test Finished\nScore: ${result.score}/${result.maxScore} (${result.percentage}%)`);
  emit('finished');
}

function goToQuestion(i: number) { currentQuestionIndex.value = i; }
</script>

<template>
  <div v-if="isLoading" class="loading-state">
    <p>Loading questions, please wait...</p>
  </div>

  <div v-else class="take-test">
    <div class="test-header">
      <h2>Taking Test</h2>
      <div class="timer" :class="{ critical: isTimeCritical }">
        Time Remaining: {{ formattedTime }}
      </div>
    </div>

    <div class="progress-bar">
      <div class="progress-info">Answered {{ answeredSet.size }} of {{ questions.length }}</div>
      <div class="progress">
        <div class="progress-fill" :style="{ width: `${progressPercent}%` }" />
      </div>
    </div>

    <div v-if="currentQuestion" class="question-section">
      <div class="points-badge">{{ currentQuestion.maxPoints }} points</div>
      <h3>{{ currentQuestion.text }}</h3>

      <div v-if="currentQuestion.answerType !== 'text'" class="options">
        <label
          v-for="option in answerOptions[currentQuestion.id] || []"
          :key="option.id"
          class="option"
          :class="{ selected: currentAnswerIds.includes(option.id) }"
        >
          <input
            :type="currentQuestion.answerType === 'single_choice' ? 'radio' : 'checkbox'"
            :value="option.id"
            v-model="selectedIds"
            :disabled="isAnswered"
          />
          <span>{{ option.optionText }}</span>
        </label>
      </div>

      <textarea
        v-else
        v-model="currentAnswerText"
        class="input-field"
        rows="6"
        :disabled="isAnswered"
        placeholder="Type your answer here..."
      />

      <div v-if="isAnswered" class="answered-badge">✓ Answer Submitted</div>
    </div>

    <div class="actions">
      <button 
        class="btn" 
        @click="goToQuestion(currentQuestionIndex - 1)" 
        :disabled="currentQuestionIndex === 0"
      >
        Previous
      </button>

      <div class="actions-right">
        <button
          v-if="!isAnswered"
          class="btn btn-primary"
          @click="handleSubmitAnswer"
          :disabled="currentQuestion?.answerType === 'text' ? !currentAnswerText.trim() : !currentAnswerIds.length"
        >
          Submit Answer
        </button>

        <button v-if="currentQuestionIndex < questions.length - 1" class="btn" @click="goToQuestion(currentQuestionIndex + 1)">
          Next
        </button>
        <button v-else class="btn btn-success" @click="handleFinish" :disabled="!canFinish && localTimeRemaining > 0">
          Finish Test
        </button>
      </div>
    </div>

    <div class="question-nav">
      <h4>Navigator</h4>
      <div class="nav-grid">
        <button
          v-for="(q, index) in questions" :key="q.id"
          class="nav-btn"
          :class="{ current: index === currentQuestionIndex, answered: answeredSet.has(q.id) }"
          @click="goToQuestion(index)"
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
