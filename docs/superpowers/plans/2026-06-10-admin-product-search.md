# Admin Product Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add instant admin product search across product name, brand, and category name while keeping pagination accurate.

**Architecture:** A small pure frontend utility normalizes and matches searchable fields. `AdminProductsView.vue` derives filtered products from the loaded list, then passes that list through the existing pagination helpers and renders a search toolbar plus empty state.

**Tech Stack:** Vue 3, Vite, native Node.js assertions, existing CSS

---

### Task 1: Product Search Utility

**Files:**
- Create: `frontend/src/utils/productSearch.mjs`
- Create: `frontend/tests/product-search.test.mjs`

- [x] **Step 1: Write failing utility tests**

Test matching by product name, brand, and category name; case-insensitive and trimmed queries; empty queries returning all products; and unmatched queries returning an empty list.

- [x] **Step 2: Run the test and verify RED**

Run: `node tests/product-search.test.mjs`

Expected: fail because `productSearch.mjs` does not exist.

- [x] **Step 3: Implement the pure search utility**

Export `filterProducts(products, query, categoryNameForId)` and normalize values with `String(value ?? '').trim().toLocaleLowerCase()`.

- [x] **Step 4: Run the test and verify GREEN**

Run: `node tests/product-search.test.mjs`

Expected: `product search tests passed`.

### Task 2: Admin Product Search UI

**Files:**
- Modify: `frontend/src/views/admin/AdminProductsView.vue`
- Modify: `frontend/src/styles.css`

- [x] **Step 1: Add filtered pagination state**

Add `searchQuery`, derive `filteredProducts`, and calculate pages, ranges, and displayed rows from the filtered list. Reset to page 1 whenever the query changes.

- [x] **Step 2: Add search toolbar and empty state**

Render a labeled search input above the table, display the filtered result count, hide the table when no results match, and show `未找到匹配商品`.

- [x] **Step 3: Add responsive toolbar styling**

Use the existing border, radius, background, and input styles. Keep the label/input on the left and result count on the right, collapsing naturally on narrow screens.

### Task 3: Verification

**Files:**
- Verify frontend search, pagination, and build output

- [x] **Step 1: Run utility and pagination tests**

Run: `node tests/product-search.test.mjs && node tests/pagination.test.mjs`

Expected: both test scripts pass.

- [x] **Step 2: Run production build**

Run: `npm run build`

Expected: Vite build exits successfully.

- [x] **Step 3: Verify in browser**

Open the admin product page, confirm name/brand/category searches update rows and counts, confirm unmatched search shows the empty state, and confirm clearing the query restores pagination.

- [x] **Step 4: Check final diff**

Run: `git diff --check` and inspect the modified frontend files.

Expected: no whitespace errors and no unrelated edits.
