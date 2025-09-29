import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSidebarStore = defineStore('sidebar', () => {
  // 侧边栏是否显示
  const isVisible = ref(false)
  // 是否悬停在左边缘
  const isHovering = ref(false)
  
  // 显示侧边栏
  const showSidebar = () => {
    isVisible.value = true
  }
  
  // 隐藏侧边栏
  const hideSidebar = () => {
    isVisible.value = false
  }
  
  // 切换侧边栏显示状态
  const toggleSidebar = () => {
    isVisible.value = !isVisible.value
  }
  
  // 设置悬停状态
  const setHovering = (hovering: boolean) => {
    isHovering.value = hovering
  }
  
  return {
    isVisible,
    isHovering,
    showSidebar,
    hideSidebar,
    toggleSidebar,
    setHovering
  }
})