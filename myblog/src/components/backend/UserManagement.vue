<template>
  <div class="user-management">
    <div class="list-header">
      <h2 class="list-title">用户管理</h2>
      <div class="header-stats">
        <span class="stat-badge total">共 {{ totalUsers }} 位用户</span>
        <span class="stat-badge active">活跃 {{ activeCount }}</span>
        <span class="stat-badge disabled">禁用 {{ disabledCount }}</span>
      </div>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <i class="bi bi-search"></i>
        <input type="text" v-model="searchKeyword" placeholder="搜索用户名..." class="search-input" />
      </div>
      <div class="custom-select-wrapper">
        <select v-model="filterLoginType" class="filter-select">
          <option value="">全部方式</option>
          <option value="password">密码登录</option>
          <option value="weixin">微信登录</option>
        </select>
        <i class="bi bi-chevron-down select-arrow"></i>
      </div>
      <div class="custom-select-wrapper">
        <select v-model="filterStatus" class="filter-select">
          <option value="">全部状态</option>
          <option value="active">活跃</option>
          <option value="disabled">禁用</option>
        </select>
        <i class="bi bi-chevron-down select-arrow"></i>
      </div>
    </div>

    <div class="table-wrapper">
      <table class="user-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-user">用户</th>
            <th class="col-type">登录方式</th>
            <th class="col-status">状态</th>
            <th class="col-date">注册时间</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in paginatedUsers" :key="user.id">
            <td class="col-id">{{ user.id }}</td>
            <td class="col-user">
              <div class="user-cell">
                <img
                  v-if="user.weixinImageUrl || user.avatar"
                  :src="user.weixinImageUrl || user.avatar"
                  :alt="user.displayName"
                  class="user-avatar"
                />
                <div v-else class="user-avatar-placeholder">
                  {{ (user.displayName || user.username || '?')[0].toUpperCase() }}
                </div>
                <div class="user-info">
                  <span class="user-name">{{ user.displayName || user.username }}</span>
                  <span v-if="user.username" class="user-login-name">@{{ user.username }}</span>
                </div>
              </div>
            </td>
            <td class="col-type">
              <span class="type-tag" :class="user.loginType">
                <i :class="user.loginType === 'weixin' ? 'bi bi-wechat' : 'bi bi-key'"></i>
                {{ user.loginType === 'weixin' ? '微信' : '密码' }}
              </span>
            </td>
            <td class="col-status">
              <span class="status-dot" :class="user.status">
                {{ user.status === 'active' ? '活跃' : '禁用' }}
              </span>
            </td>
            <td class="col-date">{{ formatDate(user.createdAt) }}</td>
            <td class="col-actions">
              <button
                class="btn-action"
                :class="user.status === 'active' ? 'btn-disable' : 'btn-enable'"
                @click="toggleUserStatus(user)"
              >
                <i :class="user.status === 'active' ? 'bi bi-lock' : 'bi bi-unlock'"></i>
                {{ user.status === 'active' ? '禁用' : '启用' }}
              </button>
              <button class="btn-action btn-delete" @click="confirmDelete(user)">
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
          <h3>确认删除用户</h3>
        </div>
        <p class="modal-body">确定要删除用户「{{ deleteTarget?.displayName || deleteTarget?.username }}」吗？此操作不可撤销。</p>
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
import { getUsers, updateUserStatus, deleteUser } from '@/services/userService.js'

const users = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const filterLoginType = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = 10
const showDeleteModal = ref(false)
const deleteTarget = ref(null)

const totalUsers = computed(() => users.value.length)
const activeCount = computed(() => users.value.filter(u => u.status === 'active').length)
const disabledCount = computed(() => users.value.filter(u => u.status === 'disabled').length)

const filteredUsers = computed(() => {
  let result = users.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(u =>
      (u.username || '').toLowerCase().includes(kw) ||
      (u.displayName || '').toLowerCase().includes(kw) ||
      (u.weixinName || '').toLowerCase().includes(kw)
    )
  }
  if (filterLoginType.value) {
    result = result.filter(u => u.loginType === filterLoginType.value)
  }
  if (filterStatus.value) {
    result = result.filter(u => u.status === filterStatus.value)
  }
  return result
})

const totalPages = computed(() => Math.ceil(filteredUsers.value.length / pageSize))

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredUsers.value.slice(start, start + pageSize)
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

watch([searchKeyword, filterLoginType, filterStatus], () => {
  currentPage.value = 1
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUsers()
    users.value = res.data.users || res.data || []
  } catch {
    users.value = []
  } finally {
    loading.value = false
  }
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 'active' ? 'disabled' : 'active'
  try {
    await updateUserStatus(user.id, newStatus)
    user.status = newStatus
  } catch {
    console.error('更新用户状态失败')
  }
}

const confirmDelete = (user) => {
  deleteTarget.value = user
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await deleteUser(deleteTarget.value.id)
    users.value = users.value.filter(u => u.id !== deleteTarget.value.id)
    showDeleteModal.value = false
    deleteTarget.value = null
  } catch {
    showDeleteModal.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '—'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 4px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.list-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.stat-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.82rem;
  font-weight: 500;
}

.stat-badge.total {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
}

.stat-badge.active {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.stat-badge.disabled {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
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
  min-width: 120px;
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

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th {
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

.user-table td {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  font-size: 0.9rem;
  color: #555;
}

.user-table tr:last-child td {
  border-bottom: none;
}

.user-table tr:hover td {
  background: rgba(0, 123, 255, 0.03);
}

.col-id { width: 60px; }
.col-type { width: 100px; }
.col-status { width: 100px; }
.col-date { width: 110px; }
.col-actions { width: 200px; }

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.user-avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #007bff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 600;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-login-name {
  font-size: 0.78rem;
  color: #999;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.78rem;
  font-weight: 500;
}

.type-tag.weixin {
  background: rgba(7, 193, 96, 0.1);
  color: #07c130;
}

.type-tag.password {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
}

.type-tag i {
  font-size: 0.85rem;
}

.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.78rem;
  font-weight: 500;
}

.status-dot::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot.active {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.status-dot.active::before {
  background: #28a745;
}

.status-dot.disabled {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.status-dot.disabled::before {
  background: #dc3545;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 4px;
}

.btn-disable {
  background: rgba(255, 193, 7, 0.1);
  color: #e0a800;
  border: 1px solid rgba(255, 193, 7, 0.25);
}

.btn-disable:hover {
  background: #ffc107;
  color: #333;
  border-color: #ffc107;
}

.btn-enable {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
  border: 1px solid rgba(40, 167, 69, 0.25);
}

.btn-enable:hover {
  background: #28a745;
  color: white;
  border-color: #28a745;
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
    align-items: flex-start;
  }

  .filter-bar {
    flex-direction: column;
  }

  .search-box {
    min-width: auto;
  }

  .user-table th,
  .user-table td {
    padding: 10px 8px;
    font-size: 0.82rem;
  }

  .col-id, .col-date {
    display: none;
  }

  .user-avatar, .user-avatar-placeholder {
    width: 30px;
    height: 30px;
  }

  .btn-action {
    padding: 4px 8px;
    font-size: 0.75rem;
  }
}
</style>