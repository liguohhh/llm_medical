import asyncio
import sys
import os
import uuid
import aiohttp
import json
from pathlib import Path
import time
import traceback
from colorama import init, Fore, Style

# 初始化colorama
init()

class TestAll:
    def __init__(self):
        self.base_url = "http://localhost:8000/api/v1"
        self.test_namespace = "medical_test"
        self.test_category_uid = None
        self.test_entry_uids = []
        self.test_doc_id = None
        self.test_file_path = None

    async def clear_test_data(self):
        """清理所有测试数据"""
        print(f"\n{Fore.CYAN}清理测试数据...{Style.RESET_ALL}")
        
        # 清理向量数据库
        try:
            async with aiohttp.ClientSession() as session:
                url = f"{self.base_url}/vectors/namespace/{self.test_namespace}"
                async with session.delete(url) as response:
                    if response.status == 200:
                        result = await response.json()
                        print(f"{Fore.GREEN}清理命名空间成功: {result['message']}{Style.RESET_ALL}")
        except Exception as e:
            print(f"{Fore.YELLOW}清理命名空间时出现异常 (继续执行): {str(e)}{Style.RESET_ALL}")

        # 清理精确查询数据
        try:
            async with aiohttp.ClientSession() as session:
                url = f"{self.base_url}/precise/categories"
                async with session.get(url) as response:
                    if response.status == 200:
                        result = await response.json()
                        for category in result["data"]["categories"]:
                            delete_url = f"{self.base_url}/precise/category/{category['uid']}"
                            async with session.delete(delete_url) as delete_response:
                                if delete_response.status == 200:
                                    print(f"{Fore.GREEN}删除大类成功: {category['name']}{Style.RESET_ALL}")
        except Exception as e:
            print(f"{Fore.YELLOW}清理精确查询数据时出现异常 (继续执行): {str(e)}{Style.RESET_ALL}")

        # 清理模板数据
        try:
            async with aiohttp.ClientSession() as session:
                url = f"{self.base_url}/templates/medical_template"
                async with session.delete(url) as response:
                    if response.status == 200:
                        print(f"{Fore.GREEN}删除模板成功{Style.RESET_ALL}")
        except Exception as e:
            print(f"{Fore.YELLOW}清理模板数据时出现异常 (继续执行): {str(e)}{Style.RESET_ALL}")

    async def test_template_management(self):
        """测试模板管理功能"""
        print(f"\n{Fore.CYAN}测试模板管理功能...{Style.RESET_ALL}")
        
        try:
            async with aiohttp.ClientSession() as session:
                # 1. 创建医疗大模板
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
                        }
                    }
                }
                
                print(f"{Fore.CYAN}创建医疗大模板...{Style.RESET_ALL}")
                async with session.post(
                    f"{self.base_url}/templates/medical_template",
                    json=medical_template
                ) as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}创建大模板结果:{Style.RESET_ALL}", result)
                
                # 2. 获取所有大模板
                print(f"\n{Fore.CYAN}获取所有大模板...{Style.RESET_ALL}")
                async with session.get(f"{self.base_url}/templates") as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}所有大模板:{Style.RESET_ALL}", result)
                
                # 3. 获取医疗大模板详情
                print(f"\n{Fore.CYAN}获取医疗大模板详情...{Style.RESET_ALL}")
                async with session.get(f"{self.base_url}/templates/medical_template") as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}医疗大模板详情:{Style.RESET_ALL}")
                    print(f"  描述: {result['data']['description']}")
                    print(f"  子模板数量: {len(result['data']['sub_templates'])}")
                    for sub_id, sub_template in result['data']['sub_templates'].items():
                        print(f"  - {sub_id}: {sub_template['description']}")
                
                return True
        except Exception as e:
            print(f"{Fore.RED}模板管理测试失败: {str(e)}{Style.RESET_ALL}")
            return False

    async def test_vector_management(self):
        """测试向量数据库管理功能"""
        print(f"\n{Fore.CYAN}测试向量数据库管理功能...{Style.RESET_ALL}")
        
        try:
            # 1. 创建测试文件
            test_dir = Path(__file__).parent
            self.test_file_path = test_dir / "test_data.txt"
            
            with open(self.test_file_path, "w", encoding="utf-8") as f:
                f.write("""
        头痛是一种常见的身体不适，可能由多种原因引起。
        
        常见原因包括：
        1. 紧张性头痛：最常见的头痛类型，通常与压力、焦虑和肌肉紧张有关。
        2. 偏头痛：特点是反复发作的中度至重度疼痛，通常伴有恶心、呕吐和对光、声音敏感。
        3. 丛集性头痛：极其剧烈的头痛，通常发生在眼睛周围，持续时间短但极为痛苦。
        4. 鼻窦炎：鼻窦感染可导致额头和面部疼痛，尤其在弯腰时加重。
        5. 荷尔蒙变化：例如月经期间的头痛。
        6. 脱水：缺水会导致头痛。
        7. 饮食因素：某些食物和饮料（如红酒、奶酪和某些添加剂）可能触发头痛。
        8. 睡眠不足或过多：睡眠模式改变可能引起头痛。
        9. 药物副作用：某些药物可能导致头痛作为副作用。
        10. 颈部或头部受伤：创伤后头痛在事故或伤害后很常见。
        
        严重情况（需要紧急医疗干预）：
        - 突然、极其剧烈的"雷击样"头痛
        - 伴有发热、颈部僵硬、精神混乱的头痛
        - 伴有虚弱、麻木、说话困难或视力问题的头痛
        - 头部受伤后的头痛
        - 持续不退且不同于以前经历的头痛
        
        治疗方法取决于头痛类型和严重程度，可能包括：
        - 非处方止痛药（如对乙酰氨基酚、布洛芬）
        - 处方药物
        - 生活方式改变（如减轻压力、保持规律作息）
        - 按摩或理疗
        """)
            
            # 2. 上传文件
            print(f"{Fore.CYAN}上传测试文件...{Style.RESET_ALL}")
            async with aiohttp.ClientSession() as session:
                data = aiohttp.FormData()
                data.add_field('file', open(self.test_file_path, 'rb'), filename='test_data.txt')
                data.add_field('namespace', self.test_namespace)
                data.add_field('chunk_size', '500')
                data.add_field('chunk_overlap', '100')
                
                async with session.post(f"{self.base_url}/vectors/upload", data=data) as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}上传结果:{Style.RESET_ALL}", result)
                    self.test_doc_id = result['data']['metadata']['doc_id']
            
            # 3. 列出命名空间
            print(f"\n{Fore.CYAN}列出命名空间...{Style.RESET_ALL}")
            async with aiohttp.ClientSession() as session:
                async with session.get(f"{self.base_url}/vectors/namespaces") as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}命名空间列表:{Style.RESET_ALL}", result)
            
            # 4. 列出文档
            print(f"\n{Fore.CYAN}列出文档...{Style.RESET_ALL}")
            async with aiohttp.ClientSession() as session:
                async with session.get(f"{self.base_url}/vectors/documents/{self.test_namespace}") as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}文档列表:{Style.RESET_ALL}", result)
            
            return True
        except Exception as e:
            print(f"{Fore.RED}向量数据库管理测试失败: {str(e)}{Style.RESET_ALL}")
            return False

    async def test_precise_management(self):
        """测试精确查询管理功能"""
        print(f"\n{Fore.CYAN}测试精确查询管理功能...{Style.RESET_ALL}")
        
        try:
            # 1. 创建大类
            print(f"{Fore.CYAN}创建大类...{Style.RESET_ALL}")
            async with aiohttp.ClientSession() as session:
                data = {"name": "头痛类"}
                async with session.post(f"{self.base_url}/precise/category", json=data) as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}创建大类结果:{Style.RESET_ALL}", result)
                    self.test_category_uid = result['data']['uid']
            
            # 2. 创建条目
            print(f"\n{Fore.CYAN}创建条目...{Style.RESET_ALL}")
            entries_data = [
                {
                    "description": "偏头痛",
                    "content": "偏头痛是一种常见的头痛类型，特点是反复发作的中度至重度疼痛...",
                    "keywords": ["偏头痛", "头痛","片侧头痛", "血管性头痛", "恶心", "呕吐"],
                    "weight": 85,
                    "is_enabled": True
                },
                {
                    "description": "紧张性头痛",
                    "content": "紧张性头痛是最常见的头痛类型，通常表现为头部两侧的钝痛或压迫感...",
                    "keywords": ["紧张性头痛", "压力性头痛", "肌肉紧张", "压迫感"],
                    "weight": 80,
                    "is_enabled": True
                }
            ]
            
            for entry_data in entries_data:
                async with aiohttp.ClientSession() as session:
                    async with session.post(
                        f"{self.base_url}/precise/entry/{self.test_category_uid}",
                        json=entry_data
                    ) as response:
                        result = await response.json()
                        print(f"{Fore.GREEN}创建条目结果:{Style.RESET_ALL}", result)
                        self.test_entry_uids.append(result['data']['uid'])
            
            # 3. 列出所有大类
            print(f"\n{Fore.CYAN}列出所有大类...{Style.RESET_ALL}")
            async with aiohttp.ClientSession() as session:
                async with session.get(f"{self.base_url}/precise/categories") as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}大类列表:{Style.RESET_ALL}", result)
            
            # 4. 列出条目
            print(f"\n{Fore.CYAN}列出条目...{Style.RESET_ALL}")
            async with aiohttp.ClientSession() as session:
                async with session.get(f"{self.base_url}/precise/entries/{self.test_category_uid}") as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}条目列表:{Style.RESET_ALL}", result)
            
            return True
        except Exception as e:
            print(f"{Fore.RED}精确查询管理测试失败: {str(e)}{Style.RESET_ALL}")
            return False

    async def test_chat(self):
        """测试聊天功能"""
        print(f"\n{Fore.CYAN}测试聊天功能...{Style.RESET_ALL}")
        
        try:
            async with aiohttp.ClientSession() as session:
                # 1. 标准问答测试
                print(f"{Fore.CYAN}测试标准问答...{Style.RESET_ALL}")
                chat_data = {
                    "message": "我最近头痛得厉害，该怎么办？",
                    "model_settings": {
                        "model_name": "deepseek-chat",
                        "api_key": "sk-e8def07773ea45dd9da0d9a1197b7e63",
                        "base_url": "https://api.deepseek.com/v1"
                    },
                    "template_config": {
                        "template_id": "medical_template",
                        "sub_template_ids": ["基础信息", "参考内容"],
                        "params": {
                            "patient_age": "30",
                            "patient_gender": "男",
                            "symptoms": "头痛"
                        }
                    },
                    "vector_search_config": {
                        "namespaces": [self.test_namespace],
                        "n_results": 3
                    },
                    "precise_search_config": {
                        "categories": [self.test_category_uid],
                        "max_results": 3,
                        "search_depth": 2
                    }
                }
                
                async with session.post(f"{self.base_url}/ask", json=chat_data) as response:
                    result = await response.json()
                    print(f"{Fore.GREEN}标准问答结果:{Style.RESET_ALL}")
                    print(f"回答: {result['answer']}")
                    print(f"向量内容: {result['vector_content']}...")
                    print(f"精确内容: {result['precise_content']}...")
                
                # 2. 流式问答测试
                print(f"\n{Fore.CYAN}测试流式问答...{Style.RESET_ALL}")
                print(f"{Fore.YELLOW}回答:{Style.RESET_ALL}")
                
                async with session.post(f"{self.base_url}/stream", json=chat_data) as response:
                    if response.status != 200:
                        error_text = await response.text()
                        print(f"{Fore.RED}流式问答失败: {error_text}{Style.RESET_ALL}")
                        return False
                    
                    buffer = ""
                    async for line in response.content:
                        if line:
                            line = line.decode('utf-8').strip()
                            if line.startswith('data: '):
                                content = line[6:]
                                if content == "[DONE]":
                                    print(f"\n{Fore.GREEN}[完成]{Style.RESET_ALL}")
                                    break
                                elif content.startswith("[ERROR]"):
                                    print(f"\n{Fore.RED}{content}{Style.RESET_ALL}")
                                    break
                                elif content:
                                    buffer += content
                                    print(content, end="", flush=True)
                    
                    print(f"\n{Fore.GREEN}流式请求完成{Style.RESET_ALL}")
                
                return True
        except Exception as e:
            print(f"{Fore.RED}聊天功能测试失败: {str(e)}{Style.RESET_ALL}")
            return False

    async def run_all_tests(self):
        """运行所有测试"""
        print(f"{Fore.MAGENTA}===== 开始综合测试 ====={Style.RESET_ALL}")
        print(f"Python版本: {sys.version}")
        print(f"当前工作目录: {os.getcwd()}")
        
        try:
            # 0. 清理测试数据
            await self.clear_test_data()
            
            # 1. 测试模板管理
            if not await self.test_template_management():
                print(f"{Fore.RED}模板管理测试失败，终止测试{Style.RESET_ALL}")
                return
            
            # 2. 测试向量数据库管理
            if not await self.test_vector_management():
                print(f"{Fore.RED}向量数据库管理测试失败，终止测试{Style.RESET_ALL}")
                return
            
            # 3. 测试精确查询管理
            if not await self.test_precise_management():
                print(f"{Fore.RED}精确查询管理测试失败，终止测试{Style.RESET_ALL}")
                return
            
            # 4. 测试聊天功能
            if not await self.test_chat():
                print(f"{Fore.RED}聊天功能测试失败，终止测试{Style.RESET_ALL}")
                return
            
            # 5. 清理测试文件
            if self.test_file_path and os.path.exists(self.test_file_path):
                os.remove(self.test_file_path)
                print(f"\n{Fore.GREEN}已清理测试文件{Style.RESET_ALL}")
            
            print(f"\n{Fore.MAGENTA}===== 所有测试完成 ====={Style.RESET_ALL}")
        except Exception as e:
            print(f"{Fore.RED}测试过程中发生未捕获的错误: {str(e)}{Style.RESET_ALL}")
            print(f"{Fore.RED}错误详情:\n{traceback.format_exc()}{Style.RESET_ALL}")

async def main():
    tester = TestAll()
    await tester.run_all_tests()

if __name__ == "__main__":
    asyncio.run(main()) 