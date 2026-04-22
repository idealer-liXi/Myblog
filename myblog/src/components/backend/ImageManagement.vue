<template>
  <div class="image-management">
    <div class="panel-shell">
      <div class="list-header">
        <div class="title-block">
          <span class="section-kicker">Media Archive</span>
          <h2 class="list-title">图片管理</h2>
          <p class="list-subtitle">像胶片联系表一样整理素材，快速检索、预览并复制引用。</p>
        </div>

        <div class="header-actions">
          <div class="header-stat">
            <span class="stat-label">已归档</span>
            <strong>{{ images.length }}</strong>
            <span class="stat-hint">张图片</span>
          </div>

          <button class="btn-upload" @click="triggerUpload" :disabled="uploading">
            <i class="bi" :class="uploading ? 'bi-arrow-repeat spin' : 'bi-cloud-upload'"></i>
            {{ uploading ? '上传中...' : '上传图片' }}
          </button>
          <input type="file" ref="fileInput" class="hidden-input" accept="image/*" multiple @change="handleFileSelect" />
        </div>
      </div>

      <div class="filter-bar">
        <div class="search-box">
          <i class="bi bi-search"></i>
          <input type="text" v-model="searchKeyword" placeholder="搜索文件名 / 后缀 ..." class="search-input" />
        </div>
        <div class="results-chip">
          <span>当前筛选</span>
          <strong>{{ filteredImages.length }}</strong>
          <em>/ {{ images.length }}</em>
        </div>
      </div>

      <div v-if="loading" class="loading-state panel-state">
        <div class="loading-spinner"></div>
        <span>正在加载图片...</span>
      </div>

      <div v-else-if="error" class="error-state panel-state">
        <i class="bi bi-exclamation-circle"></i>
        <span>{{ error }}</span>
        <button class="btn-retry" @click="loadImages">重试</button>
      </div>

      <div v-else-if="filteredImages.length === 0" class="empty-state panel-state">
        <i class="bi bi-image"></i>
        <p>暂无图片</p>
        <p class="empty-hint">点击上方“上传图片”按钮添加素材，建立你的媒体档案。</p>
      </div>

      <div v-else class="image-grid">
        <div
          v-for="(img, index) in filteredImages"
          :key="img.id"
          class="image-card"
          @click="openPreview(img)"
        >
          <div class="image-card-chrome">
            <span class="image-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="image-format">{{ getFileExtension(img.name) }}</span>
          </div>
          <div class="image-thumb">
            <img :src="img.url" :alt="img.name" loading="lazy" />
          </div>
          <div class="image-info">
            <span class="image-name" :title="img.name">{{ img.name }}</span>
            <span class="image-meta">{{ img.size }} · {{ formatDate(img.uploadedAt) }}</span>
            <span class="image-action-hint">点击查看详情与引用</span>
          </div>
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
            <span class="section-kicker preview-kicker">Asset Detail</span>
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
            <div class="preview-actions preview-actions-stack">
              <button class="btn-delete" @click="confirmDelete(previewImage)">
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

const getFileExtension = (fileName) => {
  const extension = fileName.split('.').pop()
  return extension ? extension.toUpperCase() : 'IMG'
}

onMounted(() => {
  loadImages()
})
</script>

<style scoped>
.image-management {
  --bg: #121212;
  --bg-soft: #1a1a1a;
  --panel: rgba(20, 20, 20, 0.86);
  --panel-strong: rgba(9, 9, 9, 0.96);
  --line: rgba(255, 244, 214, 0.12);
  --line-strong: rgba(255, 244, 214, 0.22);
  --text: #f7f1e3;
  --muted: rgba(247, 241, 227, 0.64);
  --dim: rgba(247, 241, 227, 0.42);
  --accent: #c58d2d;
  --accent-soft: rgba(197, 141, 45, 0.16);
  --danger: #ff6b57;
  --danger-soft: rgba(255, 107, 87, 0.14);
  padding: 8px;
  color: var(--text);
  font-family: 'Palatino Linotype', 'Book Antiqua', Georgia, serif;
}

.panel-shell {
  position: relative;
  overflow: hidden;
  padding: 28px;
  border-radius: 28px;
  border: 1px solid var(--line);
  background:
    radial-gradient(circle at top right, rgba(197, 141, 45, 0.16), transparent 28%),
    radial-gradient(circle at left center, rgba(255, 255, 255, 0.05), transparent 20%),
    linear-gradient(145deg, rgba(255, 248, 232, 0.04), rgba(0, 0, 0, 0.12)),
    var(--panel);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 24px 60px rgba(0, 0, 0, 0.28);
}

