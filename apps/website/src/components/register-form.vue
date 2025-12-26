<script setup lang="ts">
import { ref } from 'vue';
import { useAuth } from '@/composables/useAuth';

const { register, error, isLoading } = useAuth();

const email = ref('');
const password = ref('');
const firstName = ref('');
const lastName = ref('');

const handleSubmit = async () => {
  console.log("aboba")
  try {
    await register({
      email: email.value,
      password: password.value,
      firstName: firstName.value,
      lastName: lastName.value,
    });
  } catch (e) {
    // Error handled by composable
  }
};
</script>

<template>
  <form @submit.prevent="handleSubmit">
    <div class="form-row">
      <div class="form-group">
        <label>First Name</label>
        <input v-model="firstName" type="text" required />
      </div>
      
      <div class="form-group">
        <label>Last Name</label>
        <input v-model="lastName" type="text" required />
      </div>
    </div>
    
    <div class="form-group">
      <label>Email</label>
      <input v-model="email" type="email" required />
    </div>
    
    <div class="form-group">
      <label>Password</label>
      <input v-model="password" type="password" required />
    </div>
    
    <div v-if="error" class="error">{{ error }}</div>
    
    <button type="submit" :disabled="isLoading" class="btn btn-primary btn-block" @click="handleSubmit">
      {{ isLoading ? 'Loading...' : 'Register' }}
    </button>
  </form>
</template>
