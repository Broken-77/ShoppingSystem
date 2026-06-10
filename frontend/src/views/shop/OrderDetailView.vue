<template>
  <section class="page-stack">
    <div class="panel section-header">
      <div>
        <h1 class="section-title">订单详情</h1>
        <p class="muted">查看订单商品、金额和支付状态。</p>
      </div>
      <RouterLink class="ghost-button" to="/orders">返回订单</RouterLink>
    </div>

    <article v-if="order" class="panel stack">
      <div class="section-header">
        <div>
          <strong>{{ order.orderNo }}</strong>
          <p class="muted">
            创建 {{ formatTime(order.createdAt) }}
            <span v-if="order.paidAt"> 支付 {{ formatTime(order.paidAt) }}</span>
          </p>
        </div>
        <div class="actions" style="min-width:220px;display:flex;align-items:center;gap:8px;justify-content:flex-end">
          <strong class="price">¥{{ order.totalAmount }}</strong>
          <span class="status-pill" :class="statusClass(order.status)">{{ statusLabel(order.status) }}</span>
        </div>
      </div>

      <div class="table-wrap">
        <table class="list-table">
          <thead>
            <tr>
              <th>商品</th>
              <th style="text-align:right">数量</th>
              <th style="text-align:right">小计</th>
              <th>评价</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in order.items" :key="item.id">
              <td>{{ item.productName }}</td>
              <td style="text-align:right">{{ item.quantity }}</td>
              <td style="text-align:right">¥{{ item.subtotal }}</td>
              <td>
                <RouterLink class="secondary-button" :to="`/products/${item.productId}`">评价</RouterLink>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="checkout-summary">
        <p>订单金额 ¥{{ order.totalAmount }}</p>
        <p>当前状态 {{ statusLabel(order.status) }}</p>
        <div class="actions">
          <button
            v-if="order.status === 'PENDING_PAYMENT'"
            class="primary-button"
            type="button"
            :disabled="paying"
            @click="pay"
          >
            {{ paying ? '支付中' : '立即支付' }}
          </button>
          <button
            v-if="order.status === 'PENDING_PAYMENT'"
            class="secondary-button"
            type="button"
            :disabled="cancelling"
            @click="cancel"
          >
            {{ cancelling ? '取消中' : '取消订单' }}
          </button>
          <RouterLink class="secondary-button" to="/products">继续购物</RouterLink>
        </div>
      </div>

      <p v-if="message" :class="failed ? 'error' : 'success'">{{ message }}</p>
    </article>

    <div v-else-if="loading" class="empty-state">
      <h2>正在加载订单</h2>
      <p class="muted">请稍等片刻。</p>
    </div>

    <div v-else class="empty-state">
      <h2>订单不存在</h2>
      <p class="muted">请返回订单列表重新选择。</p>
      <RouterLink class="primary-button" to="/orders">返回订单</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getOrder, payOrder, cancelOrder } from '../../api/orders'

const route = useRoute()
const order = ref(null)
const loading = ref(false)
const paying = ref(false)
const cancelling = ref(false)
const message = ref('')
const failed = ref(false)

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

async function load() {
  loading.value = true
  message.value = ''
  failed.value = false
  try {
    order.value = await getOrder(route.params.id)
  } catch (err) {
    order.value = null
    failed.value = true
    message.value = err.response?.data?.message || '加载订单失败'
  } finally {
    loading.value = false
  }
}

async function pay() {
  if (!order.value || order.value.status !== 'PENDING_PAYMENT') {
    return
  }
  paying.value = true
  message.value = ''
  failed.value = false
  try {
    order.value = await payOrder(order.value.id)
    message.value = '支付成功'
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '支付失败'
  } finally {
    paying.value = false
  }
}

async function cancel() {
  if (!order.value || order.value.status !== 'PENDING_PAYMENT') {
    return
  }
  cancelling.value = true
  message.value = ''
  failed.value = false
  try {
    order.value = await cancelOrder(order.value.id)
    message.value = '订单已取消'
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '取消失败'
  } finally {
    cancelling.value = false
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>
