<template>
  <div class="container-fluid">
  <div class="row">
    <div class="left-projects col-2 ms-4">
          <div v-for="p in projects.detail" :key="p.id" class="card" style="margin-bottom: 10px">
            <img :src="p.image" class="card-img-top" alt="...">
            <div class="card-body">
              <div class="row">
                <div class="col-9">
                  <span class="card-text">{{p.name}}</span>
                </div>
                <div class="col-3"> > </div>

              </div>


            </div>
          </div>


    </div>
    <div class="main-blog col-7">
      <div class="card">
        <div class="card-body">

        </div>
      </div>
    </div>
    <div class="right-message col-2">
      <div class="card">
        <div class="card-body">
          <div class="player">
            <audio ref="audio" :src="test" @timeupdate="updateProgress"></audio>
            <div class="container-fluid">

            </div>
            <button @click="togglePlay">{{ isPlaying ? '暂停' : '播放' }}</button>
            <input type="range" v-model="progress" @input="seek">
          </div>
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
          image:'D:\\MyBlog\\myblog\\src\\assets\\images\\snakes.jpg',
          name: '项目1',
          url: '',
        },
        {
          id: 2,
          image:'',
          name: '项目2',
          url: '',
        }
      ]
    }

    return{
      projects,
    }
  }
}
</script>

<style scoped>

.row{
  margin-top: 15px;
}

</style>