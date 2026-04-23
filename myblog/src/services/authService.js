import axios from 'axios'
import { clearSession, readSession, saveSession } from '@/utils/authSession'

const PASSWORD_BASE_URL = 'http://localhost:8080/api/r1'
const LOGIN_BASE_URL = 'http://localhost:8080/api/v1/login'
const USER_BASE_URL = 'http://localhost:8080/api/user'
const DEFAULT_EXPIRY_MS = 24 * 60 * 60 * 1000
const NO_LOGIN_CODE = '1001'

function getAuthHeaders() {
  const { token } = readSession()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function normalizeUser(payloadUser = {}, fallback = {}) {
  return {
    id: payloadUser.id ?? fallback.id ?? null,
    username: payloadUser.username || fallback.username || '',
    displayName: payloadUser.displayName || fallback.displayName || payloadUser.weixinName || fallback.weixinName || payloadUser.username || fallback.username || '用户',
    avatar: payloadUser.avatar || fallback.avatar || payloadUser.weixinImageUrl || fallback.weixinImageUrl || '',
    loginType: payloadUser.loginType || fallback.loginType || 'password',
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
    displayName: username,
    loginType: 'password',
    roles: []
  })

  saveSession(session)
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

  if (typeof response.data.data === 'string') {
    throw new Error('后端尚未返回统一登录会话，请先完成微信登录接口改造')
  }

  const session = normalizeSessionFromData(response.data.data, { loginType: 'weixin' })
  saveSession(session)
  return session
}

export async function fetchCurrentUser() {
  try {
    const response = await request({
      method: 'get',
      url: `${USER_BASE_URL}/me`,
      headers: getAuthHeaders()
    })

    if (response.data.code !== '0000') {
      throw new Error(response.data.info || '获取当前用户失败')
    }

    return normalizeUser(response.data.data)
  } catch (error) {
    const { user } = readSession()
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

export function logout() {
  clearSession()
}
