import { api, unwrap } from './client'

export async function listOrders() {
  return unwrap(await api.get('/orders'))
}

export async function getOrder(id) {
  return unwrap(await api.get(`/orders/${id}`))
}

export async function createOrder() {
  return unwrap(await api.post('/orders'))
}

export async function createOrderFromSelected(cartItemIds) {
  return unwrap(await api.post('/orders/selected', { cartItemIds }))
}

export async function payOrder(id) {
  return unwrap(await api.post(`/orders/${id}/pay`))
}
