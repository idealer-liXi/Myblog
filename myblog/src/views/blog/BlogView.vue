<template>
  <div class="blog-wrapper">
    <div class="container">
      <div class="row main">
        <div class="left-projects d-none d-md-block col-md-3 col-lg-2">
          <WeatherCard/>
          <div class="sidebar-section">
            <div class="section-header">
              <i class="bi bi-folder2-open"></i>
              <span>个人项目</span>
            </div>
            <ProjectsCard/>
          </div>
        </div>
        <div class="main-blog col-12 col-md-9 col-lg-8">
          <MainBlog/>
        </div>
        <div class="right-message d-none d-lg-block col-lg-2">
          <MusicCard/>
          <CalendarCard/>
          <MessageCard/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import MainBlog from '@/components/MainBlog.vue';
import ProjectsCard from "@/components/ProjectsCard.vue";
import WeatherCard from "@/components/WeatherCard.vue";
import MusicCard from "@/components/MusicCard.vue";
import CalendarCard from "@/components/CalendarCard.vue";
import MessageCard from "@/components/MessageCard.vue";
</script>

<style scoped>
.blog-wrapper {
  height: calc(100vh - 76px);
  overflow: hidden;
}

.container {
  max-width: 1500px !important;
  padding-left: 15px !important;
  padding-right: 15px !important;
  margin: 0 auto;
  height: 100%;
}

.main {
  display: flex;
  align-items: flex-start;
  height: 100%;
}

.left-projects {
  height: calc(100vh - 76px);
  overflow-y: auto;
  overscroll-behavior: contain;
  position: sticky;
  top: 76px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.22) transparent;
}

.left-projects::-webkit-scrollbar {
  width: 5px;
}

.left-projects::-webkit-scrollbar-track {
  background: transparent;
}

.left-projects::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.18);
  border-radius: 10px;
}

.left-projects::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.32);
}

.sidebar-section {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 16px 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}

.sidebar-section > *:not(:last-child) {
  margin-bottom: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 8px;
  margin-bottom: 8px;
  background: #007bff;
  border-radius: 10px;
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 15px rgba(0, 123, 255, 0.25);
}

.section-header i {
  font-size: 1.05rem;
}

.main-blog {
  height: calc(100vh - 76px);
  overflow-y: auto;
  overscroll-behavior: contain;
  transition: all 0.3s ease;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.22) transparent;
}

.main-blog::-webkit-scrollbar {
  width: 5px;
}

.main-blog::-webkit-scrollbar-track {
  background: transparent;
}

.main-blog::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.18);
  border-radius: 10px;
}

.main-blog::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.32);
}

.right-message {
  height: calc(100vh - 76px);
  overflow-y: auto;
  overscroll-behavior: contain;
  position: sticky;
  top: 76px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.22) transparent;
}

.right-message::-webkit-scrollbar {
  width: 5px;
}

.right-message::-webkit-scrollbar-track {
  background: transparent;
}

.right-message::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.18);
  border-radius: 10px;
}

.right-message::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.32);
}

.right-message > * {
  margin-bottom: 16px;
}

@media (max-width: 991px) {
  .left-projects {
    position: static;
    height: auto;
    overflow-y: visible;
  }

  .main-blog {
    height: auto;
    overflow-y: visible;
  }

  .right-message {
    height: auto;
    overflow-y: visible;
  }

  .blog-wrapper {
    height: auto;
    overflow: visible;
  }

  .container {
    height: auto;
  }

  .main {
    height: auto;
  }
}
</style>