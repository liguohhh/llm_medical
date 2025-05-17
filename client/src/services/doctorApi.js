import request from '@/utils/request'

export const doctorApi = {
  /**
   * 获取处方列表
   * @param {Object} params 查询参数
   * @param {number} params.page 页码
   * @param {number} params.size 每页大小
   * @param {string} params.sort 排序字段
   * @param {string} params.order 排序方式 asc/desc
   * @param {number} params.status 审核状态 0-待审核 1-已通过 2-已拒绝
   * @param {string} params.keyword 关键词搜索
   * @param {string} params.startDate 开始日期
   * @param {string} params.endDate 结束日期
   * @returns {Promise<Object>}
   */
  getPrescriptionList(params) {
    return request({
      url: '/api/doctor/prescriptions',
      method: 'get',
      params
    })
  },

  /**
   * 获取处方详情
   * @param {number|string} prescriptionId 处方ID
   * @returns {Promise<Object>}
   */
  getPrescriptionDetail(prescriptionId) {
    return request({
      url: `/api/doctor/prescriptions/${prescriptionId}`,
      method: 'get'
    })
  },

  /**
   * 审核处方
   * @param {number|string} prescriptionId 处方ID
   * @param {number} status 审核状态 1-通过 2-拒绝
   * @param {string} comment 审核意见
   * @returns {Promise<Object>}
   */
  reviewPrescription(prescriptionId, status, comment) {
    return request({
      url: `/api/doctor/prescriptions/${prescriptionId}/review`,
      method: 'post',
      data: {
        status,
        comment
      }
    })
  },

  /**
   * 获取对话记录详情
   * @param {number|string} conversationId 对话ID
   * @returns {Promise<Object>}
   */
  getConversationDetail(conversationId) {
    return request({
      url: `/api/doctor/conversations/${conversationId}`,
      method: 'get'
    })
  },

  /**
   * 获取医生首页统计数据
   * @returns {Promise<Object>}
   */
  getDashboardStats() {
    return request({
      url: '/api/doctor/dashboard/stats',
      method: 'get'
    })
  },

  /**
   * 获取医生个人信息
   * @returns {Promise<Object>}
   */
  getDoctorProfile() {
    return request({
      url: '/api/doctor/profile',
      method: 'get'
    })
  },

  /**
   * 更新医生个人信息
   * @param {Object} data 医生个人信息
   * @returns {Promise<Object>}
   */
  updateDoctorProfile(data) {
    return request({
      url: '/api/doctor/profile',
      method: 'put',
      data
    })
  },

  /**
   * 修改密码
   * @param {Object} data 密码信息
   * @param {string} data.oldPassword 旧密码
   * @param {string} data.newPassword 新密码
   * @returns {Promise<Object>}
   */
  changePassword(data) {
    return request({
      url: '/api/doctor/change-password',
      method: 'post',
      data
    })
  },

  /**
   * 获取向量数据库命名空间列表
   * @returns {Promise<Object>}
   */
  getNamespaces() {
    return request({
      url: '/api/doctor/llm/vector/namespaces',
      method: 'get'
    })
  },

  /**
   * 创建新的命名空间
   * @param {string} namespace 命名空间名称
   * @returns {Promise<Object>}
   */
  createNamespace(namespace) {
    const formData = new FormData()
    formData.append('namespace', namespace)
    
    return request({
      url: '/api/doctor/llm/vector/namespace',
      method: 'post',
      data: formData
    })
  },

  /**
   * 获取向量数据库文档列表
   * @param {string} namespace 命名空间
   * @returns {Promise<Object>}
   */
  getDocuments(namespace) {
    return request({
      url: `/api/doctor/llm/vector/documents/${namespace}`,
      method: 'get'
    })
  },

  /**
   * 上传文档到向量数据库
   * @param {FormData} formData 文档表单数据
   * @returns {Promise<Object>}
   */
  uploadDocument(formData) {
    return request({
      url: '/api/doctor/llm/vector/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除向量数据库文档
   * @param {string} namespace 命名空间
   * @param {string} docId 文档ID
   * @returns {Promise<Object>}
   */
  deleteDocument(namespace, docId) {
    return request({
      url: `/api/doctor/llm/vector/document`,
      method: 'delete',
      params: { namespace, docId }
    })
  },

  /**
   * 删除命名空间
   * @param {string} namespace 命名空间
   * @returns {Promise<Object>}
   */
  deleteNamespace(namespace) {
    return request({
      url: `/api/doctor/llm/vector/namespace/${namespace}`,
      method: 'delete'
    })
  },

  /**
   * 获取精确查找数据库大类列表
   * @returns {Promise<Object>}
   */
  getPreciseCategories() {
    return request({
      url: '/api/doctor/llm/precise/categories',
      method: 'get'
    })
  },

  /**
   * 创建大类
   * @param {Object} categoryData 大类数据
   * @returns {Promise<Object>}
   */
  createPreciseCategory(categoryData) {
    return request({
      url: '/api/doctor/llm/precise/category',
      method: 'post',
      data: categoryData
    })
  },

  /**
   * 更新大类
   * @param {string} categoryUid 大类ID
   * @param {Object} categoryData 大类数据
   * @returns {Promise<Object>}
   */
  updatePreciseCategory(categoryUid, categoryData) {
    return request({
      url: `/api/doctor/llm/precise/category/${categoryUid}`,
      method: 'put',
      data: categoryData
    })
  },

  /**
   * 删除精确查找数据库大类
   * @param {string} categoryUid 大类ID
   * @returns {Promise<Object>}
   */
  deletePreciseCategory(categoryUid) {
    return request({
      url: `/api/doctor/llm/precise/category/${categoryUid}`,
      method: 'delete'
    })
  },

  /**
   * 获取精确查找数据库条目列表
   * @param {string} categoryUid 大类ID
   * @returns {Promise<Object>}
   */
  getPreciseEntries(categoryUid) {
    return request({
      url: `/api/doctor/llm/precise/entries/${categoryUid}`,
      method: 'get'
    })
  },

  /**
   * 创建条目
   * @param {string} categoryUid 大类ID
   * @param {Object} entryData 条目数据
   * @returns {Promise<Object>}
   */
  createPreciseEntry(categoryUid, entryData) {
    return request({
      url: `/api/doctor/llm/precise/entry/${categoryUid}`,
      method: 'post',
      data: entryData
    })
  },

  /**
   * 更新条目
   * @param {string} entryUid 条目ID
   * @param {Object} entryData 条目数据
   * @returns {Promise<Object>}
   */
  updatePreciseEntry(entryUid, entryData) {
    return request({
      url: `/api/doctor/llm/precise/entry/${entryUid}`,
      method: 'put',
      data: entryData
    })
  },

  /**
   * 删除精确查找数据库条目
   * @param {string} entryUid 条目ID
   * @returns {Promise<Object>}
   */
  deletePreciseEntry(entryUid) {
    return request({
      url: `/api/doctor/llm/precise/entry/${entryUid}`,
      method: 'delete'
    })
  }
}

export default doctorApi 