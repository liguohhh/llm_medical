from sentence_transformers import SentenceTransformer
import time
import numpy as np
import logging
import traceback
import os

# 配置日志
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler()
    ]
)
logger = logging.getLogger("embedding-test")

def test_sentence_transformer():
    """测试SentenceTransformer模型加载和使用"""
    try:
        logger.info("开始测试SentenceTransformer模型")
        
        # 测试环境
        logger.info(f"当前工作目录: {os.getcwd()}")
        logger.info(f"PYTHONPATH: {os.environ.get('PYTHONPATH', 'Not set')}")
        
        # 加载模型
        logger.info("开始加载模型: all-MiniLM-L6-v2")
        start_time = time.time()
        model = SentenceTransformer('all-MiniLM-L6-v2')
        load_time = time.time() - start_time
        logger.info(f"模型加载完成，耗时: {load_time:.2f}秒")
        
        # 编码测试文本
        logger.info("开始编码测试文本...")
        test_sentences = [
            "这是一个测试句子，用于生成嵌入向量。",
            "另一个测试句子，用于比较嵌入向量的相似度。",
            "这句话与第一句话的含义非常相似。"
        ]
        
        start_time = time.time()
        embeddings = model.encode(test_sentences)
        encode_time = time.time() - start_time
        logger.info(f"编码完成，耗时: {encode_time:.2f}秒")
        
        # 打印嵌入向量信息
        logger.info(f"嵌入向量形状: {embeddings.shape}")
        for i, embedding in enumerate(embeddings):
            logger.info(f"句子 {i+1} 嵌入向量: 形状={embedding.shape}, 均值={np.mean(embedding):.4f}, 范数={np.linalg.norm(embedding):.4f}")
        
        # 计算余弦相似度
        def cosine_similarity(a, b):
            return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))
        
        sim_1_2 = cosine_similarity(embeddings[0], embeddings[1])
        sim_1_3 = cosine_similarity(embeddings[0], embeddings[2])
        sim_2_3 = cosine_similarity(embeddings[1], embeddings[2])
        
        logger.info(f"句子1和句子2的相似度: {sim_1_2:.4f}")
        logger.info(f"句子1和句子3的相似度: {sim_1_3:.4f}")
        logger.info(f"句子2和句子3的相似度: {sim_2_3:.4f}")
        
        logger.info("测试完成，模型工作正常")
        return True
    except Exception as e:
        logger.error(f"测试SentenceTransformer时出错: {str(e)}")
        logger.error(traceback.format_exc())
        return False

if __name__ == "__main__":
    success = test_sentence_transformer()
    if success:
        print("✅ SentenceTransformer模型测试通过")
    else:
        print("❌ SentenceTransformer模型测试失败") 