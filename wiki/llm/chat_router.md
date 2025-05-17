# 聊天接口文档 (chat_router)

## 1. 接口概览
- 路由文件: routers/chat_router.py
- 功能模块: 提供标准问答和流式问答接口
- 基础路径: /api/v1

## 2. 接口详情

### 2.1 标准问答接口
- 路径: /ask
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

### 2.2 流式问答接口
- 路径: /stream
- 方法: POST
- 描述: 提供流式问答服务，实时返回模型生成的内容

请求体格式与标准问答接口相同。

响应:
- 流式响应，实时返回模型生成的内容
- 格式: Server-Sent Events (SSE)

## 3. 参数说明

### 3.1 请求参数
- `message`: 用户输入的问题
- `model_settings`: 模型配置信息
  - `model_name`: 模型名称
  - `api_key`: API密钥
  - `base_url`: 模型API地址
- `template_config`: 模板配置
  - `template_id`: 大模板ID，指定要使用的大模板文件
  - `sub_template_ids`: 小模板ID列表，指定要使用大模板中的哪些小模板
  - `params`: 提供给模板的参数，用于填充模板中的变量
- `vector_search_config`: 向量搜索配置（可选）
  - `namespaces`: 要搜索的命名空间列表，默认搜索所有命名空间
  - `n_results`: 每个命名空间返回的最大结果数，默认5
  - `metadata_filter`: 元数据过滤条件，可以指定文档源等
- `precise_search_config`: 精确查询配置（可选）
  - `categories`: 要搜索的大类UID列表，默认搜索所有大类
  - `max_results`: 最大返回结果数，默认5
  - `search_depth`: 搜索深度，1表示只进行关键词匹配，2表示进行二次关联匹配，默认2
- `history`: 对话历史记录，按顺序排列的用户和系统消息列表

### 3.2 大模板结构
每个大模板是一个JSON文件，包含多个小模板，格式如下:
```json
{
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
```

### 3.3 小模板选择
在请求中，你可以通过 `sub_template_ids` 列表指定要使用的小模板，系统会按照列表顺序拼接这些小模板生成最终的提示词。

### 3.4 默认参数
系统会自动为每个模板提供以下默认参数:
- `question`: 用户的问题
- `vector_content`: 向量检索内容
- `precise_content`: 精确匹配内容

这些默认参数可以在任何模板中使用，无需在请求中显式提供。

### 3.5 向量搜索配置
通过提供 `vector_search_config` 参数，可以自定义向量检索行为：

- **自定义搜索范围**：指定只在特定命名空间中搜索，例如只搜索"疾病"或"药物"相关文档
- **控制结果数量**：调整每个命名空间返回的最大结果数，控制参考内容的详细程度
- **元数据过滤**：根据文档元数据（如文档来源）过滤搜索结果
- **历史消息查询**：通过设置 `rag_history_count` 参数，指定多少条历史消息用于查询，默认为1（仅当前消息）

如果不提供此配置，系统将使用默认设置在所有可用命名空间中搜索。

### 3.6 精确查询配置
通过提供 `precise_search_config` 参数，可以自定义精确查询行为：

- **自定义搜索范围**：指定只在特定大类中搜索，例如只搜索"头痛类"或"皮肤病类"
- **控制结果数量**：调整返回的最大结果数，控制参考内容的详细程度
- **搜索深度**：设置搜索深度，1表示只进行关键词匹配，2表示进行二次关联匹配增加召回率
- **历史消息查询**：通过设置 `rag_history_count` 参数，指定多少条历史消息用于查询，默认为1（仅当前消息）

如果不提供此配置，系统将使用默认设置在所有可用大类中搜索，搜索深度为2。

## 4. 错误处理
接口可能返回以下错误码:
- 400: 参数错误
- 404: 模板不存在
- 500: 服务器内部错误

每个错误响应都会包含 `detail` 字段，提供具体的错误信息。

## 5. 使用示例

### 5.1 标准问答示例
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
    }
})
print(response.json())
```

### 5.2 仅使用部分小模板示例
```python
import requests

