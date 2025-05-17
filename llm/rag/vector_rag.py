from typing import List, Dict, Any, Optional, Union, Tuple
import chromadb
from chromadb.utils import embedding_functions
from sentence_transformers import SentenceTransformer
from pathlib import Path
import os
import uuid
import asyncio
import logging
import traceback

# 配置日志
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler()
    ]
)
logger = logging.getLogger("vector_rag")

class SentenceTransformerEmbedder:
    """自定义SentenceTransformer嵌入函数，实现chromadb所需接口"""
    def __init__(self, model_name="all-MiniLM-L6-v2"):
        try:
            logger.info(f"开始加载SentenceTransformer模型: {model_name}")
            self.model = SentenceTransformer(model_name)
            logger.info(f"SentenceTransformer模型加载成功，维度: {self.model.get_sentence_embedding_dimension()}")
        except Exception as e:
            logger.error(f"加载SentenceTransformer模型失败: {str(e)}")
            logger.error(traceback.format_exc())
            raise

    def __call__(self, input: List[str]) -> List[List[float]]:
        """
        ChromaDB要求的嵌入函数接口，参数名必须为input
        
        Args:
            input: 要嵌入的文本列表
            
        Returns:
            List[List[float]]: 嵌入向量列表
        """
        try:
            if not input:
                logger.warning("没有提供文本进行嵌入")
                return []
            
            # 检查文本内容
            for i, text in enumerate(input[:3]):  # 只记录前三个文本
                logger.debug(f"待嵌入文本 #{i+1} 示例: '{text[:50]}...' (长度: {len(text)})")
            
            logger.debug(f"开始嵌入 {len(input)} 个文本")
            
            # 检查文本是否为空
            non_empty_texts = []
            empty_indices = []
            for i, text in enumerate(input):
                if text.strip():
                    non_empty_texts.append(text)
                else:
                    empty_indices.append(i)
                    logger.warning(f"文本 #{i+1} 是空的，将使用替代文本")
                    non_empty_texts.append("空文本")
            
            # 批量处理，避免内存溢出
            batch_size = 32
            all_embeddings = []
            
            for i in range(0, len(non_empty_texts), batch_size):
                batch = non_empty_texts[i:i + batch_size]
                logger.debug(f"处理批次 {i//batch_size + 1}/{(len(non_empty_texts) + batch_size - 1)//batch_size}，批次大小: {len(batch)}")
                
                try:
                    batch_embeddings = self.model.encode(batch, convert_to_numpy=True).tolist()
                    all_embeddings.extend(batch_embeddings)
                    logger.debug(f"批次 {i//batch_size + 1} 嵌入成功")
                except Exception as batch_e:
                    logger.error(f"批次 {i//batch_size + 1} 嵌入失败: {str(batch_e)}")
                    # 为失败的批次生成零向量
                    dim = self.model.get_sentence_embedding_dimension()
                    zero_embeddings = [[0.0] * dim for _ in batch]
                    all_embeddings.extend(zero_embeddings)
            
            logger.debug(f"嵌入完成，生成了 {len(all_embeddings)} 个向量")
            
            if len(all_embeddings) > 0:
                first_embedding = all_embeddings[0]
                logger.debug(f"第一个嵌入向量: 维度={len(first_embedding)}, 前10个值={first_embedding[:10]}")
            
            return all_embeddings
        except Exception as e:
            logger.error(f"嵌入文本时出错: {str(e)}")
            logger.error(traceback.format_exc())
            # 返回零向量作为回退方案
            dim = self.model.get_sentence_embedding_dimension()
            return [[0.0] * dim for _ in input]

