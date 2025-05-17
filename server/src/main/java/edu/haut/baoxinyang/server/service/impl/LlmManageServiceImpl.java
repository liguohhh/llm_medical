package edu.haut.baoxinyang.server.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.haut.baoxinyang.server.config.LlmApiConfig;
import edu.haut.baoxinyang.server.service.LlmManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * LLM后端管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmManageServiceImpl implements LlmManageService {
    
    private final LlmApiConfig config;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建OkHttpClient实例
     */
    private OkHttpClient getHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }
    
    /**
     * JSON媒体类型
     */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    /**
     * 执行GET请求
     */
    private <T> T executeGetRequest(String url, TypeReference<T> typeReference) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("GET请求失败: {}, 状态码: {}", url, response.code());
                    return null;
                }
                
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, typeReference);
            }
        } catch (Exception e) {
            log.error("执行GET请求异常: {}", url, e);
            return null;
        }
    }
    
    /**
     * 执行POST请求
     */
    private <T> T executePostRequest(String url, Object requestBody, TypeReference<T> typeReference) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, JSON);
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("POST请求失败: {}, 状态码: {}", url, response.code());
                    return null;
                }
                
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, typeReference);
            }
        } catch (Exception e) {
            log.error("执行POST请求异常: {}", url, e);
            return null;
        }
    }
    
    /**
     * 执行PUT请求
     */
    private <T> T executePutRequest(String url, Object requestBody, TypeReference<T> typeReference) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, JSON);
            
            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .build();
            
            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("PUT请求失败: {}, 状态码: {}", url, response.code());
                    return null;
                }
                
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, typeReference);
            }
        } catch (Exception e) {
            log.error("执行PUT请求异常: {}", url, e);
            return null;
        }
    }
    
    /**
     * 执行DELETE请求
     */
    private boolean executeDeleteRequest(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .delete()
                    .build();
            
            try (Response response = getHttpClient().newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.error("执行DELETE请求异常: {}", url, e);
            return false;
        }
    }
    
    // ========== 向量数据库管理实现 ==========
    
    @Override
    public List<String> getVectorNamespaces() {
        String url = config.getVectorUrl() + "/namespaces";
        
        Map<String, Object> result = executeGetRequest(url, new TypeReference<Map<String, Object>>() {});
        if (result != null && "success".equals(result.get("status"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            @SuppressWarnings("unchecked")
            List<String> namespaces = (List<String>) data.get("namespaces");
            return namespaces;
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public List<Map<String, Object>> getVectorDocuments(String namespace) {
        String url = config.getVectorUrl() + "/documents/" + namespace;
        
        Map<String, Object> result = executeGetRequest(url, new TypeReference<Map<String, Object>>() {});
        if (result != null && "success".equals(result.get("status"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> documents = (List<Map<String, Object>>) data.get("documents");
            return documents;
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Object> uploadVectorDocument(MultipartFile file, String namespace, Integer chunkSize, Integer chunkOverlap) {
        String url = config.getVectorUrl() + "/upload";
        
        try {
            OkHttpClient client = getHttpClient();
            
            // 构建multipart请求体
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("namespace", namespace);
            
            if (chunkSize != null) {
                bodyBuilder.addFormDataPart("chunk_size", String.valueOf(chunkSize));
            }
            
            if (chunkOverlap != null) {
                bodyBuilder.addFormDataPart("chunk_overlap", String.valueOf(chunkOverlap));
            }
            
            // 添加文件
            String filename = file.getOriginalFilename();
            byte[] fileBytes = file.getBytes();
            bodyBuilder.addFormDataPart("file", filename,
                    RequestBody.create(fileBytes, MediaType.parse(file.getContentType())));
            
            RequestBody requestBody = bodyBuilder.build();
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("上传文档失败: {}, 状态码: {}", url, response.code());
                    return null;
                }
                
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.error("上传文档异常: {}", url, e);
            return null;
        }
    }
    
    @Override
    public boolean deleteVectorDocument(String namespace, String docId) {
        String url = config.getVectorUrl() + "/document?namespace=" + namespace + "&doc_id=" + docId;
        return executeDeleteRequest(url);
    }
    
    @Override
    public boolean deleteVectorNamespace(String namespace) {
        String url = config.getVectorUrl() + "/namespace/" + namespace;
        return executeDeleteRequest(url);
    }
    
    @Override
    public boolean createVectorNamespace(String namespace) {
        String url = config.getVectorUrl() + "/namespace";
        
        try {
            OkHttpClient client = getHttpClient();
            
            // 构建表单请求体
            RequestBody formBody = new FormBody.Builder()
                    .add("namespace", namespace)
                    .build();
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("创建命名空间失败: {}, 状态码: {}", url, response.code());
                    return false;
                }
                
                String responseBody = response.body().string();
                Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
                return "success".equals(result.get("status"));
            }
        } catch (Exception e) {
            log.error("创建命名空间异常: {}", url, e);
            return false;
        }
    }
    
    // ========== 精确查找数据库管理实现 ==========
    
    @Override
    public List<Map<String, Object>> getPreciseCategories() {
        String url = config.getPreciseUrl() + "/categories";
        
        Map<String, Object> result = executeGetRequest(url, new TypeReference<Map<String, Object>>() {});
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> categories = (List<Map<String, Object>>) data.get("categories");
            return categories;
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Object> createPreciseCategory(String name) {
        String url = config.getPreciseUrl() + "/category";
        
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", name);
        
        Map<String, Object> result = executePostRequest(url, requestBody, new TypeReference<Map<String, Object>>() {});
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return data;
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> updatePreciseCategory(String categoryUid, String name) {
        String url = config.getPreciseUrl() + "/category/" + categoryUid;
        
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", name);
        
        Map<String, Object> result = executePutRequest(url, requestBody, new TypeReference<Map<String, Object>>() {});
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return data;
        }
        
        return null;
    }
    
    @Override
    public boolean deletePreciseCategory(String categoryUid) {
        String url = config.getPreciseUrl() + "/category/" + categoryUid;
        return executeDeleteRequest(url);
    }
    
    @Override
    public Map<String, Object> getPreciseEntries(String categoryUid) {
        String url = config.getPreciseUrl() + "/entries/" + categoryUid;
        
        Map<String, Object> result = executeGetRequest(url, new TypeReference<Map<String, Object>>() {});
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return data;
        }
        
        return new HashMap<>();
    }
    
    @Override
    public Map<String, Object> createPreciseEntry(String categoryUid, String description, String content, 
                                                List<String> keywords, Integer weight, Boolean isEnabled) {
        String url = config.getPreciseUrl() + "/entry/" + categoryUid;
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("description", description);
        requestBody.put("content", content);
        requestBody.put("keywords", keywords);
        if (weight != null) {
            requestBody.put("weight", weight);
        }
        if (isEnabled != null) {
            requestBody.put("is_enabled", isEnabled);
        }
        
        Map<String, Object> result = executePostRequest(url, requestBody, new TypeReference<Map<String, Object>>() {});
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return data;
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> updatePreciseEntry(String entryUid, String description, String content, 
                                                List<String> keywords, Integer weight, Boolean isEnabled) {
        String url = config.getPreciseUrl() + "/entry/" + entryUid;
        
        Map<String, Object> requestBody = new HashMap<>();
        if (description != null) {
            requestBody.put("description", description);
        }
        if (content != null) {
            requestBody.put("content", content);
        }
        if (keywords != null) {
            requestBody.put("keywords", keywords);
        }
        if (weight != null) {
            requestBody.put("weight", weight);
        }
        if (isEnabled != null) {
            requestBody.put("is_enabled", isEnabled);
        }
        
        Map<String, Object> result = executePutRequest(url, requestBody, new TypeReference<Map<String, Object>>() {});
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return data;
        }
        
        return null;
    }
    
    @Override
    public boolean deletePreciseEntry(String entryUid) {
        String url = config.getPreciseUrl() + "/entry/" + entryUid;
        return executeDeleteRequest(url);
    }
    
    // ========== 提示词模板管理实现 ==========
    
    @Override
    public List<String> getTemplateIds() {
        String url = config.getTemplateUrl();
        
        Map<String, Object> result = executeGetRequest(url, new TypeReference<Map<String, Object>>() {});
        if (result != null && "success".equals(result.get("status"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            @SuppressWarnings("unchecked")
            List<String> templates = (List<String>) data.get("templates");
            return templates;
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Object> getTemplateDetail(String templateId) {
        String url = config.getTemplateUrl() + "/" + templateId;
        
        Map<String, Object> result = executeGetRequest(url, new TypeReference<Map<String, Object>>() {});
        if (result != null && "success".equals(result.get("status"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return data;
        }
        
        return new HashMap<>();
    }
    
    @Override
    public boolean saveOrUpdateTemplate(String templateId, String description, Map<String, Map<String, Object>> subTemplates) {
        String url = config.getTemplateUrl() + "/" + templateId;
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("description", description);
        requestBody.put("sub_templates", subTemplates);
        
        // 检查模板是否已存在
        Map<String, Object> templateDetail = getTemplateDetail(templateId);
        boolean templateExists = templateDetail != null && !templateDetail.isEmpty();
        
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {};
        Map<String, Object> result;
        
        if (templateExists) {
            // 更新模板
            result = executePutRequest(url, requestBody, typeReference);
        } else {
            // 创建模板
            result = executePostRequest(url, requestBody, typeReference);
        }
        
        return result != null && "success".equals(result.get("status"));
    }
    
    @Override
    public boolean deleteTemplate(String templateId) {
        String url = config.getTemplateUrl() + "/" + templateId;
        return executeDeleteRequest(url);
    }
} 