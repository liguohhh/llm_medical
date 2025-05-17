package edu.haut.baoxinyang.server.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 智能体创建/更新DTO
 */
@Data
public class AgentCreateDTO {
    
    /**
     * 智能体ID (更新时必填)
     */
    private Long id;
    
    /**
     * 智能体名称
     */
    @NotBlank(message = "智能体名称不能为空")
    private String name;
    
    /**
     * 医疗方向ID
     */
    @NotNull(message = "医疗方向不能为空")
    private Long directionId;
    
    /**
     * 对接的大模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
    
    /**
     * 对接的大模型链接地址
     */
    @NotBlank(message = "模型链接地址不能为空")
    private String modelUrl;
    
    /**
     * 对接的大模型的apikey
     */
    @NotBlank(message = "API Key不能为空")
    private String apiKey;
    
    /**
     * 使用的模板ID
     */
    @NotBlank(message = "模板ID不能为空")
    private String templateId;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 模板参数，JSON格式存储
     */
    private String templateParameters;
    
    /**
     * 使用的向量数据库命名空间
     */
    private List<String> vectorNamespaces;
    
    /**
     * 使用的精确查找数据库名称
     */
    private String preciseDbName;
    
    /**
     * 精确查找数据库UID列表
     */
    private List<String> preciseDbUids;
} 