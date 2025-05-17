INSERT INTO agent (
    name,
    direction_id,
    model_name,
    model_url,
    api_key,
    template_id,
    template_description,
    template_parameters,
    vector_namespaces,
    precise_db_name,
    precise_db_uids,
    create_time,
    update_time
) VALUES (
    '内科智能助手',                -- 智能体名称
    1,                          -- 医疗方向ID（上一步查到的id）
    'deepseek-chat',            -- 对接的大模型名称
    'https://api.deepseek.com/v1', -- 对接的大模型链接地址
    'your-api-key',             -- 对接的大模型的apikey
    'medical_template',         -- 使用的模板ID
    '内科问诊大模型模板',         -- 模板描述
    '["patient_age","patient_gender","symptoms"]', -- 模板参数（JSON格式）
    '["medical","disease"]',    -- 向量数据库命名空间（JSON格式）
    'hypertension_db',          -- 精确查找数据库名称
    '["cat_12345678"]',         -- 精确查找数据库UID列表（JSON格式）
    NOW(),                      -- 创建时间
    NOW()                       -- 更新时间
);