<template>
  <div class="image-management">
    <div class="panel-shell">
      <div class="list-header">
        <div class="title-block">
          <span class="section-kicker">Media Archive</span>
          <h2 class="list-title">{{ currentPanelTitle }}</h2>
          <p class="list-subtitle" v-if="currentPanelSubtitle">{{ currentPanelSubtitle }}</p>
          <button v-else class="btn-back" @click="exitFolder">
            <i class="bi bi-arrow-left"></i>
            <span>{{ isProjectTab ? '返回项目列表' : isUserTab ? '返回用户列表' : '返回文件夹' }}</span>
          </button>
        </div>

        <div class="header-actions">
          <div class="header-stat">
            <span class="stat-label">{{ headerStatLabel }}</span>
            <strong>{{ headerStatValue }}</strong>
            <span class="stat-hint">{{ headerStatHint }}</span>
          </div>

          <button v-if="viewMode === 'images' && !isProjectTab && !isUserTab" class="btn-upload" @click="triggerUpload" :disabled="uploading">
            <i class="bi" :class="uploading ? 'bi-arrow-repeat spin' : 'bi-cloud-upload'"></i>
            {{ uploading ? '上传中...' : '上传图片' }}
          </button>
          <input type="file" ref="fileInput" class="hidden-input" accept="image/*" multiple @change="handleFileSelect" />
        </div>
      </div>

      <div v-if="loading" class="loading-state panel-state">
        <div class="loading-spinner"></div>
        <span>正在加载图片...</span>
      </div>

      <div v-else-if="error" class="error-state panel-state">
        <i class="bi bi-exclamation-circle"></i>
        <span>{{ error }}</span>
        <button class="btn-retry" @click="loadImages">重试</button>
      </div>

      <template v-else>
        <div v-if="viewMode === 'folder'" class="folder-grid">
          <template v-if="isProjectTab">
            <button
              v-for="project in projectCards"
              :key="project.projectId"
              type="button"
              class="folder-cover-card project-cover-card"
              @click="enterProject(project.projectId)"
            >
              <div class="folder-cover-thumb">
                <img v-if="project.coverImage" :src="project.coverImage" :alt="project.projectName" />
                <div v-else class="folder-cover-empty">
                  <i class="bi bi-card-image"></i>
                </div>
                <div class="folder-cover-count project-cover-count" :class="project.hasCover ? 'has-cover' : 'no-cover'">
                  <i class="bi" :class="project.hasCover ? 'bi-check-circle' : 'bi-dash-circle'"></i>
                  <span>{{ project.hasCover ? '已有封面' : '无封面' }}</span>
                </div>
              </div>
              <div class="folder-cover-info">
                <span class="folder-cover-name">{{ project.projectName }}</span>
                <span class="folder-cover-meta">项目 ID · {{ project.projectId }}</span>
                <span class="status-badge" :class="getStatusClass(getProjectStatus(project))">{{ getProjectStatus(project) || '未知状态' }}</span>
              </div>
            </button>
          </template>

          <template v-else-if="isUserTab">
            <button
              v-for="user in userAvatarCards"
              :key="user.userId"
              type="button"
              class="folder-cover-card user-cover-card"
              @click="enterUserAvatar(user.userId)"
            >
              <div class="folder-cover-thumb user-cover-thumb">
                <img v-if="user.effectiveAvatar" :src="user.effectiveAvatar" :alt="user.displayName || user.username" />
                <div v-else class="folder-cover-empty user-avatar-fallback-card">
                  <span>{{ getUserInitial(user) }}</span>
                </div>
                <div class="folder-cover-count user-cover-source">
                  <i class="bi bi-person-circle"></i>
                  <span>{{ getAvatarSourceLabel(user.avatarSource) }}</span>
                </div>
              </div>
              <div class="folder-cover-info">
                <span class="folder-cover-name">{{ user.displayName || user.username }}</span>
                <span class="folder-cover-meta">{{ user.username ? `@${user.username}` : '微信用户' }}</span>
                <span class="folder-cover-meta">{{ user.hasUploadedAvatar ? '有上传头像' : '无上传头像' }} · {{ user.hasWeixinAvatar ? '有微信头像' : '无微信头像' }}</span>
              </div>
            </button>
          </template>

          <template v-else>
          <button
            v-for="folder in currentFolders"
            :key="folder.key"
            type="button"
            class="folder-cover-card"
            @click="enterFolder(folder.key)"
          >
            <div class="folder-cover-thumb">
              <img v-if="folder.cover" :src="folder.cover" :alt="folder.name" />
              <div v-else class="folder-cover-empty">
                <i class="bi bi-folder2-open"></i>
              </div>
              <div class="folder-cover-count">
                <i class="bi bi-images"></i>
                <span>{{ folder.images.length }}</span>
              </div>
            </div>
            <div class="folder-cover-info">
              <span class="folder-cover-name">{{ folder.name }}</span>
              <span class="folder-cover-meta">{{ folder.images.length }} 张图片</span>
            </div>
          </button>
          </template>
        </div>

        <div v-else>
          <div v-if="isProjectTab" class="project-detail panel-state">
            <div class="project-detail-media">
              <img
                v-if="projectHasCover"
                :src="projectDetail.coverImage"
                :alt="projectDetail.projectName"
                class="project-detail-image"
              />
              <div v-else class="empty-state project-detail-empty">
                <i class="bi bi-card-image"></i>
                <p>当前项目还没有封面图</p>
                <p class="empty-hint">请前往项目管理页为该项目设置封面后再回来查看。</p>
              </div>
            </div>

            <div v-if="projectDetail" class="project-detail-sidebar">
              <span class="section-kicker preview-kicker">Project Cover</span>
              <h3 class="preview-title">{{ projectDetail.projectName }}</h3>
              <div class="preview-detail">
                <span class="detail-label">项目 ID</span>
                <span class="detail-value">{{ projectDetail.projectId }}</span>
              </div>
              <div class="preview-detail">
                <span class="detail-label">项目状态</span>
                <span class="detail-value"><span class="status-badge" :class="getStatusClass(getProjectStatus(projectDetail))">{{ getProjectStatus(projectDetail) || '未知状态' }}</span></span>
              </div>
              <div class="preview-detail">
                <span class="detail-label">封面状态</span>
                <span class="detail-value">{{ projectHasCover ? '已有封面' : '无封面' }}</span>
              </div>
              <div v-if="projectDetail.updatedAt" class="preview-detail">
                <span class="detail-label">更新时间</span>
                <span class="detail-value">{{ formatDate(projectDetail.updatedAt) }}</span>
              </div>
              <div v-if="projectHasCover" class="preview-detail">
                <span class="detail-label">封面 URL</span>
                <span class="detail-value url-value">{{ projectDetail.coverImage }}</span>
              </div>
              <div v-if="projectHasCover" class="preview-actions">
                <button class="btn-copy" @click="copyToClipboard(projectDetail.coverImage)">
                  <i class="bi bi-clipboard"></i>
                  {{ copiedUrl ? '已复制' : '复制 URL' }}
                </button>
              </div>
              <div v-if="projectHasCover" class="preview-actions preview-actions-stack">
                <button class="btn-delete" @click="confirmDelete(projectDetail)">
                  <i class="bi bi-trash3"></i>
                  清除封面
                </button>
              </div>
            </div>
          </div>

          <div v-else-if="isUserTab" class="project-detail panel-state user-avatar-detail">
            <div class="project-detail-media user-avatar-preview-grid">
              <div class="user-avatar-preview-card user-avatar-current-card">
                <span class="preview-source-label"><i class="bi bi-check-circle-fill"></i> 当前生效</span>
                <img v-if="userAvatarDetail?.effectiveAvatar" :src="userAvatarDetail.effectiveAvatar" :alt="userAvatarDetail?.displayName || userAvatarDetail?.username" class="project-detail-image user-avatar-image" />
                <div v-else class="empty-state project-detail-empty compact-empty user-avatar-letter-empty">
                  <span>{{ getUserInitial(userAvatarDetail) }}</span>
                </div>
              </div>
              <div class="user-avatar-preview-card">
                <span class="preview-source-label">默认头像</span>
                <div class="empty-state project-detail-empty compact-empty user-avatar-letter-empty">
                  <span>{{ getUserInitial(userAvatarDetail) }}</span>
                </div>
              </div>
              <div class="user-avatar-preview-card" :class="{ disabled: !userAvatarDetail?.hasUploadedAvatar }">
                <span class="preview-source-label">上传头像</span>
                <img v-if="userAvatarDetail?.hasUploadedAvatar" :src="userAvatarDetail.avatar" alt="上传头像" class="project-detail-image user-avatar-image" />
                <div v-else class="empty-state project-detail-empty compact-empty user-avatar-empty-state">
                  <i class="bi bi-cloud-upload"></i>
                  <p>暂无上传</p>
                </div>
              </div>
              <div class="user-avatar-preview-card" :class="{ disabled: !userAvatarDetail?.hasWeixinAvatar }">
                <span class="preview-source-label">微信头像</span>
                <img v-if="userAvatarDetail?.hasWeixinAvatar" :src="userAvatarDetail.weixinImageUrl" alt="微信头像" class="project-detail-image user-avatar-image" />
                <div v-else class="empty-state project-detail-empty compact-empty user-avatar-empty-state">
                  <i class="bi bi-wechat"></i>
                  <p>暂无微信</p>
                </div>
              </div>
            </div>

            <div v-if="userAvatarDetail" class="project-detail-sidebar">
              <span class="section-kicker preview-kicker">User Avatar</span>
              <h3 class="preview-title">{{ userAvatarDetail.displayName || userAvatarDetail.username }}</h3>
              <div class="preview-detail">
                <span class="detail-label">用户 ID</span>
                <span class="detail-value">{{ userAvatarDetail.userId }}</span>
              </div>
              <div class="preview-detail">
                <span class="detail-label">用户名</span>
                <span class="detail-value">{{ userAvatarDetail.username || '—' }}</span>
              </div>
              <div class="preview-detail">
                <span class="detail-label">登录方式</span>
                <span class="detail-value">{{ userAvatarDetail.loginType === 'weixin' ? '微信登录' : '密码登录' }}</span>
              </div>
              <div class="preview-detail">
                <span class="detail-label">当前来源</span>
                <span class="detail-value">{{ getAvatarSourceLabel(userAvatarDetail.avatarSource) }}</span>
              </div>
              <div class="preview-actions user-avatar-actions-grid">
                <button class="btn-copy" @click="updateUserAvatar(userAvatarDetail.userId, { avatarSource: 'DEFAULT' }).then(loadImages)">
                  <i class="bi bi-person-circle"></i>
                  使用默认头像
                </button>
                <button class="btn-copy btn-upload-avatar" @click="triggerUpload" :disabled="uploading">
                  <i class="bi" :class="uploading ? 'bi-arrow-repeat spin' : 'bi-cloud-upload'"></i>
                  {{ uploading ? '上传中...' : '上传并使用站内头像' }}
                </button>
                <button class="btn-copy btn-wechat-avatar" @click="updateUserAvatar(userAvatarDetail.userId, { avatarSource: 'WECHAT' }).then(loadImages)" :disabled="!userAvatarDetail.hasWeixinAvatar">
                  <i class="bi bi-wechat"></i>
                  使用微信头像
                </button>
              </div>
              <div class="preview-actions preview-actions-stack">
                <button class="btn-delete" @click="confirmDelete(userAvatarDetail)" :disabled="!userAvatarDetail.hasUploadedAvatar">
                  <i class="bi bi-trash3"></i>
                  清空上传头像
                </button>
              </div>
            </div>
          </div>

          <template v-else>
          <div class="filter-bar">
            <div class="search-box">
              <i class="bi bi-search"></i>
              <input type="text" v-model="searchKeyword" placeholder="搜索文件名 / 后缀 ..." class="search-input" />
            </div>
            <div class="results-chip">
              <span>筛选结果</span>
              <strong>{{ filteredImages.length }}</strong>
              <em>/ {{ selectedFolder ? selectedFolder.images.length : currentTabImages.length }}</em>
            </div>
          </div>

          <div v-if="filteredImages.length === 0" class="empty-state panel-state">
            <i class="bi bi-image"></i>
            <p>暂无图片</p>
            <p class="empty-hint">点击上方"上传图片"按钮添加素材。</p>
          </div>

          <div v-else class="image-grid">
            <div
              v-for="(img, index) in filteredImages"
              :key="img.id"
              class="image-card"
              @click="openPreview(img)"
            >
              <div class="image-card-chrome">
                <span class="image-index">{{ String(index + 1).padStart(2, '0') }}</span>
                <span class="image-format">{{ getFileExtension(img.name) }}</span>
              </div>
              <div class="image-thumb">
                <img :src="img.url" :alt="img.name" loading="lazy" />
              </div>
              <div class="image-info">
                <span class="image-name" :title="img.name">{{ img.name }}</span>
                <span class="image-meta">{{ img.size }} · {{ formatDate(img.uploadedAt) }}</span>
                <span class="image-action-hint">点击查看详情与引用</span>
              </div>
            </div>
          </div>
          </template>
        </div>
      </template>
    </div>

    <div v-if="previewImage" class="modal-overlay" @click.self="closePreview">
      <div class="preview-modal">
        <button class="preview-close" @click="closePreview">
          <i class="bi bi-x-lg"></i>
        </button>
        <div class="preview-body">
          <div class="preview-image-wrapper">
            <img :src="previewImage.url" :alt="previewImage.name" class="preview-img" />
          </div>
          <div class="preview-sidebar">
            <span class="section-kicker preview-kicker">Asset Detail</span>
            <h3 class="preview-title">图片详情</h3>
            <div class="preview-detail">
              <span class="detail-label">文件名</span>
              <span class="detail-value">{{ previewImage.name }}</span>
            </div>
            <div class="preview-detail">
              <span class="detail-label">大小</span>
              <span class="detail-value">{{ previewImage.size }}</span>
            </div>
            <div class="preview-detail">
              <span class="detail-label">上传时间</span>
              <span class="detail-value">{{ formatDate(previewImage.uploadedAt) }}</span>
            </div>
            <div class="preview-detail">
              <span class="detail-label">URL</span>
              <span class="detail-value url-value">{{ previewImage.url }}</span>
            </div>
            <div class="preview-actions">
              <button class="btn-copy" @click="copyToClipboard(previewImage.url)">
                <i class="bi bi-clipboard"></i>
                {{ copiedUrl ? '已复制' : '复制 URL' }}
              </button>
              <button class="btn-copy btn-copy-md" @click="copyToClipboard(`![${previewImage.name}](${previewImage.url})`)">
                <i class="bi bi-markdown"></i>
                {{ copiedMd ? '已复制' : '复制 Markdown' }}
              </button>
            </div>
            <div class="preview-actions preview-actions-stack">
              <button class="btn-delete" @click="confirmDelete(previewImage)">
                <i class="bi bi-trash3"></i>
                删除图片
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <i class="bi bi-exclamation-triangle modal-icon"></i>
          <h3>确认删除</h3>
        </div>
        <p class="modal-body">{{ deleteModalBody }}</p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDeleteModal = false">取消</button>
          <button class="btn-confirm-delete" @click="handleDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  getImages,
  deleteImage,
  uploadImage,
  getProjectImages,
  getProjectImageByProjectId,
  clearProjectImage
} from '@/services/uploadService.js'
import {
  getUserAvatarImages,
  getUserAvatarImageByUserId,
  updateUserAvatar,
  clearUserAvatar
} from '@/services/userService.js'

