<template>
  <AdminLayout>
    <section class="panel stack">
      <div class="section-header">
        <div>
          <h1 class="section-title">分类管理</h1>
          <p class="muted">维护前台筛选使用的商品分类</p>
        </div>
        <button type="button" @click="resetForm">新增分类</button>
      </div>

      <form class="admin-form" @submit.prevent="save">
        <label class="form-row">
          <span>分类名称</span>
          <input v-model.trim="form.name" required />
        </label>
        <label class="form-row">
          <span>父级 ID</span>
          <input v-model.number="form.parentId" type="number" min="1" placeholder="可留空" />
        </label>
        <label class="form-row">
          <span>排序</span>
          <input v-model.number="form.sortOrder" type="number" min="0" />
        </label>
        <label class="form-row">
          <span>状态</span>
          <select v-model="form.enabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </label>
        <div class="actions">
          <button class="primary-button" type="submit">{{ editingId ? '保存修改' : '创建分类' }}</button>
          <button v-if="editingId" type="button" @click="resetForm">取消编辑</button>
        </div>
      </form>

      <p v-if="message" :class="failed ? 'error' : 'success'">{{ message }}</p>

      <div class="table-wrap">
        <table class="list-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>名称</th>
              <th>父级</th>
              <th>排序</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in categories" :key="category.id">
              <td>{{ category.id }}</td>
              <td>{{ category.name }}</td>
              <td>{{ category.parentId || '-' }}</td>
              <td>{{ category.sortOrder }}</td>
              <td>
                <span class="status-pill" :class="category.enabled ? 'is-live' : 'is-off'">
                  {{ category.enabled ? '启用' : '禁用' }}
                </span>
              </td>
              <td>
                <div class="actions">
                  <button type="button" @click="edit(category)">编辑</button>
                  <button v-if="category.enabled" type="button" @click="disable(category.id)">禁用</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </AdminLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import AdminLayout from '../../components/AdminLayout.vue'
import {
  createAdminCategory,
  disableAdminCategory,
  listAdminCategories,
  updateAdminCategory
} from '../../api/admin'

const categories = ref([])
const editingId = ref(null)
const message = ref('')
const failed = ref(false)

const emptyForm = {
  name: '',
  parentId: '',
  enabled: true,
  sortOrder: 0
}

const form = reactive({ ...emptyForm })

function resetForm() {
  Object.assign(form, emptyForm)
  editingId.value = null
  message.value = ''
  failed.value = false
}

function edit(category) {
  Object.assign(form, {
    name: category.name,
    parentId: category.parentId || '',
    enabled: category.enabled,
    sortOrder: category.sortOrder || 0
  })
  editingId.value = category.id
}

function payload() {
  return {
    ...form,
    parentId: form.parentId ? Number(form.parentId) : null,
    sortOrder: Number(form.sortOrder || 0)
  }
}

async function load() {
  categories.value = await listAdminCategories()
}

async function save() {
  failed.value = false
  message.value = ''
  try {
    if (editingId.value) {
      await updateAdminCategory(editingId.value, payload())
      resetForm()
      message.value = '分类已更新'
    } else {
      await createAdminCategory(payload())
      resetForm()
      message.value = '分类已创建'
    }
    await load()
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '保存分类失败'
  }
}

async function disable(id) {
  await disableAdminCategory(id)
  await load()
}

onMounted(load)
</script>
