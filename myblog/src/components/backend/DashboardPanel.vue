<template>
  <div class="dashboard">
    <section class="dashboard-hero">
      <div class="hero-copy">
        <span class="hero-eyebrow">Content Studio</span>
        <h2 class="dashboard-title">仪表盘</h2>
        <p class="dashboard-subtitle">概览站点内容状态、创作节奏与最近更新，保持后台信息清晰有序。</p>
      </div>
      <div class="hero-badge">
        <span class="hero-badge-label">当前内容库</span>
        <strong class="hero-badge-value">{{ articles.length }} 篇文章</strong>
      </div>
    </section>

    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.key">
        <div class="stat-card-glow" :style="{ background: stat.gradient }"></div>
        <div class="stat-icon-wrap">
          <div class="stat-icon" :style="{ background: stat.gradient }">
            <i :class="stat.icon"></i>
          </div>
        </div>
        <div class="stat-info">
          <span class="stat-kicker">Overview</span>
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>
    <div class="recent-section">
      <div class="section-head">
        <h3 class="section-title">
          <i class="bi bi-clock-history"></i>
          最近文章
        </h3>
        <p class="section-subtitle">最近发布与更新的内容，点击可快速进入编辑。</p>
      </div>
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
            <div v-else class="placeholder-surface">
              <i class="bi bi-image placeholder-icon"></i>
            </div>
          </div>
          <div class="recent-item-content">
            <span class="recent-item-title">{{ article.title }}</span>
            <span class="recent-item-meta">
              <span class="recent-item-category">{{ article.theme }}</span>
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
    label: '主题数量',
    value: new Set(articles.value.map(a => a.theme)).size,
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
  --dashboard-text: #1d2a44;
  --dashboard-muted: #66748f;
  --dashboard-soft: #8f9cb3;
  --dashboard-line: rgba(120, 146, 193, 0.18);
  --dashboard-line-strong: rgba(112, 147, 208, 0.28);
  --dashboard-card: rgba(255, 255, 255, 0.78);
  --dashboard-card-strong: rgba(255, 255, 255, 0.9);
  --dashboard-shadow: 0 18px 40px rgba(84, 104, 148, 0.12);
  --dashboard-shadow-hover: 0 24px 52px rgba(70, 96, 148, 0.18);
  --dashboard-accent: #3f7cff;
  position: relative;
  padding: 8px;
  color: var(--dashboard-text);
}

.dashboard::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(101, 154, 255, 0.16), transparent 34%),
    radial-gradient(circle at 85% 12%, rgba(163, 203, 255, 0.28), transparent 26%),
    linear-gradient(180deg, rgba(248, 251, 255, 0.95), rgba(241, 246, 255, 0.84));
  pointer-events: none;
}

.dashboard::after {
  content: '';
  position: absolute;
  inset: 18px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  pointer-events: none;
}

.dashboard > * {
  position: relative;
  z-index: 1;
}

.dashboard-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  margin: 0 0 28px;
  padding: 26px 28px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(248, 250, 255, 0.72));
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  box-shadow: var(--dashboard-shadow);
  overflow: hidden;
}

.dashboard-hero::before {
  content: '';
  position: absolute;
  top: -32px;
  right: -36px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(82, 151, 255, 0.2), rgba(82, 151, 255, 0));
}

.hero-copy {
  max-width: 620px;
}

.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  margin-bottom: 14px;
  border-radius: 999px;
  background: rgba(70, 122, 221, 0.09);
  border: 1px solid rgba(81, 132, 228, 0.14);
  color: #4771bf;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.dashboard-title {
  font-size: clamp(1.7rem, 2vw, 2.2rem);
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--dashboard-text);
  margin: 0;
}

.dashboard-subtitle {
  margin: 12px 0 0;
  max-width: 56ch;
  font-size: 0.98rem;
  line-height: 1.75;
  color: var(--dashboard-muted);
}

.hero-badge {
  min-width: 190px;
  padding: 16px 18px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(245, 249, 255, 0.82));
  border: 1px solid rgba(121, 154, 212, 0.16);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.hero-badge-label {
  display: block;
  margin-bottom: 8px;
  color: var(--dashboard-soft);
  font-size: 0.76rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-badge-value {
  display: block;
  font-size: 1.08rem;
  font-weight: 700;
  color: var(--dashboard-text);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
  margin-bottom: 32px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, var(--dashboard-card-strong), var(--dashboard-card));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: var(--dashboard-shadow);
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
}

.stat-card::after {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: 21px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  pointer-events: none;
}

.stat-card-glow {
  position: absolute;
  width: 130px;
  height: 130px;
  top: -44px;
  right: -36px;
  border-radius: 50%;
  opacity: 0.14;
  filter: blur(8px);
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(122, 156, 224, 0.42);
  box-shadow: var(--dashboard-shadow-hover);
}

.stat-icon-wrap {
  position: relative;
  flex-shrink: 0;
}

