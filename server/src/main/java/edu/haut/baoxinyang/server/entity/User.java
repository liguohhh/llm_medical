package edu.haut.baoxinyang.server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类（包含病人、医生、管理员）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    
    /**
     * 账户名
     */
    @TableField("username")
    private String username;
    
    /**
     * 密码
     */
    @TableField("password")
    private String password;
    
    /**
     * 姓名
     */
    @TableField("real_name")
    private String realName;
    
    /**
     * 性别：0-女，1-男
     */
    @TableField("gender")
    private Integer gender;
    
    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;
    
    /**
     * 用户类型：0-病人，1-医生，2-管理员
     */
    @TableField("user_type")
    private Integer userType;
    
    /**
     * 医疗方向ID，仅医生需要
     */
    @TableField("direction_id")
    private Long directionId;
    
    /**
     * 医疗方向（非数据库字段）
     */
    @TableField(exist = false)
    private MedicalDirection direction;
} 