const props = defineProps({
  defaultTab: {
    type: String,
    default: 'DOCUMENT'
  }
})

const activeTab = ref(props.defaultTab)
const viewMode = ref('folder')
const images = ref([])
const loading = ref(false)
const error = ref(null)
const uploading = ref(false)
const searchKeyword = ref('')
const previewImage = ref(null)
const showDeleteModal = ref(false)
const deleteTarget = ref(null)
const copiedUrl = ref(false)
const copiedMd = ref(false)
const fileInput = ref(null)
const selectedFolderKey = ref('')
const projectCards = ref([])
const selectedProjectId = ref(null)
const projectDetail = ref(null)
const projectDetailRequestId = ref(0)
const userAvatarCards = ref([])
const selectedUserAvatarId = ref(null)
const userAvatarDetail = ref(null)
const userAvatarRequestId = ref(0)

const tabLabels = {
  DOCUMENT: '文档图片',
  PROJECT: '项目图片',
  USER: '用户图片'
}

const activeTabLabel = computed(() => tabLabels[activeTab.value] || '文档图片')
const isProjectTab = computed(() => activeTab.value === 'PROJECT')
const isUserTab = computed(() => activeTab.value === 'USER')

const currentTabImages = computed(() => {
  return images.value.filter(img => (img.imageType || 'DOCUMENT') === activeTab.value)
})

