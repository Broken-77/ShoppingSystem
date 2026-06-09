import assert from 'node:assert/strict'
import { clampPage, pageCount, paginateItems } from '../src/utils/pagination.mjs'

assert.equal(pageCount(0, 20), 1)
assert.equal(pageCount(159, 25), 7)
assert.equal(clampPage(0, 159, 25), 1)
assert.equal(clampPage(99, 159, 25), 7)
assert.deepEqual(paginateItems([1, 2, 3, 4, 5], 2, 2), [3, 4])

console.log('pagination tests passed')
