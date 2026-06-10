function normalize(value) {
  return String(value ?? '').trim().toLocaleLowerCase()
}

export function filterProducts(products, query, categoryNameForId) {
  const normalizedQuery = normalize(query)
  if (!normalizedQuery) return products

  return products.filter((product) => {
    const searchableText = [
      product.name,
      product.brand,
      categoryNameForId(product.categoryId)
    ].map(normalize).join(' ')

    return searchableText.includes(normalizedQuery)
  })
}
