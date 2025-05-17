package edu.haut.baoxinyang.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方数据传输对象
 */
@Data
public class PrescriptionDTO {
    
    /**
     * 处方ID
     */
    private Long id;
    
    /**
     * 病人ID
     */
    private Long userId;
    
    /**
     * 病人姓名
     */
    private String userName;
    
    /**
     * 智能体ID
     */
    private Long agentId;
    
    /**
     * 智能体名称
     */
    private String agentName;
    
    /**
     * 医疗方向ID
     */
    private Long directionId;
    
    /**
     * 医疗方向名称
     */
    private String directionName;
    
    /**
     * 处方内容
     */
    private String content;
    
    /**
     * 审核状态：0-未审核，1-审核通过，2-审核不通过
     */
    private Integer reviewStatus;
    
    /**
     * 审核医生ID
     */
    private Long reviewerId;
    
    /**
     * 审核医生姓名
     */
    private String reviewerName;
    
    /**
     * 审核意见
     */
    private String reviewComment;
    
    /**
     * 关联的对话ID
     */
    private Long conversationId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 