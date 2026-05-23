<template>
  <section class="panel stack">
    <h1>购物车</h1>
    <table v-if="cart.items.length" class="list-table">
      <thead>
        <tr>
          <th>商品</th>
          <th>数量</th>
          <th>小计</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in cart.items" :key="item.id">
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
            <input v-model.number="item.quantity" type="number" min="1" @change="cart.update(item.id, item.quantity)" />
          </td>
          <td>¥{{ lineTotal(item) }}</td>
          <td><button type="button" @click="cart.remove(item.id)">删除</button></td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">购物车是空的</p>
    <div class="actions">
      <RouterLink class="primary-button" to="/checkout">去结算</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { getProduct } from '../../api/products'

const cart = useCartStore()
const products = ref({})

function lineTotal(item) {
  const price = Number(products.value[item.productId]?.price || 0)
  return price ? price * item.quantity : 0
}

async function loadProducts(items) {
  const missingIds = [...new Set(items.map((item) => item.productId))]
    .filter((id) => !products.value[id])

  if (!missingIds.length) {
    return
  }

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
