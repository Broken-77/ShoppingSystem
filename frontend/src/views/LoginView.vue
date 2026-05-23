<template>
  <section class="panel stack">
    <h1>登录</h1>
    <form class="stack" @submit.prevent="submitLogin">
      <label class="form-row">
        <span>用户名</span>
        <input v-model="username" autocomplete="username" />
      </label>
      <label class="form-row">
        <span>密码</span>
        <input v-model="password" type="password" autocomplete="current-password" />
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <button class="primary-button" type="submit">登录</button>
    </form>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('alice')
const password = ref('pass123')
const error = ref('')

async function submitLogin() {
  error.value = ''
  try {
    await auth.login(username.value, password.value)
    router.push('/products')
  } catch (err) {
    error.value = err.response?.data?.message || '登录失败'
  }
}
</script>
