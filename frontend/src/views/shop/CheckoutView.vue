<template>
  <section class="panel stack">
    <h1>结算</h1>
    <button class="primary-button" type="button" @click="create">提交订单</button>
    <div v-if="order" class="stack">
      <p>订单号 {{ order.orderNo }}</p>
      <p>金额 ¥{{ order.totalAmount }}</p>
      <p>状态 {{ order.status }}</p>
      <button v-if="order.status === 'PENDING_PAYMENT'" class="primary-button" type="button" @click="pay">支付</button>
    </div>
    <p v-if="message" :class="{ error: failed }">{{ message }}</p>
  </section>
</template>

<script setup>
import { ref } from 'vue'
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
