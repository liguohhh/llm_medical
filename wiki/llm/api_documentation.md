# 医疗问答系统API文档

## 1. 概述

本文档描述了医疗问答系统的所有API接口，包括模板管理、精确查询、向量数据库管理和聊天服务等功能。

## 2. 基础路径

所有API端点都以 `/api/v1` 为前缀。

## 3. 模板管理接口 (template_router)

### 3.1 添加大模板
- 路径: `/templates/{template_id}`
- 方法: POST
- 描述: 添加新的大模板

请求体:
```json
{
    "description": "医疗问诊大模板",
    "sub_templates": {
        "基础信息": {
            "template": "这是基础信息模板，包含参数 {patient_age} 和 {patient_gender}",
            "description": "患者基础信息模板",
            "parameters": ["patient_age", "patient_gender", "symptoms", "question"]
        },
        "参考内容": {
            "template": "这是参考内容模板，包含参数 {vector_content} 和 {precise_content}",
            "description": "参考内容模板",
            "parameters": ["vector_content", "precise_content"]
        }
    }
}
```

响应体:
```json
{
    "status": "success",
    "message": "Template added successfully",
    "data": {
        "template_id": "medical_template"
    }
}
```

### 3.2 更新大模板
- 路径: `/templates/{template_id}`
- 方法: PUT
- 描述: 更新现有大模板

请求体:
```json
{
    "description": "更新后的医疗问诊大模板",
    "sub_templates": {
        "基础信息": {
            "template": "更新后的基础信息模板",
            "description": "更新后的描述",
            "parameters": ["patient_age", "patient_gender", "symptoms", "question"]
        },
        "参考内容": {
            "template": "更新后的参考内容模板",
            "description": "更新后的描述",
            "parameters": ["vector_content", "precise_content"]
        }
    }
}
```

响应体:
```json
{
    "status": "success",
    "message": "Template updated successfully",
    "data": {
        "template_id": "medical_template"
    }
}
```

### 3.3 删除大模板
- 路径: `/templates/{template_id}`
- 方法: DELETE
- 描述: 删除指定大模板

响应体:
```json
{
    "status": "success",
    "message": "Template deleted successfully"
}
```

### 3.4 获取大模板
- 路径: `/templates/{template_id}`
- 方法: GET
- 描述: 获取指定大模板详情

响应体:
```json
{
    "status": "success",
    "message": "Template retrieved successfully",
    "data": {
        "description": "医疗问诊大模板",
        "sub_templates": {
            "基础信息": {
                "template": "这是基础信息模板，包含参数 {patient_age} 和 {patient_gender}",
                "description": "患者基础信息模板",
                "parameters": ["patient_age", "patient_gender", "symptoms", "question"]
            },
            "参考内容": {
                "template": "这是参考内容模板，包含参数 {vector_content} 和 {precise_content}",
                "description": "参考内容模板",
                "parameters": ["vector_content", "precise_content"]
            }
        }
    }
}
```

### 3.5 获取大模板列表
- 路径: `/templates`
- 方法: GET
- 描述: 获取所有大模板ID列表

响应体:
```json
{
    "status": "success",
    "message": "Templates retrieved successfully",
    "data": {
        "templates": ["medical_template", "disease_template", "drug_template"]
    }
}
```

## 4. 精确查询接口 (precise_router)

### 4.1 创建大类
- 路径: `/precise/category`
- 方法: POST
- 描述: 创建新的知识大类

请求体:
```json
{
    "name": "头痛类"
}
```

响应体:
```json
{
    "success": true,
    "message": "创建大类成功: 头痛类",
    "data": {
        "id": 1,
        "name": "头痛类",
        "uid": "cat_12345678"
    }
}
```

### 4.2 创建条目
- 路径: `/precise/entry/{category_uid}`
- 方法: POST
- 描述: 在指定大类下创建新条目

