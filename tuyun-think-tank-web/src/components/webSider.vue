<template>
  <div
    id="globalSider"
    v-if="loginUserStore.loginUser.id"
    :class="{ 'sidebar-hidden': !sidebarStore.isVisible }"
    @mouseenter="handleSidebarHover"
    @mouseleave="handleSidebarLeave"
  >
    <div class="modern-sidebar">
      <!-- 侧边栏头部 -->
      <div class="sidebar-header">
        <div class="user-avatar">
          <a-avatar :size="40" :src="loginUserStore.loginUser.userAvatar">
            <template #icon><UserOutlined /></template>
          </a-avatar>
        </div>
        <div class="user-info">
          <div class="username">{{ loginUserStore.loginUser.userName || '用户' }}</div>
          <div class="user-role">{{ loginUserStore.loginUser.userRole || '普通用户' }}</div>
        </div>
      </div>

      <!-- 导航菜单 -->
      <div class="sidebar-menu">
        <div
          v-for="item in menuItems"
          :key="item.key"
          class="menu-item"
          :class="{ active: current.includes(item.key), 'team-space-item': item.isTeamSpace }"
          @click="doMenuClick({ key: item.key })"
        >
          <div class="menu-icon">
            <component :is="item.icon" />
          </div>
          <div class="menu-label">{{ item.label }}</div>
          <div class="menu-indicator"></div>
        </div>
      </div>

      <!-- 侧边栏底部 -->
      <div class="sidebar-footer">
        <div class="footer-item" @click="handleSettings">
          <SettingOutlined />
          <span>设置</span>
        </div>
        <div class="footer-item" @click="handleLogout">
          <LogoutOutlined />
          <span>退出</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref, watchEffect } from 'vue'
import {
  PictureOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  HomeOutlined,
  FolderOutlined,
  HeartOutlined,
  StarOutlined, TeamOutlined
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useSidebarStore } from '@/stores/useSidebarStore.ts'
import { message } from 'ant-design-vue'
import { SPACE_TYPE_ENUM } from '@/constants/space'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController'

const loginUserStore = useLoginUserStore()
const sidebarStore = useSidebarStore()
const router = useRouter()

// 处理侧边栏鼠标悬停
const handleSidebarHover = () => {
  sidebarStore.setHovering(true)
}

const handleSidebarLeave = () => {
  sidebarStore.setHovering(false)
  // 延迟隐藏，避免意外关闭
  setTimeout(() => {
    if (!sidebarStore.isHovering) {
      sidebarStore.hideSidebar()
    }
  }, 300)
}

// 菜单项
const fixedMenuItems = [
  {
    key: '/',
    icon: HomeOutlined,
    label: '首页',
  },
  {
    key: '/gallery',
    icon: PictureOutlined,
    label: '公共图库',
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: UserOutlined,
  },

  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined),
  },

{
    key: '/favorites',
    label: '我的收藏',
    icon: HeartOutlined,
  },
  {
    key: '/my_uploads',
    label: '我的上传',
    icon: FolderOutlined,
  },
]

// 当前要高亮的菜单项
const current = ref<string[]>([])

// 监听路由变化，更新高亮菜单项
router.afterEach((to) => {
  current.value = [to.path]
})

// 初始化当前路由
current.value = [router.currentRoute.value.path]

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}
const teamSpaceList = ref<API.SpaceUserVO[]>([])
const menuItems = computed(() => {
  // 展示团队空间分组（过滤掉空间已删除或无名的残留项）
  const teamSpaceSubMenus = teamSpaceList.value
    .filter((spaceUser) => spaceUser.space && spaceUser.space.id && spaceUser.space.spaceName)
    .map((spaceUser) => {
      const space = spaceUser.space
      return {
        key: '/space/' + spaceUser.spaceId,
        label: space?.spaceName,
        icon: TeamOutlined,
        isTeamSpace: true,
      }
    })

  // 扁平化处理，将团队空间插入到创建团队后面
  return fixedMenuItems.reduce((acc, item) => {
    acc.push(item)
    if (item.key.startsWith('/add_space')) {
      acc.push(...teamSpaceSubMenus)
    }
    return acc
  }, [] as any[])
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})


// 设置功能
const handleSettings = () => {
  message.info('设置功能开发中...')
}

// 退出登录
const handleLogout = () => {
  loginUserStore.setLoginUser({
    userName: "未登录",
    id: undefined,
    userAvatar: undefined,
    userRole: undefined,
  })
  message.success('已退出登录')
  router.push('/user/login')
}
</script>

<style scoped>
#globalSider {
  position: fixed;
  left: 0;
  top: 64px; /* 从导航栏下方开始 */
  width: 200px;
  height: calc(100vh - 64px); /* 减去导航栏高度 */
  z-index: 999;
  transition: transform 0.3s ease;
  transform: translateX(0);
}

/* 隐藏状态 */
#globalSider.sidebar-hidden {
  transform: translateX(-100%);
}

.modern-sidebar {
  width: 100%;
  height: 100%;
  background: white;
  border-right: 1px solid #e0e0e0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}



/* 侧边栏头部 */
.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8f9fa;
  position: relative;
  z-index: 1;
}

.user-avatar {
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 导航菜单 */
.sidebar-menu {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 16px 20px;
  margin: 4px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #555;
  background: transparent;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f0f0f0;
  border-radius: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.team-space-item {
  margin-left: 24px;
}

.menu-item:hover::before {
  opacity: 1;
}

.menu-item:hover {
  color: #333;
  transform: translateX(4px);
}

.menu-item.active {
  background: #e3f2fd;
  color: #1976d2;
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.2);
}

.menu-item.active .menu-indicator {
  opacity: 1;
  transform: scaleY(1);
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  position: relative;
  z-index: 1;
}

.menu-label {
  font-size: 14px;
  font-weight: 500;
  flex: 1;
  position: relative;
  z-index: 1;
}

.menu-indicator {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 20px;
  background: #1976d2;
  border-radius: 2px;
  opacity: 0;
  transition: all 0.3s ease;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  background: #f8f9fa;
  position: relative;
  z-index: 1;
}

.footer-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin: 4px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
  font-size: 14px;
  gap: 12px;
}

.footer-item:hover {
  background: #e0e0e0;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #globalSider {
    width: 100%;
    height: auto;
    min-height: 200px;
  }

  .modern-sidebar {
    width: 100%;
    height: auto;
    min-height: 200px;
  }
}

/* 滚动条样式 */
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: #999;
}
</style>

