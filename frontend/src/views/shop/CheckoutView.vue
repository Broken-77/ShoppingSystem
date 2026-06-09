<template>
  <section class="panel stack">
    <div class="section-header">
      <div>
        <h1 class="section-title">结算</h1>
        <p class="muted">提交订单后可立即完成支付。</p>
      </div>
      <button class="primary-button" type="button" @click="create">提交订单</button>
    </div>
    <div v-if="order" class="checkout-summary">
      <p>订单号 {{ order.orderNo }}</p>
      <p>金额 ¥{{ order.totalAmount }}</p>
      <p>状态 {{ order.status }}</p>
      <div class="actions">
        <button v-if="order.status === 'PENDING_PAYMENT'" class="primary-button" type="button" @click="pay">支付</button>
        <RouterLink class="secondary-button" :to="`/orders/${order.id}`">查看订单详情</RouterLink>
      </div>
    </div>
    <p v-if="message" :class="failed ? 'error' : 'success'">{{ message }}</p>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { createOrder, payOrder } from '../../api/orders'
import { useCartStore } from '../../stores/cart'

const cart = useCartStore()
const order = ref(null)
const message = ref('')
const failed = ref(false)

async function create() {
  failed.value = false
  message.value = ''
  try {
    order.value = await createOrder()
    await cart.load()
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '提交订单失败'
  }
}

async function pay() {
  failed.value = false
  order.value = await payOrder(order.value.id)
  message.value = '支付成功'
}
</script>
