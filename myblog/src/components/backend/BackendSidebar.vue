<template>
  <div class="sidebar">
    <div class="sidebar-user" v-if="userInfo">
      <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" class="sidebar-avatar" />
      <div v-else class="sidebar-avatar-placeholder">
        <i class="bi bi-person-fill"></i>
      </div>
      <span class="sidebar-username">{{ username }}</span>
    </div>
    <div class="sidebar-divider"></div>
    <nav class="sidebar-nav">
      <router-link :to="{ name: 'backend-dashboard' }" class="sidebar-item" :class="{ active: $route.name === 'backend-dashboard' }">
        <i class="bi bi-speedometer2"></i>
        <span>仪表盘</span>
      </router-link>
      <div class="sidebar-group">
        <div class="sidebar-item sidebar-parent" :class="{ active: isArticleActive }" @click="articleExpanded = !articleExpanded">
          <i class="bi bi-file-earmark-text"></i>
          <span>文章管理</span>
          <i class="bi sidebar-arrow" :class="articleExpanded ? 'bi-chevron-down' : 'bi-chevron-right'"></i>
        </div>
        <transition name="expand">
          <div v-show="articleExpanded" class="sidebar-children">
            <router-link :to="{ name: 'backend-articles' }" class="sidebar-child" :class="{ active: $route.name === 'backend-articles' }">
              <i class="bi bi-list-ul"></i>
              <span>文章列表</span>
            </router-link>
            <router-link :to="{ name: 'backend-article-new' }" class="sidebar-child" :class="{ active: $route.name === 'backend-article-new' }">
              <i class="bi bi-plus-circle"></i>
              <span>新建文章</span>
            </router-link>
            <router-link :to="{ name: 'backend-themes' }" class="sidebar-child" :class="{ active: $route.name === 'backend-themes' }">
              <i class="bi bi-tags"></i>
              <span>主题管理</span>
            </router-link>
          </div>
        </transition>
      </div>
      <div class="sidebar-group">
        <div class="sidebar-item sidebar-parent" :class="{ active: isProjectActive }" @click="projectExpanded = !projectExpanded">
          <i class="bi bi-kanban"></i>
          <span>项目管理</span>
          <i class="bi sidebar-arrow" :class="projectExpanded ? 'bi-chevron-down' : 'bi-chevron-right'"></i>
        </div>
        <transition name="expand">
          <div v-show="projectExpanded" class="sidebar-children">
            <router-link :to="{ name: 'backend-projects' }" class="sidebar-child" :class="{ active: $route.name === 'backend-projects' }">
              <i class="bi bi-list-ul"></i>
              <span>项目列表</span>
            </router-link>
          </div>
        </transition>
      </div>
      <router-link :to="{ name: 'backend-music' }" class="sidebar-item" :class="{ active: $route.name === 'backend-music' }">
        <i class="bi bi-disc"></i>
        <span>音乐管理</span>
      </router-link>
      <router-link :to="{ name: 'backend-messages' }" class="sidebar-item" :class="{ active: $route.name === 'backend-messages' }">
        <i class="bi bi-chat-left-text"></i>
        <span>留言管理</span>
      </router-link>
<div class="sidebar-group">
        <div class="sidebar-item sidebar-parent" :class="{ active: isImageActive }" @click="imageExpanded = !imageExpanded">
          <i class="bi bi-image"></i>
          <span>图片管理</span>
          <i class="bi sidebar-arrow" :class="imageExpanded ? 'bi-chevron-down' : 'bi-chevron-right'"></i>
        </div>
        <transition name="expand">
          <div v-show="imageExpanded" class="sidebar-children">
            <router-link :to="{ name: 'backend-images-document' }" class="sidebar-child" :class="{ active: $route.name === 'backend-images-document' }">
              <i class="bi bi-file-earmark-text"></i>
              <span>文档图片</span>
            </router-link>
            <router-link :to="{ name: 'backend-images-project' }" class="sidebar-child" :class="{ active: $route.name === 'backend-images-project' }">
              <i class="bi bi-kanban"></i>
              <span>项目图片</span>
            </router-link>
            <router-link :to="{ name: 'backend-images-user' }" class="sidebar-child" :class="{ active: $route.name === 'backend-images-user' }">
              <i class="bi bi-person-circle"></i>
              <span>用户图片</span>
            </router-link>
          </div>
        </transition>
      </div>
      <router-link :to="{ name: 'backend-users' }" class="sidebar-item" :class="{ active: $route.name === 'backend-users' }">
        <i class="bi bi-people"></i>
        <span>用户管理</span>
      </router-link>
    </nav>
    <div class="sidebar-spacer"></div>
    <div class="sidebar-divider"></div>
    <button class="sidebar-item sidebar-logout" @click="handleLogout">
      <i class="bi bi-box-arrow-right"></i>
      <span>退出登录</span>
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'

const router = useRouter()
const route = useRoute()
const store = useStore()

