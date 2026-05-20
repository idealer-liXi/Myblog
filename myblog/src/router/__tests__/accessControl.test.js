import { describe, expect, it } from 'vitest'
import { canAccessBackend, resolveNavigationGuard } from '@/router/accessControl'

describe('accessControl', () => {
  it('allows backend access for admin users', () => {
    expect(canAccessBackend({ roles: ['ADMIN'] })).toBe(true)
  })

  it('blocks backend access for non-admin users', () => {
    expect(resolveNavigationGuard(
      { fullPath: '/backend', meta: { requiresAuth: true, requiresAdmin: true } },
      { isValid: true, user: { roles: ['USER'] } }
    )).toEqual({
      allow: false,
      redirect: { name: 'user-profile' }
    })
  })
})
