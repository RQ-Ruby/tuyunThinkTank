<!--全局顶部栏组件-->

<template>
  <a-row :wrap="false">
    <a-col flex="200px">
      <RouterLink to="/">
        <div class="title-bar">
          <img class="logo" src="../assets/logo.png" alt="logo" />
          <div class="title">图云智库</div>
        </div>
      </RouterLink>
    </a-col>
    <a-col flex="auto">
      <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click="doMenu" />
    </a-col>
    <a-col flex="120px">
      <!--         用户登录状态-->
      <div class="user-login-status">
        <div v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <ASpace>
              <a-avatar :src="loginUserStore.loginUser.userAvatar" />
              {{ loginUserStore.loginUser.userName ?? '无名' }}
            </ASpace>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>

        <div v-else>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </div>
    </a-col>
  </a-row>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue'
//为什么是type，而不能直接import  { MenuProps } from 'ant-design-vue'
import { type MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLogoutUsingPost } from '@/api/userController'
const loginUserStore = useLoginUserStore()
//原始菜单
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    label: '新建图片',
    title: '新建图片',
  }
  ,
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  }
  ,

  {
    key: 'others',
    label: h('a', { href: 'https://github.com/rich0807', target: '_blank' }, '关于赚钱'),
    title: '关于赚钱',
  },
]
//根据用户身份过滤菜单
const filterMenu = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    // 添加类型检查和空值判断
    if (menu?.key && typeof menu.key === 'string' && menu.key.startsWith('/admin')) {
      const role = loginUserStore.loginUser?.userRole
      if (role !== 'admin' || !role) {
        return false
      }
    }
    return true
  })
}

//根据用户身份过滤菜单
/*
依赖追踪：当 originItems 变化时，computed 会自动重新计算 items，无需手动触发更新
性能优化：computed 会缓存计算结果，只有 originItems 变化时才重新执行 filterMenu，避免重复计算
*/
const items = computed<MenuProps['items']>(() => filterMenu(originItems))

const router = useRouter()
// 路由跳转事件
//key 是点击的菜单的key
const doMenu = ({ key }: { key: string }) => {
  router.push({
    // 指定要跳转的页面
    path: key,
  })
}

// 当前选中菜单
const current = ref<string[]>([])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  console.log(res)
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 45px;
}
</style>
