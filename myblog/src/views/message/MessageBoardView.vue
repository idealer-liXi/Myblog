<template>
  <section class="message-board-page">
    <div class="message-board-shell">
      <header class="message-board-header">
        <div class="header-accent">
          <span class="message-board-kicker">Message Board</span>
          <h1>留言板</h1>
        </div>
        <p class="header-desc">欢迎留下你的想法。开启审核模式后，留言会在管理员通过后展示。</p>
      </header>

      <div class="message-board-grid">
        <section class="card-panel panel-public">
          <div class="message-block-header">
            <div class="block-title-group">
              <h2><i class="bi bi-chat-square-text"></i> 公开留言</h2>
              <span class="count-badge">{{ page.total }} 条</span>
            </div>
          </div>

          <div class="panel-scroll">
            <div v-if="loading" class="message-state">
              <div class="state-spinner"></div>
              <span>正在加载...</span>
            </div>
            <div v-else-if="error" class="message-state message-error">
              <i class="bi bi-exclamation-triangle"></i>
              <span>{{ error }}</span>
            </div>
            <div v-else-if="page.items.length === 0" class="message-state">
              <i class="bi bi-chat-square-text"></i>
              <span>还没有已发布留言</span>
            </div>
            <div v-else class="message-list">
              <article class="message-board-item" v-for="message in page.items" :key="message.id">
                <div class="message-avatar">
                  {{ (message.username || '用').charAt(0).toUpperCase() }}
                </div>
                <div class="message-body">
                  <div class="message-board-meta">
                    <strong>{{ message.username || '用户' }}</strong>
                    <span class="meta-time">{{ formatDate(message.createdAt) }}</span>
                  </div>
                  <p class="message-board-content">{{ message.content }}</p>
                </div>
              </article>
            </div>
          </div>

          <div class="panel-footer" v-if="page.total > page.pageSize">
            <button class="page-btn" @click="changePage(page.page - 1)" :disabled="page.page <= 1">
              <i class="bi bi-chevron-left"></i> 上一页
            </button>
            <span class="page-indicator">{{ page.page }} / {{ totalPages }}</span>
            <button class="page-btn" @click="changePage(page.page + 1)" :disabled="page.page >= totalPages">
              下一页 <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </section>

        <div class="grid-col-right">
          <div v-if="isLoggedIn" class="card-panel panel-form">
            <div class="panel-label">
              <i class="bi bi-pencil-square"></i>
              <span>撰写留言</span>
            </div>
            <textarea v-model="content" class="message-input" maxlength="500" placeholder="留下你的足迹..." />
            <div class="message-form-footer">
              <span class="message-count" :class="{ warn: content.length > 450 }">{{ content.length }}/500</span>
              <button class="submit-btn" @click="submitMessage" :disabled="submitting || !content.trim()">
                <i class="bi" :class="submitting ? 'bi-arrow-repeat spin' : 'bi-send'"></i>
                {{ submitting ? '提交中...' : '发表留言' }}
              </button>
            </div>
            <p v-if="submitHint" class="message-hint"><i class="bi bi-check-circle"></i> {{ submitHint }}</p>
            <p v-if="submitError" class="message-error-inline"><i class="bi bi-exclamation-circle"></i> {{ submitError }}</p>
          </div>
          <div v-else class="card-panel panel-login">
            <i class="bi bi-lock"></i>
            <span>请先登录后再留言</span>
          </div>

          <section v-if="isLoggedIn" class="card-panel panel-mine">
            <div class="message-block-header">
              <div class="block-title-group">
                <h2><i class="bi bi-person-lines-fill"></i> 我的留言</h2>
              </div>
              <button class="refresh-btn" @click="loadMineMessages" :disabled="mineLoading">
                <i class="bi" :class="mineLoading ? 'bi-arrow-repeat spin' : 'bi-arrow-clockwise'"></i>
                {{ mineLoading ? '刷新中...' : '刷新' }}
              </button>
            </div>

            <div class="panel-scroll">
              <div v-if="mineLoading" class="message-state">
                <div class="state-spinner"></div>
                <span>正在加载...</span>
              </div>
              <div v-else-if="mineMessages.length === 0" class="message-state">
                <i class="bi bi-chat-left-text"></i>
                <span>你还没有留言</span>
              </div>
              <div v-else class="message-list">
                <article class="message-board-item mine-item" v-for="message in mineMessages" :key="message.id">
                  <div class="message-avatar mine-avatar">
                    {{ (message.username || '我').charAt(0).toUpperCase() }}
                  </div>
                  <div class="message-body">
                    <div class="message-board-meta">
                      <strong>{{ message.username || '我' }}</strong>
                      <span class="meta-time">{{ formatDate(message.createdAt) }}</span>
                    </div>
                    <p class="message-board-content">{{ message.content }}</p>
                    <div class="mine-actions">
                      <span class="status-tag" :class="statusClass(message.status)">{{ statusLabel(message.status) }}</span>
                      <button v-if="message.status !== 'DELETED'" class="delete-btn" @click="removeMineMessage(message.id)">
                        <i class="bi bi-trash3"></i> 删除
                      </button>
                    </div>
                  </div>
                </article>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { createMessage, deleteOwnMessage, getMyMessages, getPublicMessages } from '@/services/messageService.js'

