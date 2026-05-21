import { reactive } from 'vue'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectsShowcaseView from '@/views/projects/ProjectsShowcaseView.vue'

const { getPublicProjectShowcase } = vi.hoisted(() => ({
  getPublicProjectShowcase: vi.fn()
}))

vi.mock('@/services/projectService.js', () => ({
  getPublicProjectShowcase
}))

const showcaseProjects = [
  {
    id: 'ideal-blog',
    name: 'IdealBlog 个人博客',
    status: '进行中',
    techStack: ['Vue 3', 'Spring Boot', 'MySQL'],
    description: 'desc',
    images: ['https://cdn.example.com/1.png'],
    coverImage: 'https://cdn.example.com/1.png',
    startDate: '2026-01-01',
    endDate: '',
    githubUrl: '',
    previewUrl: ''
  },
  {
    id: 'message-board',
    name: '互动留言板',
    status: '已完成',
    techStack: ['Vue 3', 'REST API'],
    description: 'desc',
    images: ['https://cdn.example.com/2.png'],
    coverImage: 'https://cdn.example.com/2.png',
    startDate: '2026-01-02',
    endDate: '',
    githubUrl: '',
    previewUrl: ''
  }
]

const routeState = reactive({ params: { id: '' } })
const pushMock = vi.fn()
const replaceMock = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({
    push: pushMock,
    replace: replaceMock
  })
}))

describe('ProjectsShowcaseView', () => {
  beforeEach(() => {
    routeState.params.id = ''
    pushMock.mockReset()
    replaceMock.mockReset()
    getPublicProjectShowcase.mockReset()
    getPublicProjectShowcase.mockResolvedValue(showcaseProjects)
  })

  it('redirects /projects to the first project and renders its details', async () => {
    const wrapper = mount(ProjectsShowcaseView, {
      global: {
        stubs: {
          ImageInitialFallback: { props: ['name'], template: '<div>{{ name }}</div>' }
        }
      }
    })

    await Promise.resolve()
    await wrapper.vm.$nextTick()

    expect(getPublicProjectShowcase).toHaveBeenCalledTimes(1)
    expect(replaceMock).toHaveBeenCalledWith({
      name: 'project-detail',
      params: { id: showcaseProjects[0].id }
    })
    expect(wrapper.text()).toContain(showcaseProjects[0].name)
  })

  it('falls back to the first project when the route id is invalid', async () => {
    routeState.params.id = 'missing-project'

    const wrapper = mount(ProjectsShowcaseView, {
      global: {
        stubs: {
          ImageInitialFallback: { props: ['name'], template: '<div>{{ name }}</div>' }
        }
      }
    })

    await Promise.resolve()
    await wrapper.vm.$nextTick()

    expect(replaceMock).toHaveBeenCalledWith({
      name: 'project-detail',
      params: { id: showcaseProjects[0].id }
    })
    expect(wrapper.text()).toContain(showcaseProjects[0].name)
  })

  it('pushes a new route when another project is selected from the list', async () => {
    routeState.params.id = showcaseProjects[0].id

    const wrapper = mount(ProjectsShowcaseView, {
      global: {
        stubs: {
          ImageInitialFallback: { props: ['name'], template: '<div>{{ name }}</div>' }
        }
      }
    })

    await Promise.resolve()
    await wrapper.vm.$nextTick()

    await wrapper.get(`[data-testid="project-list-item-${showcaseProjects[1].id}"]`).trigger('click')

    expect(pushMock).toHaveBeenCalledWith({
      name: 'project-detail',
      params: { id: showcaseProjects[1].id }
    })
  })

  it('renders the carousel and detail panel in the same right column', async () => {
    routeState.params.id = showcaseProjects[0].id

    const wrapper = mount(ProjectsShowcaseView, {
      global: {
        stubs: {
          ImageInitialFallback: { props: ['name'], template: '<div>{{ name }}</div>' },
          ProjectCarousel: { template: '<section data-testid="stub-carousel"></section>' },
          ProjectInfoPanel: { template: '<section data-testid="stub-info"></section>' }
        }
      }
    })

    await Promise.resolve()
    await wrapper.vm.$nextTick()

    expect(wrapper.get('.projects-showcase-shell').element.children).toHaveLength(2)
    expect(wrapper.get('.project-main-stage').find('[data-testid="stub-carousel"]').exists()).toBe(true)
    expect(wrapper.get('.project-main-stage').find('[data-testid="stub-info"]').exists()).toBe(true)
  })

  it('removes list-card shadow and hover lift styling', () => {
    const source = readFileSync(resolve(process.cwd(), 'src/views/projects/ProjectsShowcaseView.vue'), 'utf8')

    expect(source).not.toContain('box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);')
    expect(source).not.toContain('transform: translateY(-2px);')
  })
})
