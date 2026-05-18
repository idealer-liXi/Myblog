<template>
  <div class="article-list">
    <div v-if="error" class="error-message">
      <i class="bi bi-exclamation-triangle-fill"></i>
      <span>{{ error }}</span>
    </div>
    <div v-if="articles.length > 0" class="articles-container">
      <div 
        v-for="article in articles" 
        :key="article.id" 
        class="article-card"
        @click="viewArticle(article.id)"
      >
        <div class="article-image">
          <ImageInitialFallback
            :src="article.image"
            :name="article.title"
            :alt="article.title"
            wrapperClass="article-image-frame"
            imageClass="article-image-media"
            fallbackClass="article-image-fallback"
          />
        </div>
        <div class="article-content">
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-summary">{{ article.summary }}</p>
          <div class="article-meta">
            <span class="article-date"><i class="bi bi-clock"></i> {{ formatDate(article.date) }}</span>
            <span class="article-category">{{ article.theme }}</span>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!error && articles.length === 0" class="no-articles">
      <i class="bi bi-inbox"></i>
      <p>暂无相关文章</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticlesByTheme } from '@/services/articleService.js'
import ImageInitialFallback from '@/components/common/ImageInitialFallback.vue'

const articles = ref([])
const loading = ref(false)
const currentTheme = ref('')
const error = ref(null)

const route = useRoute()
const router = useRouter()

const fetchArticles = async (theme) => {
  loading.value = true
  error.value = null
  articles.value = []
  
  try {
    const response = await getArticlesByTheme(theme)
    articles.value = response
  } catch (err) {
    console.error('获取文章列表失败:', err)
    error.value = '获取文章列表失败，请稍后重试'
    useMockData(theme)
  } finally {
    loading.value = false
  }
}

const useMockData = (theme) => {
  const articlesByTheme = {
    'java':[
      {
        id:1,
        title:'java基础',
        summary:'java基础知识',
        date:'2025-06-28',
        theme:'java',
        image:'https://picsum.photos/200/300'
      },
      {
        id:2,
        title:'java基础',
        summary:'java基础知识',
        date:'2025-06-28',
        theme:'java',
        image:'https://picsum.photos/200/300'
      }
    ]
  }
  articles.value = articlesByTheme[theme] || []
}

const viewArticle = (articleId) => {
  router.push(`/blog/article/${currentTheme.value}/${articleId}`)
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

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
  margin: 0;
  padding: 0;
}

.articles-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  display: flex;
  flex-direction: row;
  height: 140px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(51, 65, 85, 0.1);
  border-color: rgba(71, 85, 105, 0.2);
}

.article-image {
  width: 200px;
  height: 140px;
  overflow: hidden;
  flex-shrink: 0;
}

.article-image-frame,
:deep(.article-image-media),
:deep(.article-image-fallback) {
  width: 100%;
  height: 100%;
}

:deep(.article-image-media) {
  object-fit: cover;
  transition: transform 0.5s ease;
}

:deep(.article-image-fallback) {
  border-right: 1px solid rgba(148, 163, 184, 0.14);
}

.article-card:hover :deep(.article-image-media) {
  transform: scale(1.08);
}

.article-content {
  padding: 16px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.article-title {
  font-size: 1.05rem;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 600;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-card:hover .article-title {
  color: #475569;
}

.article-summary {
  color: #777;
  line-height: 1.5;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 0.88rem;
  flex: 1;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.78rem;
  color: #aaa;
}

.article-date {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(71, 85, 105, 0.06);
  padding: 3px 10px;
  border-radius: 20px;
  color: #888;
}

.article-date i {
  font-size: 0.72rem;
}

.article-category {
  background: linear-gradient(135deg, rgba(71, 85, 105, 0.1) 0%, rgba(51, 65, 85, 0.08) 100%);
  color: #475569;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 500;
}

.no-articles {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.no-articles i {
  font-size: 3rem;
  color: #ddd;
  margin-bottom: 8px;
}

.no-articles p {
  margin: 0;
  font-size: 1rem;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 235, 238, 0.85);
  backdrop-filter: blur(10px);
  color: #c62828;
  padding: 12px 18px;
  border-radius: 12px;
  margin-bottom: 20px;
  border-left: 4px solid #c62828;
  font-size: 0.92rem;
}

.error-message i {
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .article-card {
    flex-direction: column;
    height: auto;
  }

  .article-image {
    width: 100%;
    height: 180px;
  }

  .article-content {
    padding: 14px;
  }

  .article-title {
    font-size: 1rem;
  }
}
</style>
