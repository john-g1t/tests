export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}

export interface Test {
  id: number;
  creatorId: number;
  title: string;
  description: string;
  timeLimit: number;
  maxAttempts: number;
  isActive: boolean;
  startTime: string;
  endTime: string;
  createdAt: string;
}

export interface Question {
  id: number;
  testId: number;
  text: string;
  answerType: 'single_choice' | 'multiple_choice' | 'text';
  maxPoints: number;
  orderIndex: number;
}

export interface AnswerOption {
  id: number;
  questionId: number;
  optionText: string;
  score: number;
}

export interface TestAttempt {
  id: number;
  userId: number;
  testId: number;
  testTitle?: string;
  startTime: string;
  endTime: string | null;
  score: number | null;
  isFinished: boolean;
}

export interface AttemptProgress {
  id: number;
  userId: number;
  testId: number;
  startTime: string;
  endTime: string | null;
  score: number | null;
  isFinished: boolean;
  answeredQuestions: number[];
  totalQuestions: number;
  timeRemaining: number;
}

export interface AttemptAnswer {
  questionId: number;
  questionText: string;
  userAnswerId: number | null;
  userAnswerText: string | null;
  correctAnswerText: string;
  scoreEarned: number;
  maxScore: number;
  isCorrect: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

export interface TestStatistics {
  testId: number;
  totalAttempts: number;
  completedAttempts: number;
  averageScore: number;
  maxScore: number;
  minScore: number;
  passRate: number;
}

export interface UserStatistics {
  userId: number;
  totalAttempts: number;
  completedAttempts: number;
  averageScore: number;
  bestScore: number;
  totalTestsTaken: number;
  recentActivity: Array<{
    testId: number;
    testTitle: string;
    score: number;
    completedAt: string;
  }>;
}

