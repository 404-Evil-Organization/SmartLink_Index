-- =====================================================
-- 智链指数平台测试数据初始化脚本（完整版）
-- 适用数据库：MySQL 8.0+
-- =====================================================

USE
`smartlink_index`;

-- =====================================================
-- 1. 用户表 (user)
-- =====================================================
INSERT INTO `user` (`username`, `password`, `role`, `phone`, `email`, `status`, `deleted`, `create_time`, `update_time`)
VALUES ('tech_company', '$2a$10$X7VYx/h1s2v3c4d5e6f7g8', 'manufacture', '13800138001', 'tech@example.com', 1, NULL,
        NOW(), NOW()),
       ('dg_machinery', '$2a$10$X7VYx/h1s2v3c4d5e6f7g9', 'manufacture', '13900139002', 'dg@example.com', 1, NULL, NOW(),
        NOW()),
       ('huace_test', '$2a$10$X7VYx/h1s2v3c4d5e6f8g0', 'service', '13700137003', 'huace@example.com', 1, NULL, NOW(),
        NOW()),
       ('shenzhen_logistics', '$2a$10$X7VYx/h1s2v3c4d5e6f8g1', 'service', '13600136004', 'szlog@example.com', 1, NULL,
        NOW(), NOW()),
       ('admin_user', '$2a$10$X7VYx/h1s2v3c4d5e6f8g2', 'admin', '18800188005', 'admin@zhilian.com', 1, NULL, NOW(),
        NOW());

-- =====================================================
-- 2. 制造企业表 (manufacture)
-- =====================================================
INSERT INTO `manufacture` (`user_id`, `company_name`, `region`, `address`, `contact_person`, `contact_phone`, `scale`,
                           `employee_count`, `annual_revenue`, `product_type`, `description`, `logo`,
                           `established_date`, `audit_status`, `audit_remark`, `audit_time`, `audit_user_id`, `deleted`,
                           `create_time`, `update_time`)
VALUES (1, '深圳电子科技', '深圳', '深圳市南山区科技园', '张三', '13800138001', 'medium', 500, 8000.00, 'PCB',
        '专业PCB制造商', 'https://mock-oss.example.com/logo1.png', '2010-05-01', 'approved', NULL, NOW(), 5, NULL,
        NOW(), NOW()),
       (2, '东莞精密机械', '东莞', '东莞市长安镇', '李四', '13900139002', 'large', 1200, 15000.00, '精密模具',
        '高端模具制造商', 'https://mock-oss.example.com/logo2.png', '2008-08-08', 'approved', NULL, NOW(), 5, NULL,
        NOW(), NOW());

-- =====================================================
-- 3. 服务商表 (service_provider)
-- =====================================================
INSERT INTO `service_provider` (`user_id`, `company_name`, `region`, `address`, `contact_person`, `contact_phone`,
                                `service_type`, `description`, `logo`, `website`, `established_date`, `employee_count`,
                                `qualification`, `audit_status`, `audit_remark`, `audit_time`, `audit_user_id`,
                                `deleted`, `create_time`, `update_time`)
VALUES (3, '华测检测认证集团', '深圳', '深圳市南山区科技园', '王五', '13700137003', '检测认证',
        'CNAS认可实验室，提供国际认证服务', 'https://mock-oss.example.com/logo3.png', 'www.cti.com', '2003-01-01', 2000,
        'CNAS、CMA', 'approved', NULL, NOW(), 5, NULL, NOW(), NOW()),
       (4, '深圳物流供应链', '深圳', '深圳市宝安区', '赵六', '13600136004', '物流供应链', '提供国内外物流服务',
        'https://mock-oss.example.com/logo4.png', 'www.szlog.com', '2015-03-15', 300, 'ISO9001', 'approved', NULL,
        NOW(), 5, NULL, NOW(), NOW());

-- =====================================================
-- 4. 标签字典表 (tag)
-- =====================================================
INSERT INTO `tag` (`name`, `category`, `description`, `deleted`, `create_time`, `update_time`)
VALUES ('检测认证', 'service', '各类检测认证服务', NULL, NOW(), NOW()),
       ('工业设计', 'service', '产品外观、结构设计', NULL, NOW(), NOW()),
       ('物流供应链', 'service', '物流及供应链管理', NULL, NOW(), NOW()),
       ('CNAS认证', 'certification', '中国合格评定国家认可委员会认证', NULL, NOW(), NOW()),
       ('CMA认证', 'certification', '中国计量认证', NULL, NOW(), NOW()),
       ('ISO9001', 'certification', '质量管理体系认证', NULL, NOW(), NOW()),
       ('PCB电路板', 'product', '印刷电路板', NULL, NOW(), NOW()),
       ('半导体芯片', 'product', '芯片设计制造', NULL, NOW(), NOW()),
       ('消费电子', 'product', '消费类电子产品', NULL, NOW(), NOW()),
       ('精密模具', 'product', '精密模具设计与制造', NULL, NOW(), NOW()), -- 新增，用于东莞精密机械
       ('热门推荐', 'rests', '热门推荐标签', NULL, NOW(), NOW()),
       ('新品上市', 'rests', '新品标签', NULL, NOW(), NOW());

