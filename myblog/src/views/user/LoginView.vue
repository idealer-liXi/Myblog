<template>
  <div class="container">
    <div class="row main">
      <div class="col-4 mx-auto">
        <div class="card">
          <div class="card-body">

            <form @submit.prevent="" v-if="!showQRCode">
              <div class="mb-3">
                <label for="username" class="form-label">用户名</label>
                <input type="text" class="form-control" id="username" placeholder="请输入用户名">
              </div>
              <div class="mb-3">
                <label for="password" class="form-label">密码</label>
                <input type="password" class="form-control" id="password" placeholder="请输入密码">
              </div>
              <div class="error_message mb-1">{{error_message}}</div>
              <button type="submit" class="btn btn-primary">登录</button>
              <hr>

              <div class="mb-1">使用其他方式登录</div>
              <button type="submit" class="btn btn-outline-secondary mb-1">使用QQ登录</button>
              <button type="button" class="btn btn-outline-secondary mb-1" @click="getWechatQRCode">使用微信登录</button>
              <button type="submit" class="btn btn-outline-secondary mb-1">仅游客登录</button>
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

              <button @click="cancelWechatLogin" class="btn btn-secondary mt-3">返回</button>
            </div>

          </div>

        </div>


      </div>
    </div>
  </div>

</template>

<script>
import {ref} from "vue";
import axios from "axios";
import router from "@/router";
import {useStore} from "vuex";

export default {
  setup(){
    const store = useStore()
    let error_message = ref('')
    let showQRCode = ref(false)
    let qrCodeUrl = ref('')

    const getWechatQRCode = async () => {
      try {
        // Show loading state
        showQRCode.value = true
        qrCodeUrl.value = ''
        // 请求获取二维码ticket
        const response = await axios.get('http://localhost:8080/api/v1/login/weixin_qrcode_ticket')

        if(response.data.code === "0000"){
          const ticket = response.data.data
          // 根据ticket生成二维码图片
          qrCodeUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${ticket}`
          // 开始轮询检查用户是否扫描二维码登录
          const intervalId = setInterval(() => {
            checkLoginStatus(ticket, intervalId);
          }, 3000); // 每3秒检查一次
        }else{
          console.log(response.data.info);
        }
      } catch (error) {
        console.error('Error getting wechat QR code:', error)
        error_message.value = '获取微信登录二维码失败'
        showQRCode.value = false
      }
    }

    const checkLoginStatus = async (ticket, intervalId) => {
      try{
        const response = await axios.get(`http://localhost:8080/api/v1/login/check_login?ticket=${ticket}`)

        if(response.data.code === "0000"){
          console.info("login success");
          // 停止轮询
          clearInterval(intervalId);
          // 保存登录 token 到 cookie，设置有效期为30天
          setCookie('openIdToken', response.data.data, 30);
          // 获取用户昵称和头像
          const response1 = await axios.get(`http://localhost:8080/api/v1/login/weixin_user_information?openid=${response.data.data}`)
          if(response1.data.code === "0000"){
            //成功获取用户昵称和头像
            const name = response1.data.data.weixinName;
            const image_url = response1.data.data.weixinImageUrl;
            //1.将昵称和头像持久化
            console.log(response1);
            localStorage.setItem('weixinName',name)
            localStorage.setItem('weixinImageUrl',image_url)
            //2.缓存到vuex中 todo
            store.dispatch('weixin_user/login',{
              weixinName: name,
              weixinImageUrl: image_url
            });

          }else{
            console.error("获取微信用户头像昵称失败");
          }


          //跳转页面
          router.push({name: "home"})
        }else{
          console.log(response.data.info);
        }

      } catch (e) {
        console.error('Error getting wechat QR code:', e)
        error_message.value = '获取微信登录二维码失败'
        showQRCode.value = false
      }
    }

    //保存用户信息
    function setCookie(name, value, days) {
      const date = new Date();
      date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
      const expires = "expires=" + date.toUTCString();
      document.cookie = name + "=" + value + ";" + expires + ";path=/";
    }



    const cancelWechatLogin = () => {
      showQRCode.value = false
      qrCodeUrl.value = ''
      error_message.value = ''
    }

    return {
      error_message,
      showQRCode,
      qrCodeUrl,
      getWechatQRCode,
      cancelWechatLogin
    }
  }
}
</script>

<style scoped>
.btn{
  width: 100%;
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
  color: red;
}

.capybaraloader {
  width: 14em;
  height: 10em;
  position: relative;
  z-index: 1;
  --color: rgb(204, 125, 45);
  --color2: rgb(83, 56, 28);
  transform: scale(0.75);
  margin: 0 auto; /* Added for centering */
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