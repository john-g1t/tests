import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    alias: '/main',
    name: 'Main page',
    component: () => import('@/views/main-view.vue'),
  },
  {
    path: '/second',
    name: 'Page 2',
    component: () => import('@/views/second-view.vue'),
  },
]

export const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
