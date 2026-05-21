<template>
  <teleport to="body">
    <div v-if="visible" class="floating-effects-container" :class="currentMode" aria-hidden="true">
      <div
        v-for="particle in particles"
        :key="particle.id"
        class="particle"
        :class="particle.modeClass"
        :style="particle.style"
      >
        {{ particle.char }}
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const visible = ref(false)
const particles = ref([])
const currentMode = ref('snow')

let particleId = 0
let spawnTimer = null

const EFFECT_MODES = ['snow', 'petal', 'star', 'clover', 'ribbon']

const effectConfigs = {
  snow: {
    chars: ['❄', '❅', '❆', '✻', '✼'],
    sizeMin: 12,
    sizeMax: 22,
    durationMin: 6,
    durationMax: 10,
    color: 'rgba(255, 255, 255, 0.85)',
    shadow: '0 0 4px rgba(255, 255, 255, 0.4)',
    interval: 600,
    batch: 16,
  },
  petal: {
    chars: ['🌸', '🌺', '🌷', '🍃', '🌿'],
    sizeMin: 14,
    sizeMax: 24,
    durationMin: 8,
    durationMax: 14,
    color: 'rgba(255, 183, 197, 0.9)',
    shadow: '0 0 6px rgba(255, 182, 193, 0.5)',
    interval: 800,
    batch: 12,
  },
  star: {
    chars: ['⭐', '✨', '🌟', '✦', '✧'],
    sizeMin: 10,
    sizeMax: 18,
    durationMin: 5,
    durationMax: 9,
    color: 'rgba(255, 215, 0, 0.9)',
    shadow: '0 0 8px rgba(255, 215, 0, 0.6)',
    interval: 500,
    batch: 14,
  },
  clover: {
    chars: ['🍀', '☘', '🌿', '🍃'],
    sizeMin: 14,
    sizeMax: 22,
    durationMin: 9,
    durationMax: 15,
    color: 'rgba(34, 197, 94, 0.9)',
    shadow: '0 0 5px rgba(74, 222, 128, 0.4)',
    interval: 700,
    batch: 12,
  },
  ribbon: {
    chars: ['🎀', '🎊', '🎉', '🪅', '🎏'],
    sizeMin: 12,
    sizeMax: 20,
    durationMin: 5,
    durationMax: 9,
    color: 'rgba(255, 105, 180, 0.9)',
    shadow: '0 0 6px rgba(255, 182, 193, 0.5)',
    interval: 450,
    batch: 16,
  },
}

function pickRandomMode() {
  return EFFECT_MODES[Math.floor(Math.random() * EFFECT_MODES.length)]
}

function createParticle(mode) {
  const config = effectConfigs[mode]
  const id = particleId++
  const char = config.chars[Math.floor(Math.random() * config.chars.length)]
  const left = Math.random() * 100 // vw
  const size = Math.random() * (config.sizeMax - config.sizeMin) + config.sizeMin
  const duration = Math.random() * (config.durationMax - config.durationMin) + config.durationMin
  const delay = Math.random() * 2 // 0-2s
  const opacity = Math.random() * 0.4 + 0.5 // 0.5-0.9

  const style = {
    left: `${left}vw`,
    fontSize: `${size}px`,
    animationDuration: `${duration}s`,
    animationDelay: `${delay}s`,
    opacity: `${opacity}`,
    color: config.color,
    textShadow: config.shadow,
    '--flake-opacity': opacity,
  }

  particles.value.push({ id, char, style, modeClass: `mode-${mode}` })

  // 动画结束后自动移除
  setTimeout(() => {
    particles.value = particles.value.filter((p) => p.id !== id)
  }, (duration + delay) * 1000)
}

function startEffect() {
  const mode = pickRandomMode()
  currentMode.value = mode
  visible.value = true

  const config = effectConfigs[mode]

  // 初始生成一批
  for (let i = 0; i < config.batch; i++) {
    setTimeout(() => createParticle(mode), Math.random() * 2000)
  }

  // 持续生成
  spawnTimer = setInterval(() => createParticle(mode), config.interval)
}

function stopEffect() {
  visible.value = false
  if (spawnTimer) {
    clearInterval(spawnTimer)
    spawnTimer = null
  }
  particles.value = []
}

watch(
  () => store.getters['music/isPlaying'],
  (isPlaying) => {
    if (isPlaying) {
      startEffect()
    } else {
      stopEffect()
    }
  }
)

onUnmounted(() => {
  stopEffect()
})
</script>

<style scoped>
.floating-effects-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 9998;
  overflow: hidden;
}

.particle {
  position: absolute;
  top: -40px;
  animation-timing-function: linear;
  animation-fill-mode: forwards;
  will-change: transform, opacity;
  user-select: none;
}

