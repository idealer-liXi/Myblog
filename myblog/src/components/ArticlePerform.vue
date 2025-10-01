<template>
  <div class="article-list">
    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
    </div>
    <div class="articles-container">
      <div 
        v-for="article in articles" 
        :key="article.id" 
        class="article-card"
        @click="viewArticle(article.id)"
      >
        <div class="article-image">
          <img :src="article.image" :alt="article.title" />
        </div>
        <div class="article-content">
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-summary">{{ article.summary }}</p>
          <div class="article-meta">
            <span class="article-date">{{ formatDate(article.date) }}</span>
            <span class="article-category">{{ article.category }}</span>
          </div>
        </div>
      </div>
    </div>
    <div v-if="loading" class="loading">
      <p>正在加载文章...</p>
    </div>
    <div v-if="!loading && !error && articles.length === 0" class="no-articles">
      <p>暂无相关文章</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticlesByTheme } from '@/services/articleService.js'

// 响应式数据
const articles = ref([])
const loading = ref(false)
const currentTheme = ref('')
const error = ref(null)

// 路由实例
const route = useRoute()
const router = useRouter()

// 获取文章列表
const fetchArticles = async (theme) => {
  loading.value = true
  error.value = null
  
  try {
    // 使用真实的API服务
    const response = await getArticlesByTheme(theme)
    articles.value = response
  } catch (err) {
    console.error('获取文章列表失败:', err)
    error.value = '获取文章列表失败，请稍后重试'
    articles.value = []
    
    // 如果API调用失败，使用模拟数据作为备用
    useMockData(theme)
  } finally {
    loading.value = false
  }
}

// 备用模拟数据方法
const useMockData = (theme) => {
  const articlesByTheme = {
    'java':[
      {
        id:1,
        title:'java基础',
        summary:'java基础知识',
        date:'2025-06-28',
        category:'java',
        image:'https://picsum.photos/200/300'
      },
      {
        id:2,
        title:'java基础',
        summary:'java基础知识',
        date:'2025-06-28',
        category:'java',
        image:'https://picsum.photos/200/300'
      }
    ]
  }
  
  articles.value = articlesByTheme[theme] || []
}

// 查看文章详情
const viewArticle = (articleId) => {
  router.push(`/blog/article/${currentTheme.value}/${articleId}`)
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 监听路由参数变化
watch(
  () => route.params.theme,
  (newTheme) => {
    if (newTheme) {
      currentTheme.value = newTheme
      fetchArticles(newTheme)
    }
  },
  { immediate: true }
)

// 组件挂载时获取当前主题
onMounted(() => {
  currentTheme.value = route.params.theme
  if (currentTheme.value) {
    fetchArticles(currentTheme.value)
  }
})
</script>

<style scoped>
.article-list {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 0;
}

.list-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
  font-weight: 600;
}

.articles-container {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.article-card {
  background: white;
  border-radius: 0;
  box-shadow: none;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: row;
  height: 140px;
  width: 100%;
  margin: 0;
}

.article-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.article-image {
  width: 200px;
  height: 140px;
  overflow: hidden;
  flex-shrink: 0;
}

.article-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .article-image img {
  transform: scale(1.05);
}

.article-content {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-title {
  font-size: 1.1rem;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.3;
}

.article-summary {
  color: #666;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  font-size: 0.85rem;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #999;
  margin-top: auto;
}

.article-date {
  background: #f5f5f5;
  padding: 3px 6px;
  border-radius: 3px;
}

.article-category {
  background: #e3f2fd;
  color: #1976d2;
  padding: 3px 6px;
  border-radius: 3px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-card {
    flex-direction: column;
    height: auto;
  }
  
  .article-image {
    width: 100%;
    height: 200px;
  }
  
  .article-list {
    padding: 15px;
  }
  
  .list-title {
    font-size: 1.5rem;
    margin-bottom: 20px;
  }
  
  .article-content {
    padding: 15px;
  }
  
  .article-title {
    font-size: 1.2rem;
  }
}

/* 加载和空状态样式 */
.loading, .no-articles {
  text-align: center;
  padding: 40px 20px;
  color: #666;
  font-size: 1.1rem;
}

.loading p, .no-articles p {
  margin: 0;
}

/* 错误消息样式 */
.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #c62828;
}

.error-message p {
  margin: 0;
  font-size: 1rem;
}
</style>