# Ecommerce Platform Design

Date: 2026-05-19

## Goal

Build a balanced ecommerce platform with a Vue storefront, a Vue admin console, a Spring Boot REST backend, MySQL persistence, simplified login, simulated payment, and Item-Based collaborative filtering recommendations.

## Current Project Context

The existing project is a minimal Spring Boot skeleton:

- `pom.xml` uses Java 17 and Spring Boot 4.0.6.
- `src/main/java/com/wms/shoppingsys/ShoppingSystemApplication.java` only starts the application.
- `src/main/resources/application.properties` only sets `spring.application.name`.
- `src/test/java/com/wms/shoppingsys/ShoppingSystemApplicationTests.java` only verifies context loading.

The directory is not currently a git repository, so this design document cannot be committed until git is initialized.

## Product Scope

The first version uses the balanced product shape:

- Complete storefront shopping flow.
- Complete but focused admin management flow.
- Item-Based collaborative filtering as the main recommendation algorithm.
- Hot, new, and category-based fallback recommendations for cold-start and sparse-data cases.

Out of scope for the first version:

- Real third-party payment integration.
- JWT authentication.
- Logistics tracking.
- Product tag tables and rich product media.
- Offline recommendation jobs or external machine learning services.

## Architecture

Use a single repository with two application directories:

```text
ShoppingSystem/
  backend/   Spring Boot REST API
  frontend/  Vue storefront and admin console
```

The backend owns authentication, authorization, product data, categories, cart operations, order creation, simulated payment, admin APIs, behavior tracking, and recommendation computation.

The frontend owns the customer storefront, checkout flow, order views, admin console pages, route guards, and API request state.

MySQL is the persistent database. Spring Data JPA is the recommended persistence layer for the first version because the data model is direct, entity relationships are clear, and the project can still add custom queries where recommendation scoring needs them.

## Authentication And Authorization

The first version uses simplified token login:

- Users log in with username and password.
- The backend returns an opaque token.
- The frontend stores the token and sends it with API requests.
- The backend resolves the token to a user and role.
- Roles are `USER` and `ADMIN`.

This design keeps authentication behind a small service boundary so the token implementation can later be replaced with JWT without changing controllers or frontend route concepts.

## Storefront Features

The storefront includes:

- Home page with recommended products, hot products, and new products.
- Product list page with category, keyword, and price range filtering.
- Product detail page with product information, inventory, add-to-cart, and similar products.
- Cart page with item quantity updates, deletion, and checkout entry.
- Checkout and simulated payment flow.
- My orders page with order list and order details.

Recommendation placements:

- Home: personalized recommendations for the logged-in user.
- Product detail: similar products based on item-item similarity.
- Cart: products related to cart items, excluding items already in cart.

## Admin Features

The admin console includes:

- Product management: create, edit, list, put on sale, take off sale, and update inventory.
- Category management: create, edit, list, sort, and disable categories.
- Order management: view orders, view order details, and update order status where allowed.
- Recommendation observation: view a user's recent behaviors and the products currently recommended to that user.

The first version does not include advanced recommendation configuration. The recommendation observation page exists to make the algorithm explainable during demos and development.

## Data Model

### users

- `id`
- `username`
- `password_hash`
- `role`: `USER` or `ADMIN`
- `status`
- `created_at`

### categories

- `id`
- `name`
- `parent_id`
- `enabled`
- `sort_order`

### products

- `id`
- `category_id`
- `name`
- `description`
- `brand`
- `price`
- `stock`
- `image_url`
- `status`: `ON_SALE` or `OFF_SALE`
- `sales_count`
- `created_at`
- `updated_at`

### carts

- `id`
- `user_id`
- `product_id`
- `quantity`
- `created_at`
- `updated_at`

### orders

- `id`
- `order_no`
- `user_id`
- `total_amount`
- `status`: `PENDING_PAYMENT`, `PAID`, `CANCELLED`, or `FINISHED`
- `created_at`
- `paid_at`

### order_items

- `id`
- `order_id`
- `product_id`
- `product_name`
- `product_price`
- `quantity`
- `subtotal`

Order items store product name and price snapshots so historical orders remain stable after products are renamed or repriced.

### user_behaviors

