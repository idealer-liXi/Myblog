<template>
  <div class="container">
  <div class="row main">
    <div class="left-projects col-2 mx-auto">
      <div v-for="p in projects.detail" :key="p.id" class="card" style="margin-bottom: 10px;">
        <img :src="p.image" class="card-img-top" alt="..." style="width: 100%; height: 140px; object-fit: cover;">
        <div class="card-body px-0">
          <div class="container-fluid">
            <div class="row">
              <div class="col-8">
                <span class="card-text">{{p.name}}</span>
              </div>
              <div class="col-4"> <button type="button" class="btn btn-primary btn-sm">查看</button></div>
            </div>
          </div>
        </div>
      </div>

    </div>
    <div class="main-blog col-8">
      <div class="card">
        <div class="card-body">

        </div>
      </div>
    </div>
    <div class="right-message col-2">
      <div class="card">
        <div class="card-body px-0">
            <div class="music-play">
              <div class="mx-auto mb-3" style="text-align: center">{{current_music.name}}</div>

              <div class="progress mb-3 mx-auto" role="progressbar" aria-label="Default striped example" aria-valuenow="10" aria-valuemin="0" aria-valuemax="100">
                <div class="progress-bar progress-bar-striped" style="width: 10%"></div>
              </div>


              <div class="container-fluid">
                <div class="row">
                  <div class="col-4">
                    <button class="btn btn-outline-primary music-btn p-0"><i class="bi bi-chevron-double-left"></i></button>
                  </div>
                  <div class="col-4" >
                    <button class="btn btn-outline-primary music-btn p-0"><i v-if="isOpen" class="bi bi-stop-circle"></i><i v-else class="bi bi-play-circle"></i></button>
                  </div>
                  <div class="col-4">
                    <button class="btn btn-outline-primary music-btn p-0"><i class="bi bi-chevron-double-right"></i></button>
                  </div>
                </div>

              </div>


            </div>
        </div>
      </div>
    </div>
  </div>
  </div>
</template>

<script>

import {ref} from "vue";

export default {
  setup(){
    let projects = {
      count:1,
      detail:[
        {
          id: 1,
          image:'https://www.2uyx.com/gameimages/20230322203859641af6e33da87.jpeg',
          name: 'web游戏平台',
          url: '',
        },
        {
          id: 2,
          image:'https://img2.baidu.com/it/u=3442884034,846253587&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500',
          name: '外卖系统平台',
          url: '',
        }
      ]
    };

    let music = [
      {
        id: 1,
        image: '',
        name: '红色高跟鞋',
        path: '',
      },
      {
        id: 2,
        image: '',
        name: '',
        path: '',
      }
    ];

    let current_music = music[0];
    let isOpen = ref(false);


    return{
      projects,
      current_music,
      isOpen,
    }
  }
}
</script>

<style scoped>

.left-projects{
  padding: 0;
  height: 600px;
  overflow-y: auto;
  font-family: "Source Han Sans SC", "Noto Sans SC", "Microsoft YaHei", sans-serif;
}

.music-btn{
  width: 100%;
  height: 100%;
}

.progress{
  height: 5px;
  width: 175px;
}

</style>