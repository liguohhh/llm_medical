package edu.haut.baoxinyang.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.haut.baoxinyang.server.entity.Prescription;
import edu.haut.baoxinyang.server.mapper.PrescriptionMapper;
import edu.haut.baoxinyang.server.service.PrescriptionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 处方Service实现类
 */
@Service
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription> implements PrescriptionService {
    
    @Override
    public List<Prescription> getByUserId(Long userId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getUserId, userId)
                .orderByDesc(Prescription::getUpdateTime);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<Prescription> getPendingReviewByDirectionId(Long directionId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getDirectionId, directionId)
                .eq(Prescription::getReviewStatus, 0)  // 未审核
                .orderByDesc(Prescription::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<Prescription> getReviewedByReviewerId(Long reviewerId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getReviewerId, reviewerId)
                .in(Prescription::getReviewStatus, 1, 2)  // 已审核（通过/不通过）
                .orderByDesc(Prescription::getUpdateTime);
        return baseMapper.selectList(wrapper);
    }
    
} 