const weixin_user = {
  namespaced: true,
  state: () => ({
    // User login state and related data
    isLoggedIn: false,
    userInfo: {
      weixinName: '',
      weixinImageUrl: ''
    }
  }),
  mutations: {
    SET_LOGIN_STATUS(state, status) {
      state.isLoggedIn = status;
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo;
    },
    LOGOUT(state) {
      state.isLoggedIn = false;
      state.userInfo = null;
    },
  },
  actions: {
    // Example action for logging in
    login({ commit }, userInfo) {
      // Perform login logic (e.g., API call)
      // On success:
      commit('SET_LOGIN_STATUS', true);
      commit('SET_USER_INFO', userInfo);
    },
    // Example action for logging out
    logout({ commit }) {
      // Perform logout logic (e.g., API call, clear tokens)
      commit('LOGOUT');
    },
  },
  getters: {
    getLoginStatus: (state) => state.isLoggedIn,
    getUserInfo: (state) => state.userInfo,
  },
};

export default weixin_user;
