<template>
  <article class="product-card">
    <RouterLink :to="`/products/${product.id}`">
      <img :src="product.imageUrl" :alt="product.name" />
    </RouterLink>
    <div class="product-card-body">
      <h3>{{ product.name }}</h3>
      <p class="muted">{{ product.brand || '精选商品' }}</p>
      <strong class="price">¥{{ product.price }}</strong>
      <RouterLink class="primary-button" :to="`/products/${product.id}`">查看</RouterLink>
    </div>
  </article>
</template>

<script setup>
import { RouterLink } from 'vue-router'

defineProps({
  product: {
    type: Object,
    required: true
  }
})
</script>
