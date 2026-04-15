<template>
  <div class="image-management">
    <div class="list-header">
      <h2 class="list-title">图片管理</h2>
      <button class="btn-upload" @click="triggerUpload" :disabled="uploading">
        <i class="bi" :class="uploading ? 'bi-arrow-repeat spin' : 'bi-cloud-upload'"></i>
        {{ uploading ? '上传中...' : '上传图片' }}
      </button>
      <input type="file" ref="fileInput" class="hidden-input" accept="image/*" multiple @change="handleFileSelect" />
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <i class="bi bi-search"></i>
        <input type="text" v-model="searchKeyword" placeholder="搜索文件名..." class="search-input" />
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>正在加载图片...</span>
    </div>

    <div v-else-if="error" class="error-state">
      <i class="bi bi-exclamation-circle"></i>
      <span>{{ error }}</span>
      <button class="btn-retry" @click="loadImages">重试</button>
    </div>

    <div v-else-if="filteredImages.length === 0" class="empty-state">
      <i class="bi bi-image"></i>
      <p>暂无图片</p>
      <p class="empty-hint">点击上方"上传图片"按钮添加图片</p>
    </div>

    <div v-else class="image-grid">
      <div
        v-for="img in filteredImages"
        :key="img.id"
        class="image-card"
        @click="openPreview(img)"
      >
        <div class="image-thumb">
          <img :src="img.url" :alt="img.name" loading="lazy" />
        </div>
        <div class="image-info">
          <span class="image-name" :title="img.name">{{ img.name }}</span>
          <span class="image-meta">{{ img.size }} · {{ formatDate(img.uploadedAt) }}</span>
        </div>
      </div>
    </div>

    <div v-if="previewImage" class="modal-overlay" @click.self="closePreview">
      <div class="preview-modal">
        <button class="preview-close" @click="closePreview">
          <i class="bi bi-x-lg"></i>
        </button>
        <div class="preview-body">
          <div class="preview-image-wrapper">
            <img :src="previewImage.url" :alt="previewImage.name" class="preview-img" />
          </div>
          <div class="preview-sidebar">
            <h3 class="preview-title">图片详情</h3>
            <div class="preview-detail">
              <span class="detail-label">文件名</span>
              <span class="detail-value">{{ previewImage.name }}</span>
            </div>
            <div class="preview-detail">
              <span class="detail-label">大小</span>
              <span class="detail-value">{{ previewImage.size }}</span>
            </div>
            <div class="preview-detail">
              <span class="detail-label">上传时间</span>
              <span class="detail-value">{{ formatDate(previewImage.uploadedAt) }}</span>
            </div>
            <div class="preview-detail">
              <span class="detail-label">URL</span>
              <span class="detail-value url-value">{{ previewImage.url }}</span>
            </div>
            <div class="preview-actions">
              <button class="btn-copy" @click="copyToClipboard(previewImage.url)">
                <i class="bi bi-clipboard"></i>
                {{ copiedUrl ? '已复制' : '复制 URL' }}
              </button>
              <button class="btn-copy btn-copy-md" @click="copyToClipboard(`![${previewImage.name}](${previewImage.url})`)">
                <i class="bi bi-markdown"></i>
                {{ copiedMd ? '已复制' : '复制 Markdown' }}
              </button>
            </div>
            <div class="preview-actions" style="margin-top: 8px;">
              <button class="btn-action btn-delete" @click="confirmDelete(previewImage)">
                <i class="bi bi-trash3"></i>
                删除图片
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <i class="bi bi-exclamation-triangle modal-icon"></i>
          <h3>确认删除</h3>
        </div>
        <p class="modal-body">确定要删除图片「{{ deleteTarget?.name }}」吗？文章中引用的图片链接将失效。</p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDeleteModal = false">取消</button>
          <button class="btn-confirm-delete" @click="handleDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getImages, deleteImage, uploadImage } from '@/services/uploadService.js'

const images = ref([])
const loading = ref(false)
const error = ref(null)
const uploading = ref(false)
const searchKeyword = ref('')
const previewImage = ref(null)
const showDeleteModal = ref(false)
const deleteTarget = ref(null)
const copiedUrl = ref(false)
const copiedMd = ref(false)
const fileInput = ref(null)

const filteredImages = computed(() => {
  if (!searchKeyword.value) return images.value
  const kw = searchKeyword.value.toLowerCase()
  return images.value.filter(img => img.name.toLowerCase().includes(kw))
})

const loadImages = async () => {
  loading.value = true
  error.value = null
  try {
    images.value = await getImages()
  } catch {
    error.value = '获取图片列表失败'
    images.value = []
  } finally {
    loading.value = false
  }
}

