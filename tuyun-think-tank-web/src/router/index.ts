import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import HomeView  from '@/views/HomeView.vue'
import UserLoginView from '@/views/user/UserLoginView.vue'
import UserRegisterView from '@/views/user/UserRegisterView.vue'
import UserManageView from '@/views/admin/UserManageView.vue'
import AddPictureView from '@/views/Picture/AddPictureView.vue'
/**
 * 路由配置
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
  {
    path: '/',
    name: '主页',
    component: HomeView,
  },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginView,
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterView,
  },
  {
    path: '/admin/userManage',
    name: '用户管理',
    component: UserManageView,
  },
    {
      path: '/add_picture',
      name: '新建图片',
      component: AddPictureView,
    },
  {
      path: '/about',
      name: '关于',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/HomeView.vue'),
    },
  ],
})





export default router
