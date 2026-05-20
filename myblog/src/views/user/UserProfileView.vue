<template>
  <section class="profile-shell">
    <div class="profile-scroll">
      <div class="profile-stage">
        <div class="profile-layout">
          <aside class="profile-aside">
            <div class="avatar-block">
              <div class="avatar-ring">
                <img v-if="profile.effectiveAvatar" :src="profile.effectiveAvatar" :alt="profile.displayName" class="avatar-img" />
                <div v-else class="avatar-blank">{{ profile.initial }}</div>
              </div>
              <div class="avatar-meta">
                <h1 class="user-name">{{ profile.displayName }}</h1>
                <span class="user-handle">{{ profile.handle }}</span>
                <span class="login-chip" :class="profile.loginType">
                  <i :class="profile.loginType === 'weixin' ? 'bi bi-wechat' : 'bi bi-key-fill'"></i>
                  {{ profile.loginTypeLabel }}
                </span>
              </div>
            </div>

            <p class="user-note">{{ profile.description }}</p>

            <div class="aside-nav">
              <router-link :to="{ name: 'blog' }" class="nav-btn ghost">
                <i class="bi bi-arrow-left"></i>
                返回首页
              </router-link>
              <router-link v-if="profile.canEnterBackend" :to="{ name: 'backend' }" class="nav-btn solid">
                进入后台
                <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </aside>

          <main class="profile-main">
            <div class="section-head">
              <span class="section-dot"></span>
              <h2 class="section-title">会话信息</h2>
            </div>

            <div class="field-grid">
              <div class="field-cell">
                <label class="field-label">显示名称</label>
                <span class="field-body">{{ profile.displayName }}</span>
              </div>
              <div class="field-cell">
                <label class="field-label">用户名</label>
                <span class="field-body">{{ profile.username || '—' }}</span>
              </div>
              <div class="field-cell">
                <label class="field-label">微信昵称</label>
                <span class="field-body">{{ profile.weixinName || '—' }}</span>
              </div>
              <div class="field-cell">
                <label class="field-label">登录方式</label>
                <span class="field-body">{{ profile.loginTypeLabel }}</span>
              </div>
              <div data-testid="profile-wechat-bind-cell" class="field-cell">
                <label class="field-label">微信绑定</label>
                <span class="field-body">{{ profile.weixinBound ? '已绑定' : '未绑定' }}</span>
              </div>
              <div data-testid="profile-wechat-action-cell" class="field-cell">
                <label class="field-label">绑定操作</label>
                <div class="bind-actions">
                  <button
                    v-if="!profile.weixinBound"
                    class="bind-btn solid"
                    type="button"
                    @click="startWechatBind"
                    :disabled="bindBusy"
                  >
                    {{ bindBusy ? '正在生成二维码...' : '绑定微信' }}
                  </button>
                  <button
                    v-else
                    class="bind-btn ghost"
                    type="button"
                    @click="handleUnbindWechat"
                    :disabled="bindBusy"
                  >
                    解除绑定
                  </button>
                </div>
              </div>
              <div class="field-cell span-2">
                <label class="field-label">资料操作</label>
                <div class="bind-actions">
                  <button
                    data-testid="profile-edit-toggle"
                    class="bind-btn solid"
                    type="button"
                    @click="openProfileEdit"
                    :disabled="profileSaveBusy"
                  >
                    编辑资料
                  </button>
                </div>
              </div>
              <div v-if="profileEditVisible" class="field-cell span-2">
                <label class="field-label">编辑资料</label>
                <div class="profile-edit-panel">
                  <div class="profile-edit-grid">
                    <label class="profile-edit-item">
                      <span>显示名称</span>
                      <input
                        data-testid="profile-display-name-input"
                        v-model="profileForm.displayName"
                        type="text"
                        class="profile-edit-input"
                        maxlength="32"
                        placeholder="请输入显示名称"
                      />
                    </label>
                    <label class="profile-edit-item">
                      <span>头像来源</span>
                      <select
                        data-testid="profile-avatar-source-select"
                        v-model="profileForm.avatarSource"
                        class="profile-edit-input"
                      >
                        <option value="DEFAULT">默认头像</option>
                        <option v-if="profile.weixinBound" value="WECHAT">微信头像</option>
                        <option value="UPLOAD">上传头像</option>
                      </select>
                    </label>
                    <div
                      v-if="profileForm.avatarSource === 'UPLOAD'"
                      class="profile-edit-item span-2"
                    >
                      <span>上传头像</span>
                      <div
                        class="avatar-upload-card"
                        :class="{ dragging: avatarDragActive, busy: avatarUploadBusy }"
                        tabindex="0"
                        @click="triggerAvatarUpload"
                        @dragenter.prevent="avatarDragActive = true"
                        @dragover.prevent="avatarDragActive = true"
                        @dragleave.prevent="avatarDragActive = false"
                        @drop="handleAvatarDrop"
                        @paste="handleAvatarPaste"
                      >
                        <input
                          ref="avatarFileInput"
                          data-testid="profile-avatar-file-input"
                          type="file"
                          class="hidden-input"
                          accept="image/*"
                          @change="handleAvatarFileChange"
                        />
                        <div v-if="profileForm.avatar" class="avatar-upload-preview-wrap">
                          <img
                            data-testid="profile-avatar-upload-preview"
                            :src="profileForm.avatar"
                            alt="上传头像预览"
                            class="avatar-upload-preview"
                          />
                          <span>{{ avatarUploadBusy ? '上传中...' : '图片已就绪，保存后生效' }}</span>
                        </div>
                        <div v-else class="avatar-upload-empty">
                          <i class="bi" :class="avatarUploadBusy ? 'bi-arrow-repeat spin' : 'bi-cloud-arrow-up'"></i>
                          <span>{{ avatarUploadBusy ? '正在上传头像...' : '点击、拖拽或粘贴图片上传' }}</span>
                        </div>
                      </div>
                      <div v-if="avatarUploadError" class="profile-edit-error">{{ avatarUploadError }}</div>
                    </div>
                  </div>
                  <div v-if="profileEditError" class="profile-edit-error">{{ profileEditError }}</div>
                  <div class="bind-actions">
                    <button
                      data-testid="profile-save-button"
                      class="bind-btn solid"
                      type="button"
                      @click="saveProfileEdit"
                      :disabled="profileSaveBusy"
                    >
                      {{ profileSaveBusy ? '保存中...' : '保存资料' }}
                    </button>
                    <button class="bind-btn ghost" type="button" @click="cancelProfileEdit" :disabled="profileSaveBusy">
                      取消
                    </button>
                  </div>
                </div>
              </div>
              <div v-if="bindPanelVisible" class="field-cell span-2">
                <label class="field-label">扫码绑定</label>
                <div class="bind-panel">
                  <img v-if="bindQrCodeUrl" :src="bindQrCodeUrl" alt="微信绑定二维码" class="bind-qr-image" />
                  <span v-else class="field-body">二维码生成中...</span>
                </div>
              </div>
              <div v-if="bindError" class="field-cell span-2">
                <label class="field-label">错误信息</label>
                <span class="field-body bind-error">{{ bindError }}</span>
              </div>
            </div>
          </main>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { checkWechatBind, fetchCurrentUser, fetchWechatBindTicket, unbindWechat, updateCurrentUserProfile, uploadCurrentUserAvatar } from '@/services/authService'
