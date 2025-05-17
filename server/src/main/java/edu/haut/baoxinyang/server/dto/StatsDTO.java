package edu.haut.baoxinyang.server.dto;

import lombok.Data;

/**
 * 统计数据DTO
 */
@Data
public class StatsDTO {
    
    /**
     * 总处方数
     */
    private Integer totalPrescriptions = 0;
    
    /**
     * 待审核处方数
     */
    private Integer pendingReview = 0;
    
    /**
     * 已通过处方数
     */
    private Integer approvedPrescriptions = 0;
    
    /**
     * 已拒绝处方数
     */
    private Integer rejectedPrescriptions = 0;
} 