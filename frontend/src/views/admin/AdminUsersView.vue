<template>
  <AdminLayout>
    <div class="panel">
      <div class="section-header">
        <div>
          <h1 class="section-title">用户管理</h1>
          <p class="muted">查看用户兴趣标签、相似用户，可禁用或启用账号。</p>
        </div>
      </div>
      <div class="table-wrap">
        <table class="list-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>兴趣标签</th>
              <th>相似用户</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in pageItems" :key="u.id">
              <td>
                <strong>{{ u.username }}</strong>
                <p class="muted" style="font-size:11px">{{ u.role }}{{ u.status === 'ACTIVE' ? '' : '·已禁用' }}</p>
              </td>
              <td>
                <span v-for="t in u.interests" :key="t.categoryName" class="tag-pill"
                  :style="{background: catColor(t.categoryName), color:'white', margin:'0 3px 3px 0', fontSize:'12px', padding:'2px 8px', borderRadius:'12px', display:'inline-block'}">
                  {{ t.categoryName }}
                </span>
                <span v-if="!u.interests.length" class="muted" style="font-size:12px">无兴趣数据</span>
              </td>
              <td>
                <span v-for="s in u.similarUsers" :key="s.userId" style="font-size:12px;background:#e8f4fd;padding:2px 8px;border-radius:12px;margin:0 3px 3px 0;display:inline-block">
                  ~{{ s.username }}
                </span>
                <span v-if="!u.similarUsers.length" class="muted" style="font-size:12px">无相似用户</span>
              </td>
              <td>{{ formatTime(u.id ? u.id : '') }}</td>
              <td>
                <button v-if="u.status === 'ACTIVE' && u.role !== 'ADMIN'" type="button" @click="toggleStatus(u)">禁用</button>
                <button v-else-if="u.status !== 'ACTIVE'" type="button" @click="toggleStatus(u)">启用</button>
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

function catColor(name) {
  const m = {'手机数码':'#3498db','电脑办公':'#2c3e50','家居生活':'#27ae60',
    '运动户外':'#e67e22','美妆个护':'#e91e63','食品饮料':'#d35400'}
  return m[name] || '#888'
}

function formatTime(v) {
  // 用户 profile 没有 createdAt，用注册时间列占位
  return '-'
}

async function toggleStatus(u) {
  const endpoint = u.status === 'ACTIVE'
    ? `/admin/users/${u.id}/disable`
    : `/admin/users/${u.id}/enable`
  await unwrap(await api.post(endpoint))
  await load()
}

async function load() {
  users.value = await unwrap(await api.get('/admin/users/profiles'))
}

onMounted(load)
</script>
