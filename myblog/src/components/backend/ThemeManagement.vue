<template>
  <div class="theme-management">
    <div class="list-header">
      <h2 class="list-title">主题管理</h2>
      <button class="btn-create" @click="openCreateModal">
        <i class="bi bi-plus-lg"></i>
        新建主题
      </button>
    </div>

    <div class="table-wrapper" v-if="themes.length > 0">
      <table class="theme-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-icon-col">图标</th>
            <th class="col-name">主题名称</th>
            <th class="col-color">主题色</th>
            <th class="col-sort">排序</th>
            <th class="col-count">文章数</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="theme in themes" :key="theme.id">
            <td class="col-id">{{ theme.id }}</td>
            <td class="col-icon-col">
              <i :class="theme.icon || 'bi bi-tag'" :style="{ color: theme.color || '#5378d6' }"></i>
            </td>
            <td class="col-name">{{ theme.name }}</td>
            <td class="col-color">
              <span class="color-dot" :style="{ background: theme.color || '#5378d6' }"></span>
              {{ theme.color || '-' }}
            </td>
            <td class="col-sort">{{ theme.sortOrder }}</td>
            <td class="col-count">{{ theme.articleCount || 0 }}</td>
            <td class="col-actions">
              <button class="btn-action btn-edit" @click="openEditModal(theme)">
                <i class="bi bi-pencil-square"></i>
                编辑
              </button>
              <button class="btn-action btn-delete" @click="confirmDelete(theme)">
                <i class="bi bi-trash3"></i>
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <i class="bi bi-tags"></i>
      <p>暂无主题</p>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑主题' : '新建主题' }}</h3>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-body">
          <div class="form-group">
            <label class="form-label">主题名称 <span class="required">*</span></label>
            <input type="text" v-model="form.name" class="form-input" placeholder="如：java" />
          </div>
          <div class="form-group">
            <label class="form-label">图标类名</label>
            <input type="text" v-model="form.icon" class="form-input" placeholder="如：bi bi-cup-hot-fill" />
          </div>
          <div class="form-group">
            <label class="form-label">主题色</label>
            <input type="color" v-model="form.color" class="form-input-color" />
            <input type="text" v-model="form.color" class="form-input" placeholder="#007bff" />
          </div>
          <div class="form-group">
            <label class="form-label">排序权重</label>
            <input type="number" v-model="form.sortOrder" class="form-input" placeholder="0" />
          </div>
          <div v-if="formError" class="form-error">{{ formError }}</div>
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-confirm">{{ isEdit ? '保存' : '创建' }}</button>
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
        <p class="modal-body">确定要删除主题「{{ deleteTarget?.name }}」吗？此操作不可撤销。</p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDeleteModal = false">取消</button>
          <button class="btn-confirm-delete" @click="handleDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getThemes, createTheme, updateTheme, deleteTheme } from '@/services/themeService.js'

const themes = ref([])
const loading = ref(false)
const showModal = ref(false)
const showDeleteModal = ref(false)
const isEdit = ref(false)
const deleteTarget = ref(null)
const formError = ref('')

const form = ref({
  id: null,
  name: '',
  icon: '',
  color: '#007bff',
  sortOrder: 0
})

