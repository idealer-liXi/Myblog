<template>
  <div class="music-card">
    <div class="music-header">
      <div class="music-info">
        <div class="music-cover">
          <ImageInitialFallback
            :src="currentMusic.coverImage || ''"
            :name="currentMusic.name || '音乐'"
            :alt="currentMusic.name || '音乐封面'"
            wrapperClass="music-cover-frame"
            imageClass="music-cover-image"
            fallbackClass="music-cover-fallback"
          />
          <div class="play-overlay" v-if="isPlaying">
            <i class="bi bi-music-note-beamed"></i>
          </div>
        </div>
        <div class="music-details">
          <h4 class="music-title">{{ currentMusic.name || '暂无音乐' }}</h4>
          <p class="music-artist"><i class="bi bi-music-note"></i> {{ currentMusic.artist || '请先在后台添加音乐' }}</p>
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
      <button class="control-btn prev-btn" @click="previousTrack" :disabled="!hasMusic || currentIndex === 0">
        <i class="bi bi-skip-backward-fill"></i>
      </button>

      <button class="control-btn play-btn" @click="togglePlay" :disabled="!hasMusic">
        <i v-if="isPlaying" class="bi bi-pause-circle-fill"></i>
        <i v-else class="bi bi-play-circle-fill"></i>
      </button>

      <button class="control-btn next-btn" @click="nextTrack" :disabled="!hasMusic || currentIndex === musicList.length - 1">
        <i class="bi bi-skip-forward-fill"></i>
      </button>

      <button class="playlist-toggle-btn" @click="showPlaylist = !showPlaylist" :disabled="!hasMusic" :title="showPlaylist ? '收起列表' : '播放列表'">
        <i class="bi" :class="showPlaylist ? 'bi-chevron-up' : 'bi-list-ul'"></i>
      </button>
    </div>

    <div v-if="error" class="music-error">{{ error }}</div>

    <Transition name="playlist">
      <div v-if="showPlaylist && hasMusic" class="playlist-panel">
        <button
          v-for="(music, index) in musicList"
          :key="music.id"
          type="button"
          class="playlist-item"
          :class="{ active: index === currentIndex }"
          @click="selectTrack(index, true)"
        >
          <ImageInitialFallback
            :src="music.coverImage || ''"
            :name="music.name || '音乐'"
            :alt="music.name || '音乐封面'"
            wrapperClass="playlist-cover-frame"
            imageClass="playlist-cover-image"
            fallbackClass="playlist-cover-fallback"
          />
          <span class="playlist-main">
            <strong>{{ music.name }}</strong>
            <small>{{ music.artist }}</small>
          </span>
        </button>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import ImageInitialFallback from '@/components/common/ImageInitialFallback.vue'
import { getPublicMusicList } from '@/services/musicService.js'

const musicList = ref([])
const currentIndex = ref(0)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const audio = ref(null)
const showPlaylist = ref(false)
const error = ref('')

const hasMusic = computed(() => musicList.value.length > 0)
const currentMusic = computed(() => musicList.value[currentIndex.value] || {})
const progress = computed(() => (duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0))

const togglePlay = async () => {
  if (!audio.value || !hasMusic.value) return

  if (isPlaying.value) {
    audio.value.pause()
    isPlaying.value = false
    return
  }

  try {
    await audio.value.play()
    isPlaying.value = true
  } catch {
    isPlaying.value = false
  }
}

const loadTrack = async (autoPlay = false) => {
  if (!audio.value || !hasMusic.value) return

  audio.value.src = currentMusic.value.audioUrl || ''
  audio.value.load()
  currentTime.value = 0
  duration.value = 0
  isPlaying.value = false

  if (autoPlay) {
    try {
      await audio.value.play()
      isPlaying.value = true
    } catch {
      isPlaying.value = false
    }
  }
}

const selectTrack = async (index, autoPlay = false) => {
  if (index < 0 || index >= musicList.value.length) return
  currentIndex.value = index
  await loadTrack(autoPlay)
}

const previousTrack = () => {
  if (currentIndex.value > 0) {
    selectTrack(currentIndex.value - 1, isPlaying.value)
  }
}

const nextTrack = () => {
  if (currentIndex.value < musicList.value.length - 1) {
    selectTrack(currentIndex.value + 1, isPlaying.value)
  }
}

const loadMusic = async () => {
  try {
    error.value = ''
    musicList.value = await getPublicMusicList()
    currentIndex.value = 0
    if (musicList.value.length > 0) {
      await loadTrack(false)
    } else {
      showPlaylist.value = false
    }
  } catch (err) {
    musicList.value = []
    error.value = err?.message || '加载音乐失败'
    showPlaylist.value = false
  }
}

