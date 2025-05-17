-- 创建医疗方向表
USE llm_medical;
CREATE TABLE IF NOT EXISTS `medical_direction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `uid` VARCHAR(32) NOT NULL COMMENT '唯一标识',
  `name` VARCHAR(50) NOT NULL COMMENT '医疗方向名称（内科、外科等）',
  `description` TEXT COMMENT '具体描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uid` (`uid`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗方向表';

-- 创建用户表（包含病人、医生、管理员）
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '账户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) COMMENT '姓名',
  `gender` TINYINT COMMENT '性别：0-女，1-男',
  `age` INT COMMENT '年龄',
  `user_type` TINYINT NOT NULL COMMENT '用户类型：0-病人，1-医生，2-管理员',
  `direction_id` BIGINT COMMENT '医疗方向ID，仅医生需要',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  CONSTRAINT `fk_user_direction` FOREIGN KEY (`direction_id`) REFERENCES `medical_direction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建智能体表
CREATE TABLE IF NOT EXISTS `agent` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '智能体名称',
  `direction_id` BIGINT NOT NULL COMMENT '医疗方向ID',
  `model_name` VARCHAR(100) NOT NULL COMMENT '对接的大模型名称',
  `model_url` VARCHAR(255) COMMENT '对接的大模型链接地址',
  `api_key` VARCHAR(100) COMMENT '对接的大模型的apikey',
  `template_id` VARCHAR(100) COMMENT '使用的模板ID',
  `template_description` TEXT COMMENT '模板描述',
  `template_parameters` JSON COMMENT '模板参数，JSON格式存储',
  `vector_namespaces` JSON COMMENT '使用的向量数据库命名空间，JSON格式存储',
  `precise_db_name` VARCHAR(100) COMMENT '使用的精确查找数据库名称',
  `precise_db_uids` JSON COMMENT '精确查找数据库UID列表，JSON格式存储',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_agent_direction` FOREIGN KEY (`direction_id`) REFERENCES `medical_direction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体表';

-- 创建对话表
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `uid` VARCHAR(32) NOT NULL COMMENT '对话唯一标识',
  `user_id` BIGINT NOT NULL COMMENT '病人ID',
  `agent_id` BIGINT NOT NULL COMMENT '智能体ID',
  `content` JSON NOT NULL COMMENT '对话内容，JSON格式存储',
  `is_finished` TINYINT NOT NULL DEFAULT 0 COMMENT '是否结束：0-未结束，1-已结束',
  `prescription_id` BIGINT COMMENT '处方ID，若已开处方则关联',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uid` (`uid`),
  CONSTRAINT `fk_conversation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_conversation_agent` FOREIGN KEY (`agent_id`) REFERENCES `agent` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话表';

-- 创建处方表
CREATE TABLE IF NOT EXISTS `prescription` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '病人ID',
  `agent_id` BIGINT NOT NULL COMMENT '智能体ID',
  `direction_id` BIGINT NOT NULL COMMENT '医疗方向ID',
  `content` TEXT NOT NULL COMMENT '处方内容',
  `review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-未审核，1-审核通过，2-审核不通过',
  `reviewer_id` BIGINT COMMENT '审核医生ID',
  `review_comment` TEXT COMMENT '审核意见',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_prescription_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_prescription_agent` FOREIGN KEY (`agent_id`) REFERENCES `agent` (`id`),
  CONSTRAINT `fk_prescription_direction` FOREIGN KEY (`direction_id`) REFERENCES `medical_direction` (`id`),
  CONSTRAINT `fk_prescription_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

-- 添加外键关联，将处方ID关联到对话表
ALTER TABLE `conversation` 
ADD CONSTRAINT `fk_conversation_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`);

-- 插入默认管理员账户
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`) 
VALUES ('admin', 'admin123', 2, '系统管理员'); 