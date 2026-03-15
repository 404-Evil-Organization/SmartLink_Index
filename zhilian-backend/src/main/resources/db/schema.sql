-- 创建数据库
CREATE
DATABASE IF NOT EXISTS `smartlink_index`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE
`smartlink_index`;

-- =====================================================
-- 表 `user` 用户表
-- =====================================================
CREATE TABLE `user`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '用户唯一标识',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(255) NOT NULL COMMENT '加密存储',
    `role`        ENUM('manufacture','service','park','admin') NOT NULL COMMENT '角色：制造企业、服务商、园区/政府、管理员',
    `phone`       VARCHAR(20) COMMENT '联系电话',
    `email`       VARCHAR(100) COMMENT '电子邮箱',
    `status`      TINYINT  DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `deleted`     DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username_deleted` (`username`, `deleted`),
    KEY           `idx_role` (`role`),
    KEY           `idx_status` (`status`),
    KEY           `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 表 `manufacture` 制造企业表
-- =====================================================
CREATE TABLE `manufacture`
(
    `id`               BIGINT AUTO_INCREMENT COMMENT '企业唯一标识',
    `user_id`          BIGINT       NOT NULL COMMENT '关联user.id',
    `company_name`     VARCHAR(100) NOT NULL COMMENT '企业全称',
    `region`           VARCHAR(50) COMMENT '所在区域（深圳/东莞/惠州/广州等）',
    `address`          VARCHAR(200) COMMENT '详细地址',
    `contact_person`   VARCHAR(50) COMMENT '联系人',
    `contact_phone`    VARCHAR(20) COMMENT '联系人电话',
    `scale`            ENUM('micro','small','medium','large') COMMENT '规模：微型、小型、中型、大型',
    `employee_count`   INT COMMENT '员工人数',
    `annual_revenue`   DECIMAL(15, 2) COMMENT '年营收（万元）',
    `product_type`     VARCHAR(100) COMMENT '主营产品类型（如PCB、半导体、消费电子）',
    `description`      TEXT COMMENT '企业简介',
    `logo`             VARCHAR(255) COMMENT 'Logo图片URL',
    `established_date` DATE COMMENT '成立日期',
    `audit_status`     ENUM('pending','approved','rejected') DEFAULT 'pending' COMMENT '审核状态：待审核、通过、驳回',
    `audit_remark`     VARCHAR(500) COMMENT '审核意见（驳回时填写）',
    `audit_time`       DATETIME COMMENT '审核时间',
    `audit_user_id`    BIGINT COMMENT '审核人ID，关联user.id',
    `deleted`          DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_deleted` (`user_id`, `deleted`),
    KEY                `idx_region` (`region`),
    KEY                `idx_scale` (`scale`),
    KEY                `idx_audit_status` (`audit_status`),
    KEY                `idx_deleted` (`deleted`),
    KEY                `idx_audit_user_id` (`audit_user_id`),
    CONSTRAINT `fk_manufacture_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_manufacture_audit_user` FOREIGN KEY (`audit_user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='制造企业表';

-- =====================================================
-- 表 `service_provider` 服务商表
-- =====================================================
CREATE TABLE `service_provider`
(
    `id`               BIGINT AUTO_INCREMENT COMMENT '服务商唯一标识',
    `user_id`          BIGINT       NOT NULL COMMENT '关联user.id',
    `company_name`     VARCHAR(100) NOT NULL COMMENT '企业全称',
    `region`           VARCHAR(50) COMMENT '所在区域',
    `address`          VARCHAR(200) COMMENT '详细地址',
    `contact_person`   VARCHAR(50) COMMENT '联系人',
    `contact_phone`    VARCHAR(20) COMMENT '联系人电话',
    `service_type`     VARCHAR(200) COMMENT '服务大类（如检测认证、工业设计、物流等），可多选，用逗号分隔或JSON',
    `description`      TEXT COMMENT '服务介绍',
    `logo`             VARCHAR(255) COMMENT 'Logo图片URL',
    `website`          VARCHAR(100) COMMENT '企业官网',
    `established_date` DATE COMMENT '成立日期',
    `employee_count`   INT COMMENT '员工人数',
    `qualification`    TEXT COMMENT '资质概述',
    `audit_status`     ENUM('pending','approved','rejected') DEFAULT 'pending' COMMENT '审核状态：待审核、通过、驳回',
    `audit_remark`     VARCHAR(500) COMMENT '审核意见（驳回时填写）',
    `audit_time`       DATETIME COMMENT '审核时间',
    `audit_user_id`    BIGINT COMMENT '审核人ID，关联user.id',
    `deleted`          DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_deleted` (`user_id`, `deleted`),
    KEY                `idx_region` (`region`),
    KEY                `idx_audit_status` (`audit_status`),
    KEY                `idx_deleted` (`deleted`),
    KEY                `idx_audit_user_id` (`audit_user_id`),
    CONSTRAINT `fk_service_provider_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_service_provider_audit_user` FOREIGN KEY (`audit_user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务商表';

-- =====================================================
-- 表 `demand` 需求表
-- =====================================================
CREATE TABLE `demand`
(
    `id`              BIGINT AUTO_INCREMENT COMMENT '需求唯一标识',
    `manu_id`         BIGINT       NOT NULL COMMENT '关联manufacture.id',
    `title`           VARCHAR(200) NOT NULL COMMENT '需求标题',
    `description`     TEXT COMMENT '详细描述',
    `expected_budget` DECIMAL(12, 2) COMMENT '预算金额（万元）',
    `deadline`        DATE COMMENT '期望完成日期',
    `status`          ENUM('draft','published','matched','closed') DEFAULT 'draft' COMMENT '业务状态：草稿、已发布、已匹配、已关闭',
    `views`           INT      DEFAULT 0 COMMENT '浏览次数',
    `audit_status`    ENUM('pending','approved','rejected') DEFAULT 'pending' COMMENT '审核状态：待审核、通过、驳回',
    `audit_remark`    VARCHAR(500) COMMENT '审核意见（驳回时填写）',
    `audit_time`      DATETIME COMMENT '审核时间',
    `audit_user_id`   BIGINT COMMENT '审核人ID，关联user.id',
    `deleted`         DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY               `idx_manu_id` (`manu_id`),
    KEY               `idx_audit_user_id` (`audit_user_id`),
    KEY               `idx_status` (`status`),
    KEY               `idx_audit_status` (`audit_status`),
    KEY               `idx_create_time` (`create_time`),
    KEY               `idx_deleted` (`deleted`),
    CONSTRAINT `fk_demand_manufacture` FOREIGN KEY (`manu_id`) REFERENCES `manufacture` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_demand_audit_user` FOREIGN KEY (`audit_user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求表';

-- =====================================================
-- 表 `tag` 标签字典表
-- =====================================================
CREATE TABLE `tag`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '标签唯一标识',
    `name`        VARCHAR(50) NOT NULL COMMENT '标签名称（如“PCB设计”、“CE认证”）',
    `category`    VARCHAR(50) NOT NULL DEFAULT 'general' COMMENT '标签类别（如“服务类型”、“认证类型”）',
    `description` VARCHAR(200) COMMENT '标签说明',
    `deleted`     DATETIME             DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time` DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name_category_deleted` (`name`, `category`, `deleted`),
    KEY           `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签字典表';

-- =====================================================
-- 表 `demand_tag` 需求标签关系表
-- =====================================================
CREATE TABLE `demand_tag`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '主键',
    `demand_id`   BIGINT NOT NULL COMMENT '关联demand.id',
    `tag_id`      BIGINT NOT NULL COMMENT '关联tag.id',
    `deleted`     DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demand_tag_deleted` (`demand_id`, `tag_id`, `deleted`),
    KEY           `idx_deleted` (`deleted`),
    CONSTRAINT `fk_demand_tag_demand` FOREIGN KEY (`demand_id`) REFERENCES `demand` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_demand_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求标签关系表';

-- =====================================================
-- 表 `manufacture_tag` 制造企业能力标签表
-- =====================================================
CREATE TABLE `manufacture_tag`
(
    `id`             BIGINT AUTO_INCREMENT COMMENT '主键',
    `manufacture_id` BIGINT NOT NULL COMMENT '关联manufacture.id',
    `tag_id`         BIGINT NOT NULL COMMENT '关联tag.id',
    `deleted`        DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_manufacture_tag_deleted` (`manufacture_id`, `tag_id`, `deleted`),
    KEY              `idx_manufacture_id` (`manufacture_id`),
    KEY              `idx_tag_id` (`tag_id`),
    KEY              `idx_deleted` (`deleted`),
    CONSTRAINT `fk_manufacture_tag_manufacture` FOREIGN KEY (`manufacture_id`) REFERENCES `manufacture` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_manufacture_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='制造企业-产品标签关联表';

-- =====================================================
-- 表 `service_tag` 服务商能力标签表
-- =====================================================
CREATE TABLE `service_tag`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '主键',
    `service_id`  BIGINT NOT NULL COMMENT '关联service_provider.id',
    `tag_id`      BIGINT NOT NULL COMMENT '关联tag.id',
    `deleted`     DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_service_tag_deleted` (`service_id`, `tag_id`, `deleted`),
    KEY           `idx_deleted` (`deleted`),
    CONSTRAINT `fk_service_tag_service` FOREIGN KEY (`service_id`) REFERENCES `service_provider` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_service_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务商能力标签表';

-- =====================================================
-- 表 `cooperation` 合作记录表
-- =====================================================
CREATE TABLE `cooperation`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '合作唯一标识',
    `manu_id`     BIGINT NOT NULL COMMENT '关联manufacture.id',
    `service_id`  BIGINT NOT NULL COMMENT '关联service_provider.id',
    `demand_id`   BIGINT COMMENT '关联demand.id（可选）',
    `start_date`  DATE COMMENT '合作开始日期',
    `end_date`    DATE COMMENT '合作结束日期',
    `amount`      DECIMAL(12, 2) COMMENT '合同金额（万元）',
    `description` VARCHAR(500) COMMENT '合作内容简述',
    `status`      ENUM('ongoing','completed','cancelled') DEFAULT 'ongoing' COMMENT '合作状态',
    `deleted`     DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_manu_id` (`manu_id`),
    KEY           `idx_service_id` (`service_id`),
    KEY           `idx_demand_id` (`demand_id`),
    KEY           `idx_status` (`status`),
    KEY           `idx_start_date` (`start_date`),
    KEY           `idx_deleted` (`deleted`),
    CONSTRAINT `fk_cooperation_manufacture` FOREIGN KEY (`manu_id`) REFERENCES `manufacture` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cooperation_service` FOREIGN KEY (`service_id`) REFERENCES `service_provider` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cooperation_demand` FOREIGN KEY (`demand_id`) REFERENCES `demand` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合作记录表';

-- =====================================================
-- 表 `evaluation` 评价表
-- =====================================================
CREATE TABLE `evaluation`
(
    `id`             BIGINT AUTO_INCREMENT COMMENT '评价唯一标识',
    `coop_id`        BIGINT  NOT NULL COMMENT '关联cooperation.id',
    `evaluator_id`   BIGINT  NOT NULL COMMENT '评价人 user.id',
    `evaluator_role` ENUM('manufacture','service') NOT NULL COMMENT '评价人角色（制造企业/服务商）',
    `score`          TINYINT NOT NULL COMMENT '评分（1-5星）',
    `content`        VARCHAR(500) COMMENT '评价内容',
    `is_anonymous`   TINYINT  DEFAULT 0 COMMENT '是否匿名（0否 1是）',
    `deleted`        DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_coop_evaluator_role_deleted` (`coop_id`, `evaluator_role`, `deleted`),
    KEY              `idx_score` (`score`),
    KEY              `idx_evaluator_id` (`evaluator_id`),
    KEY              `idx_evaluator_role` (`evaluator_role`),
    KEY              `idx_deleted` (`deleted`),
    CONSTRAINT `fk_evaluation_cooperation` FOREIGN KEY (`coop_id`) REFERENCES `cooperation` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_evaluation_user` FOREIGN KEY (`evaluator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `ck_evaluation_score` CHECK (`score` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- =====================================================
-- 表 `diagnosis` 诊断记录表
-- =====================================================
CREATE TABLE `diagnosis`
(
    `id`             BIGINT AUTO_INCREMENT COMMENT '诊断唯一标识',
    `manu_id`        BIGINT NOT NULL COMMENT '关联manufacture.id',
    `info_score`     TINYINT COMMENT '信息化得分（1-5）',
    `auto_score`     TINYINT COMMENT '自动化得分（1-5）',
    `data_score`     TINYINT COMMENT '数据应用得分（1-5）',
    `service_score`  TINYINT COMMENT '服务协同得分（1-5）',
    `total_score`    TINYINT COMMENT '总分（0-100）',
    `level`          VARCHAR(20) COMMENT '等级（起步期/成长期/成熟期/引领期）',
    `suggestions`    TEXT COMMENT '改进建议（可JSON存储多条）',
    `diagnosis_date` DATETIME COMMENT '诊断日期',
    `deleted`        DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY              `idx_manu_id` (`manu_id`),
    KEY              `idx_diagnosis_date` (`diagnosis_date`),
    KEY              `idx_deleted` (`deleted`),
    CONSTRAINT `fk_diagnosis_manufacture` FOREIGN KEY (`manu_id`) REFERENCES `manufacture` (`id`) ON DELETE CASCADE,
    CONSTRAINT `ck_diagnosis_info_score` CHECK (`info_score` BETWEEN 1 AND 5),
    CONSTRAINT `ck_diagnosis_auto_score` CHECK (`auto_score` BETWEEN 1 AND 5),
    CONSTRAINT `ck_diagnosis_data_score` CHECK (`data_score` BETWEEN 1 AND 5),
    CONSTRAINT `ck_diagnosis_service_score` CHECK (`service_score` BETWEEN 1 AND 5),
    CONSTRAINT `ck_diagnosis_total_score` CHECK (`total_score` BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='诊断记录表';

-- =====================================================
-- 表 `region_index` 区域指数表
-- =====================================================
CREATE TABLE `region_index`
(
    `id`           BIGINT AUTO_INCREMENT COMMENT '记录唯一标识',
    `region`       VARCHAR(50) NOT NULL COMMENT '区域名称（深圳/东莞/惠州/广州等）',
    `year`         SMALLINT    NOT NULL COMMENT '年份',
    `period_type`  ENUM('quarter','month') NOT NULL COMMENT '统计周期类型：quarter季度、month月度',
    `period_value` TINYINT     NOT NULL COMMENT '周期值：季度1-4，月份1-12',
    `coop_density` DECIMAL(8, 4) COMMENT '合作密度（合作次数/企业总数）',
    `service_rate` DECIMAL(5, 4) COMMENT '服务渗透率（使用服务企业数/制造企业总数）',
    `cross_rate`   DECIMAL(5, 4) COMMENT '跨域协同度（跨区域合作次数/总合作次数）',
    `total_index`  DECIMAL(6, 2) COMMENT '协同指数综合得分',
    `calc_time`    DATETIME COMMENT '计算时间',
    `deleted`      DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_region_year_period_deleted` (`region`, `year`, `period_type`, `period_value`, `deleted`),
    KEY            `idx_period` (`year`, `period_type`, `period_value`),
    KEY            `idx_deleted` (`deleted`),
    CONSTRAINT `ck_region_index_period` CHECK (
        (`period_type` = 'quarter' AND `period_value` BETWEEN 1 AND 4) OR
        (`period_type` = 'month' AND `period_value` BETWEEN 1 AND 12)
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域指数表';

-- =====================================================
-- 表 `certification` 资质证书表
-- =====================================================
CREATE TABLE `certification`
(
    `id`              BIGINT AUTO_INCREMENT COMMENT '证书唯一标识',
    `service_id`      BIGINT       NOT NULL COMMENT '关联service_provider.id',
    `cert_name`       VARCHAR(100) NOT NULL COMMENT '证书名称（如CNAS、CMA）',
    `cert_no`         VARCHAR(50) COMMENT '证书编号',
    `issue_authority` VARCHAR(100) COMMENT '发证机构',
    `issue_date`      DATE COMMENT '发证日期',
    `expire_date`     DATE COMMENT '有效期至',
    `cert_file_url`   VARCHAR(255) COMMENT '证书文件路径',
    `status`          TINYINT  DEFAULT 1 COMMENT '状态：0失效 1有效',
    `deleted`         DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY               `idx_service_id` (`service_id`),
    KEY               `idx_expire_date` (`expire_date`),
    KEY               `idx_status` (`status`),
    KEY               `idx_deleted` (`deleted`),
    CONSTRAINT `fk_certification_service` FOREIGN KEY (`service_id`) REFERENCES `service_provider` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资质证书表';

-- =====================================================
-- 表 `credit_score` 信用分记录表
-- =====================================================
CREATE TABLE `credit_score`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '记录唯一标识',
    `service_id`  BIGINT NOT NULL COMMENT '关联service_provider.id',
    `score`       TINYINT COMMENT '综合信用分（0-100）',
    `qual_score`  TINYINT COMMENT '资质分',
    `case_score`  TINYINT COMMENT '案例分',
    `eval_score`  TINYINT COMMENT '评价分',
    `calc_time`   DATETIME COMMENT '计算时间',
    `deleted`     DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_service_id` (`service_id`),
    KEY           `idx_calc_time` (`calc_time`),
    KEY           `idx_deleted` (`deleted`),
    CONSTRAINT `fk_credit_score_service` FOREIGN KEY (`service_id`) REFERENCES `service_provider` (`id`) ON DELETE CASCADE,
    CONSTRAINT `ck_credit_score_score` CHECK (`score` BETWEEN 0 AND 100),
    CONSTRAINT `ck_credit_score_qual` CHECK (`qual_score` BETWEEN 0 AND 100),
    CONSTRAINT `ck_credit_score_case` CHECK (`case_score` BETWEEN 0 AND 100),
    CONSTRAINT `ck_credit_score_eval` CHECK (`eval_score` BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信用分记录表';

-- =====================================================
-- 表 `abroad_case` 出海成功案例表
-- =====================================================
CREATE TABLE `abroad_case`
(
    `id`           BIGINT AUTO_INCREMENT COMMENT '案例唯一标识',
    `title`        VARCHAR(200) COMMENT '案例标题',
    `company_name` VARCHAR(100) COMMENT '企业名称（可为制造企业或服务商）',
    `company_type` ENUM('manufacture','service') COMMENT '企业类型',
    `country`      VARCHAR(50) COMMENT '目标国家',
    `service_type` VARCHAR(100) COMMENT '涉及服务类型（如CE认证、物流）',
    `description`  TEXT COMMENT '案例详情',
    `cover_image`  VARCHAR(255) COMMENT '封面图URL',
    `publish_time` DATETIME COMMENT '发布时间',
    `status`       TINYINT  DEFAULT 0 COMMENT '状态：0草稿 1发布',
    `deleted`      DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_country` (`country`),
    KEY            `idx_publish_time` (`publish_time`),
    KEY            `idx_status` (`status`),
    KEY            `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出海成功案例表';

-- =====================================================
-- 表 `oper_log` 操作日志表
-- =====================================================
CREATE TABLE `oper_log`
(
    `id`          BIGINT AUTO_INCREMENT COMMENT '日志唯一标识',
    `user_id`     BIGINT COMMENT '操作用户ID，关联user.id，可为NULL',
    `username`    VARCHAR(50) COMMENT '操作用户名（冗余，便于查询）',
    `operation`   VARCHAR(200) NOT NULL COMMENT '操作描述（如“用户登录”、“修改密码”）',
    `params`      TEXT COMMENT '请求参数（JSON格式，可选）',
    `result`      VARCHAR(50) COMMENT '操作结果（成功/失败）',
    `ip`          VARCHAR(50) COMMENT '客户端IP地址',
    `deleted`     DATETIME DEFAULT NULL COMMENT '逻辑删除时间，NULL未删除，非NULL已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_user_id` (`user_id`),
    KEY           `idx_operation` (`operation`),
    KEY           `idx_create_time` (`create_time`),
    KEY           `idx_deleted` (`deleted`),
    CONSTRAINT `fk_oper_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';