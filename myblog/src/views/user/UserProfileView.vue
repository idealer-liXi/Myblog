<template>
  <section class="profile-shell">
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
            <router-link :to="{ name: 'backend' }" class="nav-btn solid">
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
            <div class="field-cell">
              <label class="field-label">角色</label>
              <span class="field-body">{{ profile.roles }}</span>
            </div>
            <div class="field-cell">
              <label class="field-label">JWT 状态</label>
              <span class="field-body">
                <span class="status-pip" :class="profile.jwtStatus === '有效' ? 'ok' : 'off'"></span>
                {{ profile.jwtStatus }}
              </span>
            </div>
            <div class="field-cell span-2">
              <label class="field-label">当前头像来源</label>
              <span class="field-body">{{ profile.avatarSourceLabel }}</span>
            </div>
            <div class="field-cell span-2">
              <label class="field-label">头像地址</label>
              <span class="field-body mono">{{ profile.effectiveAvatar || '—' }}</span>
            </div>
            <div class="field-cell span-2">
              <label class="field-label">微信绑定</label>
              <span class="field-body">{{ profile.weixinBound ? '已绑定' : '未绑定' }}</span>
            </div>
            <div class="field-cell span-2">
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
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { useStore } from 'vuex'
import { checkWechatBind, fetchCurrentUser, fetchWechatBindTicket, unbindWechat } from '@/services/authService'
import { hasValidSession } from '@/utils/authSession'

const store = useStore()

const bindError = ref('')
const bindQrCodeUrl = ref('')
const bindPanelVisible = ref(false)
const bindBusy = ref(false)
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
  min-height: calc(100vh - 140px);
  padding: 28px 20px 48px;
}

.profile-stage {
  width: min(1040px, 100%);
  margin: 0 auto;
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

/* ── responsive ── */
@media (max-width: 780px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .field-grid {
    grid-template-columns: 1fr;
  }

  .field-cell.span-2 {
    grid-column: auto;
  }
}
</style>
