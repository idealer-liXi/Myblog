import { createRouter, createWebHistory } from 'vue-router'
import BlogView from '@/views/blog/BlogView.vue';
import MeView from '@/views/introduce/MeView.vue';
import LoginView from "@/views/user/LoginView.vue";
import RegisterView from "@/views/user/RegisterView.vue";
import BackendView from "@/views/backend/BackendView.vue";
import ArticlePerform from "@/components/ArticlePerform.vue";
import GitHubStarView from "@/views/github/GitHubStarView.vue";
import DashboardPanel from "@/components/backend/DashboardPanel.vue";
import ArticleList from "@/components/backend/ArticleList.vue";
import ArticleForm from "@/components/backend/ArticleForm.vue";
import CategoryManagement from "@/components/backend/CategoryManagement.vue";
import ImageManagement from "@/components/backend/ImageManagement.vue";
import UserManagement from "@/components/backend/UserManagement.vue";

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
    children: [
      {
        path:'/blog/article/:theme',
        name: 'article',
        component: ArticlePerform,
      }
    ]
  },
  {
    path: '/me',
    name: 'me',
    component: MeView,
  },
  {
    path: '/user/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/user/register',
    name: 'register',
    component: RegisterView,
  },
  {
    path: '/backend',
    name: 'backend',
    component: BackendView,
    meta: { requiresAuth: false },
    redirect: { name: 'backend-dashboard' },
    children: [
      {
        path: 'dashboard',
        name: 'backend-dashboard',
        component: DashboardPanel,
      },
      {
        path: 'articles',
        name: 'backend-articles',
        component: ArticleList,
      },
      {
        path: 'articles/new',
        name: 'backend-article-new',
        component: ArticleForm,
      },
      {
        path: 'articles/:id/edit',
        name: 'backend-article-edit',
        component: ArticleForm,
      },
      {
        path: 'categories',
        name: 'backend-categories',
        component: CategoryManagement,
      },
      {
        path: 'images',
        name: 'backend-images',
        component: ImageManagement,
      },
      {
        path: 'users',
        name: 'backend-users',
        component: UserManagement,
      }
    ]
  },
  {
    path: '/github/star',
    name: 'githubstar',
    component: GitHubStarView
  },


]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const jwtToken = localStorage.getItem('jwtToken')
    const jwtTokenExpiry = localStorage.getItem('jwtTokenExpiry')
    const isJwtValid = jwtToken && jwtTokenExpiry && Date.now() < Number(jwtTokenExpiry)
    const openid = document.cookie.split(';').some(c => c.trim().startsWith('openIdToken='))

    if (isJwtValid || openid) {
      next()
    } else {
      next({ name: 'login' })
    }
  } else {
    next()
  }
})

export default router