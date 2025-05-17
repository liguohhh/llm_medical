package edu.haut.baoxinyang.server.service;

import edu.haut.baoxinyang.server.dto.admin.AgentCreateDTO;
import edu.haut.baoxinyang.server.entity.Agent;
import edu.haut.baoxinyang.server.entity.MedicalDirection;
import edu.haut.baoxinyang.server.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 获取所有用户列表（可按类型过滤）
     * @param userType 用户类型：null-全部，0-病人，1-医生，2-管理员
     * @return 用户列表
     */
    List<User> getUserList(Integer userType);
    
    /**
     * 创建/更新用户
     * @param user 用户信息
     * @return 是否成功
     */
    boolean saveOrUpdateUser(User user);
    
    /**
     * 删除用户
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetUserPassword(Long userId, String newPassword);
    
    /**
     * 获取所有医疗方向列表
     * @return 医疗方向列表
     */
    List<MedicalDirection> getDirectionList();
    
    /**
     * 创建/更新医疗方向
     * @param direction 医疗方向信息
     * @return 是否成功
     */
    boolean saveOrUpdateDirection(MedicalDirection direction);
    
    /**
     * 删除医疗方向
     * @param directionId 医疗方向ID
     * @return 是否成功
     */
    boolean deleteDirection(Long directionId);
    
    /**
     * 获取所有智能体列表
     * @param directionId 可选的医疗方向ID过滤
     * @return 智能体列表
     */
    List<Agent> getAgentList(Long directionId);
    
    /**
     * 创建/更新智能体
     * @param agent 智能体信息
     * @return 是否成功
     */
    boolean saveOrUpdateAgent(Agent agent);
    
    /**
     * 从DTO创建/更新智能体
     * @param dto 智能体创建DTO
     * @return 是否成功
     */
    boolean saveOrUpdateAgent(AgentCreateDTO dto);
    
    /**
     * 删除智能体
     * @param agentId 智能体ID
     * @return 是否成功
     */
    boolean deleteAgent(Long agentId);
    
    /**
     * 测试智能体配置
     * @param agentId 智能体ID
     * @param testMessage 测试消息
     * @return 测试结果
     */
    String testAgent(Long agentId, String testMessage);
    
    /**
     * 获取系统统计数据
     * @return 统计数据
     */
    Map<String, Object> getSystemStats();
} 