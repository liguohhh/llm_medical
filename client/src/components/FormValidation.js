// 通用表单验证规则
export const validationRules = {
  // 用户名验证规则
  username: [
    value => !!value || '用户名不能为空',
    value => (value && value.length >= 4 && value.length <= 20) || '用户名长度应为4-20个字符'
  ],
  
  // 密码验证规则
  password: [
    value => !!value || '密码不能为空',
    value => (value && value.length >= 6 && value.length <= 20) || '密码长度应为6-20个字符'
  ],
  
  // 姓名验证规则
  realName: [
    value => !!value || '姓名不能为空'
  ],
  
  // 年龄验证规则
  age: [
    value => !!value || '年龄不能为空',
    value => (value !== null && value !== '' && !isNaN(value) && value > 0 && value < 150) || '请输入有效年龄'
  ],
  
  // 必填字段验证
  required: [
    value => !!value || '该字段不能为空'
  ]
} 