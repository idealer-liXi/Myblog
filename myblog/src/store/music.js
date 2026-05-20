const music = {
  namespaced: true,
  state: () => ({
    isPlaying: false,
    currentTrackIndex: 0,
    musicList: [],
    currentTime: 0,
    duration: 0,
    hasConfirmedPlay: !!localStorage.getItem('music_confirmed_play'),
  }),
  mutations: {
    SET_PLAYING(state, isPlaying) {
      state.isPlaying = isPlaying
    },
    SET_CURRENT_TRACK_INDEX(state, index) {
      state.currentTrackIndex = index
    },
    SET_MUSIC_LIST(state, list) {
      state.musicList = list
    },
    SET_CURRENT_TIME(state, time) {
      state.currentTime = time
    },
    SET_DURATION(state, duration) {
      state.duration = duration
    },
    SET_CONFIRMED_PLAY(state, confirmed) {
      state.hasConfirmedPlay = confirmed
      if (confirmed) {
        localStorage.setItem('music_confirmed_play', '1')
      } else {
        localStorage.removeItem('music_confirmed_play')
      }
    },
  },
  actions: {
    setPlaying({ commit }, isPlaying) {
      commit('SET_PLAYING', isPlaying)
    },
    setTrackIndex({ commit }, index) {
      commit('SET_CURRENT_TRACK_INDEX', index)
    },
    setMusicList({ commit }, list) {
      commit('SET_MUSIC_LIST', list)
    },
    setCurrentTime({ commit }, time) {
      commit('SET_CURRENT_TIME', time)
    },
    setDuration({ commit }, duration) {
      commit('SET_DURATION', duration)
    },
    confirmPlay({ commit }) {
      commit('SET_CONFIRMED_PLAY', true)
    },
  },
  getters: {
    isPlaying: (state) => state.isPlaying,
    currentTrackIndex: (state) => state.currentTrackIndex,
    musicList: (state) => state.musicList,
    currentTime: (state) => state.currentTime,
    duration: (state) => state.duration,
    hasMusic: (state) => state.musicList.length > 0,
    currentMusic: (state) => state.musicList[state.currentTrackIndex] || null,
    hasConfirmedPlay: (state) => state.hasConfirmedPlay,
  },
}

export default music