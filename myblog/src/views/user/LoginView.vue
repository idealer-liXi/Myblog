<template>
  <div class="container">
    <div class="row main">
      <div class="col-4 mx-auto">
        <div class="card">
          <div class="card-body">

            <form @submit.prevent="getToken" v-if="viewMode === 'password'">
              <div class="mb-3">
                <label for="username" class="form-label">用户名</label>
                <input type="text" class="form-control" id="username" placeholder="请输入用户名" v-model="username">
              </div>
              <div class="mb-3">
                <label for="password" class="form-label">密码</label>
                <input type="password" class="form-control" id="password" placeholder="请输入密码" v-model="password">
              </div>
              <transition name="error-fade">
                <div class="error_message mb-3" v-if="error_message">
                  <i class="bi bi-exclamation-circle-fill"></i>
                  <span>{{error_message}}</span>
                </div>
              </transition>
              <button type="submit" class="btn btn-primary" :disabled="isSubmitting">登录</button>
              <hr>

              <div class="mb-1">使用其他方式登录</div>
              <button type="button" class="btn btn-outline-secondary mb-1" disabled>使用QQ登录</button>
              <button data-testid="wechat-start-login" type="button" class="btn btn-outline-secondary mb-1" @click="getWechatQRCode" :disabled="isSubmitting">使用微信登录</button>
              <button type="button" class="btn btn-outline-secondary mb-1" @click="onlyGuestLogin">仅游客登录</button>
            </form>

            <div v-else-if="viewMode === 'wechat_qrcode'" class="text-center p-4">
              <h5>微信扫码登录</h5>
              <div v-if="qrCodeUrl" class="qr-container my-3">
                <img :src="qrCodeUrl" alt="微信登录二维码" class="qr-image"/>
              </div>

              <div v-else class="capybaraloader">
                <div class="capybara">
                  <div class="capyhead">
                    <div class="capyear">
                      <div class="capyear2"></div>
                    </div>
                    <div class="capyear"></div>
                    <div class="capymouth">
                      <div class="capylips"></div>
                      <div class="capylips"></div>
                    </div>
                    <div class="capyeye"></div>
                    <div class="capyeye"></div>
                  </div>
                  <div class="capyleg"></div>
                  <div class="capyleg2"></div>
                  <div class="capyleg2"></div>
                  <div class="capy"></div>
                </div>
                <div class="loader">
                  <div class="loaderline"></div>
                </div>
              </div>

              <transition name="error-fade">
                <div class="error_message mt-3" v-if="error_message">
                  <i class="bi bi-exclamation-circle-fill"></i>
                  <span>{{ error_message }}</span>
                </div>
              </transition>

              <button data-testid="wechat-cancel-login" @click="cancelWechatLogin" class="btn btn-secondary mt-3">返回</button>
            </div>

            <div v-else class="text-center p-4 wechat-decision-panel">
              <h5>完成微信登录</h5>
              <img
                v-if="pendingWechatLogin?.avatar"
                :src="pendingWechatLogin.avatar"
                alt="微信头像"
                class="wechat-avatar my-3"
              />
              <p class="mb-1 fw-semibold">{{ pendingWechatLogin?.weixinName || pendingWechatLogin?.displayName || '微信用户' }}</p>
              <p class="decision-tip">该微信尚未绑定站内账号，请完成以下二选一操作</p>

              <transition name="error-fade">
                <div class="error_message mb-3" v-if="error_message">
                  <i class="bi bi-exclamation-circle-fill"></i>
                  <span>{{ error_message }}</span>
                </div>
              </transition>

              <button
                data-testid="wechat-create-account"
                type="button"
                class="btn btn-primary mb-3"
                :disabled="isDecisionSubmitting"
                @click="handleCreateWechatAccount"
              >
                {{ isDecisionSubmitting ? '处理中...' : '创建新账号' }}
              </button>

              <div class="bind-panel text-start">
                <div class="bind-title mb-2">绑定已有账号</div>
                <div class="mb-2">
                  <input v-model="bindUsername" type="text" class="form-control" placeholder="请输入已有用户名" />
                </div>
                <div class="mb-3">
                  <input v-model="bindPassword" type="password" class="form-control" placeholder="请输入已有密码" />
                </div>
                <button
                  type="button"
                  class="btn btn-outline-primary"
                  :disabled="isDecisionSubmitting"
                  @click="handleBindExistingAccount"
                >
                  {{ isDecisionSubmitting ? '处理中...' : '绑定已有账号' }}
                </button>
              </div>

              <button data-testid="wechat-cancel-login" @click="cancelWechatLogin" class="btn btn-secondary mt-3">返回</button>
            </div>

          </div>

        </div>


      </div>
    </div>
  </div>

</template>

<script>
import { onBeforeUnmount, ref } from 'vue'
import router from '@/router'
import { useStore } from 'vuex'
import {
  bindWechatExistingAccount,
  checkWechatLogin,
  createWechatAccount,
  fetchWechatLoginTicket,
  loginWithPassword
} from '@/services/authService'

