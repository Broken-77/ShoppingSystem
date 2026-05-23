import { api, unwrap } from './client'

export async function listProducts(params = {}) {
  return unwrap(await api.get('/products', { params }))
}

export async function getProduct(id) {
  return unwrap(await api.get(`/products/${id}`))
}

export async function similarProducts(id) {
  return unwrap(await api.get(`/products/${id}/similar`))
}

export async function listCategories() {
  return unwrap(await api.get('/categories'))
}
