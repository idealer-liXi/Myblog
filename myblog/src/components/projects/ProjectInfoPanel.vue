<template>
  <aside class="project-info-panel">
    <div class="panel-body">
      <div class="project-counter">{{ paddedIndex }} / {{ paddedCount }}</div>
      <h1 class="project-title">{{ project.name }}</h1>

      <div class="project-meta-row">
        <span class="status-badge" :class="statusClass">{{ project.status || '未填写' }}</span>
        <span class="date-range"><i class="bi bi-calendar3"></i> {{ dateRange }}</span>
      </div>

      <div class="project-tags">
        <span v-for="tag in project.techStack" :key="tag" class="tag">{{ tag }}</span>
      </div>

      <p class="project-description">{{ project.description || '暂无项目简介' }}</p>

      <div class="project-actions">
        <a
          v-if="project.previewUrl"
          :href="project.previewUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="btn-action btn-preview"
          data-testid="project-info-preview-link"
        >
          <i class="bi bi-eye"></i>
          在线预览
        </a>
        <a
          v-if="project.githubUrl"
          :href="project.githubUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="btn-action btn-github"
          data-testid="project-info-github-link"
        >
          <i class="bi bi-github"></i>
          GitHub
        </a>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  project: {
    type: Object,
    required: true
  },
  projectIndex: {
    type: Number,
    default: 0
  },
  projectCount: {
    type: Number,
    default: 0
  }
})

const paddedIndex = computed(() => String(props.projectIndex).padStart(2, '0'))
const paddedCount = computed(() => String(props.projectCount).padStart(2, '0'))

const statusClass = computed(() => {
  const s = props.project.status
  if (s === '已完成') return 'status-done'
  if (s === '进行中') return 'status-active'
  if (s === '暂停') return 'status-paused'
  return ''
})

const dateRange = computed(() => {
  const start = props.project.startDate || '未填写'
  const end = props.project.endDate || '至今'
  return props.project.startDate || props.project.endDate ? `${start} - ${end}` : '未填写'
})
</script>

<style scoped>
.project-info-panel {
  border: none;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  height: 100%;
  overflow: hidden;
}

.panel-body {
  padding: 24px;
  height: 100%;
  overflow-y: auto;
}

.project-counter {
  color: #94a3b8;
  font-weight: 700;
  font-size: 0.85rem;
  letter-spacing: 1px;
  margin-bottom: 10px;
}

.project-title {
  font-size: 1.55rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 14px;
  line-height: 1.3;
}

.project-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
}

.status-badge.status-active {
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
}

.status-badge.status-done {
  background: rgba(74, 134, 232, 0.12);
  color: #2563eb;
}

.status-badge.status-paused {
  background: rgba(148, 163, 184, 0.18);
  color: #475569;
}

.date-range {
  color: #64748b;
  font-size: 0.82rem;
  display: flex;
  align-items: center;
  gap: 4px;
}

.date-range i {
  font-size: 0.78rem;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.tag {
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: #1e40af;
  font-size: 0.78rem;
  font-weight: 500;
}

.project-description {
  color: #475569;
  line-height: 1.7;
  font-size: 0.9rem;
  margin-bottom: 20px;
}

.project-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.88rem;
  text-decoration: none;
  transition: all 0.25s ease;
  border: none;
}

.btn-preview {
  background: linear-gradient(135deg, #4a86e8, #7c9cff);
  color: #fff;
  box-shadow: 0 4px 14px rgba(74, 134, 232, 0.3);
}

.btn-preview:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(74, 134, 232, 0.4);
  color: #fff;
  text-decoration: none;
}

.btn-github {
  background: rgba(15, 23, 42, 0.06);
  color: #334155;
  border: 1.5px solid rgba(148, 163, 184, 0.25);
}

.btn-github:hover {
  background: rgba(15, 23, 42, 0.12);
  transform: translateY(-1px);
  color: #0f172a;
  text-decoration: none;
}
</style>
