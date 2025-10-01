// 文章服务 - 处理文章相关的API请求

// 基础API配置
const API_BASE_URL = 'http://localhost:3000/api'; // 替换为您的实际API地址

/**
 * 根据主题获取文章列表
 * @param {string} theme - 文章主题
 * @returns {Promise<Array>} 文章列表
 */
export async function getArticlesByTheme(theme) {
  try {
    const response = await fetch(`${API_BASE_URL}/articles?theme=${encodeURIComponent(theme)}`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    return data.articles || [];
  } catch (error) {
    console.error('获取文章列表失败:', error);
    throw error;
  }
}

/**
 * 获取所有文章列表
 * @returns {Promise<Array>} 文章列表
 */
export async function getAllArticles() {
  try {
    const response = await fetch(`${API_BASE_URL}/articles`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    return data.articles || [];
  } catch (error) {
    console.error('获取所有文章失败:', error);
    throw error;
  }
}

/**
 * 根据ID获取文章详情
 * @param {number} articleId - 文章ID
 * @returns {Promise<Object>} 文章详情
 */
export async function getArticleById(articleId) {
  try {
    const response = await fetch(`${API_BASE_URL}/articles/${articleId}`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    return data.article;
  } catch (error) {
    console.error('获取文章详情失败:', error);
    throw error;
  }
}

/**
 * 创建新文章
 * @param {Object} articleData - 文章数据
 * @returns {Promise<Object>} 创建的文章
 */
export async function createArticle(articleData) {
  try {
    const response = await fetch(`${API_BASE_URL}/articles`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(articleData),
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    return data.article;
  } catch (error) {
    console.error('创建文章失败:', error);
    throw error;
  }
}

/**
 * 更新文章
 * @param {number} articleId - 文章ID
 * @param {Object} articleData - 更新的文章数据
 * @returns {Promise<Object>} 更新后的文章
 */
export async function updateArticle(articleId, articleData) {
  try {
    const response = await fetch(`${API_BASE_URL}/articles/${articleId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(articleData),
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    return data.article;
  } catch (error) {
    console.error('更新文章失败:', error);
    throw error;
  }
}

/**
 * 删除文章
 * @param {number} articleId - 文章ID
 * @returns {Promise<boolean>} 删除是否成功
 */
export async function deleteArticle(articleId) {
  try {
    const response = await fetch(`${API_BASE_URL}/articles/${articleId}`, {
      method: 'DELETE',
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return true;
  } catch (error) {
    console.error('删除文章失败:', error);
    throw error;
  }
} 