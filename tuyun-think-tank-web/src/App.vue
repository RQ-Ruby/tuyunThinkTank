<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
/*
import { healthUsingGet } from '@/api/mainController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
//已改为全局引入，所以不需要引入了
const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

healthUsingGet().then((data) => {
  console.log(data)
})
*/


</script>


<template>

  <div id="app">
    <BasicLayout/>
  </div>
</template>


<style scoped>

</style>

<style>
/* 让 Ant Design Vue 的图片预览层在所有布局之上 */
.ant-image-preview-wrap,
.ant-image-preview-mask {
  z-index: 2000 !important;
}
</style>
