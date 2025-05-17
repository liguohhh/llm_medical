package edu.haut.baoxinyang.server.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * LLM响应数据传输对象
 */
@Data
public class LlmResponseDTO {
    
    /**
     * 模型的回答
     */
    private String answer;
    
    /**
     * 向量检索的内容
     */
    @JsonProperty("vector_content")
    private String vectorContent;
    
    /**
     * 精确匹配的内容
     */
    @JsonProperty("precise_content")
    private String preciseContent;
} 