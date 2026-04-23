<template>
  <Navbar></Navbar>
  <router-view/>
  <Footer></Footer>
</template>

<script>
import { onMounted } from 'vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import router from '@/router'
import { useStore } from 'vuex'
import { clearSession, hasValidSession, readSession } from '@/utils/authSession'
import { fetchCurrentUser } from '@/services/authService'

export default {
  components: {
    Navbar,
    Footer,
  },
  setup() {
    const store = useStore()

    onMounted(async () => {
      if (!hasValidSession()) {
        store.dispatch('weixin_user/logout')
        return
      }

      const session = readSession()
      store.dispatch('weixin_user/applySession', session)

      try {
        const currentUser = await fetchCurrentUser()
        store.dispatch('weixin_user/updateProfile', currentUser)
      } catch (error) {
        clearSession()
        store.dispatch('weixin_user/logout')

        if (router.currentRoute.value.meta?.requiresAuth) {
          router.replace({
            name: 'login',
            query: { redirect: router.currentRoute.value.fullPath }
          })
        }
      }
    })

    return {}
  }
}
</script>

<style>
html {
  min-height: 100%;
  overscroll-behavior: none;
  background: linear-gradient(120deg, #a8edea 0%, #fed6e3 100%) no-repeat center center fixed !important;
  background-size: cover !important;
}

body {
  margin: 0;
  padding: 0;
  min-height: 100%;
  overflow-x: hidden;
  overscroll-behavior: none;
  background: linear-gradient(120deg, #a8edea 0%, #fed6e3 100%) no-repeat center center fixed !important;
  background-size: cover !important;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-top: 76px;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 10px;
  transition: all 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
