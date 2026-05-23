import { api, unwrap } from './client'

export async function homeRecommendations() {
  return unwrap(await api.get('/recommendations/home'))
}

export async function cartRecommendations() {
  return unwrap(await api.get('/recommendations/cart'))
}
