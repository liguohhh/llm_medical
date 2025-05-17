package edu.haut.baoxinyang.server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能体实体类
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
@TableName("agent")
public class Agent extends BaseEntity {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 智能体名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 医疗方向ID
     */
    @TableField("direction_id")
    private Long directionId;
    
    /**
     * 对接的大模型名称
     */
    @TableField("model_name")
    private String modelName;
    
    /**
     * 对接的大模型链接地址
     */
    @TableField("model_url")
    private String modelUrl;
    
    /**
     * 对接的大模型的apikey
     */
    @TableField("api_key")
    private String apiKey;
    
    /**
     * 使用的模板ID
     */
    @TableField("template_id")
    private String templateId;
    
    /**
     * 模板描述
     */
    @TableField("template_description")
    private String templateDescription;
    
    /**
     * 模板参数，JSON格式存储
     */
    @TableField("template_parameters")
    private String templateParameters;
    
    /**
     * 使用的向量数据库命名空间，JSON格式存储
     */
    @TableField("vector_namespaces")
    private String vectorNamespaces;
    
    /**
     * 使用的精确查找数据库名称
     */
    @TableField("precise_db_name")
    private String preciseDbName;
    
    /**
     * 精确查找数据库UID列表，JSON格式存储
     */
    @TableField("precise_db_uids")
    private String preciseDbUids;
    
    /**
     * 医疗方向（非数据库字段）
     */
    @TableField(exist = false)
    private MedicalDirection direction;
    
    /**
     * 获取模型基础URL
     * @return 模型基础URL
     */
    public String getBaseUrl() {
        return this.modelUrl;
    }
    
    /**
     * 获取子模板ID列表
     * @return 子模板ID列表
     */
    public List<String> getSubTemplateIds() {
        try {
            if (templateParameters == null || templateParameters.isEmpty()) {
                return new ArrayList<>();
            }
            
            Map<String, Object> params = objectMapper.readValue(templateParameters, 
                    new TypeReference<Map<String, Object>>() {});
            
            if (params.containsKey("subTemplateIds") && params.get("subTemplateIds") instanceof List) {
                return objectMapper.convertValue(params.get("subTemplateIds"), 
                        new TypeReference<List<String>>() {});
            }
            
            return new ArrayList<>();
        } catch (JsonProcessingException e) {
            log.error("解析子模板ID列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取模板参数
     * @return 模板参数Map
     */
    public Map<String, String> getParams() {
        try {
            if (templateParameters == null || templateParameters.isEmpty()) {
                return new HashMap<>();
            }
            
            Map<String, Object> paramsObj = objectMapper.readValue(templateParameters, 
                    new TypeReference<Map<String, Object>>() {});
            
            if (paramsObj.containsKey("params") && paramsObj.get("params") instanceof Map) {
                return objectMapper.convertValue(paramsObj.get("params"), 
                        new TypeReference<Map<String, String>>() {});
            }
            
            // 如果没有params字段，返回所有键值对
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : paramsObj.entrySet()) {
                if (!(entry.getValue() instanceof Map) && !(entry.getValue() instanceof List)) {
                    result.put(entry.getKey(), entry.getValue().toString());
                }
            }
            return result;
        } catch (JsonProcessingException e) {
            log.error("解析模板参数失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
} 