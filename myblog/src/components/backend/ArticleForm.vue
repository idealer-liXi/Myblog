<template>
  <div class="article-form">
    <div class="form-header">
      <h2 class="form-title">{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
      <router-link :to="{ name: 'backend-articles' }" class="btn-back">
        <i class="bi bi-arrow-left"></i>
        返回列表
      </router-link>
    </div>

    <div v-if="loading && isEdit" class="loading-state">
      <div class="loading-spinner"></div>
      <span>正在加载文章...</span>
    </div>

    <form v-else @submit.prevent="handleSubmit" class="form-body">
      <div class="form-row">
        <div class="form-group form-group-title">
          <label class="form-label">文章标题 <span class="required">*</span></label>
          <input
            type="text"
            v-model="form.title"
            class="form-input"
            :class="{ 'is-error': errors.title }"
            placeholder="请输入文章标题"
          />
          <span v-if="errors.title" class="error-tip">{{ errors.title }}</span>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="form-label">主题 <span class="required">*</span></label>
          <select v-model="form.theme" class="form-input" :class="{ 'is-error': errors.theme }">
            <option value="">请选择主题</option>
            <option v-for="theme in themes" :key="theme" :value="theme">{{ theme }}</option>
          </select>
          <span v-if="errors.theme" class="error-tip">{{ errors.theme }}</span>
        </div>
        <div class="form-group">
          <label class="form-label">封面图</label>
          <input type="text" v-model="form.image" class="form-input" placeholder="图片 URL" />
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">摘要</label>
        <textarea v-model="form.summary" class="form-input form-textarea" rows="3" placeholder="请输入文章摘要（可选）"></textarea>
      </div>

      <div class="form-group">
        <label class="form-label">文章内容 <span class="required">*</span></label>
        <div class="editor-wrapper" :class="{ 'is-error': errors.content }">
          <MdEditor
            v-model="form.content"
            :theme="editorTheme"
            :preview="true"
            language="zh-CN"
            placeholder="请输入文章内容（支持 Markdown）"
            @onUploadImg="handleUploadImg"
          />
        </div>
        <p class="editor-tip">
          直接将截图或图片粘贴、拖拽到编辑器中即可自动上传到 OSS 并插入 Markdown。图片素材库仅用于回看和维护历史图片。
        </p>
        <span v-if="errors.content" class="error-tip">{{ errors.content }}</span>
      </div>

      <div v-if="submitError" class="submit-error">
        <i class="bi bi-exclamation-circle-fill"></i>
        {{ submitError }}
      </div>

      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="router.push({ name: 'backend-articles' })">取消</button>
        <button type="submit" class="btn-submit" :disabled="submitting">
          <span v-if="submitting" class="btn-spinner"></span>
          {{ isEdit ? '保存修改' : '发布文章' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getArticleById, createArticle, updateArticle } from '@/services/articleService.js'
import { uploadImage } from '@/services/uploadService.js'
import { getThemes } from '@/services/themeService.js'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const router = useRouter()
const route = useRoute()

const themes = ref([])
const editorTheme = 'light'

const isEdit = computed(() => route.name === 'backend-article-edit')

const form = ref({
  title: '',
  content: '',
  summary: '',
  theme: '',
  image: ''
})

const errors = ref({
  title: '',
  content: '',
  theme: ''
})

const loading = ref(false)
const submitting = ref(false)
const submitError = ref('')

const buildDocumentDirectory = () => {
  const rawTitle = (form.value.title || '').trim() || '未命名文章'
  return `documents/${encodeURIComponent(rawTitle)}`
}

const handleUploadImg = async (files, callback) => {
  const urls = []
  for (const file of files) {
    try {
      const url = await uploadImage(file, {
        directory: buildDocumentDirectory()
      })
      urls.push(url)
    } catch {
      submitError.value = '图片上传失败，请稍后重试'
    }
  }
  callback(urls)
}

const validate = () => {
  errors.value = { title: '', content: '', theme: '' }
  let valid = true
  if (!form.value.title.trim()) {
    errors.value.title = '请输入文章标题'
    valid = false
  }
  if (!form.value.content.trim()) {
    errors.value.content = '请输入文章内容'
    valid = false
  }
  if (!form.value.theme) {
    errors.value.theme = '请选择主题'
    valid = false
  }
  return valid
}

const handleSubmit = async () => {
  if (!validate()) return
  submitting.value = true
  submitError.value = ''

  try {
    if (isEdit.value) {
      await updateArticle(Number(route.params.id), { ...form.value })
    } else {
      await createArticle({ ...form.value })
    }
    router.push({ name: 'backend-articles' })
  } catch (err) {
    submitError.value = isEdit.value ? '保存失败，请稍后重试' : '发布失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const data = await getThemes()
    themes.value = data.map(t => t.name)
  } catch {
    themes.value = ['java', 'python', 'c++', 'vue']
  }
  if (isEdit.value && route.params.id) {
    loading.value = true
    try {
      const article = await getArticleById(Number(route.params.id))
      form.value = {
        title: article.title || '',
        content: article.content || '',
        summary: article.summary || '',
        theme: article.theme || '',
        image: article.image || ''
      }
    } catch (err) {
      submitError.value = '获取文章详情失败'
    } finally {
      loading.value = false
    }
  }
})
</script>

<style scoped>
.article-form {
  padding: 4px;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.form-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.04);
  color: #666;
  text-decoration: none;
  font-size: 0.88rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-back:hover {
  background: rgba(0, 0, 0, 0.08);
  color: #333;
  text-decoration: none;
}

.form-body {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.form-row .form-group {
  flex: 1;
}

.form-group {
  margin-bottom: 20px;
}

.form-group-title {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 0.88rem;
  font-weight: 600;
  color: #555;
  margin-bottom: 6px;
}

.required {
  color: #dc3545;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  transition: all 0.25s ease;
  box-sizing: border-box;
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
  line-height: 1.6;
}

select.form-input {
  cursor: pointer;
  appearance: auto;
}

.error-tip {
  display: block;
  color: #dc3545;
  font-size: 0.8rem;
  margin-top: 4px;
}

.editor-wrapper {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.12);
}

.editor-tip {
  margin: 8px 2px 0;
  color: var(--muted);
  font-size: 0.82rem;
  line-height: 1.6;
}

.editor-wrapper.is-error {
  border-color: #dc3545;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
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
  margin-bottom: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 8px;
}

.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #ddd;
  border-radius: 10px;
  background: white;
  color: #666;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
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
  font-size: 0.9rem;
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
  transform: translateY(-1px);
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

.loading-state {
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

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
.article-form {
  --surface: rgba(255, 255, 255, 0.76);
  --surface-strong: rgba(255, 255, 255, 0.93);
  --line: rgba(128, 145, 184, 0.16);
  --line-strong: rgba(87, 116, 184, 0.26);
  --text: #25324d;
  --muted: #6f7b93;
  --soft: #97a2b7;
  --accent: #5378d6;
  --danger: #d46b6b;
  padding: 8px;
  color: var(--text);
  font-family: 'Avenir Next', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.form-title {
  color: var(--text);
}

.btn-back {
  background: rgba(255, 255, 255, 0.72);
  color: var(--muted);
  border: 1px solid var(--line);
  border-radius: 14px;
}

.btn-back:hover {
  background: white;
  color: var(--text);
}

.form-body {
  background: linear-gradient(180deg, var(--surface-strong), var(--surface));
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 22px;
  box-shadow: 0 18px 36px rgba(120, 136, 170, 0.12);
}

.form-label {
  color: var(--text);
}

.form-input {
  border: 1px solid var(--line);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--text);
}

.form-input:focus {
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(83, 120, 214, 0.08);
}

.form-input.is-error,
.editor-wrapper.is-error {
  border-color: rgba(212, 107, 107, 0.4);
  box-shadow: 0 0 0 4px rgba(212, 107, 107, 0.08);
}

.editor-wrapper {
  border: 1px solid var(--line);
  border-radius: 16px;
}

.submit-error {
  background: rgba(212, 107, 107, 0.08);
  border: 1px solid rgba(212, 107, 107, 0.18);
  color: var(--danger);
}

.btn-cancel {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.78);
  color: var(--muted);
}

.btn-cancel:hover {
  background: white;
}

.btn-submit {
  border: 1px solid rgba(83, 120, 214, 0.16);
  border-radius: 10px;
  background: var(--accent);
  box-shadow: 0 12px 24px rgba(95, 122, 183, 0.2);
}

.btn-submit:hover:not(:disabled) {
  background: #406bcf;
  box-shadow: 0 16px 28px rgba(95, 122, 183, 0.24);
}

.loading-state {
  color: var(--muted);
  border: 1px dashed var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.46);
}

.loading-spinner {
  border: 3px solid rgba(83, 120, 214, 0.15);
  border-top-color: var(--accent);
}
</style>
