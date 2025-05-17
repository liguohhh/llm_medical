package edu.haut.baoxinyang.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.haut.baoxinyang.server.common.R;
import edu.haut.baoxinyang.server.dto.admin.AgentCreateDTO;
import edu.haut.baoxinyang.server.dto.admin.DirectionCreateDTO;
import edu.haut.baoxinyang.server.dto.admin.UserCreateDTO;
import edu.haut.baoxinyang.server.entity.Agent;
import edu.haut.baoxinyang.server.entity.MedicalDirection;
import edu.haut.baoxinyang.server.entity.User;
import edu.haut.baoxinyang.server.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final AdminService adminService;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取系统统计数据
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = adminService.getSystemStats();
        return R.ok(stats);
    }
    
    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public R<List<User>> getUserList(@RequestParam(required = false) Integer userType) {
        List<User> users = adminService.getUserList(userType);
        return R.ok(users);
    }
    
    /**
     * 创建/更新用户
     */
    @PostMapping("/users")
    public R<Boolean> saveOrUpdateUser(@RequestBody @Valid UserCreateDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        boolean result = adminService.saveOrUpdateUser(user);
        return result ? R.ok(true) : R.error("操作失败");
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public R<Boolean> deleteUser(@PathVariable Long userId) {
        boolean result = adminService.deleteUser(userId);
        return result ? R.ok(true) : R.error("删除失败，可能用户不存在或已有关联数据");
    }
    
    /**
     * 重置用户密码
     */
    @PutMapping("/users/{userId}/password")
    public R<Boolean> resetUserPassword(@PathVariable Long userId, @RequestParam String newPassword) {
        boolean result = adminService.resetUserPassword(userId, newPassword);
        return result ? R.ok(true) : R.error("重置密码失败，用户可能不存在");
    }
    
    /**
     * 获取所有医疗方向列表
     */
    @GetMapping("/directions")
    public R<List<MedicalDirection>> getDirectionList() {
        List<MedicalDirection> directions = adminService.getDirectionList();
        return R.ok(directions);
    }
    
    /**
     * 创建/更新医疗方向
     */
    @PostMapping("/directions")
    public R<Boolean> saveOrUpdateDirection(@RequestBody @Valid DirectionCreateDTO directionDTO) {
        MedicalDirection direction = new MedicalDirection();
        BeanUtils.copyProperties(directionDTO, direction);
        
        boolean result = adminService.saveOrUpdateDirection(direction);
        return result ? R.ok(true) : R.error("操作失败");
    }
    
    /**
     * 删除医疗方向
     */
    @DeleteMapping("/directions/{directionId}")
    public R<Boolean> deleteDirection(@PathVariable Long directionId) {
        boolean result = adminService.deleteDirection(directionId);
        return result ? R.ok(true) : R.error("删除失败，该医疗方向可能已被智能体或医生关联");
    }
    
    /**
     * 获取所有智能体列表
     */
    @GetMapping("/agents")
    public R<List<Agent>> getAgentList(@RequestParam(required = false) Long directionId) {
        List<Agent> agents = adminService.getAgentList(directionId);
        return R.ok(agents);
    }
    
    /**
     * 添加或更新智能体
     * @param dto 智能体创建DTO
     * @return 响应结果
     */
    @PostMapping("/agents")
    public R<Boolean> saveOrUpdateAgent(@Valid @RequestBody AgentCreateDTO dto) {
        try {
            boolean success = adminService.saveOrUpdateAgent(dto);
            return success ? R.ok(true) : R.error("智能体保存失败，请检查日志");
        } catch (Exception e) {
            log.error("保存智能体失败", e);
            return R.error("服务器错误: " + e.getMessage());
        }
    }
    
    /**
     * 删除智能体
     */
    @DeleteMapping("/agents/{agentId}")
    public R<Boolean> deleteAgent(@PathVariable Long agentId) {
        boolean result = adminService.deleteAgent(agentId);
        return result ? R.ok(true) : R.error("删除失败，该智能体可能已被对话关联");
    }
    
    /**
     * 测试智能体配置
     */
    @PostMapping("/agents/{agentId}/test")
    public R<String> testAgent(@PathVariable Long agentId, @RequestParam String testMessage) {
        String result = adminService.testAgent(agentId, testMessage);
        return R.ok(result);
    }
} 