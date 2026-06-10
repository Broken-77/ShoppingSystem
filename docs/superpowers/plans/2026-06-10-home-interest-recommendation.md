# Home Interest Recommendation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow unpurchased viewed products to appear in home recommendations with logarithmic repeat-view weighting, while excluding purchased products and using them to recommend related same-category products.

**Architecture:** Aggregate behavior rows by user, product, and behavior type into damped interest scores. Rank every available product by direct unfinished interest plus confidence-adjusted same-category similarity contributions, then apply purchased-product exclusion again while merging service-level fallback lists.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA, JUnit 5, AssertJ, Maven

---

### Task 1: Specify Personalized Candidate Behavior

**Files:**
- Modify: `backend/src/test/java/com/wms/shoppingsys/service/ItemBasedCollaborativeFilteringEngineTest.java`

- [ ] **Step 1: Add failing tests**

Add tests that save real products and behavior rows, then assert:

```java
assertThat(recommendationEngine.recommendProductIds(userId, 10))
        .contains(viewedProduct.getId());

assertThat(recommendations.indexOf(repeatedlyViewed.getId()))
        .isLessThan(recommendations.indexOf(viewedOnce.getId()));

assertThat(ItemBasedCollaborativeFilteringEngine.dampedBehaviorScore(1, 4, 1.0))
        .isCloseTo(1 + Math.log(4), within(0.000001));
```

Add a purchased-source test where the purchased product is absent, a co-interacted same-category candidate is present, and an equally co-interacted cross-category candidate is absent.

Add a ranking-tier test where a newly viewed product ranks before a candidate with a stronger related-only score.

Add a recency test where a newly viewed product ranks before an older product with more accumulated views.

- [ ] **Step 2: Verify the tests fail**

Run:

```bash
cd backend
./mvnw -Dtest=ItemBasedCollaborativeFilteringEngineTest test
```

Expected: failures because viewed products are currently excluded, the damping helper does not exist, and home related scoring does not enforce same-category relationships.

### Task 2: Implement Aggregated Interest Scoring

**Files:**
- Modify: `backend/src/main/java/com/wms/shoppingsys/service/ItemBasedCollaborativeFilteringEngine.java`

- [ ] **Step 1: Aggregate repeated behavior**

Introduce a behavior aggregate keyed by user, product, and behavior type. Its score is:

```java
static double dampedBehaviorScore(int baseWeight, long count, double timeDecay) {
    return baseWeight * (1.0 + Math.log(count)) * timeDecay;
}
```

Use the newest event in each aggregate to determine time decay, and sum type scores into each user-product preference score and product vector.

- [ ] **Step 2: Rank available products**

Load all products once, build the target user's purchased set from `BehaviorType.ORDER`, and evaluate every on-sale product with positive stock that is not purchased:

```java
double score = directUnfinishedInterest(targetAggregates, candidateId);
score += sameCategoryRelatedScore(candidate, productsById, productVectors, targetScores);
```

Direct interest includes `VIEW` and `CART`. Related score skips the candidate itself, requires matching category IDs, and uses the existing confidence-adjusted cosine similarity. Sort candidates with positive direct interest before related-only candidates. Within the direct-interest tier sort by latest direct behavior time, then composite score and product ID; sort related-only candidates by composite score and product ID.

- [ ] **Step 3: Verify engine tests pass**

Run:

```bash
cd backend
./mvnw -Dtest=ItemBasedCollaborativeFilteringEngineTest test
```

Expected: all engine tests pass.

### Task 3: Exclude Purchased Products From Fallback

**Files:**
- Modify: `backend/src/test/java/com/wms/shoppingsys/service/RecommendationServiceTest.java`
- Modify: `backend/src/main/java/com/wms/shoppingsys/service/RecommendationService.java`

- [ ] **Step 1: Add a failing service test**

Create a purchased product that would otherwise rank first in category-hot and new-product fallback, call `homeRecommendations`, and assert:

```java
assertThat(recommendations)
        .extracting(Product::getId)
        .doesNotContain(purchased.getId());
```

- [ ] **Step 2: Verify the service test fails**

Run:

```bash
cd backend
./mvnw -Dtest=RecommendationServiceTest test
```

Expected: the purchased product is reintroduced by fallback and the test fails.

- [ ] **Step 3: Apply one exclusion set to every home source**

Build purchased IDs from the target user's `ORDER` rows and pass them through category-hot collection and final merging:

```java
Set<Long> purchasedIds = behaviors.stream()
        .filter(b -> b.getBehaviorType() == BehaviorType.ORDER)
        .map(UserBehavior::getProductId)
        .collect(Collectors.toSet());
```

Filter collaborative, category-hot, and new-product candidates against this set while preserving the existing order, duplicate removal, availability checks, and 12-product limit. Keep product-detail `similarProducts` unchanged.

- [ ] **Step 4: Verify service tests pass**

Run:

```bash
cd backend
./mvnw -Dtest=RecommendationServiceTest test
```

Expected: all service tests pass.

### Task 4: Regression Verification

**Files:**
- Verify all modified production and test files.

- [ ] **Step 1: Run the complete backend test suite**

```bash
cd backend
./mvnw test
```

Expected: `BUILD SUCCESS` with no test failures.

- [ ] **Step 2: Check the final diff**

```bash
git diff --check
git status --short
```

Expected: no whitespace errors; only the design, plan, recommendation implementation, and associated tests are changed.
