<template>
  <div class="profile-management">
    <div class="list-header">
      <h2 class="list-title">站主介绍管理</h2>
    </div>

    <form class="profile-form" @submit.prevent="save">
      <div class="form-grid">
        <label>头像地址<input v-model="form.avatar" class="form-input" /></label>
        <label>姓名<input v-model="form.name" class="form-input" /></label>
        <label>职业描述<input v-model="form.bio" class="form-input" /></label>
        <label>邮箱<input v-model="form.email" class="form-input" /></label>
        <label>GitHub 名称<input v-model="form.githubName" class="form-input" /></label>
        <label>GitHub 地址<input v-model="form.githubUrl" class="form-input" /></label>
        <label>地点<input v-model="form.location" class="form-input" /></label>
      </div>

      <label class="block-field">个人简介<textarea v-model="form.introduction" class="form-textarea" rows="5" /></label>
      <label class="block-field">兴趣爱好（每行一条）<textarea v-model="hobbiesText" class="form-textarea" rows="5" /></label>
      <label class="block-field">安徽理工大学荣誉（每行一条）<textarea v-model="undergraduateHonorsText" class="form-textarea" rows="5" /></label>
      <label class="block-field">东北大学荣誉（每行一条）<textarea v-model="graduateHonorsText" class="form-textarea" rows="5" /></label>

      <div v-if="successMessage" class="submit-success">{{ successMessage }}</div>
      <div v-if="error" class="submit-error">{{ error }}</div>
      <div class="modal-actions">
        <button type="submit" class="btn-submit" :disabled="saving">{{ saving ? '保存中...' : '保存配置' }}</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdminProfile, updateProfile } from '@/services/profileService.js'

const saving = ref(false)
const error = ref('')
const successMessage = ref('')
const form = ref({
  avatar: '',
  name: '',
  bio: '',
  email: '',
  githubName: '',
  githubUrl: '',
  location: '',
  introduction: '',
  hobbies: [],
  undergraduateHonors: [],
  graduateHonors: [],
  schools: []
})

const normalizeFormState = (profile) => {
  const undergraduate = (profile?.schools || []).find(item => item.schoolKey === 'undergraduate')
  const graduate = (profile?.schools || []).find(item => item.schoolKey === 'graduate')

  return {
    avatar: profile?.avatar || '',
    name: profile?.name || '',
    bio: profile?.bio || '',
    email: profile?.email || '',
    githubName: profile?.githubName || '',
    githubUrl: profile?.githubUrl || '',
    location: profile?.location || '',
    introduction: profile?.introduction || '',
    hobbies: Array.isArray(profile?.hobbies) ? profile.hobbies : [],
    undergraduateHonors: Array.isArray(undergraduate?.honors) ? undergraduate.honors : [],
    graduateHonors: Array.isArray(graduate?.honors) ? graduate.honors : [],
    schools: Array.isArray(profile?.schools) ? profile.schools : []
  }
}

const hobbiesText = computed({
  get: () => (form.value.hobbies || []).join('\n'),
  set: (value) => {
    form.value.hobbies = value.split(/\r?\n/).map(item => item.trim()).filter(Boolean)
  }
})

const undergraduateHonorsText = computed({
  get: () => (form.value.undergraduateHonors || []).join('\n'),
  set: (value) => {
    form.value.undergraduateHonors = value.split(/\r?\n/).map(item => item.trim()).filter(Boolean)
  }
})

const graduateHonorsText = computed({
  get: () => (form.value.graduateHonors || []).join('\n'),
  set: (value) => {
    form.value.graduateHonors = value.split(/\r?\n/).map(item => item.trim()).filter(Boolean)
  }
})

const load = async () => {
  form.value = normalizeFormState(await getAdminProfile())
}

const save = async () => {
  saving.value = true
  error.value = ''
  successMessage.value = ''
  try {
    const payload = {
      avatar: form.value.avatar,
      name: form.value.name,
      bio: form.value.bio,
      email: form.value.email,
      githubName: form.value.githubName,
      githubUrl: form.value.githubUrl,
      location: form.value.location,
      introduction: form.value.introduction,
      hobbies: form.value.hobbies,
      undergraduateHonors: form.value.undergraduateHonors,
      graduateHonors: form.value.graduateHonors
    }
    form.value = normalizeFormState(await updateProfile(payload))
    successMessage.value = '保存成功'
  } catch (err) {
    error.value = err?.message || '保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.profile-management {
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

.profile-form {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-grid label,
.block-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.92rem;
  font-weight: 600;
}

.block-field {
  margin-top: 16px;
}

.form-input,
.form-textarea {
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 0.92rem;
}

.form-textarea {
  resize: vertical;
}

.submit-error {
  margin-top: 16px;
  color: #b91c1c;
}

.submit-success {
  margin-top: 16px;
  color: #15803d;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-submit {
  border: none;
  border-radius: 12px;
  padding: 10px 18px;
  background: linear-gradient(135deg, #4a86e8, #7c9cff);
  color: #fff;
  font-weight: 600;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
