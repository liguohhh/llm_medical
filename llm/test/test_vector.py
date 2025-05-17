import asyncio
import aiohttp
import json
from pathlib import Path
import os
import sys
from colorama import init, Fore, Style
import traceback
import time

# 初始化colorama
init()

async def clear_namespace(namespace):
    """清理测试前的命名空间，确保从干净状态开始"""
    print(f"{Fore.YELLOW}清理命名空间: {namespace}{Style.RESET_ALL}")
    try:
        async with aiohttp.ClientSession() as session:
            url = f"http://localhost:8000/api/v1/vectors/namespace/{namespace}"
            async with session.delete(url) as response:
                if response.status == 200:
                    result = await response.json()
                    print(f"{Fore.GREEN}命名空间清理成功: {result['message']}{Style.RESET_ALL}")
                else:
                    print(f"{Fore.YELLOW}命名空间清理状态: {response.status} (可能是命名空间不存在){Style.RESET_ALL}")
    except Exception as e:
        print(f"{Fore.YELLOW}命名空间清理过程中出现异常 (继续执行测试): {str(e)}{Style.RESET_ALL}")

async def test_vector_upload():
    """测试上传文档"""
    print(f"\n{Fore.CYAN}1. 测试上传文档{Style.RESET_ALL}")
    
    # 创建测试文件
    test_dir = Path(__file__).parent
    test_file_path = test_dir / "test_data.txt"
    
    print(f"测试文件路径: {test_file_path}")
    
    with open(test_file_path, "w", encoding="utf-8") as f:
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
    
    # 上传文件
    print("开始上传文件...")
    try:
        async with aiohttp.ClientSession() as session:
            data = aiohttp.FormData()
            data.add_field('file', open(test_file_path, 'rb'), filename='test_data.txt')
            data.add_field('namespace', 'medical_test')
            data.add_field('chunk_size', '200')
            data.add_field('chunk_overlap', '50')
            
            url = "http://localhost:8000/api/v1/vectors/upload"
            print(f"发送请求到 {url}")
            
            start_time = time.time()
            async with session.post(url, data=data) as response:
                elapsed_time = time.time() - start_time
                print(f"请求用时: {elapsed_time:.2f}秒")
                
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}上传失败: HTTP状态码 {response.status}{Style.RESET_ALL}")
                    print(f"{Fore.RED}错误详情: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                print(f"{Fore.GREEN}上传成功: {Style.RESET_ALL}")
                print(f"  命名空间: {result['data']['namespace']}")
                print(f"  文档ID: {result['data']['metadata']['doc_id']}")
                print(f"  分块数量: {result['data']['chunks_count']}")
                
                # 保存文档ID用于后续测试
                doc_id = result['data']['metadata']['doc_id']
    except Exception as e:
        print(f"{Fore.RED}发生异常: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
        return False
    
    return {
        "namespace": "medical_test",
        "doc_id": doc_id,
        "file_path": test_file_path
    }

async def test_list_namespaces():
    """测试列出命名空间"""
    print(f"\n{Fore.CYAN}2. 测试列出命名空间{Style.RESET_ALL}")
    
    async with aiohttp.ClientSession() as session:
        url = "http://localhost:8000/api/v1/vectors/namespaces"
        async with session.get(url) as response:
            if response.status != 200:
                print(f"{Fore.RED}获取命名空间列表失败: {await response.text()}{Style.RESET_ALL}")
                return False
            
            result = await response.json()
            namespaces = result['data']['namespaces']
            print(f"{Fore.GREEN}获取命名空间列表成功:{Style.RESET_ALL}")
            for ns in namespaces:
                print(f"  - {ns}")
            
            if "medical_test" not in namespaces:
                print(f"{Fore.RED}测试命名空间未找到!{Style.RESET_ALL}")
                return False
    
    return True

async def test_list_documents(namespace):
    """测试列出文档"""
    print(f"\n{Fore.CYAN}3. 测试列出命名空间 {namespace} 中的文档{Style.RESET_ALL}")
    
    async with aiohttp.ClientSession() as session:
        url = f"http://localhost:8000/api/v1/vectors/documents/{namespace}"
        async with session.get(url) as response:
            if response.status != 200:
                print(f"{Fore.RED}获取文档列表失败: {await response.text()}{Style.RESET_ALL}")
                return False
            
            result = await response.json()
            documents = result['data']['documents']
            print(f"{Fore.GREEN}获取文档列表成功:{Style.RESET_ALL}")
            for doc in documents:
                print(f"  - {doc['source']} (ID: {doc['doc_id']})")
    
    return True

async def test_vector_search(namespace, doc_id):
    """测试向量搜索"""
    print(f"\n{Fore.CYAN}4. 测试向量搜索功能{Style.RESET_ALL}")
    
    try:
        async with aiohttp.ClientSession() as session:
            url = "http://localhost:8000/api/v1/ask"
            data = {
                "message": "头痛是什么原因引起的？需要怎么治疗？",
                "model_settings": {
                    "model_name": "deepseek-chat",
                    "api_key": "sk-e8def07773ea45dd9da0d9a1197b7e63",  # 注意：实际使用时应替换为有效的API密钥
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
                    "namespaces": [namespace],
                    "n_results": 3
                }
            }
            
            # 如果环境变量中有API密钥，使用它
            if "OPENAI_API_KEY" in os.environ:
                data["model_settings"]["api_key"] = os.environ["OPENAI_API_KEY"]
                
            print(f"发送请求到 {url}")
            print(f"搜索命名空间: {namespace}")
            print(f"搜索配置: {data['vector_search_config']}")
            
            start_time = time.time()
            async with session.post(url, json=data) as response:
                elapsed_time = time.time() - start_time
                print(f"请求用时: {elapsed_time:.2f}秒")
                
                if response.status != 200:
                    error_text = await response.text()
                    print(f"{Fore.RED}搜索失败: {error_text}{Style.RESET_ALL}")
                    return False
                
                result = await response.json()
                print(f"{Fore.GREEN}向量搜索结果:{Style.RESET_ALL}")
                vector_content = result.get('vector_content', '')
                print(f"\n向量内容: {vector_content[:200]}...\n" if vector_content else "无向量内容")
                
                print(f"{Fore.YELLOW}模型回答:{Style.RESET_ALL}")
                print(result.get('answer', '无回答'))
    except Exception as e:
        print(f"{Fore.RED}向量搜索测试发生异常: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}异常详情: {traceback.format_exc()}{Style.RESET_ALL}")
        return False
    
    return True

async def test_delete_document(namespace, doc_id):
    """测试删除文档"""
    print(f"\n{Fore.CYAN}5. 测试删除文档{Style.RESET_ALL}")
    
    async with aiohttp.ClientSession() as session:
        url = f"http://localhost:8000/api/v1/vectors/document?namespace={namespace}&doc_id={doc_id}"
        async with session.delete(url) as response:
            if response.status != 200:
                print(f"{Fore.RED}删除文档失败: {await response.text()}{Style.RESET_ALL}")
                return False
            
            result = await response.json()
            print(f"{Fore.GREEN}删除文档成功: {result['message']}{Style.RESET_ALL}")
    
    return True

async def test_delete_namespace(namespace):
    """测试删除命名空间"""
    print(f"\n{Fore.CYAN}6. 测试删除命名空间{Style.RESET_ALL}")
    
    async with aiohttp.ClientSession() as session:
        url = f"http://localhost:8000/api/v1/vectors/namespace/{namespace}"
        async with session.delete(url) as response:
            if response.status != 200:
                print(f"{Fore.RED}删除命名空间失败: {await response.text()}{Style.RESET_ALL}")
                return False
            
            result = await response.json()
            print(f"{Fore.GREEN}删除命名空间成功: {result['message']}{Style.RESET_ALL}")
    
    return True

async def main():
    print(f"{Fore.MAGENTA}===== 向量数据库功能测试 ====={Style.RESET_ALL}")
    print(f"Python版本: {sys.version}")
    print(f"当前工作目录: {os.getcwd()}")
    
    try:
        # 0. 首先清理测试命名空间
        await clear_namespace("medical_test")
        
        # 1. 上传文档
        print(f"{Fore.YELLOW}开始上传文档测试...{Style.RESET_ALL}")
        upload_result = await test_vector_upload()
        if not upload_result:
            print(f"{Fore.RED}上传文档测试失败，终止测试{Style.RESET_ALL}")
            return
        
        namespace = upload_result["namespace"]
        doc_id = upload_result["doc_id"]
        file_path = upload_result["file_path"]
        
        # 2. 列出命名空间
        if not await test_list_namespaces():
            print(f"{Fore.RED}列出命名空间测试失败，但继续测试{Style.RESET_ALL}")
        
        # 3. 列出文档
        if not await test_list_documents(namespace):
            print(f"{Fore.RED}列出文档测试失败，但继续测试{Style.RESET_ALL}")
        
        # 4. 执行向量搜索
        if not await test_vector_search(namespace, doc_id):
            print(f"{Fore.RED}向量搜索测试失败，但继续测试{Style.RESET_ALL}")
        
        # 5. 删除文档
        if not await test_delete_document(namespace, doc_id):
            print(f"{Fore.RED}删除文档测试失败，但继续测试{Style.RESET_ALL}")
        
        # 6. 删除命名空间
        if not await test_delete_namespace(namespace):
            print(f"{Fore.RED}删除命名空间测试失败{Style.RESET_ALL}")
        
        # 7. 清理测试文件
        if os.path.exists(file_path):
            os.remove(file_path)
            print(f"\n{Fore.GREEN}已清理测试文件{Style.RESET_ALL}")
        
        print(f"\n{Fore.MAGENTA}===== 测试完成 ====={Style.RESET_ALL}")
    except Exception as e:
        print(f"{Fore.RED}测试过程中发生未捕获的错误: {str(e)}{Style.RESET_ALL}")
        print(f"{Fore.RED}错误详情:\n{traceback.format_exc()}{Style.RESET_ALL}")

if __name__ == "__main__":
    asyncio.run(main()) 