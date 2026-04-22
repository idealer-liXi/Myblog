<template>
  <div class="project-management">
    <div class="list-header">
      <h2 class="list-title">项目管理</h2>
      <button class="btn-create" @click="openCreateModal">
        <i class="bi bi-plus-lg"></i>
        新建项目
      </button>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <i class="bi bi-search"></i>
        <input type="text" v-model="searchKeyword" placeholder="搜索项目名称..." class="search-input" />
      </div>
    </div>

    <div class="table-wrapper" v-if="projects.length > 0">
      <table class="project-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-cover">封面</th>
            <th class="col-name">项目名称</th>
            <th class="col-status">状态</th>
            <th class="col-sort">排序</th>
            <th class="col-date">开始日期</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="proj in filteredProjects" :key="proj.id">
            <td class="col-id">{{ proj.id }}</td>
            <td class="col-cover">
              <div class="cover-thumb" v-if="proj.coverImage">
                <img :src="proj.coverImage" alt="封面" />
              </div>
              <div class="cover-placeholder" v-else>
                <i class="bi bi-image"></i>
              </div>
            </td>
            <td class="col-name">{{ proj.name }}</td>
            <td class="col-status">
              <span class="status-badge" :class="getStatusClass(proj.status)">
                {{ proj.status }}
              </span>
            </td>
            <td class="col-sort">{{ proj.sortOrder }}</td>
            <td class="col-date">{{ proj.startDate || '-' }}</td>
            <td class="col-actions">
              <button class="btn-action btn-edit" @click="openEditModal(proj)">
                <i class="bi bi-pencil-square"></i>
                编辑
              </button>
              <button class="btn-action btn-delete" @click="confirmDelete(proj)">
                <i class="bi bi-trash3"></i>
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="projects.length === 0" class="empty-state">
      <i class="bi bi-folder"></i>
      <p>暂无项目</p>
      <p class="empty-hint">点击「新建项目」按钮添加</p>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content modal-large">
        <div class="modal-header">
          <i :class="isEditing ? 'bi bi-pencil-square' : 'bi bi-plus-circle'" class="modal-title-icon"></i>
          <h3>{{ isEditing ? '编辑项目' : '新建项目' }}</h3>
        </div>
        <form @submit.prevent="handleSave" class="modal-form">
          <div class="form-row-inline">
            <div class="form-group flex-2">
              <label class="form-label">项目名称 <span class="required">*</span></label>
              <input type="text" v-model="form.name" class="form-input" :class="{ 'is-error': formErrors.name }" placeholder="例如：个人博客系统" />
              <span v-if="formErrors.name" class="error-tip">{{ formErrors.name }}</span>
            </div>
            <div class="form-group flex-1">
              <label class="form-label">排序权重</label>
              <input type="number" v-model.number="form.sortOrder" class="form-input" placeholder="0" />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">项目描述</label>
            <textarea v-model="form.description" class="form-input form-textarea" rows="3" placeholder="简要描述项目功能和特点..."></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">技术栈</label>
            <input type="text" v-model="form.techStack" class="form-input" placeholder="例如：Vue 3, Node.js, MongoDB（逗号分隔）" />
          </div>

          <div class="form-row-inline">
            <div class="form-group flex-1">
              <label class="form-label">项目链接</label>
              <input type="url" v-model="form.projectUrl" class="form-input" placeholder="https://..." />
            </div>
            <div class="form-group flex-1">
              <label class="form-label">GitHub 地址</label>
              <input type="url" v-model="form.githubUrl" class="form-input" placeholder="https://github.com/..." />
            </div>
          </div>

          <div class="form-row-inline">
            <div class="form-group flex-1">
              <label class="form-label">预览地址</label>
              <input type="url" v-model="form.previewUrl" class="form-input" placeholder="https://..." />
            </div>
            <div class="form-group flex-1">
              <label class="form-label">封面图片 URL</label>
              <input type="url" v-model="form.coverImage" class="form-input" placeholder="https://..." />
            </div>
          </div>

          <div class="form-row-inline">
            <div class="form-group flex-1">
              <label class="form-label">状态</label>
              <select v-model="form.status" class="form-input form-select">
                <option value="进行中">进行中</option>
                <option value="已完成">已完成</option>
                <option value="暂停">暂停</option>
              </select>
            </div>
            <div class="form-group flex-1">
              <label class="form-label">开始日期</label>
              <input type="date" v-model="form.startDate" class="form-input" />
            </div>
            <div class="form-group flex-1">
              <label class="form-label">结束日期</label>
              <input type="date" v-model="form.endDate" class="form-input" />
            </div>
          </div>

          <div class="form-group form-checkbox-group">
            <label class="form-checkbox-label">
              <input type="checkbox" v-model="form.isPublic" class="form-checkbox" />
              <span>公开显示（在项目页面展示）</span>
            </label>
          </div>

          <div v-if="formError" class="submit-error">
            <i class="bi bi-exclamation-circle-fill"></i>
            {{ formError }}
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-submit" :disabled="saving">
              <span v-if="saving" class="btn-spinner"></span>
              {{ isEditing ? '保存修改' : '创建项目' }}
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
        <p class="modal-body">确定要删除项目「{{ deleteTarget?.name }}」吗？此操作不可恢复。</p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDeleteModal = false">取消</button>
          <button class="btn-confirm-delete" @click="handleDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getProjects, getProjectById, createProject, updateProject, deleteProject } from '@/services/projectService.js'

