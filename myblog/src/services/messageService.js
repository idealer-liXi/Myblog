import axios from 'axios'

const PUBLIC_BASE_URL = 'http://localhost:8080/api/public'
const USER_BASE_URL = 'http://localhost:8080/api/user'
const ADMIN_BASE_URL = 'http://localhost:8080/api/admin'

function getAuthHeaders() {
  const token = localStorage.getItem('jwtToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function unwrapAdminResponse(response, fallbackMessage) {
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || fallbackMessage)
  }
  return response.data.data
}

function normalizeMessage(message) {
  if (!message) return null
  return {
    ...message,
    username: message.username || '',
    content: message.content || '',
    status: message.status || 'PUBLISHED',
    sentiment: message.sentiment || 'UNKNOWN'
  }
}

function normalizePage(payload) {
  return {
    items: Array.isArray(payload?.items) ? payload.items.map(normalizeMessage).filter(Boolean) : [],
    page: payload?.page || 1,
    pageSize: payload?.pageSize || 10,
    total: payload?.total || 0
  }
}

export async function getPublicMessages(params = {}) {
  const response = await axios.get(`${PUBLIC_BASE_URL}/messages`, { params })
  return normalizePage(response.data)
}

export async function createMessage(payload) {
  const response = await axios.post(`${USER_BASE_URL}/messages`, payload, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return normalizeMessage(unwrapAdminResponse(response, '提交留言失败'))
}

export async function getMyMessages() {
  const response = await axios.get(`${USER_BASE_URL}/messages/mine`, {
    headers: getAuthHeaders()
  })
  const list = unwrapAdminResponse(response, '获取我的留言失败') || []
  return list.map(normalizeMessage).filter(Boolean)
}

export async function deleteOwnMessage(id) {
  const response = await axios.delete(`${USER_BASE_URL}/messages/${id}`, {
    headers: getAuthHeaders()
  })
  unwrapAdminResponse(response, '删除留言失败')
  return true
}

export async function getAdminMessages(params = {}) {
  const response = await axios.get(`${ADMIN_BASE_URL}/messages`, {
    params,
    headers: getAuthHeaders()
  })
  return normalizePage(unwrapAdminResponse(response, '获取留言列表失败'))
}

export async function publishMessage(id) {
  const response = await axios.put(`${ADMIN_BASE_URL}/messages/${id}/publish`, {}, {
    headers: getAuthHeaders()
  })
  return normalizeMessage(unwrapAdminResponse(response, '审核通过失败'))
}

export async function rejectMessage(id) {
  const response = await axios.put(`${ADMIN_BASE_URL}/messages/${id}/reject`, {}, {
    headers: getAuthHeaders()
  })
  return normalizeMessage(unwrapAdminResponse(response, '审核拒绝失败'))
}

export async function deleteAdminMessage(id) {
  const response = await axios.delete(`${ADMIN_BASE_URL}/messages/${id}`, {
    headers: getAuthHeaders()
  })
  unwrapAdminResponse(response, '删除留言失败')
  return true
}

export async function getMessageSettings() {
  const response = await axios.get(`${ADMIN_BASE_URL}/message-settings`, {
    headers: getAuthHeaders()
  })
  return unwrapAdminResponse(response, '获取留言板配置失败')
}

export async function updateMessageSettings(payload) {
  const response = await axios.put(`${ADMIN_BASE_URL}/message-settings`, payload, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return unwrapAdminResponse(response, '更新留言板配置失败')
}
