package edu.haut.baoxinyang.server.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话详情数据传输对象
 */
@Data
public class ConversationDetailDTO {
    
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
     * 消息列表
     */
    private List<MessageDTO> messages;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 