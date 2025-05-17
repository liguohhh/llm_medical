package edu.haut.baoxinyang.server.dto.llm.manage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 精确查找大类DTO
 */
@Data
public class PreciseCategoryDTO {
    
    /**
     * 大类ID
     */
    private Integer id;
    
    /**
     * 大类UID
     */
    private String uid;
    
    /**
     * 大类名称
     */
    @NotBlank(message = "大类名称不能为空")
    private String name;
} 