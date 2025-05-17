# 模板管理接口文档 (template_router)

## 1. 接口概览
- 路由文件: routers/prompt_router.py
- 功能模块: 提供大模板的增删改查功能
- 基础路径: /api/v1

## 2. 接口详情

### 2.1 添加大模板
- 路径: /templates/{template_id}
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
        },
        "回答格式": {
            "template": "这是回答格式模板，定义了响应的格式要求",
            "description": "回答格式模板",
            "parameters": []
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

### 2.2 更新大模板
- 路径: /templates/{template_id}
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
        },
        "新增小模板": {
            "template": "这是一个新增的小模板",
            "description": "新增小模板描述",
            "parameters": ["new_param"]
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

### 2.3 删除大模板
- 路径: /templates/{template_id}
- 方法: DELETE
- 描述: 删除指定大模板

响应体:
```json
{
    "status": "success",
    "message": "Template deleted successfully"
}
```

### 2.4 获取大模板
- 路径: /templates/{template_id}
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
            },
            "回答格式": {
                "template": "这是回答格式模板，定义了响应的格式要求",
                "description": "回答格式模板",
                "parameters": []
            }
        }
    }
}
```

### 2.5 获取大模板列表
- 路径: /templates
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

## 3. 大模板结构说明

### 3.1 大模板结构
每个大模板是一个JSON文件，包含多个小模板，格式如下:
```json
{
    "description": "大模板描述",
    "sub_templates": {
        "小模板1ID": {
            "template": "小模板1内容，含占位符 {param1}",
            "description": "小模板1描述",
            "parameters": ["param1", "param2"]
        },
        "小模板2ID": {
            "template": "小模板2内容，含占位符 {param3}",
            "description": "小模板2描述",
            "parameters": ["param3"]
        }
    }
}
```

### 3.2 字段说明
- `description`: 大模板的整体描述
- `sub_templates`: 包含多个小模板的对象
  - 键: 小模板ID，用于在聊天请求中指定要使用的小模板
  - 值: 小模板对象，包含以下字段
    - `template`: 模板文本，可包含占位符
    - `description`: 小模板描述
    - `parameters`: 该小模板所需参数列表

## 4. 使用示例

### 4.1 创建医疗问诊大模板
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
        },
        "回答格式": {
            "template": "## 回答要求\n\n1. 请直接回答患者问题，不要重复问题\n2. 使用通俗易懂的语言\n3. 避免使用太多专业术语\n4. 如果涉及用药，请说明用法用量和注意事项\n5. 如果情况严重，建议患者及时就医",
            "description": "回答格式模板",
            "parameters": []
        }
    }
}

response = requests.post(
    "http://localhost:8000/api/v1/templates/medical_template",
    json=template_data
)
print(response.json())
```

### 4.2 获取所有大模板
```python
import requests

response = requests.get("http://localhost:8000/api/v1/templates")
templates = response.json()["data"]["templates"]
print(templates)
```

## 5. 错误处理
所有接口在发生错误时都会返回以下格式的错误响应：
```json
{
    "detail": "错误信息"
}
```

常见错误码:
- 400: 请求参数错误（如模板ID已存在）
- 404: 模板不存在
- 500: 服务器内部错误 