import axios from 'axios'
import defaultAvatar from '@/assets/images/avatar.jpg'
import school1Image from '@/assets/images/school1.jpeg'
import school2Image from '@/assets/images/school2.jpeg'

const PUBLIC_BASE_URL = 'http://localhost:8080/api/public'
const ADMIN_BASE_URL = 'http://localhost:8080/api/admin'

function getAuthHeaders() {
  const token = localStorage.getItem('jwtToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function unwrapAdminResponse(response, fallbackMessage) {
  if (response.data.code !== '0000') {
    throw new Error(response.data.info || fallbackMessage)
  }
  return response.data.data
}

function normalizeAsset(value, fallback) {
  if (!value || value.startsWith('@/assets/')) {
    return fallback
  }
  return value
}

export const DEFAULT_PROFILE = {
  avatar: defaultAvatar,
  name: 'Idealer',
  bio: 'Java开发工程师 | 大模型应用开发工程师 | 大模型算法工程师',
  email: '2755027635@qq.com',
  githubName: 'idealer-liXi',
  githubUrl: 'https://github.com/idealer-liXi',
  location: '辽宁-沈阳',
  introduction: '具备扎实的前端、后端开发能力，熟悉Vue、SpringBoot等主流框架，热爱开源和技术分享。喜欢设计美观、体验流畅的Web应用，追求代码的优雅与高效。乐于团队协作，善于沟通，持续学习新技术。',
  hobbies: ['编程与算法', '开源社区', '技术分享', '音乐与吉他', '美食探索'],
  schools: [
    {
      schoolKey: 'undergraduate',
      image: school1Image,
      name: '安徽理工大学',
      tags: ['省部共建', '中西部高校基础能力建设工程'],
      description: '坐落于安徽省淮南市，是安徽省高等教育振兴计划‘地方特色高水平大学建设’项目立项建设高校。',
      honors: ['国家级大学生创新创业训练计划', '全国大学生数学建模竞赛一等奖']
    },
    {
      schoolKey: 'graduate',
      image: school2Image,
      name: '东北大学',
      tags: ['985', '211', '双一流'],
      description: '坐落于辽宁省沈阳市，是教育部直属的全国重点大学，由教育部、国防科工局、辽宁省、沈阳市共建。',
      honors: ['暂无~~']
    }
  ]
}

function mergeProfile(payload) {
  if (!payload) return DEFAULT_PROFILE

  const schools = DEFAULT_PROFILE.schools.map((school) => {
    const matched = (payload.schools || []).find(item => item.schoolKey === school.schoolKey) || {}
    return {
      ...school,
      honors: Array.isArray(matched.honors) && matched.honors.length > 0 ? matched.honors : school.honors
    }
  })

  return {
    avatar: normalizeAsset(payload.avatar, DEFAULT_PROFILE.avatar),
    name: payload.name || DEFAULT_PROFILE.name,
    bio: payload.bio || DEFAULT_PROFILE.bio,
    email: payload.email || DEFAULT_PROFILE.email,
    githubName: payload.githubName || DEFAULT_PROFILE.githubName,
    githubUrl: payload.githubUrl || DEFAULT_PROFILE.githubUrl,
    location: payload.location || DEFAULT_PROFILE.location,
    introduction: payload.introduction || DEFAULT_PROFILE.introduction,
    hobbies: Array.isArray(payload.hobbies) && payload.hobbies.length > 0 ? payload.hobbies : DEFAULT_PROFILE.hobbies,
    schools
  }
}

export async function getPublicProfile() {
  const response = await axios.get(`${PUBLIC_BASE_URL}/profile`)
  return mergeProfile(response.data)
}

export async function getAdminProfile() {
  const response = await axios.get(`${ADMIN_BASE_URL}/profile`, {
    headers: getAuthHeaders()
  })
  return mergeProfile(unwrapAdminResponse(response, '获取站主介绍失败'))
}

export async function updateProfile(payload) {
  const response = await axios.put(`${ADMIN_BASE_URL}/profile`, payload, {
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
  })
  return mergeProfile(unwrapAdminResponse(response, '更新站主介绍失败'))
}
