# 医疗辅助问答系统 - 流式消息API文档

## 流式消息接口

- **URL**: `/api/patient/conversations/stream`
- **方法**: POST
- **描述**: 创建新对话或向已有对话发送消息，使用流式响应返回智能体回复
- **请求格式**: JSON
- **响应格式**: Server-Sent Events (SSE)
- **认证方式**: Cookie 认证
- **权限要求**: 需要`ROLE_PATIENT`角色

### 请求参数

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

### 响应格式

响应采用Server-Sent Events (SSE)格式，可以实时接收服务器发送的事件。

事件类型:
- `message`: 智能体回复的内容，可能会分多次接收
- `error`: 错误消息

示例:
```
event: message
data: 您好，头痛可能有多种原因。

event: message
data: 根据您的描述，这可能是由压力、紧张或睡眠不足导致的。

event: message
data: 建议您多休息，保持充足的睡眠，如果症状持续，请及时就医。
```

### 使用示例

#### JavaScript前端代码示例

```javascript
const sendStreamMessage = async (conversationId, message) => {
  // 准备请求数据
  const requestData = {
    conversationId: conversationId, // 如果是新对话，则不提供conversationId而是提供agentId
    message: message
  };
  
  // 创建事件源
  const eventSource = new EventSource('/api/patient/conversations/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestData)
  });
  
  // 处理消息事件
  eventSource.addEventListener('message', (event) => {
    const responseData = event.data;
    // 将收到的消息添加到聊天界面
    appendMessage(responseData);
  });
  
  // 处理错误事件
  eventSource.addEventListener('error', (event) => {
    console.error('流式消息出错:', event.data);
    eventSource.close();
  });
  
  // 当连接关闭时
  eventSource.onclose = () => {
    console.log('流式连接已关闭');
  };
};

// 调用示例
sendStreamMessage(1, "我的头痛症状最近加重了，有什么建议吗？");
```

## 与FastAPI的连接说明

Spring Boot服务会将前端发送的消息转发给Python FastAPI服务，具体流程如下:

1. 前端发送消息到Spring Boot `/api/patient/conversations/stream` 接口
2. Spring Boot服务从数据库加载对话历史和用户信息
3. 将消息和上下文转发给FastAPI服务的流式接口 `/api/v1/stream`
4. FastAPI调用大模型并通过SSE返回流式回复
5. Spring Boot接收流式回复并实时转发给前端
6. 当回复结束时，Spring Boot将完整对话内容保存到数据库

### FastAPI接口参数说明

发送到FastAPI的参数包括:

```json
{
  "message": "用户消息",
  "model_settings": {
    "model_name": "大模型名称",
    "api_key": "API密钥",
    "base_url": "模型API地址"
  },
  "template_config": {
    "template_id": "医疗模板ID",
    "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
    "params": {
      "patient_age": "35",
      "patient_gender": "男",
      "symptoms": "头痛、发热"
    }
  },
  "vector_search_config": {
    "namespaces": ["medical", "disease"],
    "n_results": 3
  },
  "precise_search_config": {
    "categories": ["头痛类"],
    "max_results": 3,
    "search_depth": 2
  },
  "history": [
    {
      "role": "user",
      "content": "历史消息"
    },
    {
      "role": "assistant",
      "content": "历史回复"
    }
  ]
}
```

## 注意事项

1. 流式消息适合对响应时间敏感的场景，可以提供更好的用户体验
2. 处理流式响应时应考虑网络中断等异常情况
3. 前端实现时可根据不同框架选择适当的SSE客户端库
4. 对话最终会保存到数据库，可通过普通接口查询完整对话历史 