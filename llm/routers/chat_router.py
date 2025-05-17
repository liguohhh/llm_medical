from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from typing import Optional, List, Dict, Union
from llm.services.chat_service import ChatService

router = APIRouter()
chat_service = ChatService()

class ModelConfig(BaseModel):
    """模型配置"""
    model_name: str
    api_key: str
    base_url: str  # 自定义API链接

class TemplateConfig(BaseModel):
    """模板配置"""
    template_id: str  # 大模板ID
    sub_template_ids: List[str]  # 要使用的子模板ID列表
    params: Dict[str, str]  # 填充模板的参数

class VectorSearchConfig(BaseModel):
    """向量搜索配置"""
    namespaces: Optional[List[str]] = None  # 要搜索的命名空间，None表示搜索所有
    n_results: int = 5  # 搜索结果数量
    metadata_filter: Optional[Dict[str, str]] = None  # 元数据过滤条件
    rag_history_count: int = 1  # 用于RAG查询的历史消息数量，默认为1（只使用当前消息）

class PreciseSearchConfig(BaseModel):
    """精确查询配置"""
    categories: Optional[List[str]] = None  # 要搜索的大类UID列表，None表示搜索所有
    max_results: int = 5  # 最大返回结果数
    search_depth: int = 2  # 搜索深度，1表示只进行关键词匹配，2表示进行二次关联匹配
    rag_history_count: int = 1  # 用于RAG查询的历史消息数量，默认为1（只使用当前消息）

class ChatRequest(BaseModel):
    """聊天请求"""
    message: str
    model_settings: ModelConfig
    template_config: TemplateConfig  # 模板配置
    vector_search_config: Optional[VectorSearchConfig] = None  # 向量搜索配置
    precise_search_config: Optional[PreciseSearchConfig] = None  # 精确查询配置
    history: Optional[List[Dict[str, str]]] = None

class ChatResponse(BaseModel):
    """聊天响应"""
    answer: str
    vector_content: str
    precise_content: str

@router.post("/ask", response_model=ChatResponse)
async def ask_question(request: ChatRequest):
    """标准问答接口"""
    try:
        response = await chat_service.process_chat(
            request.message,
            request.model_settings,
            request.template_config.template_id,
            request.template_config.sub_template_ids,
            request.template_config.params,
            request.history,
            vector_search_config=request.vector_search_config,
            precise_search_config=request.precise_search_config
        )
        return response
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/stream")
async def stream_chat(request: ChatRequest):
    """流式问答接口"""
    try:
        return StreamingResponse(
            chat_service.process_stream_chat(
                request.message,
                request.model_settings,
                request.template_config.template_id,
                request.template_config.sub_template_ids,
                request.template_config.params,
                request.history,
                vector_search_config=request.vector_search_config,
                precise_search_config=request.precise_search_config
            ),
            media_type="text/event-stream"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
