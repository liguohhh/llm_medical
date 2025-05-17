package edu.haut.baoxinyang.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.haut.baoxinyang.server.entity.Conversation;

import java.util.List;

/**
 * 对话Service接口
 */
public interface ConversationService extends IService<Conversation> {
    
    /**
     * 根据用户ID查询对话列表
     * @param userId 用户ID
     * @return 对话列表
     */
    List<Conversation> getByUserId(Long userId);
    
    /**
     * 根据UID查询对话
     * @param uid 对话UID
     * @return 对话
     */
    Conversation getByUid(String uid);
    
    /**
     * 根据用户ID和智能体ID查询未结束的对话
     * @param userId 用户ID
     * @param agentId 智能体ID
     * @return 未结束的对话列表
     */
    List<Conversation> getUnfinishedByUserIdAndAgentId(Long userId, Long agentId);
    
} 