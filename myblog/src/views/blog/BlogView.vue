<template>
  <div class="container">
    <div class="row main">
      <div class="left-projects d-none d-md-block col-md-3 col-lg-2 mx-auto">
        <div class="project-title-card">
          个人项目
        </div>
        <ProjectsCard/>
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
</template>

<script setup>
import MainBlog from '@/components/MainBlog.vue';
import ProjectsCard from "@/components/ProjectsCard.vue";
import MusicCard from "@/components/MusicCard.vue";
import CalendarCard from "@/components/CalendarCard.vue";
import MessageCard from "@/components/MessageCard.vue";
</script>

<style scoped>
.container {
  max-width: 1500px !important;
  padding-left: 10px !important;
  padding-right: 10px !important;
  min-height: 100vh;
  padding-top: 20px;
  padding-bottom: 20px;
}

.main {
  margin-top: 10px;
}

.project-title-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px 0;
  margin-bottom: 12px;
  text-align: center;
  font-size: 1rem;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.left-projects, .main-blog, .right-message {
  margin-bottom: 20px;
}
</style>