const currentFolders = computed(() => {
  if (isProjectTab.value || isUserTab.value) return []
  const folders = new Map()
  for (const image of currentTabImages.value) {
    const key = image.folderKey || 'ungrouped'
    if (!folders.has(key)) {
      folders.set(key, {
        key,
        name: image.folderName || '未分组图片',
        images: [],
        cover: null
      })
    }
    const folder = folders.get(key)
    folder.images.push(image)
    if (!folder.cover && image.url) {
      folder.cover = image.url
    }
  }
  return Array.from(folders.values()).sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
})

const selectedFolder = computed(() => {
  return currentFolders.value.find(folder => folder.key === selectedFolderKey.value) || null
})

const currentFolderName = computed(() => {
  return selectedFolder.value ? selectedFolder.value.name : activeTabLabel.value
})

const currentPanelTitle = computed(() => {
  if (isProjectTab.value && viewMode.value === 'images') {
    return projectDetail.value?.projectName || activeTabLabel.value
  }
  if (isUserTab.value && viewMode.value === 'images') {
    return userAvatarDetail.value?.displayName || userAvatarDetail.value?.username || activeTabLabel.value
  }
  return viewMode.value === 'folder' ? activeTabLabel.value : currentFolderName.value
})

const currentPanelSubtitle = computed(() => {
  if (viewMode.value !== 'folder') return ''
  if (isProjectTab.value) return '按项目浏览和管理每个项目的封面图。'
  if (isUserTab.value) return '按用户浏览和管理头像来源。'
  return '按文件夹浏览和管理你的图片资源。'
})

const headerStatLabel = computed(() => {
  if (isProjectTab.value) {
    return viewMode.value === 'folder' ? '项目' : '封面'
  }
  if (isUserTab.value) {
    return viewMode.value === 'folder' ? '用户' : '头像'
  }
  return viewMode.value === 'folder' ? '文件夹' : '图片'
})

const headerStatValue = computed(() => {
  if (isProjectTab.value) {
    return viewMode.value === 'folder' ? projectCards.value.length : (projectHasCover.value ? 1 : 0)
  }
  if (isUserTab.value) {
    return viewMode.value === 'folder' ? userAvatarCards.value.length : 1
  }
  return viewMode.value === 'folder' ? currentFolders.value.length : filteredImages.value.length
})

const headerStatHint = computed(() => {
  if (isProjectTab.value) {
    return viewMode.value === 'folder' ? '个项目' : (projectHasCover.value ? '张封面' : '无封面')
  }
  if (isUserTab.value) {
    return viewMode.value === 'folder' ? '位用户' : '个来源视图'
  }
  return viewMode.value === 'folder' ? '个分类' : '张图片'
})

const projectHasCover = computed(() => {
  return Boolean(projectDetail.value?.hasCover && projectDetail.value?.coverImage)
})

const deleteModalBody = computed(() => {
  if (isProjectTab.value && deleteTarget.value?.projectId != null) {
    return `确定要清除项目「${deleteTarget.value.projectName}」的封面图吗？项目会恢复为无封面状态。`
  }
  if (isUserTab.value && deleteTarget.value?.userId != null) {
    return `确定要清除用户「${deleteTarget.value.displayName || deleteTarget.value.username}」的上传头像吗？系统会切回默认头像。`
  }
  return `确定要删除图片「${deleteTarget.value?.name}」吗？文章中引用的图片链接将失效。`
})

