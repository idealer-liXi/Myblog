<template>
  <nav class="navbar navbar-expand-lg bg-body-tertiary" >
    <div class="container-fluid">
      <router-link class="navbar-brand ms-3"  :to="{name: 'blog'}">IdealBlog</router-link>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNavDropdown">
        <ul class="navbar-nav">
          <li class="nav-item">
            <router-link :class="route_value === 'blog'?'nav-link active':'nav-link'" aria-current="page" :to="{name: 'blog'}">首页</router-link>
          </li>
          <li class="nav-item">
            <router-link :class="route_value === 'me'?'nav-link active':'nav-link'" :to="{name: 'me'}">个人介绍</router-link>
          </li>
          <li class="nav-item">
            <router-link :class="route_value === 'backend'?'nav-link active':'nav-link'" :to="{name: 'backend'}">后台管理</router-link>
          </li>

        </ul>

        <ul class="navbar-nav ms-auto me-4">
          <li class="nav-item me-3">
            <button class="btn btn-primary" @click="login">登录</button>
          </li>
          <li class="nav-item ">
            <button class="btn btn-primary" @click="register">注册</button>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script>

import {useRoute} from "vue-router";
import {computed} from "vue";
import router from "@/router";

export default {
  setup(){
    const route = useRoute()

    let route_value = computed(()=>{
      return route.name;
    })

    const login = () => {
      router.push({name: "login"})
    }

    const register = () => {
      router.push({name: "register"})
    }



    return {
      route_value,
      login,
      register,
    }

  }
}
</script>

<style scoped>
.navbar-brand {
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
  font-size: 1.5rem;
  background: linear-gradient(45deg, #4facfe 0%, #00f2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.navbar-brand:hover {
  transform: scale(1.05);
  text-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
</style>