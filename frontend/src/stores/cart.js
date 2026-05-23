import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as cartApi from '../api/cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const count = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))

  async function load() {
    loading.value = true
    try {
      items.value = await cartApi.getCart()
    } finally {
      loading.value = false
    }
  }

  async function add(productId, quantity = 1) {
    await cartApi.addCartItem(productId, quantity)
    await load()
  }

  async function update(id, quantity) {
    await cartApi.updateCartItem(id, quantity)
    await load()
  }

  async function remove(id) {
    await cartApi.deleteCartItem(id)
    await load()
  }

  return {
    items,
    loading,
    count,
    load,
    add,
    update,
    remove
  }
})
