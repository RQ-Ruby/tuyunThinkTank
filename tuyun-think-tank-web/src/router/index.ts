import { createRouter, createWebHistory } from 'vue-router'
import HomeView  from '@/views/HomeView.vue'
import UserLoginView from '@/views/user/UserLoginView.vue'
import UserRegisterView from '@/views/user/UserRegisterView.vue'
import UserProfileView from '@/views/user/userProfile.vue'
import UserManageView from '@/views/admin/UserManageView.vue'
import AddPictureView from '@/views/Picture/AddPictureView.vue'
import PictureManageView from '@/views/admin/PictureManageView.vue'
import PictureInformationView from '@/views/Picture/PictureInformationView.vue'
import AddPictureBatchView from '@/views/Picture/AddPictureBatchView.vue'
import SpaceManageView from '@/views/admin/SpaceManageView.vue'
import AddSpaceView from '@/views/space/AddSpaceView.vue'
import MySpaceView from '@/views/space/MySpaceView.vue'
import SpaceDetailView from '@/views/space/SpaceDetailView.vue'
import SpaceUserManageView from '@/views/space/SpaceUserManageView.vue'
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
      path: '/add_picture',
      name: '新建图片',
      component: AddPictureView,
      meta: { requiresAuth: true },
    },
    {
      path: '/add_picture/batch',
      name: '批量创建图片',
      component: AddPictureBatchView,
      meta: { requiresAuth: true },
    },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginView,
    meta: { hideLayout: true }
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterView,
    meta: { hideLayout: true }
  },
  {
    path: '/user/profile',
    name: '个人中心',
    component: UserProfileView,
    meta: { requiresAuth: true },
  },
  {
    path: '/favorites',
    name: '我的收藏',
    component: () => import('../views/user/MyFavoritesView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/my_uploads',
    name: '我的上传',
    component: () => import('../views/user/MyUploadsView.vue'),
    meta: { requiresAuth: true },
  },


  {
    path: '/admin/userManage',
    name: '用户管理',
    component: UserManageView,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/pictureManage',
    name: '图片管理',
    component: PictureManageView,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
    {
      path: '/admin/spaceManage',
      name: '空间管理',
      component: SpaceManageView,
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/picture/:id',
      name: '图片详情',
      component: PictureInformationView,
      props: true,
    },
    {
      path: '/add_space',
      name: '创建空间',
      component: AddSpaceView,
      meta: { requiresAuth: true },
    },
    {
      path: '/space/edit/:id',
      name: '编辑空间',
      component: AddSpaceView,
      props: true,
      meta: { requiresAuth: true },
    },
    {
      path: '/my_space',
      name: '我的空间',
      component: MySpaceView,
      meta: { requiresAuth: true },
    },
    {
      path: '/space/:id',
      name: '空间详情',
      component: SpaceDetailView,
      props: true,
    },
    {
      path: '/spaceUserManage/:id',
      name: '空间成员管理',
      component: SpaceUserManageView,
      props: true,
      meta: { requiresAuth: true },
    },

    {
      path: '/about',
      name: '关于',
      component: HomeView,
    },
  ],
})





export default router
