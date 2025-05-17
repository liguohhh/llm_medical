package edu.haut.baoxinyang.server.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * LLM后端管理服务接口
 * 提供向量数据库、精确查找数据库和模板管理功能
 */
public interface LlmManageService {
    
    // ========== 向量数据库管理 ==========
    
    /**
     * 获取所有向量数据库命名空间
     * @return 命名空间列表
     */
    List<String> getVectorNamespaces();
    
    /**
     * 获取指定命名空间中的文档列表
     * @param namespace 命名空间
     * @return 文档信息列表
     */
    List<Map<String, Object>> getVectorDocuments(String namespace);
    
    /**
     * 上传文档到向量数据库
     * @param file 文件（支持txt、docx、pdf格式）
     * @param namespace 命名空间
     * @param chunkSize 分块大小，默认500
     * @param chunkOverlap 分块重叠大小，默认50
     * @return 上传结果
     */
    Map<String, Object> uploadVectorDocument(MultipartFile file, String namespace, Integer chunkSize, Integer chunkOverlap);
    
    /**
     * 删除向量数据库中的文档
     * @param namespace 命名空间
     * @param docId 文档ID
     * @return 是否成功
     */
    boolean deleteVectorDocument(String namespace, String docId);
    
    /**
     * 删除向量数据库命名空间
     * @param namespace 命名空间
     * @return 是否成功
     */
    boolean deleteVectorNamespace(String namespace);
    
    /**
     * 创建向量数据库命名空间
     * @param namespace 命名空间名称
     * @return 是否成功
     */
    boolean createVectorNamespace(String namespace);
    
    // ========== 精确查找数据库管理 ==========
    
    /**
     * 获取所有精确查找大类
     * @return 大类列表
     */
    List<Map<String, Object>> getPreciseCategories();
    
    /**
     * 创建精确查找大类
     * @param name 大类名称
     * @return 创建的大类信息
     */
    Map<String, Object> createPreciseCategory(String name);
    
    /**
     * 更新精确查找大类名称
     * @param categoryUid 大类UID
     * @param name 新名称
     * @return 更新后的大类信息
     */
    Map<String, Object> updatePreciseCategory(String categoryUid, String name);
    
    /**
     * 删除精确查找大类
     * @param categoryUid 大类UID
     * @return 是否成功
     */
    boolean deletePreciseCategory(String categoryUid);
    
    /**
     * 获取精确查找大类下的条目列表
     * @param categoryUid 大类UID
     * @return 条目列表
     */
    Map<String, Object> getPreciseEntries(String categoryUid);
    
    /**
     * 创建精确查找条目
     * @param categoryUid 大类UID
     * @param description 条目描述
     * @param content 条目内容
     * @param keywords 关键词列表
     * @param weight 权重
     * @param isEnabled 是否启用
     * @return 创建的条目信息
     */
    Map<String, Object> createPreciseEntry(String categoryUid, String description, String content,
                                          List<String> keywords, Integer weight, Boolean isEnabled);
    
    /**
     * 更新精确查找条目
     * @param entryUid 条目UID
     * @param description 条目描述
     * @param content 条目内容
     * @param keywords 关键词列表
     * @param weight 权重
     * @param isEnabled 是否启用
     * @return 更新后的条目信息
     */
    Map<String, Object> updatePreciseEntry(String entryUid, String description, String content,
                                          List<String> keywords, Integer weight, Boolean isEnabled);
    
    /**
     * 删除精确查找条目
     * @param entryUid 条目UID
     * @return 是否成功
     */
    boolean deletePreciseEntry(String entryUid);
    
    // ========== 提示词模板管理 ==========
    
    /**
     * 获取所有提示词模板ID列表
     * @return 模板ID列表
     */
    List<String> getTemplateIds();
    
    /**
     * 获取提示词模板详情
     * @param templateId 模板ID
     * @return 模板详情
     */
    Map<String, Object> getTemplateDetail(String templateId);
    
    /**
     * 创建或更新提示词模板
     * @param templateId 模板ID
     * @param description 描述
     * @param subTemplates 子模板信息，每个子模板包含order字段
     * @return 是否成功
     */
    boolean saveOrUpdateTemplate(String templateId, String description, Map<String, Map<String, Object>> subTemplates);
    
    /**
     * 删除提示词模板
     * @param templateId 模板ID
     * @return 是否成功
     */
    boolean deleteTemplate(String templateId);
} 