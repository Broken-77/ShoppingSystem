import { api, unwrap } from './client'

export async function listAdminProducts() {
  return unwrap(await api.get('/admin/products'))
}

export async function createAdminProduct(body) {
  return unwrap(await api.post('/admin/products', body))
}

export async function updateAdminProduct(id, body) {
  return unwrap(await api.put(`/admin/products/${id}`, body))
}

export async function markProductOnSale(id) {
  return unwrap(await api.post(`/admin/products/${id}/on-sale`))
}

export async function markProductOffSale(id) {
  return unwrap(await api.post(`/admin/products/${id}/off-sale`))
}

export async function listAdminCategories() {
  return unwrap(await api.get('/admin/categories'))
}

export async function createAdminCategory(body) {
  return unwrap(await api.post('/admin/categories', body))
}

export async function updateAdminCategory(id, body) {
  return unwrap(await api.put(`/admin/categories/${id}`, body))
}

export async function disableAdminCategory(id) {
  return unwrap(await api.post(`/admin/categories/${id}/disable`))
}

export async function listAdminOrders() {
  return unwrap(await api.get('/admin/orders'))
}

export async function updateAdminOrderStatus(id, status) {
  return unwrap(await api.put(`/admin/orders/${id}/status`, { status }))
}