/* ========== 雪花效果 ========== */
.mode-snow {
  animation-name: snow-fall;
}

@keyframes snow-fall {
  0% {
    transform: translateY(0) translateX(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: var(--flake-opacity, 0.7);
  }
  25% {
    transform: translateY(25vh) translateX(15px) rotate(45deg);
  }
  50% {
    transform: translateY(50vh) translateX(-10px) rotate(90deg);
  }
  75% {
    transform: translateY(75vh) translateX(12px) rotate(135deg);
  }
  90% {
    opacity: var(--flake-opacity, 0.7);
  }
  100% {
    transform: translateY(110vh) translateX(-5px) rotate(180deg);
    opacity: 0;
  }
}

/* ========== 花瓣效果 ========== */
.mode-petal {
  animation-name: petal-fall;
}

@keyframes petal-fall {
  0% {
    transform: translateY(0) translateX(0) rotate(0deg) scale(1);
    opacity: 0;
  }
  8% {
    opacity: var(--flake-opacity, 0.7);
  }
  20% {
    transform: translateY(20vh) translateX(20px) rotate(30deg) scale(1.05);
  }
  40% {
    transform: translateY(40vh) translateX(-15px) rotate(75deg) scale(0.95);
  }
  60% {
    transform: translateY(60vh) translateX(18px) rotate(120deg) scale(1.02);
  }
  80% {
    transform: translateY(80vh) translateX(-8px) rotate(160deg) scale(0.98);
    opacity: var(--flake-opacity, 0.7);
  }
  100% {
    transform: translateY(110vh) translateX(5px) rotate(200deg) scale(0.9);
    opacity: 0;
  }
}

/* ========== 星星效果 ========== */
.mode-star {
  animation-name: star-fall;
}

@keyframes star-fall {
  0% {
    transform: translateY(0) scale(0.5);
    opacity: 0;
  }
  10% {
    opacity: var(--flake-opacity, 0.8);
    transform: translateY(10vh) scale(1);
  }
  20% {
    transform: translateY(20vh) scale(0.8);
  }
  30% {
    transform: translateY(30vh) scale(1.1);
  }
  50% {
    transform: translateY(50vh) scale(0.9);
  }
  70% {
    transform: translateY(70vh) scale(1.05);
  }
  90% {
    opacity: var(--flake-opacity, 0.8);
    transform: translateY(90vh) scale(0.95);
  }
  100% {
    transform: translateY(110vh) scale(0.5);
    opacity: 0;
  }
}

/* ========== 四叶草效果 ========== */
.mode-clover {
  animation-name: clover-fall;
}

@keyframes clover-fall {
  0% {
    transform: translateY(0) translateX(0) rotate(0deg) scale(0.9);
    opacity: 0;
  }
  8% {
    opacity: var(--flake-opacity, 0.8);
  }
  20% {
    transform: translateY(20vh) translateX(10px) rotate(25deg) scale(1);
  }
  40% {
    transform: translateY(40vh) translateX(-8px) rotate(60deg) scale(0.95);
  }
  60% {
    transform: translateY(60vh) translateX(12px) rotate(100deg) scale(1.02);
  }
  80% {
    transform: translateY(80vh) translateX(-6px) rotate(140deg) scale(0.98);
    opacity: var(--flake-opacity, 0.8);
  }
  100% {
    transform: translateY(110vh) translateX(3px) rotate(180deg) scale(0.9);
    opacity: 0;
  }
}

/* ========== 彩带效果 ========== */
.mode-ribbon {
  animation-name: ribbon-fall;
}

@keyframes ribbon-fall {
  0% {
    transform: translateY(0) translateX(0) rotate(-15deg) skewX(0deg);
    opacity: 0;
  }
  8% {
    opacity: var(--flake-opacity, 0.8);
  }
  15% {
    transform: translateY(15vh) translateX(18px) rotate(10deg) skewX(5deg);
  }
  30% {
    transform: translateY(30vh) translateX(-14px) rotate(-20deg) skewX(-8deg);
  }
  45% {
    transform: translateY(45vh) translateX(16px) rotate(25deg) skewX(6deg);
  }
  60% {
    transform: translateY(60vh) translateX(-12px) rotate(-15deg) skewX(-4deg);
  }
  75% {
    transform: translateY(75vh) translateX(10px) rotate(20deg) skewX(8deg);
  }
  90% {
    opacity: var(--flake-opacity, 0.8);
    transform: translateY(90vh) translateX(-6px) rotate(-8deg) skewX(-2deg);
  }
  100% {
    transform: translateY(110vh) translateX(0) rotate(0deg) skewX(0deg);
    opacity: 0;
  }
}


</style>
