<template>
  <div class="container">
    <div class="row main">
      <div class="col-4 mx-auto">
        <div class="card">
          <div class="card-body">
            <form @submit.prevent="">
              <div class="mb-3">
                <label for="username" class="form-label">用户名</label>
                <input type="text" class="form-control" id="username" placeholder="请输入用户名" v-model="username">
              </div>
              <div class="mb-3">
                <label for="password" class="form-label">密码</label>
                <input type="password" class="form-control" id="password" placeholder="请输入密码" v-model="password">
              </div>
              <div v-if="error_message != null" class="error_message mb-1">{{error_message}}</div>
              <button type="submit" class="btn btn-primary" @click="register">注册</button>
            </form>

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

export default {
  setup(){
    let error_message = ref('');
    let username = ref('');
    let password = ref('');

    const register = async () =>{
      error_message.value = ''

      if (!username.value.trim()) {
        error_message.value = '请输入用户名'
        return
      }
      if (!password.value.trim()) {
        error_message.value = '请输入密码'
        return
      }

      try {
        const response = await axios.post('http://localhost:8080/api/r1/register', {
          username: username.value,
          password: password.value
        })

        if (response.data.code === '0000') {
          router.push({ name: 'login' })
        } else {
          error_message.value = response.data.info || '注册失败'
        }
      } catch (error) {
        error_message.value = '注册请求失败，请稍后重试'
      }

    }

    return{
      error_message,
      username,
      password,
      register
    }

  }
}
</script>

<style scoped>
.error_message {
  color: red;
}

.btn{
  width: 100%;
}
</style>