.panel-shell::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(transparent 0, rgba(255, 255, 255, 0.02) 50%, transparent 100%),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.015) 0 1px, transparent 1px 12px);
  opacity: 0.6;
}

.panel-shell::after {
  content: '';
  position: absolute;
  inset: 14px;
  border-radius: 20px;
  border: 1px solid rgba(255, 244, 214, 0.06);
  pointer-events: none;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 22px;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.title-block {
  max-width: 560px;
}

.section-kicker {
  display: inline-block;
  margin-bottom: 10px;
  color: var(--accent);
  font-size: 0.72rem;
  letter-spacing: 0.32em;
  text-transform: uppercase;
}

.list-subtitle {
  margin: 10px 0 0;
  max-width: 44ch;
  color: var(--muted);
  font-size: 0.95rem;
  line-height: 1.65;
}

.list-title {
  font-size: clamp(2rem, 4vw, 2.8rem);
  font-weight: 700;
  color: var(--text);
  margin: 0;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-stat {
  min-width: 118px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid var(--line);
  background: rgba(255, 248, 232, 0.035);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.header-stat strong {
  display: block;
  margin: 2px 0;
  color: var(--text);
  font-size: 1.7rem;
  line-height: 1;
}

.stat-label,
.stat-hint {
  display: block;
  color: var(--dim);
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.btn-upload {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 54px;
  padding: 0 22px;
  background: linear-gradient(135deg, #d7a347, #b27718);
  color: #1a1206;
  border: 1px solid rgba(255, 236, 196, 0.35);
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.88rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease;
  box-shadow: 0 16px 34px rgba(138, 90, 15, 0.34);
  white-space: nowrap;
}

.btn-upload:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(138, 90, 15, 0.42);
  filter: saturate(1.08);
}

.btn-upload:disabled {
  opacity: 0.55;
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
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.search-box {
  flex: 1;
  position: relative;
}

.search-box i {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--dim);
  font-size: 0.9rem;
}

.search-input {
  width: 100%;
  min-height: 56px;
  padding: 12px 18px 12px 44px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 248, 232, 0.03);
  color: var(--text);
  font-size: 0.94rem;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.search-input::placeholder {
  color: var(--dim);
}

.search-input:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(197, 141, 45, 0.08);
  background: rgba(255, 248, 232, 0.05);
}

.results-chip {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  padding: 0 18px;
  min-height: 56px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 248, 232, 0.035);
  color: var(--muted);
  white-space: nowrap;
}

.results-chip span,
.results-chip em {
  font-size: 0.78rem;
  font-style: normal;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.results-chip strong {
  font-size: 1.4rem;
  color: var(--text);
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
  position: relative;
  z-index: 1;
}

.image-card {
  background: linear-gradient(180deg, rgba(255, 248, 232, 0.05), rgba(255, 248, 232, 0.015));
  border: 1px solid var(--line);
  border-radius: 22px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.2);
  position: relative;
}

.image-card::after {
  content: '';
  position: absolute;
  inset: auto 14px 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 244, 214, 0.28), transparent);
}

.image-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 24px 54px rgba(0, 0, 0, 0.28);
  border-color: var(--line-strong);
}

.image-card-chrome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px 0;
  color: var(--dim);
  font-size: 0.72rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.image-index,
.image-format {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
}

.image-format {
  padding: 0 9px;
  border-radius: 999px;
  border: 1px solid rgba(255, 244, 214, 0.1);
  background: rgba(255, 248, 232, 0.04);
}

.image-thumb {
  margin: 12px 14px 0;
  height: 178px;
  overflow: hidden;
  border-radius: 16px;
  background: linear-gradient(135deg, #26211a, #151515);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.image-thumb::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.28), transparent 50%);
  pointer-events: none;
}

.image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease, filter 0.45s ease;
}

.image-card:hover .image-thumb img {
  transform: scale(1.06);
  filter: saturate(1.05) contrast(1.02);
}

.image-info {
  padding: 16px 16px 18px;
}