const getStatusClass = (status) => {
  const statusMap = {
    '进行中': 'status-progress',
    '已完成': 'status-completed',
    '暂停': 'status-paused'
  }
  return statusMap[status] || 'status-default'
}

const getProjectStatus = (project) => {
  return project?.projectStatus || ''
}

const getAvatarSourceLabel = (avatarSource) => {
  return avatarSource === 'UPLOAD' ? '上传头像' : avatarSource === 'WECHAT' ? '微信头像' : '默认头像'
}

const getUserInitial = (user) => {
  const source = user?.displayName || user?.username || user?.weixinName || '?'
  return source.slice(0, 1).toUpperCase()
}

const enterFolder = (folderKey) => {
  selectedFolderKey.value = folderKey
  viewMode.value = 'images'
  searchKeyword.value = ''
}

const loadProjectDetail = async (projectId, switchView = false) => {
  const requestId = ++projectDetailRequestId.value
  const detail = await getProjectImageByProjectId(projectId)

  if (requestId !== projectDetailRequestId.value || selectedProjectId.value !== projectId) {
    return false
  }

  projectDetail.value = detail
  if (switchView) {
    viewMode.value = 'images'
  }

  return true
}

const enterProject = async (projectId) => {
  selectedProjectId.value = projectId
  loading.value = true
  error.value = null
  try {
    await loadProjectDetail(projectId, true)
  } catch {
    if (selectedProjectId.value === projectId) {
      error.value = '获取项目封面详情失败'
      projectDetail.value = null
    }
  } finally {
    if (selectedProjectId.value === projectId) {
      loading.value = false
    }
  }
}

const loadUserAvatarDetail = async (userId, switchView = false) => {
  const requestId = ++userAvatarRequestId.value
  const detail = await getUserAvatarImageByUserId(userId)

  if (requestId !== userAvatarRequestId.value || selectedUserAvatarId.value !== userId) {
    return false
  }

  userAvatarDetail.value = detail
  if (switchView) {
    viewMode.value = 'images'
  }
  return true
}

const enterUserAvatar = async (userId) => {
  selectedUserAvatarId.value = userId
  loading.value = true
  error.value = null
  try {
    await loadUserAvatarDetail(userId, true)
  } catch {
    if (selectedUserAvatarId.value === userId) {
      error.value = '获取用户头像详情失败'
      userAvatarDetail.value = null
    }
  } finally {
    if (selectedUserAvatarId.value === userId) {
      loading.value = false
    }
  }
}

const exitFolder = () => {
  viewMode.value = 'folder'
  selectedFolderKey.value = ''
  searchKeyword.value = ''
  if (isProjectTab.value) {
    projectDetailRequestId.value++
    selectedProjectId.value = null
    projectDetail.value = null
    loading.value = false
  }
  if (isUserTab.value) {
    userAvatarRequestId.value++
    selectedUserAvatarId.value = null
    userAvatarDetail.value = null
    loading.value = false
  }
}

const filteredImages = computed(() => {
  if (isProjectTab.value || isUserTab.value) return []
  const source = selectedFolder.value ? selectedFolder.value.images : currentTabImages.value
  if (!searchKeyword.value) return source
  const kw = searchKeyword.value.toLowerCase()
  return source.filter(img => img.name.toLowerCase().includes(kw))
})

const uploadDirectory = computed(() => {
  return 'documents'
})

const ensureSelectedFolder = () => {
  if (isProjectTab.value || isUserTab.value) return
  if (!currentFolders.value.length) {
    selectedFolderKey.value = ''
    return
  }
  if (viewMode.value === 'images' && selectedFolderKey.value && !currentFolders.value.some(folder => folder.key === selectedFolderKey.value)) {
    selectedFolderKey.value = currentFolders.value[0].key
  }
}

const loadImages = async () => {
  loading.value = true
  error.value = null
  try {
    if (isProjectTab.value) {
      projectCards.value = await getProjectImages()
      if (viewMode.value === 'images' && selectedProjectId.value) {
        await loadProjectDetail(selectedProjectId.value)
      } else {
        projectDetailRequestId.value++
        viewMode.value = 'folder'
        selectedProjectId.value = null
        projectDetail.value = null
      }
    } else if (isUserTab.value) {
      userAvatarCards.value = await getUserAvatarImages()
      if (viewMode.value === 'images' && selectedUserAvatarId.value) {
        await loadUserAvatarDetail(selectedUserAvatarId.value)
      } else {
        userAvatarRequestId.value++
        viewMode.value = 'folder'
        selectedUserAvatarId.value = null
        userAvatarDetail.value = null
      }
    } else {
      images.value = await getImages()
      ensureSelectedFolder()
    }
  } catch {
    error.value = isProjectTab.value ? '获取项目图片失败' : isUserTab.value ? '获取用户头像失败' : '获取图片列表失败'
    projectCards.value = []
    projectDetail.value = null
    userAvatarCards.value = []
    userAvatarDetail.value = null
    images.value = []
  } finally {
    loading.value = false
  }
}

const triggerUpload = () => {
  fileInput.value.click()
}

const handleFileSelect = async (event) => {
  if (isProjectTab.value) {
    event.target.value = ''
    return
  }
  if (isUserTab.value) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file || !selectedUserAvatarId.value) return

    uploading.value = true
    error.value = null
    try {
      const url = await uploadImage(file, {
        directory: `users/${selectedUserAvatarId.value}`
      })
      await updateUserAvatar(selectedUserAvatarId.value, {
        avatarSource: 'UPLOAD',
        avatar: url
      })
      await loadImages()
    } catch {
      error.value = '上传用户头像失败'
    } finally {
      uploading.value = false
    }
    return
  }
  const files = Array.from(event.target.files)
  if (!files.length) return

  uploading.value = true
  let successCount = 0
  const targetFolder = selectedFolder.value || { key: 'manual-upload', name: '手动上传' }
  for (const file of files) {
    try {
      await uploadImage(file, {
        directory: `${uploadDirectory.value}/${encodeURIComponent(targetFolder.name)}`
      })
      successCount++
    } catch {
      // individual file failure, continue
    }
  }
  uploading.value = false
  event.target.value = ''
  if (successCount === 0) {
    error.value = '上传失败，请稍后重试'
  } else {
    await loadImages()
  }
}

const openPreview = (img) => {
  previewImage.value = img
  copiedUrl.value = false
  copiedMd.value = false
}

const closePreview = () => {
  previewImage.value = null
}

const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    if (text.startsWith('![')) {
      copiedMd.value = true
      setTimeout(() => { copiedMd.value = false }, 2000)
    } else {
      copiedUrl.value = true
      setTimeout(() => { copiedUrl.value = false }, 2000)
    }
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    if (text.startsWith('![')) {
      copiedMd.value = true
      setTimeout(() => { copiedMd.value = false }, 2000)
    } else {
      copiedUrl.value = true
      setTimeout(() => { copiedUrl.value = false }, 2000)
    }
  }
}