请求体:
```json
{
    "description": "偏头痛",
    "content": "偏头痛是一种常见的头痛类型，特点是反复发作的中度至重度疼痛...",
    "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐"],
    "weight": 85,
    "is_enabled": true
}
```

响应体:
```json
{
    "success": true,
    "message": "创建条目成功: 偏头痛",
    "data": {
        "id": 1,
        "uid": "ent_87654321",
        "description": "偏头痛",
        "content": "偏头痛是一种常见的头痛类型，特点是反复发作的中度至重度疼痛...",
        "weight": 85,
        "is_enabled": true,
        "category_uid": "cat_12345678",
        "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐"]
    }
}
```

### 4.3 删除大类
- 路径: `/precise/category/{category_uid}`
- 方法: DELETE
- 描述: 删除指定大类及其所有条目

响应体:
```json
{
    "success": true,
    "message": "删除大类成功: cat_12345678"
}
```

### 4.4 更新大类名称
- 路径: `/precise/category/{category_uid}`
- 方法: PUT
- 描述: 更新大类名称

请求体:
```json
{
    "name": "头痛与神经类"
}
```

响应体:
```json
{
    "success": true,
    "message": "更新大类名称成功: 头痛与神经类",
    "data": {
        "id": 1,
        "name": "头痛与神经类",
        "uid": "cat_12345678"
    }
}
```

### 4.5 更新条目
- 路径: `/precise/entry/{entry_uid}`
- 方法: PUT
- 描述: 更新条目信息

请求体:
```json
{
    "description": "严重偏头痛",
    "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐", "光敏感"],
    "weight": 90
}
```

响应体:
```json
{
    "success": true,
    "message": "更新条目成功: 严重偏头痛",
    "data": {
        "id": 1,
        "uid": "ent_87654321",
        "description": "严重偏头痛",
        "content": "偏头痛是一种常见的头痛类型，特点是反复发作的中度至重度疼痛...",
        "weight": 90,
        "is_enabled": true,
        "category_uid": "cat_12345678",
        "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐", "光敏感"]
    }
}
```

### 4.6 列出所有大类
- 路径: `/precise/categories`
- 方法: GET
- 描述: 获取所有大类列表

响应体:
```json
{
    "success": true,
    "message": "获取大类列表成功，共 2 个大类",
    "data": {
        "categories": [
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
}
```

### 4.7 列出指定大类下的所有条目
- 路径: `/precise/entries/{category_uid}`
- 方法: GET
- 描述: 获取指定大类下的所有条目

