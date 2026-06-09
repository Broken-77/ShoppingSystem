import { api, unwrap } from './client'

export async function getReviews(productId) {
  return unwrap(await api.get(`/reviews/products/${productId}`))
}

export async function createReview(productId, rating, comment) {
  return unwrap(await api.post('/reviews', { productId, rating, comment }))
}

export async function replyReview(reviewId, comment) {
  return unwrap(await api.post(`/reviews/${reviewId}/reply`, { comment }))
}
