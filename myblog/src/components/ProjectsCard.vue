<template>
  <div v-for="project in projects" :key="project.id" class="card projects-card">
    <div class="image-container">
      <ImageInitialFallback
        :src="project.image"
        :name="project.name"
        :alt="project.name || '项目图片'"
        wrapperClass="card-img-frame"
        imageClass="card-img project-cover-media"
        fallbackClass="project-cover-fallback"
      />
      <div class="image-overlay">
        <button
          type="button"
          class="btn btn-light btn-sm view-btn"
          :disabled="!hasProjectDetail(project)"
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
import router from '@/router'
import { getPublicProjects } from '@/services/projectService.js'
import ImageInitialFallback from '@/components/common/ImageInitialFallback.vue'

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

function hasProjectDetail(project) {
  return project?.id !== undefined && project?.id !== null && String(project.id).trim() !== ''
}

function openProject(project) {
  if (!hasProjectDetail(project)) return
  router.push({
    name: 'project-detail',
    params: { id: String(project.id) }
  })
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
  border: none;
}

.image-container {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: rgba(71, 85, 105, 0.06);
}

.card-img-frame,
:deep(.project-cover-media),
:deep(.project-cover-fallback) {
  width: 100%;
  height: 100%;
}

:deep(.project-cover-media) {
  object-fit: cover;
  background: rgba(71, 85, 105, 0.08);
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
