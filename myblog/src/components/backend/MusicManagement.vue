<template>
  <div class="music-management">
    <div class="list-header">
      <h2 class="list-title">音乐管理</h2>
      <button class="btn-create" @click="openCreateModal">
        <i class="bi bi-plus-lg"></i>
        新建音乐
      </button>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <i class="bi bi-search"></i>
        <input v-model="searchKeyword" type="text" class="search-input" placeholder="搜索歌曲名 / 歌手..." />
      </div>
    </div>

    <div v-if="loading" class="empty-state">
      <i class="bi bi-arrow-repeat spin"></i>
      <p>正在加载音乐...</p>
    </div>

    <div v-else-if="musicList.length > 0" class="table-wrapper">
      <table class="music-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>封面</th>
            <th>歌曲名</th>
            <th>歌手</th>
            <th>排序</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="music in filteredMusic" :key="music.id">
            <td>{{ music.id }}</td>
            <td>
              <ImageInitialFallback
                :src="music.coverImage"
                :name="music.name"
                :alt="music.name || '音乐封面'"
                wrapperClass="cover-thumb-frame"
                imageClass="cover-thumb-image"
                fallbackClass="cover-placeholder"
              />
            </td>
            <td>{{ music.name }}</td>
            <td>{{ music.artist }}</td>
            <td>{{ music.sortOrder }}</td>
            <td>
              <span class="status-badge" :class="music.enabled ? 'status-active' : 'status-paused'">
                {{ music.enabled ? '启用' : '停用' }}
              </span>
            </td>
            <td class="col-actions">
              <button class="btn-action btn-edit" @click="openEditModal(music)">
                <i class="bi bi-pencil-square"></i>
                编辑
              </button>
              <button class="btn-action btn-delete" @click="confirmDelete(music)">
                <i class="bi bi-trash3"></i>
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="empty-state">
      <i class="bi bi-disc"></i>
      <p>暂无音乐</p>
      <p class="empty-hint">点击「新建音乐」按钮添加</p>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content modal-large">
        <div class="modal-header">
          <i :class="isEditing ? 'bi bi-pencil-square' : 'bi bi-plus-circle'" class="modal-title-icon"></i>
          <h3>{{ isEditing ? '编辑音乐' : '新建音乐' }}</h3>
        </div>

        <form class="modal-form" @submit.prevent="handleSave">
          <div class="form-row-inline">
            <div class="form-group flex-1">
              <label class="form-label">歌曲名 <span class="required">*</span></label>
              <input v-model="form.name" type="text" class="form-input" :class="{ 'is-error': formErrors.name }" />
              <span v-if="formErrors.name" class="error-tip">{{ formErrors.name }}</span>
            </div>
            <div class="form-group flex-1">
              <label class="form-label">歌手 <span class="required">*</span></label>
              <input v-model="form.artist" type="text" class="form-input" :class="{ 'is-error': formErrors.artist }" />
              <span v-if="formErrors.artist" class="error-tip">{{ formErrors.artist }}</span>
            </div>
          </div>

          <div class="form-row-inline">
            <div class="form-group flex-1">
              <label class="form-label">排序</label>
              <input v-model.number="form.sortOrder" type="number" class="form-input" />
            </div>
            <div class="form-group flex-1 form-checkbox-group checkbox-align-end">
              <label class="form-checkbox-label">
                <input v-model="form.enabled" type="checkbox" class="form-checkbox" />
                <span>启用前台播放</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">音频文件 <span class="required">*</span></label>
            <div class="upload-inline-row">
              <input v-model="form.audioUrl" type="text" class="form-input" placeholder="上传后自动回填音频地址" readonly />
              <button type="button" class="btn-create upload-inline-btn" @click="triggerAudioUpload" :disabled="audioUploading">
                {{ audioUploading ? '上传中...' : '上传音频' }}
              </button>
            </div>
            <input ref="audioInput" type="file" class="hidden-input" accept="audio/*,.mp3,.wav,.ogg,.m4a,.aac" @change="handleAudioFileSelect" />
          </div>

          <div class="form-group">
            <label class="form-label">封面</label>
            <div class="upload-inline-row">
              <input v-model="form.coverImage" type="text" class="form-input" placeholder="上传后自动回填封面地址" readonly />
              <button type="button" class="btn-create upload-inline-btn" @click="triggerCoverUpload" :disabled="coverUploading">
                {{ coverUploading ? '上传中...' : '上传封面' }}
              </button>
            </div>
            <input ref="coverInput" type="file" class="hidden-input" accept="image/*" @change="handleCoverFileSelect" />
            <div class="music-cover-preview" v-if="form.coverImage">
              <ImageInitialFallback
                :src="form.coverImage"
                :name="form.name || '音乐'"
                :alt="form.name || '音乐封面'"
                wrapperClass="music-cover-preview-frame"
                imageClass="music-cover-preview-image"
                fallbackClass="music-cover-preview-fallback"
              />
            </div>
          </div>

          <div v-if="formError" class="submit-error">
            <i class="bi bi-exclamation-circle-fill"></i>
            {{ formError }}
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-submit" :disabled="saving">
              <span v-if="saving" class="btn-spinner"></span>
              {{ isEditing ? '保存修改' : '创建音乐' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <i class="bi bi-exclamation-triangle modal-icon"></i>
          <h3>确认删除</h3>
        </div>
        <p class="modal-body">确定要删除音乐「{{ deleteTarget?.name }}」吗？此操作不可恢复。</p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDeleteModal = false">取消</button>
          <button class="btn-confirm-delete" @click="handleDelete" :disabled="deleting">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import ImageInitialFallback from '@/components/common/ImageInitialFallback.vue'
import {
  createMusic,
  deleteMusic,
  getAdminMusicById,
  getAdminMusicList,
  updateMusic,
  uploadMusicAudio,
  uploadMusicCover
} from '@/services/musicService.js'

const musicList = ref([])
const searchKeyword = ref('')
const loading = ref(false)
const showModal = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const deleting = ref(false)
const formError = ref('')
const showDeleteModal = ref(false)
const deleteTarget = ref(null)
const audioUploading = ref(false)
const coverUploading = ref(false)
const audioInput = ref(null)
const coverInput = ref(null)

const form = ref({
  id: null,
  name: '',
  artist: '',
  audioUrl: '',
  coverImage: '',
  sortOrder: 0,
  enabled: true
})

const formErrors = ref({
  name: '',
  artist: ''
})

const filteredMusic = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return musicList.value
  return musicList.value.filter(item =>
    item.name.toLowerCase().includes(keyword) || item.artist.toLowerCase().includes(keyword)
  )
})