response = requests.post("http://localhost:8000/api/v1/ask", json={
    "message": "我经常头晕，可能是什么问题？",
    "model_settings": {
        "model_name": "deepseek-chat",
        "api_key": "your-api-key",
        "base_url": "https://api.deepseek.com/v1"
    },
    "template_config": {
        "template_id": "medical_template",
        "sub_template_ids": ["基础信息", "参考内容"],  # 仅使用两个小模板
        "params": {
            "patient_age": "45",
            "patient_gender": "女",
            "symptoms": "头晕"
        }
    }
})
print(response.json())
```

### 5.3 带历史记录的流式问答示例
```javascript
const eventSource = new EventSource("/api/v1/stream", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
        "message": "这种情况需要吃什么药？",
        "model_settings": {
            "model_name": "deepseek-chat",
            "api_key": "your-api-key",
            "base_url": "https://api.deepseek.com/v1"
        },
        "template_config": {
            "template_id": "medical_template",
            "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
            "params": {
                "patient_age": "45",
                "patient_gender": "女",
                "symptoms": "头晕"
            }
        },
        "history": [
            {"role": "user", "content": "我经常头晕，可能是什么问题？"},
            {"role": "assistant", "content": "头晕可能与多种因素有关..."}
        ]
    })
});

eventSource.onmessage = (event) => {
    console.log(event.data);
};
```

### 5.4 使用向量搜索配置示例
```python
import requests

response = requests.post("http://localhost:8000/api/v1/ask", json={
    "message": "请介绍高血压的治疗方法",
    "model_settings": {
        "model_name": "deepseek-chat",
        "api_key": "your-api-key",
        "base_url": "https://api.deepseek.com/v1"
    },
    "template_config": {
        "template_id": "medical_template",
        "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
        "params": {
            "patient_age": "60",
            "patient_gender": "男",
            "symptoms": "高血压"
        }
    },
    "vector_search_config": {
        "namespaces": ["cardiovascular", "treatment"],
        "n_results": 5,
        "metadata_filter": {
            "source": "hypertension_guidelines.pdf"
        }
    }
})
print(response.json())
```

### 5.5 使用精确查询配置示例
```python
import requests

response = requests.post("http://localhost:8000/api/v1/ask", json={
    "message": "我最近经常头痛恶心，可能是什么原因？",
    "model_settings": {
        "model_name": "deepseek-chat",
        "api_key": "your-api-key",
        "base_url": "https://api.deepseek.com/v1"
    },
    "template_config": {
        "template_id": "medical_template",
        "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
        "params": {
            "patient_age": "35",
            "patient_gender": "女",
            "symptoms": "头痛、恶心"
        }
    },
    "precise_search_config": {
        "categories": ["cat_12345678"],  # 头痛类的UID
        "max_results": 3,
        "search_depth": 2
    }
})
print(response.json())
```

### 5.6 综合使用向量搜索和精确查询示例
```python
import requests

response = requests.post("http://localhost:8000/api/v1/ask", json={
    "message": "我近期出现头痛、恶心和畏光症状，这可能是什么问题？",
    "model_settings": {
        "model_name": "deepseek-chat",
        "api_key": "your-api-key",
        "base_url": "https://api.deepseek.com/v1"
    },
    "template_config": {
        "template_id": "medical_template",
        "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
        "params": {
            "patient_age": "28",
            "patient_gender": "女",
            "symptoms": "头痛、恶心、畏光"
        }
    },
    "vector_search_config": {
        "namespaces": ["medical_knowledge", "symptoms"],
        "n_results": 3
    },
    "precise_search_config": {
        "categories": ["cat_12345678", "cat_87654321"],  # 头痛类和神经系统类的UID
        "max_results": 3,
        "search_depth": 2
    }
})
print(response.json())
```

### 5.7 使用历史消息进行RAG查询示例
```python
import requests

response = requests.post("http://localhost:8000/api/v1/ask", json={
    "message": "有什么适合的治疗方法？",
    "model_settings": {
        "model_name": "deepseek-chat",
        "api_key": "your-api-key",
        "base_url": "https://api.deepseek.com/v1"
    },
    "template_config": {
        "template_id": "medical_template",
        "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
        "params": {
            "patient_age": "42",
            "patient_gender": "男",
            "symptoms": "高血压"
        }
    },
    "history": [
        {"role": "user", "content": "我被诊断出患有轻度高血压，应该注意什么？"},
        {"role": "assistant", "content": "高血压是一种常见的心血管疾病..."}
    ],
    "vector_search_config": {
        "namespaces": ["medical_knowledge"],
        "n_results": 3,
        "rag_history_count": 2  # 使用当前消息和1条历史消息进行向量检索
    },
    "precise_search_config": {
        "categories": ["cat_hypertension"],
        "max_results": 3,
        "rag_history_count": 3  # 使用当前消息和2条历史消息进行精确查询
    }
})
print(response.json())
``` 