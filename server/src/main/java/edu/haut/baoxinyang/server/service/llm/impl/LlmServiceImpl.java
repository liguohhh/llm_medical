package edu.haut.baoxinyang.server.service.llm.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.haut.baoxinyang.server.config.LlmApiConfig;
import edu.haut.baoxinyang.server.dto.MessageDTO;
import edu.haut.baoxinyang.server.dto.llm.LlmRequestDTO;
import edu.haut.baoxinyang.server.dto.llm.LlmResponseDTO;
import edu.haut.baoxinyang.server.entity.Agent;
import edu.haut.baoxinyang.server.entity.User;
import edu.haut.baoxinyang.server.service.llm.LlmService;
import edu.haut.baoxinyang.server.service.llm.SseCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * LLM服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmServiceImpl implements LlmService {
    
    private final LlmApiConfig llmApiConfig;
    private final ObjectMapper objectMapper;
    
    private OkHttpClient getHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(llmApiConfig.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(llmApiConfig.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(llmApiConfig.getReadTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    @Override
    public LlmResponseDTO ask(LlmRequestDTO request) {
        try {
            String url = llmApiConfig.getAskUrl();
            String jsonBody = objectMapper.writeValueAsString(request);
            
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            Response response = getHttpClient().newCall(httpRequest).execute();
            if (!response.isSuccessful()) {
                log.error("LLM API请求失败: {}", response);
                return null;
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, LlmResponseDTO.class);
        } catch (IOException e) {
            log.error("调用LLM服务失败", e);
            return null;
        }
    }
    
    @Override
    public void stream(LlmRequestDTO request, SseCallback callback) {
        try {
            String url = llmApiConfig.getStreamUrl();
            String jsonBody = objectMapper.writeValueAsString(request);
            
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            EventSourceListener listener = new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    SseCallback.SseEvent event = new SseCallback.SseEvent();
                    event.setId(id);
                    event.setEvent(type);
                    event.setData(data);
                    event.setLastEvent(false);
                    callback.onEvent(event);
                }
                
                @Override
                public void onClosed(EventSource eventSource) {
                    SseCallback.SseEvent event = new SseCallback.SseEvent();
                    event.setLastEvent(true);
                    callback.onEvent(event);
                    callback.onComplete();
                }
                
                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    callback.onError(t);
                }
            };
            
            EventSources.createFactory(getHttpClient()).newEventSource(httpRequest, listener);
        } catch (JsonProcessingException e) {
            log.error("序列化LLM请求失败", e);
            callback.onError(e);
        }
    }
    
    @Override
    public LlmRequestDTO buildRequest(Agent agent, User user, String message, List<MessageDTO> history) {
        LlmRequestDTO request = new LlmRequestDTO();
        
        // 设置当前消息
        request.setMessage(message);
        
        // 设置模型信息
        LlmRequestDTO.ModelSettings modelSettings = new LlmRequestDTO.ModelSettings();
        modelSettings.setModelName(agent.getModelName());
        modelSettings.setApiKey(agent.getApiKey());
        modelSettings.setBaseUrl(agent.getModelUrl());
        request.setModelSettings(modelSettings);
        
        // 设置模板信息
        LlmRequestDTO.TemplateConfig templateConfig = new LlmRequestDTO.TemplateConfig();
        templateConfig.setTemplateId(agent.getTemplateId());
        // 注意：虽然设置了subTemplateIds，但Python后端已修改为忽略此参数，会自动使用大模板中的所有子模板
        // 这里仍然设置一些值只是为了保持接口兼容性
        templateConfig.setSubTemplateIds(List.of("基础信息"));
        
        // 设置用户参数
        Map<String, String> params = buildUserParams(user, message);
        templateConfig.setParams(params);
        
        request.setTemplateConfig(templateConfig);
        
        // 设置向量搜索配置，如果有的话
        if (agent.getVectorNamespaces() != null && !agent.getVectorNamespaces().isEmpty()) {
            try {
                // 解析向量命名空间，实际上这里需要根据具体的JSON结构来处理
                @SuppressWarnings("unchecked")
                List<String> namespaces = objectMapper.readValue(agent.getVectorNamespaces(), List.class);
                
                LlmRequestDTO.VectorSearchConfig vectorSearchConfig = new LlmRequestDTO.VectorSearchConfig();
                vectorSearchConfig.setNamespaces(namespaces);
                vectorSearchConfig.setNResults(3);  // 默认值，可以根据需要调整
                vectorSearchConfig.setRagHistoryCount(3); // 设置默认RAG历史消息数量
                request.setVectorSearchConfig(vectorSearchConfig);
            } catch (JsonProcessingException e) {
                log.error("解析向量命名空间失败", e);
            }
        }
        
        // 设置精确查询配置，如果有的话
        if (agent.getPreciseDbName() != null && agent.getPreciseDbUids() != null) {
            try {
                // 解析精确查询UIDs，实际上这里需要根据具体的JSON结构来处理
                @SuppressWarnings("unchecked")
                List<String> categories = objectMapper.readValue(agent.getPreciseDbUids(), List.class);
                
                LlmRequestDTO.PreciseSearchConfig preciseSearchConfig = new LlmRequestDTO.PreciseSearchConfig();
                preciseSearchConfig.setCategories(categories);
                preciseSearchConfig.setMaxResults(3);  // 默认值，可以根据需要调整
                preciseSearchConfig.setSearchDepth(2);  // 默认值，可以根据需要调整
                preciseSearchConfig.setRagHistoryCount(3); // 设置默认RAG历史消息数量
                request.setPreciseSearchConfig(preciseSearchConfig);
            } catch (JsonProcessingException e) {
                log.error("解析精确查询UIDs失败", e);
            }
        }
        
        // 设置历史消息
        List<LlmRequestDTO.MessageItem> historyItems = new ArrayList<>();
        for (MessageDTO historyMsg : history) {
            String role = historyMsg.getType() == 0 ? "user" : "assistant";
            historyItems.add(new LlmRequestDTO.MessageItem(role, historyMsg.getContent()));
        }
        request.setHistory(historyItems);
        
        return request;
    }
    
    @Override
    public Map<String, String> buildUserParams(User user, String message) {
        Map<String, String> params = new HashMap<>();
        
        // 添加用户基本信息
        params.put("patient_age", user.getAge() != null ? user.getAge().toString() : "未知");
        params.put("patient_gender", user.getGender() != null ? (user.getGender() == 1 ? "男" : "女") : "未知");
        params.put("symptoms", message);  // 假设当前消息包含症状描述
        
        return params;
    }
} 