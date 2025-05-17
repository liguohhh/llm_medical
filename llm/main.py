from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
import uvicorn
import logging
import time
import traceback
import asyncio
import os

# 确保导入aiosqlite
try:
    import aiosqlite
except ImportError:
    print("缺少aiosqlite模块，尝试安装...")
    os.system("pip install aiosqlite")
    import aiosqlite

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler()
    ]
)
logger = logging.getLogger("llm-medical")

app = FastAPI(
    title="LLM Medical API",
    description="医疗大模型API服务",
    version="1.0.0"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 请求中间件，添加请求日志
@app.middleware("http")
async def log_requests(request: Request, call_next):
    start_time = time.time()
    request_id = id(request)
    logger.info(f"开始请求 [{request_id}] {request.method} {request.url.path}")
    
    try:
        response = await call_next(request)
        process_time = time.time() - start_time
        logger.info(f"完成请求 [{request_id}] {response.status_code} 耗时: {process_time:.3f}s")
        return response
    except Exception as e:
        process_time = time.time() - start_time
        logger.error(f"请求失败 [{request_id}] 耗时: {process_time:.3f}s Error: {str(e)}")
        logger.error(traceback.format_exc())
        return JSONResponse(
            status_code=500,
            content={"detail": str(e)}
        )

# 处理验证错误
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    logger.error(f"请求验证错误: {str(exc)}")
    return JSONResponse(
        status_code=422,
        content={"detail": str(exc)}
    )

# 导入路由
logger.info("开始导入路由模块...")
try:
    from llm.routers import chat_router, prompt_router as template_router, vector_router, precise_router
    logger.info("路由模块导入成功: chat_router, template_router, vector_router, precise_router")
except Exception as e:
    logger.error(f"路由模块导入失败: {str(e)}")
    logger.error(traceback.format_exc())

# 初始化数据库
logger.info("开始初始化数据库...")
try:
    # 设置事件循环
    try:
        loop = asyncio.get_event_loop()
    except RuntimeError:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        
    from llm.database.db_tools import init_db
    init_db()
    logger.info("数据库初始化成功")
except Exception as e:
    logger.error(f"数据库初始化失败: {str(e)}")
    logger.error(traceback.format_exc())

# 注册路由
logger.info("开始注册路由...")
try:
    app.include_router(chat_router.router, prefix="/api/v1", tags=["chat"])
    logger.info("chat_router注册成功")
    
    app.include_router(template_router.router, prefix="/api/v1", tags=["templates"])
    logger.info("template_router注册成功")
    
    app.include_router(vector_router.router, prefix="/api/v1", tags=["vectors"])
    logger.info("vector_router注册成功")
    
    app.include_router(precise_router.router, prefix="/api/v1", tags=["precise"])
    logger.info("precise_router注册成功")
except Exception as e:
    logger.error(f"路由注册失败: {str(e)}")
    logger.error(traceback.format_exc())

@app.get("/health")
async def health_check():
    """健康检查接口"""
    return {"status": "ok"}

# 添加调试代码，列出所有已注册的路由
def list_routes():
    """列出所有已注册的路由"""
    routes = []
    for route in app.routes:
        routes.append({
            "path": getattr(route, "path", None),
            "name": getattr(route, "name", None),
            "methods": getattr(route, "methods", None)
        })
    return routes

logger.info("以下是所有已注册的路由:")
for route in list_routes():
    logger.info(f"路径: {route['path']}, 方法: {route['methods']}, 名称: {route['name']}")

if __name__ == "__main__":
    logger.info("启动LLM Medical API服务...")
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)
