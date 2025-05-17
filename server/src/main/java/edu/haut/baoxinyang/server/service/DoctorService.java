package edu.haut.baoxinyang.server.service;

import edu.haut.baoxinyang.server.dto.ConversationDetailDTO;
import edu.haut.baoxinyang.server.dto.PrescriptionDTO;
import edu.haut.baoxinyang.server.dto.StatsDTO;

import java.util.Map;

/**
 * 医生服务接口
 */
public interface DoctorService {
    
    /**
     * 获取处方列表
     * 
     * @param doctorId 医生ID
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方式 asc/desc
     * @param status 审核状态 0-待审核 1-已通过 2-已拒绝
     * @param keyword 关键词搜索
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 处方列表和分页信息
     */
    Map<String, Object> getPrescriptions(
            Long doctorId, Integer page, Integer size, String sort, String order,
            Integer status, String keyword, String startDate, String endDate);
    
    /**
     * 获取处方详情
     * 
     * @param prescriptionId 处方ID
     * @param doctorId 医生ID
     * @return 处方详情
     */
    PrescriptionDTO getPrescriptionDetail(Long prescriptionId, Long doctorId);
    
    /**
     * 审核处方
     * 
     * @param prescriptionId 处方ID
     * @param doctorId 医生ID
     * @param status 审核状态 1-通过 2-拒绝
     * @param comment 审核意见
     * @return 处方详情
     */
    PrescriptionDTO reviewPrescription(Long prescriptionId, Long doctorId, Integer status, String comment);
    
    /**
     * 获取对话详情
     * 
     * @param conversationId 对话ID
     * @param doctorId 医生ID
     * @return 对话详情
     */
    ConversationDetailDTO getConversationDetail(Long conversationId, Long doctorId);
    
    /**
     * 获取医生首页统计数据
     * 
     * @param doctorId 医生ID
     * @return 统计数据
     */
    StatsDTO getDashboardStats(Long doctorId);
    
    /**
     * 获取医生个人信息
     * 
     * @param doctorId 医生ID
     * @return 医生个人信息
     */
    Map<String, Object> getDoctorProfile(Long doctorId);
    
    /**
     * 更新医生个人信息
     * 
     * @param doctorId 医生ID
     * @param profile 个人信息
     * @return 更新后的个人信息
     */
    Map<String, Object> updateDoctorProfile(Long doctorId, Map<String, Object> profile);
    
    /**
     * 修改密码
     * 
     * @param doctorId 医生ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long doctorId, String oldPassword, String newPassword);
} 