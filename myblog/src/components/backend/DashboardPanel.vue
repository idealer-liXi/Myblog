<template>
  <div class="dashboard">
    <h2 class="dashboard-title">仪表盘</h2>
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.key">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <i :class="stat.icon"></i>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>
    <div class="recent-section">
      <h3 class="section-title">
        <i class="bi bi-clock-history"></i>
        最近文章
      </h3>
      <div class="recent-list">
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <span>正在加载...</span>
        </div>
        <div v-else-if="error" class="error-state">
          <i class="bi bi-exclamation-circle"></i>
          <span>{{ error }}</span>
        </div>
        <div v-else-if="recentArticles.length === 0" class="empty-state">
          <i class="bi bi-inbox"></i>
          <span>暂无文章</span>
        </div>
        <router-link
          v-else
          v-for="article in recentArticles"
          :key="article.id"
          :to="{ name: 'backend-article-edit', params: { id: article.id } }"
          class="recent-item"
        >
          <div class="recent-item-image">
            <img v-if="article.image" :src="article.image" :alt="article.title" />
            <i v-else class="bi bi-image placeholder-icon"></i>
          </div>
          <div class="recent-item-content">
            <span class="recent-item-title">{{ article.title }}</span>
            <span class="recent-item-meta">
              <span class="recent-item-category">{{ article.category }}</span>
              <span class="recent-item-date">{{ formatDate(article.date) }}</span>
            </span>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAllArticles } from '@/services/articleService.js'

const articles = ref([])
const loading = ref(false)
const error = ref(null)

const recentArticles = computed(() => {
  return [...articles.value]
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .slice(0, 5)
})

const stats = computed(() => [
  {
    key: 'articles',
    label: '文章总数',
    value: articles.value.length,
    icon: 'bi bi-file-earmark-text',
    gradient: 'linear-gradient(135deg, #007bff 0%, #0056b3 100%)'
  },
  {
    key: 'categories',
    label: '分类数量',
    value: new Set(articles.value.map(a => a.category || a.theme)).size,
    icon: 'bi bi-tags',
    gradient: 'linear-gradient(135deg, #28a745 0%, #1e7e34 100%)'
  },
  {
    key: 'latest',
    label: '最新更新',
    value: recentArticles.value.length > 0 ? formatDate(recentArticles.value[0].date) : '无',
    icon: 'bi bi-clock',
    gradient: 'linear-gradient(135deg, #ffc107 0%, #e0a800 100%)'
  },
  {
    key: 'views',
    label: '总浏览量',
    value: '—',
    icon: 'bi bi-eye',
    gradient: 'linear-gradient(135deg, #dc3545 0%, #bd2130 100%)'
  }
])

const fetchArticles = async () => {
  loading.value = true
  error.value = null
  try {
    articles.value = await getAllArticles()
  } catch (err) {
    error.value = '获取文章列表失败'
    articles.value = []
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.dashboard {
  padding: 4px;
}

.dashboard-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 24px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.3rem;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.82rem;
  color: #888;
  font-weight: 500;
}

.recent-section {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #007bff;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.25s ease;
}

.recent-item:hover {
  background: rgba(0, 123, 255, 0.06);
  border-color: rgba(0, 123, 255, 0.15);
  transform: translateX(4px);
}

.recent-item-image {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recent-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-icon {
  font-size: 1.2rem;
  color: #ccc;
}

.recent-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recent-item-title {
  font-size: 0.92rem;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-item-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.78rem;
  color: #999;
}

.recent-item-category {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
  padding: 1px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.loading-state, .error-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #999;
  gap: 8px;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(0, 123, 255, 0.15);
  border-top-color: #007bff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state {
  color: #dc3545;
}

.error-state i {
  font-size: 1.5rem;
}
</style>