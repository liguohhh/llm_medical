package edu.haut.baoxinyang.server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("conversation")
public class Conversation extends BaseEntity {
    
    /**
     * 对话唯一标识
     */
    @TableField("uid")
    private String uid;
    
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
     * 对话内容，JSON格式存储
     */
    @TableField("content")
    private String content;
    
    /**
     * 是否结束：0-未结束，1-已结束
     */
    @TableField("is_finished")
    private Integer isFinished;
    
    /**
     * 处方ID，若已开处方则关联
     */
    @TableField("prescription_id")
    private Long prescriptionId;
    
    /**
     * 关联的用户（非数据库字段）
     */
    @TableField(exist = false)
    private User user;
    
    /**
     * 关联的智能体（非数据库字段）
     */
    @TableField(exist = false)
    private Agent agent;
    
    /**
     * 关联的处方（非数据库字段）
     */
    @TableField(exist = false)
    private Prescription prescription;
} 