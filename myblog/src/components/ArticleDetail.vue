<template>
  <div class="article-detail" v-if="article">
    <div class="detail-header">
      <div class="header-decoration">
        <span class="decoration-line"></span>
        <span class="decoration-dot"></span>
      </div>
      <div class="detail-meta">
        <span class="meta-chip meta-date">
          <i class="bi bi-calendar3"></i>
          {{ formatDate(article.date) }}
        </span>
        <span class="meta-chip meta-theme">
          <i class="bi bi-hash"></i>
          {{ article.theme }}
        </span>
      </div>
      <div class="title-row">
        <h1 class="detail-title">{{ article.title }}</h1>
        <router-link :to="{ name: 'article', params: { theme: article.theme } }" class="back-link">
          <i class="bi bi-arrow-left"></i>
          <span>返回列表</span>
        </router-link>
      </div>
      <div class="title-underline"></div>
    </div>
    <div class="detail-body-wrapper">
      <MdPreview editorId="article-detail-preview" :modelValue="article.content" class="detail-body" />
    </div>
  </div>
  <div v-else-if="loading" class="detail-loading">
    <div class="loading-pulse">
      <div class="pulse-bar pulse-bar-title"></div>
      <div class="pulse-bar pulse-bar-meta"></div>
      <div class="pulse-bar pulse-bar-line"></div>
      <div class="pulse-bar pulse-bar-paragraph"></div>
      <div class="pulse-bar pulse-bar-paragraph pulse-bar-short"></div>
    </div>
    <span class="loading-text">正在加载文章...</span>
  </div>
  <div v-else class="detail-error">
    <i class="bi bi-file-earmark-x"></i>
    <p>文章不存在</p>
    <router-link :to="{ name: 'blog' }" class="error-back">
      <i class="bi bi-house"></i> 返回主页
    </router-link>
  </div>
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
  padding: 0 16px 40px;
}

.detail-header {
  position: relative;
  margin-bottom: 36px;
  padding: 32px 36px 28px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 20px;
  box-shadow:
    0 14px 36px rgba(120, 138, 180, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.header-decoration {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 16px 0 14px;
}

.decoration-line {
  display: block;
  width: 28px;
  height: 2px;
  border-radius: 2px;
  background: linear-gradient(90deg, #5378d6, rgba(83, 120, 214, 0.2));
}

.decoration-dot {
  display: block;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #5378d6;
  opacity: 0.6;
}

.detail-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.78rem;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.meta-date {
  color: #475569;
  background: rgba(71, 85, 105, 0.07);
}

.meta-theme {
  color: #5378d6;
  background: rgba(83, 120, 214, 0.08);
}

.meta-chip i {
  font-size: 0.76rem;
}

.title-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.detail-title {
  font-size: 1.7rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  line-height: 1.4;
  letter-spacing: -0.01em;
  flex: 1;
  min-width: 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  color: #6b7fa0;
  text-decoration: none;
  font-size: 0.8rem;
  font-weight: 500;
  padding: 5px 12px;
  border-radius: 16px;
  background: rgba(71, 85, 105, 0.06);
  transition: all 0.25s ease;
  white-space: nowrap;
  margin-bottom: 4px;
}

.back-link:hover {
  color: #334155;
  background: rgba(71, 85, 105, 0.12);
}

.back-link i {
  font-size: 0.84rem;
}

.title-underline {
  width: 48px;
  height: 3px;
  border-radius: 3px;
  background: linear-gradient(90deg, #5378d6, rgba(83, 120, 214, 0.3));
  margin-top: 4px;
}

.detail-body-wrapper {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 20px;
  box-shadow: 0 14px 36px rgba(120, 138, 180, 0.1);
  padding: 28px 36px 32px;
  overflow: hidden;
}

.detail-body {
  line-height: 1.85;
  color: #334155;
  font-size: 0.98rem;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64px 20px;
  gap: 16px;
}

.loading-pulse {
  width: 100%;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pulse-bar {
  border-radius: 8px;
  background: linear-gradient(90deg, rgba(148, 163, 184, 0.08), rgba(148, 163, 184, 0.16), rgba(148, 163, 184, 0.08));
  background-size: 200% 100%;
  animation: pulse-sweep 1.6s ease-in-out infinite;
}

.pulse-bar-title {
  height: 28px;
  width: 70%;
}

.pulse-bar-meta {
  height: 18px;
  width: 40%;
}

.pulse-bar-line {
  height: 8px;
  width: 48px;
  margin-top: 8px;
}

.pulse-bar-paragraph {
  height: 14px;
  width: 100%;
}

.pulse-bar-short {
  width: 55%;
}

@keyframes pulse-sweep {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.loading-text {
  color: #94a3b8;
  font-size: 0.9rem;
}

.detail-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64px 20px;
  color: #94a3b8;
}

.detail-error i {
  font-size: 3rem;
  color: #cbd5e1;
  margin-bottom: 8px;
}

.detail-error p {
  font-size: 1.05rem;
  margin: 0 0 16px;
  color: #64748b;
}

.error-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 20px;
  background: rgba(71, 85, 105, 0.08);
  color: #475569;
  text-decoration: none;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.error-back:hover {
  background: rgba(71, 85, 105, 0.14);
}

@media (max-width: 768px) {
  .detail-header {
    padding: 22px 18px 20px;
    border-radius: 16px;
  }

  .detail-body-wrapper {
    padding: 20px 16px 24px;
    border-radius: 16px;
  }

  .detail-title {
    font-size: 1.35rem;
  }

  .title-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .back-link {
    margin-bottom: 0;
  }

  .meta-chip {
    font-size: 0.72rem;
  }
}
</style>
