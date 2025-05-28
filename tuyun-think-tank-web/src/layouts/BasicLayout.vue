<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header"><web-header/></a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>

      <a-layout-footer>
        <a-layout-footer class="footer">
          © 2025 图云智库
          关注我：<a href="https://github.com/rich0807" target="_blank">
          芮七
          </a>
        </a-layout-footer>

      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import WebHeader from '@/components/webHeader.vue'
</script>



<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  margin-bottom: 16px;
  color: unset;
  background: white;
}
#basicLayout .content {
  /*渐变色*/
  background: linear-gradient(to right, #fefefe, #fff);
  /*让主体的底部高一点，防止主体底部内容被遮挡*/
  margin-bottom: 28px;
  padding: 20px;
}
#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>


