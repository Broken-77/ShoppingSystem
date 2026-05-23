# Ecommerce Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Spring Boot + Vue + MySQL ecommerce platform with storefront, admin console, simplified login, simulated payment, and Item-Based collaborative filtering recommendations.

**Architecture:** Use a single repository with `backend/` for Spring Boot REST APIs and `frontend/` for Vue. The backend owns data, business rules, simplified token auth, orders, behavior tracking, and recommendation scoring. The frontend consumes REST APIs and provides separate storefront and admin route groups.

**Tech Stack:** Java 17, Spring Boot 4.0.6, Spring Data JPA, MySQL Connector/J, JUnit, MockMvc, Vue 3, Vite, Pinia, Vue Router, Axios.

---

## File Structure

Create or move files into this structure:

```text
backend/
  pom.xml
  src/main/java/com/wms/shoppingsys/
    ShoppingSystemApplication.java
    common/
      ApiResponse.java
      GlobalExceptionHandler.java
      BusinessException.java
      ErrorCode.java
    auth/
      AuthController.java
      AuthService.java
      TokenStore.java
      LoginRequest.java
      RegisterRequest.java
      LoginResponse.java
      CurrentUser.java
      AuthInterceptor.java
      AuthConfig.java
    user/
      User.java
      UserRepository.java
      UserRole.java
      UserStatus.java
    catalog/
      Category.java
      CategoryRepository.java
      Product.java
      ProductRepository.java
      ProductStatus.java
      ProductController.java
      CategoryController.java
      ProductService.java
      ProductDtos.java
    cart/
      CartItem.java
      CartRepository.java
      CartController.java
      CartService.java
      CartDtos.java
    order/
      Order.java
      OrderItem.java
      OrderRepository.java
      OrderItemRepository.java
      OrderStatus.java
      OrderController.java
      OrderService.java
      OrderDtos.java
    recommendation/
      UserBehavior.java
      UserBehaviorRepository.java
      BehaviorType.java
      BehaviorService.java
      RecommendationEngine.java
      ItemBasedCollaborativeFilteringEngine.java
      FallbackRecommendationService.java
      RecommendationService.java
      RecommendationController.java
    admin/
      AdminProductController.java
      AdminCategoryController.java
      AdminOrderController.java
      AdminRecommendationController.java
    config/
      DataInitializer.java
  src/main/resources/application.properties
  src/test/java/com/wms/shoppingsys/
    auth/AuthControllerTest.java
    catalog/ProductControllerTest.java
    cart/CartControllerTest.java
    order/OrderControllerTest.java
    recommendation/ItemBasedCollaborativeFilteringEngineTest.java
    admin/AdminAuthorizationTest.java
frontend/
  package.json
  index.html
  vite.config.js
  src/
    main.js
    App.vue
    api/client.js
    api/auth.js
    api/products.js
    api/cart.js
    api/orders.js
    api/admin.js
    router/index.js
    stores/auth.js
    stores/cart.js
    components/ProductCard.vue
    components/AppNav.vue
    components/AdminLayout.vue
    views/shop/HomeView.vue
    views/shop/ProductListView.vue
    views/shop/ProductDetailView.vue
    views/shop/CartView.vue
    views/shop/CheckoutView.vue
    views/shop/OrdersView.vue
    views/admin/AdminProductsView.vue
    views/admin/AdminCategoriesView.vue
    views/admin/AdminOrdersView.vue
```

## Task 1: Move Spring Boot App Into `backend/`

**Files:**
- Move: `pom.xml` to `backend/pom.xml`
- Move: `src/main/**` to `backend/src/main/**`
- Move: `src/test/**` to `backend/src/test/**`
- Modify: `.gitignore`

- [ ] **Step 1: Create backend directory and move project files**

Move the Maven project into `backend/` while preserving package names:

```bash
mkdir -p backend
mv pom.xml backend/pom.xml
mv src backend/src
mv mvnw backend/mvnw
mv mvnw.cmd backend/mvnw.cmd
```

- [ ] **Step 2: Keep generated and local-only files ignored**

Ensure `.gitignore` contains these entries:

```gitignore
target/
.superpowers/
backend/target/
frontend/node_modules/
frontend/dist/
```

