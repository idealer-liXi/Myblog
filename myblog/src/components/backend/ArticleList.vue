<template>
  <div class="article-list">
    <div class="list-header">
      <h2 class="list-title">文章管理</h2>
      <router-link :to="{ name: 'backend-article-new' }" class="btn-create">
        <i class="bi bi-plus-lg"></i>
        新建文章
      </router-link>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <i class="bi bi-search"></i>
        <input type="text" v-model="searchKeyword" placeholder="搜索文章标题..." class="search-input" />
      </div>
      <div class="custom-select-wrapper">
        <select v-model="filterTheme" class="filter-select">
          <option value="">全部分类</option>
          <option v-for="theme in themes" :key="theme" :value="theme">{{ theme }}</option>
        </select>
        <i class="bi bi-chevron-down select-arrow"></i>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>正在加载文章...</span>
    </div>

    <div v-else-if="error" class="error-state">
      <i class="bi bi-exclamation-triangle-fill"></i>
      <p>{{ error }}</p>
      <button class="btn-retry" @click="fetchArticles">重试</button>
    </div>

    <div v-else-if="filteredArticles.length === 0" class="empty-state">
      <i class="bi bi-inbox"></i>
      <p>暂无文章</p>
    </div>

    <div v-else class="table-wrapper">
      <table class="article-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-title">标题</th>
            <th class="col-category">分类</th>
            <th class="col-date">日期</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="article in paginatedArticles" :key="article.id">
            <td class="col-id">{{ article.id }}</td>
            <td class="col-title">
              <div class="title-cell">
                <img v-if="article.image" :src="article.image" :alt="article.title" class="article-thumb" />
                <div v-else class="article-thumb-placeholder">
                  <i class="bi bi-image"></i>
                </div>
                <span class="title-text">{{ article.title }}</span>
              </div>
            </td>
            <td class="col-category">
              <span class="category-tag">{{ article.category || article.theme }}</span>
            </td>
            <td class="col-date">{{ formatDate(article.date) }}</td>
            <td class="col-actions">
              <router-link :to="{ name: 'backend-article-edit', params: { id: article.id } }" class="btn-action btn-edit">
                <i class="bi bi-pencil-square"></i>
                编辑
              </router-link>
              <button class="btn-action btn-delete" @click="confirmDelete(article)">
                <i class="bi bi-trash3"></i>
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
        <i class="bi bi-chevron-left"></i>
      </button>
      <button
        v-for="page in displayedPages"
        :key="page"
        class="page-btn"
        :class="{ active: page === currentPage }"
        @click="currentPage = page"
      >
        {{ page }}
      </button>
      <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">
        <i class="bi bi-chevron-right"></i>
      </button>
    </div>

    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <i class="bi bi-exclamation-triangle modal-icon"></i>
          <h3>确认删除</h3>
        </div>
        <p class="modal-body">确定要删除文章「{{ deleteTarget?.title }}」吗？此操作不可撤销。</p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDeleteModal = false">取消</button>
          <button class="btn-confirm-delete" @click="handleDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getAllArticles, deleteArticle } from '@/services/articleService.js'

const articles = ref([])
const loading = ref(false)
const error = ref(null)
const searchKeyword = ref('')
const filterTheme = ref('')
const currentPage = ref(1)
const pageSize = 10
const showDeleteModal = ref(false)
const deleteTarget = ref(null)

const themes = ['java', 'python', 'c++', 'vue']

const filteredArticles = computed(() => {
  let result = articles.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(a => a.title.toLowerCase().includes(kw))
  }
  if (filterTheme.value) {
    result = result.filter(a => (a.category || a.theme) === filterTheme.value)
  }
  return result
})

const totalPages = computed(() => Math.ceil(filteredArticles.value.length / pageSize))

const paginatedArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredArticles.value.slice(start, start + pageSize)
})

const displayedPages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  const delta = 2
  const start = Math.max(1, current - delta)
  const end = Math.min(total, current + delta)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

watch([searchKeyword, filterTheme], () => {
  currentPage.value = 1
})

const fetchArticles = async () => {
  loading.value = true
  error.value = null
  try {
    articles.value = await getAllArticles()
  } catch (err) {
    error.value = '获取文章列表失败，请稍后重试'
    articles.value = []
  } finally {
    loading.value = false
  }
}

const confirmDelete = (article) => {
  deleteTarget.value = article
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await deleteArticle(deleteTarget.value.id)
    articles.value = articles.value.filter(a => a.id !== deleteTarget.value.id)
    showDeleteModal.value = false
    deleteTarget.value = null
  } catch (err) {
    error.value = '删除失败，请稍后重试'
    showDeleteModal.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.article-list {
  --surface: rgba(255, 255, 255, 0.76);
  --surface-strong: rgba(255, 255, 255, 0.93);
  --line: rgba(128, 145, 184, 0.16);
  --line-strong: rgba(87, 116, 184, 0.26);
  --text: #25324d;
  --muted: #6f7b93;
  --soft: #97a2b7;
  --accent: #5378d6;
  --accent-soft: rgba(83, 120, 214, 0.1);
  --danger: #d46b6b;
  --danger-soft: rgba(212, 107, 107, 0.1);
  padding: 8px;
  color: var(--text);
  font-family: 'Avenir Next', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  gap: 16px;
}

.list-title {
  font-size: 1.55rem;
  font-weight: 700;
  color: var(--text);
  margin: 0;
  letter-spacing: -0.02em;
}

.btn-create {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: var(--surface-strong);
  color: var(--accent);
  border: 1px solid rgba(83, 120, 214, 0.18);
  border-radius: 14px;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.25s ease;
  box-shadow: 0 12px 26px rgba(120, 136, 170, 0.12);
}

.btn-create:hover {
  background: #ffffff;
  color: #395fbe;
  text-decoration: none;
  transform: translateY(-1px);
  border-color: rgba(83, 120, 214, 0.28);
  box-shadow: 0 16px 30px rgba(120, 136, 170, 0.16);
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 200px;
  position: relative;
}

.search-box i {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--soft);
  font-size: 0.9rem;
}

