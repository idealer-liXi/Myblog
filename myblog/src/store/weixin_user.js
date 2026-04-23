import { clearSession, hasValidSession, readSession, saveSession } from '@/utils/authSession'

const createEmptyUser = () => ({
  id: null,
  username: '',
  displayName: '',
  avatar: '',
  loginType: '',
  roles: [],
  weixinBound: false,
  weixinName: '',
  openid: ''
})

const persistedSession = readSession()

const weixin_user = {
  namespaced: true,
  state: () => ({
    isLoggedIn: hasValidSession(),
    token: persistedSession.token || '',
    expiresAt: persistedSession.expiresAt || 0,
    initialized: false,
    userInfo: {
      ...createEmptyUser(),
      ...(persistedSession.user || {})
    }
  }),
  mutations: {
    SET_SESSION(state, session) {
      state.isLoggedIn = true
      state.token = session.token
      state.expiresAt = session.expiresAt
      state.userInfo = {
        ...createEmptyUser(),
        ...(session.user || {})
      }
      saveSession({
        token: state.token,
        expiresAt: state.expiresAt,
        user: state.userInfo
      })
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = {
        ...createEmptyUser(),
        ...userInfo
      }
      saveSession({
        token: state.token,
        expiresAt: state.expiresAt,
        user: state.userInfo
      })
    },
    SET_INITIALIZED(state, initialized) {
      state.initialized = initialized
    },
    LOGOUT(state) {
      state.isLoggedIn = false
      state.token = ''
      state.expiresAt = 0
      state.initialized = true
      state.userInfo = createEmptyUser()
      clearSession()
    },
  },
  actions: {
    applySession({ commit }, session) {
      commit('SET_SESSION', session)
      commit('SET_INITIALIZED', true)
    },
    updateProfile({ commit }, userInfo) {
      commit('SET_USER_INFO', userInfo)
    },
    finishBootstrap({ commit }) {
      commit('SET_INITIALIZED', true)
    },
    logout({ commit }) {
      commit('LOGOUT')
    },
  },
  getters: {
    getLoginStatus: (state) => state.isLoggedIn,
    getUserInfo: (state) => state.userInfo,
    getDisplayName: (state) => state.userInfo.displayName || state.userInfo.username || state.userInfo.weixinName || '访客',
    getAvatar: (state) => state.userInfo.avatar || '',
    getRoles: (state) => state.userInfo.roles || [],
  },
}

export default weixin_user
