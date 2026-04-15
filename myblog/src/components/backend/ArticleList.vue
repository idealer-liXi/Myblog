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
  padding: 4px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.btn-create {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: #007bff;
  color: white;
  border-radius: 10px;
  text-decoration: none;
  font-weight: 500;
  font-size: 0.9rem;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.25);
}

.btn-create:hover {
  background: #0062cc;
  color: white;
  text-decoration: none;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(0, 123, 255, 0.35);
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
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
  color: #999;
  font-size: 0.9rem;
}

.search-input {
  width: 100%;
  padding: 10px 14px 10px 38px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
  background: white;
}

.custom-select-wrapper {
  position: relative;
  min-width: 140px;
}

.filter-select {
  width: 100%;
  padding: 10px 36px 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s ease;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  color: #555;
  line-height: 1.5;
}

.filter-select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
  background: white;
}

.select-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.7rem;
  color: #888;
  pointer-events: none;
  transition: transform 0.25s ease;
}

.table-wrapper {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
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
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: rgba(0, 123, 255, 0.04);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.article-table td {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  font-size: 0.9rem;
  color: #555;
}

.article-table tr:last-child td {
  border-bottom: none;
}

.article-table tr:hover td {
  background: rgba(0, 123, 255, 0.03);
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

.article-thumb {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.article-thumb-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #ccc;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  color: #333;
}

.category-tag {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 500;
  text-decoration: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-edit {
  background: rgba(0, 123, 255, 0.08);
  color: #007bff;
  border: 1px solid rgba(0, 123, 255, 0.2);
}

.btn-edit:hover {
  background: #007bff;
  color: white;
}

.btn-delete {
  background: rgba(220, 53, 69, 0.08);
  color: #dc3545;
  border: 1px solid rgba(220, 53, 69, 0.2);
}

.btn-delete:hover {
  background: #dc3545;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  margin-top: 20px;
}

.page-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
  color: #555;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: rgba(0, 123, 255, 0.1);
  border-color: #007bff;
  color: #007bff;
}

.page-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.loading-state, .error-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
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

.error-state i {
  font-size: 2rem;
  color: #dc3545;
}

.btn-retry {
  margin-top: 8px;
  padding: 6px 16px;
  border: 1px solid #dc3545;
  border-radius: 8px;
  background: none;
  color: #dc3545;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s ease;
}

.btn-retry:hover {
  background: #dc3545;
  color: white;
}

.empty-state i {
  font-size: 3rem;
  color: #ddd;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 28px;
  width: 420px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.modal-icon {
  font-size: 1.5rem;
  color: #ffc107;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.modal-body {
  color: #666;
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
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  color: #666;
  cursor: pointer;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-confirm-delete {
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  background: #dc3545;
  color: white;
  cursor: pointer;
  font-size: 0.88rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-confirm-delete:hover {
  background: #c82333;
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

  .col-id, .col-date {
    display: none;
  }

  .article-thumb {
    width: 32px;
    height: 32px;
  }
}
</style>