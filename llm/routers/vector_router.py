from fastapi import APIRouter, HTTPException, UploadFile, File, Form, Query
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Dict, List, Optional, Any
from services.vector_service import VectorService

router = APIRouter()
vector_service = VectorService()

class VectorResponse(BaseModel):
    """向量操作响应"""
    status: str
    message: str
    data: Optional[Dict[str, Any]] = None

@router.post("/vectors/upload", response_model=VectorResponse)
async def upload_document(
    file: UploadFile = File(...),
    namespace: str = Form(...),
    chunk_size: int = Form(500),
    chunk_overlap: int = Form(50)
):
    """
    上传文档到向量数据库
    
    参数:
    - file: 要上传的文件（支持txt、docx、pdf）
    - namespace: 文档存储的命名空间
    - chunk_size: 文档分块大小
    - chunk_overlap: 分块重叠大小
    """
    try:
        # 读取文件内容
        file_content = await file.read()
        
        # 添加文档
        result = await vector_service.add_document(
            file_content=file_content,
            file_name=file.filename,
            namespace=namespace,
            chunk_size=chunk_size,
            chunk_overlap=chunk_overlap
        )
        
        if "error" in result:
            raise HTTPException(status_code=400, detail=result["error"])
        
        return VectorResponse(
            status="success",
            message=f"文档 {file.filename} 成功上传到命名空间 {namespace}",
            data=result
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.delete("/vectors/document", response_model=VectorResponse)
async def delete_document(namespace: str, doc_id: str):
    """
    删除指定文档
    
    参数:
    - namespace: 命名空间
    - doc_id: 文档ID
    """
    try:
        result = await vector_service.delete_document(namespace, doc_id)
        
        if "error" in result:
            raise HTTPException(status_code=400, detail=result["error"])
        
        return VectorResponse(
            status="success",
            message=result["message"],
            data={"namespace": namespace, "doc_id": doc_id}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.delete("/vectors/namespace/{namespace}", response_model=VectorResponse)
async def delete_namespace(namespace: str):
    """
    删除整个命名空间
    
    参数:
    - namespace: 要删除的命名空间
    """
    try:
        result = await vector_service.delete_namespace(namespace)
        
        if "error" in result:
            raise HTTPException(status_code=400, detail=result["error"])
        
        return VectorResponse(
            status="success",
            message=result["message"],
            data={"namespace": namespace}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/vectors/namespace", response_model=VectorResponse)
async def create_namespace(namespace: str = Form(...)):
    """
    创建新的命名空间
    
    参数:
    - namespace: 要创建的命名空间名称
    """
    try:
        result = await vector_service.create_namespace(namespace)
        
        if "error" in result:
            raise HTTPException(status_code=400, detail=result["error"])
        
        return VectorResponse(
            status="success",
            message=result["message"],
            data={"namespace": namespace}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/vectors/namespaces", response_model=VectorResponse)
async def list_namespaces():
    """
    列出所有命名空间
    """
    try:
        result = await vector_service.list_namespaces()
        
        if "error" in result:
            raise HTTPException(status_code=400, detail=result["error"])
        
        return VectorResponse(
            status="success",
            message="成功获取命名空间列表",
            data=result
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/vectors/documents/{namespace}", response_model=VectorResponse)
async def list_documents(namespace: str):
    """
    列出指定命名空间中的所有文档
    
    参数:
    - namespace: 命名空间
    """
    try:
        result = await vector_service.list_documents(namespace)
        
        if "error" in result:
            raise HTTPException(status_code=400, detail=result["error"])
        
        return VectorResponse(
            status="success",
            message=f"成功获取命名空间 {namespace} 的文档列表",
            data=result
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e)) 