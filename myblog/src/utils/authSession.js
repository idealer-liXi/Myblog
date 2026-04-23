const TOKEN_KEY = 'jwtToken'
const TOKEN_EXPIRY_KEY = 'jwtTokenExpiry'
const PROFILE_KEY = 'currentUserProfile'

function safeParseProfile(rawProfile) {
  if (!rawProfile) return null

  try {
    return JSON.parse(rawProfile)
  } catch (error) {
    localStorage.removeItem(PROFILE_KEY)
    return null
  }
}

export function saveSession({ token, expiresAt, user }) {
  localStorage.setItem(TOKEN_KEY, token || '')
  localStorage.setItem(TOKEN_EXPIRY_KEY, String(expiresAt || 0))
  localStorage.setItem(PROFILE_KEY, JSON.stringify(user || {}))
}

export function readSession() {
  const token = localStorage.getItem(TOKEN_KEY) || ''
  const expiresAt = Number(localStorage.getItem(TOKEN_EXPIRY_KEY) || 0)
  const user = safeParseProfile(localStorage.getItem(PROFILE_KEY))

  return { token, expiresAt, user }
}

export function hasValidSession() {
  const { token, expiresAt } = readSession()
  return Boolean(token) && Boolean(expiresAt) && Date.now() < expiresAt
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TOKEN_EXPIRY_KEY)
  localStorage.removeItem(PROFILE_KEY)
}
