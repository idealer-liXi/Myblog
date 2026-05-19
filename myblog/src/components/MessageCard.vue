<template>
  <div class="message-card">
    <div class="message-header">
      <i class="bi bi-chat-left-text-fill"></i>
      <span>留言板</span>
    </div>

    <div v-if="loading" class="message-state">正在加载留言...</div>
    <div v-else-if="error" class="message-state message-error">{{ error }}</div>
    <div v-else-if="messages.length === 0" class="message-state">还没有已发布留言</div>
    <div v-else class="message-list">
      <div class="message-item" v-for="message in messages" :key="message.id">
        <div class="message-author">{{ message.username || '用户' }}</div>
        <div class="message-content">{{ message.content }}</div>
        <div class="message-time">{{ formatDate(message.createdAt) }}</div>
      </div>
    </div>

    <div class="message-actions">
      <router-link :to="{ name: 'message-board' }" class="message-link">去留言板</router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getPublicMessages } from '@/services/messageService.js'

const messages = ref([])
const loading = ref(false)
const error = ref('')

const loadMessages = async () => {
  loading.value = true
  error.value = ''
  try {
    const page = await getPublicMessages({ latestOnly: true, limit: 5, page: 1, pageSize: 5 })
    messages.value = page.items || []
  } catch (err) {
    messages.value = []
    error.value = err?.message || '加载留言失败'
  } finally {
    loading.value = false
  }
}

const formatDate = (value) => {
  if (!value) return ''
  return value.slice(0, 10)
}

onMounted(loadMessages)
</script>

<style scoped>
.message-card {
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.07);
  margin-top: 20px;
}

.message-header {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.message-header i {
  margin-right: 6px;
  color: #007bff;
  font-size: 0.9em;
}

.message-list {
  max-height: 160px;
  overflow-y: auto;
  margin-bottom: 10px;
  padding-right: 5px;
}

.message-list::-webkit-scrollbar {
  width: 5px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 10px;
}

.message-list::-webkit-scrollbar-track {
  background: #f7f7f7;
}

.message-item {
  border-bottom: 1px solid #f5f5f5;
  padding: 8px 0;
}

.message-item:last-child {
  border-bottom: none;
}

.message-author {
  font-weight: 600;
  color: #555;
  margin-bottom: 4px;
  font-size: 0.85rem;
}

.message-content {
  color: #666;
  font-size: 0.8rem;
  word-wrap: break-word;
}

.message-time {
  font-size: 0.7rem;
  color: #aaa;
  text-align: right;
  margin-top: 4px;
}

.message-state {
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 0.82rem;
  text-align: center;
}

.message-error {
  color: #b91c1c;
}

.message-actions {
  display: flex;
  justify-content: flex-end;
}

.message-link {
  text-decoration: none;
  color: #007bff;
  font-size: 0.85rem;
  font-weight: 600;
}

.message-link:hover {
  opacity: 0.85;
}
</style>
