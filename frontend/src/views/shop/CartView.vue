<template>
  <section class="panel stack">
    <div class="section-header">
      <div>
        <h1 class="section-title">购物车</h1>
        <p class="muted">勾选商品后可单独或批量结算。</p>
      </div>
      <strong class="price">¥{{ selectedTotal }}</strong>
    </div>

    <div v-if="cart.items.length" class="table-wrap">
      <table class="list-table">
        <thead>
          <tr>
            <th style="width:40px">
              <input type="checkbox" :checked="allSelected" @change="toggleAll" />
            </th>
            <th>商品</th>
            <th>数量</th>
            <th>小计</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in cart.items" :key="item.id" :class="{ selected: selected.has(item.id) }">
            <td>
              <input type="checkbox" :checked="selected.has(item.id)" @change="toggleOne(item.id)" />
            </td>
            <td>
              <div class="cart-product">
                <img v-if="products[item.productId]?.imageUrl" :src="products[item.productId].imageUrl" :alt="products[item.productId].name" />
                <div>
                  <strong>{{ products[item.productId]?.name || `商品 #${item.productId}` }}</strong>
                  <p class="muted">{{ products[item.productId]?.brand || '加载中' }}</p>
                </div>
              </div>
            </td>
            <td>
              <input class="cart-quantity" v-model.number="item.quantity" type="number" min="1" @change="cart.update(item.id, item.quantity)" />
            </td>
            <td>¥{{ lineTotal(item) }}</td>
            <td>
              <button type="button" class="secondary-button" @click="buySingle(item)">立即购买</button>
              <button type="button" @click="cart.remove(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="empty-state">
      <h2>购物车是空的</h2>
      <p class="muted">先挑几件商品，再回来结算。</p>
      <RouterLink class="primary-button" to="/products">去逛商品</RouterLink>
    </div>

    <div class="actions" v-if="cart.items.length">
      <span class="muted" style="margin-right:8px">已选 {{ selected.size }} 件</span>
      <button class="primary-button" :disabled="selected.size === 0 || submitting" @click="checkoutSelected">
        {{ submitting ? '提交中...' : '购买选中' }}
      </button>
      <button class="secondary-button" :disabled="selected.size === 0" @click="removeSelected">
        删除选中
      </button>
      <button class="secondary-button" @click="selected = new Set(cart.items.map(i => i.id))">
        全选
      </button>
    </div>

    <p v-if="message" :class="failed ? 'error' : 'success'">{{ message }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { getProduct } from '../../api/products'
import { createOrderFromSelected } from '../../api/orders'

const router = useRouter()
const cart = useCartStore()
const products = ref({})
const selected = ref(new Set())
const submitting = ref(false)
const message = ref('')
const failed = ref(false)

const allSelected = computed(() =>
  cart.items.length > 0 && selected.value.size === cart.items.length
)

function toggleAll() {
  if (allSelected.value) {
    selected.value = new Set()
  } else {
    selected.value = new Set(cart.items.map(i => i.id))
  }
}

function toggleOne(id) {
  const next = new Set(selected.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selected.value = next
}

function toFixed(n) {
  return Math.round(n * 100) / 100
}

function lineTotal(item) {
  const price = Number(products.value[item.productId]?.price || 0)
  return toFixed(price * item.quantity)
}

const selectedTotal = computed(() =>
  toFixed(cart.items
    .filter(item => selected.value.has(item.id))
    .reduce((sum, item) => sum + lineTotal(item), 0))
)

async function checkoutSelected() {
  if (selected.value.size === 0) return
  submitting.value = true
  message.value = ''
  failed.value = false
  try {
    const order = await createOrderFromSelected([...selected.value])
    selected.value = new Set()
    await cart.load()
    router.push(`/orders/${order.id}`)
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '提交订单失败'
  } finally {
    submitting.value = false
  }
}

async function buySingle(item) {
  submitting.value = true
  message.value = ''
  failed.value = false
  try {
    const order = await createOrderFromSelected([item.id])
    await cart.load()
    router.push(`/orders/${order.id}`)
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '提交订单失败'
  } finally {
    submitting.value = false
  }
}

async function removeSelected() {
  const ids = [...selected.value]
  if (ids.length === 0) return
  await Promise.all(ids.map(id => cart.remove(id)))
  selected.value = new Set()
}

async function loadProducts(items) {
  const missingIds = [...new Set(items.map((item) => item.productId))]
    .filter((id) => !products.value[id])
  if (!missingIds.length) return
  const loaded = await Promise.all(missingIds.map((id) => getProduct(id)))
  products.value = {
    ...products.value,
    ...Object.fromEntries(loaded.map((product) => [product.id, product]))
  }
}

watch(() => cart.items, (items) => loadProducts(items), { deep: true })

onMounted(async () => {
  await cart.load()
  await loadProducts(cart.items)
})
</script>
