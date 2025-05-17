# 医疗辅助问答系统 - 病人API文档

## 基础信息

- 基础URL: `http://localhost:8086`
- 请求格式: JSON
- 响应格式: JSON
- 认证方式: Cookie 认证
- 权限要求: 所有接口需要`ROLE_PATIENT`角色

## 通用响应格式

所有API响应均采用以下JSON格式:

```json
{
  "code": 200,       // 状态码：200-成功，非200-失败
  "message": "成功",  // 响应消息
  "data": {}         // 响应数据，可能为null
}
```

## 病人相关接口

### 1. 获取所有智能体

- **URL**: `/api/patient/agents`
- **方法**: GET
- **描述**: 获取系统中所有可供对话的智能体列表
- **请求参数**: 无
- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "内科智能助手",
      "directionId": 1,
      "directionName": "内科",
      "directionDescription": "内科医学专业方向，主要处理内脏相关疾病"
    },
    {
      "id": 2,
      "name": "外科智能助手",
      "directionId": 2,
      "directionName": "外科",
      "directionDescription": "外科医学专业方向，主要处理需要手术的疾病"
    }
  ]
}
```

### 2. 获取历史对话列表

- **URL**: `/api/patient/conversations`
- **方法**: GET
- **描述**: 获取当前登录病人的历史对话列表
- **请求参数**: 无
- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "uid": "conversation-uid-1",
      "userId": 10,
      "agentId": 1,
      "agentName": "内科智能助手",
      "directionName": "内科",
      "isFinished": 0,
      "prescriptionId": null,
      "lastMessage": "最后一条消息预览",
      "createTime": "2023-10-01 12:00:00",
      "updateTime": "2023-10-01 12:30:00"
    },
    {
      "id": 2,
      "uid": "conversation-uid-2",
      "userId": 10,
      "agentId": 2,
      "agentName": "外科智能助手",
      "directionName": "外科",
      "isFinished": 1,
      "prescriptionId": 1,
      "lastMessage": "已生成处方",
      "createTime": "2023-10-02 14:00:00",
      "updateTime": "2023-10-02 14:45:00"
    }
  ]
}
```

### 3. 获取对话详情

- **URL**: `/api/patient/conversations/{conversationId}`
- **方法**: GET
- **描述**: 获取指定对话的详细内容
- **路径参数**:
  - `conversationId`: 对话ID
- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "uid": "conversation-uid-1",
    "userId": 10,
    "userName": "张三",
    "agentId": 1,
    "agentName": "内科智能助手",
    "directionName": "内科",
    "isFinished": 0,
    "prescriptionId": null,
    "messages": [
      {
        "id": "msg-1",
        "content": "我最近感觉头疼，是怎么回事？",
        "type": 0,
        "timestamp": "2023-10-01 12:00:00"
      },
      {
        "id": "msg-2",
        "content": "您好，头疼可能有多种原因。能否详细描述一下症状，如疼痛部位、持续时间等？",
        "type": 1,
        "timestamp": "2023-10-01 12:00:10"
      }
    ],
    "createTime": "2023-10-01 12:00:00",
    "updateTime": "2023-10-01 12:00:10"
  }
}
```

### 4. 创建新对话或发送消息

- **URL**: `/api/patient/conversations/message`
- **方法**: POST
- **描述**: 创建新对话或向已有对话发送消息
- **请求参数**:

```json
// 创建新对话
{
  "agentId": 1,
  "message": "我最近感觉头疼，是怎么回事？"
}

// 向已有对话发送消息
{
  "conversationId": 1,
  "message": "头疼部位在额头，持续了两天"
}
```

- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "uid": "conversation-uid-1",
    "userId": 10,
    "userName": "张三",
    "agentId": 1,
    "agentName": "内科智能助手",
    "directionName": "内科",
    "isFinished": 0,
    "prescriptionId": null,
    "messages": [
      // 对话消息列表
    ],
    "createTime": "2023-10-01 12:00:00",
    "updateTime": "2023-10-01 12:00:10"
  }
}
```

### 5. 生成处方

- **URL**: `/api/patient/conversations/{conversationId}/prescription`
- **方法**: POST
- **描述**: 结束对话并生成处方
- **路径参数**:
  - `conversationId`: 对话ID
- **请求参数**: 无
- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "userId": 10,
    "userName": "张三",
    "agentId": 1,
    "agentName": "内科智能助手",
    "directionId": 1,
    "directionName": "内科",
    "content": "根据症状分析，可能是感冒引起的头痛。建议：\n1. 充分休息\n2. 多喝水\n3. 必要时服用布洛芬缓解症状",
    "reviewStatus": 0,
    "reviewerId": null,
    "reviewerName": null,
    "reviewComment": null,
    "createTime": "2023-10-01 13:00:00",
    "updateTime": "2023-10-01 13:00:00"
  }
}
```

### 6. 获取处方详情

- **URL**: `/api/patient/prescriptions/{prescriptionId}`
- **方法**: GET
- **描述**: 获取指定处方的详细信息
- **路径参数**:
  - `prescriptionId`: 处方ID
- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "userId": 10,
    "userName": "张三",
    "agentId": 1,
    "agentName": "内科智能助手",
    "directionId": 1,
    "directionName": "内科",
    "content": "根据症状分析，可能是感冒引起的头痛。建议：\n1. 充分休息\n2. 多喝水\n3. 必要时服用布洛芬缓解症状",
    "reviewStatus": 1,
    "reviewerId": 20,
    "reviewerName": "李医生",
    "reviewComment": "处方合理，同意。",
    "createTime": "2023-10-01 13:00:00",
    "updateTime": "2023-10-02 10:00:00"
  }
}
```

## 错误码说明

| 错误码 | 说明 |
| ----- | ---- |
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或认证失败 |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

## 注意事项

1. 所有接口需要先登录才能访问
2. 只有具有`ROLE_PATIENT`角色的用户才能访问这些接口
3. 病人只能查看和操作自己的对话和处方
4. 处方的`reviewStatus`状态说明：
   - 0: 未审核
   - 1: 审核通过
   - 2: 审核不通过
5. 消息的`type`类型说明：
   - 0: 用户消息
   - 1: 系统/智能体消息 