const confirmDelete = (img) => {
  deleteTarget.value = img
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!deleteTarget.value) return
  try {
  if (isProjectTab.value && deleteTarget.value.projectId != null) {
      await clearProjectImage(deleteTarget.value.projectId)
      projectCards.value = await getProjectImages()
      if (selectedProjectId.value === deleteTarget.value.projectId) {
        await loadProjectDetail(deleteTarget.value.projectId)
      }
    } else if (isUserTab.value && deleteTarget.value.userId != null) {
      await clearUserAvatar(deleteTarget.value.userId)
      userAvatarCards.value = await getUserAvatarImages()
      if (selectedUserAvatarId.value === deleteTarget.value.userId) {
        await loadUserAvatarDetail(deleteTarget.value.userId)
      }
    } else {
      await deleteImage(deleteTarget.value.id)
      images.value = images.value.filter(i => i.id !== deleteTarget.value.id)
      ensureSelectedFolder()
      if (previewImage.value && previewImage.value.id === deleteTarget.value.id) {
        previewImage.value = null
      }
    }
  } catch {
    // error handled
  }
  showDeleteModal.value = false
  deleteTarget.value = null
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const getFileExtension = (fileName) => {
  const extension = fileName.split('.').pop()
  return extension ? extension.toUpperCase() : 'IMG'
}

const resetViewStateForTab = () => {
  viewMode.value = 'folder'
  searchKeyword.value = ''
  selectedFolderKey.value = ''
  previewImage.value = null
  showDeleteModal.value = false
  deleteTarget.value = null
  copiedUrl.value = false
  copiedMd.value = false
  projectDetailRequestId.value++
  selectedProjectId.value = null
  projectDetail.value = null
  userAvatarRequestId.value++
  selectedUserAvatarId.value = null
  userAvatarDetail.value = null
}

watch(
  () => props.defaultTab,
  async (nextTab, prevTab) => {
    if (nextTab === prevTab) return
    activeTab.value = nextTab
    resetViewStateForTab()
    await loadImages()
  }
)

onMounted(() => {
  loadImages()
})
</script>

<style scoped>
.image-management {
  --bg: #121212;
  --bg-soft: #1a1a1a;
  --panel: rgba(20, 20, 20, 0.86);
  --panel-strong: rgba(9, 9, 9, 0.96);
  --line: rgba(255, 244, 214, 0.12);
  --line-strong: rgba(255, 244, 214, 0.22);
  --text: #f7f1e3;
  --muted: rgba(247, 241, 227, 0.64);
  --dim: rgba(247, 241, 227, 0.42);
  --accent: #c58d2d;
  --accent-soft: rgba(197, 141, 45, 0.16);
  --danger: #ff6b57;
  --danger-soft: rgba(255, 107, 87, 0.14);
  padding: 8px;
  color: var(--text);
  font-family: 'Palatino Linotype', 'Book Antiqua', Georgia, serif;
}

.panel-shell {
  position: relative;
  overflow: hidden;
  padding: 28px;
  border-radius: 28px;
  border: 1px solid var(--line);
  background:
    radial-gradient(circle at top right, rgba(197, 141, 45, 0.16), transparent 28%),
    radial-gradient(circle at left center, rgba(255, 255, 255, 0.05), transparent 20%),
    linear-gradient(145deg, rgba(255, 248, 232, 0.04), rgba(0, 0, 0, 0.12)),
    var(--panel);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.04),
    0 24px 60px rgba(0, 0, 0, 0.28);
}

.panel-shell::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(transparent 0, rgba(255, 255, 255, 0.02) 50%, transparent 100%),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.015) 0 1px, transparent 1px 12px);
  opacity: 0.6;
}

.panel-shell::after {
  content: '';
  position: absolute;
  inset: 14px;
  border-radius: 20px;
  border: 1px solid rgba(255, 244, 214, 0.06);
  pointer-events: none;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 22px;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.title-block {
  max-width: 560px;
}

.section-kicker {
  display: inline-block;
  margin-bottom: 10px;
  color: var(--accent);
  font-size: 0.72rem;
  letter-spacing: 0.32em;
  text-transform: uppercase;
}

.list-subtitle {
  margin: 10px 0 0;
  max-width: 44ch;
  color: var(--muted);
  font-size: 0.95rem;
  line-height: 1.65;
}

.list-title {
  font-size: clamp(2rem, 4vw, 2.8rem);
  font-weight: 700;
  color: var(--text);
  margin: 0;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-stat {
  min-width: 118px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid var(--line);
  background: rgba(255, 248, 232, 0.035);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.header-stat strong {
  display: block;
  margin: 2px 0;
  color: var(--text);
  font-size: 1.7rem;
  line-height: 1;
}

.stat-label,
.stat-hint {
  display: block;
  color: var(--dim);
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.btn-upload {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 54px;
  padding: 0 22px;
  background: linear-gradient(135deg, #d7a347, #b27718);
  color: #1a1206;
  border: 1px solid rgba(255, 236, 196, 0.35);
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.88rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease;
  box-shadow: 0 16px 34px rgba(138, 90, 15, 0.34);
  white-space: nowrap;
}

.btn-upload:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(138, 90, 15, 0.42);
  filter: saturate(1.08);
}

.btn-upload:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.spin {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.hidden-input {
  display: none;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.folder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  position: relative;
  z-index: 1;
}

.folder-cover-card {
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  border: 1px solid var(--line);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
  background: rgba(255, 248, 232, 0.04);
  text-align: left;
}

.folder-cover-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.22);
  border-color: var(--line-strong);
}

.folder-cover-thumb {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #26211a, #151515);
}

.folder-cover-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease;
}

.folder-cover-card:hover .folder-cover-thumb img {
  transform: scale(1.06);
}

.folder-cover-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(83, 120, 214, 0.08), rgba(83, 120, 214, 0.03));
}

.folder-cover-empty i {
  font-size: 2.8rem;
  color: var(--dim);
  opacity: 0.5;
}

.folder-cover-count {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
  color: #fff;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.folder-cover-count i {
  font-size: 0.82rem;
}

.project-cover-count {
  padding-inline: 12px;
}

.project-cover-count.has-cover {
  background: rgba(12, 122, 87, 0.82);
}

.project-cover-count.no-cover {
  background: rgba(92, 108, 138, 0.82);
}

