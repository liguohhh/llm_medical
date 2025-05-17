import asyncio
import sys
import os
import aiohttp
import json
from pathlib import Path
import time
import traceback
from colorama import init, Fore, Style

# 初始化colorama
init()

async def setup_test_data():
    """设置测试数据"""
    print(f"{Fore.CYAN}设置测试数据...{Style.RESET_ALL}")
    
    # 1. 创建测试文件
    test_dir = Path(__file__).parent
    test_file_path = test_dir / "test_history_data.txt"
    
    with open(test_file_path, "w", encoding="utf-8") as f:
        f.write("""
高血压（也称为原发性高血压或动脉高血压）是一种慢性疾病，特征是持续性动脉血压升高。

高血压的主要危险因素包括：
1. 年龄增长：随着年龄增长，高血压风险增加
2. 家族史：如果直系亲属患有高血压，风险增加
3. 超重或肥胖：体重过高增加高血压风险
4. 缺乏身体活动：久坐不动的生活方式增加风险
5. 吸烟和酒精：烟草使用和过量饮酒升高血压
6. 高钠饮食：过多盐分摄入导致血压升高
7. 压力大：长期处于压力下可能导致血压升高

高血压的治疗方法包括：
1. 生活方式改变：减少盐摄入、增加锻炼、限制酒精、控制体重、戒烟
2. 药物治疗：如利尿剂、ACE抑制剂、ARB、钙通道阻滞剂、β受体阻滞剂等
3. 定期监测：定期测量血压，调整治疗方案

高血压患者的饮食建议：
1. 低盐饮食：每日摄入不超过5-6克盐
2. 增加蔬果摄入：富含钾、镁、纤维的蔬菜水果
3. 限制酒精：男性每日不超过两杯，女性不超过一杯
4. 减少饱和脂肪：限制红肉、全脂乳制品等

高血压并发症包括：
1. 心脏疾病：心肌梗塞、心力衰竭、心房颤动
2. 脑部疾病：中风、痴呆
3. 肾脏疾病：肾功能衰竭
4. 眼部损伤：视网膜病变

血压控制目标：
- 一般人群：<140/90 mmHg
- 糖尿病或肾病患者：<130/80 mmHg
- 老年人（>65岁）：<150/90 mmHg
""")

    # 2. 创建大类和条目
    async with aiohttp.ClientSession() as session:
        # 创建大类
        data = {"name": "高血压类"}
        async with session.post("http://localhost:8000/api/v1/precise/category", json=data) as response:
            result = await response.json()
            if result["success"]:
                print(f"{Fore.GREEN}创建大类成功: {result['data']['name']}{Style.RESET_ALL}")
                category_uid = result['data']['uid']
            else:
                print(f"{Fore.RED}创建大类失败: {result['message']}{Style.RESET_ALL}")
                return None, None, None
        
        # 创建条目
        entries_data = [
            {
                "description": "高血压治疗",
                "content": "高血压的治疗方法包括：生活方式改变（减少盐摄入、增加锻炼、限制酒精、控制体重、戒烟）、药物治疗（如利尿剂、ACE抑制剂、ARB、钙通道阻滞剂、β受体阻滞剂等）、定期监测（定期测量血压，调整治疗方案）。",
                "keywords": ["高血压", "治疗", "药物", "生活方式", "监测"],
                "weight": 90,
                "is_enabled": True
            },
            {
                "description": "高血压并发症",
                "content": "高血压并发症包括：心脏疾病（心肌梗塞、心力衰竭、心房颤动）、脑部疾病（中风、痴呆）、肾脏疾病（肾功能衰竭）、眼部损伤（视网膜病变）。",
                "keywords": ["高血压", "并发症", "心脏疾病", "脑部疾病", "肾脏疾病"],
                "weight": 85,
                "is_enabled": True
            }
        ]
        
        for entry_data in entries_data:
            async with session.post(f"http://localhost:8000/api/v1/precise/entry/{category_uid}", json=entry_data) as response:
                result = await response.json()
                if result["success"]:
                    print(f"{Fore.GREEN}创建条目成功: {result['data']['description']}{Style.RESET_ALL}")
                else:
                    print(f"{Fore.RED}创建条目失败: {result['message']}{Style.RESET_ALL}")
        
        # 3. 上传向量文件
        test_namespace = "hypertension_test"
        data = aiohttp.FormData()
        data.add_field('file', open(test_file_path, 'rb'), filename='test_history_data.txt')
        data.add_field('namespace', test_namespace)
        data.add_field('chunk_size', '500')
        data.add_field('chunk_overlap', '100')
        
        async with session.post("http://localhost:8000/api/v1/vectors/upload", data=data) as response:
            result = await response.json()
            if "error" not in result:
                print(f"{Fore.GREEN}上传文件成功: {result['data']['metadata']['doc_id']}{Style.RESET_ALL}")
                doc_id = result['data']['metadata']['doc_id']
            else:
                print(f"{Fore.RED}上传文件失败: {result.get('error', '未知错误')}{Style.RESET_ALL}")
                return None, None, None
    
    return category_uid, test_namespace, test_file_path

