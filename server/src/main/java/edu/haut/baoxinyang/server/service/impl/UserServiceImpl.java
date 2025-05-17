package edu.haut.baoxinyang.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.haut.baoxinyang.server.dto.UserRegisterDTO;
import edu.haut.baoxinyang.server.entity.User;
import edu.haut.baoxinyang.server.mapper.UserMapper;
import edu.haut.baoxinyang.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户Service实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return baseMapper.selectOne(wrapper);
    }
    
    @Override
    public List<User> getDoctorsByDirectionId(Long directionId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserType, 1)  // 用户类型：1-医生
                .eq(User::getDirectionId, directionId);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            return null;
        }
        
        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        
        // 保存用户
        save(user);
        
        // 返回用户信息（不包含密码）
        user.setPassword(null);
        return user;
    }
} 