.folder-cover-info {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.folder-cover-name {
  font-size: 0.96rem;
  font-weight: 700;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-cover-meta {
  font-size: 0.82rem;
  color: var(--muted);
  letter-spacing: 0.04em;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 248, 232, 0.04);
  color: var(--muted);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-back:hover {
  background: rgba(255, 248, 232, 0.08);
  border-color: var(--line-strong);
  color: var(--text);
}

.btn-back i {
  font-size: 0.9rem;
}

.search-box {
  flex: 1;
  position: relative;
}

.search-box i {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--dim);
  font-size: 0.9rem;
}

.search-input {
  width: 100%;
  min-height: 56px;
  padding: 12px 18px 12px 44px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 248, 232, 0.03);
  color: var(--text);
  font-size: 0.94rem;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.search-input::placeholder {
  color: var(--dim);
}

.search-input:focus {
  outline: none;
  border-color: var(--line-strong);
  box-shadow: 0 0 0 4px rgba(197, 141, 45, 0.08);
  background: rgba(255, 248, 232, 0.05);
}

.results-chip {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  padding: 0 18px;
  min-height: 56px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 248, 232, 0.035);
  color: var(--muted);
  white-space: nowrap;
}

.results-chip span,
.results-chip em {
  font-size: 0.78rem;
  font-style: normal;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.results-chip strong {
  font-size: 1.4rem;
  color: var(--text);
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
  position: relative;
  z-index: 1;
}

.image-card {
  background: linear-gradient(180deg, rgba(255, 248, 232, 0.05), rgba(255, 248, 232, 0.015));
  border: 1px solid var(--line);
  border-radius: 22px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.2);
  position: relative;
}

.image-card::after {
  content: '';
  position: absolute;
  inset: auto 14px 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 244, 214, 0.28), transparent);
}

.image-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 24px 54px rgba(0, 0, 0, 0.28);
  border-color: var(--line-strong);
}

.image-card-chrome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px 0;
  color: var(--dim);
  font-size: 0.72rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.image-index,
.image-format {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
}

.image-format {
  padding: 0 9px;
  border-radius: 999px;
  border: 1px solid rgba(255, 244, 214, 0.1);
  background: rgba(255, 248, 232, 0.04);
}

.image-thumb {
  margin: 12px 14px 0;
  height: 178px;
  overflow: hidden;
  border-radius: 16px;
  background: linear-gradient(135deg, #26211a, #151515);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.image-thumb::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.28), transparent 50%);
  pointer-events: none;
}

.image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease, filter 0.45s ease;
}

.image-card:hover .image-thumb img {
  transform: scale(1.06);
  filter: saturate(1.05) contrast(1.02);
}

.image-info {
  padding: 16px 16px 18px;
}

.image-name {
  display: block;
  font-size: 0.96rem;
  font-weight: 700;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  display: block;
  font-size: 0.76rem;
  color: var(--muted);
  margin-top: 6px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.image-action-hint {
  display: inline-block;
  margin-top: 14px;
  color: var(--accent);
  font-size: 0.74rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.panel-state {
  min-height: 320px;
  border-radius: 24px;
  border: 1px dashed var(--line);
  background: rgba(255, 248, 232, 0.025);
}

.project-detail {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 340px);
  overflow: hidden;
}

.project-detail-media {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: linear-gradient(180deg, #f7f9fd, #eef2f8);
}

.project-detail-image {
  max-width: 100%;
  max-height: 60vh;
  object-fit: contain;
  border-radius: 16px;
  box-shadow: 0 22px 56px rgba(0, 0, 0, 0.16);
}

.project-detail-sidebar {
  padding: 30px 24px;
  border-left: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.68);
}

.project-detail-empty {
  min-height: 100%;
  width: 100%;
}

.user-avatar-detail {
  grid-template-columns: minmax(0, 1.4fr) minmax(300px, 360px);
}

.user-avatar-preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 28px 24px;
  align-content: center;
}

.user-avatar-preview-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 22px 16px 18px;
  border-radius: 20px;
  border: 1px solid var(--line);
  background: rgba(255, 248, 232, 0.035);
  transition: all 0.28s ease;
  position: relative;
}

.user-avatar-preview-card:first-child {
  border-color: rgba(197, 141, 45, 0.35);
  background: rgba(197, 141, 45, 0.06);
  box-shadow: 0 8px 28px rgba(197, 141, 45, 0.1);
}

.user-avatar-preview-card.disabled {
  opacity: 0.45;
  filter: grayscale(0.5);
}

.preview-source-label {
  font-size: 0.68rem;
  color: var(--dim);
  letter-spacing: 0.16em;
  text-transform: uppercase;
  font-weight: 600;
}

.user-avatar-preview-card:first-child .preview-source-label {
  color: var(--accent);
}

.user-avatar-image {
  width: 88px;
  height: 88px;
  max-width: 88px;
  max-height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 2.5px solid var(--line);
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.18);
  flex-shrink: 0;
}

.user-avatar-preview-card:first-child .user-avatar-image {
  border-color: rgba(197, 141, 45, 0.4);
  box-shadow: 0 8px 28px rgba(197, 141, 45, 0.16);
}

.compact-empty {
  min-height: auto !important;
  padding: 0 !important;
  gap: 6px !important;
}

.compact-empty i {
  font-size: 2rem !important;
}

.compact-empty p {
  font-size: 0.78rem;
  margin: 0;
}

.user-avatar-letter-empty {
  width: 88px;
  height: 88px !important;
  border-radius: 50% !important;
  background: linear-gradient(135deg, rgba(197, 141, 45, 0.22), rgba(197, 141, 45, 0.08)) !important;
  border: 2.5px solid rgba(197, 141, 45, 0.25);
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  flex-shrink: 0;
}

.user-avatar-letter-empty span {
  font-size: 2.2rem;
  font-weight: 700;
  color: var(--accent);
  text-transform: uppercase;
}

.user-avatar-actions-grid {
  display: flex;
  gap: 8px;
}

.user-avatar-actions-grid .btn-copy {
  flex: 1;
  min-width: 0;
  justify-content: center;
  white-space: nowrap;
}

.user-avatar-actions-grid .btn-copy i {
  flex-shrink: 0;
}

.user-avatar-current-card {
  grid-column: 1 / -1;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 28px 32px;
}

.user-avatar-current-card .preview-source-label {
  position: absolute;
  top: 12px;
  left: 16px;
}

.user-avatar-current-card .user-avatar-image {
  width: 120px;
  height: 120px;
  max-width: 120px;
  max-height: 120px;
}

.user-avatar-current-card .user-avatar-letter-empty {
  width: 120px;
  height: 120px !important;
}

.user-avatar-current-card .user-avatar-letter-empty span {
  font-size: 3rem;
}

