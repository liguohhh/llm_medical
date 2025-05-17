package edu.haut.baoxinyang.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息数据传输对象
 */
@Data
public class MessageDTO {
    
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：0-用户消息，1-系统消息
     */
    private Integer type;
    
    /**
     * 消息时间
     */
    private LocalDateTime timestamp;
} 