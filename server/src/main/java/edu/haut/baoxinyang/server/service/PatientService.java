
package edu.haut.baoxinyang.server.service;

import edu.haut.baoxinyang.server.dto.*;
import edu.haut.baoxinyang.server.service.llm.SseCallback;

import java.util.List;

/**
 * 病人服务接口
 */
public interface PatientService {
    
    /**
     * 获取所有智能体列表
     * @return 智能体列表
     */
    List<AgentDTO> getAllAgents();
    
    /**
     * 获取病人的历史对话
     * @param userId 病人ID
     * @return 对话列表
     */
    List<ConversationDTO> getConversationsByUserId(Long userId);
    
    /**
     * 获取对话详情
     * @param conversationId 对话ID
     * @param userId 病人ID
     * @return 对话详情
     */
    ConversationDetailDTO getConversationDetail(Long conversationId, Long userId);
    
    /**
     * 创建新对话
     * @param userId 病人ID
     * @param agentId 智能体ID
     * @param firstMessage 首条消息
     * @return 新对话详情
     */
    ConversationDetailDTO createConversation(Long userId, Long agentId, String firstMessage);
    
    /**
     * 发送消息到对话
     * @param conversationId 对话ID
     * @param userId 病人ID
     * @param message 消息内容
     * @param requestDTO 高级请求参数
     * @return 更新后的对话详情
     */
    ConversationDetailDTO sendMessage(Long conversationId, Long userId, String message, ConversationRequestDTO requestDTO);
    
    /**
     * 流式发送消息到对话
     * @param conversationId 对话ID
     * @param userId 病人ID
     * @param message 消息内容
     * @param requestDTO 高级请求参数
     * @param callback SSE回调
     */
    void streamMessage(Long conversationId, Long userId, String message, ConversationRequestDTO requestDTO, SseCallback callback);
    
    /**
     * 获取处方详情
     * @param prescriptionId 处方ID
     * @param userId 病人ID
     * @return 处方详情
     */
    PrescriptionDTO getPrescriptionDetail(Long prescriptionId, Long userId);
    
    /**
     * 获取病人的所有处方
     * @param userId 病人ID
     * @return 处方列表
     */
    List<PrescriptionDTO> getPrescriptionsByUserId(Long userId);
    
    /**
     * 结束对话并生成处方
     * @param conversationId 对话ID
     * @param userId 病人ID
     * @return 生成的处方详情
     */
    PrescriptionDTO generatePrescription(Long conversationId, Long userId);
    
    /**
     * 获取聊天设置
     * @param agentId 智能体ID
     * @return 聊天设置DTO
     */
    ChatSettingsDTO getChatSettings(Long agentId);
    
    /**
     * 从对话内容中提取处方并保存
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param prescriptionContent 处方内容
     * @return 生成的处方
     */
    PrescriptionDTO extractAndSavePrescription(Long conversationId, Long userId, String prescriptionContent);
} 