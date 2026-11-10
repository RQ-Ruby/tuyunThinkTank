import 'axios'

declare module 'axios' {
  interface AxiosRequestConfig<D = any> {
    requestType?: string
  }
}
