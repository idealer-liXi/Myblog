<template>
  <div v-if="projects.length" class="projects-showcase-page">
    <div class="projects-showcase-shell container-fluid">
      <aside class="project-list-panel">
        <div class="project-list-header">
          <i class="bi bi-layers"></i>
          <span>项目列表</span>
        </div>
        <div class="project-list-scroll">
          <button
            v-for="project in projects"
            :key="project.id"
            type="button"
            class="project-list-item"
            :class="{ active: project.id === selectedProject?.id }"
            :data-testid="`project-list-item-${project.id}`"
            @click="openProject(project)"
          >
            <div class="project-list-thumb">
              <ImageInitialFallback
                :src="project.coverImage"
                :name="project.name"
                :alt="project.name"
                wrapperClass="thumb-frame"
                imageClass="thumb-image"
                fallbackClass="thumb-fallback"
              />
            </div>
            <div class="project-list-copy">
              <strong class="project-list-name">{{ project.name }}</strong>
              <span class="project-list-status" :class="statusClass(project.status)">{{ project.status }}</span>
              <div class="project-list-tags">
                <span v-for="tag in project.techStack.slice(0, 3)" :key="tag" class="tag">{{ tag }}</span>
              </div>
            </div>
          </button>
        </div>
      </aside>

      <main class="project-main-stage">
        <div class="project-carousel-card">
          <ProjectCarousel :project-name="selectedProject.name" :images="selectedProject.images" />
        </div>

        <div class="project-detail-card">
          <ProjectInfoPanel
            :project="selectedProject"
            :project-index="selectedProjectIndex + 1"
            :project-count="projects.length"
          />
        </div>
      </main>
    </div>
  </div>

  <div v-else class="projects-showcase-empty">
    <i class="bi bi-folder2-open"></i>
    <p>暂无公开项目</p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ImageInitialFallback from '@/components/common/ImageInitialFallback.vue'
import ProjectCarousel from '@/components/projects/ProjectCarousel.vue'
import ProjectInfoPanel from '@/components/projects/ProjectInfoPanel.vue'
import { getPublicProjectShowcase } from '@/services/projectService.js'

const route = useRoute()
const router = useRouter()
const projects = ref([])

const selectedProjectIndex = computed(() => {
  const matchedIndex = projects.value.findIndex((project) => project.id === route.params.id)
  return matchedIndex >= 0 ? matchedIndex : 0
})

const selectedProject = computed(() => projects.value[selectedProjectIndex.value] || null)

const statusClass = (status) => {
  if (status === '已完成') return 'status-done'
  if (status === '进行中') return 'status-active'
  if (status === '暂停') return 'status-paused'
  return ''
}

watch(
  () => [route.params.id, projects.value.length],
  (routeId) => {
    if (!projects.value.length) return
    const currentRouteId = Array.isArray(routeId) ? routeId[0] : routeId
    const hasMatch = projects.value.some((project) => project.id === currentRouteId)
    if (!hasMatch) {
      router.replace({
        name: 'project-detail',
        params: { id: projects.value[0].id }
      })
    }
  },
  { immediate: true }
)

async function loadProjects() {
  try {
    projects.value = await getPublicProjectShowcase()
  } catch {
    projects.value = []
  }
}

function openProject(project) {
  if (!project || project.id === selectedProject.value?.id) return
  router.push({
    name: 'project-detail',
    params: { id: project.id }
  })
}

onMounted(loadProjects)
</script>

<style scoped>
.projects-showcase-page {
  --showcase-page-gap: 18px;
  --showcase-shell-height: min(700px, calc(100vh - 76px - 36px));
  padding: 18px 0;
  min-height: calc(100vh - 76px);
  display: flex;
  align-items: flex-start;
}

.projects-showcase-shell {
  display: grid;
  grid-template-columns: 320px minmax(580px, 760px);
  gap: 28px;
  align-items: stretch;
  justify-content: center;
  height: var(--showcase-shell-height);
  max-width: 1160px;
  margin: 0 auto;
  width: 100%;
  padding: 0 15px;
}

