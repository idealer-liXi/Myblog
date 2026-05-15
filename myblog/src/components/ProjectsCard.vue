<template>
  <div v-for="project in projects" :key="project.id" class="card projects-card">
    <div class="image-container">
      <img :src="project.image || fallbackImage" class="card-img" alt="项目图片">
      <div class="image-overlay">
        <button
          type="button"
          class="btn btn-light btn-sm view-btn"
          :disabled="!getProjectLink(project)"
          @click="openProject(project)"
        >
          查看详情
        </button>
      </div>
    </div>
    <div class="card-body">
      <h5 class="project-name">{{ project.name }}</h5>
      <div class="project-tags">
        <span v-for="tag in getProjectTags(project.techStack)" :key="`${project.id}-${tag}`" class="tag">{{ tag }}</span>
        <span v-if="getProjectTags(project.techStack).length === 0" class="tag">{{ project.status || '项目' }}</span>
      </div>
    </div>
  </div>

  <div v-if="!loading && projects.length === 0" class="empty-projects">
    暂无公开项目
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getPublicProjects } from '@/services/projectService.js'

const fallbackImage = 'https://picsum.photos/seed/project-fallback/500/320'
const projects = ref([])
const loading = ref(false)

function getProjectTags(techStack) {
  if (!techStack) return []
  return techStack
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
    .slice(0, 3)
}

function getProjectLink(project) {
  return project.previewUrl || project.projectUrl || project.githubUrl || ''
}

function openProject(project) {
  const url = getProjectLink(project)
  if (!url) return
  window.open(url, '_blank', 'noopener,noreferrer')
}

async function loadProjects() {
  loading.value = true
  try {
    projects.value = await getPublicProjects()
  } catch {
    projects.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadProjects)
</script>

<style scoped>
.projects-card {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
  border: none;
}

.projects-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.15);
}

.image-container {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: rgba(71, 85, 105, 0.06);
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
  background: rgba(71, 85, 105, 0.08);
}

.projects-card:hover .card-img {
  transform: scale(1.1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.projects-card:hover .image-overlay {
  opacity: 1;
}

.view-btn {
  padding: 6px 14px;
  font-weight: 500;
  transform: translateY(20px);
  transition: transform 0.3s ease;
}

.projects-card:hover .view-btn {
  transform: translateY(0);
}

.view-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.card-body {
  padding: 15px;
  background: white;
}

.project-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  background: rgba(71, 85, 105, 0.08);
  color: #475569;
  font-size: 0.8rem;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.empty-projects {
  text-align: center;
  color: #7b8794;
  padding: 18px 10px;
  font-size: 0.9rem;
}
</style>
