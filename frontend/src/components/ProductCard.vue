<template>
  <article class="product-card">
    <RouterLink class="product-media" :to="`/products/${product.id}`">
      <img :src="product.imageUrl" :alt="product.name" />
      <span class="product-tag">{{ product.salesCount || 0 }} 人入手</span>
    </RouterLink>
    <div class="product-card-body">
      <h3>{{ product.name }}</h3>
      <p class="muted">{{ product.brand || '精选商品' }}</p>
      <div class="product-card-footer">
        <strong class="price">¥{{ product.price }}</strong>
        <RouterLink class="secondary-button" :to="`/products/${product.id}`">查看</RouterLink>
      </div>
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
