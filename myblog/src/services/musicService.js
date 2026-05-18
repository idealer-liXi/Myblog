import axios from 'axios'

const PUBLIC_BASE_URL = 'http://localhost:8080/api/public'
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

function normalizeMusic(music) {
  if (!music) return null
  return {
    ...music,
    audioUrl: normalizeOssUrl(music.audioUrl || ''),
    coverImage: normalizeOssUrl(music.coverImage || ''),
    enabled: music.enabled !== undefined ? music.enabled : true,
    sortOrder: music.sortOrder ?? 0
  }
}

function unwrapAdminResponse(response, fallbackMessage) {
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || fallbackMessage)
  }
  return response.data.data
}

export async function getPublicMusicList() {
  const response = await axios.get(`${PUBLIC_BASE_URL}/music`)
  const list = Array.isArray(response.data) ? response.data : response.data.data || []
  return list.map(normalizeMusic).filter(Boolean)
}

export async function getAdminMusicList() {
  const response = await axios.get(`${ADMIN_BASE_URL}/music`, {
    headers: getAuthHeaders()
  })
  const list = unwrapAdminResponse(response, '获取音乐列表失败') || []
  return list.map(normalizeMusic).filter(Boolean)
}

export async function getAdminMusicById(id) {
  const response = await axios.get(`${ADMIN_BASE_URL}/music/${id}`, {
    headers: getAuthHeaders()
  })
  return normalizeMusic(unwrapAdminResponse(response, '获取音乐详情失败'))
}

export async function createMusic(payload) {
  const response = await axios.post(`${ADMIN_BASE_URL}/music`, payload, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return normalizeMusic(unwrapAdminResponse(response, '创建音乐失败'))
}

export async function updateMusic(id, payload) {
  const response = await axios.put(`${ADMIN_BASE_URL}/music/${id}`, payload, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return normalizeMusic(unwrapAdminResponse(response, '更新音乐失败'))
}

export async function deleteMusic(id) {
  const response = await axios.delete(`${ADMIN_BASE_URL}/music/${id}`, {
    headers: getAuthHeaders()
  })
  unwrapAdminResponse(response, '删除音乐失败')
  return true
}

async function uploadFile(url, file) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await axios.post(url, formData, {
    headers: {
      ...getAuthHeaders(),
      'Content-Type': 'multipart/form-data'
    }
  })
  const payload = unwrapAdminResponse(response, '上传失败')
  return payload?.url || ''
}

export function uploadMusicAudio(file) {
  return uploadFile(`${ADMIN_BASE_URL}/music/upload-audio`, file)
}

export function uploadMusicCover(file) {
  return uploadFile(`${ADMIN_BASE_URL}/music/upload-cover`, file)
}
