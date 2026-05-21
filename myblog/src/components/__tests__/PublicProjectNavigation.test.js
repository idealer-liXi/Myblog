import { afterEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Navbar from '@/components/Navbar.vue'
import ProjectsCard from '@/components/ProjectsCard.vue'
import router from '@/router'

const { getPublicProjects } = vi.hoisted(() => ({
  getPublicProjects: vi.fn()
}))

vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRoute: () => ({ path: '/blog', name: 'blog' })
  }
})

vi.mock('vuex', () => ({
  useStore: () => ({
    getters: {
      'weixin_user/getUserInfo': {},
      'weixin_user/getLoginStatus': false,
      'weixin_user/getRoles': [],
      'weixin_user/getDisplayName': 'Guest',
      'weixin_user/getAvatar': '',
      'music/hasMusic': false,
      'music/isPlaying': false,
      'music/currentTrackIndex': 0,
      'music/musicList': [],
      'music/currentMusic': {}
    },
    dispatch: vi.fn()
  })
}))

vi.mock('@/services/audioPlayer.js', () => ({
  default: {
    togglePlay: vi.fn(),
    previous: vi.fn(),
    next: vi.fn()
  }
}))

vi.mock('@/services/projectService.js', () => ({
  getPublicProjects
}))

describe('public project navigation', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('exposes the project showcase route from the navbar', () => {
    const navbar = mount(Navbar, {
      global: {
        mocks: {
          $route: { path: '/blog' }
        },
        stubs: {
          RouterLink: {
            props: ['to'],
            template: '<a :data-route="JSON.stringify(to)"><slot /></a>'
          }
        }
      }
    })

    expect(navbar.text()).toContain('项目展示')
    expect(router.resolve({ name: 'projects' }).href).toBe('/projects')
    expect(router.resolve({ name: 'project-detail', params: { id: 'ideal-blog' } }).href).toBe('/projects/ideal-blog')
  })

  it('routes the project card detail button to the internal showcase page', async () => {
    getPublicProjects.mockResolvedValue([
      {
        id: 'ideal-blog',
        name: 'IdealBlog 个人博客',
        image: '',
        techStack: 'Vue 3, Spring Boot',
        status: '进行中'
      }
    ])

    const pushSpy = vi.spyOn(router, 'push').mockResolvedValue()
    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)

    const wrapper = mount(ProjectsCard, {
      global: {
        stubs: {
          ImageInitialFallback: { template: '<div />' }
        }
      }
    })

    await Promise.resolve()
    await wrapper.vm.$nextTick()
    await wrapper.get('.view-btn').trigger('click')

    expect(pushSpy).toHaveBeenCalledWith({
      name: 'project-detail',
      params: { id: 'ideal-blog' }
    })
    expect(openSpy).not.toHaveBeenCalled()
  })
})