const handleTimeUpdate = () => {
  if (audio.value) {
    currentTime.value = audio.value.currentTime
  }
}

const handleLoadedMetadata = () => {
  if (audio.value) {
    duration.value = Number.isFinite(audio.value.duration) ? audio.value.duration : 0
  }
}

const handleEnded = () => {
  isPlaying.value = false
  if (currentIndex.value < musicList.value.length - 1) {
    selectTrack(currentIndex.value + 1, true)
  }
}

onMounted(async () => {
  audio.value = new Audio()
  audio.value.addEventListener('timeupdate', handleTimeUpdate)
  audio.value.addEventListener('loadedmetadata', handleLoadedMetadata)
  audio.value.addEventListener('ended', handleEnded)
  await loadMusic()
})

onUnmounted(() => {
  if (audio.value) {
    audio.value.removeEventListener('timeupdate', handleTimeUpdate)
    audio.value.removeEventListener('loadedmetadata', handleLoadedMetadata)
    audio.value.removeEventListener('ended', handleEnded)
    audio.value.pause()
  }
})

const formatTime = (seconds) => {
  if (!Number.isFinite(seconds) || seconds <= 0) {
    return '0:00'
  }
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

.music-cover-frame,
.music-cover-image {
  width: 100%;
  height: 100%;
}

.music-cover-image {
  object-fit: cover;
}

.music-cover-fallback {
  border-radius: 15px;
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

.music-title {
  font-size: 0.9rem;
  font-weight: 600;
  margin: 0 0 5px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
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
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #a8edea, #fed6e3);
  border-radius: 10px;
  position: relative;
}

.time-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #666;
  opacity: 0.8;
}

.music-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 2px;
  margin-top: 8px;
  padding: 0;
  height: unset;
  min-height: unset;
  line-height: 1;
  position: relative;
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
  flex-shrink: 0;
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
  flex-shrink: 0;
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
  flex-shrink: 0;
}

.play-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

.playlist-toggle-btn {
  margin-left: auto;
  border: 1px solid rgba(74, 134, 232, 0.18);
  border-radius: 50%;
  width: 32px;
  height: 32px;
  padding: 0;
  background: rgba(168, 237, 234, 0.2);
  color: #4a86e8;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  line-height: 1;
  flex-shrink: 0;
}

.playlist-toggle-btn:hover:not(:disabled) {
  background: rgba(74, 134, 232, 0.16);
  border-color: rgba(74, 134, 232, 0.3);
}

.playlist-toggle-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.music-error {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 0.82rem;
  text-align: center;
  background: rgba(239, 68, 68, 0.1);
  color: #b91c1c;
}

.playlist-panel {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 260px;
  overflow-y: auto;
  padding-right: 4px;
}

.playlist-item {
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.55);
  padding: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  transition: all 0.3s ease;
}

.playlist-item:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.78);
}

.playlist-item.active {
  background: rgba(168, 237, 234, 0.28);
  box-shadow: inset 0 0 0 1px rgba(74, 134, 232, 0.14);
}

.playlist-enter-active,
.playlist-leave-active {
  transition: all 0.28s ease;
  overflow: hidden;
}

.playlist-enter-from,
.playlist-leave-to {
  opacity: 0;
  max-height: 0 !important;
  margin-top: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.playlist-enter-to,
.playlist-leave-from {
  opacity: 1;
  max-height: 260px;
}

.playlist-cover-frame,
.playlist-cover-image {
  width: 46px;
  height: 46px;
  border-radius: 12px;
}

.playlist-cover-image {
  object-fit: cover;
}

.playlist-cover-fallback {
  border-radius: 12px;
}

.playlist-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.playlist-main strong,
.playlist-main small {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.playlist-main strong {
  font-size: 0.85rem;
  color: #334155;
}

.playlist-main small {
  margin-top: 2px;
  font-size: 0.76rem;
  color: #64748b;
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
    gap: 12px;
    margin-top: 5px;
  }

  .playlist-panel {
    max-height: 220px;
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

  .playlist-toggle-btn {
    width: 28px;
    height: 28px;
    font-size: 0.8rem;
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

  .playlist-item {
    padding: 7px;
  }

  .playlist-cover-frame,
  .playlist-cover-image {
    width: 40px;
    height: 40px;
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

  .playlist-toggle-btn {
    width: 25px;
    height: 25px;
    font-size: 0.76rem;
  }
}
</style>
