import { beforeEach, describe, expect, it, vi } from 'vitest'
import axios from 'axios'
import {
  bindWechatExistingAccount,
  checkWechatLogin,
  createWechatAccount,
  fetchCurrentUser,
  uploadCurrentUserAvatar
} from '@/services/authService'

const { readSession } = vi.hoisted(() => ({
  readSession: vi.fn(() => ({
    token: 'stored-token',
    expiresAt: Date.now() + 60_000,
    user: {}
  }))
}))

vi.mock('axios', () => ({
  default: vi.fn()
}))

vi.mock('@/utils/authSession', () => ({
  readSession
}))

describe('authService WeChat login flow', () => {
  beforeEach(() => {
    axios.mockReset()
    readSession.mockReset()
    readSession.mockReturnValue({
      token: 'stored-token',
      expiresAt: Date.now() + 60_000,
      user: {}
    })
  })

  it('returns null while WeChat login is still pending scan', async () => {
    axios.mockResolvedValueOnce({
      data: {
        code: '1001'
      }
    })

    await expect(checkWechatLogin('wx-ticket')).resolves.toBeNull()
  })

  it('returns a typed pending decision result for unbound WeChat users', async () => {
    axios.mockResolvedValueOnce({
      data: {
        code: '0000',
        data: {
          status: 'PENDING_DECISION',
          pendingToken: 'wx-ticket',
          authType: 'wechat',
          authKey: 'openid_xxx',
          displayName: '微信昵称',
          avatar: 'https://wx.qlogo.cn/mock.png',
          weixinName: '微信昵称'
        }
      }
    })

    await expect(checkWechatLogin('wx-ticket')).resolves.toEqual({
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
  })

  it('returns a typed session result for already bound WeChat users', async () => {
    axios.mockResolvedValueOnce({
      data: {
        code: '0000',
        data: {
          token: 'wechat-token',
          expiresAt: 1776883200000,
          user: {
            username: 'admin',
            displayName: '管理员',
            loginType: 'wechat',
            roles: ['ADMIN'],
            weixinBound: true,
            weixinName: '微信昵称'
          }
        }
      }
    })

    await expect(checkWechatLogin('wx-ticket')).resolves.toEqual({
      type: 'session',
      session: expect.objectContaining({
        token: 'wechat-token',
        user: expect.objectContaining({
          username: 'admin',
          loginType: 'weixin',
          weixinBound: true
        })
      })
    })
  })

  it('creates a new site account from a pending token', async () => {
    axios.mockResolvedValueOnce({
      data: {
        code: '0000',
        data: {
          token: 'new-wechat-token',
          expiresAt: 1776883200000,
          user: {
            username: 'wx_openid_xxx',
            displayName: '微信昵称',
            loginType: 'wechat',
            roles: ['USER'],
            weixinBound: true,
            weixinName: '微信昵称'
          }
        }
      }
    })

    const session = await createWechatAccount('wx-ticket')

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      method: 'post',
      url: 'http://localhost:8080/api/v1/login/weixin/create-account',
      data: { pendingToken: 'wx-ticket' }
    }))
    expect(session.token).toBe('new-wechat-token')
    expect(session.user.username).toBe('wx_openid_xxx')
    expect(session.user.loginType).toBe('weixin')
  })

  it('binds an existing account from a pending token', async () => {
    axios.mockResolvedValueOnce({
      data: {
        code: '0000',
        data: {
          token: 'bound-wechat-token',
          expiresAt: 1776883200000,
          user: {
            username: 'admin',
            displayName: '管理员',
            loginType: 'wechat',
            roles: ['ADMIN'],
            weixinBound: true,
            weixinName: '微信昵称'
          }
        }
      }
    })

    const session = await bindWechatExistingAccount({
      pendingToken: 'wx-ticket',
      username: 'admin',
      password: 'password'
    })

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      method: 'post',
      url: 'http://localhost:8080/api/v1/login/weixin/bind-existing',
      data: {
        pendingToken: 'wx-ticket',
        username: 'admin',
        password: 'password'
      }
    }))
    expect(session.token).toBe('bound-wechat-token')
    expect(session.user.username).toBe('admin')
    expect(session.user.loginType).toBe('weixin')
  })

  it('uploads current user avatar through the user profile endpoint', async () => {
    axios.mockResolvedValueOnce({
      data: {
        code: '0000',
        data: {
          url: 'https://cdn.example.com/user-avatars/2/avatar.png'
        }
      }
    })

    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' })
    const url = await uploadCurrentUserAvatar(file)

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      method: 'post',
      url: 'http://localhost:8080/api/user/me/avatar/upload',
      headers: expect.objectContaining({
        Authorization: 'Bearer stored-token'
      })
    }))
    expect(url).toBe('https://cdn.example.com/user-avatars/2/avatar.png')
  })

  it('preserves the stored displayName when /me omits it', async () => {
    readSession.mockReturnValue({
      token: 'stored-token',
      expiresAt: Date.now() + 60_000,
      user: {
        username: 'admin',
        displayName: '理想',
        roles: ['ADMIN']
      }
    })

    axios.mockResolvedValueOnce({
      data: {
        code: '0000',
        data: {
          username: 'admin',
          roles: ['ADMIN']
        }
      }
    })

    const user = await fetchCurrentUser()

    expect(user.displayName).toBe('理想')
    expect(user.username).toBe('admin')
  })
})
