<template>
  <div class="github-view">
    <div class="github-container">

      <!-- Section 1: GitHub Profile -->
      <section class="profile-section">
        <div class="glass-card profile-card">
          <div class="profile-left">
            <img :src="profile.avatar_url || DEFAULT_AVATAR" :alt="profile.name" class="profile-avatar" @error="$event.target.src = DEFAULT_AVATAR" />
            <div class="profile-info">
              <h1 class="profile-name">{{ profile.name }}</h1>
              <p class="profile-bio">{{ profile.bio }}</p>
              <div class="profile-meta">
                <span class="meta-item" v-if="profile.location"><i class="bi bi-geo-alt"></i> {{ profile.location }}</span>
                <span class="meta-item" v-if="profile.blog"><i class="bi bi-link-45deg"></i> <a :href="profile.blog.startsWith('http') ? profile.blog : 'https://' + profile.blog" target="_blank">{{ profile.blogLabel || profile.blog }}</a></span>
                <span class="meta-item" v-if="profile.company"><i class="bi bi-building"></i> {{ profile.company }}</span>
              </div>
            </div>
          </div>
          <div class="profile-stats">
            <a :href="profile.html_url + '?tab=repositories'" target="_blank" class="stat-item">
              <span class="stat-value">{{ profile.public_repos }}</span>
              <span class="stat-label">Repos</span>
            </a>
            <a :href="profile.html_url + '?tab=stars'" target="_blank" class="stat-item">
              <span class="stat-value">{{ profile.totalStars }}</span>
              <span class="stat-label">Stars</span>
            </a>
            <a :href="profile.html_url + '?tab=followers'" target="_blank" class="stat-item">
              <span class="stat-value">{{ profile.followers }}</span>
              <span class="stat-label">Followers</span>
            </a>
            <a :href="profile.html_url + '?tab=following'" target="_blank" class="stat-item">
              <span class="stat-value">{{ profile.following }}</span>
              <span class="stat-label">Following</span>
            </a>
          </div>
        </div>

        <!-- Contribution Graph -->
        <div class="glass-card contribution-card" v-if="contributions.length">
          <div class="contribution-header">
            <h3 class="section-heading"><i class="bi bi-graph-up"></i> Contribution</h3>
            <div class="year-tabs">
              <button
                v-for="y in yearList"
                :key="y"
                class="year-tab"
                :class="{ active: selectedYear === y }"
                @click="selectYear(y)"
                :disabled="contributionLoading"
              >{{ y }}</button>
            </div>
          </div>
          <div class="contribution-scroll" v-if="!contributionLoading">
            <div class="contribution-wrapper">
              <div class="month-labels">
                <span
                  v-for="(m, i) in monthLabels"
                  :key="i"
                  class="month-label"
                  :style="{ marginLeft: m.offset + 'px' }"
                >{{ m.name }}</span>
              </div>
              <div class="contribution-grid">
                <div
                  v-for="(week, wIdx) in contributions"
                  :key="wIdx"
                  class="contribution-week"
                >
                  <div
                    v-for="(day, dIdx) in week"
                    :key="dIdx"
                    class="contribution-day"
                    :class="'level-' + day.level"
                    :title="day.date + ': ' + day.count + ' contributions'"
                  ></div>
                </div>
              </div>
            </div>
          </div>
          <div class="contribution-loading" v-else>
            <div class="loading-spinner"></div>
            <span>加载中...</span>
          </div>
          <div class="contribution-summary" v-if="!contributionLoading">
            <span class="total-count">{{ totalContributions }} contributions in {{ selectedYear }}</span>
            <div class="contribution-legend">
              <span class="legend-label">Less</span>
              <div class="legend-block level-0"></div>
              <div class="legend-block level-1"></div>
              <div class="legend-block level-2"></div>
              <div class="legend-block level-3"></div>
              <div class="legend-block level-4"></div>
              <span class="legend-label">More</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Section 2 & 3: Side by side -->
      <div class="repos-row">
        <section class="repos-col">
          <h2 class="view-heading"><i class="bi bi-star"></i> Starred Projects</h2>
          <div class="glass-card repo-list-card">
            <a
              v-for="repo in starredRepos"
              :key="repo.id"
              :href="repo.html_url"
              target="_blank"
              class="repo-item"
            >
              <div class="repo-item-top">
                <span class="repo-item-name"><i class="bi bi-star-fill star-icon"></i> {{ repo.name }}</span>
                <span class="repo-lang" :style="{ backgroundColor: langColors[repo.language] || '#999' }">{{ repo.language }}</span>
              </div>
              <p class="repo-desc">{{ repo.description }}</p>
              <div class="repo-footer">
                <span><i class="bi bi-star"></i> {{ formatStars(repo.stargazers_count) }}</span>
                <span><i class="bi bi-diagram-2"></i> {{ repo.forks_count }}</span>
              </div>
            </a>
            <div v-if="!starredRepos.length" class="empty-tip">暂无 Star 项目</div>
          </div>
        </section>

        <section class="repos-col">
          <h2 class="view-heading"><i class="bi bi-journal-bookmark-fill"></i> My Repositories</h2>
          <div class="glass-card repo-list-card">
            <a
              v-for="repo in myRepos"
              :key="repo.id"
              :href="repo.html_url"
              target="_blank"
              class="repo-item"
            >
              <div class="repo-item-top">
                <span class="repo-item-name"><i class="bi bi-git"></i> {{ repo.name }}</span>
                <span class="repo-lang" :style="{ backgroundColor: langColors[repo.language] || '#999' }">{{ repo.language }}</span>
              </div>
              <p class="repo-desc">{{ repo.description }}</p>
              <div class="repo-footer">
                <span><i class="bi bi-star"></i> {{ repo.stargazers_count }}</span>
                <span><i class="bi bi-diagram-2"></i> {{ repo.forks_count }}</span>
              </div>
            </a>
            <div v-if="!myRepos.length" class="empty-tip">暂无仓库</div>
          </div>
        </section>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const GITHUB_USERNAME = 'idealer-liXi'

