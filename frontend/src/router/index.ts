import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// Implements: plan.md §3.1 页面模块划分
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/rooms',
    name: 'RoomList',
    component: () => import('@/views/room/RoomListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/reservations/new',
    name: 'ReservationCreate',
    component: () => import('@/views/reservation/ReservationCreateView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/reservations/mine',
    name: 'MyReservations',
    component: () => import('@/views/reservation/MyReservationView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/reservations/:id',
    name: 'ReservationDetail',
    component: () => import('@/views/reservation/ReservationDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/approvals',
    name: 'Approvals',
    component: () => import('@/views/reservation/ApprovalView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/rooms',
    name: 'RoomManage',
    component: () => import('@/views/room/RoomManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/users',
    name: 'UserManage',
    component: () => import('@/views/user/UserManageView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/notification/NotificationView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/statistics',
    name: 'Statistics',
    component: () => import('@/views/statistics/StatisticsView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/',
    redirect: '/rooms',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Implements: plan.md §3.1 鉴权态 - 路由守卫检查登录状态
router.beforeEach((to, _from, next) => {
  const isAuthenticated = !!sessionStorage.getItem('user')

  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'Login' })
  } else if (!to.meta.requiresAuth && isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
