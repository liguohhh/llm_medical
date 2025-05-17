package edu.haut.baoxinyang.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处方审核DTO
 */
@Data
public class PrescriptionReviewDTO {
    
    /**
     * 审核状态：1-通过，2-拒绝
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;
    
    /**
     * 审核意见
     */
    private String comment;
} 