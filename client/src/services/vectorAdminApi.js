// 向量数据库管理API服务
import { API_BASE_URL, commonHeaders } from './api'

// 向量数据库管理相关API
export const vectorAdminApi = {
  // 获取命名空间列表
  getNamespaces: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/vector/namespaces`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取命名空间列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 创建新的命名空间
  createNamespace: async (namespace) => {
    try {
      const formData = new FormData()
      formData.append('namespace', namespace)
      
      const response = await fetch(`${API_BASE_URL}/admin/llm/vector/namespace`, {
        method: 'POST',
        credentials: 'include',
        body: formData
      })
      return await response.json()
    } catch (error) {
      console.error('创建命名空间失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 获取命名空间下的文档列表
  getDocuments: async (namespace) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/vector/documents/${namespace}`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取文档列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 上传文档到向量数据库
  uploadDocument: async (formData) => {
    try {
      // 这里不使用commonHeaders，因为是表单提交
      const response = await fetch(`${API_BASE_URL}/admin/llm/vector/upload`, {
        method: 'POST',
        credentials: 'include',
        body: formData // 包含file, namespace, chunkSize, chunkOverlap
      })
      return await response.json()
    } catch (error) {
      console.error('上传文档失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 删除文档
  deleteDocument: async (namespace, docId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/vector/document?namespace=${namespace}&docId=${docId}`, {
        method: 'DELETE',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('删除文档失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  // 删除命名空间
  deleteNamespace: async (namespace) => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/llm/vector/namespace/${namespace}`, {
        method: 'DELETE',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('删除命名空间失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  }
} 