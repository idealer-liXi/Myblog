import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectCarousel from '@/components/projects/ProjectCarousel.vue'

describe('ProjectCarousel', () => {
  it('shows the first image, moves forward, and resets when props change', async () => {
    const wrapper = mount(ProjectCarousel, {
      props: {
        projectName: 'IdealBlog',
        images: ['https://cdn.example.com/1.png', 'https://cdn.example.com/2.png']
      }
    })

    expect(wrapper.get('[data-testid="project-carousel-image"]').attributes('src')).toBe('https://cdn.example.com/1.png')

    await wrapper.get('[data-testid="project-carousel-next"]').trigger('click')
    expect(wrapper.get('[data-testid="project-carousel-image"]').attributes('src')).toBe('https://cdn.example.com/2.png')

    await wrapper.setProps({ images: ['https://cdn.example.com/3.png'] })
    expect(wrapper.get('[data-testid="project-carousel-image"]').attributes('src')).toBe('https://cdn.example.com/3.png')
  })

  it('renders an empty state when no images are available', () => {
    const wrapper = mount(ProjectCarousel, {
      props: {
        projectName: 'No Images Project',
        images: []
      }
    })

    expect(wrapper.get('[data-testid="project-carousel-empty"]').text()).toContain('暂无项目截图')
  })
})
