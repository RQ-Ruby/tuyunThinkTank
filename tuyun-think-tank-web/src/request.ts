/*请求封装*/
import axios from 'axios'
import { message } from 'ant-design-vue'

// API 前缀已经包含在生成代码中（例如 /api/user/login），这里只能配置 origin。
// 本地开发默认使用 Vite 的 /api 代理；生产环境默认走同源。
const myAxios = axios.create({
  baseURL: import.meta.env.VITE_API_ORIGIN || '',
  timeout: 50000,
  withCredentials: true,
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    if (data.code === 40100) {
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login') &&
        window.location.pathname !== '/'
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    return Promise.reject(error)
  },
)

export default myAxios