package edu.haut.baoxinyang.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.haut.baoxinyang.server.entity.MedicalDirection;

/**
 * 医疗方向Service接口
 */
public interface MedicalDirectionService extends IService<MedicalDirection> {
    
    /**
     * 根据UID查询医疗方向
     * @param uid 医疗方向UID
     * @return 医疗方向
     */
    MedicalDirection getByUid(String uid);
    
} 