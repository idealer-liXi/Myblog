import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/v1'

const getAuthHeaders = () => {
  const token = localStorage.getItem('jwtToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const mockUsers = [
  {
    id: 1,
    username: 'idealer',
    displayName: 'Idealer',
    loginType: 'password',
    status: 'active',
    createdAt: '2024-01-15T10:30:00Z',
    lastLoginAt: '2024-06-20T08:00:00Z'
  },
  {
    id: 2,
    username: null,
    weixinName: '张三',
    displayName: '张三',
    weixinImageUrl: 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKLb6qzYHpYdbqRh3RibMicBUCjmjXjXQQfBSU1ThFXDGvF5c7z1SU73m96ivedoXibPoy5libHlTbEdA/132',
    openid: 'openid_wx_1234567890',
    loginType: 'weixin',
    status: 'active',
    createdAt: '2024-03-10T14:20:00Z',
    lastLoginAt: '2024-06-22T15:30:00Z'
  },
  {
    id: 3,
    username: 'lisi',
    displayName: '李四',
    loginType: 'password',
    status: 'active',
    createdAt: '2024-04-05T09:15:00Z',
    lastLoginAt: '2024-06-18T11:45:00Z'
  },
  {
    id: 4,
    username: null,
    weixinName: '王五',
    displayName: '王五',
    weixinImageUrl: 'https://thirdwx.qlogo.cn/mmopen/vi_32/ajNVdqOKEEzgW7FRv3MDgZyStFHiciaUCmjCqvVN9bkyiaNjUz4SIAeictGh5hm7Tqibh2wQC3GuN5b5Dcb5UjRibxIQ/132',
    openid: 'openid_wx_9876543210',
    loginType: 'weixin',
    status: 'disabled',
    createdAt: '2024-02-20T16:45:00Z',
    lastLoginAt: '2024-05-10T07:30:00Z'
  },
  {
    id: 5,
    username: 'zhaoliu',
    displayName: '赵六',
    loginType: 'password',
    status: 'active',
    createdAt: '2024-05-12T20:00:00Z',
    lastLoginAt: '2024-06-21T18:10:00Z'
  },
  {
    id: 6,
    username: null,
    weixinName: '孙七',
    displayName: '孙七',
    openid: 'openid_wx_5555555555',
    loginType: 'weixin',
    status: 'active',
    createdAt: '2024-06-01T12:00:00Z',
    lastLoginAt: '2024-06-23T09:20:00Z'
  }
]

let useMock = true

export async function getUsers(params = {}) {
  if (useMock) {
    let result = [...mockUsers]
    if (params.keyword) {
      const kw = params.keyword.toLowerCase()
      result = result.filter(u =>
        (u.username || '').toLowerCase().includes(kw) ||
        (u.displayName || '').toLowerCase().includes(kw) ||
        (u.weixinName || '').toLowerCase().includes(kw)
      )
    }
    if (params.loginType) {
      result = result.filter(u => u.loginType === params.loginType)
    }
    if (params.status) {
      result = result.filter(u => u.status === params.status)
    }
    const page = params.page || 1
    const pageSize = params.pageSize || 10
    const start = (page - 1) * pageSize
    const paged = result.slice(start, start + pageSize)
    return {
      code: '0000',
      data: {
        total: result.length,
        page,
        pageSize,
        users: paged
      }
    }
  }

  const response = await axios.get(`${API_BASE_URL}/users`, {
    params,
    headers: getAuthHeaders()
  })
  return response.data
}

export async function getUserById(userId) {
  if (useMock) {
    const user = mockUsers.find(u => u.id === userId)
    return { code: '0000', data: user || null }
  }

  const response = await axios.get(`${API_BASE_URL}/users/${userId}`, {
    headers: getAuthHeaders()
  })
  return response.data
}

export async function updateUserStatus(userId, status) {
  if (useMock) {
    const user = mockUsers.find(u => u.id === userId)
    if (user) user.status = status
    return { code: '0000', info: '用户状态已更新', data: { id: userId, status } }
  }

  const response = await axios.put(`${API_BASE_URL}/users/${userId}/status`,
    { status },
    { headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' } }
  )
  return response.data
}

export async function deleteUser(userId) {
  if (useMock) {
    const idx = mockUsers.findIndex(u => u.id === userId)
    if (idx > -1) mockUsers.splice(idx, 1)
    return { code: '0000', info: '用户已删除' }
  }

  const response = await axios.delete(`${API_BASE_URL}/users/${userId}`, {
    headers: getAuthHeaders()
  })
  return response.data
}