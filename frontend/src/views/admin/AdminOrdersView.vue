<template>
  <AdminLayout>
    <section class="panel stack">
      <div class="section-header">
        <div>
          <h1 class="section-title">订单管理</h1>
          <p class="muted">查看订单明细并维护履约状态</p>
        </div>
        <button type="button" @click="load">刷新</button>
      </div>

      <div v-if="orders.length" class="table-wrap">
        <table class="list-table">
          <thead>
            <tr>
              <th>订单</th>
              <th>用户</th>
              <th>金额</th>
              <th>状态</th>
              <th>商品</th>
              <th>时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>
                <strong>{{ order.orderNo }}</strong>
              </td>
              <td>
                <strong>{{ order.username || `用户 #${order.userId}` }}</strong>
                <p class="muted table-line">ID {{ order.userId }}</p>
              </td>
              <td>¥{{ order.totalAmount }}</td>
              <td>
                <span class="status-pill" :class="statusClass(order.status)">{{ statusLabel(order.status) }}</span>
              </td>
              <td>
                <p v-for="item in order.items" :key="item.id" class="table-line">
                  {{ item.productName }} x {{ item.quantity }}
                </p>
              </td>
              <td>{{ formatTime(order.createdAt) }}</td>
              <td>
                <select :value="order.status" @change="changeStatus(order.id, $event.target.value)">
                  <option v-for="status in statuses" :key="status.value" :value="status.value">
                    {{ status.label }}
                  </option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-state">
        <h2>暂无订单</h2>
        <p class="muted">用户下单后会在这里展示履约状态。</p>
      </div>
      <p v-if="message" :class="failed ? 'error' : 'success'">{{ message }}</p>
    </section>
  </AdminLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AdminLayout from '../../components/AdminLayout.vue'
import { listAdminOrders, updateAdminOrderStatus } from '../../api/admin'

const orders = ref([])
const message = ref('')
const failed = ref(false)

const statuses = [
  { value: 'PENDING_PAYMENT', label: '待支付' },
  { value: 'PAID', label: '已支付' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'FINISHED', label: '已完成' }
]

function statusLabel(value) {
  return statuses.find((status) => status.value === value)?.label || value
}

function statusClass(value) {
  return {
    'is-paid': value === 'PAID',
    'is-finished': value === 'FINISHED',
    'is-cancelled': value === 'CANCELLED'
  }
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString() : ''
}

async function load() {
  orders.value = await listAdminOrders()
}

async function changeStatus(id, status) {
  failed.value = false
  message.value = ''
  try {
    await updateAdminOrderStatus(id, status)
    message.value = '订单状态已更新'
    await load()
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '更新订单失败'
  }
}

onMounted(load)
</script>
