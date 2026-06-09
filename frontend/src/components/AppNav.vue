<template>
  <header class="topbar">
    <div class="topbar-inner">
      <RouterLink class="brand" :to="auth.isAdmin ? '/admin/products' : '/'">
        <span class="brand-mark">S</span>
        <span>Shopping System</span>
      </RouterLink>
      <nav v-if="auth.isAdmin" class="nav-links">
        <RouterLink to="/admin/products">后台管理</RouterLink>
      </nav>
      <nav v-else class="nav-links">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/products">商品</RouterLink>
        <RouterLink to="/cart">购物车<span v-if="cart.count" class="cart-count">{{ cart.count }}</span></RouterLink>
        <RouterLink to="/orders">我的订单</RouterLink>
      </nav>
      <div class="session">
        <span v-if="auth.isLoggedIn" class="session-name">{{ auth.username }}</span>
        <button v-if="auth.isLoggedIn" type="button" @click="logout">退出</button>
        <RouterLink v-else class="primary-button" to="/login">登录</RouterLink>
      </div>
    </div>
  </header>
</template>

<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()

function logout() {
  auth.logout()
  router.push('/')
}
</script>
