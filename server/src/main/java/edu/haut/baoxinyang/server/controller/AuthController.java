package edu.haut.baoxinyang.server.controller;

import edu.haut.baoxinyang.server.common.R;
import edu.haut.baoxinyang.server.dto.UserLoginDTO;
import edu.haut.baoxinyang.server.dto.UserRegisterDTO;
import edu.haut.baoxinyang.server.dto.UserVO;
import edu.haut.baoxinyang.server.entity.User;
import edu.haut.baoxinyang.server.security.SecurityUser;
import edu.haut.baoxinyang.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    
    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public R<UserVO> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = userService.getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            return R.error("用户名已存在");
        }
        
        // 注册用户
        User user = userService.register(registerDTO);
        if (user == null) {
            return R.error("注册失败，请重试");
        }
        
        // 转换为UserVO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        return R.ok(userVO);
    }
    
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public R<UserVO> login(@RequestBody @Valid UserLoginDTO loginDTO, HttpServletRequest request) {
        try {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
            
            // 认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // 存储认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取会话并设置为有效
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            // 获取用户信息
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            User user = securityUser.getUser();
            
            // 转换为UserVO
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            
            return R.ok(userVO);
        } catch (AuthenticationException e) {
            return R.error("用户名或密码错误");
        }
    }
    
    /**
     * 获取当前登录用户信息
     * @return 当前用户信息
     */
    @GetMapping("/info")
    public R<UserVO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            User user = securityUser.getUser();
            
            // 转换为UserVO
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            
            return R.ok(userVO);
        }
        return R.error("未登录");
    }
    
    /**
     * 用户注销
     * @return 注销结果
     */
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取当前认证信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // 执行注销
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        // 确保清除会话
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // 清除安全上下文
        SecurityContextHolder.clearContext();
        
        return R.ok();
    }
} 