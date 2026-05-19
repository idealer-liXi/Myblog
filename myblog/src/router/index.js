import { createRouter, createWebHistory } from 'vue-router'
import BlogView from '@/views/blog/BlogView.vue';
import MeView from '@/views/introduce/MeView.vue';
import LoginView from "@/views/user/LoginView.vue";
import RegisterView from "@/views/user/RegisterView.vue";
import UserProfileView from "@/views/user/UserProfileView.vue";
import MessageBoardView from "@/views/message/MessageBoardView.vue";
import BackendView from "@/views/backend/BackendView.vue";
import ArticlePerform from "@/components/ArticlePerform.vue";
import ArticleDetail from "@/components/ArticleDetail.vue";
import GitHubStarView from "@/views/github/GitHubStarView.vue";
import DashboardPanel from "@/components/backend/DashboardPanel.vue";
import ArticleList from "@/components/backend/ArticleList.vue";
import ArticleForm from "@/components/backend/ArticleForm.vue";
import MessageManagement from "@/components/backend/MessageManagement.vue";
import ThemeManagement from "@/components/backend/ThemeManagement.vue";
import ProjectManagement from "@/components/backend/ProjectManagement.vue";
import ImageManagement from "@/components/backend/ImageManagement.vue";
import MusicManagement from "@/components/backend/MusicManagement.vue";
import UserManagement from "@/components/backend/UserManagement.vue";
import { clearSession, hasValidSession } from '@/utils/authSession'

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
      },
      {
        path:'/blog/article/:theme/:id',
        name: 'article-detail',
        component: ArticleDetail,
      }
    ]
  },
  {
    path: '/me',
    name: 'me',
    component: MeView,
  },
  {
    path: '/message',
    name: 'message-board',
    component: MessageBoardView,
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
    path: '/user/profile',
    name: 'user-profile',
    component: UserProfileView,
    meta: { requiresAuth: true },
  },
  {
    path: '/backend',
    name: 'backend',
    component: BackendView,
    meta: { requiresAuth: true },
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
        path: 'themes',
        name: 'backend-themes',
        component: ThemeManagement,
      },
      {
        path: 'projects',
        name: 'backend-projects',
        component: ProjectManagement,
      },
      {
        path: 'music',
        name: 'backend-music',
        component: MusicManagement,
      },
      {
        path: 'messages',
        name: 'backend-messages',
        component: MessageManagement,
      },
      {
        path: 'images',
        redirect: { name: 'backend-images-document' },
      },
      {
        path: 'images/document',
        name: 'backend-images-document',
        component: ImageManagement,
        props: { defaultTab: 'DOCUMENT' },
      },
      {
        path: 'images/project',
        name: 'backend-images-project',
        component: ImageManagement,
        props: { defaultTab: 'PROJECT' },
      },
      {
        path: 'images/user',
        name: 'backend-images-user',
        component: ImageManagement,
        props: { defaultTab: 'USER' },
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
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  if (hasValidSession()) {
    next()
    return
  }

  clearSession()
  next({ name: 'login', query: { redirect: to.fullPath } })
})

export default router
