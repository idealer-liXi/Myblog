<template>
  <nav class="navbar navbar-expand-lg glass-nav">
    <div class="container-fluid">
      <router-link class="navbar-brand" :to="{name: 'blog'}">
        <span class="brand-icon">
          <i class="bi bi-code-square"></i>
        </span>
        <span class="brand-text">IdealBlog</span>
      </router-link>
      
      <button class="navbar-toggler custom-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
        <span class="toggler-icon">
          <i class="bi bi-list"></i>
        </span>
      </button>
      
      <div class="collapse navbar-collapse" id="navbarNavDropdown">
        <ul class="navbar-nav nav-links">
          <li class="nav-item">
            <router-link :class="$route.path.startsWith('/blog')?'nav-link active':'nav-link'" aria-current="page" :to="{name: 'blog'}">
              <i class="bi bi-house-door"></i>
              <span>首页</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link :class="route_value === 'me'?'nav-link active':'nav-link'" :to="{name: 'me'}">
              <i class="bi bi-person"></i>
              <span>站主介绍</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link :class="route_value === 'githubstar'?'nav-link active':'nav-link'" :to="{name: 'githubstar'}">
              <i class="bi bi-github"></i>
              <span>GitHub项目</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link :class="$route.path.startsWith('/projects') ? 'nav-link active' : 'nav-link'" :to="{ name: 'projects' }">
              <i class="bi bi-kanban"></i>
              <span>项目展示</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link :class="route_value === 'message-board' ? 'nav-link active' : 'nav-link'" :to="{ name: 'message-board' }">
              <i class="bi bi-chat-left-text"></i>
              <span>留言板</span>
            </router-link>
          </li>
          <li v-if="isAdmin" class="nav-item">
            <router-link :class="$route.path.startsWith('/backend')?'nav-link active':'nav-link'" :to="{name: 'backend'}">
              <i class="bi bi-gear"></i>
              <span>后台管理</span>
            </router-link>
          </li>
        </ul>

        <button v-if="hasMusic && !isPlaying" class="nav-music-btn" @click="togglePlay" title="播放音乐">
          <i class="bi bi-music-note"></i>
        </button>

        <div v-if="hasMusic && isPlaying" class="nav-mini-player nav-mini-player-playing">
          <button class="mini-player-prev" @click="prevTrack" :disabled="currentTrackIndex === 0" title="上一首">
            <i class="bi bi-skip-backward-fill"></i>
          </button>
          <button class="mini-player-toggle" @click="togglePlay" title="暂停">
            <i class="bi bi-pause-fill"></i>
          </button>
          <button class="mini-player-next" @click="nextTrack" :disabled="currentTrackIndex === musicList.length - 1" title="下一首">
            <i class="bi bi-skip-forward-fill"></i>
          </button>
          <span class="mini-player-info">
            <span class="mini-player-name">{{ currentMusicName }}</span>
            <span class="mini-player-artist">{{ currentMusicArtist }}</span>
          </span>
        </div>

        <ul v-if="!isLoggedIn" class="navbar-nav auth-section ms-auto">
          <li class="nav-item">
            <button class="btn btn-login" @click="login">
              <i class="bi bi-box-arrow-in-right"></i>
              <span>登录</span>
            </button>
          </li>
          <li class="nav-item">
            <button class="btn btn-register" @click="register">
              <i class="bi bi-person-plus"></i>
              <span>注册</span>
            </button>
          </li>
        </ul>
        
        <ul v-else class="navbar-nav user-section ms-auto">
          <li class="nav-item user-entry">
            <button class="user-profile-button" type="button" @click="openProfile">
              <div class="user-avatar-wrapper">
                <img v-if="avatarSrc" :src="avatarSrc" alt="User Avatar" class="user-avatar">
                <div v-else class="user-avatar-fallback">{{ userInitial }}</div>
                <span class="online-status"></span>
              </div>
              <span class="user-name">{{ displayName }}</span>
              <i class="bi bi-chevron-right user-entry-arrow"></i>
            </button>
          </li>
          <li class="nav-item">
            <button class="btn btn-logout" @click="logout" title="退出登录">
              <i class="bi bi-box-arrow-right"></i>
            </button>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script>
import { useRoute } from "vue-router";
import { computed } from "vue";
import router from "@/router";
import { useStore } from "vuex";
import audioPlayer from "@/services/audioPlayer.js";