- [ ] **Step 3: Verify the backend skeleton still starts**

Run:

```bash
cd backend
./mvnw test
```

Expected: build succeeds and `ShoppingSystemApplicationTests.contextLoads` passes.

- [ ] **Step 4: Commit if git is initialized**

Run:

```bash
git rev-parse --is-inside-work-tree
```

Expected in the current workspace: `fatal: not a git repository`. If git has been initialized before execution, commit:

```bash
git add .gitignore backend
git commit -m "chore: move Spring Boot app into backend"
```

## Task 2: Backend Dependencies, Config, And Shared API Layer

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.properties`
- Create: `backend/src/main/java/com/wms/shoppingsys/common/ApiResponse.java`
- Create: `backend/src/main/java/com/wms/shoppingsys/common/BusinessException.java`
- Create: `backend/src/main/java/com/wms/shoppingsys/common/ErrorCode.java`
- Create: `backend/src/main/java/com/wms/shoppingsys/common/GlobalExceptionHandler.java`

- [ ] **Step 1: Add backend dependencies**

Add dependencies to `backend/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Configure MySQL and JPA**

Set `backend/src/main/resources/application.properties`:

```properties
spring.application.name=ShoppingSystem
spring.datasource.url=jdbc:mysql://localhost:3306/shopping_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=true
server.port=8080
```

- [ ] **Step 3: Add unified response wrapper**

Create `ApiResponse.java`:

```java
package com.wms.shoppingsys.common;

public record ApiResponse<T>(boolean success, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

- [ ] **Step 4: Add business errors**

Create `ErrorCode.java`:

```java
package com.wms.shoppingsys.common;

public enum ErrorCode {
    BAD_REQUEST,
    UNAUTHENTICATED,
    FORBIDDEN,
    NOT_FOUND,
    STOCK_NOT_ENOUGH,
    INVALID_ORDER_STATE
}
```

Create `BusinessException.java`:

```java
package com.wms.shoppingsys.common;

