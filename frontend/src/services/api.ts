// Implements: T007 [ARCH-D5] 统一 API Client
// 来源: plan.md §3.3 前后端联动验证约束, §1.4 D5

import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一 API Client
 *
 * - 鉴权：Session Cookie 自动携带（Axios 默认 withCredentials=false，此处显式开启）
 * - 统一错误反馈：4xx/5xx 通过 ElMessage Toast 展示 response.data.message
 * - 401 未认证：跳转登录页
 * - 403 无权限：提示无权限
 * - 加载状态：由各 Store 自行管理 loading，此处不引入全局 loading（避免过度设计）
 */

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  withCredentials: true, // Session Cookie 鉴权
})

// 请求拦截器：当前无需额外处理，预留扩展点
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => config,
  (error: AxiosError) => Promise.reject(error),
)

// 响应拦截器：统一错误处理
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ code?: string; message?: string; timestamp?: string }>) => {
    if (!error.response) {
      // 网络错误或请求未发出
      ElMessage.error('网络异常，请检查网络连接')
      return Promise.reject(error)
    }

    const { status, data } = error.response

    switch (status) {
      case 401:
        // 未认证 → 跳转登录页
        // 避免在登录页本身触发循环跳转
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        break
      case 403:
        ElMessage.error(data?.message ?? '无权限访问')
        break
      default:
        // 4xx 业务错误 / 5xx 系统故障 → Toast 展示 message
        if (status >= 400) {
          ElMessage.error(data?.message ?? `请求失败（${status}）`)
        }
        break
    }

    return Promise.reject(error)
  },
)

export default apiClient
