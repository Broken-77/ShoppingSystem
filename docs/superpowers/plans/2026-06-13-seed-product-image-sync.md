# Seed Product Image Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Synchronize configured seed image URLs into existing products without changing any other stored data.

**Architecture:** `DataInitializer` remains the source of seed image mappings and performs a narrow synchronization after loading products. `Product` exposes a dedicated image-only mutation so callers cannot accidentally overwrite business fields.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA, JUnit 5, AssertJ, Maven

---

### Task 1: Image-Only Synchronization

**Files:**
- Modify: `backend/src/test/java/com/wms/shoppingsys/config/DataInitializerTest.java`
- Modify: `backend/src/main/java/com/wms/shoppingsys/entity/Product.java`
- Modify: `backend/src/main/java/com/wms/shoppingsys/repository/ProductRepository.java`
- Modify: `backend/src/main/java/com/wms/shoppingsys/config/DataInitializer.java`

- [ ] **Step 1: Write the failing integration test**

Create an existing `iPhone 15 Pro Max` with a stale image URL and custom category, description, brand, price, stock, status, and sales count. Run `DataInitializer`, reload the same product, and assert the image URL matches `IMG` while all custom fields and timestamps remain unchanged.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `./mvnw -Dtest=DataInitializerTest#updatesOnlyImageForExistingSeedProduct test`

Expected: FAIL because the stale image URL remains unchanged.

- [ ] **Step 3: Add the image-only entity mutation**

Add this method to `Product`:

```java
public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
}
```

- [ ] **Step 4: Add an image-column-only repository update**

Add a modifying query to `ProductRepository` so JPA entity lifecycle callbacks do not change `updatedAt`:

```java
@Modifying
@Transactional
@Query("update Product product set product.imageUrl = :imageUrl where product.id = :id")
int updateImageUrlById(@Param("id") Long id, @Param("imageUrl") String imageUrl);
```

- [ ] **Step 5: Synchronize configured images**

After loading products in `seedProducts`, update only products with a non-blank configured image different from their current image:

```java
for (Product product : products) {
    String configuredImage = IMG.get(product.getName());
    if (configuredImage != null && !configuredImage.isBlank()
            && !configuredImage.equals(product.getImageUrl())) {
        productRepository.updateImageUrlById(product.getId(), configuredImage);
        product.updateImageUrl(configuredImage);
    }
}
```

- [ ] **Step 6: Run the focused test and verify GREEN**

Run: `./mvnw -Dtest=DataInitializerTest#updatesOnlyImageForExistingSeedProduct test`

Expected: PASS.

### Task 2: Regression Verification

**Files:**
- Verify backend test suite and source formatting

- [ ] **Step 1: Run all backend tests**

Run: `./mvnw test`

Expected: BUILD SUCCESS with zero test failures.

- [ ] **Step 2: Check the final diff**

Run: `git diff --check` and inspect the modified files.

Expected: no whitespace errors and no unrelated changes.
