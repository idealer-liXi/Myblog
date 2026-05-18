<template>
  <div class="article-detail" v-if="article">
    <div class="detail-header">
      <router-link :to="{ name: 'article', params: { theme: article.theme } }" class="back-link">
        <i class="bi bi-arrow-left"></i> 返回列表
      </router-link>
      <h1 class="detail-title">{{ article.title }}</h1>
      <div class="detail-meta">
        <span><i class="bi bi-clock"></i> {{ formatDate(article.date) }}</span>
        <span><i class="bi bi-tag"></i> {{ article.theme }}</span>
      </div>
    </div>
    <MdPreview editorId="article-detail-preview" :modelValue="article.content" class="detail-body" />
  </div>
  <div v-else-if="loading" class="detail-loading">加载中...</div>
  <div v-else class="detail-error">文章不存在</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import { getPublicArticleDetail } from '@/services/articleService'
import 'md-editor-v3/lib/style.css'

const route = useRoute()
const article = ref(null)
const loading = ref(true)

const formatDate = (dateString) => {
  if (!dateString) return '—'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(async () => {
  const articleId = route.params.id
  if (!articleId) {
    loading.value = false
    return
  }
  try {
    const data = await getPublicArticleDetail(Number(articleId))
    article.value = data
  } catch (e) {
    console.error('获取文章详情失败:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;
}
.detail-header {
  margin-bottom: 32px;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
  text-decoration: none;
  font-size: 0.9rem;
  margin-bottom: 16px;
}
.back-link:hover {
  color: #374151;
}
.detail-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px;
}
.detail-meta {
  display: flex;
  gap: 20px;
  color: #9ca3af;
  font-size: 0.85rem;
}
.detail-meta i {
  margin-right: 4px;
}
.detail-body {
  line-height: 1.8;
  color: #374151;
  font-size: 1rem;
}
.detail-loading,
.detail-error {
  text-align: center;
  padding: 48px;
  color: #9ca3af;
}
</style>