- `id`
- `user_id`
- `product_id`
- `behavior_type`: `VIEW`, `CART`, or `ORDER`
- `weight`
- `created_at`

Behavior weights:

- `VIEW = 1`
- `CART = 4`
- `ORDER = 8`

This table is the foundation for Item-Based collaborative filtering.

## Recommendation Algorithm

The first version uses Item-Based collaborative filtering as the main recommendation engine.

User behaviors are aggregated into an implicit user-product score matrix. Product similarity is computed using cosine similarity:

```text
similarity(itemA, itemB) = dot(A, B) / (norm(A) * norm(B))
```

`A` and `B` are product vectors across users. Each vector value is the aggregated implicit score for a user and product.

For a target user, candidate product score is:

```text
recommendScore(candidate)
= sum(userScore(historyItem) * similarity(historyItem, candidate))
```

Recommendation filters:

- Exclude off-sale products.
- Exclude products with zero stock.
- Exclude products the user has already purchased.
- Exclude current cart items for cart recommendations.

Fallback strategy:

- New user: recommend hot products and new products.
- Sparse behavior data: supplement with same-category and hot products.
- Product detail page: prefer similar products, then supplement with same-category and same-brand products.

Recommendation service boundaries:

```text
RecommendationEngine
ItemBasedCollaborativeFilteringEngine
FallbackRecommendationService
RecommendationService
```

Controllers and frontend pages depend on `RecommendationService`, not on the concrete algorithm. Later versions can add caching, scheduled similarity computation, hybrid ranking, or matrix factorization behind the same service boundary.

## REST API Design

### Auth

- `POST /api/auth/login`
- `POST /api/auth/register`

### Products

- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/products/{id}/similar`

### Categories

- `GET /api/categories`

### Cart

- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{id}`
- `DELETE /api/cart/items/{id}`

### Orders

- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders/{id}/pay`

### Recommendations

- `GET /api/recommendations/home`
- `GET /api/recommendations/cart`

### Admin

- `/api/admin/products`
- `/api/admin/categories`
- `/api/admin/orders`

Admin endpoints require the `ADMIN` role.

## Frontend Organization

Vue source structure:

```text
frontend/src/
  api/           HTTP request wrappers
  router/        storefront and admin routes
  stores/        auth state and cart state
  views/shop/    home, product list, detail, cart, checkout, orders
  views/admin/   product management, category management, order management
  components/    product cards, filters, tables, navigation, forms
```

Routes:

```text
/                 Home
/products         Product list
/products/:id     Product detail
/cart             Cart
/checkout         Checkout and simulated payment
/orders           My orders
/admin/products   Admin product management
/admin/categories Admin category management
/admin/orders     Admin order management
```

Route guards:

- Storefront order/cart routes require a logged-in user.
- Admin routes require `ADMIN`.

## Error Handling

Backend responses use a unified envelope:

```json
{
  "success": false,
  "message": "库存不足",
  "data": null
}
```

Global exception handling covers:

- Invalid request parameters.
- Unauthenticated requests.
- Unauthorized role access.
- Missing products, categories, carts, or orders.
- Insufficient stock.
- Invalid order state transitions.

Business constraints:

- Off-sale or out-of-stock products cannot be added to cart.
- Checkout revalidates stock before creating an order.
- Order creation deducts stock.
- Paying an already paid order returns an error.
- Admin users cannot use normal user cart endpoints unless explicitly logged in as a user.
- Normal users cannot access admin endpoints.

## Testing Strategy

Backend tests should cover:

- Login and role authorization.
- Product search and filtering.
- Cart quantity updates and stock limits.
- Order creation and stock deduction.
- Simulated payment status transition.
- Item-Based collaborative filtering recommendations.
- Fallback recommendation behavior.
- Admin product and order permissions.

Frontend tests can focus on route guards, key page rendering, API state transitions, and the checkout flow.

## Evolution Plan

Planned future upgrades:

- Simplified token authentication to JWT.
- Real-time recommendation computation to cached or scheduled similarity computation.
- Item-Based collaborative filtering to hybrid recommendation or matrix factorization.
- Simulated payment to payment records and third-party payment callbacks.
- Single product image to multiple images and rich product details.
- Simple account model to address book, logistics, and richer user profile.
