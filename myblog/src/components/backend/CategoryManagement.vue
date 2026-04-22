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
  --surface: rgba(255, 255, 255, 0.76);
  --surface-strong: rgba(255, 255, 255, 0.93);
  --line: rgba(128, 145, 184, 0.16);
  --line-strong: rgba(87, 116, 184, 0.26);
  --text: #25324d;
  --muted: #6f7b93;
  --soft: #97a2b7;
  --accent: #5378d6;
  --accent-soft: rgba(83, 120, 214, 0.1);
  --danger: #d46b6b;
  --danger-soft: rgba(212, 107, 107, 0.1);
  padding: 8px;
  color: var(--text);
  font-family: 'Avenir Next', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  gap: 16px;
}

.list-title {
  font-size: 1.55rem;
  font-weight: 700;
  color: var(--text);
  margin: 0;
}

.btn-create {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: var(--surface-strong);
  color: var(--accent);
  border: 1px solid rgba(83, 120, 214, 0.18);
  border-radius: 14px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 12px 26px rgba(120, 136, 170, 0.12);
}

.btn-create:hover {
  background: #fff;
  color: #395fbe;
  transform: translateY(-1px);
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
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
  color: var(--soft);
}

.search-input,
.form-input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--text);
  font-size: 0.9rem;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.search-input {
  padding-left: 40px;
}

.search-input::placeholder {
  color: var(--soft);
}

.search-input:focus,
.form-input:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(83, 120, 214, 0.08);
  background: #fff;
}

.table-wrapper {
  background: linear-gradient(180deg, var(--surface-strong), var(--surface));
  backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 18px 36px rgba(120, 136, 170, 0.12);
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
  color: var(--muted);
  background: rgba(246, 248, 252, 0.94);
  border-bottom: 1px solid var(--line);
}

.category-table td {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(128, 145, 184, 0.1);
  font-size: 0.9rem;
  color: var(--text);
}

.category-table tr:last-child td {
  border-bottom: none;
}

.category-table tr:hover td {
  background: rgba(244, 247, 255, 0.9);
}

.col-id { width: 60px; }
.col-icon { width: 60px; }
.col-key { width: 120px; }
.col-count { width: 80px; }
.col-actions { width: 200px; white-space: nowrap; }

.cat-icon-badge {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.95rem;
}

.key-badge {
  background: var(--accent-soft);
  color: var(--accent);
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.82rem;
  font-family: 'SFMono-Regular', Consolas, monospace;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 8px;
}

.btn-action:last-child {
  margin-right: 0;
}

.btn-edit {
  background: var(--accent-soft);
  color: var(--accent);
  border-color: rgba(83, 120, 214, 0.18);
}

.btn-edit:hover {
  background: rgba(83, 120, 214, 0.16);
}

.btn-delete {
  background: var(--danger-soft);
  color: var(--danger);
  border-color: rgba(212, 107, 107, 0.18);
}

.btn-delete:hover {
  background: rgba(212, 107, 107, 0.16);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(226, 231, 240, 0.44);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(247, 249, 253, 0.94));
  border-radius: 22px;
  padding: 28px;
  width: 460px;
  max-width: 90vw;
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 24px 52px rgba(120, 136, 170, 0.18);
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.modal-title-icon {
  font-size: 1.3rem;
  color: var(--accent);
}

.modal-icon {
  font-size: 1.5rem;
  color: #dea456;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text);
}

.modal-body,
.form-hint {
  color: var(--muted);
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
  color: var(--text);
}

.required,
.error-tip {
  color: var(--danger);
}

.form-input.is-error {
  border-color: rgba(212, 107, 107, 0.4);
  box-shadow: 0 0 0 4px rgba(212, 107, 107, 0.08);
}

.form-input:disabled {
  background: rgba(243, 245, 249, 0.95);
  color: var(--soft);
  cursor: not-allowed;
}

.form-color {
  width: 48px;
  height: 40px;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 3px;
  cursor: pointer;
  background: none;
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
  background: var(--danger-soft);
  border: 1px solid rgba(212, 107, 107, 0.18);
  border-radius: 12px;
  padding: 12px 16px;
  color: var(--danger);
  font-size: 0.88rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.btn-cancel,
.btn-submit,
.btn-confirm-delete {
  padding: 10px 22px;
  border-radius: 10px;
  font-size: 0.88rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel {
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.78);
  color: var(--muted);
}

.btn-cancel:hover {
  background: white;
}

.btn-submit {
  border: 1px solid rgba(83, 120, 214, 0.16);
  background: var(--accent);
  color: white;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
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
  border: 1px solid rgba(212, 107, 107, 0.14);
  background: #d66f6f;
  color: white;
  font-weight: 500;
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

  .col-id,
  .col-count {
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
