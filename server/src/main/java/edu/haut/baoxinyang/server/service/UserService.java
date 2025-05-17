package edu.haut.baoxinyang.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.haut.baoxinyang.server.dto.UserRegisterDTO;
import edu.haut.baoxinyang.server.entity.User;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);
    
    /**
     * 根据医疗方向ID查询医生列表
     * @param directionId 医疗方向ID
     * @return 医生列表
     */
    java.util.List<User> getDoctorsByDirectionId(Long directionId);
    
    /**
     * 注册用户
     * @param registerDTO 注册信息
     * @return 注册成功返回用户信息，失败返回null
     */
    User register(UserRegisterDTO registerDTO);
} 