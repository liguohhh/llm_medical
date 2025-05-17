package edu.haut.baoxinyang.server.dto;

import edu.haut.baoxinyang.server.entity.MedicalDirection;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 账户名
     */
    private String username;
    
    /**
     * 姓名
     */
    private String realName;
    
    /**
     * 性别：0-女，1-男
     */
    private Integer gender;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 用户类型：0-病人，1-医生，2-管理员
     */
    private Integer userType;
    
    /**
     * 医疗方向ID
     */
    private Long directionId;
    
    /**
     * 医疗方向
     */
    private MedicalDirection direction;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 