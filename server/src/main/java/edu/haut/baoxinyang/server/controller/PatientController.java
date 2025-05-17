package edu.haut.baoxinyang.server.controller;

import edu.haut.baoxinyang.server.common.R;
import edu.haut.baoxinyang.server.dto.*;
import edu.haut.baoxinyang.server.security.SecurityUser;
import edu.haut.baoxinyang.server.service.PatientService;
import edu.haut.baoxinyang.server.service.llm.SseCallback;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 病人控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {
    
    private final PatientService patientService;
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            return securityUser.getUser().getId();
        }
        return null;
    }
    
    /**
     * 获取所有智能体
     */
    @GetMapping("/agents")
    @PreAuthorize("hasRole('PATIENT')")
    public R<List<AgentDTO>> getAllAgents() {
        List<AgentDTO> agents = patientService.getAllAgents();
        return R.ok(agents);
    }
    
    /**
     * 获取当前病人的历史对话
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('PATIENT')")
    public R<List<ConversationDTO>> getMyConversations() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        
        List<ConversationDTO> conversations = patientService.getConversationsByUserId(userId);
        return R.ok(conversations);
    }
    
    /**
     * 获取对话详情
     */
    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("hasRole('PATIENT')")
    public R<ConversationDetailDTO> getConversationDetail(@PathVariable Long conversationId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        
        ConversationDetailDTO conversation = patientService.getConversationDetail(conversationId, userId);
        if (conversation == null) {
            return R.error("对话不存在或无权限查看");
        }
        
        return R.ok(conversation);
    }
    
    /**
     * 创建新对话或发送消息
     */
    @PostMapping("/conversations/message")
    @PreAuthorize("hasRole('PATIENT')")
    public R<ConversationDetailDTO> sendMessage(@RequestBody @Valid ConversationRequestDTO requestDTO) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        
        // 判断是创建新对话还是发送消息到已有对话
        if (requestDTO.getConversationId() == null) {
            // 创建新对话
            if (requestDTO.getAgentId() == null) {
                return R.error("创建新对话需要指定智能体ID");
            }
            
            ConversationDetailDTO conversation = patientService.createConversation(userId, requestDTO.getAgentId(), requestDTO.getMessage());
            if (conversation == null) {
                return R.error("创建对话失败");
            }
            
            return R.ok(conversation);
        } else {
            // 发送消息到已有对话，支持高级参数
            ConversationDetailDTO conversation = patientService.sendMessage(requestDTO.getConversationId(), userId, requestDTO.getMessage(), requestDTO);
            if (conversation == null) {
                return R.error("发送消息失败，对话可能不存在或已结束");
            }
            
            return R.ok(conversation);
        }
    }
    
    /**
     * 流式创建新对话或发送消息
     */
    @PostMapping(value = "/conversations/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('PATIENT')")
    public SseEmitter streamMessage(@RequestBody @Valid ConversationRequestDTO requestDTO, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(5));
        
        Long userId = getCurrentUserId();
        if (userId == null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("未登录", MediaType.TEXT_PLAIN));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        
        // 判断是创建新对话还是发送消息到已有对话
        if (requestDTO.getConversationId() == null) {
            // 创建新对话（非流式处理）
            if (requestDTO.getAgentId() == null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("创建新对话需要指定智能体ID", MediaType.TEXT_PLAIN));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
                return emitter;
            }
            
            ConversationDetailDTO conversation = patientService.createConversation(userId, requestDTO.getAgentId(), requestDTO.getMessage());
            if (conversation == null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("创建对话失败", MediaType.TEXT_PLAIN));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
                return emitter;
            }
            
            try {
                // 发送完整响应
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(conversation.getMessages().get(conversation.getMessages().size() - 1).getContent(), MediaType.TEXT_PLAIN));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        } else {
            // 流式发送消息到已有对话，支持高级参数
            SseCallback callback = new SseCallback() {
                @Override
                public void onEvent(SseEvent event) {
                    try {
                        if (event.isLastEvent()) {
                            emitter.complete();
                        } else {
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(event.getData(), MediaType.TEXT_PLAIN));
                        }
                    } catch (IOException e) {
                        log.error("发送SSE事件失败", e);
                        onError(e);
                    }
                }
                
                @Override
                public void onError(Throwable throwable) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(throwable.getMessage(), MediaType.TEXT_PLAIN));
                        emitter.completeWithError(throwable);
                    } catch (IOException e) {
                        log.error("发送SSE错误事件失败", e);
                        emitter.completeWithError(e);
                    }
                }
                
                @Override
                public void onComplete() {
                    emitter.complete();
                }
            };
            
            // 设置超时回调
            emitter.onTimeout(() -> {
                log.warn("SSE连接超时");
                callback.onError(new RuntimeException("连接超时"));
            });
            
            // 设置完成回调
            emitter.onCompletion(() -> {
                log.info("SSE连接已关闭");
            });
            
            // 设置错误回调
            emitter.onError(throwable -> {
                log.error("SSE连接出错", throwable);
                callback.onError(throwable);
            });
            
            // 开始流式处理，使用高级参数
            patientService.streamMessage(requestDTO.getConversationId(), userId, requestDTO.getMessage(), requestDTO, callback);
            
            return emitter;
        }
    }
    
    /**
     * 生成处方
     */
    @PostMapping("/conversations/{conversationId}/prescription")
    @PreAuthorize("hasRole('PATIENT')")
    public R<PrescriptionDTO> generatePrescription(@PathVariable Long conversationId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        
        PrescriptionDTO prescription = patientService.generatePrescription(conversationId, userId);
        if (prescription == null) {
            return R.error("生成处方失败，对话可能不存在、已结束或无权限操作");
        }
        
        return R.ok(prescription);
    }
    
    /**
     * 获取处方详情
     */
    @GetMapping("/prescriptions/{prescriptionId}")
    @PreAuthorize("hasRole('PATIENT')")
    public R<PrescriptionDTO> getPrescriptionDetail(@PathVariable Long prescriptionId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        
        PrescriptionDTO prescription = patientService.getPrescriptionDetail(prescriptionId, userId);
        if (prescription == null) {
            return R.error("处方不存在或无权限查看");
        }
        
        return R.ok(prescription);
    }
    
    /**
     * 获取处方列表
     */
    @GetMapping("/prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    public R<List<PrescriptionDTO>> getPrescriptions() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.error("获取处方列表失败：用户未登录");
            return R.error("未登录");
        }
        
        log.info("获取用户[{}]的处方列表", userId);
        List<PrescriptionDTO> prescriptions = patientService.getPrescriptionsByUserId(userId);
        log.info("成功获取到{}条处方记录", prescriptions.size());
        return R.ok(prescriptions);
    }
    
    /**
     * 获取聊天设置
     */
    @GetMapping("/agents/{agentId}/chat-settings")
    @PreAuthorize("hasRole('PATIENT')")
    public R<ChatSettingsDTO> getChatSettings(@PathVariable Long agentId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        
        ChatSettingsDTO settings = patientService.getChatSettings(agentId);
        if (settings == null) {
            return R.error("获取聊天设置失败，智能体可能不存在");
        }
        
        return R.ok(settings);
    }
    
    /**
     * 从对话内容中提取处方
     */
    @PostMapping("/conversations/{conversationId}/extract-prescription")
    @PreAuthorize("hasRole('PATIENT')")
    public R<PrescriptionDTO> extractPrescription(
            @PathVariable Long conversationId,
            @RequestBody Map<String, String> requestBody) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.error("未登录");
        }
        
        // 获取请求中的处方内容
        String prescriptionContent = requestBody.get("content");
        if (prescriptionContent == null || prescriptionContent.trim().isEmpty()) {
            return R.error("处方内容不能为空");
        }
        
        PrescriptionDTO prescription = patientService.extractAndSavePrescription(
                conversationId, userId, prescriptionContent);
        if (prescription == null) {
            return R.error("生成处方失败，对话可能不存在、已结束或无权限操作");
        }
        
        return R.ok(prescription);
    }
} 