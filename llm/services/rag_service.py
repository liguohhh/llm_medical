from typing import Dict, List, Tuple, Optional, Any
from llm.rag.vector_rag import VectorRAG
from llm.rag.precise_rag import PreciseRAG

class RAGService:
    def __init__(self):
        self.vector_rag = VectorRAG()
        self.precise_rag = PreciseRAG()

    async def get_rag_content(
        self, 
        query: str = None,
        vector_query: str = None,
        precise_query: str = None,
        namespaces: Optional[List[str]] = None, 
        n_results: int = 5,
        metadata_filter: Optional[Dict[str, str]] = None,
        precise_categories: Optional[List[str]] = None,
        precise_max_results: int = 5,
        precise_search_depth: int = 2
    ) -> Tuple[str, str]:
        """
        获取RAG内容
        
        Args:
            query: 用户查询(向量查询和精确查询都使用这个查询时使用，已弃用，保留参数用于兼容)
            vector_query: 用于向量搜索的查询文本
            precise_query: 用于精确查询的查询文本
            namespaces: 要搜索的向量数据库命名空间列表，默认搜索所有命名空间
            n_results: 向量搜索时每个命名空间返回的最大结果数
            metadata_filter: 向量搜索时的元数据过滤条件
            precise_categories: 精确查询时要搜索的大类UID列表，默认搜索所有大类
            precise_max_results: 精确查询时返回的最大结果数
            precise_search_depth: 精确查询的搜索深度，1表示只进行关键词匹配，2表示进行二次关联匹配
            
        Returns:
            Tuple[str, str]: (向量检索内容, 精确匹配内容)
        """
        # 处理兼容性：如果提供了query但没有提供特定查询，则使用query
        if query is not None:
            vector_query = vector_query or query
            precise_query = precise_query or query
        
        # 确保至少有一个查询文本
        if vector_query is None and precise_query is None:
            raise ValueError("必须提供至少一个查询文本")
            
        # 1. 从向量数据库获取内容
        vector_content = ""
        if vector_query:
            vector_content = await self.vector_rag.search(
                query=vector_query,
                namespaces=namespaces,
                n_results=n_results,
                metadata_filter=metadata_filter
            )
        
        # 2. 获取精确匹配内容
        precise_content = ""
        if precise_query:
            precise_content = await self.precise_rag.search(
                query=precise_query,
                category_uids=precise_categories,
                max_results=precise_max_results,
                search_depth=precise_search_depth
            )
        
        # 返回分离的内容
        return vector_content, precise_content 