<template>
  <div class="backend-container">
    <BackendSidebar />
    <div class="backend-main">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import BackendSidebar from '@/components/backend/BackendSidebar.vue'

const router = useRouter()
const store = useStore()

onMounted(() => {
  const jwtToken = localStorage.getItem('jwtToken')
  const jwtTokenExpiry = localStorage.getItem('jwtTokenExpiry')
  const isLoggedIn = store.getters['weixin_user/getLoginStatus']
  const openid = document.cookie.split(';').some(c => c.trim().startsWith('openIdToken='))

  const isJwtValid = jwtToken && jwtTokenExpiry && Date.now() < Number(jwtTokenExpiry)

  // if (!isJwtValid && !isLoggedIn && !openid) {
  //   router.replace({ name: 'login' })
  // }
})
</script>

<style scoped>
.backend-container {
  display: flex;
  height: calc(100vh - 76px);
  overflow: hidden;
  gap: 20px;
  padding: 20px;
}

.backend-main {
  flex: 1;
  overflow-y: auto;
  min-width: 0;
}

.backend-main::-webkit-scrollbar {
  width: 4px;
}

.backend-main::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 4px;
}

@media (max-width: 768px) {
  .backend-container {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - 76px);
  }

  .backend-main {
    overflow-y: visible;
  }
}
</style>