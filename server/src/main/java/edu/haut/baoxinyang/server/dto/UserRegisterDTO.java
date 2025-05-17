package edu.haut.baoxinyang.server.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {
    
    /**
     * 账户名
     */
    @NotBlank(message = "账户名不能为空")
    @Size(min = 4, max = 20, message = "账户名长度必须在4-20个字符之间")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    
    /**
     * 性别：0-女，1-男
     */
    @NotNull(message = "性别不能为空")
    private Integer gender;
    
    /**
     * 年龄
     */
    @NotNull(message = "年龄不能为空")
    private Integer age;
    
    /**
     * 用户类型：默认为0-病人
     */
    private Integer userType = 0;
} 