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

function normalizeMarkdownContent(content) {
  if (!content) return ''
  return content.replace(/https?:\/\/[^\s)]+/g, (url) => normalizeOssUrl(url))
}

function normalizeArticleItem(article) {
  if (!article) return null
  return {
    ...article,
    image: normalizeOssUrl(article.coverImage || article.image || ''),
    date: article.createdAt || article.date || ''
  }
}

function normalizeArticleDetail(article) {
  if (!article) return null
  return {
    ...article,
    image: normalizeOssUrl(article.coverImage || article.image || ''),
    date: article.createdAt || article.date || '',
    content: normalizeMarkdownContent(article.content || '')
  }
}

export async function getArticlesByTheme(theme) {
  try {
    const response = await axios.get(`${PUBLIC_BASE_URL}/articles`, {
      params: { theme }
    })
    const data = response.data
    if (data.code !== undefined) {
      if (data.code !== '0000') {
        throw new Error(data.info || '获取文章列表失败')
      }
    }
    const list = data.data?.articles || data.data || data.articles || []
    return list.map(normalizeArticleItem)
  } catch (error) {
    console.error('获取文章列表失败:', error)
    throw error
  }
}

export async function getAllArticles() {
  try {
    const response = await axios.get(`${ADMIN_BASE_URL}/articles`, {
      params: { page: 1, pageSize: 100 },
      headers: getAuthHeaders()
    })
    if (response.data.code !== '0000') {
      throw new Error(response.data.info || '获取所有文章失败')
    }
    const list = response.data.data?.articles || response.data.data || []
    return list.map(normalizeArticleItem)
  } catch (error) {
    console.error('获取所有文章失败:', error)
    throw error
  }
}

export async function getArticleById(articleId) {
  try {
    const response = await axios.get(`${ADMIN_BASE_URL}/articles/${articleId}`, {
      headers: getAuthHeaders()
    })
    if (response.data.code !== '0000') {
      throw new Error(response.data.info || '获取文章详情失败')
    }
    return normalizeArticleDetail(response.data.data)
  } catch (error) {
    console.error('获取文章详情失败:', error)
    throw error
  }
}

export async function getPublicArticleDetail(articleId) {
  try {
    const response = await axios.get(`${PUBLIC_BASE_URL}/articles/${articleId}`)
    const data = response.data
    if (data.code !== undefined) {
      if (data.code !== '0000') {
        throw new Error(data.info || '获取文章详情失败')
      }
      return normalizeArticleDetail(data.data)
    }
    return normalizeArticleDetail(data)
  } catch (error) {
    console.error('获取文章详情失败:', error)
    throw error
  }
}

export async function createArticle(articleData) {
  try {
    const payload = {
      title: articleData.title,
      content: articleData.content,
      summary: articleData.summary || '',
      theme: articleData.theme || '',
      coverImage: articleData.image || articleData.coverImage || '',
      status: articleData.status || 'PUBLISHED'
    }
    const response = await axios.post(`${ADMIN_BASE_URL}/articles`, payload, {
      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
    })
    if (response.data.code !== '0000') {
      throw new Error(response.data.info || '创建文章失败')
    }
    return normalizeArticleDetail(response.data.data)
  } catch (error) {
    console.error('创建文章失败:', error)
    throw error
  }
}

export async function updateArticle(articleId, articleData) {
  try {
    const payload = {
      title: articleData.title,
      content: articleData.content,
      summary: articleData.summary || '',
      theme: articleData.theme || '',
      coverImage: articleData.image || articleData.coverImage || '',
      status: articleData.status || 'PUBLISHED'
    }
    const response = await axios.put(`${ADMIN_BASE_URL}/articles/${articleId}`, payload, {
      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
    })
    if (response.data.code !== '0000') {
      throw new Error(response.data.info || '更新文章失败')
    }
    return normalizeArticleDetail(response.data.data)
  } catch (error) {
    console.error('更新文章失败:', error)
    throw error
  }
}

export async function deleteArticle(articleId) {
  try {
    const response = await axios.delete(`${ADMIN_BASE_URL}/articles/${articleId}`, {
      headers: getAuthHeaders()
    })
    if (response.data.code !== '0000') {
      throw new Error(response.data.info || '删除文章失败')
    }
    return true
  } catch (error) {
    console.error('删除文章失败:', error)
    throw error
  }
}
