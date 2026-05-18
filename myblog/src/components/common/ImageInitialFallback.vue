<template>
  <div :class="wrapperClass">
    <img
      v-if="showImage"
      :src="normalizedSrc"
      :alt="resolvedAlt"
      :class="imageClass"
      @error="handleError"
    />
    <div v-else :class="mergedFallbackClass">
      <span class="fallback-text">{{ initial }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
  alt: {
    type: String,
    default: ''
  },
  wrapperClass: {
    type: [String, Array, Object],
    default: ''
  },
  imageClass: {
    type: [String, Array, Object],
    default: ''
  },
  fallbackClass: {
    type: [String, Array, Object],
    default: ''
  }
})

const imageFailed = ref(false)

const normalizedSrc = computed(() => (props.src || '').trim())
const normalizedName = computed(() => String(props.name || '').trim())
const resolvedAlt = computed(() => props.alt || normalizedName.value || '图片')
const showImage = computed(() => Boolean(normalizedSrc.value) && !imageFailed.value)
const mergedFallbackClass = computed(() => ['image-initial-fallback', props.fallbackClass])

const initial = computed(() => {
  const first = normalizedName.value.charAt(0)
  if (!first) return '?'
  return /[a-z]/i.test(first) ? first.toUpperCase() : first
})

watch(() => props.src, () => {
  imageFailed.value = false
})

const handleError = () => {
  imageFailed.value = true
}
</script>

<style scoped>
.image-initial-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(226, 232, 240, 0.95) 0%, rgba(203, 213, 225, 0.95) 100%);
  color: #334155;
  user-select: none;
}

.fallback-text {
  font-size: clamp(1rem, 3vw, 2rem);
  font-weight: 700;
  line-height: 1;
}
</style>
