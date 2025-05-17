import { API_BASE_URL, commonHeaders } from '@/services/api'

/**
 * 封装请求工具函数
 * @param {Object} options 请求选项
 * @param {string} options.url 请求URL
 * @param {string} options.method 请求方法
 * @param {Object} options.params 查询参数
 * @param {Object} options.data 请求体数据
 * @param {Object} options.headers 请求头
 * @returns {Promise<Object>}
 */
const request = async (options) => {
  const { url, method = 'get', params, data, headers = {} } = options
  
  // 构建完整URL
  let fullUrl = url.startsWith('http') ? url : url.startsWith('/api') ? url : `${API_BASE_URL}${url}`
  
  // 处理查询参数
  if (params) {
    const queryParams = new URLSearchParams()
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null) {
        queryParams.append(key, params[key])
      }
    })
    const queryString = queryParams.toString()
    if (queryString) {
      fullUrl += `?${queryString}`
    }
  }
  
  // 构建请求选项
  const requestOptions = {
    method,
    headers: {
      ...commonHeaders,
      ...headers
    },
    credentials: 'include' // 允许发送cookie
  }
  
  // 添加请求体
  if (data && (method.toLowerCase() === 'post' || method.toLowerCase() === 'put' || method.toLowerCase() === 'patch')) {
    requestOptions.body = JSON.stringify(data)
  }
  
  try {
    const response = await fetch(fullUrl, requestOptions)
    
    // 尝试解析JSON响应
    let result
    try {
      result = await response.json()
    } catch (jsonError) {
      // 如果服务器返回空响应或非JSON格式，构造一个标准响应格式
      result = {
        code: response.ok ? 200 : response.status,
        message: response.ok ? '操作成功' : '操作失败',
        data: null
      }
    }
    
    return result
  } catch (error) {
    console.error('请求失败:', error)
    return {
      code: 500,
      message: '网络错误，请稍后重试',
      data: null
    }
  }
}

export default request 