import { canAccessBackend } from '@/router/accessControl'
import { hasValidSession } from '@/utils/authSession'

const store = useStore()

onMounted(() => {
  document.body.style.overflow = 'hidden'
})

onBeforeUnmount(() => {
  document.body.style.overflow = ''
})

const bindError = ref('')
const bindQrCodeUrl = ref('')
const bindPanelVisible = ref(false)
const bindBusy = ref(false)
const profileEditVisible = ref(false)
const profileSaveBusy = ref(false)
const profileEditError = ref('')
const avatarUploadBusy = ref(false)
const avatarUploadError = ref('')
const avatarDragActive = ref(false)
const avatarFileInput = ref(null)
const profileForm = ref({
  displayName: '',
  avatarSource: 'DEFAULT',
  avatar: ''
})
let bindPollTimer = null

const storeUser = computed(() => store.getters['weixin_user/getUserInfo'] || {})

const profile = computed(() => {
  const username = storeUser.value.username || ''
  const weixinName = storeUser.value.weixinName || ''
  const avatar = storeUser.value.avatar || ''
  const effectiveAvatar = storeUser.value.effectiveAvatar || avatar || ''
  const avatarSource = storeUser.value.avatarSource || 'DEFAULT'
  const loginType = storeUser.value.loginType || 'password'
  const displayName = storeUser.value.displayName || weixinName || username || '未命名用户'
  const roles = Array.isArray(storeUser.value.roles) && storeUser.value.roles.length > 0
    ? storeUser.value.roles.join(', ')
    : '—'
  const canEnterBackend = canAccessBackend(storeUser.value)

  return {
    username,
    weixinName,
    avatar,
    effectiveAvatar,
    avatarSource,
    roles,
    displayName,
    initial: displayName.slice(0, 1).toUpperCase(),
    loginType,
    loginTypeLabel: loginType === 'weixin' ? '微信登录' : '账号登录',
    handle: username ? `@${username}` : '站点会话用户',
    description: loginType === 'weixin' ? '已通过微信扫码登录到统一账户' : '已通过用户名密码登录到统一账户',
    jwtStatus: hasValidSession() ? '有效' : '未检测到或已过期',
    weixinBound: Boolean(storeUser.value.weixinBound),
    avatarSourceLabel: avatarSource === 'UPLOAD' ? '上传头像' : avatarSource === 'WECHAT' ? '微信头像' : '默认头像',
    canEnterBackend,
  }
})

