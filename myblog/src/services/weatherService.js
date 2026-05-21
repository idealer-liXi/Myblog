const WEATHER_PROXY_URL = 'http://localhost:8080/api/public/weather/current'
const CACHE_KEY = 'weather_cache'
const CACHE_DURATION = 30 * 60 * 1000 // 30分钟，单位毫秒

function getCachedWeather() {
  try {
    const cached = localStorage.getItem(CACHE_KEY)
    if (!cached) return null

    const { data, timestamp } = JSON.parse(cached)
    if (Date.now() - timestamp > CACHE_DURATION) {
      // 缓存已过期，清除
      localStorage.removeItem(CACHE_KEY)
      return null
    }
    return data
  } catch {
    return null
  }
}

function setCachedWeather(data) {
  try {
    localStorage.setItem(
      CACHE_KEY,
      JSON.stringify({ data, timestamp: Date.now() })
    )
  } catch {
    // localStorage 可能已满，忽略错误
  }
}

export async function getWeatherByIP() {
  // 先尝试读取缓存
  const cached = getCachedWeather()
  if (cached) {
    return cached
  }

  try {
    const response = await fetch(WEATHER_PROXY_URL)
    if (!response.ok) return null

    const data = await response.json()
    if (data) {
      setCachedWeather(data)
    }
    return data
  } catch {
    return null
  }
}
