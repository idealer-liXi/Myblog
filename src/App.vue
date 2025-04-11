<template>
  <Navbar></Navbar>
  <router-view/>
  <Footer></Footer>
</template>


<script>
import Navbar from "@/components/Navbar.vue";
import Footer from "@/components/Footer.vue";

export default {
  name:'',
  components:{
    Navbar,
    Footer,
  },
  setup(){

  }
}

</script>


<style>


body{
  background-image: url("@/assets/images/background.jpg");
  background-size: cover;
  background-repeat: no-repeat;
}


</style>