class VectorRAG:
    def __init__(self, persist_directory: str = "llm/database/chroma"):
        """
        初始化向量RAG服务
        
        Args:
            persist_directory: ChromaDB持久化存储路径
        """
        logger.info(f"初始化VectorRAG，存储目录: {persist_directory}")
        self.persist_directory = persist_directory
        # 确保目录存在
        Path(persist_directory).mkdir(parents=True, exist_ok=True)
        
        try:
            # 初始化ChromaDB客户端
            logger.info("正在初始化ChromaDB客户端...")
            self.client = chromadb.PersistentClient(path=persist_directory)
            logger.info("ChromaDB客户端初始化成功")
            
            # 尝试使用自定义的SentenceTransformer嵌入函数
            try:
                logger.info("正在初始化SentenceTransformer嵌入函数...")
                self.embedder = SentenceTransformerEmbedder("all-MiniLM-L6-v2")
                logger.info("嵌入函数初始化成功")
            except Exception as embedding_e:
                # 如果自定义嵌入函数初始化失败，使用默认嵌入函数
                logger.warning(f"SentenceTransformer嵌入函数初始化失败: {str(embedding_e)}")
                logger.info("使用默认的嵌入函数(全零向量)作为备用")
                self.embedder = embedding_functions.DefaultEmbeddingFunction()
        except Exception as e:
            logger.error(f"初始化过程中出错: {str(e)}")
            logger.error(traceback.format_exc())
            raise
    
    def get_collection(self, namespace: str, create_if_not_exists: bool = True) -> chromadb.Collection:
        """
        获取指定命名空间的集合
        
        Args:
            namespace: 命名空间
            create_if_not_exists: 如果不存在是否创建
            
        Returns:
            chromadb.Collection: ChromaDB集合对象
        """
        logger.info(f"尝试获取集合: {namespace}, 如不存在则创建: {create_if_not_exists}")
        try:
            collection = self.client.get_collection(name=namespace, embedding_function=self.embedder)
            logger.info(f"成功获取已存在的集合: {namespace}")
            return collection
        except Exception as e:
            # 检查是否是NotFoundError
            error_str = str(e)
            logger.info(f"获取集合 {namespace} 失败: {error_str}")
            
            if "does not exists" in error_str or "not found" in error_str.lower() or "not exist" in error_str.lower():
                if create_if_not_exists:
                    logger.info(f"集合 {namespace} 不存在，创建新集合")
                    try:
                        collection = self.client.create_collection(name=namespace, embedding_function=self.embedder)
                        logger.info(f"成功创建集合: {namespace}")
                        return collection
                    except Exception as create_e:
                        logger.error(f"创建集合 {namespace} 失败: {str(create_e)}")
                        logger.error(traceback.format_exc())
                        raise create_e
                else:
                    logger.error(f"集合 {namespace} 不存在且未指定创建，抛出异常")
                    raise ValueError(f"集合 {namespace} 不存在")
            else:
                # 其他类型的错误，直接抛出
                logger.error(f"获取集合时出现未知错误: {str(e)}")
                logger.error(traceback.format_exc())
                raise e
    
    def list_collections(self) -> List[str]:
        """
        列出所有可用的集合（命名空间）
        
        Returns:
            List[str]: 集合名称列表
        """
        logger.info("获取所有集合列表")
        try:
            collections = self.client.list_collections()
            collection_names = [col.name for col in collections]
            logger.info(f"找到 {len(collection_names)} 个集合: {collection_names}")
            return collection_names
        except Exception as e:
            logger.error(f"列出集合时出错: {str(e)}")
            logger.error(traceback.format_exc())
            return []
    
    async def add_texts(self, texts: List[str], namespace: str, metadata: Optional[Dict[str, Any]] = None) -> List[str]:
        """
        向指定命名空间添加文本
        
        Args:
            texts: 文本列表
            namespace: 命名空间
            metadata: 元数据，用于标记文档
            
        Returns:
            List[str]: 添加的文档ID列表
        """
        logger.info(f"异步添加 {len(texts)} 个文本到命名空间 {namespace}")
        try:
            # 使用线程池执行同步操作
            result = await asyncio.to_thread(self._add_texts_sync, texts, namespace, metadata)
            logger.info(f"成功添加 {len(result)} 个文本，生成的ID: {result[:3]}...")
            return result
        except Exception as e:
            logger.error(f"添加文本时出错: {str(e)}")
            logger.error(traceback.format_exc())
            raise
    
    def _add_texts_sync(self, texts: List[str], namespace: str, metadata: Optional[Dict[str, Any]] = None) -> List[str]:
        """同步版本的添加文本方法"""
        logger.debug(f"执行同步添加文本, 命名空间: {namespace}, 文本数量: {len(texts)}")
        try:
            logger.debug("获取或创建集合")
            collection = self.get_collection(namespace)
            logger.debug(f"获取集合成功: {collection.name}")
            
            ids = []
            metadatas = []
            
            for i, text in enumerate(texts):
                doc_id = str(uuid.uuid4())
                ids.append(doc_id)
                logger.debug(f"为第 {i+1}/{len(texts)} 个文本生成ID: {doc_id}")
                
                # 为每个文本添加相同的元数据
                if metadata:
                    metadatas.append(metadata)
                else:
                    metadatas.append({})
            
            logger.debug(f"开始执行批量添加, 文本数量: {len(texts)}")
            if len(texts) > 0:
                logger.debug(f"第一个文本示例: {texts[0][:100]}...")
                logger.debug(f"第一个文本长度: {len(texts[0])}")
            
            # 查看是否有非常大的文本块
            text_lengths = [len(text) for text in texts]
            avg_length = sum(text_lengths) / len(text_lengths) if text_lengths else 0
            max_length = max(text_lengths) if text_lengths else 0
            logger.debug(f"文本统计: 平均长度={avg_length:.1f}, 最大长度={max_length}")
            
            # 限制每个文本块的长度
            MAX_TEXT_LENGTH = 10000  # 限制每个文本块最大10000字符
            truncated_texts = []
            for i, text in enumerate(texts):
                if len(text) > MAX_TEXT_LENGTH:
                    logger.warning(f"文本块 {i+1} 超过最大长度限制，将被截断: {len(text)} -> {MAX_TEXT_LENGTH}")
                    truncated_texts.append(text[:MAX_TEXT_LENGTH])
                else:
                    truncated_texts.append(text)
                    
            try:
                logger.debug("执行ChromaDB add操作")
                collection.add(
                    documents=truncated_texts,
                    ids=ids,
                    metadatas=metadatas if metadata else None
                )
                logger.debug(f"批量添加成功")
            except Exception as e:
                logger.error(f"ChromaDB add操作失败: {str(e)}")
                logger.error(traceback.format_exc())
                
                # 尝试通过缩小批量大小来解决
                logger.info("尝试通过减小批次大小进行添加")
                batch_size = 5
                success_ids = []
                
                for i in range(0, len(truncated_texts), batch_size):
                    end = min(i + batch_size, len(truncated_texts))
                    batch_texts = truncated_texts[i:end]
                    batch_ids = ids[i:end]
                    batch_metadatas = metadatas[i:end] if metadatas else None
                    
                    try:
                        logger.debug(f"添加批次 {i//batch_size+1}, 大小: {len(batch_texts)}")
                        collection.add(
                            documents=batch_texts,
                            ids=batch_ids,
                            metadatas=batch_metadatas
                        )
                        success_ids.extend(batch_ids)
                        logger.debug(f"批次 {i//batch_size+1} 添加成功")
                    except Exception as batch_e:
                        logger.error(f"批次 {i//batch_size+1} 添加失败: {str(batch_e)}")
                
                if success_ids:
                    logger.info(f"通过小批量添加成功了 {len(success_ids)}/{len(ids)} 个文档")
                    return success_ids
                else:
                    raise Exception(f"所有批次都添加失败，原始错误: {str(e)}")
            
            return ids
        except Exception as e:
            logger.error(f"同步添加文本时出错: {str(e)}")
            logger.error(traceback.format_exc())
            raise
    
    async def delete_by_metadata(self, namespace: str, metadata_field: str, metadata_value: str) -> None:
        """
        根据元数据删除特定命名空间中的文档
        
        Args:
            namespace: 命名空间
            metadata_field: 元数据字段名
            metadata_value: 元数据字段值
        """
        await asyncio.to_thread(self._delete_by_metadata_sync, namespace, metadata_field, metadata_value)
    
    def _delete_by_metadata_sync(self, namespace: str, metadata_field: str, metadata_value: str) -> None:
        """同步版本的删除方法"""
        collection = self.get_collection(namespace, create_if_not_exists=False)
        collection.delete(where={metadata_field: metadata_value})
    
    async def delete_collection(self, namespace: str) -> None:
        """
        删除整个命名空间
        
        Args:
            namespace: 要删除的命名空间
        """
        await asyncio.to_thread(self._delete_collection_sync, namespace)
    
    def _delete_collection_sync(self, namespace: str) -> None:
        """同步版本的删除集合方法"""
        self.client.delete_collection(name=namespace)
    
    async def get_metadata_values(self, namespace: str, metadata_field: str) -> List[str]:
        """
        获取特定命名空间下某元数据字段的所有唯一值
        
        Args:
            namespace: 命名空间
            metadata_field: 元数据字段名
            
        Returns:
            List[str]: 元数据值列表
        """
        return await asyncio.to_thread(self._get_metadata_values_sync, namespace, metadata_field)
    
    def _get_metadata_values_sync(self, namespace: str, metadata_field: str) -> List[str]:
        """同步版本的获取元数据值方法"""
        try:
            collection = self.get_collection(namespace, create_if_not_exists=False)
            results = collection.get()
            
            # 如果没有文档，返回空列表
            if not results['metadatas']:
                return []
            
            # 提取所有值并去重
            values = set()
            for metadata in results['metadatas']:
                if metadata_field in metadata:
                    values.add(metadata[metadata_field])
            
            return list(values)
        except Exception:
            return []
    
    async def search(self, query: str, namespaces: Optional[List[str]] = None, n_results: int = 5, 
                     metadata_filter: Optional[Dict[str, str]] = None) -> str:
        """
        向量搜索
        
        Args:
            query: 用户查询文本
            namespaces: 要搜索的命名空间列表，默认搜索所有命名空间
            n_results: 每个命名空间返回的最大结果数
            metadata_filter: 元数据过滤条件
            
        Returns:
            str: 搜索结果
        """
        logger.info(f"执行向量搜索，查询: '{query}'")
        logger.info(f"搜索参数: namespaces={namespaces}, n_results={n_results}, metadata_filter={metadata_filter}")
        
        # 获取命名空间列表
        if not namespaces:
            logger.info("未指定命名空间，将搜索所有可用命名空间")
            namespaces = await asyncio.to_thread(self.list_collections)
            logger.info(f"找到 {len(namespaces)} 个可用命名空间: {namespaces}")
            
        if not namespaces:
            logger.warning("没有可用的命名空间，搜索结束")
            return "未找到相关医学知识。"
        
        try:
            # 使用线程池执行实际搜索
            logger.info(f"开始执行搜索，将在 {len(namespaces)} 个命名空间中搜索")
            result = await asyncio.to_thread(self._search_sync, query, namespaces, n_results, metadata_filter)
            logger.info("搜索完成")
            return result
        except Exception as e:
            logger.error(f"执行搜索时出错: {str(e)}")
            logger.error(traceback.format_exc())
            return f"搜索过程中出错: {str(e)}"
    
    def _search_sync(self, query: str, namespaces: List[str], n_results: int, metadata_filter: Optional[Dict[str, str]]) -> str:
        """同步版本的搜索方法"""
        # 在所有指定的命名空间中搜索
        all_results = []
        for namespace in namespaces:
            try:
                logger.debug(f"在命名空间 {namespace} 中搜索")
                collection = self.get_collection(namespace, create_if_not_exists=False)
                
                # 构建查询参数
                query_params = {
                    "query_texts": [query],
                    "n_results": n_results
                }
                
                if metadata_filter:
                    query_params["where"] = metadata_filter
                
                logger.debug(f"执行查询，参数: {query_params}")
                results = collection.query(**query_params)
                logger.debug(f"查询结果: 文档数量={len(results['documents'][0]) if results['documents'] and results['documents'][0] else 0}")
                
                # 处理当前命名空间的结果
                if results['documents'] and results['documents'][0]:
                    for i, doc in enumerate(results['documents'][0]):
                        # 提取相关性分数 (如果有)
                        score = results.get('distances', [[]])[0][i] if 'distances' in results else None
                        score_text = f"(相关度: {1 - score:.2f})" if score is not None else ""
                        
                        # 提取文档元数据
                        metadata = results.get('metadatas', [[]])[0][i] if 'metadatas' in results else {}
                        metadata_text = f"[来源: {metadata.get('source', namespace)}]" if metadata else f"[来源: {namespace}]"
                        
                        all_results.append(f"{metadata_text} {score_text}\n{doc.strip()}")
            except ValueError as e:
                logger.warning(f"命名空间 {namespace} 不存在: {str(e)}")
                continue
            except Exception as e:
                logger.error(f"在命名空间 {namespace} 中搜索时出错: {str(e)}")
                logger.error(traceback.format_exc())
                continue
        
        if not all_results:
            logger.warning("没有找到任何搜索结果")
            return "未找到相关医学知识。"
        
        # 组合所有结果
        result_text = "\n\n".join(all_results)
        logger.info(f"成功找到 {len(all_results)} 条搜索结果")
        return f"{result_text}"
