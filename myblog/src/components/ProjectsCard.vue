<template>
  <div v-for="p in projects.detail" :key="p.id" class="card projects-card">
    <div class="image-container">
      <img :src="p.image" class="card-img" alt="项目图片">
      <div class="image-overlay">
        <button type="button" class="btn btn-light btn-sm view-btn">查看详情</button>
      </div>
    </div>
    <div class="card-body">
      <h5 class="project-name">{{p.name}}</h5>
      <div class="project-tags">
        <span class="tag">Vue.js</span>
        <span class="tag">Bootstrap</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  setup(){
    let projects = {
      count:1,
      detail:[
        {
          id: 1,
          image:'https://www.2uyx.com/gameimages/20230322203859641af6e33da87.jpeg',
          name: 'Web游戏平台',
          url: '',
        },
        {
          id: 2,
          image:'https://img2.baidu.com/it/u=3442884034,846253587&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500',
          name: '商城系统',
          url: '',
        }
      ]
    };

    return {
      projects,
    }
  }


}

</script>

<style scoped>
.projects-card {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
  border: none;
}

.projects-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.15);
}

.image-container {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.projects-card:hover .card-img {
  transform: scale(1.1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.projects-card:hover .image-overlay {
  opacity: 1;
}

.view-btn {
  padding: 6px 14px;
  font-weight: 500;
  transform: translateY(20px);
  transition: transform 0.3s ease;
}

.projects-card:hover .view-btn {
  transform: translateY(0);
}

.card-body {
  padding: 15px;
  background: white;
}

.project-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  background: #e9f5ff;
  color: #74b9ff;
  font-size: 0.8rem;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 500;
}
</style>