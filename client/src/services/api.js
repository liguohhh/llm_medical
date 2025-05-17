// 使用相对路径，由代理服务器转发请求
export const API_BASE_URL = '/api'

// 通用请求头
export const commonHeaders = {
  'Content-Type': 'application/json',
  'X-Requested-With': 'XMLHttpRequest'
}

// 用户认证相关API
export const authApi = {
  // 用户注册
  register: async (userData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: commonHeaders,
        body: JSON.stringify(userData),
        credentials: 'include' // 允许发送cookie
      })
      return await response.json()
    } catch (error) {
      console.error('注册失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 用户登录
  login: async (credentials) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: commonHeaders,
        body: JSON.stringify(credentials),
        credentials: 'include' // 允许发送cookie
      })
      return await response.json()
    } catch (error) {
      console.error('登录失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取当前用户信息
  getUserInfo: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/info`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include' // 允许发送cookie
      })
      return await response.json()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 用户注销
  logout: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        headers: commonHeaders,
        credentials: 'include'
      })
      
      // 即使服务器可能返回空响应，也视为登出成功
      if (!response.ok) {
        return { code: response.status, message: '注销失败', data: null }
      }
      
      try {
        return await response.json()
      } catch (jsonError) {
        // 如果服务器返回空响应，这里会捕获JSON解析错误
        // 但仍然认为登出成功
        return { code: 200, message: '注销成功', data: null }
      }
    } catch (error) {
      console.error('注销失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  }
} 