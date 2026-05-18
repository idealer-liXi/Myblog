import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/admin/users'
const ADMIN_AVATAR_BASE_URL = 'http://localhost:8080/api/admin/user-images'

const getAuthHeaders = () => {
  const token = localStorage.getItem('jwtToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function normalizeAvatarUser(user = {}) {
  const defaultAvatar = user.defaultAvatar || ''
  const avatarSource = user.avatarSource || 'DEFAULT'
  const effectiveAvatar = user.effectiveAvatar && user.effectiveAvatar !== '/images/default-avatar.png'
    ? user.effectiveAvatar
    : avatarSource === 'UPLOAD' && user.avatar
      ? user.avatar
      : avatarSource === 'WECHAT' && user.weixinImageUrl
        ? user.weixinImageUrl
        : defaultAvatar

  return {
    ...user,
    avatarSource,
    effectiveAvatar,
    defaultAvatar,
    hasUploadedAvatar: Boolean(user.hasUploadedAvatar ?? user.avatar),
    hasWeixinAvatar: Boolean(user.hasWeixinAvatar ?? user.weixinImageUrl)
  }
}

export async function getUsers(params = {}) {
  const response = await axios.get(API_BASE_URL, {
    params,
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '获取用户列表失败')
  }
  if (response.data?.data?.users) {
    response.data.data.users = response.data.data.users.map(normalizeAvatarUser)
  }
  return response.data
}

export async function getUserById(userId) {
  const response = await axios.get(`${API_BASE_URL}/${userId}`, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '获取用户详情失败')
  }
  if (response.data.data) {
    response.data.data = normalizeAvatarUser(response.data.data)
  }
  return response.data
}

export async function updateUserStatus(userId, status) {
  const response = await axios.put(`${API_BASE_URL}/${userId}/status`,
    { status },
    { headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' } }
  )
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '更新用户状态失败')
  }
  if (response.data.data) {
    response.data.data = normalizeAvatarUser(response.data.data)
  }
  return response.data
}

export async function deleteUser(userId) {
  const response = await axios.delete(`${API_BASE_URL}/${userId}`, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '删除用户失败')
  }
  return response.data
}

export async function getUserAvatarImages() {
  const response = await axios.get(ADMIN_AVATAR_BASE_URL, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '获取用户头像失败')
  }
  return (response.data.data || []).map(normalizeAvatarUser)
}

export async function getUserAvatarImageByUserId(userId) {
  const response = await axios.get(`${ADMIN_AVATAR_BASE_URL}/${userId}`, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '获取用户头像详情失败')
  }
  return normalizeAvatarUser(response.data.data || {})
}

export async function updateUserAvatar(userId, payload) {
  const response = await axios.put(`${ADMIN_AVATAR_BASE_URL}/${userId}`, payload, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '更新用户头像失败')
  }
  return normalizeAvatarUser(response.data.data || {})
}

export async function clearUserAvatar(userId) {
  const response = await axios.delete(`${ADMIN_AVATAR_BASE_URL}/${userId}`, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '清空用户头像失败')
  }
  return true
}
