from sqlalchemy import Column, Integer, String, Text, Boolean, ForeignKey, Table
from sqlalchemy.orm import relationship
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

# 匹配词和条目的多对多关系表
keyword_entry_association = Table(
    'keyword_entry_association',
    Base.metadata,
    Column('keyword_id', Integer, ForeignKey('precise_keywords.id')),
    Column('entry_id', Integer, ForeignKey('precise_entries.id'))
)

class PreciseCategory(Base):
    """精确查询大类"""
    __tablename__ = 'precise_categories'

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(100), nullable=False, comment="大类名称")
    uid = Column(String(50), nullable=False, unique=True, comment="大类唯一标识符")
    
    # 关联关系
    entries = relationship("PreciseEntry", back_populates="category", cascade="all, delete-orphan")
    
    def __repr__(self):
        return f"<PreciseCategory(id={self.id}, name='{self.name}', uid='{self.uid}')>"

class PreciseEntry(Base):
    """精确查询条目"""
    __tablename__ = 'precise_entries'

    id = Column(Integer, primary_key=True, autoincrement=True)
    category_id = Column(Integer, ForeignKey('precise_categories.id'), nullable=False)
    description = Column(String(200), nullable=False, comment="条目描述")
    content = Column(Text, nullable=False, comment="条目具体内容")
    weight = Column(Integer, default=50, comment="权重，取值范围0-99")
    is_enabled = Column(Boolean, default=True, comment="是否启用")
    uid = Column(String(50), nullable=False, unique=True, comment="条目唯一标识符")
    
    # 关联关系
    category = relationship("PreciseCategory", back_populates="entries")
    keywords = relationship("PreciseKeyword", secondary=keyword_entry_association, back_populates="entries")
    
    def __repr__(self):
        return f"<PreciseEntry(id={self.id}, description='{self.description}', weight={self.weight})>"

class PreciseKeyword(Base):
    """精确查询匹配词"""
    __tablename__ = 'precise_keywords'

    id = Column(Integer, primary_key=True, autoincrement=True)
    keyword = Column(String(50), nullable=False, unique=True, comment="匹配词")
    
    # 关联关系
    entries = relationship("PreciseEntry", secondary=keyword_entry_association, back_populates="keywords")
    
    def __repr__(self):
        return f"<PreciseKeyword(id={self.id}, keyword='{self.keyword}')>" 