.stat-icon-wrap::before {
  content: '';
  position: absolute;
  inset: -8px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(255, 255, 255, 0.72);
}

.stat-icon {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.3rem;
  flex-shrink: 0;
  box-shadow: 0 12px 22px rgba(62, 93, 164, 0.24);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-kicker {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #7f90ad;
}

.stat-value {
  font-size: 1.55rem;
  font-weight: 800;
  color: var(--dashboard-text);
  line-height: 1.25;
  letter-spacing: -0.03em;
}

.stat-label {
  font-size: 0.84rem;
  color: var(--dashboard-muted);
  font-weight: 600;
}

.recent-section {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(248, 251, 255, 0.72));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 24px;
  padding: 22px 24px 24px;
  box-shadow: var(--dashboard-shadow);
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-title {
  font-size: 1.08rem;
  font-weight: 700;
  color: var(--dashboard-text);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title i {
  color: var(--dashboard-accent);
}

.section-subtitle {
  margin: 0;
  font-size: 0.86rem;
  color: var(--dashboard-muted);
  line-height: 1.6;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(126, 151, 199, 0.14);
  border-radius: 18px;
  text-decoration: none;
  box-shadow: 0 10px 24px rgba(96, 116, 150, 0.08);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease, background 0.25s ease;
}

.recent-item::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(77, 129, 231, 0.08), transparent 45%);
  opacity: 0;
  transition: opacity 0.25s ease;
  pointer-events: none;
}

.recent-item:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(86, 132, 222, 0.28);
  box-shadow: 0 16px 30px rgba(89, 116, 171, 0.12);
  transform: translateX(4px);
}

.recent-item:hover::after {
  opacity: 1;
}

.recent-item-image {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(180deg, #f8fbff, #edf3ff);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(132, 160, 210, 0.18);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.88);
}

.recent-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-surface {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 30% 20%, rgba(98, 145, 230, 0.14), rgba(98, 145, 230, 0) 60%);
}

.placeholder-icon {
  font-size: 1.2rem;
  color: #97a6bf;
}

.recent-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recent-item-title {
  font-size: 0.96rem;
  font-weight: 700;
  color: var(--dashboard-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 0.79rem;
  color: var(--dashboard-soft);
}

.recent-item-category {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  background: rgba(67, 123, 231, 0.1);
  color: #3f72d6;
  border: 1px solid rgba(90, 135, 218, 0.14);
  border-radius: 999px;
  font-weight: 700;
}

.recent-item-date {
  position: relative;
  padding-left: 14px;
}

.recent-item-date::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(122, 144, 183, 0.7);
  transform: translateY(-50%);
}

.loading-state, .error-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 44px 20px;
  color: var(--dashboard-muted);
  gap: 10px;
  border: 1px dashed rgba(124, 151, 203, 0.24);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.45);
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid rgba(83, 128, 222, 0.14);
  border-top-color: var(--dashboard-accent);
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

.empty-state i,
.loading-state i {
  font-size: 1.4rem;
}

@media (max-width: 900px) {
  .dashboard-hero,
  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-badge {
    min-width: 0;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .dashboard {
    padding: 4px;
  }

  .dashboard-hero,
  .recent-section,
  .stat-card {
    padding-left: 18px;
    padding-right: 18px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .recent-item {
    align-items: flex-start;
  }

  .recent-item-title {
    white-space: normal;
  }
}
.dashboard {
  --dashboard-text: #25324d;
  --dashboard-muted: #6f7b93;
  --dashboard-soft: #97a2b7;
  --dashboard-line: rgba(128, 145, 184, 0.16);
  --dashboard-line-strong: rgba(87, 116, 184, 0.26);
  --dashboard-card: rgba(255, 255, 255, 0.76);
  --dashboard-card-strong: rgba(255, 255, 255, 0.93);
  --dashboard-shadow: 0 18px 36px rgba(120, 136, 170, 0.12);
  --dashboard-shadow-hover: 0 22px 42px rgba(120, 136, 170, 0.16);
  --dashboard-accent: #5378d6;
}

.dashboard::before {
  background:
    radial-gradient(circle at top left, rgba(129, 164, 234, 0.16), transparent 34%),
    linear-gradient(180deg, rgba(250, 251, 255, 0.96), rgba(244, 247, 253, 0.88));
}

.dashboard-hero,
.recent-section,
.stat-card,
.recent-item,
.loading-state,
.error-state,
.empty-state {
  border-color: rgba(255, 255, 255, 0.72);
  box-shadow: var(--dashboard-shadow);
}

.dashboard-hero,
.recent-section,
.stat-card,
.recent-item-image {
  background: linear-gradient(180deg, var(--dashboard-card-strong), var(--dashboard-card));
}

.hero-eyebrow,
.recent-item-category {
  background: rgba(83, 120, 214, 0.1);
  color: var(--dashboard-accent);
}

.recent-item:hover {
  transform: translateX(2px);
}

.error-state {
  color: #d46b6b;
}
</style>
