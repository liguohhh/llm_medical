# 医疗辅助问答系统 - LLM后端管理API文档

## 概述

本文档详细描述了医疗辅助问答系统中管理员可用的LLM后端管理API接口，提供了向量数据库管理、精确查找数据库管理和提示词模板管理等功能的详细说明。

基础URL: `/api/admin/llm`

所有接口都需要管理员权限，使用Cookie进行身份验证。

## 向量数据库管理接口

向量数据库用于存储文档并进行语义检索，为智能体提供知识支持。

### 获取命名空间列表

- **URL**: `/api/admin/llm/vector/namespaces`
- **方法**: GET
- **描述**: 获取所有可用的向量数据库命名空间
- **请求参数**: 无
- **响应格式**: JSON

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": ["medical", "disease", "drugs"]
}
```

### 获取命名空间下的文档列表

- **URL**: `/api/admin/llm/vector/documents/{namespace}`
- **方法**: GET
- **描述**: 获取指定命名空间中的所有文档
- **请求参数**:
  - `namespace` (path): 命名空间名称

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "doc_id": "550e8400-e29b-41d4-a716-446655440000",
      "source": "example.txt"
    },
    {
      "doc_id": "550e8400-e29b-41d4-a716-446655440001",
      "source": "medical_info.pdf"
    }
  ]
}
```

### 上传文档到向量数据库

- **URL**: `/api/admin/llm/vector/upload`
- **方法**: POST
- **描述**: 上传文件到向量数据库的指定命名空间
- **请求参数**:
  - `file` (form-data): 文件数据，支持txt、docx、pdf格式
  - `namespace` (form-data): 命名空间
  - `chunkSize` (form-data, 可选): 文档分块大小，默认500
  - `chunkOverlap` (form-data, 可选): 分块重叠大小，默认50

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "namespace": "medical",
    "metadata": {
      "source": "example.txt",
      "doc_id": "550e8400-e29b-41d4-a716-446655440000"
    },
    "chunks_count": 5
  }
}
```

### 删除文档

- **URL**: `/api/admin/llm/vector/document`
- **方法**: DELETE
- **描述**: 删除指定文档
- **请求参数**:
  - `namespace` (query): 命名空间
  - `docId` (query): 文档ID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

### 删除命名空间

- **URL**: `/api/admin/llm/vector/namespace/{namespace}`
- **方法**: DELETE
- **描述**: 删除整个命名空间及其包含的所有文档
- **请求参数**:
  - `namespace` (path): 命名空间

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

## 精确查找数据库管理接口

精确查找数据库用于通过关键词精确匹配医学知识条目，为智能体提供精准答案。

### 获取所有大类

- **URL**: `/api/admin/llm/precise/categories`
- **方法**: GET
- **描述**: 获取所有精确查找大类
- **请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "头痛类",
      "uid": "cat_12345678"
    },
    {
      "id": 2,
      "name": "皮肤病类",
      "uid": "cat_87654321"
    }
  ]
}
```

### 创建大类

- **URL**: `/api/admin/llm/precise/category`
- **方法**: POST
- **描述**: 创建新的精确查找大类
- **请求格式**: JSON

**请求示例**:

```json
{
  "name": "骨科疾病"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 3,
    "name": "骨科疾病",
    "uid": "cat_34567890"
  }
}
```

### 更新大类

- **URL**: `/api/admin/llm/precise/category/{categoryUid}`
- **方法**: PUT
- **描述**: 更新指定大类的信息
- **请求参数**:
  - `categoryUid` (path): 大类UID
- **请求格式**: JSON

**请求示例**:

```json
{
  "name": "骨科疾病与治疗"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 3,
    "name": "骨科疾病与治疗",
    "uid": "cat_34567890"
  }
}
```

### 删除大类

- **URL**: `/api/admin/llm/precise/category/{categoryUid}`
- **方法**: DELETE
- **描述**: 删除指定的大类及其包含的所有条目
- **请求参数**:
  - `categoryUid` (path): 大类UID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

### 获取大类下的条目列表

