<script setup lang="ts">
import { ref } from 'vue';
import { useTests } from '@/composables/useTest';
import { questionsApi } from '@/api/question';

type AnswerType = 'single_choice' | 'multiple_choice' | 'text';

interface QuestionForm {
  text: string;
  answerType: AnswerType;
  maxPoints: number;
  options: { text: string; score: number }[];
}

const emit = defineEmits<{ created: [] }>();

const { createTest, isLoading, error } = useTests();

const form = ref({
  title: '',
  description: '',
  timeLimit: 60,
  maxAttempts: 3,
  startTime: '',
  endTime: '',
});

const questions = ref<QuestionForm[]>([
  {
    text: '',
    answerType: 'single_choice',
    maxPoints: 10,
    options: [{ text: '', score: 10 }],
  },
]);

const addQuestionForm = () => {
  questions.value.push({
    text: '',
    answerType: 'single_choice',
    maxPoints: 10,
    options: [{ text: '', score: 10 }],
  });
};

const addOptionForm = (q: QuestionForm) => {
  q.options.push({ text: '', score: 0 });
};

const handleSubmit = async () => {
  const testId = await createTest({
    ...form.value,
    timeLimit: form.value.timeLimit * 60,
    startTime: new Date(form.value.startTime).toISOString(),
    endTime: new Date(form.value.endTime).toISOString(),
  });

  for (const q of questions.value) {
    const { questionId } = await questionsApi.addQuestion(testId, {
      text: q.text,
      answerType: q.answerType,
      maxPoints: q.maxPoints,
    });

    if (q.answerType === 'multiple_choice') {
      for (const opt of q.options) {
        await questionsApi.addAnswerOption(questionId, {
          optionText: opt.text,
          score: opt.score,
        });
      }
    }
  }

  emit('created');
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

    <h3>Questions</h3>

    <div
      v-for="(q, qi) in questions"
      :key="qi"
      class="question-block"
    >
      <div class="form-group">
        <label>Question</label>
        <input v-model="q.text" required />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Type</label>
          <select v-model="q.answerType">
            <option value="single_choice">Single choice</option>
            <option value="multiple_choice">Multiple choice</option>
            <option value="text">Text</option>
          </select>
        </div>

        <div class="form-group">
          <label>Max points</label>
          <input v-model.number="q.maxPoints" type="number" min="1" />
        </div>
      </div>

      <div v-if="q.answerType !== 'text'">
        <h4>Answer options</h4>

        <div
          v-for="(o, oi) in q.options"
          :key="oi"
          class="form-row"
        >
          <input
            v-model="o.text"
            placeholder="Option text"
            required
          />
          <input
            v-model.number="o.score"
            type="number"
            placeholder="Score"
          />
        </div>

        <button type="button" @click="addOptionForm(q)">
          + Add option
        </button>
      </div>
    </div>

    <button type="button" @click="addQuestionForm">
      + Add question
    </button>

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

form {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.question-block {
  border: 1px solid #ddd;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
  background: #f9f9f9;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

textarea {
  display: block
}
</style>