const resetForm = () => {
  form.value = {
    id: null,
    name: '',
    artist: '',
    audioUrl: '',
    coverImage: '',
    sortOrder: 0,
    enabled: true
  }
  formErrors.value = { name: '', artist: '' }
  formError.value = ''
}

const loadMusic = async () => {
  loading.value = true
  try {
    musicList.value = await getAdminMusicList()
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  resetForm()
  isEditing.value = false
  showModal.value = true
}

const openEditModal = async (music) => {
  resetForm()
  isEditing.value = true
  showModal.value = true
  try {
    form.value = await getAdminMusicById(music.id)
  } catch (error) {
    formError.value = error?.message || '加载音乐详情失败'
  }
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const validateForm = () => {
  formErrors.value = { name: '', artist: '' }
  formError.value = ''

  if (!form.value.name.trim()) {
    formErrors.value.name = '请输入歌曲名'
  }
  if (!form.value.artist.trim()) {
    formErrors.value.artist = '请输入歌手'
  }
  if (!form.value.audioUrl.trim()) {
    formError.value = '请先上传音频文件'
  }

  return !formErrors.value.name && !formErrors.value.artist && form.value.audioUrl.trim()
}

const handleSave = async () => {
  if (!validateForm()) return

  saving.value = true
  formError.value = ''
  try {
    const payload = { ...form.value }
    if (isEditing.value) {
      await updateMusic(form.value.id, payload)
    } else {
      await createMusic(payload)
    }
    await loadMusic()
    closeModal()
  } catch (error) {
    formError.value = error?.message || '保存失败'
  } finally {
    saving.value = false
  }
}

const confirmDelete = (music) => {
  deleteTarget.value = music
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return

  deleting.value = true
  try {
    await deleteMusic(deleteTarget.value.id)
    showDeleteModal.value = false
    deleteTarget.value = null
    await loadMusic()
  } finally {
    deleting.value = false
  }
}

const triggerAudioUpload = () => {
  audioInput.value?.click()
}

const triggerCoverUpload = () => {
  coverInput.value?.click()
}

const handleAudioFileSelect = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  audioUploading.value = true
  formError.value = ''
  try {
    form.value.audioUrl = await uploadMusicAudio(file)
  } catch (error) {
    formError.value = error?.message || '上传音频失败'
  } finally {
    audioUploading.value = false
    event.target.value = ''
  }
}

const handleCoverFileSelect = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  coverUploading.value = true
  formError.value = ''
  try {
    form.value.coverImage = await uploadMusicCover(file)
  } catch (error) {
    formError.value = error?.message || '上传封面失败'
  } finally {
    coverUploading.value = false
    event.target.value = ''
  }
}

