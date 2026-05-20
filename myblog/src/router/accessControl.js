export function userHasRole(user, role) {
  return Array.isArray(user?.roles) && user.roles.includes(role)
}

export function canAccessBackend(user) {
  return userHasRole(user, 'ADMIN')
}

export function resolveNavigationGuard(to, session) {
  if (!to.meta?.requiresAuth) {
    return { allow: true }
  }

  if (!session.isValid) {
    return {
      allow: false,
      clearSession: true,
      redirect: { name: 'login', query: { redirect: to.fullPath } }
    }
  }

  if (to.meta?.requiresAdmin && !canAccessBackend(session.user)) {
    return {
      allow: false,
      redirect: { name: 'user-profile' }
    }
  }

  return { allow: true }
}
