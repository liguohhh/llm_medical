package edu.haut.baoxinyang.server.dto;

import lombok.Data;

/**
 * 智能体数据传输对象
 */
@Data
public class AgentDTO {
    
    /**
     * 智能体ID
     */
    private Long id;
    
    /**
     * 智能体名称
     */
    private String name;
    
    /**
     * 医疗方向ID
     */
    private Long directionId;
    
    /**
     * 医疗方向名称
     */
    private String directionName;
    
    /**
     * 医疗方向描述
     */
    private String directionDescription;
} 