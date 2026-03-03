# 智链指数平台完整接口文档

**基础URL**：`http://localhost:8080/api`（示例，实际以部署为准）
**接口响应格式**：统一返回JSON格式，包含`code`（状态码）、`message`（提示信息）、`data`（业务数据）。
**认证方式**：除登录/注册外，其他接口需在请求头携带`Authorization: Bearer <token>`。

------

## 目录

- 一、企业信息管理模块
  - 1.1 用户认证接口
  - 1.2 制造企业管理接口
  - 1.3 服务商管理接口
  - 1.4 资质证书管理接口
  - 1.5 辅助接口
- 二、企业数字化诊断模块
- 三、区域协同指数模块
- 四、智能供需匹配模块
- 五、信用评价体系模块
- 六、出海服务专区模块
- 七、数据可视化看板模块

------

## 一、企业信息管理模块

### 1.1 用户认证接口

| 接口名称         | URL                         | 请求方式 | 请求参数                                         | 响应说明                        |
| :--------------- | :-------------------------- | :------- | :----------------------------------------------- | :------------------------------ |
| 用户注册         | `/api/auth/register`        | POST     | `username`, `password`, `role`, `phone`, `email` | 注册成功返回用户基本信息        |
| 用户登录         | `/api/auth/login`           | POST     | `username`, `password`                           | 登录成功返回JWT token及用户信息 |
| 获取当前用户信息 | `/api/auth/me`              | GET      | 无                                               | 返回当前登录用户的详细信息      |
| 修改密码         | `/api/auth/change-password` | POST     | `oldPassword`, `newPassword`                     | 修改成功后返回成功消息          |

**示例：用户登录**
请求：`POST /api/auth/login`

```json
{
  "username": "tech_company",
  "password": "123456"
}
```

