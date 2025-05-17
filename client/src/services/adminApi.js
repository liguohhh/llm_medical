// 管理员API服务
import { API_BASE_URL, commonHeaders } from './api'

// 管理员相关API
export const adminApi = {
  // 获取系统统计数据
  getSystemStats: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/stats`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include' // 允许发送cookie
      })
      return await response.json()
    } catch (error) {
      console.error('获取系统统计数据失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取用户列表
  getUserList: async (userType) => {
    try {
      const url = userType !== undefined 
        ? `${API_BASE_URL}/admin/users?userType=${userType}`
        : `${API_BASE_URL}/admin/users`
      
      const response = await fetch(url, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取用户列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取医疗方向列表
  getDirectionList: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/directions`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取医疗方向列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取智能体列表
  getAgentList: async (directionId) => {
    try {
      const url = directionId !== undefined 
        ? `${API_BASE_URL}/admin/agents?directionId=${directionId}`
        : `${API_BASE_URL}/admin/agents`
      
      const response = await fetch(url, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取智能体列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  }
} 