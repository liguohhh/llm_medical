package edu.haut.baoxinyang.server.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户创建/更新DTO
 */
@Data
public class UserCreateDTO {
    
    /**
     * 用户ID (更新时必填)
     */
    private Long id;
    
    /**
     * 账户名
     */
    @NotBlank(message = "账户名不能为空")
    private String username;
    
    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    
    /**
     * 性别：0-女，1-男
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别参数错误")
    @Max(value = 1, message = "性别参数错误")
    private Integer gender;
    
    /**
     * 年龄
     */
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄不合理")
    private Integer age;
    
    /**
     * 用户类型：0-病人，1-医生，2-管理员
     */
    @NotNull(message = "用户类型不能为空")
    @Min(value = 0, message = "用户类型参数错误")
    @Max(value = 2, message = "用户类型参数错误")
    private Integer userType;
    
    /**
     * 医疗方向ID，仅医生需要
     */
    private Long directionId;
} 