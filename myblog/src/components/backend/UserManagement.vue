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
          <tr v-for="user in users" :key="user.id">
            <td class="col-id">{{ user.id }}</td>
            <td class="col-user">
              <div class="user-cell">
                <img
                  v-if="user.effectiveAvatar"
                  :src="user.effectiveAvatar"
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
              <button class="btn-action btn-detail" @click="openDetail(user)">
                <i class="bi bi-eye"></i>
                详情
              </button>
              <button
                class="btn-action"
                :class="user.status === 'active' ? 'btn-disable' : 'btn-enable'"
                @click="toggleUserStatus(user)"
                :disabled="user.status === 'deleted'"
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

    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <i class="bi bi-person-vcard modal-icon info"></i>
          <h3>用户详情</h3>
        </div>
        <div v-if="detailLoading" class="modal-body">加载中...</div>
        <div v-else-if="detailUser" class="detail-grid">
          <div class="detail-avatar-wrap">
            <img v-if="detailUser.effectiveAvatar" :src="detailUser.effectiveAvatar" :alt="detailUser.displayName" class="detail-avatar" />
            <div v-else class="user-avatar-placeholder large">{{ (detailUser.displayName || detailUser.username || '?')[0].toUpperCase() }}</div>
          </div>
          <div class="detail-fields">
            <div class="detail-row"><span>用户ID</span><strong>{{ detailUser.id }}</strong></div>
            <div class="detail-row"><span>用户名</span><strong>{{ detailUser.username || '—' }}</strong></div>
            <div class="detail-row"><span>显示名</span><strong>{{ detailUser.displayName || '—' }}</strong></div>
            <div class="detail-row"><span>登录方式</span><strong>{{ detailUser.loginType === 'weixin' ? '微信登录' : '密码登录' }}</strong></div>
            <div class="detail-row"><span>角色</span><strong>{{ (detailUser.roles || []).join(', ') || '—' }}</strong></div>
            <div class="detail-row"><span>状态</span><strong>{{ detailUser.status || '—' }}</strong></div>
            <div class="detail-row"><span>头像来源</span><strong>{{ detailUser.avatarSource || 'DEFAULT' }}</strong></div>
            <div class="detail-row"><span>微信昵称</span><strong>{{ detailUser.weixinName || '—' }}</strong></div>
            <div class="detail-row"><span>微信绑定</span><strong>{{ detailUser.weixinBound ? '已绑定' : '未绑定' }}</strong></div>
            <div class="detail-row"><span>创建时间</span><strong>{{ formatDate(detailUser.createdAt) }}</strong></div>
            <div class="detail-row"><span>更新时间</span><strong>{{ formatDate(detailUser.updatedAt) }}</strong></div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeDetail">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getUsers, updateUserStatus, deleteUser, getUserById } from '@/services/userService.js'

const users = ref([])
const total = ref(0)
const loading = ref(false)
const searchKeyword = ref('')
const filterLoginType = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = 10
const showDeleteModal = ref(false)
const deleteTarget = ref(null)
const showDetailModal = ref(false)
const detailLoading = ref(false)
const detailUser = ref(null)

const totalUsers = computed(() => total.value)
const activeCount = computed(() => users.value.filter(u => u.status === 'active').length)
const disabledCount = computed(() => users.value.filter(u => u.status === 'disabled').length)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

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
  fetchUsers()
})

watch(currentPage, () => {
  fetchUsers()
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUsers({
      keyword: searchKeyword.value || undefined,
      loginType: filterLoginType.value || undefined,
      status: filterStatus.value || undefined,
      page: currentPage.value,
      pageSize
    })
    users.value = res.data?.users || []
    total.value = Number(res.data?.total || 0)
  } catch {
    users.value = []
    total.value = 0
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

const openDetail = async (user) => {
  detailLoading.value = true
  showDetailModal.value = true
  detailUser.value = null
  try {
    const res = await getUserById(user.id)
    detailUser.value = res.data || null
  } catch {
    detailUser.value = null
  } finally {
    detailLoading.value = false
  }
}

const closeDetail = () => {
  showDetailModal.value = false
  detailUser.value = null
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
    total.value = Math.max(0, total.value - 1)
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

.btn-detail {
  background: rgba(108, 117, 125, 0.08);
  color: #495057;
  border: 1px solid rgba(108, 117, 125, 0.2);
}

.btn-detail:hover {
  background: #495057;
  color: white;
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

.detail-modal {
  width: 720px;
  max-width: 92vw;
}

.modal-icon.info {
  color: #007bff;
}

.detail-grid {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr);
  gap: 20px;
  margin-bottom: 12px;
}

.detail-avatar-wrap {
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.detail-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.user-avatar-placeholder.large {
  width: 120px;
  height: 120px;
  font-size: 2rem;
}

.detail-fields {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.detail-row span {
  color: #777;
  font-size: 0.88rem;
}

.detail-row strong {
  color: #333;
  font-size: 0.9rem;
  font-weight: 600;
  text-align: right;
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

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-avatar-wrap {
    justify-content: flex-start;
  }

  .detail-row {
    flex-direction: column;
    gap: 4px;
  }

  .detail-row strong {
    text-align: left;
  }
}
.user-management {
  --surface: rgba(255, 255, 255, 0.76);
  --line: rgba(128, 145, 184, 0.16);
  --line-strong: rgba(87, 116, 184, 0.26);
  --text: #25324d;
  --muted: #6f7b93;
  --soft: #97a2b7;
  --accent: #5378d6;
  --danger: #d46b6b;
  --success: #5c9b74;
  --warning: #d4a152;
  font-family: 'Avenir Next', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.list-title,
.user-name,
.user-table td,
.search-input,
.filter-select,
.modal-header h3 {
  color: var(--text);
}

.search-box i,
.select-arrow,
.user-login-name,
.modal-body {
  color: var(--muted);
}

.search-input,
.filter-select,
.table-wrapper,
.modal-content,
.btn-cancel,
.page-btn {
  background: rgba(255, 255, 255, 0.8);
  border-color: var(--line);
}

.search-input:focus,
.filter-select:focus {
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(83, 120, 214, 0.08);
}

.user-table th {
  color: var(--muted);
  background: rgba(246, 248, 252, 0.94);
  border-bottom-color: var(--line);
}

.user-table tr:hover td {
  background: rgba(244, 247, 255, 0.9);
}

.user-avatar-placeholder,
.page-btn.active {
  background: linear-gradient(135deg, #5a82db, #466dcc);
}

.type-tag.password,
.stat-badge.total {
  background: rgba(83, 120, 214, 0.1);
  color: var(--accent);
}

.type-tag.weixin,
.status-dot.active,
.stat-badge.active,
.btn-enable {
  background: rgba(92, 155, 116, 0.12);
  color: var(--success);
}

.btn-disable {
  background: rgba(212, 161, 82, 0.12);
  color: var(--warning);
}

.status-dot.disabled,
.stat-badge.disabled,
.btn-delete,
.btn-confirm-delete {
  background: rgba(212, 107, 107, 0.1);
  color: var(--danger);
}

.modal-overlay {
  background: rgba(226, 231, 240, 0.44);
  backdrop-filter: blur(10px);
}
</style>
