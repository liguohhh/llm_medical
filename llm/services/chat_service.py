from typing import Dict, List, Optional, AsyncGenerator, Tuple, Union, Any
from llm.services.model_service import ModelService
from llm.services.rag_service import RAGService
from llm.prompts.prompt_manager import PromptManager
import asyncio
import aiohttp

class ChatService:
    def __init__(self):
        self.model_service = ModelService()
        self.rag_service = RAGService()
        self.prompt_manager = PromptManager()

    async def _prepare_chat(
        self,
        message: str,
        model_settings: Dict,
        template_id: str,
        sub_template_ids: List[str],
        params: Dict[str, str],
        history: Optional[List[Dict[str, str]]] = None,
        vector_search_config: Optional[Union[Dict[str, Any], Any]] = None,
        precise_search_config: Optional[Union[Dict[str, Any], Any]] = None
    ) -> tuple[str, Dict[str, str], List]:
        """
        准备聊天所需的参数
        
        Args:
            message: 用户消息
            model_settings: 模型设置
            template_id: 大模板ID
            sub_template_ids: 要使用的子模板ID列表 (已废弃，将使用大模板中的所有子模板)
            params: 提示词参数
            history: 历史记录
            vector_search_config: 向量搜索配置，可以是字典或Pydantic模型
            precise_search_config: 精确查询配置，可以是字典或Pydantic模型
            
        Returns:
            tuple[str, Dict[str, str], List]: (格式化的提示词, RAG内容字典, 消息历史)
        """
        # 提取向量搜索参数
        vector_namespaces = None
        vector_n_results = 5
        vector_metadata_filter = None
        vector_rag_history_count = 1
        
        if vector_search_config:
            # 检查vector_search_config是否是字典类型
            if isinstance(vector_search_config, dict):
                vector_namespaces = vector_search_config.get('namespaces')
                vector_n_results = vector_search_config.get('n_results', 5)
                vector_metadata_filter = vector_search_config.get('metadata_filter')
                vector_rag_history_count = vector_search_config.get('rag_history_count', 1)
            else:
                # 假设是 Pydantic 模型，使用模型属性
                vector_namespaces = getattr(vector_search_config, 'namespaces', None)
                vector_n_results = getattr(vector_search_config, 'n_results', 5)
                vector_metadata_filter = getattr(vector_search_config, 'metadata_filter', None)
                vector_rag_history_count = getattr(vector_search_config, 'rag_history_count', 1)
        
        # 提取精确查询参数
        precise_categories = None
        precise_max_results = 5
        precise_search_depth = 2
        precise_rag_history_count = 1
        
        if precise_search_config:
            # 检查precise_search_config是否是字典类型
            if isinstance(precise_search_config, dict):
                precise_categories = precise_search_config.get('categories')
                precise_max_results = precise_search_config.get('max_results', 5)
                precise_search_depth = precise_search_config.get('search_depth', 2)
                precise_rag_history_count = precise_search_config.get('rag_history_count', 1)
            else:
                # 假设是 Pydantic 模型，使用模型属性
                precise_categories = getattr(precise_search_config, 'categories', None)
                precise_max_results = getattr(precise_search_config, 'max_results', 5)
                precise_search_depth = getattr(precise_search_config, 'search_depth', 2)
                precise_rag_history_count = getattr(precise_search_config, 'rag_history_count', 1)
        
        # 构建用于RAG的查询文本
        rag_query_text = message
        
        # 处理历史记录，如果需要的话
        if history and (vector_rag_history_count > 1 or precise_rag_history_count > 1):
            # 确定要使用的最大历史消息数量
            max_history_count = max(vector_rag_history_count, precise_rag_history_count) - 1
            
            # 仅保留指定数量的历史消息
            relevant_history = history[-max_history_count:] if max_history_count < len(history) else history
            
            # 构建完整查询文本，向量搜索和精确查询可能使用不同数量的历史消息
            vector_query_text = message
            precise_query_text = message
            
            if vector_rag_history_count > 1:
                # 计算向量搜索需要的历史消息数
                vector_history_count = min(len(relevant_history), vector_rag_history_count - 1)
                
                # 提取并拼接历史消息
                if vector_history_count > 0:
                    vector_history_msgs = relevant_history[-vector_history_count:]
                    vector_history_text = " ".join([f"{msg.get('role', 'user')}: {msg.get('content', '')}" for msg in vector_history_msgs])
                    vector_query_text = vector_history_text + " user: " + message
            
            if precise_rag_history_count > 1:
                # 计算精确查询需要的历史消息数
                precise_history_count = min(len(relevant_history), precise_rag_history_count - 1)
                
                # 提取并拼接历史消息
                if precise_history_count > 0:
                    precise_history_msgs = relevant_history[-precise_history_count:]
                    precise_history_text = " ".join([f"{msg.get('role', 'user')}: {msg.get('content', '')}" for msg in precise_history_msgs])
                    precise_query_text = precise_history_text + " user: " + message
        else:
            # 不使用历史消息，只使用当前消息
            vector_query_text = message
            precise_query_text = message
                
        # 获取RAG内容
        vector_content, precise_content = await self.rag_service.get_rag_content(
            vector_query=vector_query_text if vector_rag_history_count > 1 else message,
            precise_query=precise_query_text if precise_rag_history_count > 1 else message,
            namespaces=vector_namespaces,
            n_results=vector_n_results,
            metadata_filter=vector_metadata_filter,
            precise_categories=precise_categories,
            precise_max_results=precise_max_results,
            precise_search_depth=precise_search_depth
        )
        
        # 合并提示词参数 - 添加默认参数
        merged_params = {
            "question": message,
            "vector_content": vector_content,
            "precise_content": precise_content,
            **params
        }
        
        # 格式化选定的小模板并拼接
        # 注意：虽然传入了sub_template_ids，但prompt_manager已修改为忽略此参数，会自动使用大模板中的所有子模板
        final_prompt = self.prompt_manager.format_selected_templates(
            template_id, 
            sub_template_ids, 
            **merged_params
        )
        
        if not final_prompt:
            raise ValueError(f"Failed to format templates from {template_id}")
        
        # 处理历史记录为OpenAI格式
        messages = []
        if history:
            # 添加历史消息
            for item in history:
                role = item.get("role", "user")
                content = item.get("content", "")
                if role and content:
                    messages.append({"role": role, "content": content})
                    
        # 添加当前提示词作为最新的用户消息
        messages.append({"role": "user", "content": final_prompt})
        
        # 返回格式化的提示词、RAG内容字典和消息历史
        rag_contents = {
            "vector_content": vector_content,
            "precise_content": precise_content
        }
        
        return final_prompt, rag_contents, messages

    async def process_chat(
        self,
        message: str,
        model_settings,
        template_id: str,
        sub_template_ids: List[str],
        params: Dict[str, str],
        history: Optional[List[Dict[str, str]]] = None,
        vector_search_config: Optional[Union[Dict[str, Any], Any]] = None,
        precise_search_config: Optional[Union[Dict[str, Any], Any]] = None
    ) -> Dict[str, str]:
        """
        处理标准聊天请求
        
        Args:
            message: 用户消息
            model_settings: 模型设置
            template_id: 大模板ID
            sub_template_ids: 要使用的子模板ID列表 (已废弃，将使用大模板中的所有子模板)
            params: 提示词参数
            history: 历史记录
            vector_search_config: 向量搜索配置，可以是字典或Pydantic模型
            precise_search_config: 精确查询配置，可以是字典或Pydantic模型
            
        Returns:
            Dict[str, str]: 包含回答和RAG内容的响应
        """
        try:
            # 准备聊天参数
            prompt, rag_contents, messages = await self._prepare_chat(
                message, model_settings, template_id, sub_template_ids, params, history, 
                vector_search_config, precise_search_config
            )
            
            # 调用模型 - 传递完整消息历史
            response = await self.model_service.call_model(messages, model_settings)
            
            return {
                "answer": response,
                **rag_contents  # 展开RAG内容字典到响应中
            }
        except Exception as e:
            raise Exception(f"Chat processing failed: {str(e)}")

    async def process_stream_chat(
        self,
        message: str,
        model_settings,
        template_id: str,
        sub_template_ids: List[str],
        params: Dict[str, str],
        history: Optional[List[Dict[str, str]]] = None,
        vector_search_config: Optional[Union[Dict[str, Any], Any]] = None,
        precise_search_config: Optional[Union[Dict[str, Any], Any]] = None
    ) -> AsyncGenerator[str, None]:
        """
        处理流式聊天请求
        
        Args:
            message: 用户消息
            model_settings: 模型设置
            template_id: 大模板ID
            sub_template_ids: 要使用的子模板ID列表 (已废弃，将使用大模板中的所有子模板)
            params: 提示词参数
            history: 历史记录
            vector_search_config: 向量搜索配置，可以是字典或Pydantic模型
            precise_search_config: 精确查询配置，可以是字典或Pydantic模型
            
        Returns:
            AsyncGenerator[str, None]: 流式响应生成器
        """
        try:
            # 准备聊天参数
            prompt, _, messages = await self._prepare_chat(
                message, model_settings, template_id, sub_template_ids, params, history, 
                vector_search_config, precise_search_config
            )
            
            # 调用模型流式接口 - 传递完整消息历史
            async for chunk in self.model_service.call_model_stream(messages, model_settings):
                try:
                    # 检查是否应该终止流式响应
                    if asyncio.current_task().cancelled():
                        print("流式请求被取消")
                        break
                        
                    # 将文本包装成SSE格式
                    yield f"data: {chunk}\n\n"
                except (aiohttp.ClientError, ConnectionResetError) as e:
                    print(f"连接已关闭: {str(e)}")
                    break
                except Exception as e:
                    print(f"发送数据时出错: {str(e)}")
                    break
                
        except asyncio.CancelledError:
            print("流式请求被取消")
            # 发送结束标记
            try:
                yield "data: [DONE]\n\n"
            except Exception:
                pass
        except Exception as e:
            print(f"流式请求错误: {str(e)}")
            # 发送错误信息
            try:
                yield f"data: [ERROR] {str(e)}\n\n"
            except Exception:
                pass
            raise 