const DEFAULT_AVATAR = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="88" height="88"><path fill="#ccc" d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/></svg>')

const langColors = {
  JavaScript: '#f1e05a',
  TypeScript: '#3178c6',
  Python: '#3572A5',
  Java: '#b07219',
  Vue: '#41b883',
  Rust: '#dea584',
  Go: '#00ADD8',
  C: '#555555',
  'C++': '#f34b7d',
  'C#': '#178600',
  Ruby: '#701516',
  PHP: '#4F5D95',
  Swift: '#F05138',
  Kotlin: '#A97BFF',
  Dart: '#00B4AB',
  Shell: '#89e051',
  HTML: '#e34c26',
  CSS: '#563d7c',
  SCSS: '#c6538c',
  Dockerfile: '#384d54',
  Jupyter: '#DA5B0B',
  Lua: '#000080',
  Scala: '#c22d40',
  R: '#198CE7',
}

const profile = ref({
  avatar_url: DEFAULT_AVATAR,
  name: GITHUB_USERNAME,
  bio: '',
  location: '',
  blog: '',
  blogLabel: '',
  company: '',
  public_repos: 0,
  followers: 0,
  following: 0,
  totalStars: 0,
  html_url: `https://github.com/${GITHUB_USERNAME}`,
})

const contributions = ref([])
const starredRepos = ref([])
const myRepos = ref([])
const selectedYear = ref(new Date().getFullYear())
const contributionLoading = ref(false)
const contributionCache = {}

const yearList = (() => {
  const now = new Date().getFullYear()
  return [now, now - 1, now - 2, now - 3, now - 4]
})()

