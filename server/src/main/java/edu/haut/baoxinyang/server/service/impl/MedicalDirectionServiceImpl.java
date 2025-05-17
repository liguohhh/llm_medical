package edu.haut.baoxinyang.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.haut.baoxinyang.server.entity.MedicalDirection;
import edu.haut.baoxinyang.server.mapper.MedicalDirectionMapper;
import edu.haut.baoxinyang.server.service.MedicalDirectionService;
import org.springframework.stereotype.Service;

/**
 * 医疗方向Service实现类
 */
@Service
public class MedicalDirectionServiceImpl extends ServiceImpl<MedicalDirectionMapper, MedicalDirection> implements MedicalDirectionService {
    
    @Override
    public MedicalDirection getByUid(String uid) {
        LambdaQueryWrapper<MedicalDirection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalDirection::getUid, uid);
        return baseMapper.selectOne(wrapper);
    }
    
} 