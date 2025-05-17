# 医疗辅助问答系统 - 管理员API文档

## 概述

本文档详细描述了医疗辅助问答系统中管理员可用的API接口，提供了用户管理、医疗方向管理、智能体管理等功能的详细说明。

基础URL: `/api/admin`

所有接口都需要管理员权限，使用Cookie进行身份验证。

## 系统统计接口

### 获取系统统计数据

- **URL**: `/api/admin/stats`
- **方法**: GET
- **描述**: 获取系统关键数据统计信息
- **请求参数**: 无
- **响应格式**: JSON

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "totalPatients": 120,
    "totalDoctors": 25,
    "totalAdmins": 5,
    "totalDirections": 8,
    "totalAgents": 15,
    "totalConversations": 560,
    "finishedConversations": 480,
    "totalPrescriptions": 320
  }
}
```

## 用户管理接口

### 获取用户列表

- **URL**: `/api/admin/users`
- **方法**: GET
- **描述**: 获取系统中的用户列表，可按类型过滤
- **请求参数**:
  - `userType` (query, 可选): 用户类型，0-病人，1-医生，2-管理员

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "username": "patient001",
      "realName": "张三",
      "gender": 1,
      "age": 30,
      "userType": 0,
      "directionId": null,
      "direction": null,
      "createTime": "2023-05-10 12:30:45"
    },
    {
      "id": 2,
      "username": "doctor001",
      "realName": "李医生",
      "gender": 1,
      "age": 42,
      "userType": 1,
      "directionId": 1,
      "direction": {
        "id": 1,
        "uid": "dir_12345678",
        "name": "心内科",
        "description": "专注于心脏和循环系统疾病的诊断和治疗"
      },
      "createTime": "2023-04-15 09:20:30"
    }
  ]
}
```

### 创建/更新用户

- **URL**: `/api/admin/users`
- **方法**: POST
- **描述**: 创建新用户或更新现有用户
- **请求格式**: JSON

**请求示例**:

```json
{
  "id": null,  // 创建时不提供id，更新时必须提供
  "username": "newuser001",
  "realName": "王五",
  "gender": 1,
  "age": 35,
  "userType": 0,
  "directionId": null  // 用户类型为医生时才需要提供
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

### 删除用户

- **URL**: `/api/admin/users/{userId}`
- **方法**: DELETE
- **描述**: 删除指定用户
- **请求参数**:
  - `userId` (path): 用户ID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

### 重置用户密码

- **URL**: `/api/admin/users/{userId}/password`
- **方法**: PUT
- **描述**: 重置指定用户的密码
- **请求参数**:
  - `userId` (path): 用户ID
  - `newPassword` (query): 新密码

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

## 医疗方向管理接口

### 获取医疗方向列表

- **URL**: `/api/admin/directions`
- **方法**: GET
- **描述**: 获取所有医疗方向
- **请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "uid": "dir_12345678",
      "name": "心内科",
      "description": "专注于心脏和循环系统疾病的诊断和治疗",
      "createTime": "2023-03-15 10:00:00"
    },
    {
      "id": 2,
      "uid": "dir_87654321",
      "name": "神经内科",
      "description": "专注于神经系统疾病的诊断和治疗",
      "createTime": "2023-03-15 10:05:30"
    }
  ]
}
```

### 创建/更新医疗方向

- **URL**: `/api/admin/directions`
- **方法**: POST
- **描述**: 创建新医疗方向或更新现有医疗方向
- **请求格式**: JSON

**请求示例**:

```json
{
  "id": null,  // 创建时不提供id，更新时必须提供
  "name": "皮肤科",
  "description": "专注于皮肤疾病的诊断和治疗"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

### 删除医疗方向

- **URL**: `/api/admin/directions/{directionId}`
- **方法**: DELETE
- **描述**: 删除指定医疗方向
- **请求参数**:
  - `directionId` (path): 医疗方向ID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

## 智能体管理接口

### 获取智能体列表

- **URL**: `/api/admin/agents`
- **方法**: GET
- **描述**: 获取智能体列表，可按医疗方向过滤
- **请求参数**:
  - `directionId` (query, 可选): 医疗方向ID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "心脏病专家",
      "directionId": 1,
      "modelName": "gpt-3.5-turbo",
      "modelUrl": "https://api.openai.com/v1",
      "apiKey": "sk-xxxxxxxxxxxxxxxx",
      "templateId": "medical_template",
      "templateDescription": "医疗问诊模板",
      "templateParameters": "{}",
      "vectorNamespaces": "[\"medical\", \"heart_disease\"]",
      "preciseDbName": "医学知识库",
      "preciseDbUids": "[\"cat_12345678\", \"cat_87654321\"]",
      "direction": {
        "id": 1,
        "uid": "dir_12345678",
        "name": "心内科",
        "description": "专注于心脏和循环系统疾病的诊断和治疗"
      },
      "createTime": "2023-05-01 14:20:30"
    }
  ]
}
```

