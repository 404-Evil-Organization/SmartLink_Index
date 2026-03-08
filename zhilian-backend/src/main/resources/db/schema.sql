-- 创建数据库
CREATE DATABASE IF NOT EXISTS `smartlink_index` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `smartlink_index`;

-- 1. 用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一标识',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    `password` VARCHAR(255) NOT NULL COMMENT '加密存储',
    `role` ENUM('manufacture','service','park','admin') NOT NULL COMMENT '角色：制造企业、服务商、园区/政府、管理员',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '电子邮箱',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role (`role`),
    INDEX idx_status (`status`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 制造企业表
CREATE TABLE `manufacture` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '企业唯一标识',
    `user_id` BIGINT UNIQUE COMMENT '关联user.id',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业全称',
    `region` VARCHAR(50) COMMENT '所在区域（深圳/东莞/惠州/广州等）',
    `address` VARCHAR(200) COMMENT '详细地址',
    `contact_person` VARCHAR(50) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系人电话',
    `scale` ENUM('micro','small','medium','large') COMMENT '规模：微型、小型、中型、大型',
    `employee_count` INT COMMENT '员工人数',
    `annual_revenue` DECIMAL(15,2) COMMENT '年营收（万元）',
    `product_type` VARCHAR(100) COMMENT '主营产品类型（如PCB、半导体、消费电子）',
    `description` TEXT COMMENT '企业简介',
    `logo` VARCHAR(255) COMMENT 'Logo图片URL',
    `established_date` DATE COMMENT '成立日期',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX idx_region (`region`),
    INDEX idx_scale (`scale`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='制造企业表';

-- 3. 服务商表
CREATE TABLE `service_provider` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '服务商唯一标识',
    `user_id` BIGINT UNIQUE COMMENT '关联user.id',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业全称',
    `region` VARCHAR(50) COMMENT '所在区域',
    `address` VARCHAR(200) COMMENT '详细地址',
    `contact_person` VARCHAR(50) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系人电话',
    `service_type` VARCHAR(200) COMMENT '服务大类（如检测认证、工业设计、物流等），可多选，用逗号分隔或JSON',
    `description` TEXT COMMENT '服务介绍',
    `logo` VARCHAR(255) COMMENT 'Logo图片URL',
    `website` VARCHAR(100) COMMENT '企业官网',
    `established_date` DATE COMMENT '成立日期',
    `employee_count` INT COMMENT '员工人数',
    `qualification` TEXT COMMENT '资质概述',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX idx_region (`region`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务商表';

-- 4. 需求表
CREATE TABLE `demand` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求唯一标识',
    `manu_id` BIGINT NOT NULL COMMENT '关联manufacture.id',
    `title` VARCHAR(200) NOT NULL COMMENT '需求标题',
    `description` TEXT COMMENT '详细描述',
    `expected_budget` DECIMAL(12,2) COMMENT '预算金额（万元）',
    `deadline` DATE COMMENT '期望完成日期',
    `status` ENUM('draft','published','matched','closed') DEFAULT 'draft' COMMENT '状态：草稿、已发布、已匹配、已关闭',
    `views` INT DEFAULT 0 COMMENT '浏览次数',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`manu_id`) REFERENCES `manufacture`(`id`) ON DELETE CASCADE,
    INDEX idx_manu_id (`manu_id`),
    INDEX idx_status (`status`),
    INDEX idx_create_time (`create_time`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求表';

-- 5. 标签字典表
CREATE TABLE `tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签唯一标识',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称（如“PCB设计”、“CE认证”）',
    `category` VARCHAR(50) NOT NULL DEFAULT 'general' COMMENT '标签类别（如“服务类型”、“认证类型”）',
    `description` VARCHAR(200) COMMENT '标签说明',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    UNIQUE INDEX uk_name_category (`name`, `category`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签字典表';

-- 6. 需求标签关系表
CREATE TABLE `demand_tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `demand_id` BIGINT NOT NULL COMMENT '关联demand.id',
    `tag_id` BIGINT NOT NULL COMMENT '关联tag.id',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`demand_id`) REFERENCES `demand`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_demand_tag` (`demand_id`, `tag_id`, `deleted`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求标签关系表';

-- 7. 服务商能力标签表
CREATE TABLE `service_tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `service_id` BIGINT NOT NULL COMMENT '关联service_provider.id',
    `tag_id` BIGINT NOT NULL COMMENT '关联tag.id',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`service_id`) REFERENCES `service_provider`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_service_tag` (`service_id`, `tag_id`, `deleted`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务商能力标签表';

-- 8. 合作记录表
CREATE TABLE `cooperation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '合作唯一标识',
    `manu_id` BIGINT NOT NULL COMMENT '关联manufacture.id',
    `service_id` BIGINT NOT NULL COMMENT '关联service_provider.id',
    `demand_id` BIGINT COMMENT '关联demand.id（可选）',
    `start_date` DATE COMMENT '合作开始日期',
    `end_date` DATE COMMENT '合作结束日期',
    `amount` DECIMAL(12,2) COMMENT '合同金额（万元）',
    `description` VARCHAR(500) COMMENT '合作内容简述',
    `status` ENUM('ongoing','completed','cancelled') DEFAULT 'ongoing' COMMENT '合作状态',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`manu_id`) REFERENCES `manufacture`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`service_id`) REFERENCES `service_provider`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`demand_id`) REFERENCES `demand`(`id`) ON DELETE SET NULL,
    INDEX idx_manu_id (`manu_id`),
    INDEX idx_service_id (`service_id`),
    INDEX idx_demand_id (`demand_id`),
    INDEX idx_status (`status`),
    INDEX idx_start_date (`start_date`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合作记录表';

-- 9. 评价表
CREATE TABLE `evaluation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价唯一标识',
    `coop_id` BIGINT NOT NULL COMMENT '关联cooperation.id，一次合作可多条评价（按角色区分）',
    `evaluator_id` BIGINT NOT NULL COMMENT '评价人 user.id',
    `evaluator_role` ENUM('manufacture','service') NOT NULL COMMENT '评价人角色（制造企业/服务商）',
    `score` TINYINT NOT NULL COMMENT '评分（1-5星）',
    `content` VARCHAR(500) COMMENT '评价内容',
    `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名（0否 1是）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`coop_id`) REFERENCES `cooperation`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`evaluator_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX idx_score (`score`),
    INDEX idx_evaluator_id (`evaluator_id`),
    INDEX idx_evaluator_role (`evaluator_role`),
    UNIQUE KEY uniq_coop_evaluator_role (`coop_id`, `evaluator_role`),
    INDEX idx_deleted (`deleted`),
    CHECK (`score` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 10. 诊断记录表
CREATE TABLE `diagnosis` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '诊断唯一标识',
    `manu_id` BIGINT NOT NULL COMMENT '关联manufacture.id',
    `info_score` TINYINT COMMENT '信息化得分（1-5）',
    `auto_score` TINYINT COMMENT '自动化得分（1-5）',
    `data_score` TINYINT COMMENT '数据应用得分（1-5）',
    `service_score` TINYINT COMMENT '服务协同得分（1-5）',
    `total_score` TINYINT COMMENT '总分（0-100）',
    `level` VARCHAR(20) COMMENT '等级（起步期/成长期/成熟期/引领期）',
    `suggestions` TEXT COMMENT '改进建议（可JSON存储多条）',
    `diagnosis_date` DATETIME COMMENT '诊断日期',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`manu_id`) REFERENCES `manufacture`(`id`) ON DELETE CASCADE,
    INDEX idx_manu_id (`manu_id`),
    INDEX idx_diagnosis_date (`diagnosis_date`),
    INDEX idx_deleted (`deleted`),
    CHECK (`info_score` BETWEEN 1 AND 5),
    CHECK (`auto_score` BETWEEN 1 AND 5),
    CHECK (`data_score` BETWEEN 1 AND 5),
    CHECK (`service_score` BETWEEN 1 AND 5),
    CHECK (`total_score` BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='诊断记录表';

-- 11. 区域指数表
CREATE TABLE `region_index` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录唯一标识',
    `region` VARCHAR(50) NOT NULL COMMENT '区域名称（深圳/东莞/惠州/广州等）',
    `year` SMALLINT NOT NULL COMMENT '年份',
    `quarter` TINYINT NOT NULL COMMENT '季度（1-4）或月份（1-12），根据统计粒度',
    `coop_density` DECIMAL(8,4) COMMENT '合作密度（合作次数/企业总数）',
    `service_rate` DECIMAL(8,4) COMMENT '服务渗透率（使用服务企业数/制造企业总数）',
    `cross_rate` DECIMAL(8,4) COMMENT '跨域协同度（跨区域合作次数/总合作次数）',
    `total_index` DECIMAL(6,2) COMMENT '协同指数综合得分',
    `calc_time` DATETIME COMMENT '计算时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    INDEX idx_year_quarter (`year`, `quarter`),
    UNIQUE INDEX uk_region_year_quarter (`region`, `year`, `quarter`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域指数表';

-- 12. 资质证书表
CREATE TABLE `certification` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '证书唯一标识',
    `service_id` BIGINT NOT NULL COMMENT '关联service_provider.id',
    `cert_name` VARCHAR(100) NOT NULL COMMENT '证书名称（如CNAS、CMA）',
    `cert_no` VARCHAR(50) COMMENT '证书编号',
    `issue_authority` VARCHAR(100) COMMENT '发证机构',
    `issue_date` DATE COMMENT '发证日期',
    `expire_date` DATE COMMENT '有效期至',
    `cert_file_url` VARCHAR(255) COMMENT '证书文件路径',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0失效 1有效',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`service_id`) REFERENCES `service_provider`(`id`) ON DELETE CASCADE,
    INDEX idx_service_id (`service_id`),
    INDEX idx_expire_date (`expire_date`),
    INDEX idx_status (`status`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资质证书表';

-- 13. 信用分记录表
CREATE TABLE `credit_score` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录唯一标识',
    `service_id` BIGINT NOT NULL COMMENT '关联service_provider.id',
    `score` TINYINT COMMENT '综合信用分（0-100）',
    `qual_score` TINYINT COMMENT '资质分',
    `case_score` TINYINT COMMENT '案例分',
    `eval_score` TINYINT COMMENT '评价分',
    `calc_time` DATETIME COMMENT '计算时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (`service_id`) REFERENCES `service_provider`(`id`) ON DELETE CASCADE,
    INDEX idx_service_id (`service_id`),
    INDEX idx_calc_time (`calc_time`),
    INDEX idx_deleted (`deleted`),
    CHECK (`score` BETWEEN 0 AND 100),
    CHECK (`qual_score` BETWEEN 0 AND 100),
    CHECK (`case_score` BETWEEN 0 AND 100),
    CHECK (`eval_score` BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信用分记录表';

-- 14. 出海成功案例表
CREATE TABLE `abroad_case` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '案例唯一标识',
    `title` VARCHAR(200) COMMENT '案例标题',
    `company_name` VARCHAR(100) COMMENT '企业名称（可为制造企业或服务商）',
    `company_type` ENUM('manufacture','service') COMMENT '企业类型',
    `country` VARCHAR(50) COMMENT '目标国家',
    `service_type` VARCHAR(100) COMMENT '涉及服务类型（如CE认证、物流）',
    `description` TEXT COMMENT '案例详情',
    `cover_image` VARCHAR(255) COMMENT '封面图URL',
    `publish_time` DATETIME COMMENT '发布时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0草稿 1发布',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    INDEX idx_country (`country`),
    INDEX idx_publish_time (`publish_time`),
    INDEX idx_status (`status`),
    INDEX idx_deleted (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出海成功案例表';