-- 记录新增的精密模具标签ID（用于后续manufacture_tag）
SET
@mold_tag_id = (SELECT id FROM tag WHERE name = '精密模具' AND category = 'product');

-- =====================================================
-- 5. 需求表 (demand)
-- =====================================================
INSERT INTO `demand` (`manu_id`, `title`, `description`, `expected_budget`, `deadline`, `status`, `views`,
                      `audit_status`, `audit_remark`, `audit_time`, `audit_user_id`, `deleted`, `create_time`,
                      `update_time`)
VALUES (1, '寻求PCB设计服务', '需要专业PCB设计公司，有高速PCB设计经验者优先。', 10.00, '2026-04-01', 'published', 0,
        'approved', NULL, NOW(), 5, NULL, NOW(), NOW()),
       (1, '寻找EMS代工厂', '需要SMT贴片服务，月产能需达到10万片', 50.00, '2026-05-01', 'published', 0, 'approved', NULL,
        NOW(), 5, NULL, NOW(), NOW()),
       (2, '精密模具设计', '需要高精度模具设计，用于手机外壳生产', 20.00, '2026-04-15', 'published', 0, 'approved', NULL,
        NOW(), 5, NULL, NOW(), NOW());

-- =====================================================
-- 6. 需求标签关系表 (demand_tag)
-- =====================================================
INSERT INTO `demand_tag` (`demand_id`, `tag_id`, `deleted`, `create_time`, `update_time`)
VALUES (1, 7, NULL, NOW(), NOW()), -- PCB设计关联产品类型标签PCB电路板
       (1, 1, NULL, NOW(), NOW()), -- PCB设计关联检测认证服务
       (2, 8, NULL, NOW(), NOW()), -- EMS代工关联半导体芯片
       (2, 3, NULL, NOW(), NOW()), -- EMS代工关联物流供应链
       (3, 2, NULL, NOW(), NOW());
-- 模具设计关联工业设计

-- =====================================================
-- 7. 制造企业能力标签表 (manufacture_tag)  -- 之前遗漏，现在补充
-- =====================================================
INSERT INTO `manufacture_tag` (`manufacture_id`, `tag_id`, `deleted`, `create_time`, `update_time`)
VALUES (1, 7, NULL, NOW(), NOW()), -- 深圳电子科技有PCB电路板
       (1, 9, NULL, NOW(), NOW()), -- 深圳电子科技有消费电子
       (2, @mold_tag_id, NULL, NOW(), NOW());
-- 东莞精密机械有精密模具

-- =====================================================
-- 8. 服务商能力标签表 (service_tag)
-- =====================================================
INSERT INTO `service_tag` (`service_id`, `tag_id`, `deleted`, `create_time`, `update_time`)
VALUES (1, 1, NULL, NOW(), NOW()), -- 华测检测有检测认证服务
       (1, 4, NULL, NOW(), NOW()), -- 华测检测有CNAS认证
       (1, 5, NULL, NOW(), NOW()), -- 华测检测有CMA认证
       (2, 3, NULL, NOW(), NOW()), -- 深圳物流有物流供应链服务
       (2, 6, NULL, NOW(), NOW());
-- 深圳物流有ISO9001认证

-- =====================================================
-- 9. 合作记录表 (cooperation)
-- =====================================================
INSERT INTO `cooperation` (`manu_id`, `service_id`, `demand_id`, `start_date`, `end_date`, `amount`, `description`,
                           `status`, `deleted`, `create_time`, `update_time`)
VALUES (1, 1, 1, '2026-03-01', '2026-05-01', 10.00, 'PCB设计服务合作', 'ongoing', NULL, NOW(), NOW()),
       (1, 2, 2, '2026-03-10', NULL, 5.00, '物流试运行', 'ongoing', NULL, NOW(), NOW()),
       (2, 1, 3, '2026-03-05', '2026-04-05', 20.00, '模具检测服务', 'completed', NULL, NOW(), NOW());

-- =====================================================
-- 10. 评价表 (evaluation)
-- =====================================================
INSERT INTO `evaluation` (`coop_id`, `evaluator_id`, `evaluator_role`, `score`, `content`, `is_anonymous`, `deleted`,
                          `create_time`, `update_time`)
VALUES (1, 1, 'manufacture', 5, '服务很好，专业高效', 0, NULL, NOW(), NOW()),
       (2, 1, 'manufacture', 4, '物流速度还可以', 0, NULL, NOW(), NOW()),
       (3, 2, 'manufacture', 5, '检测报告很详细', 1, NULL, NOW(), NOW()),
       (1, 3, 'service', 5, '合作愉快，付款及时', 0, NULL, NOW(), NOW());

-- =====================================================
-- 11. 诊断记录表 (diagnosis)
-- =====================================================
INSERT INTO `diagnosis` (`manu_id`, `info_score`, `auto_score`, `data_score`, `service_score`, `total_score`, `level`,
                         `suggestions`, `diagnosis_date`, `deleted`, `create_time`, `update_time`)
