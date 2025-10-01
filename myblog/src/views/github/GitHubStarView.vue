<template>
  <div class="github-star-view">
    <div class="container">
      <h1 class="view-title">GitHub精选项目</h1>
      <p class="view-subtitle">一些有趣、值得学习的开源项目集合</p>
      <div class="project-list">
        <div class="project-card" v-for="project in projects" :key="project.name">
          <div class="card-header">
            <i class="bi bi-github"></i>
            <h2 class="project-name">{{ project.name }}</h2>
            <a :href="project.url" target="_blank" class="repo-link">
              访问仓库 <i class="bi bi-arrow-up-right-square"></i>
            </a>
          </div>
          <div class="card-body">
            <section class="project-section">
              <h3 class="section-title">项目介绍</h3>
              <p class="section-content">{{ project.description }}</p>
            </section>
            <section class="project-section">
              <h3 class="section-title">部署介绍</h3>
              <div class="deployment-steps" v-html="project.deployment"></div>
            </section>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const projects = ref([
  {
    name: 'Awesome Project 1',
    url: 'https://github.com/user/awesome-project-1',
    description: '这是一个非常棒的项目，它解决了...问题，使用了...技术，旨在提供一个...的解决方案。',
    deployment: `
      <ol>
        <li>克隆仓库: <code>git clone https://github.com/user/awesome-project-1.git</code></li>
        <li>安装依赖: <code>npm install</code></li>
        <li>启动开发服务器: <code>npm run serve</code></li>
        <li>构建生产版本: <code>npm run build</code></li>
      </ol>
    `
  },
  {
    name: 'Innovative Tool 2',
    url: 'https://github.com/user/innovative-tool-2',
    description: '一个具有创新性的工具，通过...方式简化了...流程，核心亮点在于其高效的算法和友好的用户界面。',
    deployment: `
      <p>该项目使用Docker进行部署，请确保您已安装Docker。</p>
      <ol>
        <li>拉取Docker镜像: <code>docker pull user/innovative-tool-2</code></li>
        <li>运行容器: <code>docker run -p 8080:80 user/innovative-tool-2</code></li>
      </ol>
    `
  },
]);
</script>

<style scoped>
.github-star-view {
  padding: 40px 0;
  min-height: 100vh;
}

.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 15px;
}

.view-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}

.view-subtitle {
  text-align: center;
  font-size: 1.1rem;
  color: #777;
  margin-bottom: 40px;
}

.project-list {
  display: grid;
  gap: 30px;
}

.project-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.project-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.card-header .bi-github {
  font-size: 1.5rem;
  color: #333;
}

.project-name {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex-grow: 1;
}

.repo-link {
  color: #007bff;
  text-decoration: none;
  font-weight: 500;
  font-size: 0.9rem;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.repo-link:hover {
  text-decoration: underline;
}

.card-body {
  padding: 20px;
}

.project-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #555;
  margin-bottom: 10px;
  border-left: 3px solid #007bff;
  padding-left: 10px;
}

.section-content {
  font-size: 1rem;
  color: #666;
  line-height: 1.7;
}

.deployment-steps ::v-deep(code) {
  background-color: #eef;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.9em;
}

.deployment-steps ::v-deep(ol) {
  padding-left: 20px;
}
</style>
