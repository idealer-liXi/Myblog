<template>
  <div class="container">
    <div class="row main">
      <div class="col-4 mx-auto">
        <div class="card">
          <div class="card-body">

            <form @submit.prevent="getToken" v-if="!showQRCode">
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
              <button type="button" class="btn btn-outline-secondary mb-1" @click="getWechatQRCode" :disabled="isSubmitting">使用微信登录</button>
              <button type="button" class="btn btn-outline-secondary mb-1" @click="onlyGuestLogin">仅游客登录</button>
            </form>

            <div v-else class="text-center p-4">
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

              <button @click="cancelWechatLogin" class="btn btn-secondary mt-3">返回</button>
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
import { checkWechatLogin, fetchWechatLoginTicket, loginWithPassword } from '@/services/authService'

export default {
  setup(){
    const store = useStore()
    const username = ref('')
    const password = ref('')
    const error_message = ref('')
    const showQRCode = ref(false)
    const qrCodeUrl = ref('')
    const isSubmitting = ref(false)
    let pollTimer = null

    const stopPolling = () => {
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
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
      showQRCode.value = true
      qrCodeUrl.value = ''
      stopPolling()

      try {
        const ticket = await fetchWechatLoginTicket()
        qrCodeUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${ticket}`
        pollTimer = setInterval(async () => {
          try {
            const session = await checkWechatLogin(ticket)
            if (!session) return

            stopPolling()
            await finishLogin(session)
          } catch (error) {
            stopPolling()
            error_message.value = error.message || '微信登录失败'
            showQRCode.value = false
          }
        }, 3000)
      } catch (error) {
        error_message.value = error.message || '获取微信登录二维码失败'
        showQRCode.value = false
      }
    }

    const cancelWechatLogin = () => {
      stopPolling()
      showQRCode.value = false
      qrCodeUrl.value = ''
      error_message.value = ''
    }

    const onlyGuestLogin = () => router.push({name: 'home'})

    onBeforeUnmount(() => {
      stopPolling()
    })

    return {
      username,
      password,
      error_message,
      showQRCode,
      qrCodeUrl,
      isSubmitting,
      getWechatQRCode,
      cancelWechatLogin,
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
