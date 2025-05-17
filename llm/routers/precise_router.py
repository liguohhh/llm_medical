from fastapi import APIRouter, Depends, HTTPException, Query
from typing import List, Dict, Optional, Any
from pydantic import BaseModel, Field
import logging

from llm.services.precise_service import PreciseService

# 获取日志记录器
logger = logging.getLogger("llm-medical")

# 定义请求和响应模型
class CategoryCreateRequest(BaseModel):
    name: str = Field(..., description="大类名称")

class CategoryUpdateRequest(BaseModel):
    name: str = Field(..., description="新的大类名称")

class EntryCreateRequest(BaseModel):
    description: str = Field(..., description="条目描述")
    content: str = Field(..., description="条目内容")
    keywords: List[str] = Field(..., description="匹配关键词列表")
    weight: int = Field(50, description="条目权重，0-99之间的整数", ge=0, le=99)
    is_enabled: bool = Field(True, description="是否启用该条目")

class EntryUpdateRequest(BaseModel):
    description: Optional[str] = Field(None, description="条目描述")
    content: Optional[str] = Field(None, description="条目内容")
    keywords: Optional[List[str]] = Field(None, description="匹配关键词列表")
    weight: Optional[int] = Field(None, description="条目权重，0-99之间的整数", ge=0, le=99)
    is_enabled: Optional[bool] = Field(None, description="是否启用该条目")

class SearchRequest(BaseModel):
    query: str = Field(..., description="搜索查询文本")
    category_uids: Optional[List[str]] = Field(None, description="限定搜索范围的大类UID列表")
    max_results: int = Field(5, description="最大返回结果数", ge=0)
    search_depth: int = Field(2, description="搜索深度", ge=1, le=3)

class StandardResponse(BaseModel):
    success: bool = Field(..., description="操作是否成功")
    message: str = Field(..., description="操作结果消息")
    data: Optional[Dict[str, Any]] = Field(None, description="返回的数据")

# 创建路由器
router = APIRouter(
    prefix="/precise",
    tags=["precise"],
    responses={404: {"description": "Not found"}},
)

# 服务实例
precise_service = PreciseService()

@router.post("/category", response_model=StandardResponse, summary="创建一个新的大类")
async def create_category(request: CategoryCreateRequest):
    """
    创建一个新的精确查询大类
    
    - **name**: 大类名称
    
    返回:
    - **success**: 操作是否成功
    - **message**: 操作结果消息
    - **data**: 包含新建大类信息的字典
    """
    logger.info(f"接收到创建大类请求: {request.name}")
    result = await precise_service.create_category(request.name)
    if not result["success"]:
        logger.error(f"创建大类失败: {result['message']}")
        raise HTTPException(status_code=400, detail=result["message"])
    logger.info(f"创建大类成功: {request.name}, UID: {result['data']['uid']}")
    return result

@router.post("/entry/{category_uid}", response_model=StandardResponse, summary="创建一个新的条目")
async def create_entry(category_uid: str, request: EntryCreateRequest):
    """
    在指定大类下创建一个新的精确查询条目
    
    - **category_uid**: 大类的唯一标识符
    - **description**: 条目描述
    - **content**: 条目内容
    - **keywords**: 匹配关键词列表
    - **weight**: 条目权重，0-99之间的整数
    - **is_enabled**: 是否启用该条目
    
    返回:
    - **success**: 操作是否成功
    - **message**: 操作结果消息
    - **data**: 包含新建条目信息的字典
    """
    logger.info(f"接收到创建条目请求: 大类UID={category_uid}, 描述={request.description}")
    
    try:
        result = await precise_service.create_entry(
            category_uid=category_uid,
            description=request.description,
            content=request.content,
            keywords=request.keywords,
            weight=request.weight,
            is_enabled=request.is_enabled
        )
        
        if not result["success"]:
            logger.error(f"创建条目失败: {result['message']}")
            raise HTTPException(status_code=400, detail=result["message"])
            
        logger.info(f"创建条目成功: {request.description}, UID: {result['data']['uid']}")
        return result
    except HTTPException:
        # 直接重新抛出HTTP异常
        raise
    except Exception as e:
        logger.error(f"创建条目时发生未处理错误: {str(e)}")
        import traceback
        logger.error(f"错误详情: {traceback.format_exc()}")
        raise HTTPException(status_code=500, detail=f"创建条目时发生服务器错误: {str(e)}")

@router.delete("/category/{category_uid}", response_model=StandardResponse, summary="删除一个大类")
async def delete_category(category_uid: str):
    """
    删除指定的精确查询大类
    
    - **category_uid**: 大类的唯一标识符
    
    返回:
    - **success**: 操作是否成功
    - **message**: 操作结果消息
    """
    logger.info(f"接收到删除大类请求: UID={category_uid}")
    result = await precise_service.delete_category(category_uid)
    if not result["success"]:
        logger.error(f"删除大类失败: {result['message']}")
        raise HTTPException(status_code=404, detail=result["message"])
    logger.info(f"删除大类成功: UID={category_uid}")
    return result