async def cleanup_test_data(category_uid, test_namespace, test_file_path):
    """清理测试数据"""
    print(f"\n{Fore.CYAN}清理测试数据...{Style.RESET_ALL}")
    
    try:
        async with aiohttp.ClientSession() as session:
            # 删除大类
            if category_uid:
                async with session.delete(f"http://localhost:8000/api/v1/precise/category/{category_uid}") as response:
                    result = await response.json()
                    if result["success"]:
                        print(f"{Fore.GREEN}删除大类成功{Style.RESET_ALL}")
                    else:
                        print(f"{Fore.RED}删除大类失败: {result['message']}{Style.RESET_ALL}")
            
            # 删除命名空间
            if test_namespace:
                async with session.delete(f"http://localhost:8000/api/v1/vectors/namespace/{test_namespace}") as response:
                    result = await response.json()
                    if "error" not in result:
                        print(f"{Fore.GREEN}删除命名空间成功{Style.RESET_ALL}")
                    else:
                        print(f"{Fore.RED}删除命名空间失败: {result.get('error', '未知错误')}{Style.RESET_ALL}")
    except Exception as e:
        print(f"{Fore.RED}清理数据时出错: {str(e)}{Style.RESET_ALL}")
    
    # 删除测试文件
    if test_file_path and os.path.exists(test_file_path):
        os.remove(test_file_path)
        print(f"{Fore.GREEN}删除测试文件成功{Style.RESET_ALL}")

