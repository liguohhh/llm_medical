import asyncio
import json
import aiohttp
import sseclient
from pathlib import Path
from colorama import init, Fore, Style

# 初始化colorama
init()

async def test_template_management():
    """测试模板管理功能"""
    async with aiohttp.ClientSession() as session:
        # 1. 创建一个医疗大模板
        medical_template = {
            "description": "医疗问诊大模板",
            "sub_templates": {
                "基础信息": {
                    "template": """你是一个专业的线上医生，请基于医学知识与提供的参考内容回答患者问题。
患者信息：年龄{patient_age}，性别{patient_gender}，症状{symptoms}。
问题：{question}""",
                    "description": "医疗问答基础模板",
                    "parameters": ["patient_age", "patient_gender", "symptoms", "question"]
                },
                "参考内容": {
                    "template": """## 参考医学知识

{vector_content}

## 精确匹配内容

{precise_content}""",
                    "description": "参考内容模板",
                    "parameters": ["vector_content", "precise_content"]
                },
                "回答格式": {
                    "template": """## 回答要求

1. 请直接回答患者问题，不要重复问题
2. 使用通俗易懂的语言
3. 避免使用太多专业术语
4. 如果涉及用药，请说明用法用量和注意事项
5. 如果情况严重，建议患者及时就医""",
                    "description": "回答格式模板",
                    "parameters": []
                }
            }
        }
        
        print(f"\n{Fore.CYAN}创建医疗大模板...{Style.RESET_ALL}")
        async with session.post(
            "http://localhost:8000/api/v1/templates/medical_template",
            json=medical_template
        ) as response:
            try:
                result = await response.json()
                print(f"{Fore.GREEN}创建大模板结果:{Style.RESET_ALL}", result)
            except aiohttp.ContentTypeError:
                text = await response.text()
                print(f"{Fore.RED}错误: {text}{Style.RESET_ALL}")
                raise Exception(f"创建大模板失败: {text}")
            
        # 2. 获取所有大模板
        print(f"\n{Fore.CYAN}获取所有大模板...{Style.RESET_ALL}")
        async with session.get("http://localhost:8000/api/v1/templates") as response:
            try:
                result = await response.json()
                print(f"{Fore.GREEN}所有大模板:{Style.RESET_ALL}", result)
            except aiohttp.ContentTypeError:
                text = await response.text()
                print(f"{Fore.RED}错误: {text}{Style.RESET_ALL}")
                raise Exception(f"获取大模板列表失败: {text}")
                
        # 3. 获取医疗大模板详情
        print(f"\n{Fore.CYAN}获取医疗大模板详情...{Style.RESET_ALL}")
        async with session.get("http://localhost:8000/api/v1/templates/medical_template") as response:
            try:
                result = await response.json()
                print(f"{Fore.GREEN}医疗大模板详情:{Style.RESET_ALL}")
                print(f"  描述: {result['data']['description']}")
                print(f"  子模板数量: {len(result['data']['sub_templates'])}")
                for sub_id, sub_template in result['data']['sub_templates'].items():
                    print(f"  - {sub_id}: {sub_template['description']}")
            except aiohttp.ContentTypeError:
                text = await response.text()
                print(f"{Fore.RED}错误: {text}{Style.RESET_ALL}")
                raise Exception(f"获取大模板详情失败: {text}")
            
        return True