响应：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1001,
    "username": "tech_company",
    "role": "manufacture"
  }
}
```

------

### 1.2 制造企业管理接口

| 接口名称         | URL                     | 请求方式 | 请求参数                                                     | 响应说明             |
| :--------------- | :---------------------- | :------- | :----------------------------------------------------------- | :------------------- |
| 获取制造企业列表 | `/api/manufacture/list` | GET      | 分页参数（`page`, `size`）、筛选条件（`region`, `scale`, `product_type`等） | 返回企业列表（分页） |
| 获取制造企业详情 | `/api/manufacture/{id}` | GET      | 路径参数`id`（企业ID）                                       | 返回指定企业详细信息 |
| 新增制造企业     | `/api/manufacture`      | POST     | 企业信息JSON（见示例）                                       | 返回新增企业ID       |
| 修改制造企业信息 | `/api/manufacture/{id}` | PUT      | 路径参数`id`，企业信息JSON（只传需修改字段）                 | 返回成功消息         |
| 删除制造企业     | `/api/manufacture/{id}` | DELETE   | 路径参数`id`（逻辑删除）                                     | 返回成功消息         |

**示例：获取制造企业列表**
请求：`GET /api/manufacture/list?page=1&size=10&region=深圳`
响应：

```json
{
  "code": 200,
  "data": {
    "total": 100,
    "records": [
      {
        "id": 1001,
        "companyName": "深圳电子科技",
        "region": "深圳",
        "scale": "medium",
        "productType": "PCB",
        "contactPerson": "张三",
        "contactPhone": "13800138001"
      }
    ]
  }
}
```

**示例：新增制造企业**
请求：`POST /api/manufacture`

```json
{
  "userId": 1001,
  "companyName": "东莞精密制造",
  "region": "东莞",
  "address": "东莞市松山湖",
  "contactPerson": "李四",
  "contactPhone": "13900139002",
  "scale": "small",
  "employeeCount": 120,
  "annualRevenue": 5000.00,
  "productType": "消费电子",
  "description": "专注于智能穿戴设备制造",
  "establishedDate": "2020-05-01"
}
```

响应：

```json
{
  "code": 200,
  "message": "新增成功",
  "data": {
    "id": 1010
  }
}
```

------

### 1.3 服务商管理接口

| 接口名称       | URL                          | 请求方式 | 请求参数                                         | 响应说明                                 |
| :------------- | :--------------------------- | :------- | :----------------------------------------------- | :--------------------------------------- |
| 获取服务商列表 | `/api/service-provider/list` | GET      | 分页参数、筛选条件（`region`, `service_type`等） | 返回服务商列表（分页）                   |
| 获取服务商详情 | `/api/service-provider/{id}` | GET      | 路径参数`id`                                     | 返回指定服务商详细信息（含资质证书列表） |
| 新增服务商     | `/api/service-provider`      | POST     | 服务商信息JSON                                   | 返回新增服务商ID                         |
| 修改服务商信息 | `/api/service-provider/{id}` | PUT      | 路径参数`id`，服务商信息JSON                     | 返回成功消息                             |
| 删除服务商     | `/api/service-provider/{id}` | DELETE   | 路径参数`id`（逻辑删除）                         | 返回成功消息                             |

**示例：获取服务商详情**
请求：`GET /api/service-provider/2001`
响应：

```json
{
  "code": 200,
  "data": {
    "id": 2001,
    "companyName": "华测检测",
    "region": "深圳",
    "serviceType": "检测认证",
    "description": "CNAS认可实验室",
    "contactPerson": "王五",
    "contactPhone": "13700137003",
    "website": "www.cti.com",
    "qualification": "CNAS、CMA",
    "certifications": [
      {
        "certName": "CNAS认证",
        "certNo": "CNAS L1234",
        "expireDate": "2026-12-31"
      }
    ]
  }
}
```

------

### 1.4 资质证书管理接口

| 接口名称         | URL                         | 请求方式 | 请求参数                                                     | 响应说明     |
| :--------------- | :-------------------------- | :------- | :----------------------------------------------------------- | :----------- |
| 获取资质证书列表 | `/api/certification/list`   | GET      | 可选`serviceId`筛选                                          | 返回证书列表 |
| 上传资质证书     | `/api/certification/upload` | POST     | `serviceId`, `certName`, `certNo`, `issueAuthority`, `issueDate`, `expireDate`, 文件（multipart/form-data） | 返回证书ID   |
| 更新证书信息     | `/api/certification/{id}`   | PUT      | 证书信息JSON                                                 | 返回成功消息 |
| 删除证书         | `/api/certification/{id}`   | DELETE   | 路径参数`id`                                                 | 返回成功消息 |

**示例：上传资质证书**
请求：`POST /api/certification/upload`（form-data格式）
表单字段：

- `serviceId`: 2001
- `certName`: CNAS认证
- `certNo`: CNAS L5678
- `issueAuthority`: 中国合格评定国家认可委员会
- `issueDate`: 2023-01-01
- `expireDate`: 2028-01-01
- `file`: （证书扫描件）

响应：

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 3005
  }
}
```

------

### 1.5 辅助接口

| 接口名称         | URL                        | 请求方式 | 说明                                           |
| :--------------- | :------------------------- | :------- | :--------------------------------------------- |
| 获取区域列表     | `/api/common/regions`      | GET      | 返回大湾区区域列表（深圳/东莞/惠州/广州等）    |
| 获取企业规模枚举 | `/api/common/scales`       | GET      | 返回规模选项（微型/小型/中型/大型）            |
| 获取服务类型标签 | `/api/common/service-tags` | GET      | 返回服务类型标签（用于服务商service_type选择） |

------

## 二、企业数字化诊断模块

