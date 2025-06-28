<template>
  <div v-for="p in projects.detail" :key="p.id" class="card projects_card" style="margin-bottom: 10px;">
    <img :src="p.image" class="card-img-top rounded-0" alt="..." style="width: 100%; height: 140px; object-fit: cover;">
    <div class="card-body px-0">
      <div class="container-fluid">
        <div class="row align-items-center justify-content-center">
          <div class="col-7 d-flex justify-content-center">
            <span class="card-text">{{p.name}}</span>
          </div>
          <div class="col-5 d-flex justify-content-center">
            <button type="button" class="btn btn-primary btn-sm">查看</button>
          </div>
        </div>
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


</style>