.image-name {
  display: block;
  font-size: 0.96rem;
  font-weight: 700;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  display: block;
  font-size: 0.76rem;
  color: var(--muted);
  margin-top: 6px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.image-action-hint {
  display: inline-block;
  margin-top: 14px;
  color: var(--accent);
  font-size: 0.74rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.panel-state {
  min-height: 320px;
  border-radius: 24px;
  border: 1px dashed var(--line);
  background: rgba(255, 248, 232, 0.025);
}

.loading-state, .error-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--muted);
  gap: 8px;
  position: relative;
  z-index: 1;
}

.loading-spinner {
  width: 34px;
  height: 34px;
  border: 3px solid rgba(197, 141, 45, 0.14);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.error-state {
  color: #ff8f7d;
}

.error-state i {
  font-size: 1.8rem;
}

.btn-retry {
  margin-top: 12px;
  padding: 8px 18px;
  border: 1px solid rgba(255, 143, 125, 0.34);
  border-radius: 999px;
  background: rgba(255, 143, 125, 0.08);
  color: #ffb1a5;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s ease;
}

.btn-retry:hover {
  background: rgba(255, 143, 125, 0.16);
  color: #fff0ec;
}

.empty-state i {
  font-size: 3.4rem;
  color: rgba(247, 241, 227, 0.2);
}

.empty-hint {
  font-size: 0.88rem;
  color: var(--dim);
  margin: 0;
  max-width: 28ch;
  text-align: center;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(5, 5, 5, 0.72);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 24px;
}

.preview-modal {
  background: linear-gradient(180deg, rgba(20, 20, 20, 0.98), rgba(12, 12, 12, 0.98));
  color: var(--text);
  border: 1px solid var(--line);
  border-radius: 28px;
  width: 90vw;
  max-width: 1040px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.42);
  position: relative;
  display: flex;
  flex-direction: column;
}

.preview-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid var(--line);
  background: rgba(255, 248, 232, 0.06);
  color: var(--text);
  font-size: 1.1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
}

.preview-close:hover {
  background: rgba(255, 248, 232, 0.12);
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
  background:
    radial-gradient(circle at center, rgba(197, 141, 45, 0.08), transparent 34%),
    linear-gradient(135deg, #1f1b16, #0f0f0f);
  padding: 32px;
  min-height: 300px;
  max-height: 70vh;
  overflow: auto;
}

.preview-img {
  max-width: 100%;
  max-height: 65vh;
  object-fit: contain;
  border-radius: 16px;
  box-shadow: 0 22px 56px rgba(0, 0, 0, 0.34);
}

.preview-sidebar {
  width: 310px;
  flex-shrink: 0;
  padding: 30px 24px;
  border-left: 1px solid var(--line);
  overflow-y: auto;
}

.preview-kicker {
  margin-bottom: 12px;
}

.preview-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 20px 0;
  letter-spacing: 0.04em;
}

.preview-detail {
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(255, 244, 214, 0.08);
}

