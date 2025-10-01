<template>
  <div class="school-card" :class="{ 'expanded': isExpanded }">
    <div class="card-content">
      <div class="school-image">
        <slot name="image"></slot>
      </div>
      <div class="school-details">
        <h5 class="school-name"><slot name="name"></slot></h5>
        <div class="school-tags">
          <slot name="tags"></slot>
        </div>
        <p class="school-description"><slot name="description"></slot></p>
        <button @click="toggleHonors" class="details-btn">
          {{ isExpanded ? '收起荣誉' : '查看在校荣誉' }} <i :class="['bi', isExpanded ? 'bi-chevron-up' : 'bi-chevron-down']"></i>
        </button>
      </div>
    </div>
    <div v-if="isExpanded" class="honors-section">
      <slot name="honors"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const isExpanded = ref(false);

const toggleHonors = () => {
  isExpanded.value = !isExpanded.value;
};
</script>

<style scoped>
.school-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.4s ease-in-out;
  overflow: hidden;
}

.card-content {
  display: flex;
  gap: 20px;
}

.school-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.school-image ::v-deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.school-details {
  flex-grow: 1;
}

.school-name {
  font-weight: 600;
  color: #333;
  margin-top: 0;
  margin-bottom: 8px;
}

.school-tags {
  margin-bottom: 12px;
}

.school-tags ::v-deep(.tag) {
  display: inline-block;
  background-color: #eef7ff;
  color: #007bff;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 0.75rem;
  margin-right: 6px;
  font-weight: 500;
}

.school-description {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.6;
}

.details-btn {
  background: none;
  border: 1px solid #007bff;
  color: #007bff;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.85rem;
}

.details-btn:hover {
  background-color: #007bff;
  color: #fff;
}

.honors-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
