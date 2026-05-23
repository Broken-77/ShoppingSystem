<template>
  <section class="stack">
    <h1>我的订单</h1>
    <article v-for="order in orders" :key="order.id" class="panel stack">
      <strong>{{ order.orderNo }}</strong>
      <p>金额 ¥{{ order.totalAmount }} · {{ order.status }}</p>
      <p class="muted">创建 {{ formatTime(order.createdAt) }} <span v-if="order.paidAt">· 支付 {{ formatTime(order.paidAt) }}</span></p>
      <table class="list-table">
        <tbody>
          <tr v-for="item in order.items" :key="item.id">
            <td>{{ item.productName }}</td>
            <td>{{ item.quantity }}</td>
            <td>¥{{ item.subtotal }}</td>
          </tr>
        </tbody>
      </table>
    </article>
    <p v-if="!orders.length" class="muted">暂无订单</p>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { listOrders } from '../../api/orders'

const orders = ref([])

function formatTime(value) {
  return value ? new Date(value).toLocaleString() : ''
}

onMounted(async () => {
  orders.value = await listOrders()
})
</script>
