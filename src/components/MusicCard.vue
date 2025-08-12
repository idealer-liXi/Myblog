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
          <p class="music-artist"><i class="bi bi-music-note"></i> {{ currentMusic.artist || '未知艺术家' }}</p>
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
  background: rgba(255, 255, 255, 0.8);
  border-radius: 15px;
  padding: 15px;
  color: #333;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  margin: 0 auto;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.music-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.music-header {
  margin-bottom: 15px;
}

.music-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 5px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.music-card:hover .music-info {
  background: rgba(255, 255, 255, 0.7);
  transform: translateY(-2px);
}

.music-cover {
  position: relative;
  width: 70px;
  height: 70px;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.music-card:hover .music-cover {
  transform: scale(1.05);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
}

.music-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.music-card:hover .music-cover img {
  transform: scale(1.1);
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(168, 237, 234, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: pulse 2s infinite;
}

.music-card:hover .play-overlay,
.play-overlay[v-if="isPlaying"] {
  opacity: 1;
}

@keyframes pulse {
  0%, 100% { opacity: 0.7; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

.music-details {
  flex: 1;
  margin-left: 15px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: all 0.3s ease;
}

.music-card:hover .music-details {
  transform: translateX(5px);
}

.music-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 5px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
  transition: color 0.3s ease;
}

.music-card:hover .music-title {
  color: #4a86e8;
}

.music-artist {
  font-size: 0.9rem;
  color: #666;
  opacity: 0.8;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  gap: 5px;
}

.music-artist i {
  font-size: 0.8rem;
  color: #fed6e3;
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
  background: rgba(0, 0, 0, 0.05);
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 8px;
  transition: height 0.3s ease;
  position: relative;
}

.music-card:hover .progress-bar {
  height: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #a8edea, #fed6e3);
  border-radius: 10px;
  transition: all 0.3s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
  opacity: 0;
  transition: all 0.3s ease;
}

.music-card:hover .progress-fill::after {
  width: 12px;
  height: 12px;
  opacity: 1;
}

.time-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #666;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.music-card:hover .time-info {
  opacity: 1;
}

.music-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 22px;
  margin-bottom: 2px;
  margin-top: 8px;
  padding: 0;
  height: unset;
  min-height: unset;
  line-height: 1;
}

.control-btn {
  background: none;
  border: none;
  color: #4a86e8;
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
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(168, 237, 234, 0.2);
  padding: 0;
  margin: 0;
  transition: all 0.3s ease;
}

.prev-btn:hover,
.next-btn:hover {
  background: rgba(168, 237, 234, 0.4);
  transform: scale(1.1);
}

.play-btn {
  font-size: 1.7rem;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a8edea, #fed6e3);
  padding: 0;
  margin: 0;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.play-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .music-card {
    padding: 12px;
    max-width: 450px;
  }
  
  .music-info {
    padding: 3px;
    margin-bottom: 10px;
  }
  
  .music-title {
    font-size: 1rem;
  }
  
  .music-artist {
    font-size: 0.8rem;
  }
  
  .music-controls {
    gap: 15px;
    margin-top: 5px;
  }
  
  .prev-btn, .next-btn {
    width: 28px;
    height: 28px;
    font-size: 0.9rem;
  }
  
  .play-btn {
    width: 34px;
    height: 34px;
    font-size: 1.4rem;
  }
}

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