const fetchThemes = async () => {
  loading.value = true
  try {
    themes.value = await getThemes()
  } catch {
    themes.value = []
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  isEdit.value = false
  form.value = { id: null, name: '', icon: '', color: '#007bff', sortOrder: 0 }
  formError.value = ''
  showModal.value = true
}

const openEditModal = (theme) => {
  isEdit.value = true
  form.value = {
    id: theme.id,
    name: theme.name || '',
    icon: theme.icon || '',
    color: theme.color || '#007bff',
    sortOrder: theme.sortOrder || 0
  }
  formError.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const handleSubmit = async () => {
  if (!form.value.name.trim()) {
    formError.value = '主题名称不能为空'
    return
  }
  try {
    if (isEdit.value) {
      await updateTheme(form.value.id, form.value)
    } else {
      await createTheme(form.value)
    }
    closeModal()
    fetchThemes()
  } catch {
    formError.value = isEdit.value ? '保存失败' : '创建失败'
  }
}

const confirmDelete = (theme) => {
  deleteTarget.value = theme
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await deleteTheme(deleteTarget.value.id)
    themes.value = themes.value.filter(t => t.id !== deleteTarget.value.id)
  } catch {
  } finally {
    showDeleteModal.value = false
    deleteTarget.value = null
  }
}

onMounted(() => {
  fetchThemes()
})
</script>

<style scoped>
.theme-management {
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
  letter-spacing: -0.02em;
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
  background: #ffffff;
  color: #395fbe;
  transform: translateY(-1px);
  border-color: rgba(83, 120, 214, 0.28);
}

.table-wrapper {
  background: linear-gradient(180deg, var(--surface-strong), var(--surface));
  backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 18px 36px rgba(120, 136, 170, 0.12);
}

.theme-table {
  width: 100%;
  border-collapse: collapse;
}

.theme-table th {
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 0.82rem;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: rgba(246, 248, 252, 0.94);
  border-bottom: 1px solid var(--line);
}

.theme-table td {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(128, 145, 184, 0.1);
  font-size: 0.9rem;
  color: var(--text);
}

.theme-table tr:last-child td {
  border-bottom: none;
}

.theme-table tr:hover td {
  background: rgba(244, 247, 255, 0.9);
}

.col-id { width: 60px; }
.col-icon-col { width: 50px; }
.col-color { width: 120px; }
.col-sort { width: 80px; }
.col-count { width: 80px; }
.col-actions { width: 200px; }

.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
  border: 1px solid rgba(0,0,0,0.1);
}

.form-input-color {
  width: 40px;
  height: 36px;
  padding: 2px;
  border: 1px solid var(--line);
  border-radius: 10px;
  cursor: pointer;
  vertical-align: middle;
  margin-right: 8px;
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
}

.btn-edit {
  background: var(--accent-soft);
  color: var(--accent);
  border-color: rgba(83, 120, 214, 0.18);
}

.btn-edit:hover {
  background: rgba(83, 120, 214, 0.16);
  color: #395fbe;
}

.btn-delete {
  background: var(--danger-soft);
  color: var(--danger);
  border-color: rgba(212, 107, 107, 0.18);
}

.btn-delete:hover {
  background: rgba(212, 107, 107, 0.16);
  color: #b95454;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--muted);
  gap: 8px;
  border: 1px dashed var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.46);
}

.empty-state i {
  font-size: 3rem;
  color: #d3d9e5;
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
  margin-bottom: 16px;
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

.modal-body {
  color: var(--muted);
  font-size: 0.92rem;
  line-height: 1.5;
  margin: 0 0 24px 0;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 6px;
}

.required {
  color: #dc3545;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--text);
  font-size: 0.9rem;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(83, 120, 214, 0.08);
  background: white;
}

.form-error {
  color: var(--danger);
  font-size: 0.85rem;
  margin-bottom: 12px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  padding: 8px 20px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.78);
  color: var(--muted);
  cursor: pointer;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: white;
}

.btn-confirm {
  padding: 8px 20px;
  border: 1px solid rgba(83, 120, 214, 0.16);
  border-radius: 10px;
  background: var(--accent);
  color: white;
  cursor: pointer;
  font-size: 0.88rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-confirm:hover {
  background: #406bcf;
}

.btn-confirm-delete {
  padding: 8px 20px;
  border: 1px solid rgba(212, 107, 107, 0.14);
  border-radius: 10px;
  background: #d66f6f;
  color: white;
  cursor: pointer;
  font-size: 0.88rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-confirm-delete:hover {
  background: #c85e5e;
}
</style>