.detail-label {
  display: block;
  font-size: 0.72rem;
  color: var(--dim);
  margin-bottom: 5px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.detail-value {
  display: block;
  font-size: 0.92rem;
  color: var(--text);
  word-break: break-all;
  line-height: 1.6;
}

.url-value {
  font-size: 0.78rem;
  color: #ffd89b;
  background: rgba(197, 141, 45, 0.1);
  padding: 10px 12px;
  border-radius: 12px;
  line-height: 1.4;
  border: 1px solid rgba(197, 141, 45, 0.16);
}

.preview-actions {
  display: flex;
  gap: 8px;
  margin-top: 18px;
}

.preview-actions-stack {
  margin-top: 8px;
}

.btn-copy {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 0.82rem;
  font-weight: 600;
  border: 1px solid rgba(197, 141, 45, 0.18);
  background: rgba(197, 141, 45, 0.08);
  color: #ffd89b;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  justify-content: center;
}

.btn-copy:hover {
  background: rgba(197, 141, 45, 0.16);
  border-color: rgba(197, 141, 45, 0.3);
  color: #fff4d6;
}

.btn-copy-md {
  background: rgba(255, 248, 232, 0.06);
  color: var(--text);
  border-color: rgba(255, 244, 214, 0.12);
}

.btn-copy-md:hover {
  background: rgba(255, 248, 232, 0.12);
  border-color: rgba(255, 244, 214, 0.24);
  color: #fff8ea;
}

.btn-delete {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 0.82rem;
  font-weight: 600;
  border: 1px solid rgba(255, 107, 87, 0.2);
  background: rgba(255, 107, 87, 0.08);
  color: #ff9d90;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-delete:hover {
  background: rgba(255, 107, 87, 0.16);
  color: #fff1ee;
}

.modal-content {
  background: linear-gradient(180deg, rgba(24, 24, 24, 0.98), rgba(13, 13, 13, 0.98));
  border: 1px solid var(--line);
  color: var(--text);
  border-radius: 24px;
  padding: 28px;
  width: 420px;
  max-width: 90vw;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.34);
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.modal-icon {
  font-size: 1.5rem;
  color: var(--accent);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
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
  border-radius: 999px;
  background: rgba(255, 248, 232, 0.03);
  color: var(--muted);
  cursor: pointer;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: rgba(255, 248, 232, 0.08);
}

.btn-confirm-delete {
  padding: 8px 20px;
  border: 1px solid rgba(255, 107, 87, 0.16);
  border-radius: 999px;
  background: linear-gradient(135deg, #c54a3d, #8f241d);
  color: #fff2ef;
  cursor: pointer;
  font-size: 0.88rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-confirm-delete:hover {
  filter: brightness(1.06);
}

@media (max-width: 768px) {
  .panel-shell {
    padding: 20px;
    border-radius: 22px;
  }

  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions,
  .filter-bar {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .header-stat {
    width: 100%;
  }

  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }

  .image-thumb {
    height: 132px;
  }

  .preview-body {
    flex-direction: column;
  }

  .preview-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid var(--line);
  }

  .preview-actions {
    flex-direction: column;
  }

  .preview-image-wrapper {
    padding: 20px;
  }

  .preview-modal,
  .modal-content {
    border-radius: 20px;
  }
}
.image-management {
  --bg: #f4f6fb;
  --bg-soft: #f8faff;
  --panel: rgba(255, 255, 255, 0.78);
  --panel-strong: rgba(255, 255, 255, 0.94);
  --line: rgba(128, 145, 184, 0.16);
  --line-strong: rgba(87, 116, 184, 0.26);
  --text: #25324d;
  --muted: #6f7b93;
  --dim: #97a2b7;
  --accent: #5378d6;
  --accent-soft: rgba(83, 120, 214, 0.1);
  --danger: #d46b6b;
  --danger-soft: rgba(212, 107, 107, 0.1);
  font-family: 'Avenir Next', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.panel-shell {
  background:
    radial-gradient(circle at top right, rgba(129, 164, 234, 0.14), transparent 26%),
    linear-gradient(180deg, var(--panel-strong), var(--panel));
  box-shadow: 0 18px 36px rgba(120, 136, 170, 0.12);
}

.section-kicker,
.image-action-hint,
.preview-kicker {
  color: var(--accent);
}

.list-subtitle,
.stat-label,
.stat-hint,
.image-meta,
.detail-label,
.results-chip,
.empty-hint {
  color: var(--muted);
}

.list-title,
.header-stat strong,
.image-name,
.preview-title,
.detail-value,
.preview-close,
.search-input,
.results-chip strong {
  color: var(--text);
}

.header-stat,
.results-chip,
.search-input,
.image-card,
.preview-close,
.url-value,
.btn-copy,
.btn-copy-md,
.btn-delete,
.modal-content,
.btn-cancel {
  border-color: var(--line);
}

.btn-upload {
  background: var(--accent);
  color: white;
  border-color: rgba(83, 120, 214, 0.16);
  box-shadow: 0 12px 24px rgba(95, 122, 183, 0.2);
}

.btn-upload:hover:not(:disabled) {
  background: #406bcf;
}

.search-input,
.results-chip,
.header-stat,
.image-card,
.panel-state,
.preview-close,
.preview-modal,
.modal-content,
.btn-cancel {
  background: rgba(255, 255, 255, 0.8);
}

.image-thumb,
.preview-image-wrapper {
  background: linear-gradient(180deg, #f7f9fd, #eef2f8);
}

.image-format,
.url-value,
.btn-copy {
  background: var(--accent-soft);
  color: var(--accent);
}

.btn-copy-md {
  background: rgba(247, 249, 253, 0.92);
  color: var(--text);
}

.btn-delete,
.btn-retry,
.btn-confirm-delete {
  background: var(--danger-soft);
  color: var(--danger);
}

.modal-overlay {
  background: rgba(226, 231, 240, 0.44);
}

.preview-modal,
.modal-content {
  color: var(--text);
  box-shadow: 0 24px 52px rgba(120, 136, 170, 0.18);
}

.preview-sidebar {
  border-left-color: var(--line);
}
</style>
