import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UserProfileView from '@/views/user/UserProfileView.vue'

const {
  storeState,
  updateCurrentUserProfile,
  fetchCurrentUser,
  fetchWechatBindTicket,
  checkWechatBind,
  unbindWechat,
  uploadCurrentUserAvatar
} = vi.hoisted(() => ({
  storeState: {
    userInfo: {},
    dispatch: vi.fn()
  },
  updateCurrentUserProfile: vi.fn(),
  fetchCurrentUser: vi.fn(),
  fetchWechatBindTicket: vi.fn(),
  checkWechatBind: vi.fn(),
  unbindWechat: vi.fn(),
  uploadCurrentUserAvatar: vi.fn()
}))

vi.mock('vuex', () => ({
  useStore: () => ({
    getters: {
      'weixin_user/getUserInfo': storeState.userInfo
    },
    dispatch: storeState.dispatch
  })
}))

vi.mock('@/services/authService', () => ({
  checkWechatBind,
  fetchCurrentUser,
  fetchWechatBindTicket,
  unbindWechat,
  updateCurrentUserProfile,
  uploadCurrentUserAvatar
}))

vi.mock('@/utils/authSession', () => ({
  hasValidSession: vi.fn(() => true)
}))

describe('UserProfileView backend entry', () => {
  const mountView = () => mount(UserProfileView, {
    global: {
      stubs: {
        RouterLink: {
          props: ['to'],
          template: '<a><slot /></a>'
        }
      }
    }
  })

  it('shows backend entry for admin users', () => {
    storeState.userInfo = {
      username: 'admin',
      displayName: '管理员',
      loginType: 'password',
      roles: ['ADMIN']
    }

    const wrapper = mountView()

    expect(wrapper.text()).toContain('进入后台')
  })

  it('hides backend entry for non-admin users', () => {
    storeState.userInfo = {
      username: 'wx_user',
      displayName: '微信用户',
      loginType: 'weixin',
      roles: ['USER']
    }

    const wrapper = mountView()

    expect(wrapper.text()).not.toContain('进入后台')
  })

  it('keeps nickname-related fields and removes the requested session fields', () => {
    storeState.userInfo = {
      username: 'wx_user',
      displayName: '微信用户',
      loginType: 'weixin',
      roles: ['USER'],
      weixinBound: true,
      weixinName: '微信昵称',
      avatarSource: 'WECHAT'
    }

    const wrapper = mountView()

    expect(wrapper.text()).toContain('显示名称')
    expect(wrapper.text()).toContain('用户名')
    expect(wrapper.text()).toContain('微信昵称')
    expect(wrapper.text()).toContain('登录方式')
    expect(wrapper.text()).not.toContain('角色')
    expect(wrapper.text()).not.toContain('JWT 状态')
    expect(wrapper.text()).not.toContain('当前头像来源')
    expect(wrapper.text()).not.toContain('头像地址')

    expect(wrapper.get('[data-testid="profile-wechat-bind-cell"]').classes()).not.toContain('span-2')
    expect(wrapper.get('[data-testid="profile-wechat-action-cell"]').classes()).not.toContain('span-2')
  })

  it('updates display name and avatar settings from the edit panel', async () => {
    storeState.userInfo = {
      id: 7,
      username: 'wx_user',
      displayName: '微信用户',
      loginType: 'weixin',
      roles: ['USER'],
      weixinBound: true,
      avatar: '',
      avatarSource: 'DEFAULT',
      weixinImageUrl: 'https://wx.example.com/avatar.png'
    }
    updateCurrentUserProfile.mockResolvedValue({
      ...storeState.userInfo,
      displayName: '新昵称',
      avatarSource: 'WECHAT',
      effectiveAvatar: 'https://wx.example.com/avatar.png'
    })

    const wrapper = mountView()

    await wrapper.get('[data-testid="profile-edit-toggle"]').trigger('click')
    await wrapper.get('[data-testid="profile-display-name-input"]').setValue('新昵称')
    await wrapper.get('[data-testid="profile-avatar-source-select"]').setValue('WECHAT')
    await wrapper.get('[data-testid="profile-save-button"]').trigger('click')

    expect(updateCurrentUserProfile).toHaveBeenCalledWith({
      displayName: '新昵称',
      avatarSource: 'WECHAT',
      avatar: ''
    })
    expect(storeState.dispatch).toHaveBeenCalledWith('weixin_user/updateProfile', expect.objectContaining({
      displayName: '新昵称',
      avatarSource: 'WECHAT'
    }))
  })

  it('uploads an avatar file before saving the profile', async () => {
    storeState.userInfo = {
      id: 7,
      username: 'wx_user',
      displayName: '微信用户',
      loginType: 'weixin',
      roles: ['USER'],
      weixinBound: true,
      avatar: '',
      avatarSource: 'DEFAULT',
      weixinImageUrl: 'https://wx.example.com/avatar.png'
    }

    uploadCurrentUserAvatar.mockResolvedValue('https://cdn.example.com/user-avatars/7/avatar.png')
    updateCurrentUserProfile.mockResolvedValue({
      ...storeState.userInfo,
      displayName: '新昵称',
      avatarSource: 'UPLOAD',
      avatar: 'https://cdn.example.com/user-avatars/7/avatar.png',
      effectiveAvatar: 'https://cdn.example.com/user-avatars/7/avatar.png'
    })

    const wrapper = mountView()
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' })

    await wrapper.get('[data-testid="profile-edit-toggle"]').trigger('click')
    await wrapper.get('[data-testid="profile-display-name-input"]').setValue('新昵称')
    await wrapper.get('[data-testid="profile-avatar-source-select"]').setValue('UPLOAD')
    const fileInput = wrapper.get('[data-testid="profile-avatar-file-input"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: [file],
      configurable: true
    })
    await fileInput.trigger('change')

    expect(uploadCurrentUserAvatar).toHaveBeenCalledWith(file)
    expect(wrapper.get('[data-testid="profile-avatar-upload-preview"]').attributes('src'))
      .toBe('https://cdn.example.com/user-avatars/7/avatar.png')

    await wrapper.get('[data-testid="profile-save-button"]').trigger('click')

    expect(updateCurrentUserProfile).toHaveBeenCalledWith({
      displayName: '新昵称',
      avatarSource: 'UPLOAD',
      avatar: 'https://cdn.example.com/user-avatars/7/avatar.png'
    })
  })
})
