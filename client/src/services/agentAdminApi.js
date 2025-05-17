// 智能体管理API服务
import { API_BASE_URL, commonHeaders } from './api'

// 智能体管理相关API
export const agentAdminApi = {
  // 获取智能体列表
  getAgentList: async (directionId) => {
    try {
      let url = `${API_BASE_URL}/admin/agents`
      if (directionId !== undefined && directionId !== null) {
        url += `?directionId=${directionId}`
      }
      
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
  },

  // 创建/更新智能体
  saveAgent: async (agentData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/agents`, {
        method: 'POST',
        headers: commonHeaders,
        credentials: 'include',
        body: JSON.stringify(agentData)
      })
      return await response.json()
    } catch (error) {
      console.error('保存智能体失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 删除智能体
  deleteAgent: async (agentId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/agents/${agentId}`, {
        method: 'DELETE',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('删除智能体失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取医疗方向列表（用于智能体关联）
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