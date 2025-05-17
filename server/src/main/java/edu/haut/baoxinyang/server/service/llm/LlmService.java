package edu.haut.baoxinyang.server.service.llm;

import edu.haut.baoxinyang.server.dto.MessageDTO;
import edu.haut.baoxinyang.server.dto.llm.LlmRequestDTO;
import edu.haut.baoxinyang.server.dto.llm.LlmResponseDTO;
import edu.haut.baoxinyang.server.entity.Agent;
import edu.haut.baoxinyang.server.entity.User;

import java.util.List;
import java.util.Map;

/**
 * LLM服务接口
 */
public interface LlmService {
    
    /**
     * 发送非流式请求
     * 
     * @param request LLM请求对象
     * @return LLM响应对象
     */
    LlmResponseDTO ask(LlmRequestDTO request);
    
    /**
     * 发送流式请求
     * 
     * @param request LLM请求对象
     * @param callback SSE回调
     */
    void stream(LlmRequestDTO request, SseCallback callback);
    
    /**
     * 根据对话内容构建LLM请求
     * 
     * @param agent 智能体
     * @param user 用户
     * @param message 当前消息
     * @param history 历史消息
     * @return LLM请求对象
     */
    LlmRequestDTO buildRequest(Agent agent, User user, String message, List<MessageDTO> history);
    
    /**
     * 根据对话内容构建用户参数
     * 
     * @param user 用户
     * @param message 当前消息
     * @return 用户参数Map
     */
    Map<String, String> buildUserParams(User user, String message);
} 