响应体:
```json
{
    "success": true,
    "message": "获取条目列表成功，共 3 个条目",
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

## 5. 向量数据库管理接口 (vector_router)

### 5.1 上传文档
- 路径: `/vectors/upload`
- 方法: POST
- 描述: 上传文档到向量数据库

请求参数:
- `file`: (FormData) 文件数据，支持txt、docx、pdf格式
- `namespace`: (FormData) 文档存储的命名空间
- `chunk_size`: (FormData) 文档分块大小，默认500
- `chunk_overlap`: (FormData) 分块重叠大小，默认50

响应体:
```json
{
    "status": "success",
    "message": "文档 example.txt 成功上传到命名空间 medical",
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

### 5.2 删除文档
- 路径: `/vectors/document`
- 方法: DELETE
- 描述: 删除指定文档

请求参数:
- `namespace`: (Query) 命名空间
- `doc_id`: (Query) 文档ID

响应体:
```json
{
    "status": "success",
    "message": "已删除命名空间 medical 中文档 550e8400-e29b-41d4-a716-446655440000 的所有内容",
    "data": {
        "namespace": "medical",
        "doc_id": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

### 5.3 删除命名空间
- 路径: `/vectors/namespace/{namespace}`
- 方法: DELETE
- 描述: 删除整个命名空间

响应体:
```json
{
    "status": "success",
    "message": "已删除命名空间 medical",
    "data": {
        "namespace": "medical"
    }
}
```

### 5.4 获取命名空间列表
- 路径: `/vectors/namespaces`
- 方法: GET
- 描述: 列出所有命名空间

响应体:
```json
{
    "status": "success",
    "message": "成功获取命名空间列表",
    "data": {
        "namespaces": ["medical", "disease", "drugs"]
    }
}
```

### 5.5 获取文档列表
- 路径: `/vectors/documents/{namespace}`
- 方法: GET
- 描述: 列出指定命名空间中的所有文档

响应体:
```json
{
    "status": "success",
    "message": "成功获取命名空间 medical 的文档列表",
    "data": {
        "namespace": "medical",
        "documents": [
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
}
```

## 6. 聊天接口 (chat_router)

### 6.1 标准问答接口
- 路径: `/ask`
- 方法: POST
- 描述: 提供标准的问答服务，返回完整的回答

请求体:
```json
{
    "message": "用户的问题",
    "model_settings": {
        "model_name": "模型名称",
        "api_key": "API密钥",
        "base_url": "模型API地址"
    },
    "template_config": {
        "template_id": "medical_template",
        "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
        "params": {
            "patient_age": "25",
            "patient_gender": "男",
            "symptoms": "头痛、发热"
        }
    },
    "vector_search_config": {
        "namespaces": ["medical", "disease"],
        "n_results": 3,
        "metadata_filter": {
            "source": "symptoms_info.pdf"
        }
    },
    "precise_search_config": {
        "categories": ["headache", "fever"],
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

响应体:
```json
{
    "answer": "模型的回答",
    "vector_content": "向量检索的内容",
    "precise_content": "精确匹配的内容"
}
```

### 6.2 流式问答接口
- 路径: `/stream`
- 方法: POST
- 描述: 提供流式问答服务，实时返回模型生成的内容

请求体格式与标准问答接口相同。

响应:
- 流式响应，实时返回模型生成的内容
- 格式: Server-Sent Events (SSE)

## 7. 错误处理

所有接口在发生错误时都会返回以下格式的错误响应：
```json
{
    "detail": "错误信息"
}
```

常见错误码:
- 400: 请求参数错误
- 404: 资源不存在
- 500: 服务器内部错误

## 8. 使用示例

### 8.1 创建医疗问诊大模板
```python
import requests

template_data = {
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
        }
    }
}

response = requests.post(
    "http://localhost:8000/api/v1/templates/medical_template",
    json=template_data
)
print(response.json())
```

### 8.2 上传文档到向量数据库
```python
import requests

url = "http://localhost:8000/api/v1/vectors/upload"
files = {"file": ("medical_info.txt", open("medical_info.txt", "rb"))}
data = {"namespace": "medical", "chunk_size": 500, "chunk_overlap": 50}

response = requests.post(url, files=files, data=data)
print(response.json())
```

### 8.3 使用聊天接口进行问答
```python
import requests

response = requests.post("http://localhost:8000/api/v1/ask", json={
    "message": "我最近头痛得厉害，该怎么办？",
    "model_settings": {
        "model_name": "deepseek-chat",
        "api_key": "your-api-key",
        "base_url": "https://api.deepseek.com/v1"
    },
    "template_config": {
        "template_id": "medical_template",
        "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
        "params": {
            "patient_age": "30",
            "patient_gender": "男",
            "symptoms": "头痛"
        }
    },
    "vector_search_config": {
        "namespaces": ["medical", "disease"],
        "n_results": 3
    },
    "precise_search_config": {
        "categories": ["cat_12345678"],
        "max_results": 3,
        "search_depth": 2
    }
})
print(response.json())
```

## 9. 注意事项

1. 所有接口都需要在请求头中包含正确的认证信息
2. 文件上传接口支持的文件类型包括：txt、docx、pdf
3. 聊天接口支持配置向量搜索和精确查询，可以根据需要选择使用
4. 模板系统支持自定义参数，可以根据实际需求扩展
5. 精确查询支持多级关联匹配，可以通过search_depth参数控制匹配深度 