package edu.haut.baoxinyang.server.dto.llm.manage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 子模板DTO
 */
@Data
public class SubTemplateDTO {
    
    /**
     * 模板内容
     */
    @NotBlank(message = "模板内容不能为空")
    private String template;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 模板参数列表
     */
    private List<String> parameters;
    
    /**
     * 模板排序值，从1开始
     */
    private Integer order;
} 