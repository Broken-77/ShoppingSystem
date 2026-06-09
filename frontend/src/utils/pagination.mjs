export function pageCount(totalItems, pageSize) {
  return Math.max(1, Math.ceil(totalItems / pageSize))
}

export function clampPage(page, totalItems, pageSize) {
  const normalizedPage = Number.isFinite(Number(page)) ? Number(page) : 1
  return Math.min(Math.max(1, normalizedPage), pageCount(totalItems, pageSize))
}

export function paginateItems(items, page, pageSize) {
  const currentPage = clampPage(page, items.length, pageSize)
  const start = (currentPage - 1) * pageSize
  return items.slice(start, start + pageSize)
}
