import { api, unwrap } from './client'

export async function login(username, password) {
  return unwrap(await api.post('/auth/login', { username, password }))
}

export async function register(username, password) {
  return unwrap(await api.post('/auth/register', { username, password }))
}
