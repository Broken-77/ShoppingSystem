<template>
  <section class="page-stack">
    <div class="panel section-header">
      <div>
        <h1 class="section-title">我的订单</h1>
        <p class="muted">查看每一笔订单的商品、金额和支付状态。</p>
      </div>
      <span class="status-pill">{{ orders.length }} 笔订单</span>
    </div>
    <article v-for="order in orders" :key="order.id" class="panel stack">
      <div class="section-header">
        <div>
          <strong>{{ order.orderNo }}</strong>
          <p class="muted">创建 {{ formatTime(order.createdAt) }} <span v-if="order.paidAt"> 支付 {{ formatTime(order.paidAt) }}</span></p>
        </div>
        <div class="actions" style="min-width:280px;display:flex;align-items:center;gap:8px;justify-content:flex-end">
          <strong class="price">¥{{ order.totalAmount }}</strong>
          <span class="status-pill" :class="statusClass(order.status)">{{ statusLabel(order.status) }}</span>
          <RouterLink class="secondary-button" :to="`/orders/${order.id}`">
            {{ order.status === 'PENDING_PAYMENT' ? '去支付' : '查看详情' }}
          </RouterLink>
          <button
            v-if="order.status === 'PENDING_PAYMENT'"
            type="button"
            :disabled="cancelling === order.id"
            @click="cancel(order.id)"

>
            {{ cancelling === order.id ? '取消中' : '取消订单' }}
          </button>
        </div>
      </div>
      <div class="table-wrap">
        <table class="list-table">
          <thead>
            <tr><th>商品</th><th style="text-align:right">数量</th><th style="text-align:right">小计</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in order.items" :key="item.id">
              <td>{{ item.productName }}</td>
              <td style="text-align:right">{{ item.quantity }}</td>
              <td style="text-align:right">¥{{ item.subtotal }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>
    <div v-if="!orders.length" class="empty-state">
      <h2>暂无订单</h2>
      <p class="muted">完成一次结算后，订单会出现在这里。</p>
      <RouterLink class="primary-button" to="/products">去逛商品</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listOrders, cancelOrder } from '../../api/orders'

const orders = ref([])
const cancelling = ref(null)

function formatTime(value) {
  return value ? new Date(value).toLocaleString() : ''
}

function statusClass(status) {
  return {
    'is-paid': status === 'PAID',
    'is-finished': status === 'FINISHED',
    'is-cancelled': status === 'CANCELLED'
  }
}

function statusLabel(status) {
  const labels = {
    PENDING_PAYMENT: '待支付',
    PAID: '已支付',
    CANCELLED: '已取消',
    FINISHED: '已完成'
  }
  return labels[status] || status
}

onMounted(async () => {
  orders.value = await listOrders()
})

async function cancel(id) {
  cancelling.value = id
  try {
    await cancelOrder(id)
    orders.value = await listOrders()
  } catch (err) {
    alert(err.response?.data?.message || '取消失败')
  } finally {
    cancelling.value = null
  }
}
</script>
