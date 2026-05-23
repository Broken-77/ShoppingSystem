<template>
  <div class="stack">
    <section class="hero">
      <h1>Shopping System</h1>
      <p class="muted">精选数码、家居与运动好物</p>
    </section>

    <section class="panel stack">
      <h2 class="section-title">猜你喜欢</h2>
      <div class="grid">
        <ProductCard v-for="product in recommendations" :key="product.id" :product="product" />
      </div>
    </section>

    <section class="panel stack">
      <h2 class="section-title">热门商品</h2>
      <div class="grid">
        <ProductCard v-for="product in hotProducts" :key="product.id" :product="product" />
      </div>
    </section>

    <section class="panel stack">
      <h2 class="section-title">新品上架</h2>
      <div class="grid">
        <ProductCard v-for="product in newProducts" :key="product.id" :product="product" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import ProductCard from '../../components/ProductCard.vue'
import { useAuthStore } from '../../stores/auth'
import { listProducts } from '../../api/products'
import { homeRecommendations } from '../../api/recommendations'

const auth = useAuthStore()
const products = ref([])
const recommendations = ref([])

const hotProducts = computed(() => [...products.value].sort((a, b) => b.salesCount - a.salesCount).slice(0, 8))
const newProducts = computed(() => [...products.value].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 8))

onMounted(async () => {
  products.value = await listProducts()
  recommendations.value = auth.isLoggedIn ? await homeRecommendations() : hotProducts.value
})
</script>
