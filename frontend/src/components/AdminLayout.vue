<template>
  <section class="admin-shell">
    <aside class="admin-sidebar">
      <strong>后台管理</strong>
      <RouterLink to="/admin/products">商品管理</RouterLink>
      <RouterLink to="/admin/categories">分类管理</RouterLink>
      <RouterLink to="/admin/orders">订单管理</RouterLink>
    </aside>
    <div class="admin-content">
      <slot />
    </div>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>