export default {
  setup(){
    const store = useStore()
    const username = ref('')
    const password = ref('')
    const error_message = ref('')
    const viewMode = ref('password')
    const qrCodeUrl = ref('')
    const isSubmitting = ref(false)
    const isDecisionSubmitting = ref(false)
    const pendingWechatLogin = ref(null)
    const bindUsername = ref('')
    const bindPassword = ref('')
    let pollTimer = null

    const stopPolling = () => {
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    }

    const resetWechatState = ({ keepError = false } = {}) => {
      stopPolling()
      viewMode.value = 'password'
      qrCodeUrl.value = ''
      pendingWechatLogin.value = null
      bindUsername.value = ''
      bindPassword.value = ''
      isDecisionSubmitting.value = false
      if (!keepError) {
        error_message.value = ''
      }
    }

    const finishLogin = async (session) => {
      store.dispatch('weixin_user/applySession', session)
      const redirect = router.currentRoute.value.query.redirect
      if (typeof redirect === 'string' && redirect) {
        await router.push(redirect)
        return
      }
      await router.push({ name: 'home' })
    }

    const getToken = async () => {
      error_message.value = ''

      if (!username.value.trim()) {
        error_message.value = '请输入用户名'
        return
      }
      if (!password.value.trim()) {
        error_message.value = '请输入密码'
        return
      }

      isSubmitting.value = true

      try {
        const session = await loginWithPassword(username.value.trim(), password.value)
        await finishLogin(session)
      } catch (error) {
        error_message.value = error.message || '登录失败'
      } finally {
        isSubmitting.value = false
      }
    }

    const getWechatQRCode = async () => {
      error_message.value = ''
      viewMode.value = 'wechat_qrcode'
      qrCodeUrl.value = ''
      pendingWechatLogin.value = null
      stopPolling()

      try {
        const ticket = await fetchWechatLoginTicket()
        qrCodeUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${ticket}`
        pollTimer = setInterval(async () => {
          try {
            const result = await checkWechatLogin(ticket)
            if (!result) return

            if (result.type === 'pending') {
              stopPolling()
              pendingWechatLogin.value = result.pending
              bindUsername.value = ''
              bindPassword.value = ''
              qrCodeUrl.value = ''
              viewMode.value = 'wechat_decision'
              return
            }

            stopPolling()
            await finishLogin(result.session)
          } catch (error) {
            resetWechatState({ keepError: true })
            error_message.value = error.message || '微信登录失败'
          }
        }, 3000)
      } catch (error) {
        resetWechatState({ keepError: true })
        error_message.value = error.message || '获取微信登录二维码失败'
      }
    }

    const cancelWechatLogin = () => {
      resetWechatState()
    }

    const handleCreateWechatAccount = async () => {
      if (!pendingWechatLogin.value?.pendingToken) {
        resetWechatState({ keepError: true })
        error_message.value = '微信登录状态已失效，请重新扫码'
        return
      }

      error_message.value = ''
      isDecisionSubmitting.value = true

      try {
        const session = await createWechatAccount(pendingWechatLogin.value.pendingToken)
        await finishLogin(session)
      } catch (error) {
        error_message.value = error.message || '创建新账号失败'
      } finally {
        isDecisionSubmitting.value = false
      }
    }

    const handleBindExistingAccount = async () => {
      if (!bindUsername.value.trim()) {
        error_message.value = '请输入用户名'
        return
      }

      if (!bindPassword.value.trim()) {
        error_message.value = '请输入密码'
        return
      }

      if (!pendingWechatLogin.value?.pendingToken) {
        resetWechatState({ keepError: true })
        error_message.value = '微信登录状态已失效，请重新扫码'
        return
      }

      error_message.value = ''
      isDecisionSubmitting.value = true

      try {
        const session = await bindWechatExistingAccount({
          pendingToken: pendingWechatLogin.value.pendingToken,
          username: bindUsername.value.trim(),
          password: bindPassword.value
        })
        await finishLogin(session)
      } catch (error) {
        error_message.value = error.message || '绑定已有账号失败'
      } finally {
        isDecisionSubmitting.value = false
      }
    }

    const onlyGuestLogin = () => router.push({name: 'home'})

    onBeforeUnmount(() => {
      resetWechatState()
    })

    return {
      username,
      password,
      error_message,
      viewMode,
      qrCodeUrl,
      isSubmitting,
      isDecisionSubmitting,
      pendingWechatLogin,
      bindUsername,
      bindPassword,
      getWechatQRCode,
      cancelWechatLogin,
      handleCreateWechatAccount,
      handleBindExistingAccount,
      onlyGuestLogin,
      getToken
    }
  }
}
</script>

<style scoped>
.btn{
  width: 100%;
}
.btn-primary,
.btn-primary:hover,
.btn-primary:active,
.btn-primary:focus,
.btn-primary:disabled {
  color: #fff !important;
  transition: background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.qr-container {
  display: flex;
  justify-content: center;
}

.qr-image {
  width: 200px;
  height: 200px;
  border: 1px solid #ddd;
  padding: 5px;
}

.wechat-decision-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wechat-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #ddd;
}

.decision-tip {
  color: #6c757d;
  font-size: 0.95rem;
  margin-bottom: 1rem;
}

.bind-panel {
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(13, 110, 253, 0.12);
  border-radius: 12px;
  background: rgba(13, 110, 253, 0.04);
}

.bind-title {
  font-weight: 600;
}

.error_message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #dc3545;
  background: rgba(220, 53, 69, 0.08);
  border: 1px solid rgba(220, 53, 69, 0.2);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 0.875rem;
  font-weight: 500;
}

.error_message i {
  font-size: 1rem;
  flex-shrink: 0;
}

.error-fade-enter-active {
  transition: all 0.3s ease;
}

.error-fade-leave-active {
  transition: all 0.2s ease;
}

.error-fade-enter-from,
.error-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.capybaraloader {
  width: 14em;
  height: 10em;
  position: relative;
  z-index: 1;
  --color: rgb(204, 125, 45);
  --color2: rgb(83, 56, 28);
  transform: scale(0.75);
  margin: 0 auto;
}
.capybara {
  width: 100%;
  height: 7.5em;
  position: relative;
  z-index: 1;
}
.loader {
  width: 100%;
  height: 2.5em;
  position: relative;
  z-index: 1;
  overflow: hidden;
}
.capy {
  width: 85%;
  height: 100%;
  background: linear-gradient(var(--color), 90%, var(--color2));
  border-radius: 45%;
  position: relative;
  z-index: 1;
  animation: movebody 1s linear infinite;
}
.capyhead {
  width: 7.5em;
  height: 7em;
  bottom: 0em;
  right: 0em;
  position: absolute;
  background-color: var(--color);
  z-index: 3;
  border-radius: 3.5em;
  box-shadow: -1em 0em var(--color2);
  animation: movebody 1s linear infinite;
}
.capyear {
  width: 2em;
  height: 2em;
  background: linear-gradient(-45deg, var(--color), 90%, var(--color2));
  top: 0em;
  left: 0em;
  border-radius: 100%;
  position: absolute;
  overflow: hidden;
  z-index: 3;
}
.capyear:nth-child(2) {
  left: 5em;
  background: linear-gradient(25deg, var(--color), 90%, var(--color2));
}
.capyear2 {
  width: 100%;
  height: 1em;
  background-color: var(--color2);
  bottom: 0em;
  left: 0.5em;
  border-radius: 100%;
  position: absolute;
  transform: rotate(-45deg);
}
.capymouth {
  width: 3.5em;
  height: 2em;
  background-color: var(--color2);
  position: absolute;
  bottom: 0em;
  left: 2.5em;
  border-radius: 50%;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 0.5em;
}
.capylips {
  width: 0.25em;
  height: 0.75em;
  border-radius: 100%;
  transform: rotate(-45deg);
  background-color: var(--color);
}
.capylips:nth-child(2) {
  transform: rotate(45deg);
}
.capyeye {
  width: 2em;
  height: 0.5em;
  background-color: var(--color2);
  position: absolute;
  bottom: 3.5em;
  left: 1.5em;
  border-radius: 5em;
  transform: rotate(45deg);
}
.capyeye:nth-child(4) {
  transform: rotate(-45deg);
  left: 5.5em;
  width: 1.75em;
}
.capyleg {
  width: 6em;
  height: 5em;
  bottom: 0em;
  left: 0em;
  position: absolute;
  background: linear-gradient(var(--color), 95%, var(--color2));
  z-index: 2;
  border-radius: 2em;
  animation: movebody 1s linear infinite;
}
.capyleg2 {
  width: 1.75em;
  height: 3em;
  bottom: 0em;
  left: 3.25em;
  position: absolute;
  background: linear-gradient(var(--color), 80%, var(--color2));
  z-index: 2;
  border-radius: 0.75em;
  box-shadow: inset 0em -0.5em var(--color2);
  animation: moveleg 1s linear infinite;
}
.capyleg2:nth-child(3) {
  width: 1.25em;
  left: 0.5em;
  height: 2em;
  animation: moveleg2 1s linear infinite 0.075s;
}

@keyframes moveleg {
  0% {
    transform: rotate(-45deg) translateX(-5%);
  }
  50% {
    transform: rotate(45deg) translateX(5%);
  }
  100% {
    transform: rotate(-45deg) translateX(-5%);
  }
}

@keyframes moveleg2 {
  0% {
    transform: rotate(45deg);
  }
  50% {
    transform: rotate(-45deg);
  }
  100% {
    transform: rotate(45deg);
  }
}

@keyframes movebody {
  0% {
    transform: translateX(0%);
  }
  50% {
    transform: translateX(2%);
  }
  100% {
    transform: translateX(0%);
  }
}

.loaderline {
  width: 50em;
  height: 0.5em;
  border-top: 0.5em dashed var(--color2);
  animation: moveline 10s linear infinite;
}

@keyframes moveline {
  0% {
    transform: translateX(0%);
    opacity: 0%;
  }
  5% {
    opacity: 100%;
  }
  95% {
    opacity: 100%;
  }
  100% {
    opacity: 0%;
    transform: translateX(-70%);
  }
}
</style>
