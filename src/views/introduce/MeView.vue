<template>
  <div class="me-container">
    <div class="profile-card">
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
          <h3>教育经历</h3>
          <ul class="edu-list">
            <li>
              <span class="edu-school">东北大学</span>
              <span class="edu-major">人工智能</span>
              <span class="edu-time">2025 - 2028</span>
            </li>
            <li>
              <span class="edu-school">安徽理工大学</span>
              <span class="edu-major">计算机科学与技术</span>
              <span class="edu-time">2021 - 2025</span>
            </li>
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
  </div>
</template>

<script>
export default {

}
</script>

<style scoped>
.me-container {
  height: 100vh;

  display: flex;
  align-items: stretch;
  justify-content: flex-start;
  padding: 0;
  margin: 0;
  overflow-y: auto;
}



.profile-card {
  background: #fff;
  border-radius: 0px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  max-width: 480px;
  width: 100%;
  height: 100vh;
  align-self: flex-start;
  padding: 32px 28px 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin: 0;
  overflow-y: auto;
  cursor: default;
}

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
  box-shadow: 0 2px 8px rgba(116,185,255,0.15);
}

.profile-info {
  flex: 1;
}

.name {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.bio {
  font-size: 1.1rem;
  color: #888;
  margin: 0;
}

.profile-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.section h3 {
  font-size: 1.1rem;
  color: #74b9ff;
  margin-bottom: 8px;
  font-weight: 600;
}

.edu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.edu-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 6px;
  font-size: 1rem;
}

.edu-school {
  font-weight: 600;
  color: #333;
}

.edu-major {
  color: #888;
}

.edu-time {
  color: #aaa;
  font-size: 0.95em;
}

.contact-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contact-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  color: #555;
  margin-bottom: 4px;
}

.contact-list a {
  color: #74b9ff;
  text-decoration: none;
}

.contact-list a:hover {
  text-decoration: underline;
}

.hobby-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
}

.hobby-list li {
  background: #74b9ff;
  color: #fff;
  border-radius: 16px;
  padding: 4px 16px;
  font-size: 0.98rem;
  box-shadow: 0 2px 8px rgba(116,185,255,0.08);
  margin-bottom: 4px;
  white-space: nowrap;
}
</style>