package edu.haut.baoxinyang.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.haut.baoxinyang.server.entity.Agent;

import java.util.List;

/**
 * 智能体Service接口
 */
public interface AgentService extends IService<Agent> {
    
    /**
     * 根据医疗方向ID查询智能体列表
     * @param directionId 医疗方向ID
     * @return 智能体列表
     */
    List<Agent> getByDirectionId(Long directionId);
    
} 