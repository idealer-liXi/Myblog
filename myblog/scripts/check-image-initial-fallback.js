const fs = require('fs')
const path = require('path')

function read(relativePath) {
  return fs.readFileSync(path.resolve(__dirname, '..', relativePath), 'utf8')
}

function assertMatch(source, pattern, message) {
  if (!pattern.test(source)) {
    console.error(message)
    process.exit(1)
  }
}

const componentPath = 'src/components/common/ImageInitialFallback.vue'
const articlePerformPath = 'src/components/ArticlePerform.vue'
const projectsCardPath = 'src/components/ProjectsCard.vue'

if (!fs.existsSync(path.resolve(__dirname, '..', componentPath))) {
  console.error('Missing shared component: src/components/common/ImageInitialFallback.vue')
  process.exit(1)
}

const componentSource = read(componentPath)
assertMatch(componentSource, /defineProps\(/, 'ImageInitialFallback.vue must define props.')
assertMatch(componentSource, /@error="handleError"/, 'ImageInitialFallback.vue must handle image load errors.')
assertMatch(componentSource, /const\s+initial\s*=\s*computed\(/, 'ImageInitialFallback.vue must compute a fallback initial.')

const articlePerformSource = read(articlePerformPath)
assertMatch(articlePerformSource, /import\s+ImageInitialFallback\s+from\s+'@\/components\/common\/ImageInitialFallback\.vue'/, 'ArticlePerform.vue must import ImageInitialFallback.')
assertMatch(articlePerformSource, /<ImageInitialFallback[\s\S]*:src="article\.image"[\s\S]*:name="article\.title"/, 'ArticlePerform.vue must render ImageInitialFallback for article covers.')

const projectsCardSource = read(projectsCardPath)
assertMatch(projectsCardSource, /import\s+ImageInitialFallback\s+from\s+'@\/components\/common\/ImageInitialFallback\.vue'/, 'ProjectsCard.vue must import ImageInitialFallback.')
assertMatch(projectsCardSource, /<ImageInitialFallback[\s\S]*:src="project\.image"[\s\S]*:name="project\.name"/, 'ProjectsCard.vue must render ImageInitialFallback for project covers.')

if (projectsCardSource.includes('picsum.photos/seed/project-fallback/500/320')) {
  console.error('ProjectsCard.vue must not keep the random picsum fallback image.')
  process.exit(1)
}

const articleListSource = read('src/components/backend/ArticleList.vue')
assertMatch(articleListSource, /import\s+ImageInitialFallback\s+from\s+'@\/components\/common\/ImageInitialFallback\.vue'/, 'ArticleList.vue must import ImageInitialFallback.')
assertMatch(articleListSource, /<ImageInitialFallback[\s\S]*:src="article\.image"[\s\S]*:name="article\.title"/, 'ArticleList.vue must render ImageInitialFallback for article thumbnails.')

const dashboardPanelSource = read('src/components/backend/DashboardPanel.vue')
assertMatch(dashboardPanelSource, /import\s+ImageInitialFallback\s+from\s+'@\/components\/common\/ImageInitialFallback\.vue'/, 'DashboardPanel.vue must import ImageInitialFallback.')
assertMatch(dashboardPanelSource, /<ImageInitialFallback[\s\S]*:src="article\.image"[\s\S]*:name="article\.title"/, 'DashboardPanel.vue must render ImageInitialFallback for recent article thumbnails.')

const projectManagementSource = read('src/components/backend/ProjectManagement.vue')
assertMatch(projectManagementSource, /import\s+ImageInitialFallback\s+from\s+'@\/components\/common\/ImageInitialFallback\.vue'/, 'ProjectManagement.vue must import ImageInitialFallback.')
assertMatch(projectManagementSource, /<ImageInitialFallback[\s\S]*:src="proj\.coverImage"[\s\S]*:name="proj\.name"/, 'ProjectManagement.vue must render ImageInitialFallback for project table covers.')

const imageManagementSource = read('src/components/backend/ImageManagement.vue')
assertMatch(imageManagementSource, /import\s+ImageInitialFallback\s+from\s+'@\/components\/common\/ImageInitialFallback\.vue'/, 'ImageManagement.vue must import ImageInitialFallback.')
assertMatch(imageManagementSource, /<ImageInitialFallback[\s\S]*:src="project\.coverImage"[\s\S]*:name="project\.projectName"/, 'ImageManagement.vue must render ImageInitialFallback for project cover cards.')
assertMatch(imageManagementSource, /<ImageInitialFallback[\s\S]*:src="projectDetail\?\.coverImage\s*\|\|\s*''"[\s\S]*:name="projectDetail\?\.projectName\s*\|\|\s*''"/, 'ImageManagement.vue must render ImageInitialFallback for project detail media.')

console.log('Image initial fallback checks passed.')
