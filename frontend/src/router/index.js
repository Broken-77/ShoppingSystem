import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import HomeView from '../views/shop/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProductsView from '../views/shop/ProductListView.vue'
import ProductDetailView from '../views/shop/ProductDetailView.vue'
import CartView from '../views/shop/CartView.vue'
import CheckoutView from '../views/shop/CheckoutView.vue'
import OrdersView from '../views/shop/OrdersView.vue'
import OrderDetailView from '../views/shop/OrderDetailView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminProductsView from '../views/admin/AdminProductsView.vue'
import AdminCategoriesView from '../views/admin/AdminCategoriesView.vue'
import AdminOrdersView from '../views/admin/AdminOrdersView.vue'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  },
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/products', name: 'products', component: ProductsView },
    { path: '/products/:id', name: 'product-detail', component: ProductDetailView },
    { path: '/cart', name: 'cart', component: CartView, meta: { requiresAuth: true } },
    { path: '/checkout', name: 'checkout', component: CheckoutView, meta: { requiresAuth: true } },
    { path: '/orders', name: 'orders', component: OrdersView, meta: { requiresAuth: true } },
    { path: '/orders/:id', name: 'order-detail', component: OrderDetailView, meta: { requiresAuth: true } },
    { path: '/admin/products', name: 'admin-products', component: AdminProductsView, meta: { requiresAdmin: true } },
    { path: '/admin/categories', name: 'admin-categories', component: AdminCategoriesView, meta: { requiresAdmin: true } },
    { path: '/admin/orders', name: 'admin-orders', component: AdminOrdersView, meta: { requiresAdmin: true } },
    { path: '/admin/users', name: 'admin-users', component: AdminUsersView, meta: { requiresAdmin: true } }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (auth.isAdmin && !to.meta.requiresAdmin && to.path !== '/login' && to.path !== '/register') {
    return '/admin/products'
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return auth.isLoggedIn ? '/products' : '/login'
  }
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return '/login'
  }
  return true
})

export default router
