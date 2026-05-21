<template>
  <section class="project-carousel">
    <div v-if="hasImages" class="carousel-stage">
      <img
        :src="currentImage"
        :alt="`${projectName} 项目截图 ${currentIndex + 1}`"
        class="carousel-image"
        data-testid="project-carousel-image"
      />

      <button
        type="button"
        class="carousel-control prev"
        data-testid="project-carousel-prev"
        :disabled="displayImages.length <= 1"
        @click="goPrev"
      >
        <i class="bi bi-chevron-left"></i>
      </button>

      <button
        type="button"
        class="carousel-control next"
        data-testid="project-carousel-next"
        :disabled="displayImages.length <= 1"
        @click="goNext"
      >
        <i class="bi bi-chevron-right"></i>
      </button>

      <div class="carousel-dots">
        <button
          v-for="(image, index) in displayImages"
          :key="`${image}-${index}`"
          type="button"
          class="carousel-dot"
          :class="{ active: index === currentIndex }"
          :aria-label="`切换到第 ${index + 1} 张截图`"
          @click="currentIndex = index"
        />
      </div>
    </div>

    <div v-else class="carousel-empty" data-testid="project-carousel-empty">
      <i class="bi bi-image"></i>
      <p>暂无项目截图</p>
      <span>{{ projectName }}</span>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  projectName: {
    type: String,
    default: ''
  }
})

const currentIndex = ref(0)

const displayImages = computed(() => props.images.filter(Boolean))
const hasImages = computed(() => displayImages.value.length > 0)
const currentImage = computed(() => displayImages.value[currentIndex.value] || '')

watch(
  () => props.images,
  () => {
    currentIndex.value = 0
  },
  { deep: true }
)

function goPrev() {
  if (!displayImages.value.length) return
  currentIndex.value = (currentIndex.value - 1 + displayImages.value.length) % displayImages.value.length
}

function goNext() {
  if (!displayImages.value.length) return
  currentIndex.value = (currentIndex.value + 1) % displayImages.value.length
}
</script>

<style scoped>
.project-carousel {
  height: 100%;
  min-height: 0;
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.carousel-stage {
  position: relative;
  height: 100%;
  min-height: 0;
  background: #0f172a;
}

.carousel-image {
  width: 100%;
  height: 100%;
  min-height: 0;
  object-fit: cover;
  transition: opacity 0.3s ease;
}

.carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(8px);
  color: #0f172a;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
}

.carousel-control:hover:not(:disabled) {
  background: #fff;
  transform: translateY(-50%) scale(1.08);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.carousel-control:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.prev {
  left: 14px;
}

.next {
  right: 14px;
}

.carousel-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.35);
  backdrop-filter: blur(6px);
}

.carousel-dot {
  width: 8px;
  height: 8px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition: all 0.25s ease;
  padding: 0;
}

.carousel-dot.active {
  background: #fff;
  width: 20px;
}

.carousel-dot:hover:not(.active) {
  background: rgba(255, 255, 255, 0.7);
}

.carousel-empty {
  height: 100%;
  min-height: 0;
  display: grid;
  place-items: center;
  gap: 8px;
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, rgba(226, 232, 240, 0.7) 0%, rgba(203, 213, 225, 0.7) 100%);
  backdrop-filter: blur(12px);
  color: #64748b;
}

.carousel-empty i {
  font-size: 2.5rem;
  color: #94a3b8;
}

.carousel-empty p {
  font-size: 0.95rem;
  margin: 0;
  color: #475569;
}

.carousel-empty span {
  font-size: 0.82rem;
  color: #94a3b8;
}

@media (max-width: 1199px) {
  .project-carousel {
    min-height: 300px;
  }

  .carousel-stage {
    min-height: 300px;
  }

  .carousel-image {
    min-height: 300px;
  }

  .carousel-empty {
    min-height: 300px;
  }
}
</style>
