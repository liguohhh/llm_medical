// 模板管理API服务
import { API_BASE_URL, commonHeaders } from './api'

// 模板管理相关API
export const templateAdminApi = {
  // 获取所有模板ID
  getTemplateIds: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/templates`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取模板ID列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取模板详情
  getTemplateDetail: async (templateId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/templates/${templateId}`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取模板详情失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 创建或更新模板
  saveTemplate: async (templateData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/templates`, {
        method: 'POST',
        headers: commonHeaders,
        credentials: 'include',
        body: JSON.stringify(templateData)
      })
      return await response.json()
    } catch (error) {
      console.error('保存模板失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 删除模板
  deleteTemplate: async (templateId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/templates/${templateId}`, {
        method: 'DELETE',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('删除模板失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  }
} 