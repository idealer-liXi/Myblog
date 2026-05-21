import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import WeatherCard from '@/components/WeatherCard.vue'

const { getWeatherByIP } = vi.hoisted(() => ({
  getWeatherByIP: vi.fn()
}))

vi.mock('@/services/weatherService.js', () => ({
  getWeatherByIP
}))

describe('WeatherCard', () => {
  it('renders a local weather icon instead of requesting the heweather CDN icon', async () => {
    getWeatherByIP.mockResolvedValue({
      city: '沈阳',
      temp: '25',
      text: '晴',
      icon: '100',
      windDir: '西南风',
      windScale: '3',
      humidity: '30'
    })

    const wrapper = mount(WeatherCard)

    await Promise.resolve()
    await nextTick()

    expect(wrapper.find('img.weather-icon').exists()).toBe(false)
    expect(wrapper.get('[data-testid="weather-local-icon"]').classes().join(' ')).toContain('bi-')
    expect(wrapper.text()).toContain('沈阳')
  })
})