export default {
  setup() {
    const route = useRoute();
    const store = useStore();

    const route_value = computed(() => route.name);
    const userInfo = computed(() => store.getters["weixin_user/getUserInfo"] || {});
    const isLoggedIn = computed(() => store.getters["weixin_user/getLoginStatus"])
    const isAdmin = computed(() => store.getters["weixin_user/getRoles"].includes('ADMIN'));
    const displayName = computed(() => store.getters['weixin_user/getDisplayName'])
    const avatarSrc = computed(() => store.getters['weixin_user/getAvatar'])
    const userInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())

    const hasMusic = computed(() => store.getters['music/hasMusic'])
    const isPlaying = computed(() => store.getters['music/isPlaying'])
    const currentTrackIndex = computed(() => store.getters['music/currentTrackIndex'])
    const musicList = computed(() => store.getters['music/musicList'])
    const currentMusic = computed(() => store.getters['music/currentMusic'] || {})
    const currentMusicName = computed(() => currentMusic.value?.name || '')
    const currentMusicArtist = computed(() => currentMusic.value?.artist || '')

    const togglePlay = () => {
      audioPlayer.togglePlay()
    }

    const prevTrack = () => {
      audioPlayer.previous()
    }

    const nextTrack = () => {
      audioPlayer.next()
    }

    const login = () => {
      router.push({ name: "login" });
    };

    const register = () => {
      router.push({ name: "register" });
    };

    const logout = () => {
      store.dispatch("weixin_user/logout");
      router.push({ name: "blog" });
    };

    const openProfile = () => {
      router.push({ name: 'user-profile' })
    }

    return {
      route_value,
      isLoggedIn,
      isAdmin,
      userInfo,
      displayName,
      avatarSrc,
      userInitial,
      hasMusic,
      isPlaying,
      currentTrackIndex,
      musicList,
      currentMusic,
      currentMusicName,
      currentMusicArtist,
      togglePlay,
      prevTrack,
      nextTrack,
      login,
      register,
      logout,
      openProfile,
    };
  },
};
</script>

<style scoped>
.glass-nav {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08), inset 0 0 0 1px rgba(255, 255, 255, 0.3);
  padding: 0.55rem 1.2rem;
  position: fixed;
  top: 12px;
  left: 16px;
  right: 16px;
  z-index: 1000;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  transition: all 0.3s ease;
  text-decoration: none !important;
}

.navbar-brand:hover {
  background: rgba(0, 123, 255, 0.08);
}

.brand-icon {
  width: 38px;
  height: 38px;
  background: #007bff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  box-shadow: 0 4px 15px rgba(0, 123, 255, 0.3);
  transition: all 0.3s ease;
}

.navbar-brand:hover .brand-icon {
  transform: rotate(-10deg) scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 123, 255, 0.4);
}

.brand-text {
  font-family: 'Poppins', 'Noto Sans SC', sans-serif;
  font-weight: 700;
  font-size: 1.4rem;
  color: #007bff;
}

.nav-links {
  gap: 0.5rem;
  margin-left: 0.5rem;
}

.nav-item {
  position: relative;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0.6rem 1rem !important;
  color: #555 !important;
  font-weight: 500;
  font-size: 0.95rem;
  border-radius: 10px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 123, 255, 0.08);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.nav-link:hover::before,
.nav-link.active::before {
  opacity: 1;
}

.nav-link:hover,
.nav-link.active {
  color: #007bff !important;
}

.nav-link:hover {
  transform: none;
}

.nav-link i {
  font-size: 1.1rem;
  transition: transform 0.3s ease;
}

.nav-link:hover i,
.nav-link.active i {
  transform: scale(1.2);
}

.nav-link.active {
  font-weight: 500;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: #007bff;
  border-radius: 2px;
}

.custom-toggler {
  border: none;
  padding: 0.5rem;
  border-radius: 10px;
  background: rgba(0, 123, 255, 0.08);
  transition: all 0.3s ease;
}

.custom-toggler:hover {
  background: rgba(0, 123, 255, 0.15);
}

.toggler-icon {
  color: #007bff;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
}

.navbar-toggler:focus {
  box-shadow: none;
}

/* Music button (collapsed state) */
.nav-music-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(168, 237, 234, 0.35);
  color: #4a86e8;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-left: 0.5rem;
  flex-shrink: 0;
}

.nav-music-btn:hover {
  background: rgba(168, 237, 234, 0.6);
  transform: scale(1.1);
}

/* Mini player (expanded state) */
.nav-mini-player {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px 10px 4px 6px;
  border-radius: 999px;
  background: rgba(168, 237, 234, 0.35);
  border: 1px solid rgba(74, 134, 232, 0.15);
  margin-left: 0.5rem;
  flex-shrink: 0;
  animation: mini-player-in 0.3s ease forwards;
}

