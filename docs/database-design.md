- ##### 表 `user`（用户表）

  | 字段名      | 类型/约束                                             | 说明                                      |
  | :---------- | :---------------------------------------------------- | :---------------------------------------- |
  | id          | BIGINT PK AUTO_INCREMENT                              | 用户唯一标识                              |
  | username    | VARCHAR(50) NOT NULL                                  | 登录账号                                  |
  | password    | VARCHAR(255) NOT NULL                                 | 加密存储                                  |
  | role        | ENUM('manufacture','service','park','admin') NOT NULL | 角色：制造企业、服务商、园区/政府、管理员 |
  | phone       | VARCHAR(20)                                           | 联系电话                                  |
  | email       | VARCHAR(100)                                          | 电子邮箱                                  |
  | status      | TINYINT DEFAULT 1                                     | 状态：0禁用 1正常                         |
  | deleted     | DATETIME DEFAULT NULL                                 | 逻辑删除时间，NULL未删除，非NULL已删除    |
  | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP                    | 注册时间                                  |
  | update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE          | 更新时间                                  |

  ------

  ##### 表 `manufacture`（制造企业表）

  | 字段名           | 类型/约束                                               | 说明                                    |
  | :--------------- | :------------------------------------------------------ | :-------------------------------------- |
  | id               | BIGINT PK AUTO_INCREMENT                                | 企业唯一标识                            |
  | user_id          | BIGINT NOT NULL                                         | 关联`user.id`                           |
  | company_name     | VARCHAR(100) NOT NULL                                   | 企业全称                                |
  | region           | VARCHAR(50)                                             | 所在区域（深圳/东莞/惠州/广州等）       |
  | address          | VARCHAR(200)                                            | 详细地址                                |
  | contact_person   | VARCHAR(50)                                             | 联系人                                  |
  | contact_phone    | VARCHAR(20)                                             | 联系人电话                              |
  | scale            | ENUM('micro','small','medium','large')                  | 规模：微型、小型、中型、大型            |
  | employee_count   | INT                                                     | 员工人数                                |
  | annual_revenue   | DECIMAL(15,2)                                           | 年营收（万元）                          |
  | product_type     | VARCHAR(100)                                            | 主营产品类型（如PCB、半导体、消费电子） |
  | description      | TEXT                                                    | 企业简介                                |
  | logo             | VARCHAR(255)                                            | Logo图片URL                             |
  | established_date | DATE                                                    | 成立日期                                |
  | audit_status     | ENUM('pending','approved','rejected') DEFAULT 'pending' | 审核状态：待审核、通过、驳回            |
  | audit_remark     | VARCHAR(500)                                            | 审核意见（驳回时填写）                  |
  | audit_time       | DATETIME                                                | 审核时间                                |
  | audit_user_id    | BIGINT                                                  | 审核人ID，关联`user.id`                 |
  | deleted          | DATETIME DEFAULT NULL                                   | 逻辑删除时间，NULL未删除，非NULL已删除  |
  | create_time      | DATETIME DEFAULT CURRENT_TIMESTAMP                      | 记录创建时间                            |
  | update_time      | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE            | 最后更新时间                            |

  ------

  ##### 表 `service_provider`（服务商表）

  | 字段名           | 类型/约束                                               | 说明                                                         |
  | :--------------- | :------------------------------------------------------ | :----------------------------------------------------------- |
  | id               | BIGINT PK AUTO_INCREMENT                                | 服务商唯一标识                                               |
  | user_id          | BIGINT NOT NULL                                         | 关联`user.id`                                                |
  | company_name     | VARCHAR(100) NOT NULL                                   | 企业全称                                                     |
  | region           | VARCHAR(50)                                             | 所在区域                                                     |
  | address          | VARCHAR(200)                                            | 详细地址                                                     |
  | contact_person   | VARCHAR(50)                                             | 联系人                                                       |
  | contact_phone    | VARCHAR(20)                                             | 联系人电话                                                   |
  | service_type     | VARCHAR(200)                                            | 服务大类（如检测认证、工业设计、物流等），可多选，用逗号分隔或JSON |
  | description      | TEXT                                                    | 服务介绍                                                     |
  | logo             | VARCHAR(255)                                            | Logo图片URL                                                  |
  | website          | VARCHAR(100)                                            | 企业官网                                                     |
  | established_date | DATE                                                    | 成立日期                                                     |
  | employee_count   | INT                                                     | 员工人数                                                     |
  | qualification    | TEXT                                                    | 资质概述                                                     |
  | audit_status     | ENUM('pending','approved','rejected') DEFAULT 'pending' | 审核状态：待审核、通过、驳回                                 |
  | audit_remark     | VARCHAR(500)                                            | 审核意见（驳回时填写）                                       |
  | audit_time       | DATETIME                                                | 审核时间                                                     |
  | audit_user_id    | BIGINT                                                  | 审核人ID，关联`user.id`                                      |
  | deleted          | DATETIME DEFAULT NULL                                   | 逻辑删除时间，NULL未删除，非NULL已删除                       |
  | create_time      | DATETIME DEFAULT CURRENT_TIMESTAMP                      | 记录创建时间                                                 |
  | update_time      | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE            | 最后更新时间                                                 |

  ------

  ##### 表 `demand`（需求表）

  | 字段名          | 类型/约束                                                    | 说明                                               |
  | :-------------- | :----------------------------------------------------------- | :------------------------------------------------- |
  | id              | BIGINT PK AUTO_INCREMENT                                     | 需求唯一标识                                       |
  | manu_id         | BIGINT NOT NULL                                              | 关联`manufacture.id`                               |
  | title           | VARCHAR(200) NOT NULL                                        | 需求标题                                           |
  | description     | TEXT                                                         | 详细描述                                           |
  | expected_budget | DECIMAL(12,2)                                                | 预算金额（万元）                                   |
  | deadline        | DATE                                                         | 期望完成日期                                       |
  | status          | ENUM('draft','published','matched','closed') DEFAULT 'draft' | 业务状态：草稿、已发布、已匹配、已关闭（默认草稿） |
  | views           | INT DEFAULT 0                                                | 浏览次数                                           |
  | audit_status    | ENUM('pending','approved','rejected') DEFAULT 'pending'      | 审核状态：待审核、通过、驳回（默认待审核）         |
  | audit_remark    | VARCHAR(500)                                                 | 审核意见（驳回时填写）                             |
  | audit_time      | DATETIME                                                     | 审核时间                                           |
  | audit_user_id   | BIGINT                                                       | 审核人ID，关联`user.id`                            |
  | deleted         | DATETIME DEFAULT NULL                                        | 逻辑删除时间，NULL未删除，非NULL已删除             |
  | create_time     | DATETIME DEFAULT CURRENT_TIMESTAMP                           | 发布时间                                           |
  | update_time     | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE                 | 最后更新时间                                       |

  ------

  ##### 表 `tag`（标签字典表）

  | 字段名      | 类型/约束                                    | 说明                                   |
  | :---------- | :------------------------------------------- | :------------------------------------- |
  | id          | BIGINT PK AUTO_INCREMENT                     | 标签唯一标识                           |
  | name        | VARCHAR(50) NOT NULL                         | 标签名称（如“PCB设计”、“CE认证”）      |
  | category    | VARCHAR(50) NOT NULL DEFAULT 'general'       | 标签类别（如“服务类型”、“认证类型”）   |
  | description | VARCHAR(200)                                 | 标签说明                               |
  | deleted     | DATETIME DEFAULT NULL                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP           | 记录创建时间                           |
  | update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后更新时间                           |

  ------

  ##### 表 `demand_tag`（需求标签关系表）

  | 字段名      | 类型/约束                                    | 说明                                   |
  | :---------- | :------------------------------------------- | :------------------------------------- |
  | id          | BIGINT PK AUTO_INCREMENT                     | 主键                                   |
  | demand_id   | BIGINT NOT NULL                              | 关联`demand.id`                        |
  | tag_id      | BIGINT NOT NULL                              | 关联`tag.id`                           |
  | deleted     | DATETIME DEFAULT NULL                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP           | 记录创建时间                           |
  | update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后更新时间                           |

  ------

  ##### 表 `service_tag`（服务商能力标签表）

  | 字段名      | 类型/约束                                    | 说明                                   |
  | :---------- | :------------------------------------------- | :------------------------------------- |
  | id          | BIGINT PK AUTO_INCREMENT                     | 主键                                   |
  | service_id  | BIGINT NOT NULL                              | 关联`service_provider.id`              |
  | tag_id      | BIGINT NOT NULL                              | 关联`tag.id`                           |
  | deleted     | DATETIME DEFAULT NULL                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP           | 记录创建时间                           |
  | update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后更新时间                           |

  ------

  ##### 表 `cooperation`（合作记录表）

  | 字段名      | 类型/约束                                                 | 说明                                   |
  | :---------- | :-------------------------------------------------------- | :------------------------------------- |
  | id          | BIGINT PK AUTO_INCREMENT                                  | 合作唯一标识                           |
  | manu_id     | BIGINT NOT NULL                                           | 关联`manufacture.id`                   |
  | service_id  | BIGINT NOT NULL                                           | 关联`service_provider.id`              |
  | demand_id   | BIGINT                                                    | 关联`demand.id`（可选）                |
  | start_date  | DATE                                                      | 合作开始日期                           |
  | end_date    | DATE                                                      | 合作结束日期                           |
  | amount      | DECIMAL(12,2)                                             | 合同金额（万元）                       |
  | description | VARCHAR(500)                                              | 合作内容简述                           |
  | status      | ENUM('ongoing','completed','cancelled') DEFAULT 'ongoing' | 合作状态                               |
  | deleted     | DATETIME DEFAULT NULL                                     | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP                        | 记录创建时间                           |
  | update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE              | 最后更新时间                           |

  ------

  ##### 表 `evaluation`（评价表）

  | 字段名         | 类型/约束                                    | 说明                                   |
  | :------------- | :------------------------------------------- | :------------------------------------- |
  | id             | BIGINT PK AUTO_INCREMENT                     | 评价唯一标识                           |
  | coop_id        | BIGINT NOT NULL                              | 关联`cooperation.id`                   |
  | evaluator_id   | BIGINT NOT NULL                              | 评价人 user.id                         |
  | evaluator_role | ENUM('manufacture','service') NOT NULL       | 评价人角色（制造企业/服务商）          |
  | score          | TINYINT NOT NULL                             | 评分（1-5星）                          |
  | content        | VARCHAR(500)                                 | 评价内容                               |
  | is_anonymous   | TINYINT DEFAULT 0                            | 是否匿名（0否 1是）                    |
  | deleted        | DATETIME DEFAULT NULL                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time    | DATETIME DEFAULT CURRENT_TIMESTAMP           | 评价时间                               |
  | update_time    | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后更新时间                           |
  | **CHECK**      | CHECK (`score` BETWEEN 1 AND 5)              |                                        |

  ------

  ##### 表 `diagnosis`（诊断记录表）

  | 字段名         | 类型/约束                                                    | 说明                                   |
  | :------------- | :----------------------------------------------------------- | :------------------------------------- |
  | id             | BIGINT PK AUTO_INCREMENT                                     | 诊断唯一标识                           |
  | manu_id        | BIGINT NOT NULL                                              | 关联`manufacture.id`                   |
  | info_score     | TINYINT                                                      | 信息化得分（1-5）                      |
  | auto_score     | TINYINT                                                      | 自动化得分（1-5）                      |
  | data_score     | TINYINT                                                      | 数据应用得分（1-5）                    |
  | service_score  | TINYINT                                                      | 服务协同得分（1-5）                    |
  | total_score    | TINYINT                                                      | 总分（0-100）                          |
  | level          | VARCHAR(20)                                                  | 等级（起步期/成长期/成熟期/引领期）    |
  | suggestions    | TEXT                                                         | 改进建议（可JSON存储多条）             |
  | diagnosis_date | DATETIME                                                     | 诊断日期                               |
  | deleted        | DATETIME DEFAULT NULL                                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time    | DATETIME DEFAULT CURRENT_TIMESTAMP                           | 记录创建时间                           |
  | update_time    | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE                 | 最后更新时间                           |
  | **CHECK**      | CHECK (`info_score` BETWEEN 1 AND 5), CHECK (`auto_score` BETWEEN 1 AND 5), CHECK (`data_score` BETWEEN 1 AND 5), CHECK (`service_score` BETWEEN 1 AND 5), CHECK (`total_score` BETWEEN 0 AND 100) |                                        |

  ------

  ##### 表 `region_index`（区域指数表）

  | 字段名       | 类型/约束                                                    | 说明                                      |
  | :----------- | :----------------------------------------------------------- | :---------------------------------------- |
  | id           | BIGINT PK AUTO_INCREMENT                                     | 记录唯一标识                              |
  | region       | VARCHAR(50) NOT NULL                                         | 区域名称（深圳/东莞/惠州/广州等）         |
  | year         | SMALLINT NOT NULL                                            | 年份                                      |
  | period_type  | ENUM('quarter','month') NOT NULL                             | 统计周期类型：quarter季度、month月度      |
  | period_value | TINYINT NOT NULL                                             | 周期值：季度1-4，月份1-12                 |
  | coop_density | DECIMAL(8,4)                                                 | 合作密度（合作次数/企业总数）             |
  | service_rate | DECIMAL(5,4)                                                 | 服务渗透率（使用服务企业数/制造企业总数） |
  | cross_rate   | DECIMAL(5,4)                                                 | 跨域协同度（跨区域合作次数/总合作次数）   |
  | total_index  | DECIMAL(6,2)                                                 | 协同指数综合得分                          |
  | calc_time    | DATETIME                                                     | 计算时间                                  |
  | deleted      | DATETIME DEFAULT NULL                                        | 逻辑删除时间，NULL未删除，非NULL已删除    |
  | create_time  | DATETIME DEFAULT CURRENT_TIMESTAMP                           | 记录创建时间                              |
  | update_time  | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE                 | 最后更新时间                              |
  | **CHECK**    | CHECK ((`period_type`='quarter' AND `period_value` BETWEEN 1 AND 4) OR (`period_type`='month' AND `period_value` BETWEEN 1 AND 12)) |                                           |

  ------

  ##### 表 `certification`（资质证书表）

  | 字段名          | 类型/约束                                    | 说明                                   |
  | :-------------- | :------------------------------------------- | :------------------------------------- |
  | id              | BIGINT PK AUTO_INCREMENT                     | 证书唯一标识                           |
  | service_id      | BIGINT NOT NULL                              | 关联`service_provider.id`              |
  | cert_name       | VARCHAR(100) NOT NULL                        | 证书名称（如CNAS、CMA）                |
  | cert_no         | VARCHAR(50)                                  | 证书编号                               |
  | issue_authority | VARCHAR(100)                                 | 发证机构                               |
  | issue_date      | DATE                                         | 发证日期                               |
  | expire_date     | DATE                                         | 有效期至                               |
  | cert_file_url   | VARCHAR(255)                                 | 证书文件路径                           |
  | status          | TINYINT DEFAULT 1                            | 状态：0失效 1有效                      |
  | deleted         | DATETIME DEFAULT NULL                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time     | DATETIME DEFAULT CURRENT_TIMESTAMP           | 上传时间                               |
  | update_time     | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后更新时间                           |

  ------

  ##### 表 `credit_score`（信用分记录表）

  | 字段名      | 类型/约束                                                    | 说明                                   |
  | :---------- | :----------------------------------------------------------- | :------------------------------------- |
  | id          | BIGINT PK AUTO_INCREMENT                                     | 记录唯一标识                           |
  | service_id  | BIGINT NOT NULL                                              | 关联`service_provider.id`              |
  | score       | TINYINT                                                      | 综合信用分（0-100）                    |
  | qual_score  | TINYINT                                                      | 资质分                                 |
  | case_score  | TINYINT                                                      | 案例分                                 |
  | eval_score  | TINYINT                                                      | 评价分                                 |
  | calc_time   | DATETIME                                                     | 计算时间                               |
  | deleted     | DATETIME DEFAULT NULL                                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP                           | 记录创建时间                           |
  | update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE                 | 最后更新时间                           |
  | **CHECK**   | CHECK (`score` BETWEEN 0 AND 100), CHECK (`qual_score` BETWEEN 0 AND 100), CHECK (`case_score` BETWEEN 0 AND 100), CHECK (`eval_score` BETWEEN 0 AND 100) |                                        |

  ------

  ##### 表 `abroad_case`（出海成功案例表）

  | 字段名       | 类型/约束                                    | 说明                                   |
  | :----------- | :------------------------------------------- | :------------------------------------- |
  | id           | BIGINT PK AUTO_INCREMENT                     | 案例唯一标识                           |
  | title        | VARCHAR(200)                                 | 案例标题                               |
  | company_name | VARCHAR(100)                                 | 企业名称（可为制造企业或服务商）       |
  | company_type | ENUM('manufacture','service')                | 企业类型                               |
  | country      | VARCHAR(50)                                  | 目标国家                               |
  | service_type | VARCHAR(100)                                 | 涉及服务类型（如CE认证、物流）         |
  | description  | TEXT                                         | 案例详情                               |
  | cover_image  | VARCHAR(255)                                 | 封面图URL                              |
  | publish_time | DATETIME                                     | 发布时间                               |
  | status       | TINYINT DEFAULT 0                            | 状态：0草稿 1发布                      |
  | deleted      | DATETIME DEFAULT NULL                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time  | DATETIME DEFAULT CURRENT_TIMESTAMP           | 记录创建时间                           |
  | update_time  | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后更新时间                           |

  ------

  ##### 表 `oper_log`（操作日志表）

  | 字段名      | 类型/约束                                    | 说明                                   |
  | :---------- | :------------------------------------------- | :------------------------------------- |
  | id          | BIGINT PK AUTO_INCREMENT                     | 日志唯一标识                           |
  | user_id     | BIGINT                                       | 操作用户ID，关联`user.id`，可为NULL    |
  | username    | VARCHAR(50)                                  | 操作用户名（冗余，便于查询）           |
  | operation   | VARCHAR(200) NOT NULL                        | 操作描述（如“用户登录”、“修改密码”）   |
  | params      | TEXT                                         | 请求参数（JSON格式，可选）             |
  | result      | VARCHAR(50)                                  | 操作结果（成功/失败）                  |
  | ip          | VARCHAR(50)                                  | 客户端IP地址                           |
  | deleted     | DATETIME DEFAULT NULL                        | 逻辑删除时间，NULL未删除，非NULL已删除 |
  | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP           | 操作时间                               |
  | update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后更新时间                           |

  ------

  #### 4.2 索引建议

  - 所有主键字段自动建立聚簇索引。
  - 外键字段（如 `manufacture.user_id`、`demand.manu_id`、`cooperation.manu_id`、`cooperation.service_id`、`evaluation.coop_id` 等）建议建立索引以加速连接查询。
  - 频繁查询的字段：`user.username`（建议建立唯一索引且包含 `deleted`）、`manufacture.region`、`service_provider.region`、`demand.status`、`demand.create_time`、`region_index.region`、`region_index.year`/`period` 等，可酌情添加索引。
  - 逻辑删除字段 `deleted` 应建立索引（`idx_deleted`），并在复合唯一索引中参与，以确保软删除后业务数据的唯一性。具体唯一索引如下：
    - `user`：`uk_username_deleted` (`username`, `deleted`)
    - `manufacture`：`uk_user_id_deleted` (`user_id`, `deleted`)
    - `service_provider`：`uk_user_id_deleted` (`user_id`, `deleted`)
    - `tag`：`uk_name_category_deleted` (`name`, `category`, `deleted`)
    - `demand_tag`：`uk_demand_tag_deleted` (`demand_id`, `tag_id`, `deleted`)
    - `service_tag`：`uk_service_tag_deleted` (`service_id`, `tag_id`, `deleted`)
    - `region_index`：`uk_region_year_period_deleted` (`region`, `year`, `period_type`, `period_value`, `deleted`)
    - `evaluation`：`uk_coop_evaluator_role_deleted` (`coop_id`, `evaluator_role`, `deleted`)
  - 多对多关系的中间表（`demand_tag`、`service_tag`）使用复合唯一索引（含 `deleted`）保证数据唯一。

  #### 4.3 ER图关键关系

  ```text
  user ──┬── manufacture (1:1)
         ├── service_provider (1:1)
  
  manufacture ── demand (1:n)
  manufacture ── diagnosis (1:n)
  manufacture ── cooperation (1:n) ── service_provider (1:n)
  cooperation ── evaluation (1:n)       // 一次合作可有多条评价（不同角色）
  evaluation ── user (n:1)              // 评价人
  
  service_provider ── certification (1:n)
  service_provider ── credit_score (1:n)
  
  demand ── demand_tag (n:n) ── tag (1:n)
  service_provider ── service_tag (n:n) ── tag (1:n)
  ```

  #### 4.4 关键业务数据流说明

  - **数字化诊断**：制造企业提交问卷后生成 `diagnosis` 记录，存储各维度得分及总分等级。
  - **供需匹配**：需求发布时关联 `demand_tag`，匹配算法通过 `service_tag` 计算标签相似度，结合 `cooperation` 统计的行业经验分和 `credit_score` 信用分，生成推荐列表。
  - **信用评价**：服务商信用分基于 `certification` 数量、`cooperation` 成功案例数以及 `evaluation` 平均分，定期存入 `credit_score` 表。
  - **区域指数**：通过定时任务聚合 `cooperation`、`manufacture`、`service_provider` 数据，按区域和周期粒度计算并写入 `region_index`。
  - **可视化看板**：聚合查询各表获得统计卡片、热力图、协同网络图等数据。
  - **逻辑删除**：所有表均采用软删除机制，通过 `deleted` 字段标记删除时间，查询时默认过滤 `deleted IS NULL` 的数据。