const totalContributions = computed(() => {
  let total = 0
  contributions.value.forEach(week => {
    week.forEach(day => {
      total += day.count
    })
  })
  return total
})

const monthLabels = computed(() => {
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
  const labels = []
  const cellSize = 13
  const gap = 3
  const seen = new Set()

  contributions.value.forEach((week, wIdx) => {
    const firstDay = week.find(d => d && d.date)
    if (!firstDay) return
    const month = new Date(firstDay.date + 'T00:00:00').getMonth()
    if (!seen.has(month) && wIdx > 0) {
      seen.add(month)
      labels.push({
        name: monthNames[month],
        offset: wIdx * (cellSize + gap)
      })
    }
  })

  return labels
})

const formatStars = (count) => {
  if (!count) return '0'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count.toString()
}

const parseContributionHtml = (html) => {
  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  const tds = doc.querySelectorAll('td.ContributionCalendar-day')
  const dayMap = {}

  tds.forEach(td => {
    const date = td.getAttribute('data-date')
    const level = parseInt(td.getAttribute('data-level') || '0', 10)
    if (date) {
      dayMap[date] = { date, level, count: 0 }
    }
  })

  const tooltips = doc.querySelectorAll('tool-tip')
  tooltips.forEach(tip => {
    const text = tip.textContent.trim()
    const match = text.match(/(\d+)\s+contribut/i)
    if (match) {
      const count = parseInt(match[1], 10)
      const forId = tip.getAttribute('for')
      if (forId) {
        const td = doc.getElementById(forId)
        if (td) {
          const date = td.getAttribute('data-date')
          if (date && dayMap[date]) {
            dayMap[date].count = count
          }
        }
      }
    }
  })

  return dayMap
}

const buildWeeks = (dayMap) => {
  const dates = Object.keys(dayMap).sort()
  if (!dates.length) return []

  const firstDate = new Date(dates[0])
  const lastDate = new Date(dates[dates.length - 1])

  const start = new Date(firstDate)
  start.setDate(start.getDate() - start.getDay())

  const end = new Date(lastDate)

  const weeks = []
  let current = new Date(start)

  while (current <= end) {
    const week = []
    for (let d = 0; d < 7; d++) {
      const dateStr = current.toISOString().split('T')[0]
      if (dayMap[dateStr]) {
        week.push(dayMap[dateStr])
      } else {
        week.push({ date: dateStr, count: 0, level: 0 })
      }
      current.setDate(current.getDate() + 1)
    }
    weeks.push(week)
  }

  return weeks
}

const generateMockContributions = () => {
  const weeks = []
  const now = new Date()
  for (let w = 0; w < 52; w++) {
    const week = []
    for (let d = 0; d < 7; d++) {
      const date = new Date(now)
      date.setDate(date.getDate() - (52 - w) * 7 - (6 - d))
      const dateStr = date.toISOString().split('T')[0]
      const rand = Math.random()
      let level = 0
      if (rand > 0.7) level = 1
      if (rand > 0.85) level = 2
      if (rand > 0.93) level = 3
      if (rand > 0.97) level = 4
      week.push({ date: dateStr, count: level * 3, level })
    }
    weeks.push(week)
  }
  return weeks
}

const CORS_PROXIES = [
  (url) => `https://api.codetabs.com/v1/proxy?quest=${encodeURIComponent(url)}`,
  (url) => `https://api.allorigins.win/raw?url=${encodeURIComponent(url)}`,
]

const fetchWithProxy = async (url) => {
  for (const proxy of CORS_PROXIES) {
    try {
      const res = await axios.get(proxy(url), { timeout: 10000 })
      if (res.data && res.status === 200) return res.data
    } catch (err) {
      continue
    }
  }
  throw new Error('All proxies failed')
}

const selectYear = (year) => {
  if (year === selectedYear.value && contributions.value.length) return
  selectedYear.value = year
  loadContributionYear(year)
}

