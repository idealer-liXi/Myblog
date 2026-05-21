import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectInfoPanel from '@/components/projects/ProjectInfoPanel.vue'

const project = {
  id: 'ideal-blog',
  name: 'IdealBlog 个人博客',
  status: '进行中',
  startDate: '2026-01-01',
  endDate: '',
  techStack: ['Vue 3', 'Spring Boot', 'MySQL'],
  description: '个人博客、后台管理、音乐播放器和留言板整合在一个统一站点中。',
  githubUrl: 'https://github.com/ideal/example-blog',
  previewUrl: 'https://demo.example.com/ideal-blog'
}

describe('ProjectInfoPanel', () => {
  it('renders the selected project details and action links', () => {
    const wrapper = mount(ProjectInfoPanel, {
      props: {
        project,
        projectIndex: 1,
        projectCount: 3
      }
    })

    expect(wrapper.text()).toContain('IdealBlog 个人博客')
    expect(wrapper.text()).toContain('进行中')
    expect(wrapper.text()).toContain('2026-01-01')
    expect(wrapper.text()).toContain('Vue 3')
    expect(wrapper.text()).toContain('01 / 03')
    expect(wrapper.get('[data-testid="project-info-preview-link"]').attributes('href')).toBe('https://demo.example.com/ideal-blog')
    expect(wrapper.get('[data-testid="project-info-github-link"]').attributes('href')).toBe('https://github.com/ideal/example-blog')
  })

  it('omits missing external links and shows a safe date fallback', () => {
    const wrapper = mount(ProjectInfoPanel, {
      props: {
        project: {
          ...project,
          previewUrl: '',
          githubUrl: '',
          startDate: '',
          endDate: ''
        },
        projectIndex: 2,
        projectCount: 3
      }
    })

    expect(wrapper.text()).toContain('未填写')
    expect(wrapper.find('[data-testid="project-info-preview-link"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="project-info-github-link"]').exists()).toBe(false)
  })
})
