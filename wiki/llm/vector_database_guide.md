# 向量数据库使用指南

## 基本概念

### 命名空间 (Namespace)
在Chroma向量数据库中，**命名空间**是数据的逻辑分区单元，相当于传统数据库中的"表"概念。

- **定义**: 命名空间是包含多个文档及其向量嵌入的集合
- **用途**: 用于组织和隔离不同主题、领域或业务场景的数据
- **特点**: 支持独立查询、管理和权限控制

在Chroma中，命名空间通过`collection`实现，每个collection代表一个独立命名空间。

```python
# 创建或获取命名空间示例
collection = self.client.get_or_create_collection(namespace)
```

### 文档ID (Document ID)
**文档ID**是向量数据库中每个文档的唯一标识符。

- **定义**: 在特定命名空间内唯一标识一个文档
- **生成方式**: 可由系统自动生成(UUID)或用户指定
- **用途**: 用于文档检索、更新和删除操作

在实现中，文档ID通常存储在元数据中：

```python
metadata = {
    "doc_id": doc_id,
    "source": file_name,
    # 其他元数据...
}
```

### 查询方式

向量数据库支持两种主要查询方式：

1. **语义相似度查询**:
   - 将查询文本转换为向量嵌入
   - 查找与查询向量最相似的文档片段
   - 基于余弦相似度或欧几里得距离计算

   ```python
   results = collection.query(
       query_texts=[query],
       n_results=n_results,
       where=metadata_filter  # 可选的元数据过滤条件
   )
   ```

2. **元数据过滤查询**:
   - 基于文档的元数据字段进行过滤
   - 可结合语义查询使用，实现更精准的检索

   ```python
   # 元数据过滤示例
   metadata_filter = {"doc_id": "specific_doc_id"}
   ```

## Chroma实现机制

### 数据存储结构

Chroma将数据组织为以下结构:

```
Chroma数据库
├── 命名空间A (Collection A)
│   ├── 文档1
│   │   ├── 文本块1 + 向量嵌入 + 元数据
│   │   ├── 文本块2 + 向量嵌入 + 元数据
│   │   └── ...
│   ├── 文档2
│   │   ├── 文本块1 + 向量嵌入 + 元数据
│   │   └── ...
│   └── ...
├── 命名空间B (Collection B)
└── ...
```

每个Collection内部包含:
- 文本内容 (`documents`) 
- 向量嵌入 (`embeddings`)
- 元数据 (`metadatas`)
- ID (`ids`)

### 数据添加过程

1. **文档分块**: 将大文档拆分为较小的文本块
   ```python
   chunks = self._split_text(text, chunk_size, chunk_overlap)
   ```

2. **生成向量嵌入**: 使用嵌入模型将每个文本块转换为向量
   ```python
   embedder = SentenceTransformerEmbedder("all-MiniLM-L6-v2")
   ```

3. **添加到数据库**: 将文本块、向量嵌入和元数据添加到集合中
   ```python
   collection.add(
       documents=chunks,
       embeddings=embeddings,
       metadatas=metadatas,
       ids=ids
   )
   ```

### 查询检索过程

1. **查询向量化**: 将用户查询转换为向量嵌入
2. **相似度搜索**: 在指定命名空间中找出最相似的文本块
3. **结果合并与排序**: 根据相似度分数合并并排序结果
4. **内容返回**: 返回最相关的文本段落供LLM参考

## 具体使用方案

### 1. 设计命名空间策略

根据业务需求划分命名空间:

- **基于主题**: 医疗、法律、财务等不同领域分别使用独立命名空间
- **基于用户**: 为不同用户或组织创建专属命名空间
- **基于数据源**: 根据数据来源(如教科书、论文、网页)分隔

推荐命名规则:
- 使用小写字母和下划线
- 添加前缀标识数据类型(如`medical_`、`legal_`)
- 考虑版本号(如`medical_v1`)

### 2. 文档上传与管理

**上传流程**:
1. 选择或创建目标命名空间
2. 设置适当的分块参数(大小、重叠)
3. 上传文件，生成唯一文档ID
4. 添加必要的元数据(来源、日期、作者等)

**元数据设计**:
```json
{
  "doc_id": "unique_id",
  "source": "filename.pdf",
  "title": "文档标题",
  "author": "作者",
  "date": "2023-06-15",
  "category": "内科",
  "tags": ["头痛", "偏头痛", "诊断"]
}
```

### 3. 查询优化策略

**基础查询**:
```python
results = collection.query(
    query_texts=["头痛的症状和治疗方法"],
    n_results=5
)
```

**高级查询**:
```python
# 结合元数据过滤的查询
results = collection.query(
    query_texts=["头痛的症状和治疗方法"],
    n_results=5,
    where={"category": "内科", "tags": "偏头痛"}
)
```

**多命名空间查询**:
1. 并行查询多个命名空间
2. 合并结果并根据相似度重新排序
3. 返回最相关的结果

### 4. 与LLM集成方式

**RAG模式集成**:
1. 接收用户查询
2. 从向量数据库检索相关内容
3. 将检索内容注入到提示模板中
4. 调用LLM生成回答

```python
# 查询向量数据库
vector_content = await rag_service.get_rag_content(
    query=user_message,
    namespaces=["medical_knowledge"],
    n_results=3
)

# 构建提示
prompt = f"""
你是医疗顾问。基于以下信息回答问题:
{vector_content}

问题: {user_message}
"""

# 调用LLM
response = await llm_service.call_model(prompt)
```

### 5. 性能和扩展性考虑

**性能优化**:
- 使用适当的向量维度(经验值384-768)
- 合理设置文本块大小(200-1000字符)
- 为频繁查询构建缓存层

**扩展方案**:
- 单机部署: 适用于小型数据集(<100万向量)
- 分布式部署: 用于大规模数据集
- 考虑使用PGVector或Qdrant实现高可用性

## 最佳实践

1. **定期维护和更新**:
   - 设置文档过期策略
   - 定期重新嵌入以利用新模型

2. **监控与日志**:
   - 记录查询延迟和命中率
   - 跟踪命名空间大小和增长率

3. **安全性考虑**:
   - 实施命名空间级别的访问控制
   - 敏感数据加密存储

4. **测试与评估**:
   - 建立标准测试集评估检索质量
   - 使用A/B测试比较不同配置

## 错误处理和故障排查

常见问题及解决方案:

1. **查询无结果**:
   - 检查命名空间是否存在
   - 验证查询向量化是否正常
   - 尝试增加返回结果数量

2. **相关性低**:
   - 优化文本分块策略
   - 考虑使用更先进的嵌入模型
   - 调整相似度阈值

3. **性能下降**:
   - 监控集合大小，过大时考虑分片
   - 检查并优化查询逻辑
   - 考虑添加索引或缓存 