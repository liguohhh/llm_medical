from typing import Dict, List, Tuple, Optional, Any
import os
import io
import uuid
from pathlib import Path
import asyncio
import docx
import fitz  # PyMuPDF
from rag.vector_rag import VectorRAG
import traceback

class VectorService:
    def __init__(self):
        """初始化向量服务"""
        self.vector_rag = VectorRAG()
        
    async def add_document(self, file_content: bytes, file_name: str, 
                           namespace: str, chunk_size: int = 500, 
                           chunk_overlap: int = 50) -> Dict[str, Any]:
        """
        添加文档到向量数据库
        
        Args:
            file_content: 文件内容（二进制）
            file_name: 文件名
            namespace: 文档存储的命名空间
            chunk_size: 文档分块大小
            chunk_overlap: 分块重叠大小
            
        Returns:
            Dict: 包含命名空间和文档元数据的字典
        """
        try:
            # 生成文档唯一标识
            doc_id = str(uuid.uuid4())
            
            # 根据文件类型提取文本
            try:
                text = await self._extract_text(file_content, file_name)
                print(f"成功提取文本，长度: {len(text)}")
            except Exception as e:
                print(f"提取文本时出错: {str(e)}")
                print(traceback.format_exc())
                return {"error": f"提取文本时出错: {str(e)}"}
            
            # 文档切分
            try:
                chunks = self._split_text(text, chunk_size, chunk_overlap)
                print(f"成功切分文本，生成了 {len(chunks)} 个文本块")
            except Exception as e:
                print(f"切分文本时出错: {str(e)}")
                print(traceback.format_exc())
                return {"error": f"切分文本时出错: {str(e)}"}
            
            if not chunks:
                return {"error": "文档内容为空或无法处理"}
            
            # 创建元数据
            metadata = {
                "source": file_name,
                "doc_id": doc_id
            }
            
            # 添加到向量数据库
            try:
                print(f"开始添加 {len(chunks)} 个文本块到向量数据库")
                await self.vector_rag.add_texts(chunks, namespace, metadata)
                print("成功添加到向量数据库")
            except Exception as e:
                print(f"添加到向量数据库时出错: {str(e)}")
                print(traceback.format_exc())
                return {"error": f"添加到向量数据库时出错: {str(e)}"}
            
            return {
                "namespace": namespace,
                "metadata": metadata,
                "chunks_count": len(chunks)
            }
        except Exception as e:
            error_msg = f"处理文档时出错: {str(e)}"
            stack_trace = traceback.format_exc()
            print(error_msg)
            print(stack_trace)
            return {"error": error_msg}
    
    async def delete_document(self, namespace: str, doc_id: str) -> Dict[str, Any]:
        """
        删除指定文档
        
        Args:
            namespace: 命名空间
            doc_id: 文档ID
            
        Returns:
            Dict: 操作结果
        """
        try:
            await self.vector_rag.delete_by_metadata(namespace, "doc_id", doc_id)
            return {"status": "success", "message": f"已删除命名空间 {namespace} 中文档 {doc_id} 的所有内容"}
        except Exception as e:
            return {"error": f"删除文档时出错: {str(e)}"}
    
    async def delete_namespace(self, namespace: str) -> Dict[str, Any]:
        """
        删除整个命名空间
        
        Args:
            namespace: 要删除的命名空间
            
        Returns:
            Dict: 操作结果
        """
        try:
            await self.vector_rag.delete_collection(namespace)
            return {"status": "success", "message": f"已删除命名空间 {namespace}"}
        except Exception as e:
            return {"error": f"删除命名空间时出错: {str(e)}"}
    
    async def create_namespace(self, namespace: str) -> Dict[str, Any]:
        """
        创建新的命名空间
        
        Args:
            namespace: 要创建的命名空间名称
            
        Returns:
            Dict: 操作结果
        """
        try:
            # 检查命名空间是否已存在
            namespaces = await asyncio.to_thread(self.vector_rag.list_collections)
            if namespace in namespaces:
                return {"error": f"命名空间 {namespace} 已存在"}
            
            # 使用get_collection方法创建新的命名空间
            await asyncio.to_thread(self.vector_rag.get_collection, namespace, True)
            return {"status": "success", "message": f"已创建命名空间 {namespace}", "namespace": namespace}
        except Exception as e:
            return {"error": f"创建命名空间时出错: {str(e)}"}
    
    async def list_namespaces(self) -> Dict[str, Any]:
        """
        列出所有命名空间
        
        Returns:
            Dict: 包含命名空间列表的字典
        """
        try:
            # 使用线程池执行同步操作
            namespaces = await asyncio.to_thread(self.vector_rag.list_collections)
            return {"namespaces": namespaces}
        except Exception as e:
            return {"error": f"获取命名空间列表时出错: {str(e)}"}
    
    async def list_documents(self, namespace: str) -> Dict[str, Any]:
        """
        列出指定命名空间中的所有文档
        
        Args:
            namespace: 命名空间
            
        Returns:
            Dict: 包含文档列表的字典
        """
        try:
            # 获取文档ID列表
            doc_ids = await self.vector_rag.get_metadata_values(namespace, "doc_id")
            
            # 获取每个文档的详细信息
            documents = []
            for doc_id in doc_ids:
                # 获取文档源信息
                sources = await self.vector_rag.get_metadata_values(namespace, "source")
                # 找到与当前doc_id对应的source
                source = next((s for s in sources if s), "未知来源")
                
                documents.append({
                    "doc_id": doc_id,
                    "source": source
                })
            
            return {
                "namespace": namespace,
                "documents": documents
            }
        except Exception as e:
            return {"error": f"获取文档列表时出错: {str(e)}"}
    
    async def _extract_text(self, file_content: bytes, file_name: str) -> str:
        """
        从不同类型的文件中提取文本
        
        Args:
            file_content: 文件内容（二进制）
            file_name: 文件名
            
        Returns:
            str: 提取的文本内容
        """
        # 获取文件扩展名
        file_ext = Path(file_name).suffix.lower()
        
        # 使用线程池执行IO密集型操作
        return await asyncio.to_thread(self._extract_text_sync, file_content, file_name, file_ext)
    
    def _extract_text_sync(self, file_content: bytes, file_name: str, file_ext: str) -> str:
        """同步版本的文本提取方法"""
        if file_ext == '.txt':
            # 文本文件
            return file_content.decode('utf-8', errors='ignore')
        
        elif file_ext == '.docx':
            # Word文档
            doc = docx.Document(io.BytesIO(file_content))
            return '\n'.join([para.text for para in doc.paragraphs if para.text])
        
        elif file_ext == '.pdf':
            # PDF文档
            text = ""
            with fitz.open(stream=file_content, filetype="pdf") as pdf:
                for page in pdf:
                    text += page.get_text()
            return text
        
        else:
            # 不支持的文件类型
            raise ValueError(f"不支持的文件类型: {file_ext}")
    
    def _split_text(self, text: str, chunk_size: int = 500, chunk_overlap: int = 50) -> List[str]:
        """
        将文本切分为多个小块
        
        Args:
            text: 要切分的文本
            chunk_size: 每个块的大小
            chunk_overlap: 相邻块之间的重叠大小
            
        Returns:
            List[str]: 切分后的文本块列表
        """
        print(f"开始切分文本, 文本类型: {type(text)}, 文本长度: {len(text) if text else 0}")
        
        try:
            if not text:
                print("文本为空，返回空列表")
                return []
            
            if not isinstance(text, str):
                print(f"文本不是字符串类型，尝试转换为字符串")
                text = str(text)
                
            chunks = []
            start = 0
            text_length = len(text)
            print(f"文本总长度: {text_length}，开始切分成大小为 {chunk_size} 的块，重叠 {chunk_overlap}")
            
            while start < text_length:
                try:
                    # 检查剩余文本的长度
                    remaining_length = text_length - start
                    
                    # 如果剩余文本长度小于目标块长度，则将剩余所有文本作为最后一块
                    if remaining_length <= chunk_size:
                        end = text_length
                        # 检查最后一块是否有足够的意义（不是太小）
                        if remaining_length < chunk_size / 4 and len(chunks) > 0:
                            # 如果最后一块太小且已有其他块，则将其附加到上一块
                            last_chunk = chunks.pop()
                            chunk = last_chunk + text[start:]
                            print(f"将小于 {chunk_size/4} 字符的剩余文本 ({remaining_length} 字符) 附加到上一块")
                        else:
                            # 否则作为独立的最后一块
                            chunk = text[start:]
                            print(f"将剩余文本 ({remaining_length} 字符) 作为最后一块")
                        
                        if chunk and chunk.strip():
                            chunks.append(chunk)
                            print(f"添加最后一个文本块，长度: {len(chunk)}")
                        break
                    else:
                        # 确定当前块的结束位置
                        end = min(start + chunk_size, text_length)
                    
                    # 提取当前块
                    chunk = text[start:end]
                    
                    # 添加到结果列表
                    if chunk and chunk.strip():  # 确保块不是空白
                        chunks.append(chunk)
                        print(f"添加第 {len(chunks)} 个文本块，长度: {len(chunk)}")
                    else:
                        print(f"跳过空白文本块，起始位置: {start}, 结束位置: {end}")
                    
                    # 移动到下一个起始位置，考虑重叠
                    start = end - chunk_overlap
                except Exception as chunk_e:
                    print(f"处理文本块时出错 (start={start}, end={end}): {str(chunk_e)}")
                    print(traceback.format_exc())
                    # 继续处理下一个块
                    start = end
            
            print(f"文本切分完成，共生成 {len(chunks)} 个文本块")
            
            # 如果没有成功切分出任何块，尝试整体作为一个块
            if not chunks and text.strip():
                print("没有成功切分出块，尝试将整体作为一个块")
                MAX_LENGTH = 2000  # 限制最大长度
                if len(text) > MAX_LENGTH:
                    text = text[:MAX_LENGTH]
                    print(f"文本过长，截断为 {MAX_LENGTH} 字符")
                chunks.append(text)
            
            return chunks
        except Exception as e:
            print(f"文本切分过程中出现异常: {str(e)}")
            print(traceback.format_exc())
            
            # 返回简单处理的结果，避免完全失败
            if text and isinstance(text, str):
                MAX_LENGTH = 1000  # 异常情况下使用更小的限制
                text_sample = text[:MAX_LENGTH]
                print(f"出现异常，返回截断的文本作为单个块 (长度: {len(text_sample)})")
                return [text_sample]
            return [] 