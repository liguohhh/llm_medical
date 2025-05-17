package edu.haut.baoxinyang.server.service.impl;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.haut.baoxinyang.server.dto.*;
import edu.haut.baoxinyang.server.dto.llm.LlmRequestDTO;
import edu.haut.baoxinyang.server.dto.llm.LlmResponseDTO;
import edu.haut.baoxinyang.server.entity.*;
import edu.haut.baoxinyang.server.service.*;
import edu.haut.baoxinyang.server.service.llm.LlmService;
import edu.haut.baoxinyang.server.service.llm.SseCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 病人服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    
    private final AgentService agentService;
    private final UserService userService;
    private final ConversationService conversationService;
    private final PrescriptionService prescriptionService;
    private final MedicalDirectionService medicalDirectionService;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<AgentDTO> getAllAgents() {
        // 获取所有智能体
        List<Agent> agents = agentService.list();
        
        // 转换为DTO
        return agents.stream().map(agent -> {
            AgentDTO dto = new AgentDTO();
            BeanUtils.copyProperties(agent, dto);
            
            // 获取医疗方向
            if (agent.getDirectionId() != null) {
                MedicalDirection direction = medicalDirectionService.getById(agent.getDirectionId());
                if (direction != null) {
                    dto.setDirectionName(direction.getName());
                    dto.setDirectionDescription(direction.getDescription());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<ConversationDTO> getConversationsByUserId(Long userId) {
        // 获取用户的所有对话
        List<Conversation> conversations = conversationService.getByUserId(userId);
        
        // 转换为DTO
        return conversations.stream().map(conversation -> {
            ConversationDTO dto = new ConversationDTO();
            BeanUtils.copyProperties(conversation, dto);
            
            // 设置智能体名称和医疗方向名称
            if (conversation.getAgentId() != null) {
                Agent agent = agentService.getById(conversation.getAgentId());
                if (agent != null) {
                    dto.setAgentName(agent.getName());
                    
                    // 设置医疗方向名称
                    if (agent.getDirectionId() != null) {
                        MedicalDirection direction = medicalDirectionService.getById(agent.getDirectionId());
                        if (direction != null) {
                            dto.setDirectionName(direction.getName());
                        } else {
                            dto.setDirectionName("未知科室");
                        }
                    } else {
                        dto.setDirectionName("未知科室");
                    }
                } else {
                    dto.setAgentName("未知助手");
                    dto.setDirectionName("未知科室");
                }
            } else {
                dto.setAgentName("未知助手");
                dto.setDirectionName("未知科室");
            }
            
            // 解析对话内容，提取最后一条消息作为预览
            String lastMessage = "无消息内容";
            List<MessageDTO> messages = parseConversationContent(conversation.getContent());
            if (!messages.isEmpty()) {
                // 获取最后一条消息
                MessageDTO lastMsg = messages.get(messages.size() - 1);
                if (lastMsg != null && lastMsg.getContent() != null && !lastMsg.getContent().isEmpty()) {
                    // 截取最后一条消息的前50个字符作为预览
                    String content = lastMsg.getContent();
                    lastMessage = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                }
            }
            dto.setLastMessage(lastMessage);
            
            // 确保时间字段不为空
            if (dto.getCreateTime() == null) {
                dto.setCreateTime(LocalDateTime.now());
            }
            if (dto.getUpdateTime() == null) {
                dto.setUpdateTime(LocalDateTime.now());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public ConversationDetailDTO getConversationDetail(Long conversationId, Long userId) {
        // 获取对话
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return null;
        }
        
        // 转换为DTO
        ConversationDetailDTO dto = new ConversationDetailDTO();
        BeanUtils.copyProperties(conversation, dto);
        
        // 获取用户信息
        User user = userService.getById(userId);
        if (user != null) {
            dto.setUserName(user.getRealName());
        }
        
        // 获取智能体信息
        Agent agent = agentService.getById(conversation.getAgentId());
        if (agent != null) {
            dto.setAgentName(agent.getName());
            
            // 获取医疗方向
            MedicalDirection direction = medicalDirectionService.getById(agent.getDirectionId());
            if (direction != null) {
                dto.setDirectionName(direction.getName());
            }
        }
        
        // 解析对话内容（这里简化处理，实际需要解析conversation.getContent()）
        List<MessageDTO> messages = parseConversationContent(conversation.getContent());
        dto.setMessages(messages);
        
        return dto;
    }
    
    /**
     * 解析对话内容JSON字符串为消息列表
     * @param content 对话内容JSON字符串
     * @return 消息列表
     */
    private List<MessageDTO> parseConversationContent(String content) {
        List<MessageDTO> messages = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return messages;
        }
        
        try {
            // 假设content是JSON数组格式的字符串
            messages = objectMapper.readValue(content, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MessageDTO.class));
        } catch (JsonProcessingException e) {
            log.error("解析对话内容失败: {}", e.getMessage());
        }
        
        return messages;
    }
    
    /**
     * 将消息列表序列化为JSON字符串
     * @param messages 消息列表
     * @return JSON字符串
     */
    private String serializeMessages(List<MessageDTO> messages) {
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            log.error("序列化消息列表失败: {}", e.getMessage());
            return "[]";
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationDetailDTO createConversation(Long userId, Long agentId, String firstMessage) {
        // 检查用户和智能体是否存在
        User user = userService.getById(userId);
        Agent agent = agentService.getById(agentId);
        if (user == null || agent == null) {
            return null;
        }
        
        // 创建新对话
        Conversation conversation = new Conversation();
        conversation.setUid(IdUtil.fastSimpleUUID());
        conversation.setUserId(userId);
        conversation.setAgentId(agentId);
        conversation.setIsFinished(0);
        
        // 创建初始消息（这里简化处理，实际需要创建JSON格式的消息内容）
        List<MessageDTO> messages = new ArrayList<>();
        MessageDTO userMessage = new MessageDTO();
        userMessage.setId(IdUtil.fastSimpleUUID());
        userMessage.setContent(firstMessage);
        userMessage.setType(0); // 用户消息
        userMessage.setTimestamp(LocalDateTime.now());
        messages.add(userMessage);
        
        // 调用LLM服务获取智能体回复
        LlmRequestDTO llmRequest = llmService.buildRequest(agent, user, firstMessage, new ArrayList<>());
        LlmResponseDTO llmResponse = llmService.ask(llmRequest);
        
        // 添加智能体回复
        MessageDTO agentMessage = new MessageDTO();
        agentMessage.setId(IdUtil.fastSimpleUUID());
        agentMessage.setContent(llmResponse != null ? llmResponse.getAnswer() : "抱歉，我暂时无法回答您的问题。");
        agentMessage.setType(1); // 系统消息
        agentMessage.setTimestamp(LocalDateTime.now());
        messages.add(agentMessage);
        
        // 保存对话内容
        conversation.setContent(serializeMessages(messages));
        conversationService.save(conversation);
        
        // 转换为DTO
        ConversationDetailDTO dto = new ConversationDetailDTO();
        BeanUtils.copyProperties(conversation, dto);
        dto.setUserName(user.getRealName());
        dto.setAgentName(agent.getName());
        
        // 获取医疗方向
        MedicalDirection direction = medicalDirectionService.getById(agent.getDirectionId());
        if (direction != null) {
            dto.setDirectionName(direction.getName());
        }
        
        dto.setMessages(messages);
        
        return dto;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationDetailDTO sendMessage(Long conversationId, Long userId, String message, ConversationRequestDTO advancedOptions) {
        // 获取对话
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId) || conversation.getIsFinished() == 1) {
            return null;
        }
        
        // 获取用户和智能体
        User user = userService.getById(userId);
        Agent agent = agentService.getById(conversation.getAgentId());
        if (user == null || agent == null) {
            return null;
        }
        
        // 解析现有对话内容
        List<MessageDTO> messages = parseConversationContent(conversation.getContent());
        
        // 添加用户消息
        MessageDTO userMessage = new MessageDTO();
        userMessage.setId(IdUtil.fastSimpleUUID());
        userMessage.setContent(message);
        userMessage.setType(0); // 用户消息
        userMessage.setTimestamp(LocalDateTime.now());
        messages.add(userMessage);
        
        // 根据对话上下文构建LLM请求
        LlmRequestDTO llmRequest = buildLlmRequest(agent, user, message, messages.subList(0, messages.size() - 1), advancedOptions);
        
        // 调用LLM服务获取智能体回复
        LlmResponseDTO llmResponse = llmService.ask(llmRequest);
        
        // 添加智能体回复
        MessageDTO agentMessage = new MessageDTO();
        agentMessage.setId(IdUtil.fastSimpleUUID());
        agentMessage.setContent(llmResponse != null ? llmResponse.getAnswer() : "抱歉，我暂时无法回答您的问题。");
        agentMessage.setType(1); // 系统消息
        agentMessage.setTimestamp(LocalDateTime.now());
        messages.add(agentMessage);
        
        // 更新对话内容
        conversation.setContent(serializeMessages(messages));
        conversationService.updateById(conversation);
        
        // 构建返回DTO
        ConversationDetailDTO dto = new ConversationDetailDTO();
        BeanUtils.copyProperties(conversation, dto);
        dto.setUserName(user.getRealName());
        dto.setAgentName(agent.getName());
        
        // 获取医疗方向
        MedicalDirection direction = medicalDirectionService.getById(agent.getDirectionId());
        if (direction != null) {
            dto.setDirectionName(direction.getName());
        }
        
        dto.setMessages(messages);
        
        return dto;
    }
    
    @Override
    public void streamMessage(Long conversationId, Long userId, String message, ConversationRequestDTO advancedOptions, SseCallback callback) {
        // 获取对话
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId) || conversation.getIsFinished() == 1) {
            callback.onError(new RuntimeException("对话不存在或无权限访问"));
            return;
        }
        
        // 获取用户和智能体
        User user = userService.getById(userId);
        Agent agent = agentService.getById(conversation.getAgentId());
        if (user == null || agent == null) {
            callback.onError(new RuntimeException("用户或智能体不存在"));
            return;
        }
        
        // 解析现有对话内容
        List<MessageDTO> messages = parseConversationContent(conversation.getContent());
        
        // 添加用户消息
        MessageDTO userMessage = new MessageDTO();
        userMessage.setId(IdUtil.fastSimpleUUID());
        userMessage.setContent(message);
        userMessage.setType(0); // 用户消息
        userMessage.setTimestamp(LocalDateTime.now());
        messages.add(userMessage);
        
        // 更新对话内容（先只添加用户消息）
        conversation.setContent(serializeMessages(messages));
        conversationService.updateById(conversation);
        
        // 预创建智能体消息，使用空内容
        MessageDTO agentMessage = new MessageDTO();
        final String agentMessageId = IdUtil.fastSimpleUUID();
        agentMessage.setId(agentMessageId);
        agentMessage.setContent("");
        agentMessage.setType(1); // 系统消息
        agentMessage.setTimestamp(LocalDateTime.now());
        messages.add(agentMessage);
        
        // 准备LLM请求，使用高级参数
        LlmRequestDTO llmRequest = buildLlmRequest(agent, user, message, messages.subList(0, messages.size() - 2), advancedOptions);
        
        // 创建一个自定义回调来处理流式消息
        SseCallback streamCallback = new SseCallback() {
            private final StringBuilder fullResponse = new StringBuilder();
            
            @Override
            public void onEvent(SseEvent event) {
                try {
                    if (!event.isLastEvent() && event.getData() != null) {
                        // 追加到完整响应中
                        fullResponse.append(event.getData());
                        
                        // 将事件传递给原始回调
                        callback.onEvent(event);
                    } else if (event.isLastEvent()) {
                        // 更新完整消息
                        String completeMessage = fullResponse.toString();
                        
                        // 更新智能体消息
                        agentMessage.setContent(completeMessage);
                        
                        // 更新对话内容
                        conversation.setContent(serializeMessages(messages));
                        conversationService.updateById(conversation);
                        
                        // 标记为完成
                        callback.onEvent(event);
                    }
                } catch (Exception e) {
                    log.error("处理流式响应事件失败", e);
                    onError(e);
                }
            }
            
            @Override
            public void onError(Throwable throwable) {
                log.error("流式请求失败", throwable);
                
                // 如果出错，仍然保存已收到的内容
                if (fullResponse.length() > 0) {
                    agentMessage.setContent(fullResponse.toString());
                } else {
                    agentMessage.setContent("抱歉，处理您的请求时出现错误。");
                }
                
                // 更新对话内容
                conversation.setContent(serializeMessages(messages));
                conversationService.updateById(conversation);
                
                // 将错误传递给原始回调
                callback.onError(throwable);
            }
            
            @Override
            public void onComplete() {
                callback.onComplete();
            }
        };
        
        // 发送流式请求
        llmService.stream(llmRequest, streamCallback);
    }
    
    /**
     * 构建LLM请求，支持高级聊天参数
     */
    private LlmRequestDTO buildLlmRequest(Agent agent, User user, String message, List<MessageDTO> history, ConversationRequestDTO advancedOptions) {
        // 首先使用默认方法创建基础请求
        LlmRequestDTO request = llmService.buildRequest(agent, user, message, history);
        
        // 如果没有高级选项，直接返回基础请求
        if (advancedOptions == null) {
            return request;
        }
        
        // 应用高级向量搜索配置
        if (request.getVectorSearchConfig() != null) {
            request.getVectorSearchConfig().setNResults(advancedOptions.getVectorResults());
            request.getVectorSearchConfig().setRagHistoryCount(advancedOptions.getVectorHistoryCount());
        }
        
        // 应用高级精确查询配置
        if (request.getPreciseSearchConfig() != null) {
            request.getPreciseSearchConfig().setMaxResults(advancedOptions.getPreciseResults());
            request.getPreciseSearchConfig().setSearchDepth(advancedOptions.getSearchDepth());
            request.getPreciseSearchConfig().setRagHistoryCount(advancedOptions.getPreciseHistoryCount());
        }
        
        // 应用自定义模板参数
        if (advancedOptions.getTemplateParams() != null && !advancedOptions.getTemplateParams().isEmpty()) {
            // 合并现有参数和自定义参数，自定义参数优先级更高
            request.getTemplateConfig().getParams().putAll(advancedOptions.getTemplateParams());
        }
        
        return request;
    }
    
    @Override
    public PrescriptionDTO getPrescriptionDetail(Long prescriptionId, Long userId) {
        // 获取处方
        Prescription prescription = prescriptionService.getById(prescriptionId);
        if (prescription == null || !prescription.getUserId().equals(userId)) {
            return null;
        }
        
        // 转换为DTO
        PrescriptionDTO prescriptionDTO = new PrescriptionDTO();
        BeanUtils.copyProperties(prescription, prescriptionDTO);
        
        // 获取智能体名称
        Agent agent = agentService.getById(prescription.getAgentId());
        if (agent != null) {
            prescriptionDTO.setAgentName(agent.getName());
        }
        
        // 获取医疗方向名称
        MedicalDirection direction = medicalDirectionService.getById(prescription.getDirectionId());
        if (direction != null) {
            prescriptionDTO.setDirectionName(direction.getName());
        }
        
        // 获取审核医生姓名
        if (prescription.getReviewerId() != null) {
            User reviewer = userService.getById(prescription.getReviewerId());
            if (reviewer != null) {
                prescriptionDTO.setReviewerName(reviewer.getRealName());
            }
        }
        
        // 获取病人姓名
        User user = userService.getById(prescription.getUserId());
        if (user != null) {
            prescriptionDTO.setUserName(user.getRealName());
        }
        
        // 查询关联的对话ID
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getPrescriptionId, prescriptionId);
        Conversation conversation = conversationService.getOne(queryWrapper);
        if (conversation != null) {
            prescriptionDTO.setConversationId(conversation.getId());
        }
        
        return prescriptionDTO;
    }
    
    /**
     * 获取病人的所有处方
     * @param userId 病人ID
     * @return 处方列表
     */
    @Override
    public List<PrescriptionDTO> getPrescriptionsByUserId(Long userId) {
        // 根据用户ID查询所有处方
        LambdaQueryWrapper<Prescription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Prescription::getUserId, userId);
        queryWrapper.orderByDesc(Prescription::getCreateTime); // 按创建时间倒序
        
        List<Prescription> prescriptions = prescriptionService.list(queryWrapper);
        
        // 转换为DTO列表
        return prescriptions.stream().map(prescription -> {
            PrescriptionDTO dto = new PrescriptionDTO();
            BeanUtils.copyProperties(prescription, dto);
            
            // 获取智能体名称
            Agent agent = agentService.getById(prescription.getAgentId());
            if (agent != null) {
                dto.setAgentName(agent.getName());
            }
            
            // 获取医疗方向名称
            MedicalDirection direction = medicalDirectionService.getById(prescription.getDirectionId());
            if (direction != null) {
                dto.setDirectionName(direction.getName());
            }
            
            // 获取审核医生姓名
            if (prescription.getReviewerId() != null) {
                User reviewer = userService.getById(prescription.getReviewerId());
                if (reviewer != null) {
                    dto.setReviewerName(reviewer.getRealName());
                }
            }
            
            // 获取病人姓名
            User user = userService.getById(prescription.getUserId());
            if (user != null) {
                dto.setUserName(user.getRealName());
            }
            
            // 查询关联的对话ID
            LambdaQueryWrapper<Conversation> conversationWrapper = new LambdaQueryWrapper<>();
            conversationWrapper.eq(Conversation::getPrescriptionId, prescription.getId());
            Conversation conversation = conversationService.getOne(conversationWrapper);
            if (conversation != null) {
                dto.setConversationId(conversation.getId());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionDTO generatePrescription(Long conversationId, Long userId) {
        // 验证对话存在且属于当前用户
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId) || conversation.getIsFinished() == 1) {
            log.error("对话不存在或不属于当前用户: conversationId={}, userId={}", conversationId, userId);
            return null;
        }
        
        // 获取对话内容
        List<MessageDTO> messages = parseConversationContent(conversation.getContent());
        if (messages == null || messages.isEmpty()) {
            log.error("对话内容为空: conversationId={}", conversationId);
            return null;
        }
        
        // 查找最后一条系统消息
        MessageDTO lastSystemMessage = null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getType() == 1) { // 系统消息
                lastSystemMessage = messages.get(i);
                break;
            }
        }
        
        if (lastSystemMessage == null) {
            log.error("未找到系统消息: conversationId={}", conversationId);
            return null;
        }
        
        // 提取处方内容
        String content = lastSystemMessage.getContent();
        String prescriptionContent = null;
        
        // 尝试从消息中提取<处方>标签内容
        Pattern pattern = Pattern.compile("<处方>(.*?)</处方>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            prescriptionContent = matcher.group(1).trim();
        }
        
        // 如果没有找到处方标签，使用整个最后一条消息作为处方内容
        if (prescriptionContent == null || prescriptionContent.isEmpty()) {
            prescriptionContent = content;
        }
        
        // 获取智能体信息
        Agent agent = agentService.getById(conversation.getAgentId());
        if (agent == null) {
            log.error("智能体不存在: agentId={}", conversation.getAgentId());
            return null;
        }
        
        // 创建处方
        Prescription prescription = new Prescription();
        prescription.setUserId(userId);
        prescription.setAgentId(agent.getId());
        prescription.setDirectionId(agent.getDirectionId());
        prescription.setContent(prescriptionContent);
        prescription.setReviewStatus(0); // 未审核
        
        // 保存处方
        prescriptionService.save(prescription);
        
        // 更新对话状态
        conversation.setIsFinished(1); // 标记为已结束
        conversation.setPrescriptionId(prescription.getId()); // 关联处方
        conversationService.updateById(conversation);
        
        // 转换为DTO返回
        return getPrescriptionDetail(prescription.getId(), userId);
    }
    
    /**
     * 创建系统消息
     * @param content 消息内容
     * @return 消息DTO
     */
    private MessageDTO createSystemMessage(String content) {
        MessageDTO message = new MessageDTO();
        message.setId(IdUtil.fastSimpleUUID());
        message.setContent(content);
        message.setType(1); // 系统消息
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
    
    /**
     * 获取聊天设置
     * 
     * @param agentId 智能体ID
     * @return 聊天设置DTO
     */
    @Override
    public ChatSettingsDTO getChatSettings(Long agentId) {
        // 获取智能体
        Agent agent = agentService.getById(agentId);
        if (agent == null) {
            return null;
        }
        
        ChatSettingsDTO settings = new ChatSettingsDTO();
        settings.setAgentId(agent.getId());
        settings.setAgentName(agent.getName());
        
        // 获取医疗方向
        if (agent.getDirectionId() != null) {
            MedicalDirection direction = medicalDirectionService.getById(agent.getDirectionId());
            if (direction != null) {
                settings.setDirectionName(direction.getName());
            }
        }
        
        // 设置模板信息
        settings.setTemplateId(agent.getTemplateId());
        settings.setTemplateDescription(agent.getTemplateDescription());
        
        // 解析模板参数
        if (agent.getTemplateParameters() != null && !agent.getTemplateParameters().isEmpty()) {
            try {
                // 增加类型检查和错误处理
                List<ChatSettingsDTO.TemplateParam> templateParams = new ArrayList<>();
                
                // 首先尝试确定JSON的格式
                String templateParamsStr = agent.getTemplateParameters().trim();
                
                if (templateParamsStr.startsWith("[")) {
                    // 如果是JSON数组格式
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> paramsList = objectMapper.readValue(templateParamsStr, 
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                        
                        for (Map<String, Object> param : paramsList) {
                            ChatSettingsDTO.TemplateParam templateParam = new ChatSettingsDTO.TemplateParam();
                            
                            if (param.containsKey("name")) {
                                templateParam.setName(param.get("name").toString());
                            }
                            
                            if (param.containsKey("description")) {
                                templateParam.setDescription(param.get("description").toString());
                            }
                            
                            if (param.containsKey("required")) {
                                Object required = param.get("required");
                                if (required instanceof Boolean) {
                                    templateParam.setRequired((Boolean) required);
                                } else if (required != null) {
                                    templateParam.setRequired(Boolean.parseBoolean(required.toString()));
                                }
                            }
                            
                            if (param.containsKey("defaultValue")) {
                                Object defaultValue = param.get("defaultValue");
                                if (defaultValue != null) {
                                    templateParam.setDefaultValue(defaultValue.toString());
                                }
                            }
                            
                            if (param.containsKey("options")) {
                                Object options = param.get("options");
                                if (options instanceof List) {
                                    @SuppressWarnings("unchecked")
                                    List<Object> optionsList = (List<Object>) options;
                                    List<String> stringOptions = new ArrayList<>();
                                    for (Object option : optionsList) {
                                        if (option != null) {
                                            stringOptions.add(option.toString());
                                        }
                                    }
                                    templateParam.setOptions(stringOptions);
                                }
                            }
                            
                            templateParams.add(templateParam);
                        }
                    } catch (Exception e) {
                        log.warn("解析模板参数为List<Map>失败: {}", e.getMessage());
                        // 继续尝试其他格式
                    }
                } else if (templateParamsStr.startsWith("{")) {
                    // 如果是JSON对象格式
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> paramsMap = objectMapper.readValue(templateParamsStr, Map.class);
                        
                        for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                            ChatSettingsDTO.TemplateParam templateParam = new ChatSettingsDTO.TemplateParam();
                            templateParam.setName(entry.getKey());
                            
                            if (entry.getValue() instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> paramDetails = (Map<String, Object>) entry.getValue();
                                
                                if (paramDetails.containsKey("description")) {
                                    templateParam.setDescription(paramDetails.get("description").toString());
                                } else {
                                    templateParam.setDescription(entry.getKey());
                                }
                                
                                if (paramDetails.containsKey("required")) {
                                    Object required = paramDetails.get("required");
                                    if (required instanceof Boolean) {
                                        templateParam.setRequired((Boolean) required);
                                    } else if (required != null) {
                                        templateParam.setRequired(Boolean.parseBoolean(required.toString()));
                                    }
                                }
                                
                                if (paramDetails.containsKey("defaultValue")) {
                                    Object defaultValue = paramDetails.get("defaultValue");
                                    if (defaultValue != null) {
                                        templateParam.setDefaultValue(defaultValue.toString());
                                    }
                                }
                                
                                if (paramDetails.containsKey("options")) {
                                    Object options = paramDetails.get("options");
                                    if (options instanceof List) {
                                        @SuppressWarnings("unchecked")
                                        List<Object> optionsList = (List<Object>) options;
                                        List<String> stringOptions = new ArrayList<>();
                                        for (Object option : optionsList) {
                                            if (option != null) {
                                                stringOptions.add(option.toString());
                                            }
                                        }
                                        templateParam.setOptions(stringOptions);
                                    }
                                }
                            } else {
                                // 如果值是简单类型，将其设为默认值
                                templateParam.setDescription(entry.getKey());
                                if (entry.getValue() != null) {
                                    templateParam.setDefaultValue(entry.getValue().toString());
                                }
                            }
                            
                            templateParams.add(templateParam);
                        }
                    } catch (Exception e) {
                        log.warn("解析模板参数为Map失败: {}", e.getMessage());
                        // 继续尝试其他格式
                    }
                } else {
                    // 如果是简单字符串或逗号分隔的列表
                    try {
                        String[] paramNames = templateParamsStr.split(",");
                        for (String paramName : paramNames) {
                            paramName = paramName.trim();
                            if (!paramName.isEmpty()) {
                                ChatSettingsDTO.TemplateParam templateParam = new ChatSettingsDTO.TemplateParam();
                                templateParam.setName(paramName);
                                templateParam.setDescription(paramName);
                                templateParam.setRequired(false);
                                templateParams.add(templateParam);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析模板参数为字符串列表失败: {}", e.getMessage());
                    }
                }
                
                // 如果所有尝试都失败，添加一个默认参数
                if (templateParams.isEmpty()) {
                    ChatSettingsDTO.TemplateParam defaultParam = new ChatSettingsDTO.TemplateParam();
                    defaultParam.setName("default");
                    defaultParam.setDescription("默认参数");
                    defaultParam.setRequired(false);
                    templateParams.add(defaultParam);
                }
                
                settings.setTemplateParams(templateParams);
            } catch (Exception e) {
                log.error("解析模板参数失败: {}", e.getMessage(), e);
                // 设置空列表而不是null
                settings.setTemplateParams(new ArrayList<>());
            }
        } else {
            // 设置空列表而不是null
            settings.setTemplateParams(new ArrayList<>());
        }
        
        // 解析向量命名空间
        if (agent.getVectorNamespaces() != null && !agent.getVectorNamespaces().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                List<String> namespaces = objectMapper.readValue(agent.getVectorNamespaces(), List.class);
                settings.setVectorNamespaces(namespaces);
            } catch (JsonProcessingException e) {
                log.error("解析向量命名空间失败: {}", e.getMessage());
                settings.setVectorNamespaces(new ArrayList<>());
            }
        } else {
            settings.setVectorNamespaces(new ArrayList<>());
        }
        
        // 解析精确查找数据库UIDs
        if (agent.getPreciseDbUids() != null && !agent.getPreciseDbUids().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                List<String> categories = objectMapper.readValue(agent.getPreciseDbUids(), List.class);
                settings.setPreciseCategories(categories);
            } catch (JsonProcessingException e) {
                log.error("解析精确查找数据库UIDs失败: {}", e.getMessage());
                settings.setPreciseCategories(new ArrayList<>());
            }
        } else {
            settings.setPreciseCategories(new ArrayList<>());
        }
        
        return settings;
    }

    /**
     * 从对话内容中提取处方并保存
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param prescriptionContent 处方内容
     * @return 生成的处方
     */
    @Override
    public PrescriptionDTO extractAndSavePrescription(Long conversationId, Long userId, String prescriptionContent) {
        // 验证对话存在且属于当前用户
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            log.error("对话不存在或不属于当前用户: conversationId={}, userId={}", conversationId, userId);
            return null;
        }
        
        // 检查对话是否已结束
        if (conversation.getIsFinished() == 1) {
            log.error("对话已结束，无法生成处方: conversationId={}", conversationId);
            return null;
        }
        
        // 获取智能体信息
        Agent agent = agentService.getById(conversation.getAgentId());
        if (agent == null) {
            log.error("智能体不存在: agentId={}", conversation.getAgentId());
            return null;
        }
        
        // 创建处方
        Prescription prescription = new Prescription();
        prescription.setUserId(userId);
        prescription.setAgentId(agent.getId());
        prescription.setDirectionId(agent.getDirectionId());
        prescription.setContent(prescriptionContent);
        prescription.setReviewStatus(0); // 未审核
        
        // 保存处方
        prescriptionService.save(prescription);
        
        // 更新对话状态
        conversation.setIsFinished(1); // 标记为已结束
        conversation.setPrescriptionId(prescription.getId()); // 关联处方
        conversationService.updateById(conversation);
        
        // 转换为DTO返回
        return getPrescriptionDetail(prescription.getId(), userId);
    }
} 