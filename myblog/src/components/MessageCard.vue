<template>
  <div class="message-card">
    <div class="message-header">
      <i class="bi bi-chat-left-text-fill"></i>
      <span>留言板</span>
    </div>
    <div class="message-list">
      <div class="message-item" v-for="message in messages" :key="message.id">
        <div class="message-author">{{ message.author }}</div>
        <div class="message-content">{{ message.content }}</div>
        <div class="message-time">{{ message.time }}</div>
      </div>
    </div>
    <div class="message-form">
      <textarea v-model="newMessage" placeholder="留下你的足迹..." class="message-input"></textarea>
      <button @click="addMessage" class="submit-btn">发表</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const messages = ref([
  { id: 1, author: 'Alice', content: '这个博客真棒！', time: '2024-07-20' },
  { id: 2, author: 'Bob', content: '学到了很多东西，感谢博主！', time: '2024-07-21' },
]);

const newMessage = ref('');

const addMessage = () => {
  if (newMessage.value.trim()) {
    messages.value.push({
      id: Date.now(),
      author: 'Guest', // In a real app, this would be the logged-in user
      content: newMessage.value,
      time: new Date().toLocaleDateString(),
    });
    newMessage.value = '';
  }
};
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
  max-height: 120px; /* Reduced height */
  overflow-y: auto;
  margin-bottom: 10px;
  padding-right: 5px; /* For scrollbar */
}

/* Simple scrollbar styling */
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

.message-form {
  display: flex;
  flex-direction: column;
}

.message-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  resize: vertical;
  min-height: 40px; /* Reduced height */
  margin-bottom: 8px;
  font-size: 0.85rem;
}

.submit-btn {
  align-self: flex-end;
  background: #007bff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: opacity 0.3s;
  font-size: 0.85rem;
}

.submit-btn:hover {
  opacity: 0.9;
}
</style>
