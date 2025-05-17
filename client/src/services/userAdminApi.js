// 管理员用户管理API服务
import { API_BASE_URL, commonHeaders } from './api'

// 用户管理相关API
export const userAdminApi = {
  // 获取用户列表（带分页）
  getUserList: async (userType, page = 1, pageSize = 10) => {
    try {
      let url = `${API_BASE_URL}/admin/users?page=${page}&pageSize=${pageSize}`
      if (userType !== undefined) {
        url += `&userType=${userType}`
      }
      
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

  // 创建/更新用户
  saveUser: async (userData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/users`, {
        method: 'POST',
        headers: commonHeaders,
        credentials: 'include',
        body: JSON.stringify(userData)
      })
      return await response.json()
    } catch (error) {
      console.error('保存用户信息失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 删除用户
  deleteUser: async (userId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/users/${userId}`, {
        method: 'DELETE',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('删除用户失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 重置用户密码
  resetUserPassword: async (userId, newPassword) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/users/${userId}/password?newPassword=${newPassword}`, {
        method: 'PUT',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('重置用户密码失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取医疗方向列表（用于医生关联）
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
  }
} 