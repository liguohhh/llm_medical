from typing import List, Dict, Optional, Any
from llm.services.precise_service import PreciseService

class PreciseRAG:
    def __init__(self):
        """初始化精确匹配RAG服务"""
        self.precise_service = PreciseService()

    async def search(
        self, 
        query: str, 
        category_uids: Optional[List[str]] = None, 
        max_results: int = 5,
        search_depth: int = 2
    ) -> str:
        """
        在精确查询库中搜索匹配的内容
        
        Args:
            query: 用户查询
            category_uids: 搜索范围限定的大类UID列表，为空则搜索所有大类
            max_results: 最大返回结果数
            search_depth: 搜索深度，1表示只进行关键词匹配，2表示进行二次关联匹配
            
        Returns:
            str: 合并后的精确匹配内容
        """
        result = await self.precise_service.search(
            query=query,
            category_uids=category_uids,
            max_results=max_results,
            search_depth=search_depth
        )
        
        # 如果搜索失败或没有结果，返回空字符串
        if not result["success"] or not result["data"]["entries"]:
            return ""
        
        # 返回合并的内容
        return result["data"]["precise_content"]
