<template>
  <section class="blog-stage">
    <div class="blog-card">
      <nav class="tab-bar">
        <router-link
          v-for="(theme, i) in themes"
          :key="theme"
          :to="{ path: '/blog/article/' + theme }"
          class="tab-pill"
          :class="{ active: $route.path === '/blog/article/' + theme }"
          :style="{ '--i': i }"
        >
          <i :class="themeIcon(theme)"></i>
          <span>{{ theme }}</span>
        </router-link>
      </nav>

      <div class="content-area">
        <router-view></router-view>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const themes = ref(["java", "python", "c++", "vue"])

const themeIcon = (theme) => {
  const icons = {
    'java': 'bi bi-cup-hot-fill',
    'python': 'bi bi-stack',
    'c++': 'bi bi-cpu-fill',
    'vue': 'bi bi-palette2'
  }
  return icons[theme] || 'bi bi-code-slash'
}
</script>

<style scoped>
.blog-stage {
  position: relative;
}

.blog-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 20px;
  box-shadow: 0 14px 36px rgba(120, 138, 180, 0.1);
  overflow: hidden;
}

/* ── tab bar ── */
.tab-bar {
  display: flex;
  gap: 6px;
  padding: 14px 16px 0;
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 38px;
  padding: 0 16px;
  border-radius: 10px 10px 0 0;
  color: #7e8da6;
  font-weight: 600;
  font-size: 0.84rem;
  text-decoration: none;
  transition: color 0.2s, background 0.2s;
}

.tab-pill i {
  font-size: 0.88rem;
}

.tab-pill:hover {
  color: #4a5d7a;
  background: rgba(255, 255, 255, 0.5);
  text-decoration: none;
}

.tab-pill.active {
  color: #1f2e4a;
  background: rgba(255, 255, 255, 0.72);
}

/* ── content ── */
.content-area {
  border-top: 1px solid rgba(140, 158, 196, 0.12);
  padding: 20px 20px 14px;
  background: rgba(255, 255, 255, 0.38);
  min-height: 200px;
}

@media (max-width: 720px) {
  .tab-bar {
    padding: 12px 12px 0;
    gap: 4px;
  }

  .tab-pill {
    height: 34px;
    padding: 0 12px;
    font-size: 0.8rem;
    gap: 5px;
  }

  .content-area {
    padding: 16px 14px 10px;
  }
}
</style>
