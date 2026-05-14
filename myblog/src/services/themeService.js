import axios from 'axios'

const PUBLIC_BASE_URL = 'http://localhost:8080/api/public'
const ADMIN_BASE_URL = 'http://localhost:8080/api/admin'

function getAuthHeaders() {
  const token = localStorage.getItem('jwtToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function getPublicThemes() {
  try {
    const response = await axios.get(`${PUBLIC_BASE_URL}/themes`)
    if (response.data.code !== '0000') {
      throw new Error(response.data.info || '获取主题列表失败')
    }
    return response.data.data || []
  } catch (error) {
    console.error('获取主题列表失败:', error)
    throw error
  }
}

export async function getThemes() {
  const response = await axios.get(`${ADMIN_BASE_URL}/themes`, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '获取主题列表失败')
  }
  return response.data.data || []
}

export async function getThemeById(themeId) {
  const response = await axios.get(`${ADMIN_BASE_URL}/themes/${themeId}`, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '获取主题详情失败')
  }
  return response.data.data
}

export async function createTheme(themeData) {
  const response = await axios.post(`${ADMIN_BASE_URL}/themes`, themeData, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '创建主题失败')
  }
  return response.data.data
}

export async function updateTheme(themeId, themeData) {
  const response = await axios.put(`${ADMIN_BASE_URL}/themes/${themeId}`, themeData, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '更新主题失败')
  }
  return response.data.data
}

export async function deleteTheme(themeId) {
  const response = await axios.delete(`${ADMIN_BASE_URL}/themes/${themeId}`, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '删除主题失败')
  }
  return true
}