const userInfo = computed(() => store.getters['weixin_user/getUserInfo'] || {})
const username = computed(() => userInfo.value.displayName || userInfo.value.username || userInfo.value.weixinName || '管理员')

const articleExpanded = ref(true)
const projectExpanded = ref(true)
const imageExpanded = ref(true)

const isArticleActive = computed(() => [
  'backend-articles',
  'backend-article-new',
  'backend-article-edit',
  'backend-themes'
].includes(route.name))

const isProjectActive = computed(() => [
  'backend-projects'
].includes(route.name))

const isImageActive = computed(() => [
  'backend-images-document',
  'backend-images-project',
  'backend-images-user'
].includes(route.name))

const handleLogout = () => {
  store.dispatch('weixin_user/logout')
  router.push({ name: 'blog' })
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 20px 14px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: auto;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.sidebar:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.sidebar::-webkit-scrollbar {
  width: 3px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: rgba(0, 123, 255, 0.06);
  border-radius: 12px;
  margin-bottom: 8px;
}

.sidebar-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #007bff;
  flex-shrink: 0;
}

.sidebar-avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #007bff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1.2rem;
}

.sidebar-username {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(0, 123, 255, 0.2) 50%, transparent 100%);
  margin: 12px 0;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  color: #555;
  font-weight: 500;
  font-size: 0.92rem;
  cursor: pointer;
  transition: all 0.25s ease;
  text-decoration: none;
  position: relative;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
}

.sidebar-item:hover {
  background: rgba(0, 123, 255, 0.08);
  color: #007bff;
  text-decoration: none;
}

.sidebar-item.active {
  background: rgba(0, 123, 255, 0.12);
  color: #007bff;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #007bff;
}

.sidebar-item i:first-child {
  font-size: 1.1rem;
  width: 20px;
  text-align: center;
}

.sidebar-parent {
  justify-content: space-between;
}

.sidebar-parent > span {
  flex: 1;
}

.sidebar-arrow {
  font-size: 0.75rem;
  transition: transform 0.25s ease;
}

.sidebar-children {
  padding-left: 16px;
  overflow: hidden;
}

.sidebar-child {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 8px;
  color: #777;
  font-size: 0.88rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
  margin: 2px 0;
}

.sidebar-child:hover {
  background: rgba(0, 123, 255, 0.08);
  color: #007bff;
  text-decoration: none;
}

.sidebar-child.active {
  color: #007bff;
  font-weight: 600;
  background: rgba(0, 123, 255, 0.1);
}

.sidebar-child i {
  font-size: 0.85rem;
  width: 18px;
  text-align: center;
}

.sidebar-spacer {
  flex: 1;
}

.sidebar-logout {
  margin-top: 4px;
  color: #dc3545 !important;
}

.sidebar-logout:hover {
  background: rgba(220, 53, 69, 0.1) !important;
  color: #dc3545 !important;
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.25s ease;
  max-height: 180px;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: auto;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    padding: 10px 14px;
    gap: 4px;
  }

  .sidebar-user {
    width: 100%;
    margin-bottom: 4px;
  }

  .sidebar-divider {
    display: none;
  }

  .sidebar-nav {
    flex-direction: row;
    flex-wrap: wrap;
    width: 100%;
    gap: 4px;
  }

  .sidebar-item {
    padding: 6px 10px;
    font-size: 0.85rem;
  }

  .sidebar-children {
    display: flex;
    flex-direction: row;
    padding-left: 0;
    gap: 4px;
  }

  .sidebar-child {
    padding: 4px 8px;
    font-size: 0.8rem;
  }

  .sidebar-spacer {
    display: none;
  }

  .sidebar-logout {
    margin-left: auto;
  }

  .sidebar-arrow {
    display: none;
  }
}
.sidebar {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  box-shadow: 0 18px 36px rgba(120, 136, 170, 0.12);
}

.sidebar-user {
  background: rgba(244, 247, 255, 0.82);
}

.sidebar-avatar {
  border-color: rgba(83, 120, 214, 0.8);
}

.sidebar-avatar-placeholder {
  background: linear-gradient(135deg, #5a82db, #466dcc);
}

.sidebar-username {
  color: #25324d;
}

.sidebar-divider {
  background: linear-gradient(90deg, transparent 0%, rgba(83, 120, 214, 0.18) 50%, transparent 100%);
}

.sidebar-item,
.sidebar-child {
  color: #66748f;
  border-radius: 12px;
}

.sidebar-item:hover,
.sidebar-child:hover {
  background: rgba(83, 120, 214, 0.08);
  color: #5378d6;
}

.sidebar-item.active {
  background: rgba(83, 120, 214, 0.1);
  color: #5378d6;
  box-shadow: inset 3px 0 0 #5378d6;
}

.sidebar-child.active {
  background: rgba(83, 120, 214, 0.08);
  color: #5378d6;
}

.sidebar-logout {
  color: #d46b6b !important;
}

.sidebar-logout:hover {
  background: rgba(212, 107, 107, 0.08) !important;
  color: #d46b6b !important;
}
</style>
