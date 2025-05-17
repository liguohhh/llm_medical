package edu.haut.baoxinyang.server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 处方实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prescription")
public class Prescription extends BaseEntity {
    
    /**
     * 病人ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 智能体ID
     */
    @TableField("agent_id")
    private Long agentId;
    
    /**
     * 医疗方向ID
     */
    @TableField("direction_id")
    private Long directionId;
    
    /**
     * 处方内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 审核状态：0-未审核，1-审核通过，2-审核不通过
     */
    @TableField("review_status")
    private Integer reviewStatus;
    
    /**
     * 审核医生ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;
    
    /**
     * 审核意见
     */
    @TableField("review_comment")
    private String reviewComment;
    
    /**
     * 关联的病人（非数据库字段）
     */
    @TableField(exist = false)
    private User user;
    
    /**
     * 关联的智能体（非数据库字段）
     */
    @TableField(exist = false)
    private Agent agent;
    
    /**
     * 关联的医疗方向（非数据库字段）
     */
    @TableField(exist = false)
    private MedicalDirection direction;
    
    /**
     * 关联的审核医生（非数据库字段）
     */
    @TableField(exist = false)
    private User reviewer;
} 