# 智链指数平台开发规范文档

> **文档版本**：1.0
> **最后更新**：2026年3月3日
> **项目名称**：智链指数——大湾区电子信息产业两业融合智能评估与协同服务平台

------

## 目录

- 1. 引言

- 2. 技术架构总览

- 3. 后端开发规范

  - 3.1 技术栈
  - 3.2 项目结构
  - 3.3 编码规范
  - 3.4 异常处理
  - 3.5 日志规范
  - 3.6 安全规范
  - 3.7 单元测试

- 4. 前端开发规范

  - 4.1 技术栈
  - 4.2 目录结构
  - 4.3 编码规范
  - 4.4 状态管理
  - 4.5 与后端交互

- 5. 数据库设计规范

  - 5.1 命名规范
  - 5.2 表设计规范
  - 5.3 索引规范
  - 5.4 核心表结构

- 6. 接口设计规范

  - 6.1 RESTful 风格
  - 6.2 统一响应格式
  - 6.3 参数校验
  - 6.4 分页规范
  - 6.5 认证与授权
  - 6.6 API 文档

- 7. 文件存储规范（阿里云 OSS）

  - 7.1 选型背景
  - 7.2 集成配置
  - 7.3 使用规范
  - 7.4 安全建议

- 8. 部署与运维规范

  - 8.1 环境要求
  - 8.2 部署架构
  - 8.3 容器化（可选）
  - 8.4 监控与日志

- 9. 版本控制与协作流程

  - 9.1 Git 分支模型
  - 9.2 Commit 规范
  - 9.3 Code Review

------

## 1. 引言

本文档旨在为“智链指数”项目的开发团队提供统一的技术规范和开发指南，确保项目在技术选型、编码风格、数据库设计、接口定义、文件存储、部署运维等方面保持一致性和高质量。项目遵循 **Spring Boot + Vue 3** 技术栈，深度融合大湾区电子信息产业的两业融合业务场景，为中小企业提供数字化诊断、智能匹配、信用评价、出海服务等一体化平台。

------

## 2. 技术架构总览

平台采用前后端分离架构，后端基于 Spring Boot 提供 RESTful API，前端使用 Vue 3 构建单页应用，数据存储使用 MySQL，非结构化文件（如资质证书）使用阿里云 OSS 对象存储。整体架构如下：

- **用户访问层**：浏览器（PC端适配）
- **前端展示层**：Vue 3 + Element Plus + ECharts
- **API 网关层**：Spring Boot Controller + JWT 拦截器
- **业务逻辑层**：Spring Service 组件
- **算法模型层**：Java 工具类封装（加权评分、相似度计算等）
- **数据访问层**：MyBatis Plus Mapper
- **数据存储层**：MySQL（业务数据）+ 阿里云 OSS（文件）

------

## 3. 后端开发规范

### 3.1 技术栈

| 技术/组件    | 版本/选型              | 说明                           |
| :----------- | :--------------------- | :----------------------------- |
| 核心框架     | Spring Boot            | 3.5.x                          |
| JDK          | OpenJDK 17             | LTS 版本                       |
| 数据库       | MySQL                  | 8.0+                           |
| 连接池       | HikariCP               | Spring Boot 默认               |
| ORM 框架     | MyBatis Plus           | 3.5.x                          |
| 代码生成器   | MyBatis Plus Generator | 快速生成 Entity/Mapper/Service |
| 安全框架     | Spring Security        | 6.x                            |
| JWT 支持     | jjwt                   | 0.12.6                         |
| API 文档     | springdoc-openapi      | 2.6.0                          |
| JSON 处理    | Jackson                | Spring Boot 默认               |
| 文件上传     | Spring MultipartFile   | 支持单文件/多文件上传          |
| 对象存储 SDK | 阿里云 OSS SDK         | 3.17.4                         |
| 定时任务     | Spring Scheduling      | @Scheduled                     |
| 工具库       | Lombok                 | 1.18.24                        |
| 工具库       | HuTools                | 用于字符串、集合等操作         |
| 测试框架     | JUnit 5 + Mockito      | 单元测试与模拟测试             |
| 构建工具     | Maven                  | 3.8+                           |