const stopBindPolling = () => {
  if (bindPollTimer) {
    clearInterval(bindPollTimer)
    bindPollTimer = null
  }
}

const refreshProfile = async () => {
  const currentUser = await fetchCurrentUser()
  store.dispatch('weixin_user/updateProfile', currentUser)
  return currentUser
}

const openProfileEdit = () => {
  profileEditError.value = ''
  avatarUploadError.value = ''
  avatarDragActive.value = false
  profileForm.value = {
    displayName: profile.value.displayName || '',
    avatarSource: storeUser.value.avatarSource || 'DEFAULT',
    avatar: storeUser.value.avatar || ''
  }
  profileEditVisible.value = true
}

const cancelProfileEdit = () => {
  profileEditVisible.value = false
  profileSaveBusy.value = false
  profileEditError.value = ''
  avatarUploadError.value = ''
  avatarDragActive.value = false
}

const uploadAvatarFile = async (file) => {
  if (!file) return
  if (!file.type || !file.type.startsWith('image/')) {
    avatarUploadError.value = '仅支持图片文件'
    return
  }

  avatarUploadBusy.value = true
  avatarUploadError.value = ''

  try {
    const url = await uploadCurrentUserAvatar(file)
    profileForm.value.avatarSource = 'UPLOAD'
    profileForm.value.avatar = url
  } catch (error) {
    avatarUploadError.value = error.message || '头像上传失败'
  } finally {
    avatarUploadBusy.value = false
  }
}

const handleAvatarFileChange = async (event) => {
  const [file] = event.target.files || []
  await uploadAvatarFile(file)
  event.target.value = ''
}

const triggerAvatarUpload = () => {
  avatarFileInput.value?.click()
}

const handleAvatarDrop = async (event) => {
  event.preventDefault()
  avatarDragActive.value = false
  const [file] = event.dataTransfer?.files || []
  await uploadAvatarFile(file)
}

const handleAvatarPaste = async (event) => {
  const items = Array.from(event.clipboardData?.items || [])
  const imageItem = items.find(item => item.type && item.type.startsWith('image/'))
  if (!imageItem) return
  event.preventDefault()
  await uploadAvatarFile(imageItem.getAsFile())
}

