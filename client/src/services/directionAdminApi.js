// 医疗方向管理API服务
import { API_BASE_URL, commonHeaders } from './api'

// 医疗方向管理相关API
export const directionAdminApi = {
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

  // 创建/更新医疗方向
  saveDirection: async (directionData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/directions`, {
        method: 'POST',
        headers: commonHeaders,
        credentials: 'include',
        body: JSON.stringify(directionData)
      })
      return await response.json()
    } catch (error) {
      console.error('保存医疗方向失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 删除医疗方向
  deleteDirection: async (directionId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/directions/${directionId}`, {
        method: 'DELETE',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('删除医疗方向失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  }
} 