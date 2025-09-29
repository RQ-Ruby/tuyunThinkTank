<template>
  <div v-if="hideLayout">
    <router-view />
  </div>
  <div id="basicLayout" v-if="!hideLayout">
    <!-- 导航栏在最上方 -->
    <a-layout-header class="header">
      <web-header/>
    </a-layout-header>

    <!-- 下方是侧边栏和内容的容器 -->
    <div class="main-container">
      <!-- 左边缘触发区域 -->
      <div 
        v-if="hasLoggedInUser && !sidebarStore.isVisible"
        class="sidebar-trigger-area"
        @mouseenter="handleTriggerHover"
        @mouseleave="handleTriggerLeave"
      ></div>
      
      <!-- 侧边栏组件 -->
      <web-sider v-if="hasLoggedInUser" />
      
      <!-- 右侧内容区域 -->
      <div class="content-wrapper" :class="{ 'with-sidebar': hasLoggedInUser && sidebarStore.isVisible }">
        <!-- 内容区域 -->
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>

        <!-- 底部 -->
        <a-layout-footer class="footer">
          © 2025 图云智库
          关注我：<a href="https://github.com/rich0807" target="_blank">芮七</a>
        </a-layout-footer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import WebHeader from '@/components/webHeader.vue'
import WebSider from '@/components/webSider.vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useSidebarStore } from '@/stores/useSidebarStore.ts'

const route = useRoute()
const loginUserStore = useLoginUserStore()
const sidebarStore = useSidebarStore()

const hideLayout = computed(() => route.meta.hideLayout)
const hasLoggedInUser = computed(() => !!loginUserStore.loginUser.id)

// 处理左边缘触发区域的鼠标悬停
const handleTriggerHover = () => {
  sidebarStore.setHovering(true)
  sidebarStore.showSidebar()
}

const handleTriggerLeave = () => {
  sidebarStore.setHovering(false)
  // 延迟隐藏，给用户时间移动到侧边栏
  setTimeout(() => {
    if (!sidebarStore.isHovering) {
      sidebarStore.hideSidebar()
    }
  }, 300)
}
</script>

<style scoped>
#basicLayout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 导航栏样式 */
#basicLayout .header {
  padding-inline: 20px;
  color: unset;
  background: white;
  position: relative;
  z-index: 1001; /* 确保导航栏在最上层 */
  flex-shrink: 0; /* 防止导航栏被压缩 */
}

/* 主容器：侧边栏和内容的容器 */
.main-container {
  display: flex;
  flex: 1;
  position: relative;
  overflow: hidden;
}

.sidebar-trigger-area {
  position: fixed;
  left: 0;
  top: 64px;
  width: 10px;
  height: calc(100vh - 64px);
  z-index: 999;
  background: transparent;
  cursor: pointer;
}

/* 内容包装器 */
.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
  margin-left: 0; /* 默认无边距 */
  transition: margin-left 0.3s ease;
}

/* 有侧边栏时的内容包装器 */
.content-wrapper.with-sidebar {
  margin-left: 200px; /* 为固定定位的侧边栏留出空间 */
}

/* 内容区域样式 */
#basicLayout .content {
  background: linear-gradient(to right, #fefefe, #fff);
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

/* 底部样式 */
#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  text-align: center;
  flex-shrink: 0; /* 防止底部被压缩 */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-container {
    flex-direction: column;
  }
  
  /* 在移动端移除内容区域的左边距 */
  .content-wrapper.with-sidebar {
    margin-left: 0;
  }
  
  .sidebar-trigger-area {
    width: 15px; /* 移动端增加触发区域宽度 */
  }
  
  /* 在移动端隐藏侧边栏 */
  .main-container > web-sider {
    display: none;
  }
}
</style>


