package edu.haut.baoxinyang.server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医疗方向实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("medical_direction")
public class MedicalDirection extends BaseEntity {
    
    /**
     * 唯一标识
     */
    @TableField("uid")
    private String uid;
    
    /**
     * 医疗方向名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 具体描述
     */
    @TableField("description")
    private String description;
} 