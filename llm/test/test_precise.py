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

async def test_create_category():
    """测试创建大类"""
    print(f"\n{Fore.CYAN}1. 测试创建大类{Style.RESET_ALL}")
    
    category_name = "头痛类"
    data = {"name": category_name}
    
    try:
        async with aiohttp.ClientSession() as session:
            url = "http://localhost:8000/api/v1/precise/category"
            print(f"发送请求到 {url}，创建大类: {category_name}")
            
            async with session.post(url, json=data) as response:
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}创建大类失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                    print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                    return None
                
                result = await response.json()
                if result["success"]:
                    print(f"{Fore.GREEN}创建大类成功: {Style.RESET_ALL}")
                    print(f"  名称: {result['data']['name']}")
                    print(f"  UID: {result['data']['uid']}")
                    return result["data"]["uid"]
                else:
                    print(f"{Fore.RED}创建大类失败: {result['message']}{Style.RESET_ALL}")
                    return None
    except Exception as e:
        print(f"{Fore.RED}创建大类过程中发生异常: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
        return None

async def test_create_entries(category_uid):
    """测试创建条目"""
    print(f"\n{Fore.CYAN}2. 测试创建条目{Style.RESET_ALL}")
    
    entries_data = [
        {
            "description": "偏头痛",
            "content": "偏头痛是一种常见的头痛类型，特点是反复发作的中度至重度疼痛，通常伴有恶心、呕吐和对光、声音敏感。可能与神经系统、血管和遗传因素有关。治疗包括止痛药、紧张性头痛,丛集性头痛,预防性药物和生活方式调整。",
            "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐", "光敏感","提瓦特"],
            "weight": 85
        },
        {
            "description": "紧张性头痛",
            "content": "紧张性头痛是最常见的头痛类型，通常表现为头部两侧的钝痛或压迫感，提瓦特程度轻到中度。常与压力、焦虑和肌肉紧张有关。治疗包括非处方止痛药、放松技巧和压力管理。",
            "keywords": ["紧张性头痛", "压力性头痛", "肌肉紧张", "压迫感", "钝痛","米哈游"],
            "weight": 80
        },
        {
            "description": "丛集性头痛",
            "content": "丛集性头痛是一种罕见但严重的头痛类型，特点是眼睛周围的剧烈疼痛，通常发生在一侧。疼米哈游痛来势凶猛，可持续15分钟到3小时，且周期性发作，故名。治疗包括氧气吸入、舒马曲坦和预防性药物。",
            "keywords": ["丛集性头痛", "眼周疼痛", "剧烈疼痛", "单侧头痛", "周期性","原神"],
            "weight": 90
        }
    ]
    
    entry_uids = []
    for entry_data in entries_data:
        try:
            async with aiohttp.ClientSession() as session:
                url = f"http://localhost:8000/api/v1/precise/entry/{category_uid}"
                data = {
                    "description": entry_data["description"],
                    "content": entry_data["content"],
                    "keywords": entry_data["keywords"],
                    "weight": entry_data["weight"],
                    "is_enabled": True
                }
                
                print(f"发送请求到 {url}，创建条目: {entry_data['description']}")
                
                async with session.post(url, json=data) as response:
                    if response.status != 200:
                        error_text = await response.text()
                        print(f"{Fore.RED}创建条目失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                        print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                        continue
                    
                    result = await response.json()
                    if result["success"]:
                        print(f"{Fore.GREEN}创建条目成功: {result['data']['description']}{Style.RESET_ALL}")
                        print(f"  关键词: {', '.join(result['data']['keywords'])}")
                        print(f"  权重: {result['data']['weight']}")
                        print(f"  UID: {result['data']['uid']}")
                        entry_uids.append(result["data"]["uid"])
                    else:
                        print(f"{Fore.RED}创建条目失败: {result['message']}{Style.RESET_ALL}")
        except Exception as e:
            print(f"{Fore.RED}创建条目过程中发生异常: {str(e)}{Style.RESET_ALL}")
            print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
    
    return entry_uids

async def test_list_categories():
    """测试列出所有大类"""
    print(f"\n{Fore.CYAN}3. 测试列出所有大类{Style.RESET_ALL}")
    
    try:
        async with aiohttp.ClientSession() as session:
            url = "http://localhost:8000/api/v1/precise/categories"
            print(f"发送请求到 {url}")
            
            async with session.get(url) as response:
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}获取大类列表失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                    print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                if result["success"]:
                    print(f"{Fore.GREEN}获取大类列表成功:{Style.RESET_ALL}")
                    for category in result["data"]["categories"]:
                        print(f"  - {category['name']} (UID: {category['uid']})")
                    return True
                else:
                    print(f"{Fore.RED}获取大类列表失败: {result['message']}{Style.RESET_ALL}")
                    return False
    except Exception as e:
        print(f"{Fore.RED}获取大类列表过程中发生异常: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
        return False

async def test_list_entries(category_uid):
    """测试列出指定大类下的所有条目"""
    print(f"\n{Fore.CYAN}4. 测试列出大类下的所有条目{Style.RESET_ALL}")
    
    try:
        async with aiohttp.ClientSession() as session:
            url = f"http://localhost:8000/api/v1/precise/entries/{category_uid}"
            print(f"发送请求到 {url}")
            
            async with session.get(url) as response:
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}获取条目列表失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                    print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                if result["success"]:
                    print(f"{Fore.GREEN}获取条目列表成功:{Style.RESET_ALL}")
                    print(f"  大类: {result['data']['category']['name']}")
                    for entry in result["data"]["entries"]:
                        print(f"  - {entry['description']} (UID: {entry['uid']}, 权重: {entry['weight']})")
                        print(f"    关键词: {', '.join(entry['keywords'])}")
                    return True
                else:
                    print(f"{Fore.RED}获取条目列表失败: {result['message']}{Style.RESET_ALL}")
                    return False
    except Exception as e:
        print(f"{Fore.RED}获取条目列表过程中发生异常: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
        return False

async def test_search():
    """测试搜索功能"""
    print(f"\n{Fore.CYAN}5. 测试搜索功能{Style.RESET_ALL}")
    
    try:
        # 1. 基本搜索（默认深度为2）
        search_query = "我经常头疼恶心，可能是什么原因？"
        print(f"搜索查询（默认深度=2）: {search_query}")
        
        async with aiohttp.ClientSession() as session:
            url = "http://localhost:8000/api/v1/precise/search"
            data = {"query": search_query}
            
            async with session.post(url, json=data) as response:
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}搜索失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                    print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                if result["success"]:
                    print(f"{Fore.GREEN}搜索成功，找到 {len(result['data']['entries'])} 个匹配条目:{Style.RESET_ALL}")
                    for i, entry in enumerate(result["data"]["entries"]):
                        print(f"  {i+1}. {entry['entry']['description']} (权重: {entry['entry']['weight']}, 匹配级别: {entry['entry']['match_level']})")
                        print(f"     大类: {entry['category']['name']}")
                        print(f"     内容: {entry['entry']['content'][:100]}...")
                else:
                    print(f"{Fore.RED}搜索失败: {result['message']}{Style.RESET_ALL}")
        
        # 2. 深度为1的搜索（只进行直接匹配）
        search_query = "我的头一侧经常疼痛"
        print(f"\n搜索查询(深度=3): {search_query}")
        
        async with aiohttp.ClientSession() as session:
            url = "http://localhost:8000/api/v1/precise/search"
            data = {"query": search_query, "search_depth": 1}
            
            async with session.post(url, json=data) as response:
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}搜索失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                    print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                if result["success"]:
                    print(f"{Fore.GREEN}深度1搜索成功，找到 {len(result['data']['entries'])} 个匹配条目:{Style.RESET_ALL}")
                    for i, entry in enumerate(result["data"]["entries"]):
                        print(f"  {i+1}. {entry['entry']['description']} (权重: {entry['entry']['weight']}, 匹配级别: {entry['entry']['match_level']})")
                else:
                    print(f"{Fore.RED}搜索失败: {result['message']}{Style.RESET_ALL}")
        
        # 3. 深度为3的搜索（进行三级关联匹配）
        search_query = "原神"
        print(f"\n搜索查询(深度=3): {search_query}")
        
        async with aiohttp.ClientSession() as session:
            url = "http://localhost:8000/api/v1/precise/search"
            data = {"query": search_query, "search_depth": 3}
            
            async with session.post(url, json=data) as response:
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}搜索失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                    print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                if result["success"]:
                    print(f"{Fore.GREEN}深度3搜索成功，找到 {len(result['data']['entries'])} 个匹配条目:{Style.RESET_ALL}")
                    for i, entry in enumerate(result["data"]["entries"]):
                        print(f"  {i+1}. {entry['entry']['description']} (权重: {entry['entry']['weight']}, 匹配级别: {entry['entry']['match_level']})")
                        print(f"     内容摘要: {entry['entry']['content'][:50]}...")
                else:
                    print(f"{Fore.RED}搜索失败: {result['message']}{Style.RESET_ALL}")
        
        # 4. 测试与聊天接口的集成
        print(f"\n{Fore.CYAN}测试与聊天接口的集成{Style.RESET_ALL}")
        search_query = "我最近经常头痛恶心，可能是什么原因？"
        print(f"测试问题: {search_query}")
        
        async with aiohttp.ClientSession() as session:
            url = "http://localhost:8000/api/v1/ask"
            data = {
                "message": search_query,
                "model_settings": {
                    "model_name": "deepseek-chat",
                    "api_key": "sk-e8def07773ea45dd9da0d9a1197b7e63",  # 注意：实际使用时应替换为有效的API密钥
                    "base_url": "https://api.deepseek.com/v1"
                },
                "template_config": {
                    "template_id": "medical_template",
                    "sub_template_ids": ["基础信息", "参考内容"],
                    "params": {
                        "patient_age": "35",
                        "patient_gender": "女",
                        "symptoms": "头痛、恶心"
                    }
                },
                "precise_search_config": {
                    "max_results": 3,
                    "search_depth": 2
                }
            }
            
            # 如果环境变量中有API密钥，使用它
            if "OPENAI_API_KEY" in os.environ:
                data["model_settings"]["api_key"] = os.environ["OPENAI_API_KEY"]
            
            print(f"发送请求到 {url}")
            print(f"精确查询配置: {data['precise_search_config']}")
            
            start_time = time.time()
            async with session.post(url, json=data) as response:
                elapsed_time = time.time() - start_time
                print(f"请求用时: {elapsed_time:.2f}秒")
                
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}集成测试失败: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                print(f"{Fore.GREEN}集成测试结果:{Style.RESET_ALL}")
                precise_content = result.get('precise_content', '')
                print(f"\n精确内容: {precise_content[:200]}...\n" if precise_content else "无精确匹配内容")
                
                print(f"{Fore.YELLOW}模型回答:{Style.RESET_ALL}")
                print(result.get('answer', '无回答'))
        
        return True
    except Exception as e:
        print(f"{Fore.RED}搜索测试过程中发生异常: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
        return False

async def test_update_entry(entry_uid):
    """测试更新条目"""
    print(f"\n{Fore.CYAN}6. 测试更新条目{Style.RESET_ALL}")
    
    try:
        async with aiohttp.ClientSession() as session:
            url = f"http://localhost:8000/api/v1/precise/entry/{entry_uid}"
            data = {
                "description": "更新后的偏头痛",
                "weight": 95,
                "keywords": ["偏头痛", "片侧头痛", "血管性头痛", "恶心", "呕吐", "光敏感", "高频疾病"]
            }
            
            print(f"发送请求到 {url}")
            
            async with session.put(url, json=data) as response:
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}更新条目失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                    print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                if result["success"]:
                    print(f"{Fore.GREEN}更新条目成功:{Style.RESET_ALL}")
                    print(f"  描述: {result['data']['description']}")
                    print(f"  权重: {result['data']['weight']}")
                    print(f"  关键词: {', '.join(result['data']['keywords'])}")
                    return True
                else:
                    print(f"{Fore.RED}更新条目失败: {result['message']}{Style.RESET_ALL}")
                    return False
    except Exception as e:
        print(f"{Fore.RED}更新条目过程中发生异常: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
        return False

async def test_delete():
    """测试删除"""
    print(f"\n{Fore.CYAN}7. 测试删除大类{Style.RESET_ALL}")
    
    try:
        async with aiohttp.ClientSession() as session:
            url = "http://localhost:8000/api/v1/precise/categories"
            async with session.get(url) as response:
                if response.status != 200:
                    print(f"{Fore.RED}获取大类列表失败，无法继续删除操作{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                if not result["success"] or not result["data"]["categories"]:
                    print(f"{Fore.YELLOW}没有可删除的大类{Style.RESET_ALL}")
                    return True
                
                for category in result["data"]["categories"]:
                    print(f"删除大类: {category['name']} (UID: {category['uid']})")
                    delete_url = f"http://localhost:8000/api/v1/precise/category/{category['uid']}"
                    
                    async with session.delete(delete_url) as delete_response:
                        if delete_response.status != 200:
                            error_text = await delete_response.text()
                            print(f"{Fore.RED}删除大类失败: HTTP状态码 {delete_response.status}{Style.RESET_ALL}")
                            print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                            continue
                        
                        delete_result = await delete_response.json()
                        if delete_result["success"]:
                            print(f"{Fore.GREEN}删除大类成功{Style.RESET_ALL}")
                        else:
                            print(f"{Fore.RED}删除大类失败: {delete_result['message']}{Style.RESET_ALL}")
                
                return True
    except Exception as e:
        print(f"{Fore.RED}删除大类过程中发生异常: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
        return False

async def main():
    print(f"{Fore.MAGENTA}===== 精确查询功能测试 ====={Style.RESET_ALL}")
    print(f"Python版本: {sys.version}")
    print(f"当前工作目录: {os.getcwd()}")
    
    try:
        # 1. 创建大类
        category_uid = await test_create_category()
        if not category_uid:
            print(f"{Fore.RED}创建大类失败，终止测试{Style.RESET_ALL}")
            return
        
        # 2. 创建条目
        entry_uids = await test_create_entries(category_uid)
        if not entry_uids:
            print(f"{Fore.RED}创建条目失败，终止测试{Style.RESET_ALL}")
            return
        
        # 3. 列出所有大类
        if not await test_list_categories():
            print(f"{Fore.RED}列出大类测试失败，但继续测试{Style.RESET_ALL}")
        
        # 4. 列出大类下的所有条目
        if not await test_list_entries(category_uid):
            print(f"{Fore.RED}列出条目测试失败，但继续测试{Style.RESET_ALL}")
        
        # 5. 搜索测试
        if not await test_search():
            print(f"{Fore.RED}搜索测试失败，但继续测试{Style.RESET_ALL}")
        
        # 6. 更新条目
        if entry_uids and not await test_update_entry(entry_uids[0]):
            print(f"{Fore.RED}更新条目测试失败，但继续测试{Style.RESET_ALL}")
        
        # 7. 删除大类和条目
        if not await test_delete():
            print(f"{Fore.RED}删除测试失败{Style.RESET_ALL}")
        
        print(f"\n{Fore.MAGENTA}===== 测试完成 ====={Style.RESET_ALL}")
    except Exception as e:
        import traceback
        print(f"{Fore.RED}测试过程中发生未捕获的错误: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}错误详情:\n{traceback.format_exc()}{Style.RESET_ALL}")

if __name__ == "__main__":
    asyncio.run(main()) 