- **URL**: `/api/admin/llm/precise/entries/{categoryUid}`
- **方法**: GET
- **描述**: 获取指定大类下的所有条目
- **请求参数**:
  - `categoryUid` (path): 大类UID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "category": {
      "id": 1,
      "name": "头痛类",
      "uid": "cat_12345678"
    },
    "entries": [
      {
        "id": 1,
        "uid": "ent_11111111",
        "description": "偏头痛",
        "content": "偏头痛是一种常见的头痛类型...",
        "weight": 85,
        "is_enabled": true,
        "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐"]
      },
      {
        "id": 2,
        "uid": "ent_22222222",
        "description": "紧张性头痛",
        "content": "紧张性头痛是最常见的头痛类型...",
        "weight": 80,
        "is_enabled": true,
        "keywords": ["紧张性头痛", "压力性头痛", "肌肉紧张", "压迫感"]
      }
    ]
  }
}
```

### 创建条目

- **URL**: `/api/admin/llm/precise/entry/{categoryUid}`
- **方法**: POST
- **描述**: 在指定大类下创建新条目
- **请求参数**:
  - `categoryUid` (path): 大类UID
- **请求格式**: JSON

**请求示例**:

```json
{
  "description": "丛集性头痛",
  "content": "丛集性头痛是一种罕见但严重的头痛类型...",
  "keywords": ["丛集性头痛", "眼周疼痛", "剧烈疼痛", "单侧头痛"],
  "weight": 90,
  "isEnabled": true
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 3,
    "uid": "ent_33333333",
    "description": "丛集性头痛",
    "content": "丛集性头痛是一种罕见但严重的头痛类型...",
    "weight": 90,
    "is_enabled": true,
    "category_uid": "cat_12345678",
    "keywords": ["丛集性头痛", "眼周疼痛", "剧烈疼痛", "单侧头痛"]
  }
}
```

### 更新条目

- **URL**: `/api/admin/llm/precise/entry/{entryUid}`
- **方法**: PUT
- **描述**: 更新指定条目
- **请求参数**:
  - `entryUid` (path): 条目UID
- **请求格式**: JSON

**请求示例**:

```json
{
  "description": "严重丛集性头痛",
  "content": "丛集性头痛是一种罕见但非常严重的头痛类型...",
  "keywords": ["丛集性头痛", "眼周疼痛", "剧烈疼痛", "单侧头痛", "自杀性头痛"],
  "weight": 95,
  "isEnabled": true
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 3,
    "uid": "ent_33333333",
    "description": "严重丛集性头痛",
    "content": "丛集性头痛是一种罕见但非常严重的头痛类型...",
    "weight": 95,
    "is_enabled": true,
    "category_uid": "cat_12345678",
    "keywords": ["丛集性头痛", "眼周疼痛", "剧烈疼痛", "单侧头痛", "自杀性头痛"]
  }
}
```

### 删除条目

- **URL**: `/api/admin/llm/precise/entry/{entryUid}`
- **方法**: DELETE
- **描述**: 删除指定条目
- **请求参数**:
  - `entryUid` (path): 条目UID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

## 提示词模板管理接口

提示词模板定义了与LLM对话的结构，指导LLM生成符合要求的回答。

### 获取所有模板ID

- **URL**: `/api/admin/llm/templates`
- **方法**: GET
- **描述**: 获取所有可用的提示词模板ID
- **请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": ["medical_template", "disease_template", "drug_template"]
}
```

### 获取模板详情

- **URL**: `/api/admin/llm/templates/{templateId}`
- **方法**: GET
- **描述**: 获取指定模板的详细信息
- **请求参数**:
  - `templateId` (path): 模板ID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "description": "医疗问诊大模板",
    "sub_templates": {
      "基础信息": {
        "template": "你是一个专业的线上医生，请基于医学知识与提供的参考内容回答患者问题。\n患者信息：年龄{patient_age}，性别{patient_gender}，症状{symptoms}。\n问题：{question}",
        "description": "医疗问答基础模板",
        "parameters": ["patient_age", "patient_gender", "symptoms", "question"]
      },
      "参考内容": {
        "template": "## 参考医学知识\n\n{vector_content}\n\n## 精确匹配内容\n\n{precise_content}",
        "description": "参考内容模板",
        "parameters": ["vector_content", "precise_content"]
      },
      "回答格式": {
        "template": "## 回答要求\n\n1. 请直接回答患者问题，不要重复问题\n2. 使用通俗易懂的语言\n3. 避免使用太多专业术语\n4. 如果涉及用药，请说明用法用量和注意事项\n5. 如果情况严重，建议患者及时就医",
        "description": "回答格式模板",
        "parameters": []
      }
    }
  }
}
```

### 创建或更新模板

- **URL**: `/api/admin/llm/templates`
- **方法**: POST
- **描述**: 创建新模板或更新现有模板
- **请求格式**: JSON

**请求示例**:

```json
{
  "templateId": "drug_template",
  "description": "药物咨询模板",
  "subTemplates": {
    "基础信息": {
      "template": "你是一个专业的药剂师，请基于医学知识与提供的参考内容回答患者对药物的咨询。\n患者信息：年龄{patient_age}，性别{patient_gender}。\n问题：{question}",
      "description": "药物咨询基础模板",
      "parameters": ["patient_age", "patient_gender", "question"]
    },
    "参考内容": {
      "template": "## 药物知识库参考\n\n{vector_content}\n\n## 精确匹配内容\n\n{precise_content}",
      "description": "药物参考内容模板",
      "parameters": ["vector_content", "precise_content"]
    },
    "回答格式": {
      "template": "## 回答要求\n\n1. 请详细说明药物的用法用量\n2. 明确列出药物的禁忌症和不良反应\n3. 解释药物的作用机制\n4. 提醒患者用药注意事项",
      "description": "药物咨询回答格式",
      "parameters": []
    }
  }
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

### 删除模板

- **URL**: `/api/admin/llm/templates/{templateId}`
- **方法**: DELETE
- **描述**: 删除指定模板
- **请求参数**:
  - `templateId` (path): 模板ID

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": true
}
```

## 与智能体的集成

LLM后端管理接口管理的数据直接影响智能体的行为：

1. **向量数据库**: 智能体可以从中检索相关文档，获取背景知识
2. **精确查找数据库**: 智能体可以从中获取精确的问题答案
3. **提示词模板**: 定义了智能体的角色、语气和回答格式

在创建智能体时，管理员可以为智能体指定使用的向量数据库命名空间、精确查找大类和提示词模板，实现智能体行为的精确控制。

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
2. 仅允许管理员访问这些接口
3. 对敏感操作实施IP限制和操作日志记录
4. 定期备份数据库内容
5. 文件上传时进行严格的类型检查和大小限制 