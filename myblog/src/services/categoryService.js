import axios from 'axios'

const API_BASE_URL = 'http://localhost:3000/api'

const mockCategories = [
  { id: 1, name: 'Java', key: 'java', icon: 'bi bi-cup-hot-fill', color: '#e76f00', articleCount: 12 },
  { id: 2, name: 'Python', key: 'python', icon: 'bi bi-stack', color: '#3776ab', articleCount: 8 },
  { id: 3, name: 'C++', key: 'c++', icon: 'bi bi-cpu-fill', color: '#00599c', articleCount: 5 },
  { id: 4, name: 'Vue', key: 'vue', icon: 'bi bi-palette2', color: '#42b883', articleCount: 10 }
]

let nextId = 5
let useMock = true

function getAuthHeaders() {
  const token = localStorage.getItem('jwtToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function getCategories() {
  if (useMock) {
    return [...mockCategories]
  }
  const response = await axios.get(`${API_BASE_URL}/categories`, {
    headers: getAuthHeaders()
  })
  return response.data.categories || response.data
}

export async function getCategoryById(categoryId) {
  if (useMock) {
    return mockCategories.find(c => c.id === categoryId) || null
  }
  const response = await axios.get(`${API_BASE_URL}/categories/${categoryId}`, {
    headers: getAuthHeaders()
  })
  return response.data.category || response.data
}

export async function createCategory(categoryData) {
  if (useMock) {
    const newCategory = {
      id: nextId++,
      name: categoryData.name,
      key: categoryData.key,
      icon: categoryData.icon || 'bi bi-tag',
      color: categoryData.color || '#007bff',
      articleCount: 0
    }
    mockCategories.push(newCategory)
    return { ...newCategory }
  }
  const response = await axios.post(`${API_BASE_URL}/categories`, categoryData, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return response.data.category || response.data
}

export async function updateCategory(categoryId, categoryData) {
  if (useMock) {
    const idx = mockCategories.findIndex(c => c.id === categoryId)
    if (idx > -1) {
      mockCategories[idx] = { ...mockCategories[idx], ...categoryData }
      return { ...mockCategories[idx] }
    }
    return null
  }
  const response = await axios.put(`${API_BASE_URL}/categories/${categoryId}`, categoryData, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return response.data.category || response.data
}

export async function deleteCategory(categoryId) {
  if (useMock) {
    const idx = mockCategories.findIndex(c => c.id === categoryId)
    if (idx > -1) mockCategories.splice(idx, 1)
    return true
  }
  const response = await axios.delete(`${API_BASE_URL}/categories/${categoryId}`, {
    headers: getAuthHeaders()
  })
  return response.data.success !== undefined ? response.data.success : true
}