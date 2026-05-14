import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/admin'

const mockProjects = [
  {
    id: 1,
    name: '个人博客系统',
    description: '基于 Vue 3 + Node.js 的全栈博客系统，支持文章管理、分类标签、用户认证等功能',
    techStack: 'Vue 3, Node.js, Express, MongoDB, Bootstrap',
    projectUrl: 'https://myblog.example.com',
    githubUrl: 'https://github.com/username/myblog',
    previewUrl: 'https://demo.myblog.example.com',
    coverImage: 'https://picsum.photos/seed/blog/400/300',
    status: '已完成',
    sortOrder: 1,
    startDate: '2024-01-15',
    endDate: '2024-03-20',
    isPublic: true
  },
  {
    id: 2,
    name: '在线商城平台',
    description: '电商购物平台，包含商品展示、购物车、订单管理、支付集成等核心功能',
    techStack: 'React, Spring Boot, MySQL, Redis, Docker',
    projectUrl: 'https://shop.example.com',
    githubUrl: 'https://github.com/username/shop',
    previewUrl: '',
    coverImage: 'https://picsum.photos/seed/shop/400/300',
    status: '进行中',
    sortOrder: 2,
    startDate: '2024-04-01',
    endDate: '',
    isPublic: true
  },
  {
    id: 3,
    name: '任务管理工具',
    description: '团队协作任务管理应用，支持看板视图、甘特图、团队权限管理',
    techStack: 'Vue 3, TypeScript, Pinia, Element Plus, Node.js',
    projectUrl: '',
    githubUrl: 'https://github.com/username/taskmanager',
    previewUrl: 'https://taskdemo.example.com',
    coverImage: '',
    status: '进行中',
    sortOrder: 3,
    startDate: '2024-05-10',
    endDate: '',
    isPublic: false
  },
  {
    id: 4,
    name: '数据可视化大屏',
    description: '企业级数据可视化展示平台，支持实时数据更新、多维度图表分析',
    techStack: 'React, D3.js, ECharts, Python, FastAPI',
    projectUrl: 'https://dataviz.example.com',
    githubUrl: '',
    previewUrl: 'https://dataviz.example.com/demo',
    coverImage: 'https://picsum.photos/seed/dataviz/400/300',
    status: '暂停',
    sortOrder: 4,
    startDate: '2024-02-01',
    endDate: '2024-06-15',
    isPublic: true
  }
]

let nextId = 5
let useMock = true

function getAuthHeaders() {
  const token = localStorage.getItem('jwtToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function getProjects() {
  if (useMock) {
    return [...mockProjects]
  }
  const response = await axios.get(`${API_BASE_URL}/projects`, {
    headers: getAuthHeaders()
  })
  return response.data.projects || response.data
}

export async function getProjectById(projectId) {
  if (useMock) {
    return mockProjects.find(p => p.id === projectId) || null
  }
  const response = await axios.get(`${API_BASE_URL}/projects/${projectId}`, {
    headers: getAuthHeaders()
  })
  return response.data.project || response.data
}

export async function createProject(projectData) {
  if (useMock) {
    const newProject = {
      id: nextId++,
      name: projectData.name,
      description: projectData.description || '',
      techStack: projectData.techStack || '',
      projectUrl: projectData.projectUrl || '',
      githubUrl: projectData.githubUrl || '',
      previewUrl: projectData.previewUrl || '',
      coverImage: projectData.coverImage || '',
      status: projectData.status || '进行中',
      sortOrder: projectData.sortOrder ?? 0,
      startDate: projectData.startDate || '',
      endDate: projectData.endDate || '',
      isPublic: projectData.isPublic !== undefined ? projectData.isPublic : true
    }
    mockProjects.push(newProject)
    return { ...newProject }
  }
  const response = await axios.post(`${API_BASE_URL}/projects`, projectData, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return response.data.project || response.data
}

export async function updateProject(projectId, projectData) {
  if (useMock) {
    const idx = mockProjects.findIndex(p => p.id === projectId)
    if (idx > -1) {
      mockProjects[idx] = { ...mockProjects[idx], ...projectData }
      return { ...mockProjects[idx] }
    }
    return null
  }
  const response = await axios.put(`${API_BASE_URL}/projects/${projectId}`, projectData, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return response.data.project || response.data
}

export async function deleteProject(projectId) {
  if (useMock) {
    const idx = mockProjects.findIndex(p => p.id === projectId)
    if (idx > -1) mockProjects.splice(idx, 1)
    return true
  }
  const response = await axios.delete(`${API_BASE_URL}/projects/${projectId}`, {
    headers: getAuthHeaders()
  })
  return response.data.success !== undefined ? response.data.success : true
}