VALUES (1, 4, 3, 2, 3, 65, '成熟期', '["建议引入数据分析工具，提升数据应用能力","可考虑将非核心业务外包，聚焦主业"]',
        NOW(), NULL, NOW(), NOW()),
       (2, 5, 4, 3, 4, 80, '引领期', '["继续保持自动化优势","可拓展海外市场"]', NOW(), NULL, NOW(), NOW());

-- =====================================================
-- 12. 区域指数表 (region_index)
-- =====================================================
INSERT INTO `region_index` (`region`, `year`, `period_type`, `period_value`, `coop_density`, `service_rate`,
                            `cross_rate`, `total_index`, `calc_time`, `deleted`, `create_time`, `update_time`)
VALUES ('深圳', 2026, 'quarter', 1, 0.85, 0.72, 0.45, 75.8, NOW(), NULL, NOW(), NOW()),
       ('东莞', 2026, 'quarter', 1, 0.78, 0.68, 0.52, 73.2, NOW(), NULL, NOW(), NOW()),
       ('广州', 2026, 'quarter', 1, 0.82, 0.70, 0.48, 74.5, NOW(), NULL, NOW(), NOW());

-- =====================================================
-- 13. 资质证书表 (certification)
-- =====================================================
INSERT INTO `certification` (`service_id`, `cert_name`, `cert_no`, `issue_authority`, `issue_date`, `expire_date`,
                             `cert_file_url`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (1, 'CNAS认证', 'CNAS L1234', '中国合格评定国家认可委员会', '2023-01-01', '2026-12-31',
        'https://mock-oss.example.com/cert1.pdf', 1, NULL, NOW(), NOW()),
       (1, 'CMA认证', 'CMA 2023-001', '国家认证认可监督管理委员会', '2023-03-01', '2026-02-28',
        'https://mock-oss.example.com/cert2.pdf', 1, NULL, NOW(), NOW()),
       (2, 'ISO9001', 'ISO9001-2025', 'SGS', '2025-01-10', '2028-01-09', 'https://mock-oss.example.com/cert3.pdf', 1,
        NULL, NOW(), NOW());

-- =====================================================
-- 14. 信用分记录表 (credit_score)
-- =====================================================
INSERT INTO `credit_score` (`service_id`, `score`, `qual_score`, `case_score`, `eval_score`, `calc_time`, `deleted`,
                            `create_time`, `update_time`)
VALUES (1, 92, 100, 85, 90, NOW(), NULL, NOW(), NOW()),
       (2, 88, 90, 75, 92, NOW(), NULL, NOW(), NOW());

-- =====================================================
-- 15. 出海成功案例表 (abroad_case)
-- =====================================================
INSERT INTO `abroad_case` (`title`, `company_name`, `company_type`, `country`, `service_type`, `description`,
                           `cover_image`, `publish_time`, `status`, `deleted`, `create_time`, `update_time`)
VALUES ('某电子公司CE认证成功案例', '东莞电子', 'manufacture', '欧盟', 'CE认证',
        '通过华测检测服务，顺利获得CE认证，产品成功进入欧洲市场。', 'https://mock-oss.example.com/case1.jpg',
        '2026-02-10 10:00:00', 1, NULL, NOW(), NOW()),
       ('深圳电子科技FCC认证案例', '深圳电子科技', 'manufacture', '美国', 'FCC认证',
        '通过华测检测协助，快速通过FCC认证，产品出口美国。', 'https://mock-oss.example.com/case2.jpg',
        '2026-02-15 14:30:00', 1, NULL, NOW(), NOW()),
       ('华测检测协助某企业通过日本PSE认证', '华测检测', 'service', '日本', 'PSE认证',
        '华测检测提供一站式PSE认证服务，帮助企业成功进入日本市场。', 'https://mock-oss.example.com/case3.jpg',
        '2026-02-20 09:00:00', 1, NULL, NOW(), NOW());

-- =====================================================
-- 16. 操作日志表 (oper_log)
-- =====================================================
INSERT INTO `oper_log` (`user_id`, `username`, `operation`, `params`, `result`, `ip`, `deleted`, `create_time`,
                        `update_time`)
VALUES (5, 'admin_user', '用户登录', '{}', '成功', '192.168.1.1', NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
       (1, 'tech_company', '发布需求', '{"demandId":1}', '成功', '192.168.1.2', NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR),
        NOW()),
       (3, 'huace_test', '上传资质证书', '{"certId":1}', '成功', '192.168.1.3', NULL,
        DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW()),
       (5, 'admin_user', '审核企业', '{"type":"manufacture","status":"approved"}', '成功', '192.168.1.1', NULL,
        DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
       (2, 'dg_machinery', '提交诊断问卷', '{"diagnosisId":2}', '成功', '192.168.1.4', NULL,
        DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- =====================================================
-- 数据插入完成
-- =====================================================