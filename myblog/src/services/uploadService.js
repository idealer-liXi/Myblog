import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/admin'

let useMock = false
let nextId = 6

const mockImages = [
  { id: 1, name: '博客封面.jpg', url: 'https://picsum.photos/seed/blog1/800/400', size: '245 KB', uploadedAt: '2024-06-20T10:30:00Z' },
  { id: 2, name: '项目截图.png', url: 'https://picsum.photos/seed/proj2/800/400', size: '189 KB', uploadedAt: '2024-06-18T14:20:00Z' },
  { id: 3, name: '架构图.png', url: 'https://picsum.photos/seed/arch3/800/400', size: '312 KB', uploadedAt: '2024-06-15T09:10:00Z' },
  { id: 4, name: '技术分享.jpg', url: 'https://picsum.photos/seed/tech4/800/400', size: '156 KB', uploadedAt: '2024-06-10T16:45:00Z' },
  { id: 5, name: '学习笔记.png', url: 'https://picsum.photos/seed/note5/800/400', size: '278 KB', uploadedAt: '2024-06-05T11:00:00Z' }
]

function getAuthHeaders() {
  const token = localStorage.getItem('jwtToken')
  const headers = {}
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

function formatFileSize(size) {
  if (typeof size !== 'number' || Number.isNaN(size)) return ''
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${Math.round(size / 1024)} KB`
  return `${(size / (1024 * 1024)).toFixed(2)} MB`
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

function decodeUrlSegment(value) {
  if (!value) return ''
  let decoded = value
  for (let i = 0; i < 3; i++) {
    try {
      const next = decodeURIComponent(decoded)
      if (next === decoded) {
        break
      }
      decoded = next
    } catch {
      break
    }
  }
  return decoded
}

function parseImageLocation(url) {
  if (!url) {
    return {
      imageType: 'DOCUMENT',
      folderKey: 'ungrouped',
      folderName: '未分组图片'
    }
  }

  try {
    const parsedUrl = new URL(url)
    const segments = parsedUrl.pathname.split('/').filter(Boolean)
    const blogImagesIndex = segments.indexOf('blog-images')
    const relevantSegments = blogImagesIndex >= 0 ? segments.slice(blogImagesIndex + 1) : segments

    if (relevantSegments[0] === 'documents' && relevantSegments[1]) {
      return {
        imageType: 'DOCUMENT',
        folderKey: relevantSegments[1],
        folderName: decodeUrlSegment(relevantSegments[1])
      }
    }

    if (relevantSegments[0] === 'projects' && relevantSegments[1]) {
      return {
        imageType: 'PROJECT',
        folderKey: relevantSegments[1],
        folderName: decodeUrlSegment(relevantSegments[1])
      }
    }

    if (relevantSegments[0] === 'users' && relevantSegments[1]) {
      return {
        imageType: 'USER',
        folderKey: relevantSegments[1],
        folderName: decodeUrlSegment(relevantSegments[1])
      }
    }
  } catch {
    // fall through to ungrouped
  }

  return {
    imageType: 'DOCUMENT',
    folderKey: 'ungrouped',
    folderName: '未分组图片'
  }
}

function normalizeImageRecord(image) {
  if (!image) return null
  const location = parseImageLocation(image.url || image.ossUrl || '')
  return {
    ...image,
    name: image.name || image.fileName || '',
    url: normalizeOssUrl(image.url || image.ossUrl || ''),
    size: image.size || formatFileSize(image.fileSize),
    uploadedAt: image.uploadedAt || image.createTime || '',
    imageType: image.imageType || location.imageType,
    folderKey: image.folderKey || location.folderKey,
    folderName: image.folderName || location.folderName
  }
}

export async function uploadImage(file, options = {}) {
  const formData = new FormData()
  formData.append('file', file)
  if (options.directory) {
    formData.append('directory', options.directory)
  }

  const response = await axios.post(`${API_BASE_URL}/images/upload`, formData, {
    headers: {
      ...getAuthHeaders(),
      'Content-Type': 'multipart/form-data'
    }
  })

  if (response.data.code === '0000' || response.data.url) {
    return response.data.url || response.data.data?.url
  }

  throw new Error(response.data.info || '上传失败')
}

export async function getImages() {
  if (useMock) {
    return [...mockImages]
  }
  const response = await axios.get(`${API_BASE_URL}/images`, {
    headers: getAuthHeaders()
  })
  const list = response.data.data || response.data || []
  return list.map(normalizeImageRecord).filter(Boolean)
}

export async function deleteImage(id) {
  if (useMock) {
    const idx = mockImages.findIndex(img => img.id === id)
    if (idx > -1) mockImages.splice(idx, 1)
    return true
  }
  const response = await axios.delete(`${API_BASE_URL}/images/${id}`, {
    headers: getAuthHeaders()
  })
  return response.data.code === '0000' || response.data.success !== false
}
