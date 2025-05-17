package edu.haut.baoxinyang.server.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 医疗方向创建/更新DTO
 */
@Data
public class DirectionCreateDTO {
    
    /**
     * 医疗方向ID (更新时必填)
     */
    private Long id;
    
    /**
     * 医疗方向名称
     */
    @NotBlank(message = "医疗方向名称不能为空")
    private String name;
    
    /**
     * 具体描述
     */
    private String description;
} 