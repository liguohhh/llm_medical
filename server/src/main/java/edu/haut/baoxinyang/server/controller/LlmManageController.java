package edu.haut.baoxinyang.server.controller;

import edu.haut.baoxinyang.server.common.R;
import edu.haut.baoxinyang.server.dto.llm.manage.PreciseCategoryDTO;
import edu.haut.baoxinyang.server.dto.llm.manage.PreciseEntryDTO;
import edu.haut.baoxinyang.server.dto.llm.manage.TemplateCreateDTO;
import edu.haut.baoxinyang.server.service.LlmManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM后端管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/llm")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LlmManageController {
    
    private final LlmManageService llmManageService;
    
    // ========== 向量数据库管理 ==========
    
    /**
     * 获取所有向量数据库命名空间
     */
    @GetMapping("/vector/namespaces")
    public R<List<String>> getVectorNamespaces() {
        List<String> namespaces = llmManageService.getVectorNamespaces();
        return R.ok(namespaces);
    }
    
    /**
     * 获取指定命名空间中的文档列表
     */
    @GetMapping("/vector/documents/{namespace}")
    public R<List<Map<String, Object>>> getVectorDocuments(@PathVariable String namespace) {
        List<Map<String, Object>> documents = llmManageService.getVectorDocuments(namespace);
        return R.ok(documents);
    }
    
    /**
     * 上传文档到向量数据库
     */
    @PostMapping("/vector/upload")
    public R<Map<String, Object>> uploadVectorDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("namespace") String namespace,
            @RequestParam(value = "chunkSize", required = false) Integer chunkSize,
            @RequestParam(value = "chunkOverlap", required = false) Integer chunkOverlap) {
        
        if (file.isEmpty()) {
            return R.error("文件不能为空");
        }
        
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        log.info("上传文件: {}, ContentType: {}", fileName, contentType);
        
        // 检查文件扩展名
        boolean isValidExtension = false;
        if (fileName != null) {
            String lowerFileName = fileName.toLowerCase();
            isValidExtension = lowerFileName.endsWith(".txt") || 
                               lowerFileName.endsWith(".pdf") ||
                               lowerFileName.endsWith(".doc") ||
                               lowerFileName.endsWith(".docx");
        }
        
        // 检查MIME类型
        boolean isValidMimeType = false;
        if (contentType != null) {
            isValidMimeType = contentType.equals("text/plain") || 
                             contentType.contains("pdf") || 
                             contentType.contains("word") ||
                             contentType.contains("document") ||
                             contentType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml");
        }
        
        if (!isValidExtension && !isValidMimeType) {
            return R.error("仅支持txt、docx、pdf格式文件");
        }
        
        Map<String, Object> result = llmManageService.uploadVectorDocument(file, namespace, chunkSize, chunkOverlap);
        if (result == null) {
            return R.error("上传失败");
        }
        
        return R.ok(result);
    }
    
    /**
     * 删除向量数据库中的文档
     */
    @DeleteMapping("/vector/document")
    public R<Boolean> deleteVectorDocument(
            @RequestParam("namespace") String namespace,
            @RequestParam("docId") String docId) {
        
        boolean success = llmManageService.deleteVectorDocument(namespace, docId);
        return success ? R.ok(true) : R.error("删除文档失败");
    }
    
    /**
     * 删除向量数据库命名空间
     */
    @DeleteMapping("/vector/namespace/{namespace}")
    public R<Boolean> deleteVectorNamespace(@PathVariable String namespace) {
        boolean success = llmManageService.deleteVectorNamespace(namespace);
        return success ? R.ok(true) : R.error("删除命名空间失败");
    }
    
    /**
     * 创建向量数据库命名空间
     */
    @PostMapping("/vector/namespace")
    public R<Boolean> createVectorNamespace(@RequestParam("namespace") String namespace) {
        boolean success = llmManageService.createVectorNamespace(namespace);
        return success ? R.ok(true) : R.error("创建命名空间失败");
    }
    
    // ========== 精确查找数据库管理 ==========
    
    /**
     * 获取所有精确查找大类
     */
    @GetMapping("/precise/categories")
    public R<List<Map<String, Object>>> getPreciseCategories() {
        List<Map<String, Object>> categories = llmManageService.getPreciseCategories();
        return R.ok(categories);
    }
    
    /**
     * 创建精确查找大类
     */
    @PostMapping("/precise/category")
    public R<Map<String, Object>> createPreciseCategory(@RequestBody @Valid PreciseCategoryDTO categoryDTO) {
        Map<String, Object> result = llmManageService.createPreciseCategory(categoryDTO.getName());
        if (result == null) {
            return R.error("创建大类失败");
        }
        return R.ok(result);
    }
    
    /**
     * 更新精确查找大类
     */
    @PutMapping("/precise/category/{categoryUid}")
    public R<Map<String, Object>> updatePreciseCategory(
            @PathVariable String categoryUid,
            @RequestBody @Valid PreciseCategoryDTO categoryDTO) {
        
        Map<String, Object> result = llmManageService.updatePreciseCategory(categoryUid, categoryDTO.getName());
        if (result == null) {
            return R.error("更新大类失败");
        }
        return R.ok(result);
    }
    
    /**
     * 删除精确查找大类
     */
    @DeleteMapping("/precise/category/{categoryUid}")
    public R<Boolean> deletePreciseCategory(@PathVariable String categoryUid) {
        boolean success = llmManageService.deletePreciseCategory(categoryUid);
        return success ? R.ok(true) : R.error("删除大类失败");
    }
    
    /**
     * 获取精确查找大类下的条目列表
     */
    @GetMapping("/precise/entries/{categoryUid}")
    public R<Map<String, Object>> getPreciseEntries(@PathVariable String categoryUid) {
        Map<String, Object> result = llmManageService.getPreciseEntries(categoryUid);
        return R.ok(result);
    }
    
    /**
     * 创建精确查找条目
     */
    @PostMapping("/precise/entry/{categoryUid}")
    public R<Map<String, Object>> createPreciseEntry(
            @PathVariable String categoryUid,
            @RequestBody @Valid PreciseEntryDTO entryDTO) {
        
        Map<String, Object> result = llmManageService.createPreciseEntry(
                categoryUid, 
                entryDTO.getDescription(), 
                entryDTO.getContent(), 
                entryDTO.getKeywords(), 
                entryDTO.getWeight(), 
                entryDTO.getIsEnabled()
        );
        
        if (result == null) {
            return R.error("创建条目失败");
        }
        return R.ok(result);
    }
    
    /**
     * 更新精确查找条目
     */
    @PutMapping("/precise/entry/{entryUid}")
    public R<Map<String, Object>> updatePreciseEntry(
            @PathVariable String entryUid,
            @RequestBody PreciseEntryDTO entryDTO) {
        
        Map<String, Object> result = llmManageService.updatePreciseEntry(
                entryUid, 
                entryDTO.getDescription(), 
                entryDTO.getContent(), 
                entryDTO.getKeywords(), 
                entryDTO.getWeight(), 
                entryDTO.getIsEnabled()
        );
        
        if (result == null) {
            return R.error("更新条目失败");
        }
        return R.ok(result);
    }
    
    /**
     * 删除精确查找条目
     */
    @DeleteMapping("/precise/entry/{entryUid}")
    public R<Boolean> deletePreciseEntry(@PathVariable String entryUid) {
        boolean success = llmManageService.deletePreciseEntry(entryUid);
        return success ? R.ok(true) : R.error("删除条目失败");
    }
    
    // ========== 提示词模板管理 ==========
    
    /**
     * 获取所有提示词模板ID列表
     */
    @GetMapping("/templates")
    public R<List<String>> getTemplateIds() {
        List<String> templateIds = llmManageService.getTemplateIds();
        return R.ok(templateIds);
    }
    
    /**
     * 获取提示词模板详情
     */
    @GetMapping("/templates/{templateId}")
    public R<Map<String, Object>> getTemplateDetail(@PathVariable String templateId) {
        Map<String, Object> template = llmManageService.getTemplateDetail(templateId);
        if (template.isEmpty()) {
            return R.error("模板不存在");
        }
        return R.ok(template);
    }
    
    /**
     * 创建或更新提示词模板
     */
    @PostMapping("/templates")
    public R<Boolean> saveOrUpdateTemplate(@RequestBody @Valid TemplateCreateDTO templateDTO) {
        Map<String, Map<String, Object>> subTemplates = new HashMap<>();
        
        // 将DTO中的子模板转换为FastAPI需要的格式
        templateDTO.getSubTemplates().forEach((key, subTemplate) -> {
            Map<String, Object> subMap = new HashMap<>();
            subMap.put("template", subTemplate.getTemplate());
            subMap.put("description", subTemplate.getDescription());
            subMap.put("parameters", subTemplate.getParameters());
            // 添加排序字段
            subMap.put("order", subTemplate.getOrder());
            subTemplates.put(key, subMap);
        });
        
        boolean success = llmManageService.saveOrUpdateTemplate(
                templateDTO.getTemplateId(),
                templateDTO.getDescription(),
                subTemplates
        );
        
        return success ? R.ok(true) : R.error("保存模板失败");
    }
    
    /**
     * 删除提示词模板
     */
    @DeleteMapping("/templates/{templateId}")
    public R<Boolean> deleteTemplate(@PathVariable String templateId) {
        boolean success = llmManageService.deleteTemplate(templateId);
        return success ? R.ok(true) : R.error("删除模板失败");
    }
} 