### 3.2 项目结构

```text
zhilian-backend/
├── src/main/java/com/zhilian/zhilianbackend/
│   ├── controller/          # 控制器层
│   ├── service/             # 业务逻辑层
│   │   ├── impl/            # 服务实现
│   │   └── algorithm/       # 算法封装（诊断、匹配、信用分等）
│   ├── mapper/              # MyBatis Plus Mapper
│   ├── entity/              # 实体类（与数据库表对应）
│   ├── dto/                 # 数据传输对象（细分为 request/ 和 response/，response 即视图对象 VO）
│   │   ├── request/         # 请求参数对象（DTO）
│   │   └── response/        # 响应结果对象（VO）
│   ├── config/              # 配置类（Security、Swagger、OSS等）
│   ├── utils/               # 工具类（JWT、OSS客户端等）
│   ├── common/                  # 公共模块
│   │   ├── result/              # 统一响应封装
│   │   │   ├── Result.java      # 统一响应体
│   │   │   ├── ResultCode.java  # 状态码枚举
│   │   │   └── PageResult.java  # 分页结果封装
│   │   └── exception/           # 自定义异常
│   │       └── BusinessException.java
│   └── ZhilianApplication.java # 启动类
├── src/main/resources/
│   ├── db/                  # 数据库脚本目录
│   │   ├── schema.sql       # 建表语句（所有表的DDL）
│   │   └── data.sql         # 初始数据（如标签、区域等基础数据）
│   ├── mapper/              # XML 映射文件（若使用）
│   ├── application.yml      # 主配置文件
│   └── static/              # 静态资源（一般不存放）
├── pom.xml                  # Maven 依赖管理
└── README.md
```

### 3.3 编码规范

- **类名**：大驼峰命名，如 `UserController`。
- **方法名**：小驼峰命名，如 `getUserById`。
- **变量名**：小驼峰，避免使用拼音，使用英文单词。
- **常量名**：全大写，下划线分隔，如 `MAX_FILE_SIZE`。
- **包名**：全小写，点分隔，如 `com.zhilian.service`。
- **代码格式**：使用 IDE 默认格式化（如 IntelliJ IDEA 的 Ctrl+Alt+L），保持缩进一致。
- **注释**：关键类、方法、复杂逻辑必须添加注释；类头使用 `/** ... */` 描述功能；方法注释说明作用、参数、返回值。
- **Lombok**：使用 `@Data`、`@Builder` 等简化代码，避免手写 getter/setter。

### 3.4 异常处理

- 使用全局异常处理器 `@RestControllerAdvice` 统一处理异常。
- 自定义业务异常 `BusinessException`，包含错误码和错误信息。
- 对于参数校验失败，抛出 `MethodArgumentNotValidException`，统一返回 400 错误。
- 所有异常最终返回标准格式：`{ "code": 500, "message": "服务器内部错误", "data": null }`（生产环境隐藏堆栈）。

### 3.5 日志规范

- 使用 SLF4J + Logback 记录日志。
- 日志级别：开发环境 DEBUG，生产环境 INFO。
- 关键操作（如用户登录、数据修改、文件上传）需记录 INFO 日志，包含操作人、操作时间、操作内容。
- 异常日志需记录堆栈信息。

### 3.6 安全规范

- 密码使用 BCrypt 加密存储。
- 使用 JWT 进行无状态认证，Token 有效期设置为 2 小时，支持刷新机制。
- 敏感信息（如 AccessKey）通过环境变量或配置中心注入，禁止硬编码。
- 接口权限控制：使用 Spring Security 的 `@PreAuthorize` 注解或自定义拦截器，区分角色权限。

### 3.7 单元测试

- 业务逻辑层（Service）必须编写单元测试，覆盖核心算法和业务规则。
- 使用 JUnit 5 + Mockito 进行 Mock 测试，避免依赖外部服务。
- 测试类命名规范：`XxxServiceTest`，方法命名：`testMethodName_条件_期望结果`。

------

## 4. 前端开发规范

### 4.1 技术栈

