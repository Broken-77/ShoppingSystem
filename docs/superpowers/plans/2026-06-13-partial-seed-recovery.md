# Partial Seed Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Recover missing demo categories and products without overwriting existing data or assuming fixed category IDs.

**Architecture:** `DataInitializer` performs idempotent name-based upserts for seed categories and products. Existing records remain authoritative, while behavior generation groups products using the actual recovered category IDs.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA, JUnit 5, AssertJ, Maven

---

### Task 1: Category Recovery

**Files:**
- Modify: `backend/src/main/java/com/wms/shoppingsys/config/DataInitializer.java`
- Test: `backend/src/test/java/com/wms/shoppingsys/config/DataInitializerTest.java`

- [ ] **Step 1: Run the existing failing category test**

Run: `mvn -Dtest=DataInitializerTest#fillsMissingSeedCategoriesWhenCategoryTableAlreadyHasRows test`

Expected: ERROR with `NoSuchElementException` while looking up a missing built-in category.

- [ ] **Step 2: Implement name-based category recovery**

Load existing categories into a name map, then call a helper for each built-in category:

```java
private Category ensureCategory(Map<String, Category> categoriesByName, String name, int sortOrder) {
    return categoriesByName.computeIfAbsent(name,
            ignored -> categoryRepository.save(new Category(name, null, true, sortOrder)));
}
```

Return the six recovered category instances under the existing logical keys.

- [ ] **Step 3: Run the category test and verify GREEN**

Run: `mvn -Dtest=DataInitializerTest#fillsMissingSeedCategoriesWhenCategoryTableAlreadyHasRows test`

Expected: PASS.

### Task 2: Product And Behavior Recovery

**Files:**
- Modify: `backend/src/main/java/com/wms/shoppingsys/config/DataInitializer.java`
- Modify: `backend/src/test/java/com/wms/shoppingsys/config/DataInitializerTest.java`

- [ ] **Step 1: Strengthen the partial-product regression test**

Insert an unrelated category before the six seed categories so their generated IDs are not `1` through `6`. Preserve the existing product's ID, price, stock, and brand assertions after initialization.

- [ ] **Step 2: Run the product test and verify RED**

Run: `mvn -Dtest=DataInitializerTest#fillsMissingSeedProductsWhenProductTableAlreadyHasRows test`

Expected: ERROR because missing seed products are not inserted and behavior grouping uses fixed category IDs.

- [ ] **Step 3: Make seed product insertion idempotent**

Load existing product names into a mutable set and route every seed product through:

```java
private void saveSeedProduct(Set<String> existingProductNames, Product product) {
    if (existingProductNames.add(product.getName())) {
        productRepository.save(product);
    }
}
```

Always evaluate the seed product list so missing products are inserted even when the table is non-empty.

- [ ] **Step 4: Use actual category IDs for behavior groups**

Pass the recovered category map into `seedBehaviors` and filter products with `Objects.equals(product.getCategoryId(), categories.get(key).getId())`.

- [ ] **Step 5: Run the product test and verify GREEN**

Run: `mvn -Dtest=DataInitializerTest#fillsMissingSeedProductsWhenProductTableAlreadyHasRows test`

Expected: PASS with the existing product unchanged and missing seed products present.

### Task 3: Verification

**Files:**
- Verify backend tests and final source diff

- [ ] **Step 1: Run all initializer tests**

Run: `mvn -Dtest=DataInitializerTest test`

Expected: all initializer tests pass.

- [ ] **Step 2: Run the full backend suite**

Run: `mvn test`

Expected: BUILD SUCCESS with zero failures and errors.

- [ ] **Step 3: Check final diff**

Run: `git diff --check` and inspect modified files.

Expected: no whitespace errors and no unrelated changes.