const loadContributionYear = async (year) => {
  if (contributionCache[year]) {
    contributions.value = contributionCache[year]
    return
  }

  contributionLoading.value = true
  const url = `https://github.com/users/${GITHUB_USERNAME}/contributions?from=${year}-01-01&to=${year}-12-31`
  try {
    const html = await fetchWithProxy(url)
    const dayMap = parseContributionHtml(html)
    if (Object.keys(dayMap).length > 0) {
      const weeks = buildWeeks(dayMap)
      contributionCache[year] = weeks
      contributions.value = weeks
    } else {
      const mock = generateMockContributions()
      contributionCache[year] = mock
      contributions.value = mock
    }
  } catch (err) {
    console.warn(`Failed to fetch contributions for ${year}:`, err.message)
    const mock = generateMockContributions()
    contributionCache[year] = mock
    contributions.value = mock
  }
  contributionLoading.value = false
}

const fetchContributions = () => {
  return loadContributionYear(selectedYear.value)
}

const fetchGitHubData = async () => {
  try {
    const [profileRes, starredRes, reposRes] = await Promise.all([
      axios.get(`https://api.github.com/users/${GITHUB_USERNAME}`),
      axios.get(`https://api.github.com/users/${GITHUB_USERNAME}/starred?per_page=30&sort=stars&direction=desc`),
      axios.get(`https://api.github.com/users/${GITHUB_USERNAME}/repos?per_page=30&sort=updated&direction=desc`),
    ])

    const pData = profileRes.data
    profile.value = {
      avatar_url: pData.avatar_url,
      name: pData.name || pData.login,
      bio: pData.bio || '',
      location: pData.location || '',
      blog: pData.blog || '',
      blogLabel: pData.blog ? pData.blog.replace(/^https?:\/\//, '') : '',
      company: pData.company || '',
      public_repos: pData.public_repos || 0,
      followers: pData.followers || 0,
      following: pData.following || 0,
      totalStars: reposRes.data.reduce((sum, r) => sum + r.stargazers_count, 0),
      html_url: pData.html_url,
    }

    starredRepos.value = starredRes.data.map(r => ({
      id: r.id,
      name: r.full_name || r.name,
      description: r.description || '',
      html_url: r.html_url,
      language: r.language || '—',
      stargazers_count: r.stargazers_count,
      forks_count: r.forks_count,
    }))

    myRepos.value = reposRes.data.map(r => ({
      id: r.id,
      name: r.name,
      description: r.description || '',
      html_url: r.html_url,
      language: r.language || '—',
      stargazers_count: r.stargazers_count,
      forks_count: r.forks_count,
    }))

  } catch (err) {
    console.error('Failed to fetch GitHub data:', err)
    profile.value.avatar_url = `https://avatars.githubusercontent.com/${GITHUB_USERNAME}`
  }

  fetchContributions()
}

onMounted(() => {
  fetchGitHubData()
})
</script>

<style scoped>
.github-view {
  padding: 20px 0;
  min-height: 100vh;
}

.github-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

/* Profile */
.profile-section {
  margin-bottom: 30px;
}

.profile-card {
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.profile-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.profile-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  border: 3px solid #007bff;
  box-shadow: 0 4px 16px rgba(0, 123, 255, 0.2);
  object-fit: cover;
  background: #f0f0f0;
}

.profile-name {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 4px;
}

.profile-bio {
  color: #666;
  font-size: 0.95rem;
  margin: 0 0 10px;
}

.profile-meta {
  display: flex;
  gap: 18px;
  font-size: 0.85rem;
  color: #888;
  flex-wrap: wrap;
}

.profile-meta .meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.profile-meta a {
  color: #007bff;
  text-decoration: none;
}

.profile-meta a:hover {
  text-decoration: underline;
}

.profile-stats {
  display: flex;
  gap: 28px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-item:hover .stat-value {
  color: #0056b3;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: #007bff;
}

.stat-label {
  font-size: 0.78rem;
  color: #999;
  margin-top: 2px;
}

/* Contribution */
.contribution-card {
  padding: 24px 28px;
}

.contribution-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.section-heading i {
  color: #007bff;
}

.year-tabs {
  display: flex;
  gap: 6px;
}

.year-tab {
  padding: 4px 14px;
  border-radius: 20px;
  border: 1.5px solid #ddd;
  background: transparent;
  color: #666;
  font-size: 0.82rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.year-tab:hover:not(:disabled) {
  border-color: #007bff;
  color: #007bff;
}

.year-tab.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.year-tab:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.contribution-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 30px 0;
  color: #999;
  font-size: 0.9rem;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-top-color: #007bff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.contribution-scroll {
  display: flex;
  justify-content: center;
  overflow-x: auto;
}

.contribution-wrapper {
  display: inline-flex;
  flex-direction: column;
}

.month-labels {
  display: flex;
  position: relative;
  height: 18px;
  margin-bottom: 4px;
}

.month-label {
  position: absolute;
  font-size: 0.72rem;
  color: #999;
  line-height: 18px;
  white-space: nowrap;
}

.contribution-grid {
  display: inline-flex;
  gap: 3px;
}

.contribution-week {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.contribution-day {
  width: 13px;
  height: 13px;
  border-radius: 3px;
  transition: transform 0.15s ease;
}

.contribution-day:hover {
  transform: scale(1.5);
}

.level-0 { background: #ebedf0; }
.level-1 { background: #9be9a8; }
.level-2 { background: #40c463; }
.level-3 { background: #30a14e; }
.level-4 { background: #216e39; }

.contribution-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 0.85rem;
  color: #666;
}

.total-count {
  font-weight: 500;
}

.contribution-legend {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-block {
  width: 13px;
  height: 13px;
  border-radius: 3px;
}

.legend-label {
  margin: 0 4px;
  font-size: 0.72rem;
  color: #999;
}

/* View Heading */
.view-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.2rem;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 16px;
}

.view-heading i {
  color: #007bff;
}

/* Side by Side */
.repos-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.repos-col {
  min-width: 0;
}

.repo-list-card {
  padding: 12px;
  max-height: 600px;
  overflow-y: auto;
}

.repo-list-card::-webkit-scrollbar {
  width: 4px;
}

.repo-list-card::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.12);
  border-radius: 4px;
}

.repo-item {
  display: block;
  padding: 16px;
  border-radius: 12px;
  text-decoration: none;
  transition: background 0.2s ease;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.repo-item:last-child {
  border-bottom: none;
}

.repo-item:hover {
  background: rgba(0, 123, 255, 0.05);
}

.repo-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.repo-item-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #007bff;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.star-icon {
  color: #ffc107;
  font-size: 0.85rem;
}

.repo-lang {
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 20px;
  color: white;
  font-weight: 500;
  white-space: nowrap;
}

.repo-desc {
  font-size: 0.82rem;
  color: #777;
  line-height: 1.5;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.repo-footer {
  display: flex;
  gap: 14px;
  font-size: 0.78rem;
  color: #aaa;
}

.repo-footer i {
  margin-right: 3px;
}

.empty-tip {
  text-align: center;
  color: #bbb;
  padding: 40px 0;
  font-size: 0.9rem;
}

/* Responsive */
@media (max-width: 900px) {
  .repos-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .profile-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    padding: 22px 20px;
  }

  .profile-stats {
    gap: 20px;
  }

  .contribution-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .contribution-grid {
    gap: 2px;
  }

  .contribution-day {
    width: 10px;
    height: 10px;
  }

  .contribution-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .profile-left {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .profile-info {
    text-align: center;
  }

  .profile-meta {
    justify-content: center;
  }

  .profile-name {
    font-size: 1.4rem;
  }

  .profile-stats {
    width: 100%;
    justify-content: space-around;
  }
}
</style>