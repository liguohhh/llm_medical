package edu.haut.baoxinyang.server.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 聊天设置数据传输对象
 */
@Data
public class ChatSettingsDTO {
    
    /**
     * 智能体ID
     */
    private Long agentId;
    
    /**
     * 智能体名称
     */
    private String agentName;
    
    /**
     * 医疗方向
     */
    private String directionName;
    
    /**
     * 模板ID
     */
    private String templateId;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 支持的模板参数列表
     */
    private List<TemplateParam> templateParams;
    
    /**
     * 向量数据库命名空间
     */
    private List<String> vectorNamespaces;
    
    /**
     * 精确查找数据库类别
     */
    private List<String> preciseCategories;
    
    /**
     * 模板参数定义
     */
    @Data
    public static class TemplateParam {
        /**
         * 参数名称
         */
        private String name;
        
        /**
         * 参数描述
         */
        private String description;
        
        /**
         * 是否必填
         */
        private boolean required;
        
        /**
         * 默认值
         */
        private String defaultValue;
        
        /**
         * 可选值列表（如果有）
         */
        private List<String> options;
    }
} 