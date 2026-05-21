import { afterEach, describe, expect, it, vi } from 'vitest'
import { getWeatherByIP } from '@/services/weatherService'

describe('weatherService', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('loads weather data from the backend proxy endpoint instead of localStorage-managed keys', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        city: '沈阳',
        temp: '25',
        text: '晴',
        icon: '100',
        windDir: '西南风',
        windScale: '3',
        humidity: '30',
        feelsLike: '26',
        updateTime: '2026-05-21T10:00:00Z'
      })
    })

    vi.stubGlobal('fetch', fetchMock)

    const data = await getWeatherByIP()

    expect(fetchMock).toHaveBeenCalledTimes(1)
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/public/weather/current')
    expect(data.city).toBe('沈阳')
  })
})
