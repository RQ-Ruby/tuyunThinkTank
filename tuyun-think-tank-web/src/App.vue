<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { healthUsingGet } from '@/api/mainController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

healthUsingGet().then((data) => {
  console.log(data)
})
</script>


<template>

  <div id="app">
    <BasicLayout/>
  </div>
</template>


<style scoped>

</style>
