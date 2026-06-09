<template>
  <section class="auth-page">
    <div class="auth-showcase">
      <h1>加入我们</h1>
      <p>注册后即可浏览商品、加入购物车、下单购买，还能获得个性化推荐。</p>
    </div>
    <div class="auth-card">
      <div>
        <h1>注册</h1>
        <p class="muted">创建一个新账号，开始购物体验。</p>
      </div>
      <form @submit.prevent="submitRegister">
        <label class="form-row">
          <span>用户名</span>
          <input v-model="username" autocomplete="username" />
        </label>
        <label class="form-row">
          <span>密码</span>
          <input v-model="password" type="password" autocomplete="new-password" />
        </label>
        <label class="form-row">
          <span>确认密码</span>
          <input v-model="confirm" type="password" autocomplete="new-password" />
        </label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="primary-button" type="submit">注册</button>
      </form>
      <p class="muted" style="margin-top: 16px; text-align: center;">
        已有账号？
        <RouterLink to="/login">去登录</RouterLink>
      </p>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const confirm = ref('')
const error = ref('')

async function submitRegister() {
  error.value = ''
  if (!username.value.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码至少 6 位'
    return
  }
  if (password.value !== confirm.value) {
    error.value = '两次密码输入不一致'
    return
  }
  try {
    await auth.register(username.value.trim(), password.value)
    router.push(auth.isAdmin ? '/admin/products' : '/products')
  } catch (err) {
    error.value = err.response?.data?.message || '注册失败，请稍后重试'
  }
}
</script>