const projects = ref([])
const searchKeyword = ref('')
const showModal = ref(false)
const showDeleteModal = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const formError = ref('')
const deleteTarget = ref(null)

const form = ref({
  id: null,
  name: '',
  description: '',
  techStack: '',
  projectUrl: '',
  githubUrl: '',
  previewUrl: '',
  coverImage: '',
  status: '进行中',
  sortOrder: 0,
  startDate: '',
  endDate: '',
  isPublic: true
})

const formErrors = ref({
  name: ''
})

const filteredProjects = computed(() => {
  if (!searchKeyword.value) return projects.value
  const kw = searchKeyword.value.toLowerCase()
  return projects.value.filter(p =>
    p.name.toLowerCase().includes(kw)
  )
})

const getStatusClass = (status) => {
  const statusMap = {
    '进行中': 'status-progress',
    '已完成': 'status-completed',
    '暂停': 'status-paused'
  }
  return statusMap[status] || 'status-default'
}

const loadProjects = async () => {
  try {
    projects.value = await getProjects()
  } catch {
    projects.value = []
  }
}

const openCreateModal = () => {
  isEditing.value = false
  form.value = {
    id: null,
    name: '',
    description: '',
    techStack: '',
    projectUrl: '',
    githubUrl: '',
    previewUrl: '',
    coverImage: '',
    status: '进行中',
    sortOrder: 0,
    startDate: '',
    endDate: '',
    isPublic: true
  }
  formErrors.value = { name: '' }
  formError.value = ''
  showModal.value = true
}