const isLoggedIn = computed(() => Boolean(localStorage.getItem('jwtToken')))
const content = ref('')
const page = ref({ items: [], page: 1, pageSize: 10, total: 0 })
const mineMessages = ref([])
const loading = ref(false)
const error = ref('')
const mineLoading = ref(false)
const submitting = ref(false)
const submitHint = ref('')
const submitError = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil((page.value.total || 0) / page.value.pageSize)))

const loadMessages = async (targetPage = 1) => {
  loading.value = true
  error.value = ''
  try {
    page.value = await getPublicMessages({ page: targetPage, pageSize: page.value.pageSize || 10 })
  } catch (err) {
    error.value = err?.message || '加载留言失败'
  } finally {
    loading.value = false
  }
}

const loadMineMessages = async () => {
  if (!isLoggedIn.value) return
  mineLoading.value = true
  try {
    mineMessages.value = await getMyMessages()
  } catch {
    mineMessages.value = []
  } finally {
    mineLoading.value = false
  }
}

const submitMessage = async () => {
  submitting.value = true
  submitHint.value = ''
  submitError.value = ''
  try {
    const created = await createMessage({ content: content.value })
    content.value = ''
    submitHint.value = created.status === 'PENDING' ? '留言已提交，等待审核。' : '留言已发布。'
    await Promise.all([loadMessages(1), loadMineMessages()])
  } catch (err) {
    submitError.value = err?.message || '提交留言失败'
  } finally {
    submitting.value = false
  }
}

const changePage = (targetPage) => {
  if (targetPage < 1 || targetPage > totalPages.value) return
  loadMessages(targetPage)
}

const removeMineMessage = async (id) => {
  try {
    await deleteOwnMessage(id)
    await Promise.all([loadMessages(page.value.page), loadMineMessages()])
  } catch (err) {
    submitError.value = err?.message || '删除留言失败'
  }
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
  if (status === 'PENDING') return 'pending'
  if (status === 'PUBLISHED') return 'published'
  if (status === 'REJECTED') return 'rejected'
  if (status === 'DELETED') return 'deleted'
  return ''
}

onMounted(async () => {
  await loadMessages(1)
  await loadMineMessages()
})
</script>

<style scoped>
.message-board-page {
  height: calc(100vh - 76px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.message-board-shell {
  flex: 1;
  min-height: 0;
  max-width: 1100px;
  margin: 0 auto;
  padding: 16px 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
  box-sizing: border-box;
}

.message-board-header {
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 10px 28px rgba(120, 138, 180, 0.1);
  padding: 18px 24px 14px;
  flex-shrink: 0;
}

.header-accent {
  display: flex;
  align-items: baseline;
  gap: 14px;
}

.message-board-kicker {
  color: #4a86e8;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.message-board-header h1 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.01em;
}

.header-desc {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 0.84rem;
  line-height: 1.5;
}

.message-board-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 14px;
}

.grid-col-right {
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.card-panel {
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 10px 28px rgba(120, 138, 180, 0.1);
  padding: 18px 22px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-public {
  height: 100%;
}

.panel-form {
  flex-shrink: 0;
}

.panel-mine {
  flex: 1;
  min-height: 0;
}

.panel-login {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
  font-size: 0.88rem;
  padding: 28px 20px;
}

.panel-login i {
  font-size: 1rem;
  color: #94a3b8;
}

.panel-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.panel-scroll::-webkit-scrollbar {
  width: 5px;
}

.panel-scroll::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 10px;
}

.panel-scroll::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.25);
  border-radius: 10px;
}

.panel-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.4);
}

.panel-footer {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.5);
}

.panel-label {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 12px;
  font-size: 0.88rem;
  font-weight: 600;
  color: #334155;
  flex-shrink: 0;
}

.panel-label i {
  font-size: 0.92rem;
  color: #4a86e8;
}

.message-input {
  width: 100%;
  min-height: 80px;
  padding: 10px 12px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.6);
  color: #334155;
  font-size: 0.88rem;
  line-height: 1.55;
  resize: none;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
  box-sizing: border-box;
  flex-shrink: 0;
}

.message-input::placeholder {
  color: #94a3b8;
}

