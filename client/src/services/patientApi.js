import { API_BASE_URL, commonHeaders } from './api'

// 病人相关API
export const patientApi = {
  // 获取所有智能体
  getAgents: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/agents`, {
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
  
  // 获取历史对话列表
  getConversations: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/conversations`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取对话列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },
  
  // 获取对话详情
  getConversation: async (conversationId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/conversations/${conversationId}`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取对话详情失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },
  
  // 创建新对话
  createConversation: async (agentId, initialMessage) => {
    try {
      const data = {
        agentId,
        message: initialMessage
      }
      const response = await fetch(`${API_BASE_URL}/patient/conversations/message`, {
        method: 'POST',
        headers: commonHeaders,
        body: JSON.stringify(data),
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('创建对话失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },
  
  // 创建新对话或发送消息
  sendMessage: async (data) => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/conversations/message`, {
        method: 'POST',
        headers: commonHeaders,
        body: JSON.stringify(data),
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('发送消息失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },
  
  // 生成处方
  generatePrescription: async (conversationId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/conversations/${conversationId}/prescription`, {
        method: 'POST',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('生成处方失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },
  
  // 获取处方详情
  getPrescription: async (prescriptionId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/prescriptions/${prescriptionId}`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取处方详情失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },
  
  // 获取聊天设置
  getChatSettings: async (agentId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/agents/${agentId}/chat-settings`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取聊天设置失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },
  
  // 流式发送消息
  streamMessage: async (data, onMessage, onError, onComplete) => {
    try {
      // 构造完整URL字符串
      const urlString = `${window.location.origin}${API_BASE_URL}/patient/conversations/stream`;
      
      // 发送POST请求，因为EventSource只支持GET
      fetch(urlString, {
        method: 'POST',
        headers: commonHeaders,
        body: JSON.stringify(data),
        credentials: 'include'
      }).catch(error => {
        console.error('发送POST请求失败:', error);
        if (onError && typeof onError === 'function') {
          onError(error);
        }
      });
      
      // 创建EventSource
      const eventSource = new EventSource(urlString, {
        withCredentials: true
      });
      
      // 监听消息事件
      eventSource.addEventListener('message', (event) => {
        if (onMessage && typeof onMessage === 'function') {
          onMessage(event.data);
        }
      });
      
      // 监听错误事件
      eventSource.addEventListener('error', (event) => {
        console.error('SSE连接错误:', event);
        if (onError && typeof onError === 'function') {
          onError(event);
        }
        eventSource.close();
      });
      
      // 连接打开事件
      eventSource.onopen = () => {
        console.log('SSE连接已打开');
      };
      
      // 10分钟后自动关闭连接
      const timeout = setTimeout(() => {
        eventSource.close();
        if (onComplete && typeof onComplete === 'function') {
          onComplete();
        }
      }, 10 * 60 * 1000);
      
      // 返回EventSource实例和清除超时的函数
      return {
        eventSource,
        close: () => {
          clearTimeout(timeout);
          eventSource.close();
          if (onComplete && typeof onComplete === 'function') {
            onComplete();
          }
        }
      };
    } catch (error) {
      console.error('创建流式连接失败:', error);
      if (onError && typeof onError === 'function') {
        onError(error);
      }
      return null;
    }
  },

  /**
   * 从对话内容中提取处方
   * @param {number} conversationId - 对话ID
   * @param {string} content - 处方内容
   * @returns {Promise<Object>} - 处方信息
   */
  extractPrescription: async (conversationId, content) => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/conversations/${conversationId}/extract-prescription`, {
        method: 'POST',
        headers: commonHeaders,
        body: JSON.stringify({ content }),
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('提取处方失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },

  /**
   * 获取处方详情
   * @param {number} prescriptionId - 处方ID
   * @returns {Promise<Object>} - 处方详情
   */
  getPrescriptionDetail: async (prescriptionId) => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/prescriptions/${prescriptionId}`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取处方详情失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  },
  
  /**
   * 获取处方列表
   * @returns {Promise<Object>} - 处方列表
   */
  getPrescriptions: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/patient/prescriptions`, {
        method: 'GET',
        headers: commonHeaders,
        credentials: 'include'
      })
      return await response.json()
    } catch (error) {
      console.error('获取处方列表失败:', error)
      return { code: 500, message: '服务器连接失败', data: null }
    }
  }
} 