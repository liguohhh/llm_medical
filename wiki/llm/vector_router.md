# 向量数据库管理接口文档 (vector_router)

## 1. 接口概览
- 路由文件: routers/vector_router.py
- 功能模块: 提供向量数据库的文档管理和查询功能
- 基础路径: /api/v1

## 2. 接口详情

### 2.1 上传文档
- 路径: /vectors/upload
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

### 2.2 删除文档
- 路径: /vectors/document
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

### 2.3 删除命名空间
- 路径: /vectors/namespace/{namespace}
- 方法: DELETE
- 描述: 删除整个命名空间

请求参数:
- `namespace`: (Path) 要删除的命名空间

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

### 2.4 获取命名空间列表
- 路径: /vectors/namespaces
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

### 2.5 获取文档列表
- 路径: /vectors/documents/{namespace}
- 方法: GET
- 描述: 列出指定命名空间中的所有文档

请求参数:
- `namespace`: (Path) 命名空间

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

## 3. 与聊天接口集成

聊天接口现在支持指定向量搜索配置：

```json
{
    "message": "头痛可能是什么原因？",
    "model_settings": {
        "model_name": "gpt-3.5-turbo",
        "api_key": "your-api-key",
        "base_url": "https://api.openai.com/v1"
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
        "n_results": 3,
        "metadata_filter": {
            "source": "headache_info.pdf"
        }
    }
}
```

其中，`vector_search_config`字段是可选的，包含以下属性：
- `namespaces`: 要搜索的命名空间列表，默认搜索所有命名空间
- `n_results`: 每个命名空间返回的最大结果数，默认5
- `metadata_filter`: 元数据过滤条件，可以指定文档源等

## 4. 支持的文件类型
目前支持上传以下类型的文件：
- 文本文件 (.txt)
- Word文档 (.docx)
- PDF文档 (.pdf)

## 5. 错误处理
所有接口在发生错误时都会返回以下格式的错误响应：
```json
{
    "detail": "错误消息"
}
```

常见错误码:
- 400: 请求参数错误（如文件类型不支持等）
- 404: 资源不存在（如命名空间或文档不存在）
- 500: 服务器内部错误

## 6. 使用示例

### 6.1 上传文档
```python
import requests

url = "http://localhost:8000/api/v1/vectors/upload"
files = {"file": ("medical_info.txt", open("medical_info.txt", "rb"))}
data = {"namespace": "medical", "chunk_size": 500, "chunk_overlap": 50}

response = requests.post(url, files=files, data=data)
print(response.json())
```

### 6.2 使用向量搜索进行聊天
```python
import requests

url = "http://localhost:8000/api/v1/ask"
data = {
    "message": "头痛可能是什么原因？",
    "model_settings": {
        "model_name": "gpt-3.5-turbo",
        "api_key": "your-api-key",
        "base_url": "https://api.openai.com/v1"
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
        "namespaces": ["medical"],
        "n_results": 3
    }
}

response = requests.post(url, json=data)
print(response.json())
``` 