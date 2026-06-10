# Same-Category Product Similarity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restrict product-detail similarity results to the source product's category and reduce sparse behavior noise with common-user confidence weighting.

**Architecture:** `ItemBasedCollaborativeFilteringEngine` will filter candidates using product metadata before scoring and multiply cosine similarity by a confidence factor based on common users. `RecommendationService` will fill remaining positions only with same-category products ordered by sales count.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA, JUnit 5, AssertJ, H2

---

### Task 1: Same-Category Behavioral Similarity

**Files:**
- Modify: `backend/src/test/java/com/wms/shoppingsys/service/ItemBasedCollaborativeFilteringEngineTest.java`
- Modify: `backend/src/main/java/com/wms/shoppingsys/service/ItemBasedCollaborativeFilteringEngine.java`

- [x] **Step 1: Write failing engine tests**

Add products in two categories and verify `similarProductIds` excludes a cross-category candidate even when it shares users with the source. Add a ranking case where three common users make a candidate outrank a one-common-user candidate after confidence correction.

- [x] **Step 2: Run the engine test and verify RED**

Run: `mvn -q -Dtest=ItemBasedCollaborativeFilteringEngineTest test`

Expected: the cross-category exclusion or confidence-ranking assertion fails against the current raw cosine implementation.

- [x] **Step 3: Implement category filtering and confidence weighting**

Load the source product, build an ID-to-product map, keep only on-sale in-stock candidates from the same category, and score each candidate with:

```text
adjustedSimilarity = cosineSimilarity * min(commonUserCount / 3.0, 1.0)
```

- [x] **Step 4: Run the engine test and verify GREEN**

Run: `mvn -q -Dtest=ItemBasedCollaborativeFilteringEngineTest test`

Expected: all engine tests pass.

### Task 2: Same-Category Sales Fallback

**Files:**
- Create: `backend/src/test/java/com/wms/shoppingsys/service/RecommendationServiceTest.java`
- Modify: `backend/src/main/java/com/wms/shoppingsys/service/RecommendationService.java`

- [x] **Step 1: Write a failing service test**

Create a source product, two same-category products with different sales counts, and a higher-selling cross-category product. With no behavior data, assert that only the same-category products are returned and that they are ordered by sales count descending.

- [x] **Step 2: Run the service test and verify RED**

Run: `mvn -q -Dtest=RecommendationServiceTest test`

Expected: the current global hot/new fallback includes the cross-category product or does not preserve the required same-category sales order.

- [x] **Step 3: Implement same-category-only fallback**

Sort available same-category products by `salesCount` descending and product ID ascending, then merge them after behavioral results. Remove same-brand, global hot, and global new fallback lists from this placement.

- [x] **Step 4: Run the service test and verify GREEN**

Run: `mvn -q -Dtest=RecommendationServiceTest test`

Expected: all service tests pass.

### Task 3: Regression Verification

**Files:**
- Verify all modified backend files

- [x] **Step 1: Run focused recommendation tests**

Run: `mvn -q -Dtest=ItemBasedCollaborativeFilteringEngineTest,RecommendationServiceTest test`

Expected: all focused tests pass.

- [x] **Step 2: Run the full backend test suite**

Run: `mvn -q test`

Expected: the build exits successfully with no test failures.

Actual: the suite retains two pre-existing `DataInitializerTest` errors. All tests pass when that unrelated failing class is excluded.

- [x] **Step 3: Check the final diff**

Run: `git diff --check` and `git diff -- backend/src/main backend/src/test docs/superpowers`

Expected: no whitespace errors and only the planned recommendation changes.
