const weixin_user = {
  namespaced: true,
  state: () => ({
    isLoggedIn: false,
    userInfo: {
      displayName: '',
      username: '',
      weixinName: '',
      weixinImageUrl: '',
      loginType: '',
      openid: ''
    }
  }),
  mutations: {
    SET_LOGIN_STATUS(state, status) {
      state.isLoggedIn = status
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = {
        displayName: '',
        username: '',
        weixinName: '',
        weixinImageUrl: '',
        loginType: '',
        openid: '',
        ...userInfo
      }
    },
    LOGOUT(state) {
      state.isLoggedIn = false
      state.userInfo = {
        displayName: '',
        username: '',
        weixinName: '',
        weixinImageUrl: '',
        loginType: '',
        openid: ''
      }
    },
  },
  actions: {
    login({ commit }, userInfo) {
      commit('SET_LOGIN_STATUS', true)
      commit('SET_USER_INFO', userInfo)
    },
    logout({ commit }) {
      commit('LOGOUT')
    },
  },
  getters: {
    getLoginStatus: (state) => state.isLoggedIn,
    getUserInfo: (state) => state.userInfo,
    getDisplayName: (state) => state.userInfo.displayName || state.userInfo.weixinName || state.userInfo.username || '访客',
    getAvatar: (state) => state.userInfo.weixinImageUrl || '',
  },
}

export default weixin_user
