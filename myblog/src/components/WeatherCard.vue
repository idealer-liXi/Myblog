<template>
  <div class="weather-card">
    <template v-if="weather">
      <div class="weather-main">
        <div class="weather-icon-wrap">
          <img :src="iconUrl" :alt="weather.text" class="weather-icon" />
        </div>
        <div class="weather-info">
          <span class="weather-temp">{{ weather.temp }}°</span>
          <span class="weather-text">{{ weather.text }}</span>
        </div>
      </div>
      <div class="weather-details">
        <span class="weather-city"><i class="bi bi-geo-alt"></i> {{ weather.city }}</span>
        <span class="weather-detail-item"><i class="bi bi-droplet"></i> {{ weather.humidity }}%</span>
        <span class="weather-detail-item"><i class="bi bi-wind"></i> {{ weather.windDir }} {{ weather.windScale }}级</span>
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
import { getWeatherByIP, hasApiKey } from '@/services/weatherService.js'

const weather = ref(null)
const error = ref('')

const iconUrl = computed(() => {
  if (!weather.value?.icon) return ''
  return `https://a.hecdn.net/img/common/icon/202106/${weather.value.icon}.png`
})

const loadWeather = async () => {
  if (!hasApiKey()) {
    error.value = '未配置天气密钥'
    return
  }
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
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  font-size: 0.85rem;
  color: #334155;
  flex-shrink: 0;
  margin-bottom: 12px;
}

.weather-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
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

.weather-icon {
  width: 44px;
  height: 44px;
  object-fit: contain;
}

.weather-info {
  display: flex;
  flex-direction: column;
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