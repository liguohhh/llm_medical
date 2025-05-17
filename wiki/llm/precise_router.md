# 精确查询API文档

## 概述

精确查询API提供了一套完整的接口，用于管理和查询精确匹配的医学知识库。该系统基于关键词匹配和分词技术，能够精确定位医学知识条目，并根据权重提供最相关的结果。

## 基础路径

所有API端点都以 `/api/v1/precise` 为前缀。

## 数据模型

### 大类 (Category)

大类是知识条目的顶层分类，例如"头痛类"、"皮肤病类"等。

```json
{
  "id": 1,
  "name": "头痛类",
  "uid": "cat_12345678"
}
```

### 条目 (Entry)

条目是具体的知识点，包含描述、内容、关键词列表和权重等信息。

```json
{
  "id": 1,
  "uid": "ent_87654321",
  "description": "偏头痛",
  "content": "偏头痛是一种常见的头痛类型，特点是反复发作的中度至重度疼痛...",
  "weight": 85,
  "is_enabled": true,
  "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐"]
}
```

## API端点

### 1. 创建大类

**请求**

```
POST /api/v1/precise/category
```

**请求体**

```json
{
  "name": "头痛类"
}
```

**响应**

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

### 2. 创建条目

**请求**

```
POST /api/v1/precise/entry/{category_uid}
```

**请求体**

```json
{
  "description": "偏头痛",
  "content": "偏头痛是一种常见的头痛类型，特点是反复发作的中度至重度疼痛...",
  "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐"],
  "weight": 85,
  "is_enabled": true
}
```

**响应**

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

### 3. 删除大类

**请求**

```
DELETE /api/v1/precise/category/{category_uid}
```

**响应**

```json
{
  "success": true,
  "message": "删除大类成功: cat_12345678"
}
```

### 4. 更新大类名称

**请求**

```
PUT /api/v1/precise/category/{category_uid}
```

**请求体**

```json
{
  "name": "头痛与神经类"
}
```

**响应**

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

### 5. 更新条目

**请求**

```
PUT /api/v1/precise/entry/{entry_uid}
```

**请求体**

```json
{
  "description": "严重偏头痛",
  "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐", "光敏感"],
  "weight": 90
}
```

**响应**

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

### 6. 列出所有大类

**请求**

```
GET /api/v1/precise/categories
```

**响应**

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

### 7. 列出指定大类下的所有条目

**请求**

```
GET /api/v1/precise/entries/{category_uid}
```

**响应**

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
      },
      {
        "id": 3,
        "uid": "ent_33333333",
        "description": "丛集性头痛",
        "content": "丛集性头痛是一种罕见但严重的头痛类型...",
        "weight": 90,
        "is_enabled": true,
        "keywords": ["丛集性头痛", "眼周疼痛", "剧烈疼痛", "单侧头痛"]
      }
    ]
  }
}
```

## 与聊天API集成

精确查询可以通过Chat API的`precise_search_config`参数进行集成，允许用户在聊天请求中指定精确查询的配置。

### 聊天请求示例

```json
{
  "message": "我最近经常头疼恶心，可能是什么原因？",
  "model_settings": {
    "model_name": "gpt-3.5-turbo",
    "api_key": "your-api-key",
    "base_url": "https://api.openai.com/v1"
  },
  "template_config": {
    "template_id": "medical_template",
    "sub_template_ids": ["基础信息", "参考内容"],
    "params": {
      "patient_age": "30",
      "patient_gender": "男",
      "symptoms": "头痛"
    }
  },
  "vector_search_config": {
    "namespaces": ["medical_knowledge"],
    "n_results": 3
  },
  "precise_search_config": {
    "categories": ["cat_12345678"],
    "max_results": 3,
    "search_depth": 2
  }
}
```

### 精确查询配置参数

精确查询配置 `precise_search_config` 包含以下参数：

- **categories**: 要搜索的大类UID列表，为空则搜索所有大类
- **max_results**: 最大返回结果数 (默认值: 5)
- **search_depth**: 搜索深度 (默认值: 2)
  - 1: 只进行关键词匹配
  - 2: 进行二次关联匹配，提高召回率

## 搜索机制

精确查询使用以下步骤进行搜索：

1. 使用jieba分词对用户查询进行分词处理
2. 在精确查询库中查找包含这些词语的条目
3. 如果search_depth > 1，则从已匹配条目中提取新的关键词进行二次查询
4. 根据条目权重排序结果
5. 返回排名靠前的结果(数量由max_results限制)

## 错误代码

- **400 Bad Request**: 请求参数无效
- **404 Not Found**: 资源未找到，例如大类或条目不存在
- **500 Internal Server Error**: 服务器内部错误 