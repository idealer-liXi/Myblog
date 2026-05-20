const QWEATHER_API_BASE = 'https://devapi.qweather.com/v7'
const QWEATHER_WEB_API_BASE = 'https://geoapi.qweather.com/v2'

function getApiKey() {
  return localStorage.getItem('qweather_key') || ''
}

export async function getWeatherByIP() {
  const key = getApiKey()
  if (!key) return null

  try {
    const locRes = await fetch(`${QWEATHER_WEB_API_BASE}/city/lookup?location=auto&key=${key}&lang=zh`)
    const locData = await locRes.json()
    if (locData.code !== '200' || !locData.location?.[0]) return null

    const loc = locData.location[0]
    const weatherRes = await fetch(`${QWEATHER_API_BASE}/weather/now?location=${loc.id}&key=${key}&lang=zh`)
    const weatherData = await weatherRes.json()
    if (weatherData.code !== '200' || !weatherData.now) return null

    return {
      city: loc.name,
      temp: weatherData.now.temp,
      text: weatherData.now.text,
      icon: weatherData.now.icon,
      windDir: weatherData.now.windDir,
      windScale: weatherData.now.windScale,
      humidity: weatherData.now.humidity,
      feelsLike: weatherData.now.feelsLike,
      updateTime: weatherData.updateTime
    }
  } catch {
    return null
  }
}

export function setApiKey(key) {
  if (key) {
    localStorage.setItem('qweather_key', key)
  } else {
    localStorage.removeItem('qweather_key')
  }
}

export function hasApiKey() {
  return !!getApiKey()
}