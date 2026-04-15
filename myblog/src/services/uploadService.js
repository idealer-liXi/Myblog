import axios from 'axios'

const API_BASE_URL = 'http://localhost:3000/api'

let useMock = true
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

export async function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await axios.post(`${API_BASE_URL}/upload/image`, formData, {
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
  return response.data.data || response.data
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