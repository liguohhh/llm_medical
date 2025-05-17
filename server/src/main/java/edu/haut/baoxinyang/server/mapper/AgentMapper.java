package edu.haut.baoxinyang.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.haut.baoxinyang.server.entity.Agent;
import org.springframework.stereotype.Repository;

/**
 * 智能体Mapper接口
 * 
 * 继承BaseMapper后，MyBatis-Plus会自动提供基础的CRUD方法：
 * - insert: 插入记录
 * - deleteById: 根据ID删除
 * - updateById: 根据ID更新
 * - selectById: 根据ID查询
 * - selectList: 查询列表
 * - selectCount: 查询总数
 * 等常用方法
 */
@Repository
public interface AgentMapper extends BaseMapper<Agent> {
    
} 