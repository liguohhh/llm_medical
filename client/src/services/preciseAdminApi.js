// 精确查找数据库管理API服务
import { API_BASE_URL, commonHeaders } from './api'

// 精确查找数据库管理相关API
export const preciseAdminApi = {
  // 获取所有大类
  getCategories: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/precise/categories`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取大类列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 创建大类
  createCategory: async (categoryData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/precise/category`, {
        method: 'POST',
        headers: commonHeaders,
        credentials: 'include',
        body: JSON.stringify(categoryData)
      })
      return await response.json()
    } catch (error) {
      console.error('创建大类失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 更新大类
  updateCategory: async (categoryUid, categoryData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/precise/category/${categoryUid}`, {
        method: 'PUT',
        headers: commonHeaders,
        credentials: 'include',
        body: JSON.stringify(categoryData)
      })
      return await response.json()
    } catch (error) {
      console.error('更新大类失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 删除大类
  deleteCategory: async (categoryUid) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/precise/category/${categoryUid}`, {
        method: 'DELETE',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('删除大类失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取大类下的条目列表
  getEntries: async (categoryUid) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/precise/entries/${categoryUid}`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取条目列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 创建条目
  createEntry: async (categoryUid, entryData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/precise/entry/${categoryUid}`, {
        method: 'POST',
        headers: commonHeaders,
        credentials: 'include',
        body: JSON.stringify(entryData)
      })
      return await response.json()
    } catch (error) {
      console.error('创建条目失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 更新条目
  updateEntry: async (entryUid, entryData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/precise/entry/${entryUid}`, {
        method: 'PUT',
        headers: commonHeaders,
        credentials: 'include',
        body: JSON.stringify(entryData)
      })
      return await response.json()
    } catch (error) {
      console.error('更新条目失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 删除条目
  deleteEntry: async (entryUid) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/precise/entry/${entryUid}`, {
        method: 'DELETE',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('删除条目失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  }
} 