<template>
  <section v-if="product" class="page-stack">
    <div class="panel detail-layout">
      <div class="detail-media">
        <img :src="product.imageUrl" :alt="product.name" />
      </div>
      <div class="stack detail-copy">
        <h1>{{ product.name }}</h1>
        <p class="muted">{{ product.brand }}</p>
        <strong class="price">¥{{ product.price }}</strong>
        <p>{{ product.description }}</p>
        <div class="metric-strip">
          <div class="metric">
            <strong>{{ product.stock }}</strong>
            <span class="muted">库存</span>
          </div>
          <div class="metric">
            <strong>{{ product.salesCount }}</strong>
            <span class="muted">销量</span>
          </div>
          <div class="metric">
            <strong>{{ similar.length }}</strong>
            <span class="muted">相似商品</span>
          </div>
        </div>
        <div class="actions">
          <label class="form-row">
            <span>数量</span>
            <input v-model.number="quantity" type="number" min="1" :max="product.stock" />
          </label>
          <button class="primary-button" type="button" @click="addToCart">加入购物车</button>
          <button class="primary-button" type="button" :disabled="buying" @click="buyNow">
            {{ buying ? '提交中...' : '立即购买' }}
          </button>
        </div>
        <p v-if="message" :class="failed ? 'error' : 'success'">{{ message }}</p>
      </div>
    </div>

    <section class="panel stack">
      <div class="section-header">
        <div>
          <h2 class="section-title">相似商品</h2>
          <p class="muted">同分类和相似行为会影响展示结果。</p>
        </div>
      </div>
      <div class="grid">
        <ProductCard v-for="item in similar" :key="item.id" :product="item" />
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import { getProduct, similarProducts } from '../../api/products'
import { useCartStore } from '../../stores/cart'
import { useAuthStore } from '../../stores/auth'
import { createOrderFromSelected } from '../../api/orders'
import { addCartItem } from '../../api/cart'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const auth = useAuthStore()
const product = ref(null)
const similar = ref([])
const quantity = ref(1)
const message = ref('')
const failed = ref(false)
const buying = ref(false)

async function load() {
  product.value = await getProduct(route.params.id)
  similar.value = await similarProducts(route.params.id)
}

async function addToCart() {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  message.value = ''
  failed.value = false
  try {
    await cart.add(product.value.id, quantity.value)
    message.value = '已加入购物车'
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '加入购物车失败'
  }
}

async function buyNow() {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  buying.value = true
  message.value = ''
  failed.value = false
  try {
    const added = await addCartItem(product.value.id, quantity.value)
    const order = await createOrderFromSelected([added.id])
    await cart.load()
    router.push(`/orders/${order.id}`)
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '购买失败'
  } finally {
    buying.value = false
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>
