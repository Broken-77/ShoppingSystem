# Home Interest Recommendation Design

## Goal

Make the home-page "猜你喜欢" reflect the current user's recent product interest while preventing already purchased products from being recommended again.

## Scope

This change applies to personalized home, cart, and user recommendation results because they share `RecommendationService.homeRecommendations`. It does not change behavior recording, database tables, API response shapes, or the product-detail similar-products placement.

## Recommendation Rules

Products the user viewed or added to the cart but has not purchased remain eligible. They may appear directly in the recommendation list because they represent unfinished interest.

Products the user has purchased are never eligible themselves. They remain preference sources and contribute recommendations for related products in the same category.

All recommendation sources, including collaborative filtering, category-hot fallback, and new-product fallback, must exclude purchased product IDs. This prevents a purchased product from being removed by the engine and then reintroduced during service-level fallback.

Only on-sale products with positive stock are eligible for the final list.

## Interest Weighting

Repeated views increase direct interest with logarithmic growth:

```text
viewInterest = (1 + ln(viewCount)) * latestViewTimeDecay
```

The first view receives a score of `1`. Additional views increase the score, but each additional view contributes less than the previous one. This limits the effect of repeated page refreshes.

Cart and order behavior retain their stronger base importance. Repeated events use the same logarithmic damping:

```text
cartInterest  = 4 * (1 + ln(cartCount)) * latestCartTimeDecay
orderInterest = 8 * (1 + ln(orderCount)) * latestOrderTimeDecay
```

The existing time-decay bands remain unchanged. For each behavior type and product, the most recent event determines the time-decay factor so old repeated events do not outweigh recent intent indefinitely.

The preference score for a product is the sum of its view, cart, and order interest. Direct display eligibility and preference strength are separate: an ordered product has a strong preference score but is excluded from the output.

## Related Product Scoring

For each eligible candidate, calculate related-product contributions from the user's preference products in the same category:

```text
relatedContribution = sourcePreference * adjustedSimilarity(source, candidate)
candidateScore = directInterest(candidate) + sum(relatedContribution)
```

`directInterest` includes only the candidate's own view and cart interest. Order interest is never a direct-display score because purchased products are excluded.

`adjustedSimilarity` reuses cosine similarity and the existing common-user confidence correction:

```text
confidence = min(commonUserCount / 3.0, 1.0)
adjustedSimilarity = cosineSimilarity * confidence
```

The same-category condition applies to every source-to-candidate relationship. In particular, a purchased phone can recommend other phones in its category but cannot recommend sports or food products merely because one user interacted with both.

Candidates are divided into two ranking tiers:

1. Unpurchased candidates with direct view or cart interest.
2. Candidates supported only by related-product contributions.

The direct-interest tier always ranks before the related-only tier. Direct-interest candidates are ranked by their latest view or cart time descending, then composite score descending, then product ID ascending. Related-only candidates are ranked by composite score descending, then product ID ascending. This ensures that opening a product immediately increases its home-page priority, while repeat counts and related signals continue to resolve ranking among similarly recent products. Candidates with no positive score are omitted from the personalized engine result.

## Fallback And Limit

The home page continues to return at most 12 products:

1. Personalized candidates ranked by composite score.
2. Hot products from the user's highest-weight categories.
3. New products.

Fallback lists preserve their current ordering, remove duplicates, and exclude every purchased product. They only fill positions not supplied by the personalized result.

## Data Flow

1. Load all behavior rows and the target user's rows.
2. Aggregate the target user's behavior by product and behavior type, counting events and retaining the latest timestamp.
3. Build the purchased-product exclusion set.
4. Build time-decayed product vectors for collaborative similarity.
5. Score eligible on-sale, in-stock candidates using direct interest and same-category related contributions.
6. Sort candidates and return IDs to `RecommendationService`.
7. Merge category-hot and new-product fallback while applying the same purchased-product exclusion.

## Tests

Automated tests will verify that:

- A viewed but unpurchased product may appear in the user's recommendations.
- A product viewed repeatedly ranks above an otherwise equivalent product viewed once.
- A directly viewed product ranks above a higher-scoring candidate supported only by related behavior.
- A more recently viewed product ranks above an older directly viewed product even when the older product has more accumulated views.
- Logarithmic damping is used instead of linear accumulation.
- A purchased product is excluded from personalized, category-hot, and new-product results.
- A purchased product can recommend a behaviorally related product in the same category.
- Cross-category products do not receive related-product score from viewed, carted, or purchased source products.
- Existing product-detail same-category similarity behavior remains unchanged.
- The final recommendation list remains available-only, duplicate-free, deterministic, and limited to 12 products.