.message-input:focus {
  outline: none;
  border-color: rgba(74, 134, 232, 0.4);
  box-shadow: 0 0 0 3px rgba(74, 134, 232, 0.08);
  background: #fff;
}

.message-form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  flex-shrink: 0;
}

.message-count {
  font-size: 0.74rem;
  color: #94a3b8;
  font-weight: 500;
  letter-spacing: 0.02em;
  transition: color 0.2s ease;
}

.message-count.warn {
  color: #f59e0b;
}

.submit-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: none;
  border-radius: 18px;
  padding: 7px 18px;
  background: linear-gradient(135deg, #4a86e8, #6fa3f5);
  color: #fff;
  font-weight: 600;
  font-size: 0.82rem;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 14px rgba(74, 134, 232, 0.25);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(74, 134, 232, 0.3);
  filter: brightness(1.05);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn i {
  font-size: 0.8rem;
}

.spin {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.message-hint {
  margin: 6px 0 0;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(34, 197, 94, 0.08);
  color: #15803d;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
}

.message-error-inline {
  margin: 6px 0 0;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
}

.message-block-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.block-title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.message-block-header h2 {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.message-block-header h2 i {
  font-size: 0.94rem;
  color: #4a86e8;
}

.count-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 9px;
  border-radius: 999px;
  background: rgba(74, 134, 232, 0.08);
  color: #4a86e8;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 14px;
  padding: 3px 10px;
  background: rgba(248, 250, 252, 0.6);
  color: #475569;
  font-size: 0.74rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(248, 250, 252, 0.9);
  border-color: rgba(148, 163, 184, 0.35);
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.refresh-btn i {
  font-size: 0.72rem;
}

.message-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28px 12px;
  color: #94a3b8;
  font-size: 0.84rem;
  gap: 8px;
}

.message-state i {
  font-size: 1.6rem;
  color: #cbd5e1;
}

.state-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(74, 134, 232, 0.14);
  border-top-color: #4a86e8;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.message-error {
  flex-direction: row;
  gap: 6px;
  color: #b91c1c;
}

.message-error i {
  color: #b91c1c;
  font-size: 0.9rem;
}

.message-list {
  display: flex;
  flex-direction: column;
}

.message-board-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}

.message-board-item:last-child {
  border-bottom: none;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(74, 134, 232, 0.14), rgba(168, 237, 234, 0.2));
  color: #4a86e8;
  font-size: 0.76rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1.5px solid rgba(74, 134, 232, 0.16);
}

.mine-avatar {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.14), rgba(250, 204, 21, 0.18));
  color: #b45309;
  border-color: rgba(251, 191, 36, 0.2);
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-board-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 3px;
}

.message-board-meta strong {
  font-size: 0.84rem;
  font-weight: 600;
  color: #334155;
}

.meta-time {
  font-size: 0.7rem;
  color: #94a3b8;
}

.message-board-content {
  margin: 0;
  color: #475569;
  font-size: 0.86rem;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.mine-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 0.68rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.status-tag.pending {
  background: rgba(250, 204, 21, 0.12);
  color: #a16207;
}

.status-tag.published {
  background: rgba(34, 197, 94, 0.1);
  color: #15803d;
}

.status-tag.rejected,
.status-tag.deleted {
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
}

.delete-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  border: 1px solid rgba(239, 68, 68, 0.15);
  border-radius: 12px;
  padding: 2px 8px;
  background: rgba(239, 68, 68, 0.05);
  color: #b91c1c;
  font-size: 0.68rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.28);
}

.delete-btn i {
  font-size: 0.66rem;
}

.page-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 14px;
  padding: 4px 10px;
  background: rgba(248, 250, 252, 0.6);
  color: #475569;
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: #fff;
  border-color: rgba(74, 134, 232, 0.3);
  color: #4a86e8;
}

.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.page-btn i {
  font-size: 0.72rem;
}

.page-indicator {
  font-size: 0.78rem;
  color: #94a3b8;
  font-weight: 500;
}

@media (max-width: 900px) {
  .message-board-grid {
    grid-template-columns: 1fr;
  }

  .panel-public {
    height: 50vh;
  }

  .message-board-shell {
    padding: 12px 12px 20px;
  }

  .header-accent {
    flex-direction: column;
    gap: 2px;
  }
}

@media (max-width: 600px) {
  .message-board-header {
    padding: 14px 14px 12px;
  }

  .card-panel {
    padding: 14px;
  }

  .message-board-header h1 {
    font-size: 1.1rem;
  }

  .message-avatar {
    width: 28px;
    height: 28px;
    font-size: 0.7rem;
  }

  .message-board-item {
    gap: 8px;
  }

  .mine-actions {
    flex-wrap: wrap;
  }
}
</style>