.user-avatar-empty-state {
  background: none;
  border: 2px dashed var(--line);
  border-radius: 50%;
  width: 88px;
  height: 88px;
  margin: 0;
  min-height: auto;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.user-avatar-empty-state i {
  font-size: 1.6rem !important;
}

.user-avatar-empty-state p {
  font-size: 0.68rem;
  color: var(--dim);
  margin: 0;
}

.btn-upload-avatar {
  background: rgba(197, 141, 45, 0.12) !important;
  border-color: rgba(197, 141, 45, 0.25) !important;
  color: #ffd89b !important;
}

.btn-upload-avatar:hover:not(:disabled) {
  background: rgba(197, 141, 45, 0.2) !important;
  color: #fff4d6 !important;
}

.btn-wechat-avatar {
  background: rgba(7, 193, 96, 0.08) !important;
  border-color: rgba(7, 193, 96, 0.2) !important;
  color: #47d67a !important;
}

.btn-wechat-avatar:hover:not(:disabled) {
  background: rgba(7, 193, 96, 0.14) !important;
  color: #6fe09c !important;
}

.btn-wechat-avatar:disabled {
  opacity: 0.4;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.76rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.status-progress {
  color: #1f6b52;
  background: rgba(73, 174, 129, 0.14);
}

.status-completed {
  color: #2857b8;
  background: rgba(83, 120, 214, 0.14);
}

.status-paused {
  color: #9c6227;
  background: rgba(217, 160, 82, 0.18);
}

.status-default {
  color: var(--muted);
  background: rgba(128, 145, 184, 0.14);
}

.loading-state, .error-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--muted);
  gap: 8px;
  position: relative;
  z-index: 1;
}

.loading-spinner {
  width: 34px;
  height: 34px;
  border: 3px solid rgba(197, 141, 45, 0.14);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.error-state {
  color: #ff8f7d;
}

.error-state i {
  font-size: 1.8rem;
}

.btn-retry {
  margin-top: 12px;
  padding: 8px 18px;
  border: 1px solid rgba(255, 143, 125, 0.34);
  border-radius: 999px;
  background: rgba(255, 143, 125, 0.08);
  color: #ffb1a5;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s ease;
}

.btn-retry:hover {
  background: rgba(255, 143, 125, 0.16);
  color: #fff0ec;
}

.empty-state i {
  font-size: 3.4rem;
  color: rgba(247, 241, 227, 0.2);
}

.empty-hint {
  font-size: 0.88rem;
  color: var(--dim);
  margin: 0;
  max-width: 28ch;
  text-align: center;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(5, 5, 5, 0.72);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 24px;
}

.preview-modal {
  background: linear-gradient(180deg, rgba(20, 20, 20, 0.98), rgba(12, 12, 12, 0.98));
  color: var(--text);
  border: 1px solid var(--line);
  border-radius: 28px;
  width: 90vw;
  max-width: 1040px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.42);
  position: relative;
  display: flex;
  flex-direction: column;
}

.preview-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid var(--line);
  background: rgba(255, 248, 232, 0.06);
  color: var(--text);
  font-size: 1.1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
}

.preview-close:hover {
  background: rgba(255, 248, 232, 0.12);
}

.preview-body {
  display: flex;
  overflow: hidden;
}

.preview-image-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at center, rgba(197, 141, 45, 0.08), transparent 34%),
    linear-gradient(135deg, #1f1b16, #0f0f0f);
  padding: 32px;
  min-height: 300px;
  max-height: 70vh;
  overflow: auto;
}

.preview-img {
  max-width: 100%;
  max-height: 65vh;
  object-fit: contain;
  border-radius: 16px;
  box-shadow: 0 22px 56px rgba(0, 0, 0, 0.34);
}

.preview-sidebar {
  width: 310px;
  flex-shrink: 0;
  padding: 30px 24px;
  border-left: 1px solid var(--line);
  overflow-y: auto;
}

.preview-kicker {
  margin-bottom: 12px;
}

.preview-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 20px 0;
  letter-spacing: 0.04em;
}

.preview-detail {
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(255, 244, 214, 0.08);
}

.detail-label {
  display: block;
  font-size: 0.72rem;
  color: var(--dim);
  margin-bottom: 5px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.detail-value {
  display: block;
  font-size: 0.92rem;
  color: var(--text);
  word-break: break-all;
  line-height: 1.6;
}

.url-value {
  font-size: 0.78rem;
  color: #ffd89b;
  background: rgba(197, 141, 45, 0.1);
  padding: 10px 12px;
  border-radius: 12px;
  line-height: 1.4;
  border: 1px solid rgba(197, 141, 45, 0.16);
}

.preview-actions {
  display: flex;
  gap: 8px;
  margin-top: 18px;
}

.preview-actions-stack {
  margin-top: 8px;
}

.btn-copy {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 0.82rem;
  font-weight: 600;
  border: 1px solid rgba(197, 141, 45, 0.18);
  background: rgba(197, 141, 45, 0.08);
  color: #ffd89b;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  justify-content: center;
}

.btn-copy:hover {
  background: rgba(197, 141, 45, 0.16);
  border-color: rgba(197, 141, 45, 0.3);
  color: #fff4d6;
}

.btn-copy-md {
  background: rgba(255, 248, 232, 0.06);
  color: var(--text);
  border-color: rgba(255, 244, 214, 0.12);
}

.btn-copy-md:hover {
  background: rgba(255, 248, 232, 0.12);
  border-color: rgba(255, 244, 214, 0.24);
  color: #fff8ea;
}

.btn-delete {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 0.82rem;
  font-weight: 600;
  border: 1px solid rgba(255, 107, 87, 0.2);
  background: rgba(255, 107, 87, 0.08);
  color: #ff9d90;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-delete:hover {
  background: rgba(255, 107, 87, 0.16);
  color: #fff1ee;
}

.modal-content {
  background: linear-gradient(180deg, rgba(24, 24, 24, 0.98), rgba(13, 13, 13, 0.98));
  border: 1px solid var(--line);
  color: var(--text);
  border-radius: 24px;
  padding: 28px;
  width: 420px;
  max-width: 90vw;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.34);
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.modal-icon {
  font-size: 1.5rem;
  color: var(--accent);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: var(--text);
}

.modal-body {
  color: var(--muted);
  font-size: 0.92rem;
  line-height: 1.5;
  margin: 0 0 24px 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  padding: 8px 20px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 248, 232, 0.03);
  color: var(--muted);
  cursor: pointer;
  font-size: 0.88rem;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: rgba(255, 248, 232, 0.08);
}

