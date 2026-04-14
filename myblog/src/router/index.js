import { createRouter, createWebHistory } from 'vue-router'
import BlogView from '@/views/blog/BlogView.vue';
import MeView from '@/views/introduce/MeView.vue';
import LoginView from "@/views/user/LoginView.vue";
import RegisterView from "@/views/user/RegisterView.vue";
import BackendView from "@/views/backend/BackendView.vue";
import ArticlePerform from "@/components/ArticlePerform.vue";
import GitHubStarView from "@/views/github/GitHubStarView.vue";

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
    component: BackendView
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

export default router
