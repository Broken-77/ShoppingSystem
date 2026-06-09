<template>
  <div class="page-stack">
    <section class="hero">
      <div class="hero-copy">
        <h1>把好物放进今天</h1>
        <p>精选数码、家居和运动单品，按你的浏览、加购和下单行为持续更新推荐。</p>
        <div class="hero-actions">
          <RouterLink class="primary-button" to="/products">逛商品</RouterLink>
          <RouterLink class="secondary-button" to="/cart">看购物车</RouterLink>
        </div>
      </div>
      <div class="hero-visual">
        <img :src="heroProduct?.imageUrl || 'https://picsum.photos/seed/shopping-system-hero/760/760'" alt="精选商品展示" />
        <div class="hero-badge">
          <strong>今日精选</strong>
          <span>{{ heroProduct?.name || 'Aurora Phone 15' }}</span>
        </div>
      </div>
    </section>

    <section class="metric-strip">
      <div class="metric">
        <strong>{{ products.length }}</strong>
        <span class="muted">件在售商品</span>
      </div>
      <div class="metric">
        <strong>{{ hotProducts.length }}</strong>
        <span class="muted">件热门推荐</span>
      </div>
      <div class="metric">
        <strong>{{ recommendations.length }}</strong>
        <span class="muted">条个性化结果</span>
      </div>
    </section>

    <section class="panel stack">
      <div class="section-header">
        <div>
          <h2 class="section-title">猜你喜欢</h2>
          <p class="muted">登录后会根据行为权重调整推荐顺序。</p>
        </div>
        <RouterLink class="ghost-button" to="/products">全部商品</RouterLink>
      </div>
      <div class="grid">
        <ProductCard v-for="product in recommendations" :key="product.id" :product="product" />
      </div>
    </section>

    <section class="panel stack">
      <div class="section-header">
        <div>
          <h2 class="section-title">热门商品</h2>
          <p class="muted">按销量排序，适合快速挑选。</p>
        </div>
      </div>
      <div class="grid">
        <ProductCard v-for="product in hotProducts" :key="product.id" :product="product" />
      </div>
    </section>

    <section class="panel stack">
      <div class="section-header">
        <div>
          <h2 class="section-title">新品上架</h2>
          <p class="muted">最近创建的商品优先展示。</p>
        </div>
      </div>
      <div class="grid">
        <ProductCard v-for="product in newProducts" :key="product.id" :product="product" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import { useAuthStore } from '../../stores/auth'
import { listProducts } from '../../api/products'
import { homeRecommendations } from '../../api/recommendations'

const auth = useAuthStore()
const products = ref([])
const recommendations = ref([])

const hotProducts = computed(() => [...products.value].sort((a, b) => b.salesCount - a.salesCount).slice(0, 8))
const newProducts = computed(() => [...products.value].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 8))
const heroProduct = computed(() => hotProducts.value[0])

onMounted(async () => {
  products.value = await listProducts()
  recommendations.value = auth.isLoggedIn ? await homeRecommendations() : hotProducts.value
})
</script>
