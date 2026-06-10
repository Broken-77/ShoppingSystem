import assert from 'node:assert/strict'
import { filterProducts } from '../src/utils/productSearch.mjs'

const products = [
  { id: 1, name: 'Aurora Phone 15', brand: 'Aurora', categoryId: 10 },
  { id: 2, name: 'Nova Mini Phone', brand: 'Nova', categoryId: 10 },
  { id: 3, name: 'Trail Running Shoes', brand: 'Peak', categoryId: 20 }
]

const categoryNames = new Map([
  [10, '手机数码'],
  [20, '运动户外']
])
const categoryNameForId = (id) => categoryNames.get(id) || ''

assert.deepEqual(filterProducts(products, '', categoryNameForId), products)
assert.deepEqual(filterProducts(products, '  AURORA phone  ', categoryNameForId).map((item) => item.id), [1])
assert.deepEqual(filterProducts(products, 'nova', categoryNameForId).map((item) => item.id), [2])
assert.deepEqual(filterProducts(products, '运动户外', categoryNameForId).map((item) => item.id), [3])
assert.deepEqual(filterProducts(products, 'not-found', categoryNameForId), [])

console.log('product search tests passed')
