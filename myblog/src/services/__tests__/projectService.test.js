import { describe, expect, it, vi, beforeEach } from 'vitest'
import axios from 'axios'
import { getProjects } from '@/services/projectService.js'

vi.mock('axios', () => ({
  default: {
    get: vi.fn()
  }
}))

describe('projectService admin normalization', () => {
  beforeEach(() => {
    axios.get.mockReset()
  })

  it('keeps showcaseImages as an editable array for project management', async () => {
    axios.get.mockResolvedValueOnce({
      data: {
        code: '0000',
        data: [
          {
            id: 9,
            name: 'IdealBlog',
            coverImage: 'https://cdn.example.com/cover.png',
            showcaseImages: ['https://cdn.example.com/a.png', 'https://cdn.example.com/b.png']
          }
        ]
      }
    })

    const projects = await getProjects()

    expect(projects[0].showcaseImages).toEqual([
      'https://cdn.example.com/a.png',
      'https://cdn.example.com/b.png'
    ])
  })

  it('defaults showcaseImages to an empty array when the backend omits it', async () => {
    axios.get.mockResolvedValueOnce({
      data: {
        code: '0000',
        data: [
          {
            id: 10,
            name: 'No Showcase Images',
            coverImage: 'https://cdn.example.com/cover.png'
          }
        ]
      }
    })

    const projects = await getProjects()

    expect(projects[0].showcaseImages).toEqual([])
  })
})
