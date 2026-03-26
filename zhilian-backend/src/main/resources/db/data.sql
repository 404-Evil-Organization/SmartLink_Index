-- =====================================================
-- 智链指数平台测试数据初始化脚本（完整版）
-- 适用数据库：MySQL 8.0+
-- =====================================================

USE `smartlink_index`;

-- =====================================================
-- 1. 用户表 (user)
-- =====================================================
INSERT INTO `user` (`username`, `password`, `role`, `phone`, `email`, `status`, `deleted`, `create_time`, `update_time`)
VALUES ('tech_company', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'manufacture', '13800138001', 'tech@example.com', 1, '1970-01-01 00:00:00',
        NOW(), NOW()),
       ('dg_machinery', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'manufacture', '13900139002', 'dg@example.com', 1, '1970-01-01 00:00:00', NOW(),
        NOW()),
       ('huace_test', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'service', '13700137003', 'huace@example.com', 1, '1970-01-01 00:00:00', NOW(),
        NOW()),
       ('shenzhen_logistics', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'service', '13600136004', 'szlog@example.com', 1, '1970-01-01 00:00:00',
        NOW(), NOW()),
       ('admin_user', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'admin', '18800188005', 'admin@zhilian.com', 1, '1970-01-01 00:00:00', NOW(),
        NOW()),
       -- 新增制造企业用户
       ('guangzhou_auto', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'manufacture', '13500135006', 'guangzhou_auto@example.com', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       ('foshan_elec', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'manufacture', '13400134007', 'foshan_elec@example.com', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       ('zhuhai_med', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'manufacture', '13300133008', 'zhuhai_med@example.com', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增服务商用户
       ('sgs_cert', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'service', '13200132009', 'sgs@example.com', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       ('design_studio', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'service', '13100131010', 'design@example.com', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       ('logistics_co', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'service', '13000130011', 'logistics@example.com', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增园区用户
       ('shenzhen_park', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'park', '12900129012', 'park@sz.gov.cn', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       ('dongguan_park', '$2a$10$rkkoqXCdFDjjl8lc4LPdfO0MkYakJg27pFkU/I0hEiN.Rvr3FMbSu', 'park', '12800128013', 'park@dg.gov.cn', 1, '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 2. 制造企业表 (manufacture)
-- =====================================================
INSERT INTO `manufacture` (`user_id`, `company_name`, `region`, `address`, `contact_person`, `contact_phone`, `scale`,
                           `employee_count`, `annual_revenue`, `product_type`, `description`, `logo`,
                           `established_date`, `audit_status`, `audit_remark`, `audit_time`, `audit_user_id`, `deleted`,
                           `create_time`, `update_time`)
VALUES (1, '深圳电子科技', '深圳', '深圳市南山区科技园', '张三', '13800138001', 'medium', 500, 8000.00, 'PCB',
        '专业PCB制造商', 'https://mock-oss.example.com/logo1.png', '2010-05-01', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00',
        NOW(), NOW()),
       (2, '东莞精密机械', '东莞', '东莞市长安镇', '李四', '13900139002', 'large', 1200, 15000.00, '精密模具',
        '高端模具制造商', 'https://mock-oss.example.com/logo2.png', '2008-08-08', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00',
        NOW(), NOW()),
       -- 新增制造企业 (user_id 6, 7, 8 对应用户表中的制造企业用户)
       (6, '广州汽车零部件', '广州', '广州市黄埔区', '王明', '13500135006', 'large', 800, 12000.00, '汽车零部件',
        '专业汽车零部件制造商', 'https://mock-oss.example.com/logo5.png', '2012-03-15', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00',
        NOW(), NOW()),
       (7, '佛山电器制造', '佛山', '佛山市顺德区', '陈华', '13400134007', 'medium', 300, 5000.00, '家用电器',
        '家电产品制造商', 'https://mock-oss.example.com/logo6.png', '2015-08-20', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00',
        NOW(), NOW()),
       (8, '珠海医疗器械', '珠海', '珠海市香洲区', '刘强', '13300133008', 'small', 150, 2000.00, '医疗器械',
        '医疗设备研发制造', 'https://mock-oss.example.com/logo7.png', '2018-11-10', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00',
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
        'CNAS、CMA', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       (4, '深圳物流供应链', '深圳', '深圳市宝安区', '赵六', '13600136004', '物流供应链', '提供国内外物流服务',
        'https://mock-oss.example.com/logo4.png', 'www.szlog.com', '2015-03-15', 300, 'ISO9001', 'approved', NULL,
        NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增服务商
       (9, 'SGS通标标准技术服务', '广州', '广州市天河区', '张伟', '13200132009', '检测认证,咨询服务',
        '全球领先的检验、鉴定、测试和认证机构', 'https://mock-oss.example.com/logo8.png', 'www.sgs.com', '1991-05-10', 1500,
        'CNAS、CMA、ISO17025', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       (10, '创新设计工作室', '深圳', '深圳市福田区', '李娜', '13100131010', '工业设计,产品设计',
        '专业产品外观与结构设计服务', 'https://mock-oss.example.com/logo9.png', 'www.designstudio.com', '2017-09-01', 50,
        '红点设计奖、IF设计奖', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       (11, '顺丰速运有限公司', '深圳', '深圳市南山区', '陈明', '13000130011', '物流供应链,仓储服务',
        '国内领先的快递物流综合服务商', 'https://mock-oss.example.com/logo10.png', 'www.sf-express.com', '1993-03-26', 40000,
        'ISO9001、ISO14001', 'approved', NULL, NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 4. 标签字典表 (tag)
-- =====================================================
INSERT INTO `tag` (`name`, `category`, `description`, `deleted`, `create_time`, `update_time`)
VALUES ('检测认证', 'service', '各类检测认证服务', '1970-01-01 00:00:00', NOW(), NOW()),
       ('工业设计', 'service', '产品外观、结构设计', '1970-01-01 00:00:00', NOW(), NOW()),
       ('物流供应链', 'service', '物流及供应链管理', '1970-01-01 00:00:00', NOW(), NOW()),
       ('CNAS认证', 'certification', '中国合格评定国家认可委员会认证', '1970-01-01 00:00:00', NOW(), NOW()),
       ('CMA认证', 'certification', '中国计量认证', '1970-01-01 00:00:00', NOW(), NOW()),
       ('ISO9001', 'certification', '质量管理体系认证', '1970-01-01 00:00:00', NOW(), NOW()),
       ('PCB电路板', 'product', '印刷电路板', '1970-01-01 00:00:00', NOW(), NOW()),
       ('半导体芯片', 'product', '芯片设计制造', '1970-01-01 00:00:00', NOW(), NOW()),
       ('消费电子', 'product', '消费类电子产品', '1970-01-01 00:00:00', NOW(), NOW()),
       ('精密模具', 'product', '精密模具设计与制造', '1970-01-01 00:00:00', NOW(), NOW()),
       ('热门推荐', 'general', '热门推荐标签', '1970-01-01 00:00:00', NOW(), NOW()),
       ('新品上市', 'general', '新品标签', '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增标签
       ('CE认证', 'certification', '欧盟CE认证', '1970-01-01 00:00:00', NOW(), NOW()),
       ('FCC认证', 'certification', '美国FCC认证', '1970-01-01 00:00:00', NOW(), NOW()),
       ('RoHS认证', 'certification', '有害物质限制认证', '1970-01-01 00:00:00', NOW(), NOW()),
       ('汽车零部件', 'product', '汽车相关零部件', '1970-01-01 00:00:00', NOW(), NOW()),
       ('医疗器械', 'product', '医疗设备及器械', '1970-01-01 00:00:00', NOW(), NOW()),
       ('家用电器', 'product', '家用电器产品', '1970-01-01 00:00:00', NOW(), NOW()),
       ('咨询服务', 'service', '专业技术咨询服务', '1970-01-01 00:00:00', NOW(), NOW()),
       ('仓储服务', 'service', '仓储物流服务', '1970-01-01 00:00:00', NOW(), NOW()),
       ('软件开发', 'service', '软件定制开发服务', '1970-01-01 00:00:00', NOW(), NOW()),
       ('质量检测', 'service', '产品质量检测服务', '1970-01-01 00:00:00', NOW(), NOW());

-- 记录新增的精密模具标签ID（用于后续manufacture_tag）
SET @mold_tag_id = (SELECT id FROM tag WHERE name = '精密模具' AND category = 'product');

-- =====================================================
-- 5. 需求表 (demand)
-- =====================================================
INSERT INTO `demand` (`manu_id`, `title`, `description`, `expected_budget`, `deadline`, `status`, `views`,
                      `audit_status`, `audit_remark`, `audit_time`, `audit_user_id`, `deleted`, `create_time`,
                      `update_time`)
VALUES (1, '寻求PCB设计服务', '需要专业PCB设计公司，有高速PCB设计经验者优先。', 10.00, '2026-04-01', 'published', 0,
        'approved', NULL, NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       (1, '寻找EMS代工厂', '需要SMT贴片服务，月产能需达到10万片', 50.00, '2026-05-01', 'published', 0, 'approved', NULL,
        NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       (2, '精密模具设计', '需要高精度模具设计，用于手机外壳生产', 20.00, '2026-04-15', 'published', 0, 'approved', NULL,
        NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增需求 (manu_id引用制造企业表的ID: 3, 4, 5)
       (3, '汽车零部件CE认证', '需要CE认证服务，产品出口欧盟市场', 15.00, '2026-06-01', 'published', 0, 'approved', NULL,
        NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       (4, '医疗器械质量检测', '需要专业的医疗器械质量检测服务', 25.00, '2026-05-20', 'published', 0, 'approved', NULL,
        NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW()),
       (5, '家电产品FCC认证', '需要FCC认证，产品出口美国市场', 12.00, '2026-07-01', 'published', 0, 'approved', NULL,
        NOW(), 5, '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 6. 需求标签关系表 (demand_tag)
-- =====================================================
INSERT INTO `demand_tag` (`demand_id`, `tag_id`, `deleted`, `create_time`, `update_time`)
VALUES (1, 7, '1970-01-01 00:00:00', NOW(), NOW()),
       (1, 1, '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 8, '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 3, '1970-01-01 00:00:00', NOW(), NOW()),
       (3, 2, '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增需求标签关系 (demand_id引用需求表的ID: 4, 5, 6)
       (4, 13, '1970-01-01 00:00:00', NOW(), NOW()), -- CE认证
       (4, 1, '1970-01-01 00:00:00', NOW(), NOW()), -- 检测认证
       (5, 22, '1970-01-01 00:00:00', NOW(), NOW()), -- 质量检测
       (5, 17, '1970-01-01 00:00:00', NOW(), NOW()), -- 医疗器械
       (6, 14, '1970-01-01 00:00:00', NOW(), NOW()), -- FCC认证
       (6, 18, '1970-01-01 00:00:00', NOW(), NOW()); -- 家用电器

-- =====================================================
-- 7. 制造企业能力标签表 (manufacture_tag)
-- =====================================================
INSERT INTO `manufacture_tag` (`manufacture_id`, `tag_id`, `deleted`, `create_time`, `update_time`)
VALUES (1, 7, '1970-01-01 00:00:00', NOW(), NOW()),
       (1, 9, '1970-01-01 00:00:00', NOW(), NOW()),
       (2, @mold_tag_id, '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增制造企业能力标签 (manufacture_id引用制造企业表的ID: 3, 4, 5)
       (3, 16, '1970-01-01 00:00:00', NOW(), NOW()), -- 汽车零部件
       (4, 17, '1970-01-01 00:00:00', NOW(), NOW()), -- 医疗器械
       (5, 18, '1970-01-01 00:00:00', NOW(), NOW()); -- 家用电器

-- =====================================================
-- 8. 服务商能力标签表 (service_tag)
-- =====================================================
INSERT INTO `service_tag` (`service_id`, `tag_id`, `deleted`, `create_time`, `update_time`)
VALUES (1, 1, '1970-01-01 00:00:00', NOW(), NOW()),
       (1, 4, '1970-01-01 00:00:00', NOW(), NOW()),
       (1, 5, '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 3, '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 6, '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增服务商能力标签
       (3, 1, '1970-01-01 00:00:00', NOW(), NOW()), -- 检测认证
       (3, 13, '1970-01-01 00:00:00', NOW(), NOW()), -- CE认证
       (3, 14, '1970-01-01 00:00:00', NOW(), NOW()), -- FCC认证
       (4, 2, '1970-01-01 00:00:00', NOW(), NOW()), -- 工业设计
       (4, 19, '1970-01-01 00:00:00', NOW(), NOW()), -- 咨询服务
       (5, 3, '1970-01-01 00:00:00', NOW(), NOW()), -- 物流供应链
       (5, 20, '1970-01-01 00:00:00', NOW(), NOW()); -- 仓储服务

-- =====================================================
-- 9. 合作记录表 (cooperation)
-- =====================================================
INSERT INTO `cooperation` (`manu_id`, `service_id`, `demand_id`, `start_date`, `end_date`, `amount`, `description`,
                           `status`, `deleted`, `create_time`, `update_time`)
VALUES (1, 1, 1, '2026-03-01', '2026-05-01', 10.00, 'PCB设计服务合作', 'ongoing', '1970-01-01 00:00:00', NOW(), NOW()),
       (1, 2, 2, '2026-03-10', NULL, 5.00, '物流试运行', 'ongoing', '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 1, 3, '2026-03-05', '2026-04-05', 20.00, '模具检测服务', 'completed', '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 10. 评价表 (evaluation)
-- =====================================================
INSERT INTO `evaluation` (`coop_id`, `evaluator_id`, `evaluator_role`, `score`, `content`, `is_anonymous`, `deleted`,
                          `create_time`, `update_time`)
VALUES (1, 1, 'manufacture', 5, '服务很好，专业高效', 0, '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 1, 'manufacture', 4, '物流速度还可以', 0, '1970-01-01 00:00:00', NOW(), NOW()),
       (3, 2, 'manufacture', 5, '检测报告很详细', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       (1, 3, 'service', 5, '合作愉快，付款及时', 0, '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 11. 诊断记录表 (diagnosis)
-- =====================================================
INSERT INTO `diagnosis` (`manu_id`, `info_score`, `auto_score`, `data_score`, `service_score`, `total_score`, `level`,
                         `suggestions`, `diagnosis_date`, `deleted`, `create_time`, `update_time`)
VALUES (1, 4, 3, 2, 3, 65, '成熟期', '["建议引入数据分析工具，提升数据应用能力","可考虑将非核心业务外包，聚焦主业"]',
        NOW(), '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 5, 4, 3, 4, 80, '引领期', '["继续保持自动化优势","可拓展海外市场"]', NOW(), '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 12. 区域指数表 (region_index)
-- =====================================================
INSERT INTO `region_index` (`region`, `year`, `period_type`, `period_value`, `coop_density`, `service_rate`,
                            `cross_rate`, `total_index`, `calc_time`, `deleted`, `create_time`, `update_time`)
VALUES ('深圳', 2026, 'quarter', 1, 0.85, 0.72, 0.45, 75.8, NOW(), '1970-01-01 00:00:00', NOW(), NOW()),
       ('东莞', 2026, 'quarter', 1, 0.78, 0.68, 0.52, 73.2, NOW(), '1970-01-01 00:00:00', NOW(), NOW()),
       ('广州', 2026, 'quarter', 1, 0.82, 0.70, 0.48, 74.5, NOW(), '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增季度数据
       ('深圳', 2025, 'quarter', 4, 0.83, 0.70, 0.43, 74.2, DATE_SUB(NOW(), INTERVAL 3 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       ('深圳', 2025, 'quarter', 3, 0.81, 0.68, 0.41, 72.8, DATE_SUB(NOW(), INTERVAL 6 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       ('深圳', 2025, 'quarter', 2, 0.79, 0.65, 0.39, 70.5, DATE_SUB(NOW(), INTERVAL 9 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       ('东莞', 2025, 'quarter', 4, 0.76, 0.66, 0.50, 72.0, DATE_SUB(NOW(), INTERVAL 3 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       ('广州', 2025, 'quarter', 4, 0.80, 0.68, 0.46, 73.2, DATE_SUB(NOW(), INTERVAL 3 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增月份数据
       ('深圳', 2026, 'month', 3, 0.84, 0.71, 0.44, 75.2, DATE_SUB(NOW(), INTERVAL 1 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       ('深圳', 2026, 'month', 2, 0.82, 0.69, 0.42, 73.8, DATE_SUB(NOW(), INTERVAL 2 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       ('深圳', 2026, 'month', 1, 0.80, 0.67, 0.40, 72.0, DATE_SUB(NOW(), INTERVAL 3 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       ('东莞', 2026, 'month', 3, 0.77, 0.67, 0.51, 72.8, DATE_SUB(NOW(), INTERVAL 1 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       ('广州', 2026, 'month', 3, 0.81, 0.69, 0.47, 74.0, DATE_SUB(NOW(), INTERVAL 1 MONTH), '1970-01-01 00:00:00', NOW(), NOW()),
       -- 新增其他区域数据
       ('佛山', 2026, 'quarter', 1, 0.75, 0.65, 0.45, 70.5, NOW(), '1970-01-01 00:00:00', NOW(), NOW()),
       ('惠州', 2026, 'quarter', 1, 0.72, 0.62, 0.42, 68.8, NOW(), '1970-01-01 00:00:00', NOW(), NOW()),
       ('珠海', 2026, 'quarter', 1, 0.70, 0.60, 0.40, 67.2, NOW(), '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 13. 资质证书表 (certification)
-- =====================================================
INSERT INTO `certification` (`service_id`, `cert_name`, `cert_no`, `issue_authority`, `issue_date`, `expire_date`,
                             `cert_file_url`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (1, 'CNAS认证', 'CNAS L1234', '中国合格评定国家认可委员会', '2023-01-01', '2026-12-31',
        'https://mock-oss.example.com/cert1.pdf', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       (1, 'CMA认证', 'CMA 2023-001', '国家认证认可监督管理委员会', '2023-03-01', '2026-02-28',
        'https://mock-oss.example.com/cert2.pdf', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 'ISO9001', 'ISO9001-2025', 'SGS', '2025-01-10', '2028-01-09', 'https://mock-oss.example.com/cert3.pdf', 1,
        '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 14. 信用分记录表 (credit_score)
-- =====================================================
INSERT INTO `credit_score` (`service_id`, `score`, `qual_score`, `case_score`, `eval_score`, `calc_time`, `deleted`,
                            `create_time`, `update_time`)
VALUES (1, 92, 100, 85, 90, NOW(), '1970-01-01 00:00:00', NOW(), NOW()),
       (2, 88, 90, 75, 92, NOW(), '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 15. 出海成功案例表 (abroad_case)
-- =====================================================
INSERT INTO `abroad_case` (`title`, `company_name`, `company_type`, `country`, `service_type`, `description`,
                           `cover_image`, `publish_time`, `status`, `deleted`, `create_time`, `update_time`)
VALUES ('某电子公司CE认证成功案例', '东莞电子', 'manufacture', '欧盟', 'CE认证',
        '通过华测检测服务，顺利获得CE认证，产品成功进入欧洲市场。', 'https://mock-oss.example.com/case1.jpg',
        '2026-02-10 10:00:00', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       ('深圳电子科技FCC认证案例', '深圳电子科技', 'manufacture', '美国', 'FCC认证',
        '通过华测检测协助，快速通过FCC认证，产品出口美国。', 'https://mock-oss.example.com/case2.jpg',
        '2026-02-15 14:30:00', 1, '1970-01-01 00:00:00', NOW(), NOW()),
       ('华测检测协助某企业通过日本PSE认证', '华测检测', 'service', '日本', 'PSE认证',
        '华测检测提供一站式PSE认证服务，帮助企业成功进入日本市场。', 'https://mock-oss.example.com/case3.jpg',
        '2026-02-20 09:00:00', 1, '1970-01-01 00:00:00', NOW(), NOW());

-- =====================================================
-- 16. 操作日志表 (oper_log)
-- =====================================================
INSERT INTO `oper_log` (`user_id`, `username`, `operation`, `params`, `result`, `ip`, `deleted`, `create_time`,
                        `update_time`)
VALUES (5, 'admin_user', '用户登录', '{}', '成功', '192.168.1.1', '1970-01-01 00:00:00', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
       (1, 'tech_company', '发布需求', '{"demandId":1}', '成功', '192.168.1.2', '1970-01-01 00:00:00', DATE_SUB(NOW(), INTERVAL 1 HOUR),
        NOW()),
       (3, 'huace_test', '上传资质证书', '{"certId":1}', '成功', '192.168.1.3', '1970-01-01 00:00:00',
        DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW()),
       (5, 'admin_user', '审核企业', '{"type":"manufacture","status":"approved"}', '成功', '192.168.1.1', '1970-01-01 00:00:00',
        DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
       (2, 'dg_machinery', '提交诊断问卷', '{"diagnosisId":2}', '成功', '192.168.1.4', '1970-01-01 00:00:00',
        DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- =====================================================
-- 数据插入完成
-- =====================================================