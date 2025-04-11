<template>
  <nav class="navbar navbar-expand-lg bg-body-tertiary bg-dark" data-bs-theme="dark">
    <div class="container-fluid">
      <a class="navbar-brand ms-3" href="#">IdealBlog</a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNavDropdown">
        <ul class="navbar-nav">
          <li class="nav-item">
            <a class="nav-link" aria-current="page" href="#">首页</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#">个人介绍</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#">后台管理</a>
          </li>

        </ul>

        <ul class="navbar-nav ms-auto me-3">
          <li class="nav-item ">
            <a class="nav-link" href="#">登录</a>
          </li>
          <li class="nav-item ">
            <a class="nav-link" href="#">注册</a>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script>

export default {
  setup(){

  }
}
</script>

<style scoped>

</style>