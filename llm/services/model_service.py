from typing import Dict, AsyncGenerator, List, Union
import openai
from pydantic import BaseModel

class ModelService:
    def __init__(self):
        self.client = None

    def _get_client(self, model_settings):
        """
        获取或创建OpenAI客户端
        
        Args:
            model_settings: 模型设置，可以是Dict或ModelConfig对象
            
        Returns:
            OpenAI: OpenAI客户端
        """
        # 将ModelConfig对象转换为字典
        if hasattr(model_settings, "model_dump"):
            # Pydantic v2
            config = model_settings.model_dump()
        elif hasattr(model_settings, "dict"):
            # Pydantic v1
            config = model_settings.dict()
        else:
            # 已经是字典
            config = model_settings
            
        api_key = config.get("api_key")
        base_url = config.get("base_url")
        
        # 创建OpenAI客户端
        return openai.OpenAI(
            api_key=api_key,
            base_url=base_url
        )

    async def call_model(self, messages_or_prompt: Union[List[Dict], str], model_settings) -> str:
        """
        调用模型
        
        Args:
            messages_or_prompt: 消息列表或单个提示词字符串
            model_settings: 模型设置
            
        Returns:
            str: 模型回答
        """
        try:
            # 获取客户端
            client = self._get_client(model_settings)
            
            # 将ModelConfig对象转换为字典
            if hasattr(model_settings, "model_dump"):
                config = model_settings.model_dump()
            elif hasattr(model_settings, "dict"):
                config = model_settings.dict()
            else:
                config = model_settings
            
            # 根据输入类型准备消息
            if isinstance(messages_or_prompt, str):
                # 如果是字符串，创建单个消息
                messages = [{"role": "user", "content": messages_or_prompt}]
            else:
                # 否则直接使用提供的消息列表
                messages = messages_or_prompt
                
            # 调用模型
            response = client.chat.completions.create(
                model=config.get("model_name"),
                messages=messages,
                stream=False
            )
            
            return response.choices[0].message.content
        except Exception as e:
            raise Exception(f"Model call failed: {str(e)}")

    async def call_model_stream(self, messages_or_prompt: Union[List[Dict], str], model_settings) -> AsyncGenerator[str, None]:
        """
        流式调用模型
        
        Args:
            messages_or_prompt: 消息列表或单个提示词字符串
            model_settings: 模型设置
            
        Returns:
            AsyncGenerator[str, None]: 流式结果
        """
        try:
            # 获取客户端
            client = self._get_client(model_settings)
            
            # 将ModelConfig对象转换为字典
            if hasattr(model_settings, "model_dump"):
                config = model_settings.model_dump()
            elif hasattr(model_settings, "dict"):
                config = model_settings.dict()
            else:
                config = model_settings
            
            # 根据输入类型准备消息
            if isinstance(messages_or_prompt, str):
                # 如果是字符串，创建单个消息
                messages = [{"role": "user", "content": messages_or_prompt}]
            else:
                # 否则直接使用提供的消息列表
                messages = messages_or_prompt
                
            # 调用模型
            response = client.chat.completions.create(
                model=config.get("model_name"),
                messages=messages,
                stream=True
            )
            
            for chunk in response:
                if chunk.choices and chunk.choices[0].delta.content:
                    yield chunk.choices[0].delta.content
        except Exception as e:
            raise Exception(f"Model stream call failed: {str(e)}") 