| 技术/组件    | 版本/选型         | 说明                                |
| :----------- | :---------------- | :---------------------------------- |
| 核心框架     | Vue 3             | 3.2+                                |
| 运行环境     | Node.js           | 22.x                                |
| 构建工具     | Vite              | 4.x                                 |
| UI 组件库    | Element Plus      | 2.3+                                |
| 状态管理     | Pinia             | Vue 3 官方推荐                      |
| 路由管理     | Vue Router        | 4.x                                 |
| HTTP 客户端  | Axios             | 封装拦截器、统一处理 Token 和错误码 |
| 数据可视化   | ECharts           | 5.x                                 |
| 图表组件封装 | vue-echarts       | ECharts 的 Vue 3 封装组件           |
| 日期处理     | day.js            | 轻量级                              |
| 代码规范     | ESLint + Prettier | 统一代码风格                        |

### 4.2 目录结构

```text
zhilian-frontend/
├── public/                     # 公共静态资源（favicon、index.html模板等）
├── src/                        # 源代码主目录
│   ├── api/                    # API 接口封装（按模块划分）
│   ├── assets/                 # 静态资源（图片、字体、样式等）
│   ├── components/             # 公共组件
│   ├── composables/            # 组合式函数（逻辑复用）
│   ├── layouts/                # 布局组件
│   ├── router/                 # 路由配置
│   ├── stores/                 # Pinia 状态存储
│   ├── utils/                  # 工具函数（日期格式化、文件下载等）
│   ├── views/                  # 页面组件（按业务模块划分）
│   │   ├── dashboard/          # 可视化看板
│   │   ├── diagnosis/          # 数字化诊断
│   │   ├── match/              # 智能匹配
│   │   ├── credit/             # 信用评价
│   │   ├── abroad/             # 出海服务
│   │   └── manage/             # 企业管理
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── index.html                  # 项目入口 HTML
├── package.json                # 项目依赖
├── vite.config.js              # Vite 配置文件
├── .env.example                # 环境变量模板
└── ...                         # 其他配置文件
```

### 4.3 编码规范

- 使用 Vue 3 组合式 API（`<script setup>` 语法）。
- 组件命名：大驼峰，如 `UserProfile.vue`。
- 样式使用 SCSS，遵循 BEM 命名规范。
- ESLint 规则：继承 `eslint:recommended`、`plugin:vue/vue3-recommended`、`prettier`。
- 提交前自动格式化（husky + lint-staged）。

### 4.4 状态管理

- 使用 Pinia 管理全局状态（用户信息、主题配置等）。
- 每个模块的 store 独立文件，如 `useUserStore`、`useDemandStore`。
- 避免在 store 中存放大量组件私有状态。

### 4.5 与后端交互

- 使用 Axios 实例，统一配置 baseURL、超时时间。
- 请求拦截器：自动添加 JWT Token（从 localStorage 获取）。
- 响应拦截器：统一处理成功/失败响应，对 401 状态跳转登录页。
- 所有 API 调用封装在 `src/api/` 下，按模块划分，返回 Promise。

------

## 5. 数据库设计规范

### 5.1 命名规范

- **数据库名**：`zhilian_db`，全小写，下划线分隔。
- **表名**：全小写，下划线分隔，使用单数形式，如 `user`、`manufacture`。
- **字段名**：全小写，下划线分隔，如 `user_id`、`create_time`。
- **主键**：统一命名为 `id`，类型为 `BIGINT`，自增。
- **外键**：建议命名为 `关联表名_id`，如 `manufacture.user_id`。

### 5.2 表设计规范

- 每张表必须包含 `create_time` 和 `update_time` 字段（DATETIME），用于记录创建和更新时间（可利用 MyBatis Plus 自动填充）。
- 使用 `status` 字段（TINYINT）进行逻辑删除或状态控制，0 表示禁用/删除，1 表示正常。
- 金额字段使用 `DECIMAL(12,2)` 或 `DECIMAL(15,2)`，避免浮点数精度问题。
- 评分字段使用 `TINYINT`（1-5）或 `DECIMAL(2,1)` 保留一位小数。
- 枚举字段尽量使用 `ENUM` 类型，若后期可能扩展则使用 `TINYINT` 并配合代码注释。