onMounted(loadMusic)
</script>

<style scoped>
.music-management {
  color: #1f2937;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.list-title {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
}

.btn-create,
.btn-submit,
.btn-confirm-delete {
  border: none;
  border-radius: 12px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #4a86e8, #7c9cff);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 600;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.btn-create:hover,
.btn-submit:hover,
.btn-confirm-delete:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(74, 134, 232, 0.2);
}

.btn-create:disabled,
.btn-submit:disabled,
.btn-confirm-delete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.filter-bar {
  margin-bottom: 18px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.15);
}

.search-input,
.form-input {
  width: 100%;
  border: none;
  background: transparent;
  outline: none;
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.music-table {
  width: 100%;
  border-collapse: collapse;
}

.music-table th,
.music-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
}

.music-table th {
  font-size: 0.84rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.cover-thumb-frame {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  aspect-ratio: 1 / 1;
}

.cover-thumb-frame :deep(.wrapper-root) {
  width: 100%;
  height: 100%;
}

.cover-thumb-frame :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 14px;
}

.cover-thumb-frame :deep(.image-initial-fallback) {
  border-radius: 14px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
}

.status-active {
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
}

.status-paused {
  background: rgba(148, 163, 184, 0.18);
  color: #475569;
}

.col-actions {
  white-space: nowrap;
}

.btn-action,
.btn-cancel,
.upload-inline-btn {
  border: none;
  border-radius: 10px;
  padding: 8px 12px;
  margin-right: 8px;
  font-weight: 600;
}

.btn-edit {
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
}

.btn-delete,
.btn-confirm-delete {
  background: rgba(239, 68, 68, 0.14);
  color: #b91c1c;
}

.btn-cancel {
  background: rgba(148, 163, 184, 0.15);
  color: #475569;
}

.empty-state {
  padding: 48px 20px;
  text-align: center;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  color: #64748b;
}

.empty-state i {
  font-size: 2rem;
  margin-bottom: 12px;
}

.empty-hint {
  margin-top: 6px;
  font-size: 0.9rem;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 1000;
}

.modal-content {
  width: min(100%, 560px);
  border-radius: 20px;
  background: #fff;
  padding: 24px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
}

.modal-large {
  width: min(100%, 760px);
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.modal-title-icon,
.modal-icon {
  font-size: 1.3rem;
  color: #4a86e8;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row-inline {
  display: flex;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.flex-1 {
  flex: 1;
}

.form-label {
  font-size: 0.92rem;
  font-weight: 600;
}

.required {
  color: #ef4444;
}

.form-input {
  min-height: 44px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  padding: 0 14px;
}

.is-error {
  border-color: #ef4444;
}

.error-tip,
.submit-error {
  color: #b91c1c;
  font-size: 0.82rem;
}

.submit-error {
  display: flex;
  align-items: center;
  gap: 6px;
}

.upload-inline-row {
  display: flex;
  gap: 12px;
}

.upload-inline-btn {
  margin-right: 0;
  white-space: nowrap;
}

.hidden-input {
  display: none;
}

.checkbox-align-end {
  justify-content: flex-end;
}

.form-checkbox-group {
  justify-content: center;
}

.form-checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
}

.music-cover-preview {
  margin-top: 12px;
}

.music-cover-preview-frame {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  aspect-ratio: 1 / 1;
}

.music-cover-preview-frame :deep(.wrapper-root) {
  width: 100%;
  height: 100%;
}

.music-cover-preview-frame :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 18px;
}

.music-cover-preview-frame :deep(.image-initial-fallback) {
  border-radius: 18px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.modal-body {
  margin: 0 0 20px;
  color: #475569;
}

.btn-spinner,
.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .form-row-inline,
  .upload-inline-row,
  .list-header {
    flex-direction: column;
    align-items: stretch;
  }

  .music-table th,
  .music-table td {
    padding: 12px;
  }

  .modal-content,
  .modal-large {
    width: 100%;
  }
}
</style>
