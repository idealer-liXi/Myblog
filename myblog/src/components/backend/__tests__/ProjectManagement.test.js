import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('ProjectManagement showcase images', () => {
  it('defines showcase image defaults and reorder/remove handlers', () => {
    const source = readFileSync(resolve(process.cwd(), 'src/components/backend/ProjectManagement.vue'), 'utf8')

    expect(source).toContain('showcaseImages: []')
    expect(source).toContain('const removeShowcaseImage = (index) => {')
    expect(source).toContain('const moveShowcaseImageLeft = (index) => {')
    expect(source).toContain('const moveShowcaseImageRight = (index) => {')
    expect(source).toContain('用于项目展示页轮播图，封面图仍单独维护')
  })
})
