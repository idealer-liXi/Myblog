<template>
  <div class="weather-card">
    <template v-if="weather">
      <div class="weather-main">
        <div class="weather-icon-wrap">
          <i :class="weatherIconClass" class="weather-icon-glyph" data-testid="weather-local-icon"></i>
        </div>
        <div class="weather-info">
          <span class="weather-temp">{{ weather.temp }}°</span>
          <span class="weather-text">{{ weather.text }}</span>
        </div>
      </div>
      <div class="weather-details">
        <span class="weather-city"><i class="bi bi-geo-alt"></i> {{ weather.city }}</span>
        <span class="weather-detail-item"><i class="bi bi-droplet"></i> {{ weather.humidity }}%</span>
        <span class="weather-detail-item"><i class="bi bi-wind"></i> {{ weather.windScale }}级</span>
      </div>
    </template>
    <template v-else-if="error">
      <div class="weather-error">
        <i class="bi bi-cloud-slash"></i>
        <span>{{ error }}</span>
      </div>
    </template>
    <template v-else>
      <div class="weather-loading">
        <i class="bi bi-arrow-repeat spin"></i>
        <span>加载天气...</span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getWeatherByIP } from '@/services/weatherService.js'

const weather = ref(null)
const error = ref('')

const weatherIconClass = computed(() => {
  const icon = String(weather.value?.icon || '')
  const iconMap = {
    '100': 'bi-brightness-high-fill',
    '101': 'bi-cloud-sun-fill',
    '104': 'bi-clouds-fill',
    '150': 'bi-moon-stars-fill',
    '151': 'bi-cloud-moon-fill',
    '302': 'bi-cloud-lightning-rain-fill',
    '305': 'bi-cloud-rain-fill',
    '400': 'bi-cloud-snow-fill',
    '500': 'bi-cloud-fog2-fill'
  }
  return iconMap[icon] || 'bi-cloud-fill'
})

const loadWeather = async () => {
  try {
    error.value = ''
    const data = await getWeatherByIP()
    if (data) {
      weather.value = data
    } else {
      error.value = '暂无天气数据'
    }
  } catch {
    error.value = '天气获取失败'
  }
}

onMounted(loadWeather)
</script>

<style scoped>
.weather-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 12px 14px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  font-size: 0.85rem;
  color: #334155;
  flex-shrink: 0;
  margin-bottom: 12px;
}

.weather-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.weather-icon-wrap {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.weather-icon-glyph {
  font-size: 2rem;
  line-height: 1;
  color: #4a86e8;
}

.weather-info {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
}

.weather-temp {
  font-size: 1.4rem;
  font-weight: 700;
  line-height: 1.1;
  color: #1e293b;
}

.weather-text {
  font-size: 0.8rem;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.weather-details {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.75rem;
  color: #64748b;
  flex-wrap: wrap;
}

.weather-city {
  font-weight: 600;
  color: #475569;
  display: flex;
  align-items: center;
  gap: 2px;
}

.weather-city i {
  font-size: 0.72rem;
  color: #ef4444;
}

.weather-detail-item {
  display: flex;
  align-items: center;
  gap: 2px;
}

.weather-detail-item i {
  font-size: 0.72rem;
}

.weather-error,
.weather-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 6px 0;
  color: #94a3b8;
  font-size: 0.82rem;
}

.weather-error i {
  font-size: 1rem;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
