<template>
  <section class="stack">
    <div class="panel stack">
      <h1>商品</h1>
      <form class="toolbar" @submit.prevent="loadProducts">
        <label class="form-row">
          <span>关键词</span>
          <input v-model="filters.keyword" placeholder="phone" />
        </label>
        <label class="form-row">
          <span>分类</span>
          <select v-model="filters.categoryId">
            <option value="">全部</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </label>
        <label class="form-row">
          <span>最低价</span>
          <input v-model.number="filters.minPrice" type="number" min="0" />
        </label>
        <label class="form-row">
          <span>最高价</span>
          <input v-model.number="filters.maxPrice" type="number" min="0" />
        </label>
        <button class="primary-button" type="submit">筛选</button>
      </form>
    </div>

    <div class="grid">
      <ProductCard v-for="product in visibleProducts" :key="product.id" :product="product" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import ProductCard from '../../components/ProductCard.vue'
import { listCategories, listProducts } from '../../api/products'

const categories = ref([])
const products = ref([])
const filters = reactive({
  keyword: '',
  categoryId: '',
  minPrice: '',
  maxPrice: ''
})

const visibleProducts = computed(() => {
  return products.value.filter((product) => {
    const price = Number(product.price)
    if (filters.minPrice !== '' && price < Number(filters.minPrice)) return false
    if (filters.maxPrice !== '' && price > Number(filters.maxPrice)) return false
    return true
  })
})

async function loadProducts() {
  products.value = await listProducts({
    keyword: filters.keyword || undefined,
    categoryId: filters.categoryId || undefined
  })
}

onMounted(async () => {
  categories.value = await listCategories()
  await loadProducts()
})
</script>