async def test_chat():
    """测试聊天功能"""
    async with aiohttp.ClientSession() as session:
        # 共享的基本请求数据
        base_settings = {
            "model_name": "deepseek-chat",
            "api_key": "sk-e8def07773ea45dd9da0d9a1197b7e63",
            "base_url": "https://api.deepseek.com/v1"
        }
        
        # 1. 使用完整小模板集测试
        print(f"\n{Fore.CYAN}测试使用完整小模板集...{Style.RESET_ALL}")
        full_template_data = {
            "message": "我最近头痛得厉害，该怎么办？",
            "model_settings": base_settings,
            "template_config": {
                "template_id": "medical_template",
                "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
                "params": {
                    "patient_age": "30",
                    "patient_gender": "男",
                    "symptoms": "头痛"
                }
            }
        }
        
        async with session.post(
            "http://localhost:8000/api/v1/ask",
            json=full_template_data
        ) as response:
            try:
                if response.status != 200:
                    text = await response.text()
                    print(f"{Fore.RED}错误 ({response.status}): {text}{Style.RESET_ALL}")
                    return
                    
                result = await response.json()
                print(f"{Fore.GREEN}完整模板问答响应:{Style.RESET_ALL}")
                print(f"{Fore.YELLOW}回答:{Style.RESET_ALL} {result['answer']}")
                
                # 保存第一轮对话内容用于历史记录测试
                first_question = full_template_data["message"]
                first_answer = result["answer"]
            except Exception as e:
                print(f"{Fore.RED}请求处理出错: {str(e)}{Style.RESET_ALL}")
                return
        
        # 2. 使用部分小模板测试
        print(f"\n{Fore.CYAN}测试使用部分小模板...{Style.RESET_ALL}")
        partial_template_data = {
            "message": "我经常头晕，可能是什么问题？",
            "model_settings": base_settings,
            "template_config": {
                "template_id": "medical_template",
                "sub_template_ids": ["基础信息", "参考内容"],  # 只使用基础信息和参考内容，不使用回答格式
                "params": {
                    "patient_age": "45",
                    "patient_gender": "女",
                    "symptoms": "头晕"
                }
            }
        }
        
        async with session.post(
            "http://localhost:8000/api/v1/ask",
            json=partial_template_data
        ) as response:
            try:
                if response.status != 200:
                    text = await response.text()
                    print(f"{Fore.RED}错误 ({response.status}): {text}{Style.RESET_ALL}")
                    return
                    
                result = await response.json()
                print(f"{Fore.GREEN}部分模板问答响应:{Style.RESET_ALL}")
                print(f"{Fore.YELLOW}回答:{Style.RESET_ALL} {result['answer']}")
                print(f"{Fore.YELLOW}向量检索内容:{Style.RESET_ALL} {result['vector_content'][:100]}...")
                print(f"{Fore.YELLOW}精确匹配内容:{Style.RESET_ALL} {result['precise_content'][:100]}...")
                
                # 保存第二轮对话内容用于历史记录测试
                second_question = partial_template_data["message"]
                second_answer = result["answer"]
            except Exception as e:
                print(f"{Fore.RED}请求处理出错: {str(e)}{Style.RESET_ALL}")
                return
        
        # 3. 带历史记录测试
        print(f"\n{Fore.CYAN}测试带历史记录的问答...{Style.RESET_ALL}")
        history_template_data = {
            "message": "这种情况需要吃什么药？",
            "model_settings": base_settings,
            "template_config": {
                "template_id": "medical_template",
                "sub_template_ids": ["基础信息", "参考内容", "回答格式"],
                "params": {
                    "patient_age": "45",
                    "patient_gender": "女",
                    "symptoms": "头晕"
                }
            },
            "history": [
                {"role": "user", "content": second_question},
                {"role": "assistant", "content": second_answer}
            ]
        }
        
        async with session.post(
            "http://localhost:8000/api/v1/ask",
            json=history_template_data
        ) as response:
            try:
                if response.status != 200:
                    text = await response.text()
                    print(f"{Fore.RED}错误 ({response.status}): {text}{Style.RESET_ALL}")
                    return
                    
                result = await response.json()
                print(f"{Fore.GREEN}带历史记录的问答响应:{Style.RESET_ALL}")
                print(f"{Fore.YELLOW}回答:{Style.RESET_ALL} {result['answer']}")
            except Exception as e:
                print(f"{Fore.RED}请求处理出错: {str(e)}{Style.RESET_ALL}")
                return
            
        # 4. 流式问答测试
        print(f"\n{Fore.CYAN}测试流式问答...{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}回答:{Style.RESET_ALL}")
        
        try:
            print(" " * 4, end="", flush=True)  # 初始缩进
            
            async with session.post(
                "http://localhost:8000/api/v1/stream",
                json=full_template_data  # 使用完整模板数据
            ) as response:
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}错误 ({response.status}): {error_text}{Style.RESET_ALL}")
                    return
                    
                buffer = ""
                async for line in response.content:
                    if line:
                        line = line.decode('utf-8').strip()
                        if line.startswith('data: '):
                            content = line[6:]  # 移除 'data: ' 前缀
                            if content == "[DONE]":
                                print(f"\n{Fore.GREEN}[完成]{Style.RESET_ALL}")
                                break
                            elif content.startswith("[ERROR]"):
                                print(f"\n{Fore.RED}{content}{Style.RESET_ALL}")
                                break
                            elif content:
                                buffer += content
                                print(content, end="", flush=True)
                                
                                # 如果遇到句号、问号或感叹号，换行
                                if any(p in content for p in ["。", "？", "！", ".", "?", "!"]):
                                    print()
                                    print(" " * 4, end="", flush=True)  # 缩进
                                    buffer = ""
                
                print(f"\n{Fore.GREEN}流式请求完成{Style.RESET_ALL}")
        except Exception as e:
            print(f"\n{Fore.RED}流式请求错误: {str(e)}{Style.RESET_ALL}")
        finally:
            print(Style.RESET_ALL)

async def test_delete_template():
    """测试删除大模板"""
    async with aiohttp.ClientSession() as session:
        print(f"\n{Fore.CYAN}删除医疗大模板...{Style.RESET_ALL}")
        async with session.delete("http://localhost:8000/api/v1/templates/medical_template") as response:
            try:
                result = await response.json()
                print(f"{Fore.GREEN}删除大模板结果:{Style.RESET_ALL}", result)
            except aiohttp.ContentTypeError:
                text = await response.text()
                print(f"{Fore.RED}错误: {text}{Style.RESET_ALL}")

async def main():
    """主函数"""
    try:
        # 1. 测试模板管理
        try:
            success = await test_template_management()
            if not success:
                print(f"{Fore.RED}模板管理测试失败，终止测试{Style.RESET_ALL}")
                return
        except Exception as e:
            print(f"{Fore.RED}模板管理测试失败: {str(e)}{Style.RESET_ALL}")
            return
        
        # 2. 测试聊天功能
        await test_chat()
        
        # 3. 测试删除模板
        await test_delete_template()
        
    except Exception as e:
        print(f"{Fore.RED}测试过程中发生错误: {str(e)}{Style.RESET_ALL}")

if __name__ == "__main__":
    asyncio.run(main()) 