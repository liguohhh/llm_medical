package edu.haut.baoxinyang.server.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.haut.baoxinyang.server.dto.admin.AgentCreateDTO;
import edu.haut.baoxinyang.server.dto.llm.LlmRequestDTO;
import edu.haut.baoxinyang.server.dto.llm.LlmResponseDTO;
import edu.haut.baoxinyang.server.entity.Agent;
import edu.haut.baoxinyang.server.entity.Conversation;
import edu.haut.baoxinyang.server.entity.MedicalDirection;
import edu.haut.baoxinyang.server.entity.User;
import edu.haut.baoxinyang.server.mapper.AgentMapper;
import edu.haut.baoxinyang.server.mapper.MedicalDirectionMapper;
import edu.haut.baoxinyang.server.mapper.UserMapper;
import edu.haut.baoxinyang.server.service.*;
import edu.haut.baoxinyang.server.service.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final UserService userService;
    private final AgentService agentService;
    private final MedicalDirectionService directionService;
    private final ConversationService conversationService;
    private final PrescriptionService prescriptionService;
    private final LlmService llmService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private MedicalDirectionMapper directionMapper;
    
    @Autowired
    private AgentMapper agentMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public List<User> getUserList(Integer userType) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        
        List<User> users = userService.list(wrapper);
        
        // 填充医疗方向信息
        for (User user : users) {
            if (user.getDirectionId() != null) {
                MedicalDirection direction = directionService.getById(user.getDirectionId());
                user.setDirection(direction);
            }
        }
        
        return users;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateUser(User user) {
        // 创建新用户时需要处理密码
        if (user.getId() == null) {
            // 设置默认密码为123456
            String defaultPassword = "123456";
            String hashedPassword = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
        } else {
            // 更新用户时不处理密码字段，需要单独调用resetUserPassword方法
            User existingUser = userService.getById(user.getId());
            if (existingUser != null) {
                user.setPassword(existingUser.getPassword());
            }
        }
        
        return userService.saveOrUpdate(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        return userService.removeById(userId);
    }
    
    @Override
    public boolean resetUserPassword(Long userId, String newPassword) {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        
        return userService.updateById(user);
    }
    
    @Override
    public List<MedicalDirection> getDirectionList() {
        return directionService.list();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateDirection(MedicalDirection direction) {
        // 创建新医疗方向时，生成唯一标识
        if (direction.getId() == null) {
            direction.setUid(IdUtil.fastSimpleUUID());
        }
        
        return directionService.saveOrUpdate(direction);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDirection(Long directionId) {
        // 检查是否有智能体或医生关联了该医疗方向
        LambdaQueryWrapper<Agent> agentWrapper = new LambdaQueryWrapper<>();
        agentWrapper.eq(Agent::getDirectionId, directionId);
        long agentCount = agentService.count(agentWrapper);
        
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDirectionId, directionId);
        long userCount = userService.count(userWrapper);
        
        if (agentCount > 0 || userCount > 0) {
            log.warn("医疗方向已被关联，无法删除: {}", directionId);
            return false;
        }
        
        return directionService.removeById(directionId);
    }
    
    @Override
    public List<Agent> getAgentList(Long directionId) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        if (directionId != null) {
            wrapper.eq(Agent::getDirectionId, directionId);
        }
        
        List<Agent> agents = agentService.list(wrapper);
        
        // 填充医疗方向信息
        for (Agent agent : agents) {
            if (agent.getDirectionId() != null) {
                MedicalDirection direction = directionService.getById(agent.getDirectionId());
                agent.setDirection(direction);
            }
        }
        
        return agents;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateAgent(Agent agent) {
        return agentService.saveOrUpdate(agent);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAgent(Long agentId) {
        // 检查是否有对话关联了该智能体
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getAgentId, agentId);
        long count = conversationService.count(wrapper);
        
        if (count > 0) {
            log.warn("智能体已被对话关联，无法删除: {}", agentId);
            return false;
        }
        
        return agentService.removeById(agentId);
    }
    
    @Override
    public String testAgent(Long agentId, String testMessage) {
        Agent agent = agentService.getById(agentId);
        if (agent == null) {
            return "智能体不存在";
        }
        
        try {
            // 构建测试用户
            User testUser = new User();
            testUser.setAge(30);
            testUser.setGender(1);
            testUser.setRealName("测试用户");
            
            // 构建请求
            LlmRequestDTO request = llmService.buildRequest(agent, testUser, testMessage, new ArrayList<>());
            
            // 发送请求
            LlmResponseDTO response = llmService.ask(request);
            
            if (response != null) {
                return response.getAnswer();
            } else {
                return "测试失败：未获取到响应";
            }
        } catch (Exception e) {
            log.error("测试智能体失败", e);
            return "测试失败：" + e.getMessage();
        }
    }
    
    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        stats.put("totalPatients", userService.count(new LambdaQueryWrapper<User>().eq(User::getUserType, 0)));
        stats.put("totalDoctors", userService.count(new LambdaQueryWrapper<User>().eq(User::getUserType, 1)));
        stats.put("totalAdmins", userService.count(new LambdaQueryWrapper<User>().eq(User::getUserType, 2)));
        
        // 方向统计
        stats.put("totalDirections", directionService.count());
        
        // 智能体统计
        stats.put("totalAgents", agentService.count());
        
        // 对话统计
        stats.put("totalConversations", conversationService.count());
        stats.put("finishedConversations", conversationService.count(
                new LambdaQueryWrapper<Conversation>().eq(Conversation::getIsFinished, 1)));
        
        // 处方统计
        stats.put("totalPrescriptions", prescriptionService.count());
        
        return stats;
    }
    
    /**
     * 从DTO创建或更新智能体
     * @param dto 智能体创建DTO
     * @return 是否成功
     */
    @Transactional
    public boolean saveOrUpdateAgent(AgentCreateDTO dto) {
        try {
            Agent agent = new Agent();
            
            // 如果是更新，先查询现有数据
            if (dto.getId() != null) {
                agent = agentMapper.selectById(dto.getId());
                if (agent == null) {
                    log.error("更新智能体失败：找不到ID为{}的智能体", dto.getId());
                    return false;
                }
            }
            
            // 设置基本属性
            agent.setId(dto.getId());
            agent.setName(dto.getName());
            agent.setDirectionId(dto.getDirectionId());
            agent.setModelName(dto.getModelName());
            agent.setModelUrl(dto.getModelUrl());
            agent.setApiKey(dto.getApiKey());
            agent.setTemplateId(dto.getTemplateId());
            agent.setTemplateDescription(dto.getTemplateDescription());
            
            // 处理模板参数（JSON）
            if (dto.getTemplateParameters() != null) {
                agent.setTemplateParameters(dto.getTemplateParameters());
            }
            
            // 处理向量数据库命名空间（JSON）
            if (dto.getVectorNamespaces() != null) {
                agent.setVectorNamespaces(objectMapper.writeValueAsString(dto.getVectorNamespaces()));
            }
            
            // 处理精确查找数据库
            agent.setPreciseDbName(dto.getPreciseDbName());
            
            // 处理精确查找数据库UID（JSON）
            if (dto.getPreciseDbUids() != null) {
                agent.setPreciseDbUids(objectMapper.writeValueAsString(dto.getPreciseDbUids()));
            }
            
            return saveOrUpdateAgent(agent);
        } catch (JsonProcessingException e) {
            log.error("处理智能体JSON数据失败", e);
            return false;
        }
    }
} 