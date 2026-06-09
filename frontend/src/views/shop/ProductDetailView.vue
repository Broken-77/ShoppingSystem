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
          <h2 class="section-title">商品评价</h2>
          <p class="muted">{{ reviews.length }} 条评价</p>
        </div>
      </div>
      <div v-if="reviews.length" class="stack">
        <article v-for="r in reviews" :key="r.id" class="review-item" style="border-bottom:1px solid var(--border);padding:12px 0">
          <div style="display:flex;justify-content:space-between">
            <strong>{{ r.username }}</strong>
            <span style="color:gold">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5-r.rating) }}</span>
          </div>
          <p v-if="r.comment" style="margin-top:4px">{{ r.comment }}</p>
          <p class="muted" style="font-size:12px">{{ formatTime(r.createdAt) }}</p>
        </article>
      </div>
      <div v-else class="empty-state"><p class="muted">暂无评价，购买后可以评价</p></div>
      <div v-if="auth.isLoggedIn" style="margin-top:12px;border-top:1px solid var(--border);padding-top:12px">
        <p class="muted" style="margin-bottom:8px">发表评价</p>
        <div style="display:flex;gap:8px;align-items:center;margin-bottom:8px">
          <button v-for="s in 5" :key="s" type="button" @click="rating = s" 
            style="background:none;border:none;font-size:20px;cursor:pointer;padding:0">
            {{ s <= rating ? '★' : '☆' }}
          </button>
        </div>
        <textarea v-model="reviewText" placeholder="分享你的使用体验（可选）" rows="2" style="width:100%;margin-bottom:8px"></textarea>
        <button class="primary-button" :disabled="submitting" @click="submitReview">{{ submitting ? '提交中' : '提交评价' }}</button>
        <p v-if="reviewMsg" :class="reviewFailed ? 'error' : 'success'" style="margin-top:4px">{{ reviewMsg }}</p>
      </div>
    </section>

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
import { createDirectOrder } from '../../api/orders'
import { getReviews, createReview } from '../../api/reviews'

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

// 评价
const reviews = ref([])
const rating = ref(5)
const reviewText = ref('')
const submitting = ref(false)
const reviewMsg = ref('')
const reviewFailed = ref(false)

async function load() {
  product.value = await getProduct(route.params.id)
  similar.value = await similarProducts(route.params.id)
  try { reviews.value = await getReviews(route.params.id) } catch {}
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
    const order = await createDirectOrder(product.value.id, quantity.value)
    await cart.load()
    router.push(`/orders/${order.id}`)
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '购买失败'
  } finally {
    buying.value = false
  }
}

async function submitReview() {
  reviewMsg.value = ''
  reviewFailed.value = false
  submitting.value = true
  try {
    await createReview(product.value.id, rating.value, reviewText.value.trim())
    reviewMsg.value = '评价成功'
    reviewText.value = ''
    rating.value = 5
    reviews.value = await getReviews(product.value.id)
  } catch (err) {
    reviewFailed.value = true
    reviewMsg.value = err.response?.data?.message || '评价失败'
  } finally {
    submitting.value = false
  }
}

function formatTime(v) { return v ? new Date(v).toLocaleString() : '' }

watch(() => route.params.id, load)
onMounted(load)
</script>
