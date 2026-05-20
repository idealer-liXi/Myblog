import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import LoginView from '@/views/user/LoginView.vue'

const {
  pushMock,
  dispatchMock,
  fetchWechatLoginTicket,
  checkWechatLogin,
  createWechatAccount,
  bindWechatExistingAccount
} = vi.hoisted(() => ({
  pushMock: vi.fn(),
  dispatchMock: vi.fn(),
  fetchWechatLoginTicket: vi.fn(),
  checkWechatLogin: vi.fn(),
  createWechatAccount: vi.fn(),
  bindWechatExistingAccount: vi.fn()
}))

vi.mock('@/router', () => ({
  default: {
    push: pushMock,
    currentRoute: {
      value: {
        query: {}
      }
    }
  }
}))

vi.mock('vuex', () => ({
  useStore: () => ({
    dispatch: dispatchMock
  })
}))

vi.mock('@/services/authService', () => ({
  fetchWechatLoginTicket,
  checkWechatLogin,
  createWechatAccount,
  bindWechatExistingAccount,
  loginWithPassword: vi.fn()
}))

async function flushUi() {
  await Promise.resolve()
  await nextTick()
}

describe('LoginView WeChat decision flow', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    pushMock.mockReset()
    dispatchMock.mockReset()
    fetchWechatLoginTicket.mockReset()
    checkWechatLogin.mockReset()
    createWechatAccount.mockReset()
    bindWechatExistingAccount.mockReset()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('switches to forced decision mode when the backend returns a pending decision', async () => {
    fetchWechatLoginTicket.mockResolvedValue('wx-ticket')
    checkWechatLogin.mockResolvedValue({
      type: 'pending',
      pending: {
        status: 'PENDING_DECISION',
        pendingToken: 'wx-ticket',
        authType: 'wechat',
        authKey: 'openid_xxx',
        displayName: '微信昵称',
        avatar: 'https://wx.qlogo.cn/mock.png',
        weixinName: '微信昵称'
      }
    })

    const wrapper = mount(LoginView)

    await wrapper.get('[data-testid="wechat-start-login"]').trigger('click')
    await flushUi()
    await vi.advanceTimersByTimeAsync(3000)
    await flushUi()

    expect(wrapper.text()).toContain('创建新账号')
    expect(wrapper.text()).toContain('绑定已有账号')
    expect(dispatchMock).not.toHaveBeenCalledWith('weixin_user/applySession', expect.anything())
    expect(pushMock).not.toHaveBeenCalled()
  })

  it('creates a new account from the decision panel and then finishes login', async () => {
    fetchWechatLoginTicket.mockResolvedValue('wx-ticket')
    checkWechatLogin.mockResolvedValue({
      type: 'pending',
      pending: {
        status: 'PENDING_DECISION',
        pendingToken: 'wx-ticket',
        authType: 'wechat',
        authKey: 'openid_xxx',
        displayName: '微信昵称',
        avatar: 'https://wx.qlogo.cn/mock.png',
        weixinName: '微信昵称'
      }
    })
    createWechatAccount.mockResolvedValue({
      token: 'wechat-token',
      expiresAt: 1776883200000,
      user: {
        username: 'wx_openid_xxx',
        displayName: '微信昵称',
        loginType: 'weixin',
        roles: ['USER'],
        weixinBound: true,
        weixinName: '微信昵称'
      }
    })

    const wrapper = mount(LoginView)

    await wrapper.get('[data-testid="wechat-start-login"]').trigger('click')
    await flushUi()
    await vi.advanceTimersByTimeAsync(3000)
    await flushUi()

    await wrapper.get('[data-testid="wechat-create-account"]').trigger('click')
    await flushUi()

    expect(createWechatAccount).toHaveBeenCalledWith('wx-ticket')
    expect(dispatchMock).toHaveBeenCalledWith('weixin_user/applySession', expect.objectContaining({
      token: 'wechat-token'
    }))
    expect(pushMock).toHaveBeenCalledWith({ name: 'home' })
  })

  it('treats leaving the decision panel as a failed login and clears temporary state', async () => {
    fetchWechatLoginTicket.mockResolvedValue('wx-ticket')
    checkWechatLogin.mockResolvedValue({
      type: 'pending',
      pending: {
        status: 'PENDING_DECISION',
        pendingToken: 'wx-ticket',
        authType: 'wechat',
        authKey: 'openid_xxx',
        displayName: '微信昵称',
        avatar: 'https://wx.qlogo.cn/mock.png',
        weixinName: '微信昵称'
      }
    })

    const wrapper = mount(LoginView)

    await wrapper.get('[data-testid="wechat-start-login"]').trigger('click')
    await flushUi()
    await vi.advanceTimersByTimeAsync(3000)
    await flushUi()

    await wrapper.get('[data-testid="wechat-cancel-login"]').trigger('click')
    await flushUi()

    expect(wrapper.text()).toContain('使用微信登录')
    expect(wrapper.text()).not.toContain('创建新账号')
    expect(dispatchMock).not.toHaveBeenCalledWith('weixin_user/applySession', expect.anything())
    expect(pushMock).not.toHaveBeenCalled()
  })
})
