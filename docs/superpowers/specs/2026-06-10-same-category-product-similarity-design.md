# Same-Category Product Similarity Design

## Goal

Improve the product-detail "similar products" results so that unrelated products from other categories are never shown as similar.

## Scope

This change applies only to `RecommendationService.similarProducts` and `RecommendationEngine.similarProductIds`. The personalized home recommendation algorithm remains unchanged.

## Candidate Selection

The source product must exist and be on sale. A candidate is eligible only when all of the following are true:

- It is not the source product.
- It belongs to the same category as the source product.
- It is on sale and has stock available.

Cross-category products must never appear in the product-detail similar-products list, including fallback results.

## Behavioral Similarity

For eligible candidates, retain the existing cosine similarity over time-decayed user behavior vectors:

```text
cosine(A, B) = dot(A, B) / (norm(A) * norm(B))
```

Reduce sparse-data noise by multiplying cosine similarity by a confidence factor based on the number of users who interacted with both products:

```text
confidence = min(commonUserCount / 3.0, 1.0)
adjustedSimilarity = cosineSimilarity * confidence
```

This means one common user contributes one third of the raw cosine score, two common users contribute two thirds, and three or more common users receive the full score.

## Ranking And Fallback

Candidates with a positive adjusted similarity are ranked by:

1. Adjusted similarity descending.
2. Product ID ascending for deterministic ties.

The service then appends remaining same-category products ordered by sales count descending. Duplicate products are removed while preserving order, and the final list is limited to 12 products.

Global hot products, global new products, and same-brand products from other categories are not used as fallback for this placement.

## Tests

Automated tests will verify that:

- Cross-category products are excluded even when their behavioral cosine similarity is high.
- A candidate supported by more common users ranks above a candidate supported by only one common user when their raw similarity would otherwise be misleading.
- Sparse or absent behavior data falls back to same-category products ordered by sales count.
- Existing personalized home recommendation behavior is unchanged.

