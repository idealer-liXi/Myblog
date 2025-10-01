<template>
  <div class="me-container">
    <div class="profile-card">
      <!-- Profile content remains the same -->
      <div class="profile-header">
        <img class="avatar" src="@/assets/images/avatar.jpg" alt="个人头像" />
        <div class="profile-info">
          <h2 class="name">Idealer</h2>
          <p class="bio">Java开发工程师 | 大模型应用开发工程师 | 大模型算法工程师</p>
        </div>
      </div>
      <div class="profile-body">
        <section class="section">
          <h3>联系方式</h3>
          <ul class="contact-list">
            <li><i class="bi bi-envelope"></i> 邮箱：2755027635@qq.com</li>
            <li><i class="bi bi-github"></i> GitHub：<a href="https://github.com/zhangsan" target="_blank">**</a></li>
            <li><i class="bi bi-geo-alt"></i> 地点：**</li>
          </ul>
        </section>
        <section class="section">
          <h3>个人简介</h3>
          <p>
            具备扎实的前端、后端开发能力，熟悉Vue、SpringBoot等主流框架，热爱开源和技术分享。喜欢设计美观、体验流畅的Web应用，追求代码的优雅与高效。乐于团队协作，善于沟通，持续学习新技术。
          </p>
        </section>
        <section class="section">
          <h3>兴趣爱好</h3>
          <ul class="hobby-list">
            <li>编程与算法</li>
            <li>开源社区</li>
            <li>技术分享</li>
            <li>音乐与吉他</li>
            <li>美食探索</li>
          </ul>
        </section>
      </div>
    </div>
    <div class="schools-container">
      <SchoolCard>
        <template #image><img src="@/assets/images/school1.svg" alt="本科院校"></template>
        <template #name>安徽理工大学</template>
        <template #tags><span class="tag">省部共建</span> <span class="tag">中西部高校基础能力建设工程</span></template>
        <template #description>坐落于安徽省淮南市，是安徽省高等教育振兴计划‘地方特色高水平大学建设’项目立项建设高校。</template>
        <template #honors>
          <ul>
            <li>国家级大学生创新创业训练计划</li>
            <li>全国大学生数学建模竞赛一等奖</li>
          </ul>
        </template>
      </SchoolCard>
      <SchoolCard>
        <template #image><img src="@/assets/images/school2.svg" alt="研究生院校"></template>
        <template #name>东北大学</template>
        <template #tags><span class="tag">985</span> <span class="tag">211</span> <span class="tag">双一流</span></template>
        <template #description>坐落于辽宁省沈阳市，是教育部直属的全国重点大学，由教育部、国防科工局、辽宁省、沈阳市共建。</template>
        <template #honors>
          暂无~~
        </template>
      </SchoolCard>
    </div>
  </div>
</template>

<script setup>
import SchoolCard from '@/components/SchoolCard.vue';
</script>

<style scoped>
.me-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  gap: 20px;
  padding: 20px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  width: 480px;
  flex-shrink: 0;
  padding: 32px 28px;
  overflow-y: auto;
}

.schools-container {
  flex-grow: 1;
  overflow-y: auto;
  padding-right: 15px; /* space for scrollbar */
}

/* Profile Card Styles (some adjustments might be needed) */
.profile-header {
  display: flex;
  align-items: center;
  gap: 22px;
  margin-bottom: 10px;
}

.avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #74b9ff;
}

.name {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
}

.bio {
  font-size: 1.1rem;
  color: #888;
}

.section h3 {
  font-size: 1.1rem;
  color: #74b9ff;
  margin-bottom: 8px;
}

.contact-list, .hobby-list {
  list-style: none;
  padding: 0;
}

.contact-list li {
  margin-bottom: 4px;
}

.hobby-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hobby-list li {
  background: #74b9ff;
  color: #fff;
  border-radius: 16px;
  padding: 4px 16px;
}
</style>
