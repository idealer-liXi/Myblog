import { createRouter, createWebHistory } from 'vue-router'
import BlogView from '@/views/blog/BlogView.vue';

const routes = [
  {
    path:'/',
    name:'home',
    redirect: '/blog',
  },
  {
    path: '/blog',
    name: 'blog',
    component: BlogView,
  }


]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
