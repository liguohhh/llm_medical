package edu.haut.baoxinyang.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.haut.baoxinyang.server.entity.Prescription;

import java.util.List;

/**
 * 处方Service接口
 */
public interface PrescriptionService extends IService<Prescription> {
    
    /**
     * 根据用户ID查询处方列表
     * @param userId 用户ID
     * @return 处方列表
     */
    List<Prescription> getByUserId(Long userId);
    
    /**
     * 根据医疗方向ID查询待审核的处方列表
     * @param directionId 医疗方向ID
     * @return 待审核的处方列表
     */
    List<Prescription> getPendingReviewByDirectionId(Long directionId);
    
    /**
     * 根据医生ID查询已审核的处方列表
     * @param reviewerId 医生ID
     * @return 已审核的处方列表
     */
    List<Prescription> getReviewedByReviewerId(Long reviewerId);
    
} 