/* ── Left panel ── */

.project-list-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  height: 100%;
  min-height: 0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.56);
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.08);
}

.project-list-scroll {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.3) transparent;
}

.project-list-scroll::-webkit-scrollbar {
  width: 6px;
}

.project-list-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.project-list-scroll::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.28);
  border-radius: 999px;
}

.project-list-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  padding: 12px 14px;
  margin-bottom: 2px;
  background: linear-gradient(135deg, #1677eb, #3a8bff);
  border-radius: 16px;
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  box-shadow: 0 10px 24px rgba(22, 119, 235, 0.28);
}

.project-list-header i {
  font-size: 1.05rem;
}

.project-list-item {
  display: grid;
  grid-template-columns: 68px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  width: 100%;
  padding: 12px 13px;
  border: 1.5px solid transparent;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.74);
  backdrop-filter: blur(12px);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.project-list-item:hover {
  background: rgba(255, 255, 255, 0.74);
}

.project-list-item.active {
  border-color: #4a86e8;
  background: linear-gradient(135deg, rgba(74, 134, 232, 0.14), rgba(124, 156, 255, 0.08));
}

.project-list-thumb {
  width: 68px;
  height: 68px;
  border-radius: 16px;
  overflow: hidden;
  flex-shrink: 0;
}

.project-list-thumb :deep(.thumb-frame) {
  width: 100%;
  height: 100%;
}

.project-list-thumb :deep(.thumb-image) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.project-list-thumb :deep(.thumb-fallback) {
  border-radius: 16px;
}

.project-list-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow: hidden;
}

.project-list-name {
  color: #0f172a;
  font-size: 0.96rem;
  font-weight: 700;
  line-height: 1.35;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-list-status {
  font-size: 0.73rem;
  font-weight: 600;
  display: inline-block;
  padding: 3px 9px;
  border-radius: 999px;
  width: fit-content;
}

.project-list-status.status-active {
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
}

.project-list-status.status-done {
  background: rgba(74, 134, 232, 0.12);
  color: #2563eb;
}

.project-list-status.status-paused {
  background: rgba(148, 163, 184, 0.18);
  color: #475569;
}

.project-list-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: #1d4ed8;
  font-size: 0.71rem;
  font-weight: 500;
  line-height: 1;
}

/* ── Center stage ── */

.project-main-stage {
  min-width: 0;
  height: 100%;
  min-height: 0;
  display: grid;
  grid-template-rows: 320px minmax(0, 1fr);
  gap: 18px;
}

.project-carousel-card,
.project-detail-card {
  min-height: 0;
}

.project-carousel-card :deep(.project-carousel) {
  height: 100%;
  min-height: 0;
}

.project-detail-card :deep(.project-info-panel) {
  height: 100%;
}

/* ── Empty state ── */

.projects-showcase-empty {
  padding: 160px 16px 60px;
  text-align: center;
  color: #94a3b8;
}

.projects-showcase-empty i {
  font-size: 3rem;
  margin-bottom: 12px;
  display: block;
}

.projects-showcase-empty p {
  font-size: 1.1rem;
  margin: 0;
}

/* ── Responsive ── */

@media (max-width: 1199px) {
  .projects-showcase-shell {
    grid-template-columns: 1fr;
    height: auto;
  }

  .project-list-panel {
    position: static;
    height: auto;
    padding: 0;
    padding-bottom: 8px;
    border: none;
    background: none;
    box-shadow: none;
    backdrop-filter: none;
  }

  .project-list-scroll {
    flex-direction: row;
    overflow-x: auto;
    overflow-y: visible;
    padding-right: 0;
    padding-bottom: 4px;
  }

  .project-list-header {
    display: none;
  }

  .project-list-item {
    min-width: 250px;
  }

  .project-main-stage {
    height: auto;
    grid-template-rows: minmax(280px, 44vh) auto;
  }
}
</style>