### 创建/更新智能体

- **URL**: `/api/admin/agents`
- **方法**: POST
- **描述**: 创建新智能体或更新现有智能体
- **请求格式**: JSON

**请求示例**:

```json
{
  "id": null,  // 创建时不提供id，更新时必须提供
  "name": "皮肤病专家",
  "directionId": 3,
  "modelName": "gpt-3.5-turbo",
  "modelUrl": "https://api.openai.com/v1",
  "apiKey": "sk-xxxxxxxxxxxxxxxx",
  "templateId": "medical_template",
  "templateDescription": "医疗问诊模板",
  "vectorNamespaces": ["medical", "skin_disease"],
  "preciseDbName": "医学知识库",
  "preciseDbUids": ["cat_34567890"]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

### 删除智能体

- **URL**: `/api/admin/agents/{agentId}`
- **方法**: DELETE
- **描述**: 删除指定智能体
- **请求参数**:
  - `agentId` (path): 智能体ID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

### 测试智能体配置

- **URL**: `/api/admin/agents/{agentId}/test`
- **方法**: POST
- **描述**: 测试智能体配置是否正常工作
- **请求参数**:
  - `agentId` (path): 智能体ID
  - `testMessage` (query): 测试消息

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": "您好，我是皮肤病专家，很高兴为您服务。根据您的描述，这种情况可能是常见的湿疹，建议您保持皮肤清洁干燥，避免使用刺激性化妆品和肥皂..."
}
```

## 与FastAPI集成

本系统通过LLM服务与FastAPI进行集成，智能体管理接口中的配置直接影响系统与FastAPI的交互行为。

### FastAPI集成参数说明

智能体配置中的以下参数用于与FastAPI进行集成:

- **modelName**: 对应FastAPI请求中的 `model_settings.model_name`
- **modelUrl**: 对应FastAPI请求中的 `model_settings.base_url`
- **apiKey**: 对应FastAPI请求中的 `model_settings.api_key`
- **templateId**: 对应FastAPI请求中的 `template_config.template_id`
- **vectorNamespaces**: 对应FastAPI请求中的 `vector_search_config.namespaces`
- **preciseDbUids**: 对应FastAPI请求中的 `precise_search_config.categories`

### FastAPI请求示例

当管理员创建智能体并使用它进行对话时，系统会向FastAPI发送如下请求:

```json
{
  "message": "用户消息",
  "model_settings": {
    "model_name": "gpt-3.5-turbo",
    "api_key": "sk-xxxxxxxxxxxxxxxx",
    "base_url": "https://api.openai.com/v1"
  },
  "template_config": {
    "template_id": "medical_template",
    "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
    "params": {
      "patient_age": "35",
      "patient_gender": "男",
      "symptoms": "头痛、发热"
    }
  },
  "vector_search_config": {
    "namespaces": ["medical", "heart_disease"],
    "n_results": 3
  },
  "precise_search_config": {
    "categories": ["cat_12345678", "cat_87654321"],
    "max_results": 3,
    "search_depth": 2
  }
}
```

## 错误码说明

所有API都使用统一的错误码格式:

- **200**: 操作成功
- **400**: 请求参数错误
- **401**: 未授权，需要登录
- **403**: 权限不足，需要管理员权限
- **404**: 资源不存在
- **500**: 服务器内部错误

## 安全性建议

1. 所有API请求必须通过HTTPS进行
2. 生产环境中的API密钥应该加密存储
3. 应定期更改管理员密码
4. 对敏感操作实施IP限制和操作日志记录 