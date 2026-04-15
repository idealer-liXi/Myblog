<template>
  <div class="category-management">
    <div class="list-header">
      <h2 class="list-title">分类管理</h2>
      <button class="btn-create" @click="openCreateModal">
        <i class="bi bi-plus-lg"></i>
        新建分类
      </button>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <i class="bi bi-search"></i>
        <input type="text" v-model="searchKeyword" placeholder="搜索分类名称..." class="search-input" />
      </div>
    </div>

    <div class="table-wrapper">
      <table class="category-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-icon">图标</th>
            <th class="col-name">分类名称</th>
            <th class="col-key">标识键</th>
            <th class="col-count">文章数</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cat in filteredCategories" :key="cat.id">
            <td class="col-id">{{ cat.id }}</td>
            <td class="col-icon">
              <span class="cat-icon-badge" :style="{ background: cat.color || '#007bff' }">
                <i :class="cat.icon"></i>
              </span>
            </td>
            <td class="col-name">{{ cat.name }}</td>
            <td class="col-key"><code class="key-badge">{{ cat.key }}</code></td>
            <td class="col-count">{{ cat.articleCount }}</td>
            <td class="col-actions">
              <button class="btn-action btn-edit" @click="openEditModal(cat)">
                <i class="bi bi-pencil-square"></i>
                编辑
              </button>
              <button class="btn-action btn-delete" @click="confirmDelete(cat)">
                <i class="bi bi-trash3"></i>
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <i :class="isEditing ? 'bi bi-pencil-square' : 'bi bi-plus-circle'" class="modal-title-icon"></i>
          <h3>{{ isEditing ? '编辑分类' : '新建分类' }}</h3>
        </div>
        <form @submit.prevent="handleSave" class="modal-form">
          <div class="form-group">
            <label class="form-label">分类名称 <span class="required">*</span></label>
            <input type="text" v-model="form.name" class="form-input" :class="{ 'is-error': formErrors.name }" placeholder="例如：Java" />
            <span v-if="formErrors.name" class="error-tip">{{ formErrors.name }}</span>
          </div>
          <div class="form-group">
            <label class="form-label">标识键 <span class="required">*</span></label>
            <input type="text" v-model="form.key" class="form-input" :class="{ 'is-error': formErrors.key }" placeholder="例如：java" :disabled="isEditing" />
            <span v-if="formErrors.key" class="error-tip">{{ formErrors.key }}</span>
            <span class="form-hint" v-if="!isEditing">用于 URL 和 API 参数，创建后不可修改</span>
          </div>
          <div class="form-row-inline">
            <div class="form-group">
              <label class="form-label">图标类名</label>
              <input type="text" v-model="form.icon" class="form-input" placeholder="例如：bi bi-cup-hot-fill" />
            </div>
            <div class="form-group">
              <label class="form-label">颜色</label>
              <input type="color" v-model="form.color" class="form-color" />
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
              {{ isEditing ? '保存修改' : '创建分类' }}
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
        <p class="modal-body">确定要删除分类「{{ deleteTarget?.name }}」吗？该分类下有 {{ deleteTarget?.articleCount || 0 }} 篇文章，删除后文章的分类将被清空。</p>
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
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/services/categoryService.js'

const categories = ref([])
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
  key: '',
  icon: '',
  color: '#007bff'
})

const formErrors = ref({
  name: '',
  key: ''
})

const filteredCategories = computed(() => {
  if (!searchKeyword.value) return categories.value
  const kw = searchKeyword.value.toLowerCase()
  return categories.value.filter(c =>
    c.name.toLowerCase().includes(kw) || c.key.toLowerCase().includes(kw)
  )
})

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch {
    categories.value = []
  }
}

const openCreateModal = () => {
  isEditing.value = false
  form.value = { id: null, name: '', key: '', icon: '', color: '#007bff' }
  formErrors.value = { name: '', key: '' }
  formError.value = ''
  showModal.value = true
}

const openEditModal = (cat) => {
  isEditing.value = true
  form.value = { ...cat }
  formErrors.value = { name: '', key: '' }
  formError.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const validateForm = () => {
  formErrors.value = { name: '', key: '' }
  let valid = true
  if (!form.value.name.trim()) {
    formErrors.value.name = '请输入分类名称'
    valid = false
  }
  if (!form.value.key.trim()) {
    formErrors.value.key = '请输入标识键'
    valid = false
  } else if (!isEditing.value && categories.value.some(c => c.key === form.value.key.trim())) {
    formErrors.value.key = '该标识键已存在'
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
      const idx = categories.value.findIndex(c => c.id === form.value.id)
      if (idx > -1) {
        const updated = await updateCategory(form.value.id, { ...form.value })
        categories.value.splice(idx, 1, updated)
      }
    } else {
      const created = await createCategory({ ...form.value })
      categories.value.push(created)
    }
    closeModal()
  } catch {
    formError.value = isEditing.value ? '保存失败，请稍后重试' : '创建失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

const confirmDelete = (cat) => {
  deleteTarget.value = cat
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await deleteCategory(deleteTarget.value.id)
    categories.value = categories.value.filter(c => c.id !== deleteTarget.value.id)
  } catch {
    // error already handled
  }
  showDeleteModal.value = false
  deleteTarget.value = null
}

loadCategories()
</script>

<style scoped>
.category-management {
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

.category-table {
  width: 100%;
  border-collapse: collapse;
}

.category-table th {
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 0.82rem;
  color: #888;
  background: rgba(0, 123, 255, 0.04);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.category-table td {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  font-size: 0.9rem;
  color: #555;
}

.category-table tr:last-child td {
  border-bottom: none;
}

.category-table tr:hover td {
  background: rgba(0, 123, 255, 0.03);
}

.col-id { width: 60px; }
.col-icon { width: 60px; }
.col-key { width: 120px; }
.col-count { width: 80px; }
.col-actions { width: 200px; white-space: nowrap; }

.cat-icon-badge {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.95rem;
}

.key-badge {
  background: rgba(0, 123, 255, 0.08);
  color: #007bff;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 0.82rem;
  font-family: 'Consolas', 'Monaco', monospace;
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

.form-input:disabled {
  background: #f5f5f5;
  color: #888;
  cursor: not-allowed;
}

.form-color {
  width: 44px;
  height: 36px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  padding: 2px;
  cursor: pointer;
  background: none;
}

.form-hint {
  font-size: 0.78rem;
  color: #999;
}

.error-tip {
  color: #dc3545;
  font-size: 0.8rem;
}

.form-row-inline {
  display: flex;
  gap: 16px;
}

.form-row-inline .form-group {
  flex: 1;
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

  .category-table th,
  .category-table td {
    padding: 10px 8px;
    font-size: 0.82rem;
  }

  .col-id, .col-count {
    display: none;
  }

  .modal-content {
    padding: 20px;
  }

  .form-row-inline {
    flex-direction: column;
    gap: 0;
  }
}
</style>