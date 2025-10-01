<template>
  <div class="card">
    <div class="card-body">
      <ul class="nav nav-underline">
        <li v-for="theme in themes" :key="theme" class="nav-item">
          <router-link class="nav-link" :to="{ path: '/blog/article/' + theme }">{{ theme }}</router-link>
        </li>
      </ul>
      <hr class="m-0">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 响应式数据
const themes = ref(["java", "python", "c++", "vue"])
</script>

<style scoped>
.nav-link {
  color: gray;              /* 普通链接灰色 */
}

.nav-link:hover {
  color: #0077ff;              /* 鼠标覆盖时蓝色 */
}

.router-link-active {
  color: black;           /* 激活链接深黑色 */
}
</style>