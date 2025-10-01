<template>
  <div class="container">
    <div class="row main">
      <div class="col-4 mx-auto">
        <div class="card">
          <div class="card-body">

            <form @submit.prevent="">
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
              <button type="submit" class="btn btn-outline-secondary mb-1">使用微信登录</button>
              <button type="submit" class="btn btn-outline-secondary mb-1">仅游客登录</button>
            </form>

          </div>

        </div>


      </div>
    </div>
  </div>

</template>

<script>
import {ref} from "vue";

export default {
  setup(){

    let error_message = ref('')

    return {
      error_message
    }

  }

}
</script>

<style scoped>


.btn{
  width: 100%;
}
</style>