const openEditModal = (proj) => {
  isEditing.value = true
  form.value = { ...proj }
  formErrors.value = { name: '' }
  formError.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const validateForm = () => {
  formErrors.value = { name: '' }
  let valid = true
  if (!form.value.name.trim()) {
    formErrors.value.name = '请输入项目名称'
    valid = false
  }
  return valid
}

const handleSave = async () => {
  if (!validateForm()) return
  saving.value = true
  formError.value = ''
  try {
    if (isEditing.value) {
      const idx = projects.value.findIndex(p => p.id === form.value.id)
      if (idx > -1) {
        const updated = await updateProject(form.value.id, { ...form.value })
        projects.value.splice(idx, 1, updated)
      }
    } else {
      const created = await createProject({ ...form.value })
      projects.value.push(created)
    }
    closeModal()
  } catch {
    formError.value = isEditing.value ? '保存失败，请稍后重试' : '创建失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

const confirmDelete = (proj) => {
  deleteTarget.value = proj
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await deleteProject(deleteTarget.value.id)
    projects.value = projects.value.filter(p => p.id !== deleteTarget.value.id)
  } catch {
    // error already handled
  }
  showDeleteModal.value = false
  deleteTarget.value = null
}

loadProjects()
</script>

<style scoped>
.project-management {
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
  border: none;
  border-radius: 10px;
  font-weight: 500;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.25);
}

.btn-create:hover {
  background: #0062cc;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(0, 123, 255, 0.35);
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

.table-wrapper {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.project-table {
  width: 100%;
  border-collapse: collapse;
}

.project-table th {
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 0.82rem;
  color: #888;
  background: rgba(0, 123, 255, 0.04);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.project-table td {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  font-size: 0.9rem;
  color: #555;
}

.project-table tr:last-child td {
  border-bottom: none;
}

.project-table tr:hover td {
  background: rgba(0, 123, 255, 0.03);
}

.col-id { width: 60px; }
.col-cover { width: 70px; }
.col-name { min-width: 150px; }
.col-status { width: 100px; }
.col-sort { width: 80px; }
.col-date { width: 110px; }
.col-actions { width: 200px; white-space: nowrap; }

.cover-thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.cover-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: rgba(0, 123, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #007bff;
  font-size: 1.2rem;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-progress {
  background: rgba(0, 123, 255, 0.12);
  color: #007bff;
}

.status-completed {
  background: rgba(40, 167, 69, 0.12);
  color: #28a745;
}

.status-paused {
  background: rgba(108, 117, 125, 0.12);
  color: #6c757d;
}

.status-default {
  background: rgba(0, 0, 0, 0.08);
  color: #666;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #999;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.empty-state i {
  font-size: 4rem;
  margin-bottom: 16px;
  color: #ddd;
}

.empty-state p {
  margin: 0 0 8px 0;
  font-size: 1.1rem;
  color: #888;
}

.empty-hint {
  font-size: 0.9rem;
  color: #bbb;
  margin: 0;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 8px;
}

.btn-action:last-child {
  margin-right: 0;
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
  width: 460px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modal-large {
  width: 600px;
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.modal-title-icon {
  font-size: 1.3rem;
  color: #007bff;
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

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #555;
}

.required {
  color: #dc3545;
}

.form-input {
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  transition: all 0.25s ease;
}

.form-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
  background: white;
}

.form-input.is-error {
  border-color: #dc3545;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-select {
  cursor: pointer;
}

.form-row-inline {
  display: flex;
  gap: 16px;
}

.form-row-inline .form-group {
  flex: 1;
}

.flex-2 {
  flex: 2 !important;
}

.flex-1 {
  flex: 1 !important;
}

.form-checkbox-group {
  margin-top: 4px;
}

.form-checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #555;
  cursor: pointer;
}

.form-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.error-tip {
  color: #dc3545;
  font-size: 0.8rem;
}

.submit-error {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(220, 53, 69, 0.08);
  border: 1px solid rgba(220, 53, 69, 0.2);
  border-radius: 10px;
  padding: 12px 16px;
  color: #dc3545;
  font-size: 0.88rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #ddd;
  border-radius: 10px;
  background: white;
  color: #666;
  cursor: pointer;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-submit {
  padding: 10px 28px;
  border: none;
  border-radius: 10px;
  background: #007bff;
  color: white;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.25);
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-submit:hover:not(:disabled) {
  background: #0062cc;
  box-shadow: 0 6px 20px rgba(0, 123, 255, 0.35);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
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

  .project-table th,
  .project-table td {
    padding: 10px 8px;
    font-size: 0.82rem;
  }

  .col-id, .col-sort {
    display: none;
  }

  .modal-content {
    padding: 20px;
  }

  .modal-large {
    width: 90vw;
  }

  .form-row-inline {
    flex-direction: column;
    gap: 0;
  }
}
.project-management {
  --surface: rgba(255, 255, 255, 0.76);
  --surface-strong: rgba(255, 255, 255, 0.93);
  --line: rgba(128, 145, 184, 0.16);
  --line-strong: rgba(87, 116, 184, 0.26);
  --text: #25324d;
  --muted: #6f7b93;
  --soft: #97a2b7;
  --accent: #5378d6;
  --danger: #d46b6b;
  --success: #5c9b74;
  font-family: 'Avenir Next', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.list-title,
.form-label,
.modal-header h3,
.project-table td,
.search-input,
.form-input,
.form-checkbox-label {
  color: var(--text);
}

.btn-create,
.table-wrapper,
.empty-state,
.modal-content,
.form-input,
.btn-cancel {
  background: rgba(255, 255, 255, 0.8);
}

.btn-create {
  border: 1px solid rgba(83, 120, 214, 0.18);
  color: var(--accent);
  border-radius: 14px;
  box-shadow: 0 12px 26px rgba(120, 136, 170, 0.12);
}

.search-input,
.form-input,
.table-wrapper,
.modal-content,
.btn-cancel {
  border-color: var(--line);
}

.search-input:focus,
.form-input:focus {
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(83, 120, 214, 0.08);
}

.project-table th {
  color: var(--muted);
  background: rgba(246, 248, 252, 0.94);
  border-bottom-color: var(--line);
}

.project-table tr:hover td {
  background: rgba(244, 247, 255, 0.9);
}

.status-progress,
.btn-edit {
  background: rgba(83, 120, 214, 0.1);
  color: var(--accent);
}

.status-completed {
  background: rgba(92, 155, 116, 0.12);
  color: var(--success);
}

.btn-delete,
.submit-error,
.btn-confirm-delete {
  background: rgba(212, 107, 107, 0.1);
  color: var(--danger);
}

.modal-overlay {
  background: rgba(226, 231, 240, 0.44);
  backdrop-filter: blur(10px);
}
</style>