| 接口名称     | URL                          | 请求方式 | 请求参数                                                     | 响应说明                                     |
| :----------- | :--------------------------- | :------- | :----------------------------------------------------------- | :------------------------------------------- |
| 提交诊断问卷 | `/api/diagnosis/submit`      | POST     | `manuId`（企业ID）、`infoScore`、`autoScore`、`dataScore`、`serviceScore` | 返回诊断ID、总分、等级、各维度得分及改进建议 |
| 获取诊断报告 | `/api/diagnosis/result/{id}` | GET      | 路径参数`id`（诊断记录ID）                                   | 返回指定诊断的详细报告                       |

**示例：提交诊断问卷**
请求：

```json
{
  "manuId": 1001,
  "infoScore": 4,
  "autoScore": 3,
  "dataScore": 2,
  "serviceScore": 3
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "diagnosisId": 5001,
    "totalScore": 65,
    "level": "成熟期",
    "radarData": {
      "信息化": 80,
      "自动化": 60,
      "数据应用": 40,
      "服务协同": 60
    },
    "suggestions": [
      "建议引入数据分析工具，提升数据应用能力",
      "可考虑将非核心业务外包，聚焦主业"
    ]
  }
}
```

------

## 三、区域协同指数模块

| 接口名称         | URL                          | 请求方式 | 请求参数                                     | 响应说明                   |
| :--------------- | :--------------------------- | :------- | :------------------------------------------- | :------------------------- |
| 获取所有区域指数 | `/api/index/region/list`     | GET      | 可选：`quarter`（如2025Q1）、`year`、`month` | 返回各区域协同指数列表     |
| 获取特定区域指数 | `/api/index/region/{region}` | GET      | 路径参数`region`，可选时间参数同上           | 返回指定区域指数详情       |
| 获取趋势数据     | `/api/index/trend`           | GET      | 必选：`region`，可选时间范围                 | 返回该区域指数历史趋势数据 |

**示例：获取所有区域指数（2025年第一季度）**
请求：`GET /api/index/region/list?quarter=2025Q1`
响应：

```json
{
  "code": 200,
  "data": [
    {
      "region": "深圳",
      "coopDensity": 0.85,
      "serviceRate": 0.72,
      "crossRate": 0.45,
      "totalIndex": 75.8
    },
    {
      "region": "东莞",
      "coopDensity": 0.78,
      "serviceRate": 0.68,
      "crossRate": 0.52,
      "totalIndex": 72.4
    }
  ]
}
```

------

## 四、智能供需匹配模块

| 接口名称     | URL                               | 请求方式 | 请求参数                                                     | 响应说明                                            |
| :----------- | :-------------------------------- | :------- | :----------------------------------------------------------- | :-------------------------------------------------- |
| 发布需求     | `/api/demand/publish`             | POST     | `manuId`, `title`, `description`, `expected_budget`, `deadline`, `tags`（标签ID列表）等 | 返回发布成功后的需求ID                              |
| 获取匹配推荐 | `/api/match/recommend/{demandId}` | GET      | 路径参数`demandId`                                           | 返回Top N服务商列表，包含匹配分、匹配理由、信用分等 |
| 匹配结果反馈 | `/api/match/feedback`             | POST     | `demandId`, `selectedServiceId`, `feedbackType`（采纳/忽略）等 | 用于优化匹配算法                                    |

**示例：获取匹配推荐（需求ID 3001）**
请求：`GET /api/match/recommend/3001`
响应：

```json
{
  "code": 200,
  "data": {
    "demandId": 3001,
    "recommendations": [
      {
        "serviceId": 2001,
        "companyName": "华测检测认证集团",
        "matchScore": 92,
        "matchReason": "有8年电子产品CE认证经验，服务过华为、比亚迪",
        "creditScore": 95,
        "tags": ["CNAS认证", "欧盟CE", "美国FCC"]
      },
      {
        "serviceId": 2005,
        "companyName": "SGS通标标准",
        "matchScore": 88,
        "matchReason": "全球领先检测机构，在东莞设有实验室",
        "creditScore": 98,
        "tags": ["国际认可", "CB认证", "UL认证"]
      }
    ]
  }
}
```

