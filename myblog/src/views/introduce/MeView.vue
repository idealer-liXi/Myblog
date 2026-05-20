<template>
  <div class="me-container">
    <div class="profile-card">
      <div class="profile-header">
        <img class="avatar" :src="profile.avatar" alt="个人头像" />
        <div class="profile-info">
          <h2 class="name">{{ profile.name }}</h2>
          <p class="bio">{{ profile.bio }}</p>
        </div>
      </div>
      <div class="profile-body">
        <section class="section">
          <h3>联系方式</h3>
          <ul class="contact-list">
            <li><i class="bi bi-envelope"></i> 邮箱：{{ profile.email }}</li>
            <li><i class="bi bi-github"></i> GitHub：<a :href="profile.githubUrl" target="_blank">{{ profile.githubName }}</a></li>
            <li><i class="bi bi-geo-alt"></i> 地点：{{ profile.location }}</li>
          </ul>
        </section>
        <section class="section">
          <h3>个人简介</h3>
          <p>{{ profile.introduction }}</p>
        </section>
        <section class="section">
          <h3>兴趣爱好</h3>
          <ul class="hobby-list">
            <li v-for="hobby in profile.hobbies" :key="hobby">{{ hobby }}</li>
          </ul>
        </section>
      </div>
    </div>
    <div class="schools-container">
      <SchoolCard v-for="school in profile.schools" :key="school.schoolKey">
        <template #image><img :src="school.image" :alt="school.name"></template>
        <template #name>{{ school.name }}</template>
        <template #tags><span class="tag" v-for="tag in school.tags" :key="tag">{{ tag }}</span></template>
        <template #description>{{ school.description }}</template>
        <template #honors>
          <ul>
            <li v-for="honor in school.honors" :key="honor">{{ honor }}</li>
          </ul>
        </template>
      </SchoolCard>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import SchoolCard from '@/components/SchoolCard.vue'
import { DEFAULT_PROFILE, getPublicProfile } from '@/services/profileService.js'

const profile = ref(DEFAULT_PROFILE)

const loadProfile = async () => {
  try {
    profile.value = await getPublicProfile()
  } catch {
    profile.value = DEFAULT_PROFILE
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.me-container {
  display: flex;
  height: calc(100vh - 76px);
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
  padding-right: 15px;
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
  border: 4px solid #007bff;
  display: block;
  flex-shrink: 0;
  aspect-ratio: 1 / 1;
  background: rgba(0, 123, 255, 0.08);
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
  color: #007bff;
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
  background: #007bff;
  color: #fff;
  border-radius: 16px;
  padding: 4px 16px;
}

@media (max-width: 900px) {
  .me-container {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - 76px);
    overflow-y: auto;
  }

  .profile-card {
    width: 100%;
    overflow-y: visible;
  }

  .schools-container {
    overflow-y: visible;
    padding-right: 0;
  }
}
</style>
