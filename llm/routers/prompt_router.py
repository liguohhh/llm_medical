from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Dict, List, Optional
from prompts.prompt_manager import PromptManager

router = APIRouter()
prompt_manager = PromptManager()

class SubTemplateModel(BaseModel):
    """小模板数据模型"""
    template: str
    description: Optional[str] = None
    parameters: Optional[List[str]] = None
    order: Optional[int] = None

class TemplateData(BaseModel):
    """大模板数据模型"""
    description: str
    sub_templates: Dict[str, SubTemplateModel]

class TemplateResponse(BaseModel):
    """模板操作响应"""
    status: str
    message: str
    data: Optional[Dict] = None

@router.post("/templates/{template_id}", response_model=TemplateResponse)
async def add_template(template_id: str, template_data: TemplateData):
    """
    添加新的大模板
    
    参数:
    - template_id: 大模板ID
    - template_data: 大模板数据，包含描述和子模板集合
    """
    try:
        success = prompt_manager.add_template(template_id, template_data.dict())
        if not success:
            raise HTTPException(status_code=400, detail="Template ID already exists")
        return TemplateResponse(
            status="success",
            message="Template added successfully",
            data={"template_id": template_id}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.put("/templates/{template_id}", response_model=TemplateResponse)
async def update_template(template_id: str, template_data: TemplateData):
    """
    更新现有大模板
    
    参数:
    - template_id: 大模板ID
    - template_data: 新的大模板数据
    """
    try:
        success = prompt_manager.update_template(template_id, template_data.dict())
        if not success:
            raise HTTPException(status_code=404, detail="Template not found")
        return TemplateResponse(
            status="success",
            message="Template updated successfully",
            data={"template_id": template_id}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.delete("/templates/{template_id}", response_model=TemplateResponse)
async def delete_template(template_id: str):
    """
    删除大模板
    
    参数:
    - template_id: 要删除的大模板ID
    """
    try:
        success = prompt_manager.delete_template(template_id)
        if not success:
            raise HTTPException(status_code=404, detail="Template not found")
        return TemplateResponse(
            status="success",
            message="Template deleted successfully"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/templates/{template_id}", response_model=TemplateResponse)
async def get_template(template_id: str):
    """
    获取指定大模板
    
    参数:
    - template_id: 大模板ID
    """
    try:
        template = prompt_manager.get_template(template_id)
        if not template:
            raise HTTPException(status_code=404, detail="Template not found")
        return TemplateResponse(
            status="success",
            message="Template retrieved successfully",
            data=template
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/templates", response_model=TemplateResponse)
async def list_templates():
    """
    获取所有大模板列表
    """
    try:
        templates = prompt_manager.list_templates()
        return TemplateResponse(
            status="success",
            message="Templates retrieved successfully",
            data={"templates": templates}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e)) 