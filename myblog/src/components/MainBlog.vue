<template>
  <div class="blog-card">
    <div class="blog-card-body">
      <div class="tab-nav">
        <router-link
          v-for="theme in themes"
          :key="theme"
          :to="{ path: '/blog/article/' + theme }"
          class="tab-item"
          :class="{ active: $route.path === '/blog/article/' + theme }"
        >
          <i :class="themeIcon(theme)"></i>
          <span>{{ theme }}</span>
        </router-link>
      </div>
      <div class="tab-divider"></div>
      <router-view></router-view>
    </div>
  </div>
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
.blog-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.blog-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.blog-card-body {
  padding: 20px 24px;
}

.tab-nav {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 10px;
  color: #666;
  font-weight: 500;
  font-size: 0.92rem;
  text-decoration: none;
  transition: color 0.3s ease;
  position: relative;
}

.tab-item:hover {
  color: #475569;
  text-decoration: none;
}

.tab-item.active {
  color: #334155;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: linear-gradient(135deg, #475569 0%, #334155 100%);
  border-radius: 2px;
}

.tab-item i {
  font-size: 1rem;
}

.tab-item span {
}

.tab-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(71, 85, 105, 0.15) 50%, transparent 100%);
  margin: 16px 0;
}

@media (max-width: 576px) {
  .blog-card-body {
    padding: 14px 16px;
  }

  .tab-item {
    padding: 6px 12px;
    font-size: 0.85rem;
  }

  .tab-nav {
    gap: 4px;
  }
}
</style>