const saveProfileEdit = async () => {
  const displayName = profileForm.value.displayName.trim()
  if (!displayName) {
    profileEditError.value = '显示名称不能为空'
    return
  }

  profileEditError.value = ''
  profileSaveBusy.value = true

  try {
    const updatedProfile = await updateCurrentUserProfile({
      displayName,
      avatarSource: profileForm.value.avatarSource,
      avatar: profileForm.value.avatar
    })
    store.dispatch('weixin_user/updateProfile', updatedProfile)
    profileEditVisible.value = false
  } catch (error) {
    profileEditError.value = error.message || '更新个人资料失败'
  } finally {
    profileSaveBusy.value = false
  }
}

const startWechatBind = async () => {
  bindError.value = ''
  bindPanelVisible.value = true
  bindQrCodeUrl.value = ''
  bindBusy.value = true
  stopBindPolling()

  try {
    const ticket = await fetchWechatBindTicket()
    bindQrCodeUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${ticket}`
    bindPollTimer = setInterval(async () => {
      try {
        const result = await checkWechatBind(ticket)
        if (!result) return

        stopBindPolling()
        await refreshProfile()
        bindPanelVisible.value = false
      } catch (error) {
        stopBindPolling()
        bindError.value = error.message || '微信绑定失败'
      }
    }, 3000)
  } catch (error) {
    bindError.value = error.message || '获取绑定二维码失败'
    bindPanelVisible.value = false
  } finally {
    bindBusy.value = false
  }
}

const handleUnbindWechat = async () => {
  bindError.value = ''
  bindBusy.value = true

  try {
    await unbindWechat()
    await refreshProfile()
  } catch (error) {
    bindError.value = error.message || '解除微信绑定失败'
  } finally {
    bindBusy.value = false
  }
}

onBeforeUnmount(() => {
  stopBindPolling()
})
</script>

<style scoped>
.profile-shell {
  height: calc(100vh - 76px);
  overflow: hidden;
}

.profile-scroll {
  height: 100%;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.22) transparent;
}

.profile-scroll::-webkit-scrollbar {
  width: 5px;
}

.profile-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.profile-scroll::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.18);
  border-radius: 10px;
}

.profile-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.32);
}

.profile-stage {
  width: min(1160px, 100%);
  margin: 0 auto;
  padding: 28px 24px 48px;
}

.profile-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

/* ── aside ── */
.profile-aside {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 20px;
  padding: 28px 24px 24px;
  box-shadow: 0 14px 36px rgba(120, 138, 180, 0.1);
}

.avatar-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.avatar-ring {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(100, 140, 220, 0.16), rgba(255, 255, 255, 0.9));
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.85), 0 8px 22px rgba(100, 130, 190, 0.16);
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-blank {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4a68a8;
  font-size: 1.75rem;
  font-weight: 700;
}

.avatar-meta {
  margin-top: 16px;
}

.user-name {
  margin: 0;
  color: #1f2e4a;
  font-size: 1.3rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.user-handle {
  display: block;
  margin-top: 4px;
  color: #8390a8;
  font-size: 0.82rem;
}

.login-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-top: 12px;
  padding: 3px 11px;
  border-radius: 999px;
  font-size: 0.74rem;
  font-weight: 600;
}

.login-chip.weixin {
  background: rgba(82, 172, 120, 0.1);
  color: #3d8a58;
}

.login-chip.password {
  background: rgba(83, 120, 214, 0.1);
  color: #4a6cc7;
}

.user-note {
  margin: 20px 0 0;
  color: #8a95ab;
  font-size: 0.84rem;
  line-height: 1.65;
  text-align: center;
}

.aside-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 22px;
}

.nav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  min-height: 40px;
  border-radius: 10px;
  font-size: 0.84rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
}

.nav-btn.ghost {
  background: rgba(255, 255, 255, 0.6);
  color: #5e6e8a;
  border: 1px solid rgba(140, 155, 190, 0.14);
}

.nav-btn.ghost:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #3d4e6a;
}

.nav-btn.solid {
  background: #4f72c4;
  color: #fff;
  box-shadow: 0 8px 20px rgba(79, 114, 196, 0.2);
}

.nav-btn.solid:hover {
  background: #4368b8;
  box-shadow: 0 10px 24px rgba(79, 114, 196, 0.28);
}

/* ── main ── */
.profile-main {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 14px 36px rgba(120, 138, 180, 0.1);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 22px;
}

.section-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #4f72c4;
}

.section-title {
  margin: 0;
  color: #1f2e4a;
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.field-cell {
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(140, 158, 196, 0.1);
  transition: background 0.18s ease;
}

.field-cell:hover {
  background: rgba(255, 255, 255, 0.78);
}

.field-cell.span-2 {
  grid-column: 1 / -1;
}

.field-label {
  display: block;
  margin-bottom: 6px;
  color: #8a95ab;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.field-body {
  display: block;
  color: #1f2e4a;
  font-size: 0.92rem;
  line-height: 1.55;
  word-break: break-word;
}

.field-body.mono {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.8rem;
  color: #3d4e6a;
}

.status-pip {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 5px;
  vertical-align: middle;
  position: relative;
  top: -1px;
}

.status-pip.ok {
  background: #48b872;
  box-shadow: 0 0 6px rgba(72, 184, 114, 0.4);
}

.status-pip.off {
  background: #c4c9d4;
}

.bind-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.bind-btn {
  min-width: 128px;
  min-height: 40px;
  padding: 0 16px;
  border-radius: 10px;
  border: 1px solid transparent;
  font-size: 0.84rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.bind-btn.solid {
  background: #4f72c4;
  color: #fff;
}

.bind-btn.ghost {
  background: rgba(255, 255, 255, 0.6);
  color: #5e6e8a;
  border-color: rgba(140, 155, 190, 0.14);
}

.bind-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.bind-panel {
  display: flex;
  justify-content: center;
}

.bind-qr-image {
  width: 200px;
  height: 200px;
  border: 1px solid rgba(140, 158, 196, 0.25);
  border-radius: 12px;
  padding: 6px;
  background: #fff;
}

.bind-error {
  color: #c0526a;
}

.hidden-input {
  display: none;
}

.profile-edit-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profile-edit-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.profile-edit-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #5b6983;
  font-size: 0.84rem;
  font-weight: 600;
}

.profile-edit-item.span-2 {
  grid-column: 1 / -1;
}

.avatar-upload-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 148px;
  padding: 16px;
  border: 1px dashed rgba(79, 114, 196, 0.28);
  border-radius: 12px;
  background: rgba(248, 250, 255, 0.9);
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.avatar-upload-card.dragging {
  border-color: rgba(79, 114, 196, 0.72);
  background: rgba(236, 242, 255, 0.95);
}

.avatar-upload-card.busy {
  cursor: progress;
}

.avatar-upload-preview-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #3d4e6a;
}

.avatar-upload-preview {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 8px 24px rgba(79, 114, 196, 0.18);
}

.avatar-upload-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #5e6e8a;
  text-align: center;
}

.avatar-upload-empty i {
  font-size: 1.25rem;
}

.profile-edit-input {
  width: 100%;
  min-height: 40px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid rgba(140, 158, 196, 0.24);
  background: rgba(255, 255, 255, 0.88);
  color: #1f2e4a;
}

.profile-edit-input:focus {
  outline: none;
  border-color: rgba(79, 114, 196, 0.5);
  box-shadow: 0 0 0 3px rgba(79, 114, 196, 0.12);
}

.profile-edit-error {
  color: #c0526a;
  font-size: 0.84rem;
  font-weight: 600;
}

/* ── responsive ── */
@media (max-width: 780px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .field-grid {
    grid-template-columns: 1fr;
  }

  .profile-edit-grid {
    grid-template-columns: 1fr;
  }

  .field-cell.span-2 {
    grid-column: auto;
  }
}
</style>
