<template>
  <div class="paper-view">
    <div class="container">
      <h1 class="view-title">深度学习论文精选</h1>
      <p class="view-subtitle">前沿深度学习研究的探索与复现</p>
      <div class="paper-list">
        <div class="paper-card" v-for="paper in papers" :key="paper.title">
          <div class="card-header">
            <h2 class="paper-title">{{ paper.title }}</h2>
            <div class="paper-meta">
              <span class="meta-item" v-for="author in paper.authors" :key="author">{{ author }}</span>
            </div>
          </div>
          <div class="card-body">
            <section class="paper-section">
              <h3 class="section-title">论文介绍</h3>
              <p class="section-content">{{ paper.description }}</p>
            </section>
            <section class="paper-section">
              <h3 class="section-title">代码复现</h3>
              <div class="code-reproduction">
                <p>官方或社区提供的代码实现链接：</p>
                <a :href="paper.codeUrl" target="_blank" class="code-link">
                  <i class="bi bi-github"></i> {{ paper.codeUrl.replace('https://', '') }}
                </a>
              </div>
            </section>
          </div>
          <div class="card-footer">
            <a :href="paper.pdfUrl" target="_blank" class="action-btn pdf-btn">阅读原文 <i class="bi bi-file-earmark-pdf"></i></a>
            <span class="publish-date">发表于: {{ paper.conference }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const papers = ref([
  {
    title: 'Attention Is All You Need',
    authors: ['Ashish Vaswani', 'Noam Shazeer', 'Niki Parmar', 'et al.'],
    conference: 'NeurIPS 2017',
    description: '这篇论文提出了Transformer模型，完全基于自注意力机制，摒弃了循环和卷积结构。它已成为自然语言处理领域的主流架构。',
    codeUrl: 'https://github.com/tensorflow/tensor2tensor',
    pdfUrl: 'https://arxiv.org/abs/1706.03762'
  },
  {
    title: 'Deep Residual Learning for Image Recognition',
    authors: ['Kaiming He', 'Xiangyu Zhang', 'Shaoqing Ren', 'Jian Sun'],
    conference: 'CVPR 2016',
    description: '本文介绍了残差网络（ResNet），通过引入残差连接，成功地训练了非常深层的神经网络，极大地提升了图像识别的性能。',
    codeUrl: 'https://github.com/KaimingHe/deep-residual-networks',
    pdfUrl: 'https://arxiv.org/abs/1512.03385'
  },
]);
</script>

<style scoped>
.paper-view {
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
  color: #212529;
  margin-bottom: 10px;
}

.view-subtitle {
  text-align: center;
  font-size: 1.1rem;
  color: #6c757d;
  margin-bottom: 40px;
}

.paper-list {
  display: grid;
  gap: 30px;
}

.paper-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
}

.card-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.paper-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #343a40;
  margin: 0 0 10px 0;
}

.paper-meta {
  font-size: 0.9rem;
  color: #6c757d;
}

.meta-item:not(:last-child)::after {
  content: ', ';
  margin-right: 4px;
}

.card-body {
  padding: 20px;
  flex-grow: 1;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #495057;
  margin-bottom: 10px;
}

.section-content {
  font-size: 1rem;
  color: #495057;
  line-height: 1.7;
}

.code-reproduction .code-link {
  display: inline-block;
  background-color: #212529;
  color: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  text-decoration: none;
  transition: background-color 0.3s;
}

.code-reproduction .code-link:hover {
  background-color: #495057;
}

.card-footer {
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-btn {
  text-decoration: none;
  padding: 8px 15px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s;
}

.pdf-btn {
  background-color: #dc3545;
  color: #fff;
}

.pdf-btn:hover {
  background-color: #c82333;
}

.publish-date {
  font-size: 0.9rem;
  color: #6c757d;
}
</style>
