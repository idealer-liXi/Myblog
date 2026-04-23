<template>
  <div class="backend-container">
    <BackendSidebar />
    <div class="backend-main">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import BackendSidebar from '@/components/backend/BackendSidebar.vue'
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
