package edu.haut.baoxinyang.server.dto.llm.manage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * 模板创建/更新DTO
 */
@Data
public class TemplateCreateDTO {
    
    /**
     * 模板ID
     */
    @NotBlank(message = "模板ID不能为空")
    private String templateId;
    
    /**
     * 模板描述
     */
    @NotBlank(message = "模板描述不能为空")
    private String description;
    
    /**
     * 子模板列表
     */
    @NotEmpty(message = "子模板不能为空")
    private Map<String, SubTemplateDTO> subTemplates;
} 