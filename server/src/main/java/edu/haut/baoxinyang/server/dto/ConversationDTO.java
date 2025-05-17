package edu.haut.baoxinyang.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话数据传输对象
 */
@Data
public class ConversationDTO {
    
    /**
     * 对话ID
     */
    private Long id;
    
    /**
     * 对话唯一标识
     */
    private String uid;
    
    /**
     * 病人ID
     */
    private Long userId;
    
    /**
     * 智能体ID
     */
    private Long agentId;
    
    /**
     * 智能体名称
     */
    private String agentName;
    
    /**
     * 医疗方向名称
     */
    private String directionName;
    
    /**
     * 是否结束：0-未结束，1-已结束
     */
    private Integer isFinished;
    
    /**
     * 处方ID
     */
    private Long prescriptionId;
    
    /**
     * 最后一条消息内容（用于预览）
     */
    private String lastMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 