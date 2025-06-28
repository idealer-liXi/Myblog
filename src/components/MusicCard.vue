<template>
  <div class="music-card">
    <div class="music-header">
      <div class="music-info">
        <div class="music-cover">
          <img :src="currentMusic.image || '/src/assets/images/default-music.jpg'" :alt="currentMusic.name" />
          <div class="play-overlay" v-if="isPlaying">
            <i class="bi bi-music-note-beamed"></i>
          </div>
        </div>
        <div class="music-details">
          <h4 class="music-title">{{ currentMusic.name || '未知歌曲' }}</h4>
          <p class="music-artist">{{ currentMusic.artist || '未知艺术家' }}</p>
        </div>
      </div>
    </div>

    <div class="music-progress">
      <div class="progress-container">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <div class="time-info">
          <span class="current-time">{{ formatTime(currentTime) }}</span>
          <span class="total-time">{{ formatTime(duration) }}</span>
        </div>
      </div>
    </div>

    <div class="music-controls">
      <button class="control-btn prev-btn" @click="previousTrack" :disabled="currentIndex === 0">
        <i class="bi bi-skip-backward-fill"></i>
      </button>
      
      <button class="control-btn play-btn" @click="togglePlay">
        <i v-if="isPlaying" class="bi bi-pause-circle-fill"></i>
        <i v-else class="bi bi-play-circle-fill"></i>
      </button>
      
      <button class="control-btn next-btn" @click="nextTrack" :disabled="currentIndex === musicList.length - 1">
        <i class="bi bi-skip-forward-fill"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 音乐列表数据
const musicList = ref([
  {
    id: 1,
    name: '红色高跟鞋',
    artist: '蔡健雅',
    image: '/src/assets/images/music1.jpg',
    path: '/src/assets/music/song1.mp3',
    duration: 240
  },
  {
    id: 2,
    name: '夜曲',
    artist: '周杰伦',
    image: '/src/assets/images/music2.jpg',
    path: '/src/assets/music/song2.mp3',
    duration: 210
  },
  {
    id: 3,
    name: '青花瓷',
    artist: '周杰伦',
    image: '/src/assets/images/music3.jpg',
    path: '/src/assets/music/song3.mp3',
    duration: 235
  }
])

// 响应式数据
const currentIndex = ref(0)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const audio = ref(null)

// 计算属性
const currentMusic = computed(() => musicList.value[currentIndex.value])
const progress = computed(() => {
  return duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0
})

// 方法
const togglePlay = () => {
  if (!audio.value) return
  
  if (isPlaying.value) {
    audio.value.pause()
  } else {
    audio.value.play()
  }
  isPlaying.value = !isPlaying.value
}

const previousTrack = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    loadTrack()
  }
}

const nextTrack = () => {
  if (currentIndex.value < musicList.value.length - 1) {
    currentIndex.value++
    loadTrack()
  }
}

const loadTrack = () => {
  if (!audio.value) return
  
  audio.value.src = currentMusic.value.path
  audio.value.load()
  duration.value = currentMusic.value.duration
  currentTime.value = 0
  isPlaying.value = false
}

// 音频事件监听
const handleTimeUpdate = () => {
  if (audio.value) {
    currentTime.value = audio.value.currentTime
  }
}

const handleEnded = () => {
  isPlaying.value = false
  nextTrack()
}

// 生命周期
onMounted(() => {
  audio.value = new Audio()
  audio.value.addEventListener('timeupdate', handleTimeUpdate)
  audio.value.addEventListener('ended', handleEnded)
  loadTrack()
})

onUnmounted(() => {
  if (audio.value) {
    audio.value.removeEventListener('timeupdate', handleTimeUpdate)
    audio.value.removeEventListener('ended', handleEnded)
    audio.value.pause()
  }
})

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.music-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  padding: 12px;
  color: white;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  margin: 0 auto;
}

.music-header {
  margin-bottom: 15px;
}

.music-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.music-cover {
  position: relative;
  width: 65px;
  height: 65px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.music-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}

.music-details {
  flex: 1;
}

.music-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 5px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.music-artist {
  font-size: 0.9rem;
  opacity: 0.8;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.music-progress {
  margin-bottom: 2px;
}

.progress-container {
  position: relative;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff6b6b, #feca57);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.time-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  opacity: 0.8;
}

.music-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 22px;
  margin-bottom: 2px;
  margin-top: 0;
  padding: 0;
  height: unset;
  min-height: unset;
  line-height: 1;
}

.control-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.control-btn:hover:not(:disabled) {
  transform: scale(1.1);
  filter: brightness(1.2);
}

.control-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.prev-btn, .next-btn {
  font-size: 1.1rem;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  padding: 0;
  margin: 0;
}

.play-btn {
  font-size: 1.7rem;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  padding: 0;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .music-card {
    padding: 10px;
    margin: 10px;
    max-width: 100%;
  }
  
  .music-cover {
    width: 55px;
    height: 55px;
  }
  
  .music-title {
    font-size: 1.1rem;
  }
  
  .play-btn {
    font-size: 1.2rem;
    width: 35px;
    height: 35px;
  }
  
  .prev-btn, .next-btn {
    font-size: 0.8rem;
    width: 25px;
    height: 25px;
  }
}
</style>
