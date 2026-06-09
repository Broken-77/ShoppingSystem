<template>
  <AdminLayout>
    <div class="panel">
      <div class="section-header">
        <div>
          <h1 class="section-title">用户管理</h1>
          <p class="muted">查看所有用户，可禁用或启用账号。</p>
        </div>
      </div>
      <div class="table-wrap">
        <table class="list-table">
          <thead>
            <tr><th>ID</th><th>用户名</th><th>角色</th><th>状态</th><th>注册时间</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="u in pageItems" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.username }}</td>
              <td><span class="status-pill">{{ u.role }}</span></td>
              <td><span class="status-pill" :class="u.status === 'ACTIVE' ? 'is-paid' : 'is-cancelled'">{{ u.status === 'ACTIVE' ? '正常' : '已禁用' }}</span></td>
              <td>{{ formatTime(u.createdAt) }}</td>
              <td>
                <button v-if="u.status === 'ACTIVE'" type="button" @click="toggleStatus(u)">禁用</button>
                <button v-else type="button" @click="toggleStatus(u)">启用</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page <= 1" @click="page--">上一页</button>
        <span>{{ page }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages" @click="page++">下一页</button>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '../../components/AdminLayout.vue'
import { api, unwrap } from '../../api/client'

const users = ref([])
const page = ref(1)
const PAGE_SIZE = 15

const pageItems = computed(() => {
  const start = (page.value - 1) * PAGE_SIZE
  return users.value.slice(start, start + PAGE_SIZE)
})
const totalPages = computed(() => Math.ceil(users.value.length / PAGE_SIZE))

function formatTime(v) { return v ? new Date(v).toLocaleString() : '' }

async function toggleStatus(u) {
  if (u.role === 'ADMIN' && u.status === 'ACTIVE') { alert('不能禁用管理员'); return }
  const endpoint = u.status === 'ACTIVE'
    ? `/admin/users/${u.id}/disable`
    : `/admin/users/${u.id}/enable`
  await unwrap(await api.post(endpoint))
  await load()
}

async function load() {
  users.value = await unwrap(await api.get('/admin/users'))
}

onMounted(load)
</script>
