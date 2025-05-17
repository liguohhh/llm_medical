package edu.haut.baoxinyang.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.haut.baoxinyang.server.entity.Agent;
import edu.haut.baoxinyang.server.mapper.AgentMapper;
import edu.haut.baoxinyang.server.service.AgentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体Service实现类
 */
@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {
    
    @Override
    public List<Agent> getByDirectionId(Long directionId) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getDirectionId, directionId);
        return baseMapper.selectList(wrapper);
    }
    
} 