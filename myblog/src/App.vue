<template>
  <Navbar></Navbar>
  <router-view/>
  <Footer></Footer>
</template>


<script>
import Navbar from "@/components/Navbar.vue";
import Footer from "@/components/Footer.vue";
import { useStore } from "vuex";

export default {
  name: '',
  components: {
    Navbar,
    Footer,
  },
  setup() {
    const store = useStore();

    const getCookie = (name) => {
      const value = `; ${document.cookie}`;
      const parts = value.split(`; ${name}=`);
      if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const weixinName = getCookie("weixinName");
    const weixinImageUrl = getCookie("weixinImageUrl");

    if (weixinName && weixinImageUrl) {
      store.dispatch("weixin_user/login", {
        weixinName: decodeURIComponent(weixinName),
        weixinImageUrl: weixinImageUrl,
      });
    }
  }
}
</script>


<style>

body{
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  min-height: 100vh;
  background: linear-gradient(120deg, #a8edea 0%, #fed6e3 100%);
  background-attachment: fixed;
}

/* 全局滚动条样式 */
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