### 5.3 索引规范

- 主键自动建立聚簇索引。
- 外键字段（如 `user_id`、`manu_id`、`service_id` 等）需建立普通索引。
- 频繁查询的字段（如 `region`、`status`、`create_time`）可酌情建立索引。
- 联合索引应根据查询条件顺序建立，避免冗余索引。
- 多对多关系的中间表（如 `demand_tag`）的联合字段作为联合主键，自动建立索引。

### 5.4 核心表结构

详细表结构参见《数据库设计.md》，核心表清单：

- `user`：用户表
- `manufacture`：制造企业表
- `service_provider`：服务商表
- `demand`：需求表
- `demand_tag`：需求-标签关联表
- `tag`：标签字典表
- `service_tag`：服务商-标签关联表
- `cooperation`：合作记录表
- `evaluation`：评价表
- `diagnosis`：诊断记录表
- `region_index`：区域指数表
- `certification`：资质证书表
- `credit_score`：信用分记录表
- `abroad_case`：出海成功案例表

------

## 6. 接口设计规范

### 6.1 RESTful 风格

- **资源**：使用名词复数表示，如 `/manufacture`、`/demand`。
- **HTTP 方法**：
  - GET：获取资源列表或单个资源
  - POST：创建资源
  - PUT：全量更新资源
  - PATCH：部分更新资源
  - DELETE：删除资源
- **路径参数**：使用 `{id}` 表示资源标识，如 `/manufacture/{id}`。
- **查询参数**：用于分页、筛选、排序，如 `?page=1&size=10&region=深圳`。

### 6.2 统一响应格式

所有接口返回 JSON 格式，包含以下字段：

```json
{
  "code": 200,          // 状态码（200 成功，其他失败）
  "message": "success", // 提示信息
  "data": {}            // 业务数据，可为 null
}
```

成功时 code 为 200，message 为 "success"；失败时 code 为 4xx/5xx，message 为错误描述。

### 6.3 参数校验

- 使用 Bean Validation（`@Valid`）对请求体进行校验，如 `@NotNull`、`@Size`、`@Pattern`。
- 在 Controller 层对查询参数进行基础校验，如分页参数不能小于 1。
- 校验失败时，全局异常处理器捕获 `MethodArgumentNotValidException`，返回 400 及具体错误信息。

### 6.4 分页规范

- **请求参数**：`page`（页码，从 1 开始）、`size`（每页条数，默认 10）。
- **响应格式**：

```json
{
  "code": 200,
  "data": {
    "total": 100,           // 总记录数
    "records": [ ... ]      // 当前页数据列表
  }
}
```

### 6.5 认证与授权

- 除登录、注册、获取公钥等开放接口外，其他接口均需认证。
- 认证方式：在请求头携带 `Authorization: Bearer <token>`。
- 服务端使用 JWT 拦截器验证 token 有效性，并将用户信息存入 `SecurityContext`。
- 权限控制：使用 `@PreAuthorize("hasRole('manufacture')")` 等注解进行角色鉴权。

### 6.6 API 文档

- 集成 **springdoc-openapi**，自动生成符合 OpenAPI 3 规范的 API 文档。

- 在 Controller 类和方法上使用 OpenAPI 3 注解（如 `@Tag`、`@Operation`、`@Parameter`）描述接口。

- 实体类使用 `@Schema` 注解描述字段。

- 文档访问地址：`/swagger-ui.html`（默认）或 `/swagger-ui/index.html`。

- 支持通过 `application.yml` 配置文档信息，例如：

  ```yaml
  springdoc:
    api-docs:
      path: /api-docs          # OpenAPI JSON 路径
    swagger-ui:
      path: /swagger-ui.html   # Swagger UI 路径
      operations-sorter: method
  ```

------

## 7. 文件存储规范（阿里云 OSS）

### 7.1 选型背景

- 资质证书、案例封面等文件属于非结构化数据，不适合直接存入数据库。
- 阿里云 OSS 提供高可用、低成本的对象存储服务，支持 CDN 加速，适合学生项目（有免费额度）。