const triggerUpload = () => {
  fileInput.value.click()
}

const handleFileSelect = async (event) => {
  const files = Array.from(event.target.files)
  if (!files.length) return

  uploading.value = true
  let successCount = 0
  for (const file of files) {
    try {
      const url = await uploadImage(file)
      images.value.unshift({
        id: Date.now() + Math.random(),
        name: file.name,
        url,
        size: formatFileSize(file.size),
        uploadedAt: new Date().toISOString()
      })
      successCount++
    } catch {
      // individual file failure, continue
    }
  }
  uploading.value = false
  event.target.value = ''
  if (successCount === 0) {
    error.value = '上传失败，请稍后重试'
  }
}

const openPreview = (img) => {
  previewImage.value = img
  copiedUrl.value = false
  copiedMd.value = false
}

const closePreview = () => {
  previewImage.value = null
}

const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    if (text.startsWith('![')) {
      copiedMd.value = true
      setTimeout(() => { copiedMd.value = false }, 2000)
    } else {
      copiedUrl.value = true
      setTimeout(() => { copiedUrl.value = false }, 2000)
    }
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    if (text.startsWith('![')) {
      copiedMd.value = true
      setTimeout(() => { copiedMd.value = false }, 2000)
    } else {
      copiedUrl.value = true
      setTimeout(() => { copiedUrl.value = false }, 2000)
    }
  }
}

const confirmDelete = (img) => {
  deleteTarget.value = img
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await deleteImage(deleteTarget.value.id)
    images.value = images.value.filter(i => i.id !== deleteTarget.value.id)
    if (previewImage.value && previewImage.value.id === deleteTarget.value.id) {
      previewImage.value = null
    }
  } catch {
    // error handled
  }
  showDeleteModal.value = false
  deleteTarget.value = null
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
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
  loadImages()
})
</script>

<style scoped>
.image-management {
  padding: 4px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 12px;
}

.list-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.btn-upload {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 500;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.25);
  white-space: nowrap;
}

.btn-upload:hover:not(:disabled) {
  background: #0062cc;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(0, 123, 255, 0.35);
}

.btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spin {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.hidden-input {
  display: none;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-box {
  flex: 1;
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

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.image-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.image-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: rgba(0, 123, 255, 0.2);
}

.image-thumb {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-card:hover .image-thumb img {
  transform: scale(1.05);
}

.image-info {
  padding: 10px 12px;
}

.image-name {
  display: block;
  font-size: 0.85rem;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  display: block;
  font-size: 0.75rem;
  color: #999;
  margin-top: 2px;
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

.error-state {
  color: #dc3545;
}

.error-state i {
  font-size: 1.5rem;
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

.empty-hint {
  font-size: 0.85rem;
  color: #bbb;
  margin: 0;
}

/* Preview Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.preview-modal {
  background: white;
  border-radius: 16px;
  width: 90vw;
  max-width: 900px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  position: relative;
  display: flex;
  flex-direction: column;
}

.preview-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  color: #666;
  font-size: 1.1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
}

.preview-close:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #333;
}

.preview-body {
  display: flex;
  overflow: hidden;
}

.preview-image-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  padding: 20px;
  min-height: 300px;
  max-height: 70vh;
  overflow: auto;
}

.preview-img {
  max-width: 100%;
  max-height: 65vh;
  object-fit: contain;
  border-radius: 8px;
}

.preview-sidebar {
  width: 260px;
  flex-shrink: 0;
  padding: 24px 20px;
  border-left: 1px solid #eee;
  overflow-y: auto;
}

.preview-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.preview-detail {
  margin-bottom: 12px;
}

.detail-label {
  display: block;
  font-size: 0.78rem;
  color: #999;
  margin-bottom: 2px;
}

.detail-value {
  display: block;
  font-size: 0.88rem;
  color: #333;
  word-break: break-all;
}

.url-value {
  font-size: 0.78rem;
  color: #007bff;
  background: rgba(0, 123, 255, 0.06);
  padding: 6px 8px;
  border-radius: 6px;
  line-height: 1.4;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.btn-copy {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 500;
  border: 1px solid rgba(0, 123, 255, 0.2);
  background: rgba(0, 123, 255, 0.08);
  color: #007bff;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  justify-content: center;
}

.btn-copy:hover {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.btn-delete {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 500;
  border: 1px solid rgba(220, 53, 69, 0.2);
  background: rgba(220, 53, 69, 0.08);
  color: #dc3545;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-delete:hover {
  background: #dc3545;
  color: white;
}

/* Delete Confirm Modal */
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

  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 10px;
  }

  .image-thumb {
    height: 100px;
  }

  .preview-body {
    flex-direction: column;
  }

  .preview-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid #eee;
  }
}
</style>