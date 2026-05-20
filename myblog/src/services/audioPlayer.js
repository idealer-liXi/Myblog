class AudioPlayer {
  constructor() {
    this._audio = new Audio()
    this._store = null
    this._bound = false
    this._confirmCallback = null
    this._switching = false
  }

  onConfirm(callback) {
    this._confirmCallback = callback
  }

  bindStore(store) {
    if (this._bound) return
    this._store = store
    this._bound = true

    this._audio.addEventListener('timeupdate', () => {
      if (this._audio.currentTime) {
        this._store.dispatch('music/setCurrentTime', this._audio.currentTime)
      }
    })

    this._audio.addEventListener('loadedmetadata', () => {
      const d = Number.isFinite(this._audio.duration) ? this._audio.duration : 0
      this._store.dispatch('music/setDuration', d)
    })

    this._audio.addEventListener('play', () => {
      if (!this._switching) {
        this._store.dispatch('music/setPlaying', true)
      }
    })

    this._audio.addEventListener('pause', () => {
      if (!this._switching) {
        this._store.dispatch('music/setPlaying', false)
      }
    })

    this._audio.addEventListener('ended', () => {
      this._store.dispatch('music/setPlaying', false)
      const index = this._store.getters['music/currentTrackIndex']
      const list = this._store.getters['music/musicList']
      if (index < list.length - 1) {
        this.playTrack(index + 1, true)
      }
    })

    this._audio.addEventListener('error', () => {
      console.error('[AudioPlayer] playback error:', this._audio.error)
      this._store.dispatch('music/setPlaying', false)
    })

    this._audio.addEventListener('waiting', () => {
      this._audio.play().catch(() => {})
    })
  }

  async _checkConfirmed() {
    if (this._store.getters['music/hasConfirmedPlay']) return true
    if (this._confirmCallback) {
      return await this._confirmCallback()
    }
    return false
  }

  async loadTrack(index, autoPlay = false) {
    const list = this._store.getters['music/musicList']
    if (!list || index < 0 || index >= list.length) return

    this._switching = true
    this._store.dispatch('music/setPlaying', false)

    const track = list[index]
    this._audio.src = track.audioUrl || ''
    this._audio.load()
    this._store.dispatch('music/setTrackIndex', index)
    this._store.dispatch('music/setCurrentTime', 0)
    this._store.dispatch('music/setDuration', 0)

    this._switching = false

    if (autoPlay) {
      if (!(await this._checkConfirmed())) return
      try {
        await this._audio.play()
      } catch {
        this._store.dispatch('music/setPlaying', false)
      }
    }
  }

  async playTrack(index, autoPlay = true) {
    await this.loadTrack(index, autoPlay)
  }

  async togglePlay() {
    const hasMusic = this._store.getters['music/hasMusic']
    if (!hasMusic) return

    if (!this._audio.src && this._store.getters['music/musicList'].length > 0) {
      await this.loadTrack(this._store.getters['music/currentTrackIndex'], true)
      return
    }

    if (this._audio.paused) {
      if (!(await this._checkConfirmed())) return
      try {
        await this._audio.play()
      } catch {}
    } else {
      this._audio.pause()
    }
  }

  async previous() {
    const index = this._store.getters['music/currentTrackIndex']
    if (index > 0) {
      const isPlaying = this._store.getters['music/isPlaying']
      await this.loadTrack(index - 1, isPlaying)
    }
  }

  async next() {
    const index = this._store.getters['music/currentTrackIndex']
    const list = this._store.getters['music/musicList']
    if (index < list.length - 1) {
      const isPlaying = this._store.getters['music/isPlaying']
      await this.loadTrack(index + 1, isPlaying)
    }
  }

  seekTo(time) {
    if (this._audio && Number.isFinite(time)) {
      this._audio.currentTime = time
    }
  }

  destroy() {
    this._audio.pause()
    this._audio.src = ''
    this._bound = false
    this._store = null
  }
}

const audioPlayer = new AudioPlayer()

export default audioPlayer