.btn-confirm-delete {
  padding: 8px 20px;
  border: 1px solid rgba(255, 107, 87, 0.16);
  border-radius: 999px;
  background: linear-gradient(135deg, #c54a3d, #8f241d);
  color: #fff2ef;
  cursor: pointer;
  font-size: 0.88rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-confirm-delete:hover {
  filter: brightness(1.06);
}

@media (max-width: 768px) {
  .panel-shell {
    padding: 20px;
    border-radius: 22px;
  }

  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .header-stat {
    width: 100%;
  }

  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }

  .image-thumb {
    height: 132px;
  }

  .preview-body {
    flex-direction: column;
  }

  .project-detail {
    grid-template-columns: 1fr;
  }

  .project-detail-sidebar {
    border-left: none;
    border-top: 1px solid var(--line);
  }

  .preview-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid var(--line);
  }

  .preview-actions {
    flex-direction: column;
  }

  .preview-image-wrapper {
    padding: 20px;
  }

  .preview-modal,
  .modal-content {
    border-radius: 20px;
  }

  .user-avatar-detail {
    grid-template-columns: 1fr;
  }

  .user-avatar-preview-grid {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    padding: 20px 16px;
  }

  .user-avatar-image {
    width: 64px;
    height: 64px;
    max-width: 64px;
    max-height: 64px;
  }

  .user-avatar-letter-empty {
    width: 64px;
    height: 64px !important;
  }

  .user-avatar-letter-empty span {
    font-size: 1.6rem;
  }

  .user-avatar-actions-grid {
    flex-direction: column;
  }

  .user-avatar-current-card {
    flex-direction: column;
    gap: 14px;
    padding: 22px 16px 18px;
  }

  .user-avatar-current-card .preview-source-label {
    position: static;
  }

  .user-avatar-current-card .user-avatar-image {
    width: 80px;
    height: 80px;
    max-width: 80px;
    max-height: 80px;
  }

  .user-avatar-current-card .user-avatar-letter-empty {
    width: 80px;
    height: 80px !important;
  }

  .user-avatar-current-card .user-avatar-letter-empty span {
    font-size: 2rem;
  }

  .user-avatar-empty-state {
    width: 64px;
    height: 64px;
  }
}
.image-management {
  --bg: #f4f6fb;
  --bg-soft: #f8faff;
  --panel: rgba(255, 255, 255, 0.78);
  --panel-strong: rgba(255, 255, 255, 0.94);
  --line: rgba(128, 145, 184, 0.16);
  --line-strong: rgba(87, 116, 184, 0.26);
  --text: #25324d;
  --muted: #6f7b93;
  --dim: #97a2b7;
  --accent: #5378d6;
  --accent-soft: rgba(83, 120, 214, 0.1);
  --danger: #d46b6b;
  --danger-soft: rgba(212, 107, 107, 0.1);
  font-family: 'Avenir Next', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.panel-shell {
  background:
    radial-gradient(circle at top right, rgba(129, 164, 234, 0.14), transparent 26%),
    linear-gradient(180deg, var(--panel-strong), var(--panel));
  box-shadow: 0 18px 36px rgba(120, 136, 170, 0.12);
}

.btn-back,
.folder-cover-card,
.folder-cover-name,
.folder-cover-meta {
  color: var(--text);
}

.folder-cover-meta {
  color: var(--muted);
}

.folder-cover-card {
  background: rgba(255, 255, 255, 0.8);
}

.folder-cover-card:hover {
  box-shadow: 0 20px 48px rgba(120, 136, 170, 0.18);
}

.folder-cover-thumb,
.folder-cover-empty {
  background: linear-gradient(180deg, #f7f9fd, #eef2f8);
}

.btn-back {
  background: rgba(255, 255, 255, 0.8);
}

.section-kicker,
.image-action-hint,
.preview-kicker {
  color: var(--accent);
}

.list-subtitle,
.stat-label,
.stat-hint,
.image-meta,
.detail-label,
.results-chip,
.empty-hint {
  color: var(--muted);
}

.list-title,
.header-stat strong,
.image-name,
.preview-title,
.detail-value,
.preview-close,
.search-input,
.results-chip strong {
  color: var(--text);
}

.header-stat,
.results-chip,
.search-input,
.image-card,
.preview-close,
.url-value,
.btn-copy,
.btn-copy-md,
.btn-delete,
.modal-content,
.btn-cancel {
  border-color: var(--line);
}

.btn-upload {
  background: var(--accent);
  color: white;
  border-color: rgba(83, 120, 214, 0.16);
  box-shadow: 0 12px 24px rgba(95, 122, 183, 0.2);
}

.btn-upload:hover:not(:disabled) {
  background: #406bcf;
}

.search-input,
.results-chip,
.header-stat,
.image-card,
.panel-state,
.preview-close,
.preview-modal,
.modal-content,
.btn-cancel {
  background: rgba(255, 255, 255, 0.8);
}

.image-thumb,
.preview-image-wrapper {
  background: linear-gradient(180deg, #f7f9fd, #eef2f8);
}

.image-format,
.url-value,
.btn-copy {
  background: var(--accent-soft);
  color: var(--accent);
}

.btn-copy-md {
  background: rgba(247, 249, 253, 0.92);
  color: var(--text);
}

.btn-delete,
.btn-retry,
.btn-confirm-delete {
  background: var(--danger-soft);
  color: var(--danger);
}

.modal-overlay {
  background: rgba(226, 231, 240, 0.44);
}

.preview-modal,
.modal-content {
  color: var(--text);
  box-shadow: 0 24px 52px rgba(120, 136, 170, 0.18);
}

.preview-sidebar {
  border-left-color: var(--line);
}

.user-avatar-preview-card:first-child {
  border-color: rgba(83, 120, 214, 0.3);
  background: rgba(83, 120, 214, 0.06);
  box-shadow: 0 8px 28px rgba(83, 120, 214, 0.1);
}

.user-avatar-preview-card {
  background: rgba(255, 255, 255, 0.7);
}

.user-avatar-preview-card:first-child .preview-source-label {
  color: var(--accent);
}

.user-avatar-image {
  border-color: rgba(128, 145, 184, 0.2);
  box-shadow: 0 6px 18px rgba(95, 122, 183, 0.12);
}

.user-avatar-preview-card:first-child .user-avatar-image {
  border-color: rgba(83, 120, 214, 0.35);
  box-shadow: 0 8px 24px rgba(83, 120, 214, 0.14);
}

.user-avatar-letter-empty {
  background: linear-gradient(135deg, rgba(83, 120, 214, 0.18), rgba(83, 120, 214, 0.06)) !important;
  border-color: rgba(83, 120, 214, 0.25) !important;
}

.user-avatar-letter-empty span {
  color: var(--accent);
}

.btn-upload-avatar {
  background: rgba(83, 120, 214, 0.1) !important;
  border-color: rgba(83, 120, 214, 0.2) !important;
  color: var(--accent) !important;
}

.btn-upload-avatar:hover:not(:disabled) {
  background: rgba(83, 120, 214, 0.18) !important;
}

.btn-wechat-avatar {
  background: rgba(7, 193, 96, 0.06) !important;
  border-color: rgba(7, 193, 96, 0.15) !important;
  color: #07c160 !important;
}

.btn-wechat-avatar:hover:not(:disabled) {
  background: rgba(7, 193, 96, 0.12) !important;
}

.user-avatar-empty-state {
  border-color: var(--line);
}
</style>
