package edu.haut.baoxinyang.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.haut.baoxinyang.server.dto.ConversationDetailDTO;
import edu.haut.baoxinyang.server.dto.MessageDTO;
import edu.haut.baoxinyang.server.dto.PrescriptionDTO;
import edu.haut.baoxinyang.server.dto.StatsDTO;
import edu.haut.baoxinyang.server.entity.*;
import edu.haut.baoxinyang.server.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医生服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final UserService userService;
    private final PrescriptionService prescriptionService;
    private final ConversationService conversationService;
    private final MedicalDirectionService medicalDirectionService;
    private final AgentService agentService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getPrescriptions(
            Long doctorId, Integer page, Integer size, String sort, String order,
            Integer status, String keyword, String startDate, String endDate) {
        
        // 获取医生所属医疗方向
        User doctor = userService.getById(doctorId);
        if (doctor == null || doctor.getDirectionId() == null) {
            return null;
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Prescription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Prescription::getDirectionId, doctor.getDirectionId());
        
        // 添加审核状态过滤
        if (status != null) {
            queryWrapper.eq(Prescription::getReviewStatus, status);
        }
        
        // 添加关键词搜索
        if (StringUtils.hasText(keyword)) {
            // 这里需要关联用户表进行搜索，简化处理，直接在后面过滤
            // 实际应该使用联表查询或全文索引
        }
        
        // 添加日期范围过滤
        if (StringUtils.hasText(startDate)) {
            LocalDate start = LocalDate.parse(startDate);
            queryWrapper.ge(Prescription::getCreateTime, start.atStartOfDay());
        }
        
        if (StringUtils.hasText(endDate)) {
            LocalDate end = LocalDate.parse(endDate);
            queryWrapper.le(Prescription::getCreateTime, end.plusDays(1).atStartOfDay());
        }
        
        // 添加排序
        boolean isAsc = "asc".equalsIgnoreCase(order);
        switch (sort) {
            case "id":
                queryWrapper.orderBy(true, isAsc, Prescription::getId);
                break;
            case "reviewStatus":
                queryWrapper.orderBy(true, isAsc, Prescription::getReviewStatus);
                break;
            case "updateTime":
                queryWrapper.orderBy(true, isAsc, Prescription::getUpdateTime);
                break;
            case "createTime":
            default:
                queryWrapper.orderBy(true, isAsc, Prescription::getCreateTime);
                break;
        }
        
        // 执行分页查询
        Page<Prescription> prescriptionPage = new Page<>(page, size);
        Page<Prescription> resultPage = prescriptionService.page(prescriptionPage, queryWrapper);
        
        // 转换为DTO
        List<PrescriptionDTO> prescriptionDTOs = resultPage.getRecords().stream()
                .map(prescription -> {
                    PrescriptionDTO dto = new PrescriptionDTO();
                    BeanUtils.copyProperties(prescription, dto);
                    
                    // 获取用户名
                    User user = userService.getById(prescription.getUserId());
                    if (user != null) {
                        dto.setUserName(user.getRealName());
                    }
                    
                    // 获取智能体名称
                    Agent agent = agentService.getById(prescription.getAgentId());
                    if (agent != null) {
                        dto.setAgentName(agent.getName());
                    }
                    
                    // 获取医疗方向名称
                    MedicalDirection direction = medicalDirectionService.getById(prescription.getDirectionId());
                    if (direction != null) {
                        dto.setDirectionName(direction.getName());
                    }
                    
                    // 获取审核医生姓名
                    if (prescription.getReviewerId() != null) {
                        User reviewer = userService.getById(prescription.getReviewerId());
                        if (reviewer != null) {
                            dto.setReviewerName(reviewer.getRealName());
                        }
                    }
                    
                    // 查询关联的对话ID
                    LambdaQueryWrapper<Conversation> conversationWrapper = new LambdaQueryWrapper<>();
                    conversationWrapper.eq(Conversation::getPrescriptionId, prescription.getId());
                    Conversation conversation = conversationService.getOne(conversationWrapper);
                    if (conversation != null) {
                        dto.setConversationId(conversation.getId());
                    }
                    
                    return dto;
                })
                .filter(dto -> {
                    // 如果有关键词，在DTO层面过滤
                    if (!StringUtils.hasText(keyword)) {
                        return true;
                    }
                    
                    String lowerKeyword = keyword.toLowerCase();
                    return (dto.getUserName() != null && dto.getUserName().toLowerCase().contains(lowerKeyword)) ||
                           (dto.getAgentName() != null && dto.getAgentName().toLowerCase().contains(lowerKeyword)) ||
                           (dto.getDirectionName() != null && dto.getDirectionName().toLowerCase().contains(lowerKeyword));
                })
                .collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", prescriptionDTOs);
        result.put("total", resultPage.getTotal());
        result.put("size", resultPage.getSize());
        result.put("current", resultPage.getCurrent());
        result.put("pages", resultPage.getPages());
        
        return result;
    }

    @Override
    public PrescriptionDTO getPrescriptionDetail(Long prescriptionId, Long doctorId) {
        // 获取处方
        Prescription prescription = prescriptionService.getById(prescriptionId);
        if (prescription == null) {
            return null;
        }
        
        // 获取医生所属医疗方向
        User doctor = userService.getById(doctorId);
        if (doctor == null || doctor.getDirectionId() == null) {
            return null;
        }
        
        // 验证处方是否属于医生所属医疗方向
        if (!prescription.getDirectionId().equals(doctor.getDirectionId())) {
            return null;
        }
        
        // 转换为DTO
        PrescriptionDTO dto = new PrescriptionDTO();
        BeanUtils.copyProperties(prescription, dto);
        
        // 获取用户名
        User user = userService.getById(prescription.getUserId());
        if (user != null) {
            dto.setUserName(user.getRealName());
        }
        
        // 获取智能体名称
        Agent agent = agentService.getById(prescription.getAgentId());
        if (agent != null) {
            dto.setAgentName(agent.getName());
        }
        
        // 获取医疗方向名称
        MedicalDirection direction = medicalDirectionService.getById(prescription.getDirectionId());
        if (direction != null) {
            dto.setDirectionName(direction.getName());
        }
        
        // 获取审核医生姓名
        if (prescription.getReviewerId() != null) {
            User reviewer = userService.getById(prescription.getReviewerId());
            if (reviewer != null) {
                dto.setReviewerName(reviewer.getRealName());
            }
        }
        
        // 查询关联的对话ID
        LambdaQueryWrapper<Conversation> conversationWrapper = new LambdaQueryWrapper<>();
        conversationWrapper.eq(Conversation::getPrescriptionId, prescription.getId());
        Conversation conversation = conversationService.getOne(conversationWrapper);
        if (conversation != null) {
            dto.setConversationId(conversation.getId());
        }
        
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionDTO reviewPrescription(Long prescriptionId, Long doctorId, Integer status, String comment) {
        // 获取处方
        Prescription prescription = prescriptionService.getById(prescriptionId);
        if (prescription == null) {
            return null;
        }
        
        // 获取医生所属医疗方向
        User doctor = userService.getById(doctorId);
        if (doctor == null || doctor.getDirectionId() == null) {
            return null;
        }
        
        // 验证处方是否属于医生所属医疗方向
        if (!prescription.getDirectionId().equals(doctor.getDirectionId())) {
            return null;
        }
        
        // 检查处方是否已经审核过
        if (prescription.getReviewStatus() != 0) {
            log.warn("处方已经审核过: prescriptionId={}, currentStatus={}", prescriptionId, prescription.getReviewStatus());
            return null;
        }
        
        // 更新处方审核状态
        prescription.setReviewStatus(status);
        prescription.setReviewerId(doctorId);
        prescription.setReviewComment(comment);
        prescription.setUpdateTime(LocalDateTime.now());
        
        // 保存更新
        boolean success = prescriptionService.updateById(prescription);
        if (!success) {
            log.error("更新处方审核状态失败: prescriptionId={}", prescriptionId);
            return null;
        }
        
        // 返回更新后的处方详情
        return getPrescriptionDetail(prescriptionId, doctorId);
    }

    @Override
    public ConversationDetailDTO getConversationDetail(Long conversationId, Long doctorId) {
        // 获取对话
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return null;
        }
        
        // 如果对话没有关联处方，无权查看
        if (conversation.getPrescriptionId() == null) {
            return null;
        }
        
        // 获取关联的处方
        Prescription prescription = prescriptionService.getById(conversation.getPrescriptionId());
        if (prescription == null) {
            return null;
        }
        
        // 获取医生所属医疗方向
        User doctor = userService.getById(doctorId);
        if (doctor == null || doctor.getDirectionId() == null) {
            return null;
        }
        
        // 验证处方是否属于医生所属医疗方向
        if (!prescription.getDirectionId().equals(doctor.getDirectionId())) {
            return null;
        }
        
        // 转换为DTO
        ConversationDetailDTO dto = new ConversationDetailDTO();
        BeanUtils.copyProperties(conversation, dto);
        
        // 获取用户信息
        User user = userService.getById(conversation.getUserId());
        if (user != null) {
            dto.setUserName(user.getRealName());
        }
        
        // 获取智能体信息
        Agent agent = agentService.getById(conversation.getAgentId());
        if (agent != null) {
            dto.setAgentName(agent.getName());
            
            // 获取医疗方向
            MedicalDirection direction = medicalDirectionService.getById(agent.getDirectionId());
            if (direction != null) {
                dto.setDirectionName(direction.getName());
            }
        }
        
        // 解析对话内容
        List<MessageDTO> messages = parseConversationContent(conversation.getContent());
        dto.setMessages(messages);
        
        return dto;
    }
    
    /**
     * 解析对话内容JSON字符串为消息列表
     * @param content 对话内容JSON字符串
     * @return 消息列表
     */
    private List<MessageDTO> parseConversationContent(String content) {
        List<MessageDTO> messages = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return messages;
        }
        
        try {
            // 假设content是JSON数组格式的字符串
            messages = objectMapper.readValue(content, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MessageDTO.class));
        } catch (JsonProcessingException e) {
            log.error("解析对话内容失败: {}", e.getMessage());
        }
        
        return messages;
    }

    @Override
    public StatsDTO getDashboardStats(Long doctorId) {
        // 获取医生所属医疗方向
        User doctor = userService.getById(doctorId);
        if (doctor == null || doctor.getDirectionId() == null) {
            return new StatsDTO();
        }
        
        Long directionId = doctor.getDirectionId();
        StatsDTO stats = new StatsDTO();
        
        // 查询总处方数
        LambdaQueryWrapper<Prescription> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.eq(Prescription::getDirectionId, directionId);
        stats.setTotalPrescriptions(Math.toIntExact(prescriptionService.count(totalQuery)));
        
        // 查询待审核处方数
        LambdaQueryWrapper<Prescription> pendingQuery = new LambdaQueryWrapper<>();
        pendingQuery.eq(Prescription::getDirectionId, directionId);
        pendingQuery.eq(Prescription::getReviewStatus, 0);
        stats.setPendingReview(Math.toIntExact(prescriptionService.count(pendingQuery)));
        
        // 查询已通过处方数
        LambdaQueryWrapper<Prescription> approvedQuery = new LambdaQueryWrapper<>();
        approvedQuery.eq(Prescription::getDirectionId, directionId);
        approvedQuery.eq(Prescription::getReviewStatus, 1);
        stats.setApprovedPrescriptions(Math.toIntExact(prescriptionService.count(approvedQuery)));
        
        // 查询已拒绝处方数
        LambdaQueryWrapper<Prescription> rejectedQuery = new LambdaQueryWrapper<>();
        rejectedQuery.eq(Prescription::getDirectionId, directionId);
        rejectedQuery.eq(Prescription::getReviewStatus, 2);
        stats.setRejectedPrescriptions(Math.toIntExact(prescriptionService.count(rejectedQuery)));
        
        return stats;
    }

    @Override
    public Map<String, Object> getDoctorProfile(Long doctorId) {
        // 获取医生信息
        User doctor = userService.getById(doctorId);
        if (doctor == null) {
            return null;
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", doctor.getId());
        profile.put("username", doctor.getUsername());
        profile.put("realName", doctor.getRealName());
        profile.put("gender", doctor.getGender());
        profile.put("age", doctor.getAge());
        
        // 获取医疗方向
        if (doctor.getDirectionId() != null) {
            MedicalDirection direction = medicalDirectionService.getById(doctor.getDirectionId());
            if (direction != null) {
                profile.put("directionId", direction.getId());
                profile.put("directionName", direction.getName());
                profile.put("directionDescription", direction.getDescription());
            }
        }
        
        return profile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateDoctorProfile(Long doctorId, Map<String, Object> profile) {
        // 获取医生信息
        User doctor = userService.getById(doctorId);
        if (doctor == null) {
            return null;
        }
        
        // 更新基本信息
        if (profile.containsKey("realName")) {
            doctor.setRealName((String) profile.get("realName"));
        }
        
        if (profile.containsKey("gender")) {
            doctor.setGender((Integer) profile.get("gender"));
        }
        
        if (profile.containsKey("age")) {
            doctor.setAge((Integer) profile.get("age"));
        }
        
        // 更新医生信息
        boolean success = userService.updateById(doctor);
        if (!success) {
            log.error("更新医生信息失败: doctorId={}", doctorId);
            return null;
        }
        
        // 返回更新后的个人信息
        return getDoctorProfile(doctorId);
    }

    @Override
    public boolean changePassword(Long doctorId, String oldPassword, String newPassword) {
        // 获取医生信息
        User doctor = userService.getById(doctorId);
        if (doctor == null) {
            return false;
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, doctor.getPassword())) {
            log.warn("旧密码验证失败: doctorId={}", doctorId);
            return false;
        }
        
        // 更新密码
        doctor.setPassword(passwordEncoder.encode(newPassword));
        
        // 保存更新
        boolean success = userService.updateById(doctor);
        if (!success) {
            log.error("更新密码失败: doctorId={}", doctorId);
            return false;
        }
        
        return true;
    }
} 