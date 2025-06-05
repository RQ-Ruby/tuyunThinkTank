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
router.beforeEach(async (to, from, next) => {
  // 1. 获取用户状态管理实例（Pinia/Vuex）
  const loginUserStore = useLoginUserStore()

  // 2. 获取当前缓存的用户信息
  let loginUser = loginUserStore.loginUser

  /*
   * 3. 首次加载处理（解决刷新页面时的用户信息同步问题）
   * - 通过firstLoginUser标记判断是否首次加载
   * - 异步请求最新用户数据并更新本地状态
   */
  if (firstLoginUser) {
    await loginUserStore.fetchLoginUser() // 3.1 请求后端获取用户信息
    loginUser = loginUserStore.loginUser // 3.2 更新本地用户数据引用
    firstLoginUser = false // 3.3 标记已加载完成
  }

  // 4. 获取目标路由完整路径
  const targetPath = to.fullPath

  /*
   * 5. 管理员路由权限校验
   * - 匹配所有/admin开头的路径
   * - 需同时满足：用户已登录 + 角色为admin
   */
  if (targetPath.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 'admin') {
      // 5.1 权限不足处理
      message.error('没有权限')
      // 5.2 跳转登录页并携带重定向路径
      //redirect=${targetPath}：将当前试图访问的完整路径作为URL参数传递
      next(`/user/login?redirect=${targetPath}`)
      return // 5.3 终止后续守卫执行
    }
  }

  // 6. 放行路由（必须调用next()，否则导航挂起
  next()
})
