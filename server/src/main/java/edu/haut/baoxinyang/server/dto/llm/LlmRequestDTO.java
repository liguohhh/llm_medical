package edu.haut.baoxinyang.server.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LLM请求数据传输对象
 */
@Data
public class LlmRequestDTO {
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 模型设置
     */
    @JsonProperty("model_settings")
    private ModelSettings modelSettings;
    
    /**
     * 模板配置
     */
    @JsonProperty("template_config")
    private TemplateConfig templateConfig;
    
    /**
     * 向量搜索配置
     */
    @JsonProperty("vector_search_config")
    private VectorSearchConfig vectorSearchConfig;
    
    /**
     * 精确查询配置
     */
    @JsonProperty("precise_search_config")
    private PreciseSearchConfig preciseSearchConfig;
    
    /**
     * 历史消息
     */
    private List<MessageItem> history = new ArrayList<>();
    
    /**
     * 模型设置
     */
    @Data
    public static class ModelSettings {
        @JsonProperty("model_name")
        private String modelName;
        
        @JsonProperty("api_key")
        private String apiKey;
        
        @JsonProperty("base_url")
        private String baseUrl;
    }
    
    /**
     * 模板配置
     */
    @Data
    public static class TemplateConfig {
        @JsonProperty("template_id")
        private String templateId;
        
        @JsonProperty("sub_template_ids")
        private List<String> subTemplateIds;
        
        private Map<String, String> params;
    }
    
    /**
     * 向量搜索配置
     */
    @Data
    public static class VectorSearchConfig {
        private List<String> namespaces;
        
        @JsonProperty("n_results")
        private Integer nResults;
        
        @JsonProperty("metadata_filter")
        private Map<String, Object> metadataFilter;
        
        @JsonProperty("rag_history_count")
        private Integer ragHistoryCount;
    }
    
    /**
     * 精确查询配置
     */
    @Data
    public static class PreciseSearchConfig {
        private List<String> categories;
        
        @JsonProperty("max_results")
        private Integer maxResults;
        
        @JsonProperty("search_depth")
        private Integer searchDepth;
        
        @JsonProperty("rag_history_count")
        private Integer ragHistoryCount;
    }
    
    /**
     * 消息项
     */
    @Data
    public static class MessageItem {
        private String role;
        private String content;
        
        public MessageItem() {
        }
        
        public MessageItem(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
} 