<template>
  <AdminLayout>
    <section class="panel stack">
      <div class="section-header">
        <div>
          <h1 class="section-title">商品管理</h1>
          <p class="muted">维护商品信息、库存和上下架状态</p>
        </div>
        <button type="button" @click="resetForm">新增商品</button>
      </div>

      <form class="admin-form" @submit.prevent="save">
        <label class="form-row">
          <span>商品名称</span>
          <input v-model.trim="form.name" required />
        </label>
        <label class="form-row">
          <span>分类</span>
          <select v-model.number="form.categoryId" required>
            <option disabled value="">请选择</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </label>
        <label class="form-row">
          <span>品牌</span>
          <input v-model.trim="form.brand" />
        </label>
        <label class="form-row">
          <span>价格</span>
          <input v-model.number="form.price" type="number" min="0" step="0.01" required />
        </label>
        <label class="form-row">
          <span>库存</span>
          <input v-model.number="form.stock" type="number" min="0" required />
        </label>
        <label class="form-row">
          <span>状态</span>
          <select v-model="form.status">
            <option value="ON_SALE">上架</option>
            <option value="OFF_SALE">下架</option>
          </select>
        </label>
        <label class="form-row form-row-wide">
          <span>图片 URL</span>
          <input v-model.trim="form.imageUrl" />
        </label>
        <label class="form-row form-row-wide">
          <span>描述</span>
          <textarea v-model.trim="form.description" rows="3" />
        </label>
        <div class="actions form-row-wide">
          <button class="primary-button" type="submit">{{ editingId ? '保存修改' : '创建商品' }}</button>
          <button v-if="editingId" type="button" @click="resetForm">取消编辑</button>
        </div>
      </form>

      <p v-if="message" :class="failed ? 'error' : 'success'">{{ message }}</p>

      <div class="table-wrap">
        <table class="list-table">
          <thead>
            <tr>
              <th>商品</th>
              <th>分类</th>
              <th>价格</th>
              <th>库存</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.id">
              <td>
                <div class="cart-product">
                  <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" />
                  <div>
                    <strong>{{ product.name }}</strong>
                    <p class="muted">{{ product.brand || '无品牌' }}</p>
                  </div>
                </div>
              </td>
              <td>{{ categoryName(product.categoryId) }}</td>
              <td>¥{{ product.price }}</td>
              <td>{{ product.stock }}</td>
              <td>
                <span class="status-pill" :class="product.status === 'ON_SALE' ? 'is-live' : 'is-off'">
                  {{ product.status === 'ON_SALE' ? '上架' : '下架' }}
                </span>
              </td>
              <td>
                <div class="actions">
                  <button type="button" @click="edit(product)">编辑</button>
                  <button type="button" @click="toggleStatus(product)">
                    {{ product.status === 'ON_SALE' ? '下架' : '上架' }}
                  </button>
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
  createAdminProduct,
  listAdminCategories,
  listAdminProducts,
  markProductOffSale,
  markProductOnSale,
  updateAdminProduct
} from '../../api/admin'

const products = ref([])
const categories = ref([])
const editingId = ref(null)
const message = ref('')
const failed = ref(false)

const emptyForm = {
  categoryId: '',
  name: '',
  description: '',
  brand: '',
  price: 0,
  stock: 0,
  imageUrl: '',
  status: 'ON_SALE',
  salesCount: 0
}

const form = reactive({ ...emptyForm })

function categoryName(id) {
  return categories.value.find((category) => category.id === id)?.name || `分类 #${id}`
}

function resetForm() {
  Object.assign(form, emptyForm)
  editingId.value = null
  message.value = ''
  failed.value = false
}

function edit(product) {
  Object.assign(form, {
    categoryId: product.categoryId,
    name: product.name,
    description: product.description || '',
    brand: product.brand || '',
    price: Number(product.price),
    stock: product.stock,
    imageUrl: product.imageUrl || '',
    status: product.status,
    salesCount: product.salesCount || 0
  })
  editingId.value = product.id
}

async function load() {
  const [productRows, categoryRows] = await Promise.all([
    listAdminProducts(),
    listAdminCategories()
  ])
  products.value = productRows
  categories.value = categoryRows
}

async function save() {
  failed.value = false
  message.value = ''
  try {
    const body = { ...form, categoryId: Number(form.categoryId) }
    if (editingId.value) {
      await updateAdminProduct(editingId.value, body)
      resetForm()
      message.value = '商品已更新'
    } else {
      await createAdminProduct(body)
      resetForm()
      message.value = '商品已创建'
    }
    await load()
  } catch (err) {
    failed.value = true
    message.value = err.response?.data?.message || '保存商品失败'
  }
}

async function toggleStatus(product) {
  if (product.status === 'ON_SALE') {
    await markProductOffSale(product.id)
  } else {
    await markProductOnSale(product.id)
  }
  await load()
}

onMounted(load)
</script>
