<template>
  <div class="message-management">
    <div class="list-header">
      <div>
        <h2 class="list-title">留言管理</h2>
        <p class="list-subtitle">审核、拒绝、删除留言，并切换发布模式。</p>
      </div>
      <div class="settings-panel">
        <label class="settings-label">发布模式</label>
        <select v-model="reviewMode" class="settings-select" @change="saveSettings">
          <option value="AUTO_PUBLISH">直接公开</option>
          <option value="MANUAL_REVIEW">先审后发</option>
        </select>
      </div>
    </div>

    <div class="filter-bar">
      <button
        v-for="item in statusOptions"
        :key="item.value || 'all'"
        class="filter-pill"
        :class="{ active: statusFilter === item.value }"
        @click="changeStatus(item.value)"
      >
        {{ item.label }}
      </button>
    </div>

    <div v-if="loading" class="empty-state">正在加载留言...</div>
    <div v-else-if="page.items.length === 0" class="empty-state">当前筛选下暂无留言</div>
    <div v-else class="message-table-shell">
      <table class="message-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户</th>
            <th>内容</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="message in page.items" :key="message.id">
            <td>{{ message.id }}</td>
            <td>{{ message.username || '用户' }}</td>
            <td class="message-content-cell">{{ message.content }}</td>
            <td>
              <span class="status-badge" :class="statusClass(message.status)">{{ statusLabel(message.status) }}</span>
            </td>
            <td>{{ formatDate(message.createdAt) }}</td>
            <td class="message-actions-cell">
              <button v-if="message.status === 'PENDING'" class="btn-action btn-approve" @click="publish(message.id)">通过</button>
              <button v-if="message.status === 'PENDING'" class="btn-action btn-reject" @click="reject(message.id)">拒绝</button>
              <button class="btn-action btn-delete" @click="remove(message.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { deleteAdminMessage, getAdminMessages, getMessageSettings, publishMessage, rejectMessage, updateMessageSettings } from '@/services/messageService.js'

const loading = ref(false)
const statusFilter = ref('')
const reviewMode = ref('AUTO_PUBLISH')
const page = ref({ items: [], page: 1, pageSize: 20, total: 0 })
const statusOptions = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已删除', value: 'DELETED' }
]

const loadMessages = async () => {
  loading.value = true
  try {
    page.value = await getAdminMessages({ status: statusFilter.value, page: 1, pageSize: 20 })
  } finally {
    loading.value = false
  }
}

const loadSettings = async () => {
  const settings = await getMessageSettings()
  reviewMode.value = settings.reviewMode || 'AUTO_PUBLISH'
}

const saveSettings = async () => {
  await updateMessageSettings({ reviewMode: reviewMode.value })
}

const changeStatus = async (status) => {
  statusFilter.value = status
  await loadMessages()
}

const publish = async (id) => {
  await publishMessage(id)
  await loadMessages()
}

const reject = async (id) => {
  await rejectMessage(id)
  await loadMessages()
}

const remove = async (id) => {
  await deleteAdminMessage(id)
  await loadMessages()
}

const formatDate = (value) => {
  if (!value) return ''
  return value.replace('T', ' ').replace('Z', '').slice(0, 16)
}

const statusLabel = (status) => {
  if (status === 'PENDING') return '待审核'
  if (status === 'PUBLISHED') return '已发布'
  if (status === 'REJECTED') return '已拒绝'
  if (status === 'DELETED') return '已删除'
  return status || '未知状态'
}

const statusClass = (status) => {
  if (status === 'PENDING') return 'status-pending'
  if (status === 'PUBLISHED') return 'status-published'
  return 'status-rejected'
}

onMounted(async () => {
  await loadSettings()
  await loadMessages()
})
</script>

<style scoped>
.message-management {
  color: #1f2937;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.list-title {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
}

.list-subtitle {
  margin: 8px 0 0;
  color: #64748b;
}

.settings-panel {
  min-width: 220px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settings-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
}

.settings-select {
  min-height: 44px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.9);
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.filter-pill {
  border: none;
  border-radius: 999px;
  padding: 8px 14px;
  background: rgba(148, 163, 184, 0.12);
  color: #475569;
  font-weight: 600;
}

.filter-pill.active {
  background: rgba(74, 134, 232, 0.16);
  color: #2563eb;
}

.message-table-shell {
  overflow-x: auto;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.message-table {
  width: 100%;
  border-collapse: collapse;
}

.message-table th,
.message-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
}

.message-table th {
  font-size: 0.84rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.message-content-cell {
  min-width: 280px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-actions-cell {
  white-space: nowrap;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
}

.status-pending {
  background: rgba(250, 204, 21, 0.16);
  color: #a16207;
}

.status-published {
  background: rgba(34, 197, 94, 0.14);
  color: #15803d;
}

.status-rejected {
  background: rgba(239, 68, 68, 0.12);
  color: #b91c1c;
}

.btn-action {
  border: none;
  border-radius: 10px;
  padding: 8px 12px;
  margin-right: 8px;
  font-weight: 600;
}

.btn-approve {
  background: rgba(34, 197, 94, 0.14);
  color: #15803d;
}

.btn-reject,
.btn-delete {
  background: rgba(239, 68, 68, 0.12);
  color: #b91c1c;
}

.empty-state {
  padding: 48px 20px;
  text-align: center;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  color: #64748b;
}

@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
  }

  .settings-panel {
    width: 100%;
  }
}
</style>