------

## 五、信用评价体系模块

| 接口名称         | URL                                | 请求方式 | 请求参数                                              | 响应说明                         |
| :--------------- | :--------------------------------- | :------- | :---------------------------------------------------- | :------------------------------- |
| 获取服务商信用分 | `/api/credit/{serviceId}`          | GET      | 路径参数`serviceId`                                   | 返回该服务商综合信用分及各维度分 |
| 提交评价         | `/api/evaluation/submit`           | POST     | `coopId`, `score`（1-5星）, `content`, `is_anonymous` | 返回评价ID                       |
| 获取评价列表     | `/api/evaluation/list/{serviceId}` | GET      | 路径参数`serviceId`，可选分页参数                     | 返回该服务商收到的所有评价列表   |

**示例：获取服务商信用分**
请求：`GET /api/credit/2001`
响应：

```json
{
  "code": 200,
  "data": {
    "serviceId": 2001,
    "score": 92,
    "qualScore": 100,
    "caseScore": 85,
    "evalScore": 90,
    "calcTime": "2025-03-15 10:30:00"
  }
}
```

------

## 六、出海服务专区模块

| 接口名称             | URL                             | 请求方式 | 请求参数                                 | 响应说明                                 |
| :------------------- | :------------------------------ | :------- | :--------------------------------------- | :--------------------------------------- |
| 获取出海服务列表     | `/api/abroad/services`          | GET      | 可选分类筛选（如`serviceType`=国际认证） | 返回服务商列表及服务详情                 |
| 获取特定国家准入指南 | `/api/abroad/country/{country}` | GET      | 路径参数`country`                        | 返回该国电子信息产品准入要求、认证流程等 |
| 获取成功案例         | `/api/abroad/cases`             | GET      | 可选参数：`country`, `serviceType`       | 返回出海成功案例列表                     |

**示例：获取出海服务列表**
请求：`GET /api/abroad/services?serviceType=国际认证`
响应：

```json
{
  "code": 200,
  "data": [
    {
      "serviceId": 2001,
      "companyName": "华测检测",
      "serviceType": "国际认证",
      "description": "提供CE、FCC、UL等认证服务",
      "countryCoverage": ["欧盟", "美国", "日本"]
    }
  ]
}
```

------

## 七、数据可视化看板模块

| 接口名称         | URL                         | 请求方式 | 请求参数                | 响应说明                                  |
| :--------------- | :-------------------------- | :------- | :---------------------- | :---------------------------------------- |
| 获取统计卡片数据 | `/api/dashboard/statistics` | GET      | 无                      | 返回企业总数、服务商数、需求数、合作数等  |
| 获取热力图数据   | `/api/dashboard/heatmap`    | GET      | 可选时间范围            | 返回各区域需求分布热力图数据              |
| 获取热门需求     | `/api/dashboard/topDemands` | GET      | 可选数量限制（如top=5） | 返回最热门服务类型及对应数量              |
| 获取网络关系数据 | `/api/dashboard/network`    | GET      | 无                      | 返回制造企业-服务商合作网络节点与关系数据 |

**示例：获取统计卡片数据**
请求：`GET /api/dashboard/statistics`
响应：

```json
{
  "code": 200,
  "data": {
    "manufactureCount": 1250,
    "serviceCount": 380,
    "demandCount": 560,
    "cooperationCount": 890
  }
}
```

------

## 附录：通用说明

- **分页参数**：`page`（页码，从1开始）、`size`（每页条数），默认为`page=1, size=10`。
- **时间格式**：统一使用`yyyy-MM-dd`或`yyyy-MM-dd HH:mm:ss`。
- **状态码说明**：
  - `200`：成功
  - `400`：请求参数错误
  - `401`：未认证或token无效
  - `403`：无权限
  - `404`：资源不存在
  - `500`：服务器内部错误