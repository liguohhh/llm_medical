package edu.haut.baoxinyang.server.dto.llm.manage;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 精确查找条目DTO
 */
@Data
public class PreciseEntryDTO {
    
    /**
     * 条目ID
     */
    private Integer id;
    
    /**
     * 条目UID
     */
    private String uid;
    
    /**
     * 大类UID
     */
    private String categoryUid;
    
    /**
     * 条目描述
     */
    @NotBlank(message = "条目描述不能为空")
    private String description;
    
    /**
     * 条目内容
     */
    @NotBlank(message = "条目内容不能为空")
    private String content;
    
    /**
     * 条目权重
     */
    @Min(value = 1, message = "权重最小为1")
    @Max(value = 100, message = "权重最大为100")
    private Integer weight = 50;
    
    /**
     * 是否启用
     */
    private Boolean isEnabled = true;
    
    /**
     * 关键词列表
     */
    @NotEmpty(message = "关键词不能为空")
    private List<String> keywords;
} 