### 7.2 集成配置

**Maven 依赖**：

```xml
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.17.4</version>
</dependency>
```

**application.yml 配置**：

```yaml
aliyun:
  oss:
    endpoint: oss-cn-shenzhen.aliyuncs.com
    access-key-id: ${OSS_ACCESS_KEY_ID}          # 从环境变量读取
    access-key-secret: ${OSS_ACCESS_KEY_SECRET}  # 从环境变量读取
    bucket-name: zhilian-cert
    base-url: https://zhilian-cert.oss-cn-shenzhen.aliyuncs.com/
```

**注意**：AccessKey 禁止明文写在代码库中，必须通过环境变量或配置中心注入。

### 7.3 使用规范

- **文件路径**：采用 `业务类型/年份/月份/文件名` 结构，如 `cert/2026/03/abc.jpg`。
- **文件名**：建议使用 UUID 或 时间戳 + 原后缀，避免中文和特殊字符。
- **上传流程**：
  1. 后端接收 MultipartFile。
  2. 生成唯一对象名。
  3. 调用 OSS SDK 上传文件流。
  4. 将返回的 URL 存入数据库对应字段（如 `certification.cert_file_url`）。
- **删除流程**：删除数据库记录时，同步调用 OSS 删除文件（可选，根据业务决定是否物理删除）。
- **访问控制**：默认设置为私有读写（通过签名 URL 临时访问）或公共读（根据业务需求）。若需安全控制，可使用签名 URL 或防盗链。

### 7.4 安全建议

- 使用 RAM 子账号 AccessKey，授予最小权限（仅允许 PutObject、GetObject、DeleteObject）。
- 对于敏感文件，可设置过期时间（如通过签名 URL 访问，有效期 5 分钟）。
- 开启 OSS 日志记录，便于审计。

------

## 8. 部署与运维规范

### 8.1 环境要求

- **服务器**：Linux CentOS 7+ 或 Ubuntu 20.04，至少 1核2GB 内存。
- **JDK**：OpenJDK 17
- **MySQL**：8.0+
- **Nginx**：1.20+
- **Node.js**：16+（仅用于前端构建）

### 8.2 部署架构

```text
用户 → Nginx（反向代理）
        ├── /api/* → 后端 Spring Boot（端口 8080）
        └── /*     → 前端静态文件（dist 目录）
```

- 后端打包为 jar 包，使用 `java -jar` 运行，或通过 systemd 管理。
- 前端使用 `npm run build` 生成 dist 目录，由 Nginx 托管。

### 8.3 容器化（可选）

- 使用 Docker 打包后端、前端、MySQL，使用 Docker Compose 编排，便于环境一致性。
- 示例 Dockerfile 和 docker-compose.yml 可参考项目文档。

### 8.4 监控与日志

- 后端集成 Spring Boot Actuator，暴露健康检查端点（`/actuator/health`）。
- 使用 ELK 或简单脚本收集日志文件（`logs/` 目录）。
- 关键指标（如 QPS、错误率）可通过 Prometheus + Grafana 监控（可选）。

------

## 9. 版本控制与协作流程

### 9.1 Git 分支模型

- **main**：主分支，仅存放稳定版本，不可直接提交代码。
- **dev**：开发分支，日常开发基于此分支。
- **feature/xxx**：功能分支，从 dev 检出，完成后合并回 dev。
  - 若是前端，则为**feature/frontend-xxx**
  - 若是后端，则为**feature/backend-xxx**

- ~~**hotfix/xxx**：紧急修复分支，从 master 检出，修复后同时合并到 master 和 dev。~~

### 9.2 Commit 规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码风格调整（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链变动

示例：`feat: backend-新增数字化诊断问卷提交接口`

### 9.3 Code Review

- 所有合并到 dev 或 master 的代码必须经过 Pull Request 和至少一人 Review。
- Review 关注点：代码规范、业务逻辑正确性、异常处理、安全漏洞。
- 使用 GitHub/GitLab 的 PR 功能进行讨论和审核。

------

> 本文档自发布之日起生效，所有开发人员须严格遵守。如有更新，将及时通知团队。