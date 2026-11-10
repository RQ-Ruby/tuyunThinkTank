/**
 * 全局权限校验
 * - 管理员路由权限校验
 * - 登录用户权限校验
 * - 路由拦截器
 * - 权限不足时跳转登录页并携带重定向路径
 */
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import router from '@/router'

// 是否为首次获取登录用户
let firstLoginUser = true

/**
 * 全局权限校验, 路由拦截器
 * 1. 校验是否为管理员
 * 2. 校验是否登录
 * 3. 校验是否有权限访问该页面
 */
router.beforeEach(async (to) => {
  // 1. 获取用户状态管理实例
  const loginUserStore = useLoginUserStore()

  // 2. 获取当前缓存的用户信息
  let loginUser = loginUserStore.loginUser

  /*
   * 3. 首次加载处理（解决刷新页面时的用户信息同步问题）
   * - 通过firstLoginUser标记判断是否首次加载
   * - 异步请求最新用户数据并更新本地状态
   */
  if (firstLoginUser) {
    try {
      await loginUserStore.fetchLoginUser()
    } catch {
      // 首次请求用户信息失败时保持未登录状态，不要阻断页面导航
    }
    loginUser = loginUserStore.loginUser
    firstLoginUser = false
  }

  // 4. 读取路由元信息，统一处理登录校验
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  if (requiresAuth && !loginUser.id) {
    message.warning('请先登录')
    return {
      path: '/user/login',
      query: { redirect: to.fullPath },
    }
  }

  // 5. 后台路由同时校验后端返回的用户角色
  const requiresAdmin =
    to.matched.some((record) => record.meta.requiresAdmin) || to.path.startsWith('/admin')
  if (requiresAdmin && loginUser.userRole !== 'admin') {
    message.error('没有权限')
    return loginUser.id
      ? { path: '/' }
      : { path: '/user/login', query: { redirect: to.fullPath } }
  }

  return true
})