@keyframes mini-player-in {
  from {
    opacity: 0;
    transform: scale(0.7);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.mini-player-prev,
.mini-player-toggle,
.mini-player-next {
  border: none;
  background: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4a86e8;
  padding: 0;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.mini-player-prev,
.mini-player-next {
  width: 24px;
  height: 24px;
  font-size: 0.75rem;
  border-radius: 50%;
}

.mini-player-prev:hover:not(:disabled),
.mini-player-next:hover:not(:disabled) {
  background: rgba(74, 134, 232, 0.12);
}

.mini-player-prev:disabled,
.mini-player-next:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.mini-player-toggle {
  width: 28px;
  height: 28px;
  font-size: 0.95rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #a8edea, #fed6e3);
}

.mini-player-toggle:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.mini-player-info {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  max-width: 160px;
  overflow: hidden;
  white-space: nowrap;
  font-size: 0.78rem;
  color: #475569;
  margin-left: 4px;
}

.mini-player-name {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mini-player-artist {
  overflow: hidden;
  text-overflow: ellipsis;
}

.mini-player-artist::before {
  content: '-';
  margin: 0 2px;
}

.auth-section {
  gap: 0.8rem;
  align-items: center;
}

.btn-login,
.btn-register,
.btn-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0.5rem 1.2rem;
  border-radius: 10px;
  font-weight: 500;
  font-size: 0.9rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  cursor: pointer;
}

.btn-login {
  background: rgba(0, 123, 255, 0.08);
  color: #007bff;
  border: 1.5px solid rgba(0, 123, 255, 0.25);
}

.btn-login:hover {
  background: #007bff;
  color: white;
  border-color: #007bff;
  box-shadow: 0 4px 16px rgba(0, 123, 255, 0.3);
  transform: translateY(-1px);
}

.btn-login:focus,
.btn-login:active {
  background: #0062cc;
  color: white;
  border-color: #0062cc;
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.3);
  transform: translateY(0);
}

.btn-register {
  background: #007bff;
  color: white;
  box-shadow: 0 2px 12px rgba(0, 123, 255, 0.25);
}

.btn-register:hover {
  background: #0062cc;
  color: white;
  box-shadow: 0 6px 20px rgba(0, 123, 255, 0.35);
  transform: translateY(-1px);
}

.btn-register:focus,
.btn-register:active {
  background: #0056b3;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.3);
  transform: translateY(0);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-entry {
  display: flex;
  align-items: center;
}

.user-profile-button {
  display: inline-flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.35rem 0.55rem 0.35rem 0.4rem;
  border: 1px solid rgba(0, 123, 255, 0.12);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 10px 22px rgba(125, 142, 182, 0.12);
  color: #3f5ea8;
  cursor: pointer;
  transition: all 0.24s ease;
}

.user-profile-button:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14px 28px rgba(125, 142, 182, 0.18);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.4rem 1rem;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 30px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.user-avatar-wrapper {
  position: relative;
  width: 38px;
  height: 38px;
  flex-shrink: 0;
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: block;
}

.user-avatar-fallback {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6090f3, #4c6fcb);
  color: white;
  font-weight: 700;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.online-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #4cd964;
  border-radius: 50%;
  border: 2px solid white;
}

.user-name {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-entry-arrow {
  color: rgba(63, 94, 168, 0.7);
  font-size: 0.8rem;
}

.btn-logout {
  width: 40px;
  height: 40px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
  border-radius: 50%;
}

.btn-logout:hover {
  background: #ff3b30;
  color: white;
  transform: rotate(90deg);
}

.btn-logout:focus {
  box-shadow: none;
}

@media (max-width: 991px) {
  .glass-nav {
    left: 10px;
    right: 10px;
    padding: 0.5rem;
  }

  .nav-links {
    margin-left: 0;
    margin-top: 1rem;
    gap: 0.3rem;
  }

  .navbar-collapse {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 15px;
    margin-top: 0.5rem;
    padding: 1rem;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  }

  .auth-section,
  .user-section {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
  }

  .nav-link::after {
    display: none;
  }

  .nav-mini-player {
    margin-left: 0;
    margin-top: 0.5rem;
    align-self: flex-start;
  }
}

@media (max-width: 576px) {
  .glass-nav {
    left: 6px;
    right: 6px;
    border-radius: 12px;
    top: 8px;
  }

  .brand-text {
    font-size: 1.2rem;
  }

  .brand-icon {
    width: 32px;
    height: 32px;
    font-size: 1rem;
  }

  .nav-link {
    padding: 0.5rem 0.8rem !important;
    font-size: 0.9rem;
  }

  .btn-login,
  .btn-register {
    padding: 0.4rem 1rem;
    font-size: 0.85rem;
  }

  .user-info {
    padding: 0.3rem 0.8rem;
  }

  .user-profile-button {
    width: 100%;
    justify-content: space-between;
  }

  .user-avatar,
  .user-avatar-fallback {
    width: 32px;
    height: 32px;
  }

  .user-avatar-wrapper {
    width: 32px;
    height: 32px;
  }

  .user-name {
    font-size: 0.85rem;
  }

  .mini-player-info {
    max-width: 100px;
  }
}
</style>
