import axios from 'axios'

const PUBLIC_BASE_URL = 'http://localhost:8080/api'
const ADMIN_BASE_URL = 'http://localhost:8080/api/admin'

function getAuthHeaders() {
  const token = localStorage.getItem('jwtToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function normalizeOssUrl(url) {
  if (!url) return ''
  try {
    const parsedUrl = new URL(url)
    if (!parsedUrl.hostname.includes('aliyuncs.com')) {
      return url
    }
    if (parsedUrl.pathname.includes('%25')) {
      return url
    }
    const encodedPath = parsedUrl.pathname
      .split('/')
      .map((segment, index) => (index === 0 ? segment : encodeURIComponent(segment)))
      .join('/')
    return `${parsedUrl.protocol}//${parsedUrl.host}${encodedPath}${parsedUrl.search}${parsedUrl.hash}`
  } catch {
    return url
  }
}

function normalizeProject(project) {
  if (!project) return null
  return {
    ...project,
    coverImage: normalizeOssUrl(project.coverImage || project.image || ''),
    image: normalizeOssUrl(project.coverImage || project.image || ''),
    isPublic: project.isPublic !== undefined ? project.isPublic : true,
    enabled: project.enabled !== undefined ? project.enabled : true,
    accessType: project.accessType || 'public',
    allowedRoles: project.allowedRoles || ''
  }
}

function unwrapAdminResponse(response, fallbackMessage) {
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || fallbackMessage)
  }
  return response.data.data
}

export async function getPublicProjects() {
  try {
    const response = await axios.get(`${PUBLIC_BASE_URL}/projects`)
    const list = Array.isArray(response.data)
      ? response.data
      : response.data.data || []
    return list.map(normalizeProject).filter(Boolean)
  } catch (error) {
    console.error('获取公开项目列表失败:', error)
    throw error
  }
}

export async function getProjects() {
  const response = await axios.get(`${ADMIN_BASE_URL}/projects`, {
    headers: getAuthHeaders()
  })
  const list = unwrapAdminResponse(response, '获取项目列表失败') || []
  return list.map(normalizeProject).filter(Boolean)
}

export async function getProjectById(projectId) {
  const response = await axios.get(`${ADMIN_BASE_URL}/projects/${projectId}`, {
    headers: getAuthHeaders()
  })
  return normalizeProject(unwrapAdminResponse(response, '获取项目详情失败'))
}

export async function createProject(projectData) {
  const response = await axios.post(`${ADMIN_BASE_URL}/projects`, projectData, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return normalizeProject(unwrapAdminResponse(response, '创建项目失败'))
}

export async function updateProject(projectId, projectData) {
  const response = await axios.put(`${ADMIN_BASE_URL}/projects/${projectId}`, projectData, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return normalizeProject(unwrapAdminResponse(response, '更新项目失败'))
}

export async function deleteProject(projectId) {
  const response = await axios.delete(`${ADMIN_BASE_URL}/projects/${projectId}`, {
    headers: getAuthHeaders()
  })
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || '删除项目失败')
  }
  return true
}