@router.put("/category/{category_uid}", response_model=StandardResponse, summary="更新大类名称")
async def update_category(category_uid: str, request: CategoryUpdateRequest):
    """
    更新指定大类的名称
    
    - **category_uid**: 大类的唯一标识符
    - **name**: 新的大类名称
    
    返回:
    - **success**: 操作是否成功
    - **message**: 操作结果消息
    - **data**: 包含更新后大类信息的字典
    """
    logger.info(f"接收到更新大类请求: UID={category_uid}, 新名称={request.name}")
    result = await precise_service.update_category(category_uid, request.name)
    if not result["success"]:
        logger.error(f"更新大类失败: {result['message']}")
        raise HTTPException(status_code=404, detail=result["message"])
    logger.info(f"更新大类成功: UID={category_uid}, 新名称={request.name}")
    return result

@router.put("/entry/{entry_uid}", response_model=StandardResponse, summary="更新条目信息")
async def update_entry(entry_uid: str, request: EntryUpdateRequest):
    """
    更新指定条目的信息
    
    - **entry_uid**: 条目的唯一标识符
    - **description**: 新的条目描述 (可选)
    - **content**: 新的条目内容 (可选)
    - **keywords**: 新的匹配关键词列表 (可选)
    - **weight**: 新的条目权重 (可选)
    - **is_enabled**: 是否启用该条目 (可选)
    
    返回:
    - **success**: 操作是否成功
    - **message**: 操作结果消息
    - **data**: 包含更新后条目信息的字典
    """
    logger.info(f"接收到更新条目请求: UID={entry_uid}")
    result = await precise_service.update_entry(
        entry_uid=entry_uid,
        description=request.description,
        content=request.content,
        keywords=request.keywords,
        weight=request.weight,
        is_enabled=request.is_enabled
    )
    if not result["success"]:
        logger.error(f"更新条目失败: {result['message']}")
        raise HTTPException(status_code=404, detail=result["message"])
    logger.info(f"更新条目成功: UID={entry_uid}")
    return result

@router.get("/categories", response_model=StandardResponse, summary="列出所有大类")
async def list_categories():
    """
    获取所有精确查询大类的列表
    
    返回:
    - **success**: 操作是否成功
    - **message**: 操作结果消息
    - **data.categories**: 包含所有大类信息的列表
    """
    logger.info("接收到列出所有大类请求")
    result = await precise_service.list_categories()
    logger.info(f"列出大类成功，共找到 {len(result['data']['categories'])} 个大类")
    return result

@router.get("/entries/{category_uid}", response_model=StandardResponse, summary="列出指定大类下的所有条目")
async def list_entries(category_uid: str):
    """
    获取指定大类下所有精确查询条目的列表
    
    - **category_uid**: 大类的唯一标识符
    
    返回:
    - **success**: 操作是否成功
    - **message**: 操作结果消息
    - **data.category**: 大类信息
    - **data.entries**: 包含所有条目信息的列表
    """
    logger.info(f"接收到列出大类条目请求: UID={category_uid}")
    result = await precise_service.list_entries(category_uid)
    if not result["success"]:
        logger.error(f"列出条目失败: {result['message']}")
        raise HTTPException(status_code=404, detail=result["message"])
    logger.info(f"列出条目成功，大类: {result['data']['category']['name']}, 共找到 {len(result['data']['entries'])} 个条目")
    return result

@router.post("/search", response_model=StandardResponse, summary="搜索匹配的条目")
async def search(request: SearchRequest):
    """
    在精确查询库中搜索匹配的条目
    
    - **query**: 搜索查询文本
    - **category_uids**: 限定搜索范围的大类UID列表 (可选)
    - **max_results**: 最大返回结果数 (默认5)
    - **search_depth**: 搜索深度，1表示只进行一次关键词匹配，2表示进行二次关联匹配 (默认2)
    
    返回:
    - **success**: 操作是否成功
    - **message**: 操作结果消息
    - **data.entries**: 包含匹配条目的列表
    - **data.precise_content**: 合并后的条目内容文本
    """
    logger.info(f"接收到搜索请求: 查询={request.query}, 深度={request.search_depth}, 最大结果数={request.max_results}")
    
    try:
        result = await precise_service.search(
            query=request.query,
            category_uids=request.category_uids,
            max_results=request.max_results,
            search_depth=request.search_depth
        )
        
        if not result["success"]:
            logger.error(f"搜索失败: {result['message']}")
            raise HTTPException(status_code=400, detail=result["message"])
            
        logger.info(f"搜索成功，找到 {len(result['data']['entries'])} 个条目")
        return result
    except HTTPException:
        # 直接重新抛出HTTP异常
        raise
    except Exception as e:
        logger.error(f"搜索时发生未处理错误: {str(e)}")
        import traceback
        logger.error(f"错误详情: {traceback.format_exc()}")
        raise HTTPException(status_code=500, detail=f"搜索失败: {str(e)}") 