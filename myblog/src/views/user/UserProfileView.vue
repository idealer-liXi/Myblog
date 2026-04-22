<template>
  <section class="profile-shell">
    <div class="profile-stage">
      <div class="profile-layout">
        <aside class="profile-aside">
          <div class="avatar-block">
            <div class="avatar-ring">
              <img v-if="profile.avatar" :src="profile.avatar" :alt="profile.displayName" class="avatar-img" />
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
            <div class="field-cell span-2">
              <label class="field-label">OpenID</label>
              <span class="field-body mono">{{ profile.openid || '—' }}</span>
            </div>
            <div class="field-cell">
              <label class="field-label">JWT 状态</label>
              <span class="field-body">
                <span class="status-pip" :class="profile.jwtStatus === '有效' ? 'ok' : 'off'"></span>
                {{ profile.jwtStatus }}
              </span>
            </div>
            <div class="field-cell span-2">
              <label class="field-label">头像地址</label>
              <span class="field-body mono">{{ profile.avatar || '—' }}</span>
            </div>
          </div>
        </main>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

const getCookie = (name) => {
  const cookieArr = document.cookie.split(';')
  for (const part of cookieArr) {
    const cookiePair = part.split('=')
    if (name === cookiePair[0].trim()) {
      return decodeURIComponent(cookiePair[1])
    }
  }
  return ''
}

const storeUser = computed(() => store.getters['weixin_user/getUserInfo'] || {})

const profile = computed(() => {
  const username = storeUser.value.username || localStorage.getItem('loginUsername') || ''
  const weixinName = storeUser.value.weixinName || localStorage.getItem('weixinName') || ''
  const avatar = storeUser.value.weixinImageUrl || localStorage.getItem('weixinImageUrl') || ''
  const openid = storeUser.value.openid || getCookie('openIdToken') || ''
  const loginType = storeUser.value.loginType || (openid ? 'weixin' : 'password')
  const displayName = storeUser.value.displayName || weixinName || username || '未命名用户'
  const jwtToken = localStorage.getItem('jwtToken')
  const jwtTokenExpiry = localStorage.getItem('jwtTokenExpiry')
  const isJwtValid = jwtToken && jwtTokenExpiry && Date.now() < Number(jwtTokenExpiry)

  return {
    username,
    weixinName,
    avatar,
    openid,
    displayName,
    initial: displayName.slice(0, 1).toUpperCase(),
    loginType,
    loginTypeLabel: loginType === 'weixin' ? '微信登录' : '账号登录',
    handle: username ? `@${username}` : (openid ? `openid: ${openid.slice(0, 10)}...` : '站点会话用户'),
    description: loginType === 'weixin' ? '已通过微信授权登录' : '已通过用户名密码登录',
    jwtStatus: isJwtValid ? '有效' : '未检测到或已过期',
  }
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
