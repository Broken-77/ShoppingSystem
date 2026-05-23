import { api, unwrap } from './client'

export async function getCart() {
  return unwrap(await api.get('/cart'))
}

export async function addCartItem(productId, quantity) {
  return unwrap(await api.post('/cart/items', { productId, quantity }))
}

export async function updateCartItem(id, quantity) {
  return unwrap(await api.put(`/cart/items/${id}`, { quantity }))
}

export async function deleteCartItem(id) {
  return unwrap(await api.delete(`/cart/items/${id}`))
}