public class BusinessException extends RuntimeException {
    private final ErrorCode code;

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
```

- [ ] **Step 5: Add global exception handling**

Create `GlobalExceptionHandler.java`:

```java
package com.wms.shoppingsys.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case STOCK_NOT_ENOUGH, INVALID_ORDER_STATE, BAD_REQUEST -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("参数错误");
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }
}
```

- [ ] **Step 6: Verify compilation**

Run:

```bash
cd backend
./mvnw test
```

Expected: tests compile. If Maven cannot download dependencies because network is blocked, rerun with approval for Maven dependency access.

## Task 3: Users, Simplified Token Auth, And Role Guards

**Files:**
- Create: `backend/src/main/java/com/wms/shoppingsys/user/User.java`
- Create: `backend/src/main/java/com/wms/shoppingsys/user/UserRepository.java`
- Create: `backend/src/main/java/com/wms/shoppingsys/user/UserRole.java`
- Create: `backend/src/main/java/com/wms/shoppingsys/user/UserStatus.java`
- Create: `backend/src/main/java/com/wms/shoppingsys/auth/*`
- Test: `backend/src/test/java/com/wms/shoppingsys/auth/AuthControllerTest.java`

- [ ] **Step 1: Write auth tests**

Create `AuthControllerTest.java` with tests for registration, login, and admin guard:

```java
package com.wms.shoppingsys.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void registersAndLogsInUser() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("USER"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd backend
./mvnw -Dtest=AuthControllerTest test
```

Expected: fail because `/api/auth/register` and `/api/auth/login` are not implemented.

- [ ] **Step 3: Implement user entity and enums**

Create `UserRole.java`:

```java
package com.wms.shoppingsys.user;

public enum UserRole {
    USER,
    ADMIN
}
```

Create `UserStatus.java`:

```java
package com.wms.shoppingsys.user;

public enum UserStatus {
    ACTIVE,
    DISABLED
}
```

Create `User.java` with fields matching the design: id, username, passwordHash, role, status, createdAt.

- [ ] **Step 4: Implement token login**

Implement:

```text
LoginRequest(username, password)
RegisterRequest(username, password)
LoginResponse(token, username, role)
CurrentUser(id, username, role)
TokenStore: in-memory ConcurrentHashMap token -> CurrentUser
AuthService: register, login, requireUser, requireAdmin
AuthController: /api/auth/register and /api/auth/login
```

Use `UUID.randomUUID().toString()` for token values. Store password hashes with `SHA-256` in the first version.

- [ ] **Step 5: Implement request auth interceptor**

Implement `AuthInterceptor` to read the `Authorization` header. Support both values:

```text
Bearer <token>
<token>
```

Store the resolved `CurrentUser` in request attributes under `currentUser`.

- [ ] **Step 6: Run auth tests**

Run:

```bash
cd backend
./mvnw -Dtest=AuthControllerTest test
```

Expected: pass.

## Task 4: Catalog And Admin Product Management

**Files:**
- Create: `backend/src/main/java/com/wms/shoppingsys/catalog/*`
- Create: `backend/src/main/java/com/wms/shoppingsys/admin/AdminProductController.java`
- Create: `backend/src/main/java/com/wms/shoppingsys/admin/AdminCategoryController.java`
- Test: `backend/src/test/java/com/wms/shoppingsys/catalog/ProductControllerTest.java`
- Test: `backend/src/test/java/com/wms/shoppingsys/admin/AdminAuthorizationTest.java`

- [ ] **Step 1: Write product listing tests**

Create tests that seed categories/products and verify:

```text
GET /api/products returns only ON_SALE products
GET /api/products?keyword=phone filters by name
GET /api/products?categoryId=1 filters by category
GET /api/products/{id} records a VIEW behavior for logged-in users
```

- [ ] **Step 2: Implement catalog entities**

Create:

```text
Category(id, name, parentId, enabled, sortOrder)
Product(id, categoryId, name, description, brand, price, stock, imageUrl, status, salesCount, createdAt, updatedAt)
ProductStatus(ON_SALE, OFF_SALE)
```

Use `BigDecimal` for price fields and `Integer` for stock and sales count.

- [ ] **Step 3: Implement catalog repositories**

Create repository methods:

```java
List<Product> findByStatus(ProductStatus status);
List<Product> findByStatusAndCategoryId(ProductStatus status, Long categoryId);
List<Product> findByStatusAndNameContainingIgnoreCase(ProductStatus status, String keyword);
List<Product> findTop12ByStatusOrderBySalesCountDesc(ProductStatus status);
List<Product> findTop12ByStatusOrderByCreatedAtDesc(ProductStatus status);
```

- [ ] **Step 4: Implement public catalog APIs**

Implement:

```text
GET /api/products
GET /api/products/{id}
GET /api/products/{id}/similar
GET /api/categories
```

`GET /api/products/{id}` should call `BehaviorService.recordView` when a valid user token is present.

- [ ] **Step 5: Implement admin catalog APIs**

Implement:

```text
GET /api/admin/products
POST /api/admin/products
PUT /api/admin/products/{id}
POST /api/admin/products/{id}/on-sale
POST /api/admin/products/{id}/off-sale
GET /api/admin/categories
POST /api/admin/categories
PUT /api/admin/categories/{id}
POST /api/admin/categories/{id}/disable
```

All admin endpoints call `AuthService.requireAdmin(request)`.

- [ ] **Step 6: Run catalog and admin tests**

Run:

```bash
cd backend
./mvnw -Dtest=ProductControllerTest,AdminAuthorizationTest test
```

Expected: product queries and admin authorization pass.

## Task 5: Cart, Orders, And Simulated Payment

**Files:**
- Create: `backend/src/main/java/com/wms/shoppingsys/cart/*`
- Create: `backend/src/main/java/com/wms/shoppingsys/order/*`
- Create: `backend/src/main/java/com/wms/shoppingsys/admin/AdminOrderController.java`
- Test: `backend/src/test/java/com/wms/shoppingsys/cart/CartControllerTest.java`
- Test: `backend/src/test/java/com/wms/shoppingsys/order/OrderControllerTest.java`

- [ ] **Step 1: Write cart tests**

Cover:

```text
Adding an ON_SALE product creates a cart item
Adding the same product increments quantity
Adding quantity beyond stock returns STOCK_NOT_ENOUGH
Deleting a cart item removes it from the user's cart
```

- [ ] **Step 2: Write order tests**

Cover:

```text
POST /api/orders creates PENDING_PAYMENT order from cart
Order creation deducts product stock
POST /api/orders/{id}/pay changes status to PAID and sets paidAt
Paying the same order twice returns INVALID_ORDER_STATE
GET /api/orders returns only the current user's orders
```

- [ ] **Step 3: Implement cart model and APIs**

Implement:

```text
CartItem(id, userId, productId, quantity, createdAt, updatedAt)
GET /api/cart
POST /api/cart/items
PUT /api/cart/items/{id}
DELETE /api/cart/items/{id}
```

`POST /api/cart/items` records a `CART` behavior after the item is added.

- [ ] **Step 4: Implement order model and APIs**

Implement:

```text
Order(id, orderNo, userId, totalAmount, status, createdAt, paidAt)
OrderItem(id, orderId, productId, productName, productPrice, quantity, subtotal)
OrderStatus(PENDING_PAYMENT, PAID, CANCELLED, FINISHED)
```

Generate order numbers with:

```java
"SO" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + randomFourDigits
```

- [ ] **Step 5: Implement checkout transaction**

`OrderService.createOrder` must run in one transaction:

```text
Load current user's cart
Reject empty cart
Reload each product
Reject OFF_SALE or insufficient stock
Deduct stock
Create order and order items with product snapshots
Clear cart
Record ORDER behavior for each product
```

- [ ] **Step 6: Implement simulated payment**

`POST /api/orders/{id}/pay`:

```text
Requires current user
Requires order belongs to current user
Requires status PENDING_PAYMENT
Sets status PAID
Sets paidAt to now
Returns updated order
```

- [ ] **Step 7: Implement admin order APIs**

Implement:

```text
GET /api/admin/orders
GET /api/admin/orders/{id}
PUT /api/admin/orders/{id}/status
```

Allowed admin status changes:

```text
PAID -> FINISHED
PENDING_PAYMENT -> CANCELLED
```

- [ ] **Step 8: Run cart and order tests**

Run:

```bash
cd backend
./mvnw -Dtest=CartControllerTest,OrderControllerTest test
```

Expected: pass.

## Task 6: Item-Based Collaborative Filtering Recommendations

**Files:**
- Create: `backend/src/main/java/com/wms/shoppingsys/recommendation/*`
- Create: `backend/src/main/java/com/wms/shoppingsys/admin/AdminRecommendationController.java`
- Test: `backend/src/test/java/com/wms/shoppingsys/recommendation/ItemBasedCollaborativeFilteringEngineTest.java`

- [ ] **Step 1: Write recommendation engine unit test**

Use this scenario:

```text
User 1: product 1 ORDER weight 8, product 2 CART weight 4
User 2: product 1 ORDER weight 8, product 2 VIEW weight 1
User 3: product 1 VIEW weight 1, product 3 ORDER weight 8
Target user: product 1 VIEW weight 1
Expected: product 2 ranks before product 3 because product 2 is more similar to product 1
```

- [ ] **Step 2: Implement behavior model**

Create:

```text
BehaviorType(VIEW, CART, ORDER)
UserBehavior(id, userId, productId, behaviorType, weight, createdAt)
UserBehaviorRepository
BehaviorService.recordView
BehaviorService.recordCart
BehaviorService.recordOrder
```

- [ ] **Step 3: Implement `RecommendationEngine`**

Interface:

```java
package com.wms.shoppingsys.recommendation;

import java.util.List;

public interface RecommendationEngine {
    List<Long> recommendProductIds(Long userId, int limit);
    List<Long> similarProductIds(Long productId, int limit);
}
```

- [ ] **Step 4: Implement cosine similarity**

In `ItemBasedCollaborativeFilteringEngine`:

```text
Group behaviors by productId, then userId
Sum behavior weights per user-product pair
For two product vectors, compute dot product and vector norms
Return 0 when either norm is 0
```

Recommendation scoring:

```text
Load target user's product scores
For every candidate product not already purchased by the user:
  score = sum(userProductScore * similarity(userProduct, candidate))
Sort descending by score
Return product ids up to limit
```

- [ ] **Step 5: Implement fallback recommendation service**

Fallback order:

```text
Hot products by salesCount desc
New products by createdAt desc
Same-category products for detail pages
Same-brand products for detail pages
```

Always filter:

```text
status = ON_SALE
stock > 0
```

- [ ] **Step 6: Implement recommendation REST APIs**

Implement:

```text
GET /api/recommendations/home
GET /api/recommendations/cart
GET /api/products/{id}/similar
GET /api/admin/recommendations/users/{userId}
```

Home recommendation requires login. Product detail similar products can work anonymously by using item similarity plus fallback.

- [ ] **Step 7: Run recommendation tests**

Run:

```bash
cd backend
./mvnw -Dtest=ItemBasedCollaborativeFilteringEngineTest test
```

Expected: collaborative filtering ranking test passes.

## Task 7: Seed Data For Local Demo

**Files:**
- Create: `backend/src/main/java/com/wms/shoppingsys/config/DataInitializer.java`

- [ ] **Step 1: Seed users**

Create:

```text
admin / admin123 / ADMIN
alice / pass123 / USER
bob / pass123 / USER
```

- [ ] **Step 2: Seed categories**

Create:

```text
手机数码
电脑办公
家居生活
运动户外
```

- [ ] **Step 3: Seed products**

Create at least 16 ON_SALE products across the categories with realistic price, stock, brand, imageUrl, salesCount, createdAt values.

Use stable development image URLs:

```text
https://picsum.photos/seed/<product-slug>/640/480
```

- [ ] **Step 4: Seed behavior data**

Seed enough behavior rows to make collaborative filtering visible:

```text
alice interacts with phone, phone case, charger
bob interacts with phone, phone case, headphones
another user interacts with laptop, keyboard, mouse
```

- [ ] **Step 5: Verify local demo data**

Run backend and open:

```text
GET http://localhost:8080/api/products
GET http://localhost:8080/api/recommendations/home with alice token
```

Expected: product list has seeded products and home recommendations return non-empty products.

## Task 8: Vue Scaffold, API Client, Auth Store, And Router

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/index.html`
- Create: `frontend/vite.config.js`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/api/*.js`
- Create: `frontend/src/router/index.js`
- Create: `frontend/src/stores/auth.js`
- Create: `frontend/src/stores/cart.js`

- [ ] **Step 1: Create Vue package files**

Use this `package.json`:

```json
{
  "scripts": {
    "dev": "vite --host 127.0.0.1",
    "build": "vite build",
    "preview": "vite preview --host 127.0.0.1"
  },
  "dependencies": {
    "@vitejs/plugin-vue": "^6.0.0",
    "axios": "^1.7.9",
    "pinia": "^3.0.0",
    "vite": "^7.0.0",
    "vue": "^3.5.0",
    "vue-router": "^4.5.0"
  },
  "devDependencies": {}
}
```

- [ ] **Step 2: Implement Axios client**

`frontend/src/api/client.js`:

```js
import axios from 'axios'

export const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
```

- [ ] **Step 3: Implement auth store**

`auth.js` stores:

```text
token
username
role
isLoggedIn
isAdmin
login(username, password)
register(username, password)
logout()
```

Persist token, username, and role in localStorage.

- [ ] **Step 4: Implement routes and guards**

Routes:

```text
/
/products
/products/:id
/cart
/checkout
/orders
/admin/products
/admin/categories
/admin/orders
```

Guards:

```text
cart, checkout, orders require login
admin routes require role ADMIN
```

- [ ] **Step 5: Verify frontend dependencies**

Run:

```bash
cd frontend
npm install
npm run build
```

Expected: dependencies install and build succeeds. If network is blocked, rerun `npm install` with approval for registry access.

## Task 9: Storefront Pages

**Files:**
- Create: `frontend/src/components/AppNav.vue`
- Create: `frontend/src/components/ProductCard.vue`
- Create: `frontend/src/views/shop/*.vue`
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: Implement global app shell**

`App.vue` renders `AppNav` and `<router-view />`. The nav includes:

```text
Logo
首页
商品
购物车
我的订单
后台入口 for ADMIN
登录/退出
```

- [ ] **Step 2: Implement home page**

`HomeView.vue` loads:

```text
/api/recommendations/home when logged in
/api/products as hot/new fallback when anonymous
```

Show sections:

```text
猜你喜欢
热门商品
新品上架
```

- [ ] **Step 3: Implement product list**

`ProductListView.vue` supports:

```text
keyword input
category select
minPrice and maxPrice inputs
product grid
```

Submit filters to `GET /api/products`.

- [ ] **Step 4: Implement product detail**

`ProductDetailView.vue` loads:

```text
GET /api/products/:id
GET /api/products/:id/similar
```

Add-to-cart button calls `POST /api/cart/items`.

- [ ] **Step 5: Implement cart and checkout**

`CartView.vue` supports quantity update, delete, and checkout navigation.

`CheckoutView.vue`:

```text
Calls POST /api/orders
Shows created order summary
Calls POST /api/orders/{id}/pay
Shows paid success state
```

- [ ] **Step 6: Implement orders page**

`OrdersView.vue` calls:

```text
GET /api/orders
GET /api/orders/{id}
```

Show order number, amount, status, created time, paid time, and items.

- [ ] **Step 7: Build frontend**

Run:

```bash
cd frontend
npm run build
```

Expected: build succeeds.

## Task 10: Admin Pages

**Files:**
- Create: `frontend/src/components/AdminLayout.vue`
- Create: `frontend/src/views/admin/AdminProductsView.vue`
- Create: `frontend/src/views/admin/AdminCategoriesView.vue`
- Create: `frontend/src/views/admin/AdminOrdersView.vue`
- Modify: `frontend/src/api/admin.js`

- [ ] **Step 1: Implement admin layout**

Admin layout includes side navigation:

```text
商品管理
分类管理
订单管理
返回商城
```

- [ ] **Step 2: Implement product management**

Product page supports:

```text
table list
create form
edit form
stock update
on-sale button
off-sale button
```

Fields:

```text
name
categoryId
description
brand
price
stock
imageUrl
status
```

- [ ] **Step 3: Implement category management**

Category page supports:

```text
table list
create form
edit form
disable button
sortOrder input
```

- [ ] **Step 4: Implement order management**

Order page supports:

```text
order table
order detail expansion
status update from PAID to FINISHED
status update from PENDING_PAYMENT to CANCELLED
```

- [ ] **Step 5: Build frontend**

Run:

```bash
cd frontend
npm run build
```

Expected: build succeeds and no route import errors occur.

## Task 11: End-To-End Local Verification

**Files:**
- Modify only files needed to fix verification failures.

- [ ] **Step 1: Run backend tests**

Run:

```bash
cd backend
./mvnw test
```

Expected: all backend tests pass.

- [ ] **Step 2: Run frontend build**

Run:

```bash
cd frontend
npm run build
```

Expected: build succeeds.

- [ ] **Step 3: Start backend**

Run:

```bash
cd backend
./mvnw spring-boot:run
```

Expected: backend listens on `http://localhost:8080`.

- [ ] **Step 4: Start frontend**

Run:

```bash
cd frontend
npm run dev
```

Expected: frontend listens on the Vite URL shown in the terminal, usually `http://127.0.0.1:5173`.

- [ ] **Step 5: Verify customer flow in browser**

Manual flow:

```text
Log in as alice / pass123
Open home page and confirm recommendations render
Open a product detail page
Add product to cart
Change cart quantity
Create order
Confirm simulated payment
Open my orders and confirm status PAID
```

- [ ] **Step 6: Verify admin flow in browser**

Manual flow:

```text
Log out
Log in as admin / admin123
Open /admin/products
Create or edit a product
Open /admin/categories
Create or edit a category
Open /admin/orders
Change a PAID order to FINISHED
```

- [ ] **Step 7: Final check**

Run:

```bash
cd backend
./mvnw test
cd ../frontend
npm run build
```

Expected: both commands pass.

## Self-Review Notes

- Spec coverage: storefront, admin, simplified login, MySQL, simulated payment, behavior tracking, Item-Based collaborative filtering, fallback recommendations, API shape, error handling, and testing are covered by tasks.
- Placeholder scan: this plan has no deferred implementation markers. Each task has concrete files, behaviors, commands, and expected outcomes.
- Type consistency: recommendation interfaces use `Long` product and user ids; backend roles use `USER` and `ADMIN`; order states match the design; behavior weights match the design.