.search-input,
.filter-select {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: var(--surface);
  color: var(--text);
  font-size: 0.9rem;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.search-input {
  padding: 11px 14px 11px 40px;
}

.search-input::placeholder {
  color: var(--soft);
}

.search-input:focus,
.filter-select:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(83, 120, 214, 0.08);
  background: white;
}

.custom-select-wrapper {
  position: relative;
  min-width: 140px;
}

.filter-select {
  padding: 11px 36px 11px 14px;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  line-height: 1.5;
}

.select-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.7rem;
  color: var(--soft);
  pointer-events: none;
}

.table-wrapper {
  background: linear-gradient(180deg, var(--surface-strong), var(--surface));
  backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 18px 36px rgba(120, 136, 170, 0.12);
}

.article-table {
  width: 100%;
  border-collapse: collapse;
}

.article-table th {
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 0.82rem;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: rgba(246, 248, 252, 0.94);
  border-bottom: 1px solid var(--line);
}

.article-table td {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(128, 145, 184, 0.1);
  font-size: 0.9rem;
  color: var(--text);
}

.article-table tr:last-child td {
  border-bottom: none;
}

.article-table tr:hover td {
  background: rgba(244, 247, 255, 0.9);
}

.col-id { width: 60px; }
.col-category { width: 100px; }
.col-date { width: 110px; }
.col-actions { width: 160px; }

.title-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.article-thumb,
.article-thumb-placeholder {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  flex-shrink: 0;
}

.article-thumb {
  object-fit: cover;
}

.article-thumb-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(242, 245, 250, 0.95);
  color: #b5bece;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  color: var(--text);
}

.category-tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent);
  font-size: 0.8rem;
  font-weight: 600;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-edit {
  background: var(--accent-soft);
  color: var(--accent);
  border-color: rgba(83, 120, 214, 0.18);
}

.btn-edit:hover {
  background: rgba(83, 120, 214, 0.16);
  color: #395fbe;
}

.btn-delete {
  background: var(--danger-soft);
  color: var(--danger);
  border-color: rgba(212, 107, 107, 0.18);
}

.btn-delete:hover {
  background: rgba(212, 107, 107, 0.16);
  color: #b95454;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  margin-top: 20px;
}

.page-btn {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--surface);
  color: var(--muted);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: rgba(83, 120, 214, 0.08);
  border-color: rgba(83, 120, 214, 0.24);
  color: var(--accent);
}

.page-btn.active {
  background: var(--accent);
  color: white;
  border-color: var(--accent);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.loading-state,
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--muted);
  gap: 8px;
  border: 1px dashed var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.46);
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(83, 120, 214, 0.15);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state i {
  font-size: 2rem;
  color: var(--danger);
}

.btn-retry {
  margin-top: 8px;
  padding: 7px 16px;
  border: 1px solid rgba(212, 107, 107, 0.24);
  border-radius: 10px;
  background: none;
  color: var(--danger);
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s ease;
}

.btn-retry:hover {
  background: rgba(212, 107, 107, 0.08);
}

.empty-state i {
  font-size: 3rem;
  color: #d3d9e5;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(226, 231, 240, 0.44);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(247, 249, 253, 0.94));
  border-radius: 22px;
  padding: 28px;
  width: 420px;
  max-width: 90vw;
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 24px 52px rgba(120, 136, 170, 0.18);
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.modal-icon {
  font-size: 1.5rem;
  color: #dea456;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text);
}

.modal-body {
  color: var(--muted);
  font-size: 0.92rem;
  line-height: 1.5;
  margin: 0 0 24px 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  padding: 8px 20px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.78);
  color: var(--muted);
  cursor: pointer;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: white;
}

.btn-confirm-delete {
  padding: 8px 20px;
  border: 1px solid rgba(212, 107, 107, 0.14);
  border-radius: 10px;
  background: #d66f6f;
  color: white;
  cursor: pointer;
  font-size: 0.88rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-confirm-delete:hover {
  background: #c85e5e;
}

@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .filter-bar {
    flex-direction: column;
  }

  .search-box {
    min-width: auto;
  }

  .article-table th,
  .article-table td {
    padding: 10px 8px;
    font-size: 0.82rem;
  }

  .col-id,
  .col-date {
    display: none;
  }

  .article-thumb,
  .article-thumb-placeholder {
    width: 34px;
    height: 34px;
  }
}
</style>
