import axios from 'axios'
import { readSession } from '@/utils/authSession'

const PASSWORD_BASE_URL = 'http://localhost:8080/api/r1'
const LOGIN_BASE_URL = 'http://localhost:8080/api/v1/login'
const USER_BASE_URL = 'http://localhost:8080/api/user'
const DEFAULT_EXPIRY_MS = 24 * 60 * 60 * 1000
const NO_LOGIN_CODE = '1001'

function normalizeLoginType(loginType) {
  return loginType === 'wechat' ? 'weixin' : loginType
}

function getAuthHeaders() {
  const { token } = readSession()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function normalizeUser(payloadUser = {}, fallback = {}) {
  const avatarSource = payloadUser.avatarSource || fallback.avatarSource || 'DEFAULT'
  const avatar = payloadUser.avatar || fallback.avatar || ''
  const weixinImageUrl = payloadUser.weixinImageUrl || fallback.weixinImageUrl || payloadUser.weixinImageUrl || ''
  const defaultAvatar = payloadUser.defaultAvatar || fallback.defaultAvatar || ''
  const effectiveAvatar = payloadUser.effectiveAvatar || fallback.effectiveAvatar || (
    avatarSource === 'UPLOAD' && avatar ? avatar
      : avatarSource === 'WECHAT' && weixinImageUrl ? weixinImageUrl
        : defaultAvatar
  )

  const payloadDisplayName = payloadUser.displayName || ''
  const fallbackDisplayName = fallback.displayName || ''

  let displayName = payloadDisplayName || fallbackDisplayName
  if (!displayName) {
    displayName = payloadUser.weixinName || fallback.weixinName || payloadUser.username || fallback.username || '用户'
  }

  return {
    id: payloadUser.id ?? fallback.id ?? null,
    username: payloadUser.username || fallback.username || '',
    displayName,
    avatar,
    avatarSource,
    effectiveAvatar,
    defaultAvatar,
    weixinImageUrl,
    loginType: normalizeLoginType(payloadUser.loginType || fallback.loginType || 'password'),
    roles: Array.isArray(payloadUser.roles) ? payloadUser.roles : (fallback.roles || []),
    weixinBound: typeof payloadUser.weixinBound === 'boolean' ? payloadUser.weixinBound : Boolean(fallback.weixinBound),
    weixinName: payloadUser.weixinName || fallback.weixinName || '',
    openid: payloadUser.openid || fallback.openid || ''
  }
}

function normalizeSessionFromData(data, fallback = {}) {
  if (typeof data === 'string') {
    return {
      token: data,
      expiresAt: Date.now() + DEFAULT_EXPIRY_MS,
      user: normalizeUser({}, fallback)
    }
  }

  return {
    token: data?.token || '',
    expiresAt: Number(data?.expiresAt || Date.now() + DEFAULT_EXPIRY_MS),
    user: normalizeUser(data?.user, fallback)
  }
}

function normalizePendingWechatLogin(data = {}) {
  return {
    status: data.status || '',
    pendingToken: data.pendingToken || '',
    authType: data.authType || '',
    authKey: data.authKey || '',
    displayName: data.displayName || '',
    avatar: data.avatar || '',
    weixinName: data.weixinName || ''
  }
}

function isCompleteSessionPayload(data) {
  return Boolean(data && data.token && data.user)
}

async function request(config) {
  try {
    return await axios(config)
  } catch (error) {
    const message = error.response?.data?.info || error.message || '请求失败'
    const wrappedError = new Error(message)
    wrappedError.status = error.response?.status
    throw wrappedError
  }
}

export async function loginWithPassword(username, password) {
  const response = await request({
    method: 'post',
    url: `${PASSWORD_BASE_URL}/login/token`,
    data: { username, password }
  })

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '登录失败')
  }

const session = normalizeSessionFromData(response.data.data, {
    username,
    loginType: 'password',
    roles: []
  })

  return session
}

export async function fetchWechatLoginTicket() {
  const response = await request({
    method: 'get',
    url: `${LOGIN_BASE_URL}/weixin_qrcode_ticket`
  })

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '获取微信登录二维码失败')
  }

  return response.data.data
}

export async function checkWechatLogin(ticket) {
  const response = await request({
    method: 'get',
    url: `${LOGIN_BASE_URL}/check_login`,
    params: { ticket }
  })

  if (response.data.code === NO_LOGIN_CODE) {
    return null
  }

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '微信登录失败')
  }

  const payload = response.data.data

  if (payload?.status === NO_LOGIN_CODE) {
    return null
  }

  if (payload?.status === 'PENDING_DECISION') {
    return {
      type: 'pending',
      pending: normalizePendingWechatLogin(payload)
    }
  }

  if (isCompleteSessionPayload(payload)) {
    return {
      type: 'session',
      session: normalizeSessionFromData(payload, { loginType: 'weixin' })
    }
  }

  throw new Error('微信登录响应格式不正确')
}

export async function createWechatAccount(pendingToken) {
  const response = await request({
    method: 'post',
    url: `${LOGIN_BASE_URL}/weixin/create-account`,
    data: { pendingToken }
  })

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '创建微信账号失败')
  }

  return normalizeSessionFromData(response.data.data, { loginType: 'weixin' })
}

export async function bindWechatExistingAccount({ pendingToken, username, password }) {
  const response = await request({
    method: 'post',
    url: `${LOGIN_BASE_URL}/weixin/bind-existing`,
    data: { pendingToken, username, password }
  })

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '绑定已有账号失败')
  }

  return normalizeSessionFromData(response.data.data, { loginType: 'weixin' })
}

export async function fetchCurrentUser() {
  const { user } = readSession()

  try {
    const response = await request({
      method: 'get',
      url: `${USER_BASE_URL}/me`,
      headers: getAuthHeaders()
    })

    if (response.data.code !== '0000') {
      throw new Error(response.data.info || '获取当前用户失败')
    }

    return normalizeUser(response.data.data, user || {})
  } catch (error) {
    if (user && error.status !== 401 && error.status !== 403) {
      return normalizeUser(user)
    }
    throw error
  }
}

export async function fetchWechatBindTicket() {
  const response = await request({
    method: 'get',
    url: `${USER_BASE_URL}/weixin/bind/qrcode`,
    headers: getAuthHeaders()
  })

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '获取绑定二维码失败')
  }

  return response.data.data
}

export async function checkWechatBind(ticket) {
  const response = await request({
    method: 'get',
    url: `${USER_BASE_URL}/weixin/bind/check`,
    params: { ticket },
    headers: getAuthHeaders()
  })

  if (response.data.code === NO_LOGIN_CODE) {
    return null
  }

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '微信绑定失败')
  }

  return normalizeUser(response.data.data)
}

export async function unbindWechat() {
  const response = await request({
    method: 'delete',
    url: `${USER_BASE_URL}/weixin/bind`,
    headers: getAuthHeaders()
  })

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '解除微信绑定失败')
  }

  return response.data.data
}

export async function uploadCurrentUserAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await request({
    method: 'post',
    url: `${USER_BASE_URL}/me/avatar/upload`,
    data: formData,
    headers: {
      ...getAuthHeaders(),
      'Content-Type': 'multipart/form-data'
    }
  })

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '头像上传失败')
  }

  const url = response.data.data?.url || ''
  if (!url) {
    throw new Error('头像上传失败')
  }

  return url
}

export async function updateCurrentUserProfile(payload) {
  const response = await request({
    method: 'put',
    url: `${USER_BASE_URL}/me/profile`,
    data: payload,
    headers: getAuthHeaders()
  })

  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '更新个人资料失败')
  }

  return normalizeUser(response.data.data)
}
