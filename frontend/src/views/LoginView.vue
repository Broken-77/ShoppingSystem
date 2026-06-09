<template>
  <section class="auth-page">
    <div class="auth-showcase">
      <h1>欢迎回来</h1>
      <p>登录后可同步购物车、查看订单，并获得更贴合个人偏好的商品推荐。</p>
    </div>
    <div class="auth-card">
      <div>
        <h1>登录</h1>
        <p class="muted">默认示例账号已填好，可直接体验。</p>
      </div>
      <form @submit.prevent="submitLogin">
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
    </div>
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
    router.push(auth.isAdmin ? '/admin/products' : '/products')
  } catch (err) {
    error.value = err.response?.data?.message || '登录失败'
  }
}
</script>
