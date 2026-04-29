import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/Dashboard/Dashboard.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/ecrs',
      name: 'ecr-list',
      component: () => import('@/views/ECR/ECRList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/ecrs/:id',
      name: 'ecr-detail',
      component: () => import('@/views/ECR/ECRDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/ecos',
      name: 'eco-list',
      component: () => import('@/views/ECO/ECOList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/ecos/:id',
      name: 'eco-detail',
      component: () => import('@/views/ECO/ECODetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/users',
      name: 'user-list',
      component: () => import('@/views/User/UserList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/users/:id',
      name: 'user-detail',
      component: () => import('@/views/User/UserDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/parts',
      name: 'part-list',
      component: () => import('@/views/Part/PartList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/parts/:id',
      name: 'part-detail',
      component: () => import('@/views/Part/PartDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/documents',
      name: 'document-list',
      component: () => import('@/views/Document/DocumentList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/documents/:id',
      name: 'document-detail',
      component: () => import('@/views/Document/DocumentDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/boms',
      name: 'bom-list',
      component: () => import('@/views/BOM/BOMList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/boms/:id',
      name: 'bom-detail',
      component: () => import('@/views/BOM/BOMDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/audit-logs',
      name: 'audit-log-list',
      component: () => import('@/views/AuditLog/AuditLogList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/audit-logs/:id',
      name: 'audit-log-detail',
      component: () => import('@/views/AuditLog/AuditLogDetail.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // If route requires auth and user is not authenticated
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // Try to verify token
    const isValid = await authStore.checkAuth()
    if (!isValid) {
      next({ name: 'login' })
      return
    }
  }

  // If user is authenticated and trying to access login page, redirect to home
  if (to.name === 'login' && authStore.isAuthenticated) {
    next({ name: 'home' })
    return
  }

  next()
})

export default router
