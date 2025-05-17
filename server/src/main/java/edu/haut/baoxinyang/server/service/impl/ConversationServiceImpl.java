package edu.haut.baoxinyang.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.haut.baoxinyang.server.entity.Conversation;
import edu.haut.baoxinyang.server.mapper.ConversationMapper;
import edu.haut.baoxinyang.server.service.ConversationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对话Service实现类
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {
    
    @Override
    public List<Conversation> getByUserId(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getUpdateTime);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public Conversation getByUid(String uid) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUid, uid);
        return baseMapper.selectOne(wrapper);
    }
    
    @Override
    public List<Conversation> getUnfinishedByUserIdAndAgentId(Long userId, Long agentId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getAgentId, agentId)
                .eq(Conversation::getIsFinished, 0)  // 未结束
                .orderByDesc(Conversation::getUpdateTime);
        return baseMapper.selectList(wrapper);
    }
    
} 