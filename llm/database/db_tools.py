import os
import asyncio
from sqlalchemy import create_engine
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_scoped_session
from sqlalchemy.orm import sessionmaker, scoped_session
from sqlalchemy.ext.declarative import declarative_base
from pathlib import Path

# 确保数据库目录存在
db_dir = Path("./llm/database")
db_dir.mkdir(exist_ok=True, parents=True)

# 数据库URL
SQLALCHEMY_DATABASE_URL = f"sqlite:///{db_dir}/precise.db"
ASYNC_SQLALCHEMY_DATABASE_URL = f"sqlite+aiosqlite:///{db_dir}/precise.db"

# 创建同步引擎和会话
engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 创建异步引擎和会话
async_engine = create_async_engine(
    ASYNC_SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},  # 允许跨线程访问
    pool_pre_ping=True,  # 连接前检查
    echo=False  # 设置为True可以看到SQL语句
)
AsyncSessionLocal = sessionmaker(
    bind=async_engine, class_=AsyncSession, expire_on_commit=False
)

# 同步获取数据库会话
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# 异步获取数据库会话
async def get_async_db():
    async with AsyncSessionLocal() as session:
        yield session

# 确保事件循环正在运行
def ensure_event_loop():
    try:
        asyncio.get_event_loop()
    except RuntimeError:
        asyncio.set_event_loop(asyncio.new_event_loop())

# 初始化数据库表
def init_db():
    ensure_event_loop()  # 确保事件循环存在
    from llm.models.precise_models import Base
    Base.metadata.create_all(bind=engine)
    print(f"数据库初始化完成，路径: {db_dir}/precise.db") 