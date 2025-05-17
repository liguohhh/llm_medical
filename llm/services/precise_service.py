import uuid
from typing import List, Dict, Optional, Any, Set, Tuple
from sqlalchemy import select, delete, update
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload
from sqlalchemy.sql import or_, and_

from llm.models.precise_models import PreciseCategory, PreciseEntry, PreciseKeyword
from llm.database.db_tools import AsyncSessionLocal

class PreciseService:
    def __init__(self):
        pass
        
    async def create_category(self, name: str) -> Dict[str, Any]:
        """
        创建一个新的大类
        
        Args:
            name: 大类名称
            
        Returns:
            Dict: 包含创建结果的字典
        """
        # 导入同步会话
        from sqlalchemy.orm import Session
        from llm.database.db_tools import engine
        
        # 创建同步会话
        session = Session(engine)
        try:
            # 生成唯一ID
            uid = f"cat_{uuid.uuid4().hex[:8]}"
            
            # 创建新大类
            new_category = PreciseCategory(name=name, uid=uid)
            session.add(new_category)
            session.commit()
            session.refresh(new_category)
            
            return {
                "success": True,
                "message": f"创建大类成功: {name}",
                "data": {
                    "id": new_category.id,
                    "name": new_category.name,
                    "uid": new_category.uid
                }
            }
        except Exception as e:
            session.rollback()
            import traceback
            error_details = traceback.format_exc()
            print(f"创建大类时发生错误: {str(e)}")
            print(f"错误详情: {error_details}")
            return {
                "success": False,
                "message": f"创建大类失败: {str(e)}",
                "data": None
            }
        finally:
            session.close()
    
    async def create_entry(
        self, 
        category_uid: str, 
        description: str, 
        content: str, 
        keywords: List[str],
        weight: int = 50, 
        is_enabled: bool = True
    ) -> Dict[str, Any]:
        """
        创建一个新的条目
        
        Args:
            category_uid: 大类的UID
            description: 条目描述
            content: 条目内容
            keywords: 匹配关键词列表
            weight: 条目权重 (0-99)
            is_enabled: 是否启用
            
        Returns:
            Dict: 包含创建结果的字典
        """
        # 导入同步会话
        from sqlalchemy.orm import Session
        from llm.database.db_tools import engine
        
        # 创建同步会话
        session = Session(engine)
        try:
            # 检查大类是否存在
            category = session.query(PreciseCategory).filter(PreciseCategory.uid == category_uid).one_or_none()
            
            if not category:
                return {
                    "success": False,
                    "message": f"大类不存在: {category_uid}"
                }
            
            # 规范化权重值
            if weight < 0:
                weight = 0
            elif weight > 99:
                weight = 99
                
            # 生成唯一ID
            uid = f"ent_{uuid.uuid4().hex[:8]}"
            
            # 创建新条目
            new_entry = PreciseEntry(
                category_id=category.id,
                description=description,
                content=content,
                weight=weight,
                is_enabled=is_enabled,
                uid=uid
            )
            
            # 处理关键词
            for keyword_text in keywords:
                # 查找现有关键词或创建新关键词
                keyword = session.query(PreciseKeyword).filter(PreciseKeyword.keyword == keyword_text).one_or_none()
                
                if not keyword:
                    keyword = PreciseKeyword(keyword=keyword_text)
                    session.add(keyword)
                
                # 关联关键词和条目
                new_entry.keywords.append(keyword)
            
            session.add(new_entry)
            session.commit()
            session.refresh(new_entry)
            
            # 准备返回的关键词列表
            keyword_list = [k.keyword for k in new_entry.keywords]
            
            return {
                "success": True,
                "message": f"创建条目成功: {description}",
                "data": {
                    "id": new_entry.id,
                    "uid": new_entry.uid,
                    "description": new_entry.description,
                    "content": new_entry.content,
                    "weight": new_entry.weight,
                    "is_enabled": new_entry.is_enabled,
                    "category_uid": category_uid,
                    "keywords": keyword_list
                }
            }
        except Exception as e:
            session.rollback()
            import traceback
            error_details = traceback.format_exc()
            print(f"创建条目时发生错误: {str(e)}")
            print(f"错误详情: {error_details}")
            return {
                "success": False,
                "message": f"创建条目失败: {str(e)}"
            }
        finally:
            session.close()
    
    async def delete_category(self, category_uid: str) -> Dict[str, Any]:
        """
        删除一个大类及其所有条目
        
        Args:
            category_uid: 大类的UID
            
        Returns:
            Dict: 包含删除结果的字典
        """
        # 导入同步会话
        from sqlalchemy.orm import Session
        from llm.database.db_tools import engine
        
        # 创建同步会话
        session = Session(engine)
        try:
            # 检查大类是否存在
            category = session.query(PreciseCategory).filter(PreciseCategory.uid == category_uid).one_or_none()
            
            if not category:
                return {
                    "success": False,
                    "message": f"大类不存在: {category_uid}"
                }
            
            # 删除大类（相关条目将级联删除）
            session.delete(category)
            session.commit()
            
            return {
                "success": True,
                "message": f"删除大类成功: {category_uid}"
            }
        except Exception as e:
            session.rollback()
            import traceback
            error_details = traceback.format_exc()
            print(f"删除大类时发生错误: {str(e)}")
            print(f"错误详情: {error_details}")
            return {
                "success": False,
                "message": f"删除大类失败: {str(e)}"
            }
        finally:
            session.close()
    
    async def update_category(self, category_uid: str, new_name: str) -> Dict[str, Any]:
        """
        更新大类名称
        
        Args:
            category_uid: 大类的UID
            new_name: 新的大类名称
            
        Returns:
            Dict: 包含更新结果的字典
        """
        async with AsyncSessionLocal() as session:
            # 检查大类是否存在
            category = await session.execute(
                select(PreciseCategory).where(PreciseCategory.uid == category_uid)
            )
            category = category.scalar_one_or_none()
            
            if not category:
                return {
                    "success": False,
                    "message": f"大类不存在: {category_uid}"
                }
            
            # 更新大类名称
            category.name = new_name
            await session.commit()
            
            return {
                "success": True,
                "message": f"更新大类名称成功: {new_name}",
                "data": {
                    "id": category.id,
                    "name": category.name,
                    "uid": category.uid
                }
            }
    
    async def update_entry(
        self, 
        entry_uid: str, 
        description: Optional[str] = None, 
        content: Optional[str] = None, 
        keywords: Optional[List[str]] = None,
        weight: Optional[int] = None, 
        is_enabled: Optional[bool] = None
    ) -> Dict[str, Any]:
        """
        更新条目信息
        
        Args:
            entry_uid: 条目的UID
            description: 新的条目描述
            content: 新的条目内容
            keywords: 新的匹配关键词列表
            weight: 新的条目权重 (0-99)
            is_enabled: 是否启用
            
        Returns:
            Dict: 包含更新结果的字典
        """
        async with AsyncSessionLocal() as session:
            # 检查条目是否存在
            entry_query = select(PreciseEntry).options(
                selectinload(PreciseEntry.keywords),
                selectinload(PreciseEntry.category)
            ).where(PreciseEntry.uid == entry_uid)
            
            entry_result = await session.execute(entry_query)
            entry = entry_result.scalar_one_or_none()
            
            if not entry:
                return {
                    "success": False,
                    "message": f"条目不存在: {entry_uid}"
                }
            
            # 更新条目信息
            if description is not None:
                entry.description = description
                
            if content is not None:
                entry.content = content
                
            if weight is not None:
                # 规范化权重值
                if weight < 0:
                    weight = 0
                elif weight > 99:
                    weight = 99
                entry.weight = weight
                
            if is_enabled is not None:
                entry.is_enabled = is_enabled
                
            # 更新关键词
            if keywords is not None:
                # 清除现有关键词关联
                entry.keywords = []
                
                # 添加新关键词
                for keyword_text in keywords:
                    # 查找现有关键词或创建新关键词
                    keyword = await session.execute(
                        select(PreciseKeyword).where(PreciseKeyword.keyword == keyword_text)
                    )
                    keyword = keyword.scalar_one_or_none()
                    
                    if not keyword:
                        keyword = PreciseKeyword(keyword=keyword_text)
                        session.add(keyword)
                    
                    # 关联关键词和条目
                    entry.keywords.append(keyword)
            
            await session.commit()
            await session.refresh(entry)
            
            # 准备返回的关键词列表
            keyword_list = [k.keyword for k in entry.keywords]
            
            return {
                "success": True,
                "message": f"更新条目成功: {entry.description}",
                "data": {
                    "id": entry.id,
                    "uid": entry.uid,
                    "description": entry.description,
                    "content": entry.content,
                    "weight": entry.weight,
                    "is_enabled": entry.is_enabled,
                    "category_uid": entry.category.uid,
                    "keywords": keyword_list
                }
            }
    
    async def list_categories(self) -> Dict[str, Any]:
        """
        列出所有大类
        
        Returns:
            Dict: 包含大类列表的字典
        """
        # 导入同步会话
        from sqlalchemy.orm import Session
        from llm.database.db_tools import engine
        
        # 创建同步会话
        session = Session(engine)
        try:
            categories = session.query(PreciseCategory).all()
            
            category_list = [
                {
                    "id": cat.id,
                    "name": cat.name,
                    "uid": cat.uid
                }
                for cat in categories
            ]
            
            return {
                "success": True,
                "message": f"获取大类列表成功，共 {len(category_list)} 个大类",
                "data": {
                    "categories": category_list
                }
            }
        except Exception as e:
            import traceback
            error_details = traceback.format_exc()
            print(f"列出大类时发生错误: {str(e)}")
            print(f"错误详情: {error_details}")
            return {
                "success": False,
                "message": f"获取大类列表失败: {str(e)}",
                "data": {
                    "categories": []
                }
            }
        finally:
            session.close()
    
    async def list_entries(self, category_uid: str) -> Dict[str, Any]:
        """
        列出指定大类下的所有条目
        
        Args:
            category_uid: 大类的UID
            
        Returns:
            Dict: 包含条目列表的字典
        """
        # 导入同步会话
        from sqlalchemy.orm import Session
        from llm.database.db_tools import engine
        
        # 创建同步会话
        session = Session(engine)
        try:
            # 检查大类是否存在
            category = session.query(PreciseCategory).filter(
                PreciseCategory.uid == category_uid
            ).one_or_none()
            
            if not category:
                return {
                    "success": False,
                    "message": f"大类不存在: {category_uid}"
                }
            
            # 获取大类下的所有条目
            entries = session.query(PreciseEntry).options(
                selectinload(PreciseEntry.keywords)
            ).filter(
                PreciseEntry.category_id == category.id
            ).all()
            
            entry_list = []
            for entry in entries:
                keyword_list = [k.keyword for k in entry.keywords]
                
                entry_list.append({
                    "id": entry.id,
                    "uid": entry.uid,
                    "description": entry.description,
                    "content": entry.content,
                    "weight": entry.weight,
                    "is_enabled": entry.is_enabled,
                    "keywords": keyword_list
                })
            
            return {
                "success": True,
                "message": f"获取条目列表成功，共 {len(entry_list)} 个条目",
                "data": {
                    "category": {
                        "id": category.id,
                        "name": category.name,
                        "uid": category.uid
                    },
                    "entries": entry_list
                }
            }
        except Exception as e:
            import traceback
            error_details = traceback.format_exc()
            print(f"列出条目时发生错误: {str(e)}")
            print(f"错误详情: {error_details}")
            return {
                "success": False,
                "message": f"获取条目列表失败: {str(e)}",
                "data": {
                    "entries": []
                }
            }
        finally:
            session.close()
            
    async def search(
        self, 
        query: str, 
        category_uids: Optional[List[str]] = None, 
        max_results: int = 5,
        search_depth: int = 2
    ) -> Dict[str, Any]:
        """
        在精确查询库中搜索匹配的条目，使用有限自动机方式进行匹配
        
        Args:
            query: 用户查询文本
            category_uids: 搜索范围限定的大类UID列表，为空则搜索所有大类
            max_results: 最大返回结果数
            search_depth: 搜索深度，1表示只进行一次关键词匹配，2表示进行二次关联匹配，3表示三次匹配，以此类推
            
        Returns:
            Dict: 包含搜索结果的字典
        """
        # 导入同步会话
        from sqlalchemy.orm import Session
        from llm.database.db_tools import engine
        
        # 创建同步会话
        session = Session(engine)
        try:
            # 预处理查询文本 - 移除特殊字符，转换为小写
            processed_query = ''.join(c for c in query if c.isalnum() or c.isspace() or ord(c) > 127)
            
            # 记录请求信息
            print(f"搜索请求: {query}, 处理后: {processed_query}, 类别限制: {category_uids}, 搜索深度: {search_depth}")
            
            # 存储结果和已处理的条目ID
            results = []
            processed_entry_ids = set()  # 用于跟踪已处理的条目ID，避免重复添加
            
            # 确定搜索范围
            category_filter = None
            if category_uids:
                # 如果指定了大类，先获取大类ID
                category_ids = session.query(PreciseCategory.id).filter(
                    PreciseCategory.uid.in_(category_uids)
                ).all()
                category_ids = [c[0] for c in category_ids]
                
                if not category_ids:
                    return {
                        "success": True,
                        "message": "未找到指定的大类",
                        "data": {
                            "entries": []
                        }
                    }
            
            # 获取所有可能的关键词
            all_keywords = session.query(PreciseKeyword).all()
            keyword_dict = {kw.keyword: kw.id for kw in all_keywords}
            
            # 初始关键词匹配
            matched_keyword_ids = set()
            
            # 检查关键词是否在查询中出现
            for keyword in keyword_dict:
                if keyword in processed_query:
                    matched_keyword_ids.add(keyword_dict[keyword])
            
            # 保存每一层的匹配结果
            all_matched_ids_by_level = {1: matched_keyword_ids}
            
            # 初始查询深度
            current_depth = 1
            
            # 开始搜索循环
            while current_depth <= search_depth and all_matched_ids_by_level[current_depth]:
                # 当前深度的关键词ID集合
                current_matched_ids = all_matched_ids_by_level[current_depth]
                
                # 如果有匹配的关键词，获取相关条目
                query_obj = session.query(PreciseEntry).options(
                    selectinload(PreciseEntry.keywords),
                    selectinload(PreciseEntry.category)
                ).join(
                    PreciseEntry.keywords
                ).filter(
                    PreciseKeyword.id.in_(current_matched_ids)
                )
                
                if category_uids:
                    query_obj = query_obj.filter(PreciseEntry.category_id.in_(category_ids))
                
                query_obj = query_obj.filter(PreciseEntry.is_enabled == True)
                matched_entries = query_obj.all()
                
                # 处理匹配的条目，跟踪已处理ID避免重复
                new_entries_this_level = []  # 本层级新增的条目，用于下一级关联
                
                for entry in matched_entries:
                    if entry.id not in processed_entry_ids:
                        processed_entry_ids.add(entry.id)  # 标记为已处理
                        entry_data = {
                            "category": {
                                "name": entry.category.name,
                                "uid": entry.category.uid
                            },
                            "entry": {
                                "uid": entry.uid,
                                "description": entry.description,
                                "content": entry.content,
                                "weight": entry.weight,
                                "match_level": current_depth  # 记录匹配级别
                            }
                        }
                        results.append(entry_data)
                        new_entries_this_level.append(entry_data)  # 添加到本层级新增条目
                
                # 如果还需要继续搜索更深层次
                if current_depth < search_depth:
                    # 从已匹配条目中提取内容用于下一层匹配
                    next_level_entries = set()
                    # 仅使用当前层级新增的条目来寻找下一层关联，避免重复
                    
                    for result in new_entries_this_level:
                        content = result["entry"]["content"]
                        
                        # 对于每个关键词，检查是否在内容中
                        for keyword, keyword_id in keyword_dict.items():
                            # 避免重复匹配已匹配过的关键词
                            if keyword_id not in sum([list(ids) for ids in all_matched_ids_by_level.values()], []) and keyword in content:
                                next_level_entries.add(keyword_id)
                    
                    # 准备下一深度的匹配
                    current_depth += 1
                    all_matched_ids_by_level[current_depth] = next_level_entries
                    
                    print(f"深度 {current_depth}: 找到 {len(next_level_entries)} 个关键词匹配")
                else:
                    # 达到最大深度，退出循环
                    break
            
            # 根据权重排序结果
            # 同等权重下，优先展示匹配级别较低的条目（直接匹配优先）
            results.sort(key=lambda x: (x["entry"]["weight"], x["entry"]["match_level"]), reverse=True)
            
            # 限制返回数量
            if max_results > 0:
                results = results[:max_results]
            
            # 提取纯文本内容
            precise_content = "\n\n".join([result["entry"]["content"] for result in results])
            
            return {
                "success": True,
                "message": f"搜索成功，找到 {len(results)} 个匹配条目",
                "data": {
                    "query": str(query),  # 确保使用字符串而不是Query对象
                    "precise_content": precise_content,
                    "entries": results
                }
            }
        except Exception as e:
            import traceback
            error_details = traceback.format_exc()
            print(f"搜索时发生错误: {str(e)}")
            print(f"错误详情: {error_details}")
            return {
                "success": False,
                "message": f"搜索失败: {str(e)}",
                "data": {
                    "entries": []
                }
            }
        finally:
            session.close() 