async def test_history_rag(category_uid, test_namespace):
    """测试历史消息RAG功能"""
    print(f"\n{Fore.CYAN}测试历史消息RAG功能...{Style.RESET_ALL}")
    
    # 创建模板
    async with aiohttp.ClientSession() as session:
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
        async with session.post("http://localhost:8000/api/v1/templates/rag_history_template", json=medical_template) as response:
            result = await response.json()
            print(f"{Fore.GREEN}创建大模板结果: {result['status']}{Style.RESET_ALL}")
    
    # 测试历史消息RAG
    try:
        async with aiohttp.ClientSession() as session:
            # 1. 只使用当前消息查询
            print(f"\n{Fore.CYAN}1. 测试只使用当前消息查询 (rag_history_count=1)...{Style.RESET_ALL}")
            chat_data = {
                "message": "高血压会导致哪些并发症？",
                "model_settings": {
                    "model_name": "deepseek-chat",
                    "api_key": "sk-e8def07773ea45dd9da0d9a1197b7e63",
                    "base_url": "https://api.deepseek.com/v1"
                },
                "template_config": {
                    "template_id": "rag_history_template",
                    "sub_template_ids": ["基础信息", "参考内容"],
                    "params": {
                        "patient_age": "55",
                        "patient_gender": "男",
                        "symptoms": "高血压"
                    }
                },
                "vector_search_config": {
                    "namespaces": [test_namespace],
                    "n_results": 3,
                    "rag_history_count": 1  # 只使用当前消息
                },
                "precise_search_config": {
                    "categories": [category_uid],
                    "max_results": 3,
                    "rag_history_count": 1  # 只使用当前消息
                }
            }
            
            async with session.post("http://localhost:8000/api/v1/ask", json=chat_data) as response:
                result = await response.json()
                print(f"{Fore.GREEN}查询结果:{Style.RESET_ALL}")
                print(f"精确匹配内容: {result['precise_content'][:200]}...")
                print(f"向量检索内容: {result['vector_content'][:200]}...")
            
            # 保存第一轮对话
            first_question = chat_data["message"]
            first_answer = result["answer"]
            
            # 2. 使用历史消息查询
            print(f"\n{Fore.CYAN}2. 测试使用历史消息查询 (rag_history_count=2)...{Style.RESET_ALL}")
            chat_data = {
                "message": "针对这些并发症有哪些预防措施？",
                "model_settings": {
                    "model_name": "deepseek-chat",
                    "api_key": "sk-e8def07773ea45dd9da0d9a1197b7e63",
                    "base_url": "https://api.deepseek.com/v1"
                },
                "template_config": {
                    "template_id": "rag_history_template",
                    "sub_template_ids": ["基础信息", "参考内容"],
                    "params": {
                        "patient_age": "55",
                        "patient_gender": "男",
                        "symptoms": "高血压"
                    }
                },
                "history": [
                    {"role": "user", "content": first_question},
                    {"role": "assistant", "content": first_answer}
                ],
                "vector_search_config": {
                    "namespaces": [test_namespace],
                    "n_results": 3,
                    "rag_history_count": 2  # 使用当前消息和1条历史消息
                },
                "precise_search_config": {
                    "categories": [category_uid],
                    "max_results": 3,
                    "rag_history_count": 2  # 使用当前消息和1条历史消息
                }
            }
            
            async with session.post("http://localhost:8000/api/v1/ask", json=chat_data) as response:
                result = await response.json()
                print(f"{Fore.GREEN}查询结果:{Style.RESET_ALL}")
                print(f"精确匹配内容: {result['precise_content'][:200]}...")
                print(f"向量检索内容: {result['vector_content'][:200]}...")
            
            # 保存第二轮对话
            second_question = chat_data["message"]
            second_answer = result["answer"]
            
            # 3. 使用更多历史消息查询
            print(f"\n{Fore.CYAN}3. 测试使用更多历史消息查询 (rag_history_count=3)...{Style.RESET_ALL}")
            chat_data = {
                "message": "副作用？",
                "model_settings": {
                    "model_name": "deepseek-chat",
                    "api_key": "sk-e8def07773ea45dd9da0d9a1197b7e63",
                    "base_url": "https://api.deepseek.com/v1"
                },
                "template_config": {
                    "template_id": "rag_history_template",
                    "sub_template_ids": ["基础信息", "参考内容"],
                    "params": {
                        "patient_age": "55",
                        "patient_gender": "男",
                        "symptoms": "高血压"
                    }
                },
                "history": [
                    {"role": "user", "content": first_question},
                    {"role": "assistant", "content": first_answer},
                    {"role": "user", "content": second_question},
                    {"role": "assistant", "content": second_answer}
                ],
                "vector_search_config": {
                    "namespaces": [test_namespace],
                    "n_results": 3,
                    "rag_history_count": 3  # 使用当前消息和2条历史消息
                },
                "precise_search_config": {
                    "categories": [category_uid],
                    "max_results": 3,
                    "rag_history_count": 3  # 使用当前消息和2条历史消息
                }
            }
            
            async with session.post("http://localhost:8000/api/v1/ask", json=chat_data) as response:
                result = await response.json()
                print(f"{Fore.GREEN}查询结果:{Style.RESET_ALL}")
                print(f"精确匹配内容: {result['precise_content'][:200]}...")
                print(f"向量检索内容: {result['vector_content'][:200]}...")
            
            # 4. 对照测试：使用相同问题但不使用历史消息
            print(f"\n{Fore.CYAN}4. 对照测试：相同问题但不使用历史消息 (rag_history_count=1)...{Style.RESET_ALL}")
            chat_data = {
                "message": "副作用？",
                "model_settings": {
                    "model_name": "deepseek-chat",
                    "api_key": "sk-e8def07773ea45dd9da0d9a1197b7e63",
                    "base_url": "https://api.deepseek.com/v1"
                },
                "template_config": {
                    "template_id": "rag_history_template",
                    "sub_template_ids": ["基础信息", "参考内容"],
                    "params": {
                        "patient_age": "55",
                        "patient_gender": "男",
                        "symptoms": "高血压"
                    }
                },
                "history": [
                    {"role": "user", "content": first_question},
                    {"role": "assistant", "content": first_answer},
                    {"role": "user", "content": second_question},
                    {"role": "assistant", "content": second_answer}
                ],
                "vector_search_config": {
                    "namespaces": [test_namespace],
                    "n_results": 3,
                    "rag_history_count": 1  # 只使用当前消息
                },
                "precise_search_config": {
                    "categories": [category_uid],
                    "max_results": 3,
                    "rag_history_count": 1  # 只使用当前消息
                }
            }
            
            async with session.post("http://localhost:8000/api/v1/ask", json=chat_data) as response:
                result = await response.json()
                print(f"{Fore.GREEN}查询结果:{Style.RESET_ALL}")
                print(f"精确匹配内容: {result['precise_content'][:200]}...")
                print(f"向量检索内容: {result['vector_content'][:200]}...")
            
            # 删除模板
            print(f"\n{Fore.CYAN}删除测试模板...{Style.RESET_ALL}")
            async with session.delete("http://localhost:8000/api/v1/templates/rag_history_template") as response:
                result = await response.json()
                if result["status"] == "success":
                    print(f"{Fore.GREEN}删除模板成功{Style.RESET_ALL}")
                else:
                    print(f"{Fore.RED}删除模板失败: {result['message']}{Style.RESET_ALL}")
                
    except Exception as e:
        print(f"{Fore.RED}测试历史消息RAG功能时出错: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}错误详情: {traceback.format_exc()}{Style.RESET_ALL}")

async def main():
    """主函数"""
    print(f"{Fore.MAGENTA}===== 历史消息RAG功能测试 ====={Style.RESET_ALL}")
    print(f"Python版本: {sys.version}")
    print(f"当前工作目录: {os.getcwd()}")
    
    try:
        # 1. 设置测试数据
        category_uid, test_namespace, test_file_path = await setup_test_data()
        if not category_uid or not test_namespace:
            print(f"{Fore.RED}设置测试数据失败，终止测试{Style.RESET_ALL}")
            return
        
        # 2. 测试历史消息RAG功能
        await test_history_rag(category_uid, test_namespace)
        
        # 3. 清理测试数据
        await cleanup_test_data(category_uid, test_namespace, test_file_path)
        
        print(f"\n{Fore.MAGENTA}===== 测试完成 ====={Style.RESET_ALL}")
    except Exception as e:
        print(f"{Fore.RED}测试过程中发生未捕获的错误: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}错误详情:\n{traceback.format_exc()}{Style.RESET_ALL}")

if __name__ == "__main__":
    asyncio.run(main()) 