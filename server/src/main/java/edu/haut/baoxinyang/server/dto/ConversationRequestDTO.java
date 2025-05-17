package edu.haut.baoxinyang.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 对话请求数据传输对象
 */
@Data
public class ConversationRequestDTO {
    
    /**
     * 对话ID，创建新对话时为null
     */
    private Long conversationId;
    
    /**
     * 智能体ID，创建新对话时必须指定
     */
    private Long agentId;
    
    /**
     * 消息内容
     */
    @NotNull(message = "消息内容不能为空")
    private String message;
    
    /**
     * 向量搜索结果数量
     */
    private Integer vectorResults = 3;
    
    /**
     * 精确搜索结果数量
     */
    private Integer preciseResults = 3;
    
    /**
     * 精确搜索深度
     */
    private Integer searchDepth = 2;
    
    /**
     * 向量搜索历史消息数量
     */
    private Integer vectorHistoryCount = 1;
    
    /**
     * 精确搜索历史消息数量
     */
    private Integer preciseHistoryCount = 1;
    
    /**
     * 模板参数，用于填充模板中的变量
     */
    private Map<String, String> templateParams;
} 