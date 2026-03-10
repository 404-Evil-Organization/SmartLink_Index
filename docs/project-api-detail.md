## 一、企业信息管理模块

### 1.1 用户认证接口

#### 1.1.1 用户注册

- **URL**: `/api/auth/register`
- **Method**: `POST`
- **请求参数**（JSON Body）:

| 参数名   | 类型   | 必填 | 描述                                         |
| :------- | :----- | :--- | :------------------------------------------- |
| username | string | 是   | 用户名，唯一                                 |
| password | string | 是   | 密码（明文，后端加密存储）                   |
| role     | string | 是   | 角色：`manufacture`/`service`/`park`/`admin` |
| phone    | string | 是   | 联系电话                                     |
| email    | string | 是   | 电子邮箱                                     |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1001,
    "username": "tech_company",
    "role": "manufacture"
  }
}
```

#### 1.1.2 用户登录

- **URL**: `/api/auth/login`
- **Method**: `POST`
- **请求参数**（JSON Body）:

| 参数名   | 类型   | 必填 | 描述   |
| :------- | :----- | :--- | :----- |
| username | string | 是   | 用户名 |
| password | string | 是   | 密码   |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
  }
}
```

#### 1.1.3 获取当前用户信息

- **URL**: `/api/auth/me`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**: 无
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "username": "tech_company",
    "role": "manufacture",
    "phone": "13800138001",
    "email": "test@example.com",
    "status": 1,
    "createTime": "2026-03-01 10:00:00"
  }
}
```

#### 1.1.4 修改密码

- **URL**: `/api/auth/change-password`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**（JSON Body）:

| 参数名      | 类型   | 必填 | 描述   |
| :---------- | :----- | :--- | :----- |
| oldPassword | string | 是   | 旧密码 |
| newPassword | string | 是   | 新密码 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

------

### 1.2 制造企业管理接口

#### 1.2.1 获取制造企业列表

- **URL**: `/api/manufacture/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**（Query）:

| 参数名      | 类型   | 必填 | 描述                                       |
| :---------- | :----- | :--- | :----------------------------------------- |
| page        | int    | 否   | 页码，默认1                                |
| size        | int    | 否   | 每页条数，默认10                           |
| region      | string | 否   | 区域筛选（如“深圳”）                       |
| scale       | string | 否   | 规模筛选：`micro`/`small`/`medium`/`large` |
| productType | string | 否   | 主营产品类型（模糊匹配）                   |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
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

#### 1.2.2 获取制造企业详情

- **URL**: `/api/manufacture/{id}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (企业ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "userId": 1001,
    "companyName": "深圳电子科技",
    "region": "深圳",
    "address": "深圳市南山区",
    "contactPerson": "张三",
    "contactPhone": "13800138001",
    "scale": "medium",
    "employeeCount": 500,
    "annualRevenue": 8000.00,
    "productType": "PCB",
    "description": "专业PCB制造商",
    "logo": "https://...",
    "establishedDate": "2010-05-01",
    "createTime": "2026-03-01 10:00:00",
    "updateTime": "2026-03-01 10:00:00"
  }
}
```

#### 1.2.3 新增制造企业

- **URL**: `/api/manufacture`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需具有相应权限）
- **请求参数**（JSON Body）:

| 参数名          | 类型    | 必填 | 描述           |
| :-------------- | :------ | :--- | :------------- |
| userId          | long    | 是   | 关联的用户ID   |
| companyName     | string  | 是   | 企业全称       |
| region          | string  | 否   | 区域           |
| address         | string  | 否   | 详细地址       |
| contactPerson   | string  | 否   | 联系人         |
| contactPhone    | string  | 否   | 联系电话       |
| scale           | string  | 否   | 规模枚举       |
| employeeCount   | int     | 否   | 员工人数       |
| annualRevenue   | decimal | 否   | 年营收（万元） |
| productType     | string  | 否   | 主营产品类型   |
| description     | string  | 否   | 企业简介       |
| logo            | string  | 否   | Logo图片URL    |
| establishedDate | date    | 否   | 成立日期       |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1010
  }
}
```

#### 1.2.4 修改制造企业信息

- **URL**: `/api/manufacture/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (企业ID)
- **请求参数**（JSON Body，只传需修改字段）: 同新增接口字段
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 1.2.5 删除制造企业

- **URL**: `/api/manufacture/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (企业ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

------

### 1.3 服务商管理接口

#### 1.3.1 获取服务商列表

- **URL**: `/api/service-provider/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**（Query）:

| 参数名      | 类型   | 必填 | 描述                         |
| :---------- | :----- | :--- | :--------------------------- |
| page        | int    | 否   | 页码，默认1                  |
| size        | int    | 否   | 每页条数，默认10             |
| region      | string | 否   | 区域筛选（如“深圳”）         |
| serviceType | string | 否   | 服务大类筛选（如“检测认证”） |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "records": [
      {
        "id": 2001,
        "companyName": "华测检测认证集团",
        "region": "深圳",
        "serviceType": "检测认证",
        "contactPerson": "王五",
        "contactPhone": "13700137003",
        "qualification": "CNAS、CMA"
      }
    ]
  }
}
```

#### 1.3.2 获取服务商详情

- **URL**: `/api/service-provider/{id}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (服务商ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "userId": 2001,
    "companyName": "华测检测认证集团",
    "region": "深圳",
    "address": "深圳市南山区科技园",
    "contactPerson": "王五",
    "contactPhone": "13700137003",
    "serviceType": "检测认证",
    "description": "CNAS认可实验室，提供国际认证服务",
    "logo": "https://...",
    "website": "www.cti.com",
    "establishedDate": "2003-01-01",
    "employeeCount": 2000,
    "qualification": "CNAS、CMA",
    "createTime": "2026-03-01 10:00:00",
    "updateTime": "2026-03-01 10:00:00",
    "certifications": [
      {
        "id": 3001,
        "certName": "CNAS认证",
        "certNo": "CNAS L1234",
        "expireDate": "2026-12-31"
      }
    ]
  }
}
```

#### 1.3.3 新增服务商

- **URL**: `/api/service-provider`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需具有相应权限）
- **请求参数**（JSON Body）:

| 参数名          | 类型   | 必填 | 描述                               |
| :-------------- | :----- | :--- | :--------------------------------- |
| userId          | long   | 是   | 关联的用户ID                       |
| companyName     | string | 是   | 企业全称                           |
| region          | string | 否   | 区域                               |
| address         | string | 否   | 详细地址                           |
| contactPerson   | string | 否   | 联系人                             |
| contactPhone    | string | 否   | 联系电话                           |
| serviceType     | string | 否   | 服务大类（可多选，逗号分隔或JSON） |
| description     | string | 否   | 服务介绍                           |
| logo            | string | 否   | Logo图片URL                        |
| website         | string | 否   | 企业官网                           |
| establishedDate | date   | 否   | 成立日期                           |
| employeeCount   | int    | 否   | 员工人数                           |
| qualification   | string | 否   | 资质概述                           |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2010
  }
}
```

#### 1.3.4 修改服务商信息

- **URL**: `/api/service-provider/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (服务商ID)
- **请求参数**（JSON Body，只传需修改字段）: 同新增接口字段
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 1.3.5 删除服务商

- **URL**: `/api/service-provider/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (服务商ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

------

### 1.4 资质证书管理接口

#### 1.4.1 获取资质证书列表

- **URL**: `/api/certification/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**（Query）:

| 参数名    | 类型 | 必填 | 描述                       |
| :-------- | :--- | :--- | :------------------------- |
| serviceId | long | 否   | 服务商ID，筛选该服务商证书 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 3001,
      "serviceId": 2001,
      "certName": "CNAS认证",
      "certNo": "CNAS L1234",
      "issueAuthority": "中国合格评定国家认可委员会",
      "issueDate": "2023-01-01",
      "expireDate": "2026-12-31",
      "certFileUrl": "https://...",
      "status": 1,
      "createTime": "2026-03-01 10:00:00"
    }
  ]
}
```

#### 1.4.2 上传资质证书

- **URL**: `/api/certification/upload`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`，Content-Type: `multipart/form-data`
- **请求参数**（Form Data）:

| 参数名         | 类型   | 必填 | 描述                 |
| :------------- | :----- | :--- | :------------------- |
| serviceId      | long   | 是   | 服务商ID             |
| certName       | string | 是   | 证书名称             |
| certNo         | string | 否   | 证书编号             |
| issueAuthority | string | 否   | 发证机构             |
| issueDate      | date   | 否   | 发证日期             |
| expireDate     | date   | 否   | 有效期至             |
| file           | file   | 是   | 证书文件（图片/PDF） |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3005
  }
}
```

#### 1.4.3 更新证书信息

- **URL**: `/api/certification/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (证书ID)
- **请求参数**（JSON Body）: 可更新字段同上传接口（不含文件）
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 1.4.4 删除证书

- **URL**: `/api/certification/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (证书ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

------

### 1.5 辅助接口

#### 1.5.1 获取区域列表

- **URL**: `/api/common/regions`
- **Method**: `GET`
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": ["深圳", "东莞", "惠州", "广州", "佛山", "中山", "珠海", "江门", "肇庆"]
}
```

#### 1.5.2 获取企业规模枚举

- **URL**: `/api/common/scales`
- **Method**: `GET`
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "value": "micro", "label": "微型企业" },
    { "value": "small", "label": "小型企业" },
    { "value": "medium", "label": "中型企业" },
    { "value": "large", "label": "大型企业" }
  ]
}
```

#### 1.5.3 获取服务类型标签

- **URL**: `/api/common/service-tags`
- **Method**: `GET`
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 1, "name": "检测认证", "category": "服务类型" },
    { "id": 2, "name": "工业设计", "category": "服务类型" },
    { "id": 3, "name": "物流供应链", "category": "服务类型" }
  ]
}
```

------

## 二、企业数字化诊断模块

### 2.1 提交诊断问卷

- **URL**: `/api/diagnosis/submit`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需制造企业角色）
- **请求参数**（JSON Body）:

| 参数名       | 类型 | 必填 | 描述                |
| :----------- | :--- | :--- | :------------------ |
| manuId       | long | 是   | 制造企业ID          |
| infoScore    | int  | 是   | 信息化得分（1-5）   |
| autoScore    | int  | 是   | 自动化得分（1-5）   |
| dataScore    | int  | 是   | 数据应用得分（1-5） |
| serviceScore | int  | 是   | 服务协同得分（1-5） |

- **返回数据**:

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

### 2.2 获取诊断报告

- **URL**: `/api/diagnosis/result/{id}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (诊断记录ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "diagnosisId": 5001,
    "manuId": 1001,
    "infoScore": 4,
    "autoScore": 3,
    "dataScore": 2,
    "serviceScore": 3,
    "totalScore": 65,
    "level": "成熟期",
    "suggestions": ["..."],
    "diagnosisDate": "2026-03-07 14:30:00"
  }
}
```

------

## 三、区域协同指数模块

### 3.1 获取所有区域指数

- **URL**: `/api/index/region/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（可选）
- **请求参数**（Query）:

| 参数名  | 类型   | 必填 | 描述              |
| :------ | :----- | :--- | :---------------- |
| quarter | string | 否   | 季度，如 "2025Q1" |
| year    | int    | 否   | 年份              |
| month   | int    | 否   | 月份              |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "region": "深圳",
      "coopDensity": 0.85,
      "serviceRate": 0.72,
      "crossRate": 0.45,
      "totalIndex": 75.8
    }
  ]
}
```

### 3.2 获取特定区域指数

- **URL**: `/api/index/region/{region}`
- **Method**: `GET`
- **路径参数**: `region` (区域名称，如"深圳")
- **请求参数**（可选）: 同3.1的时间参数
- **返回数据**: 单个区域指数详情（字段同上，且包含 `message`）

### 3.3 获取趋势数据

- **URL**: `/api/index/trend`
- **Method**: `GET`
- **请求参数**（Query）:

| 参数名 | 类型   | 必填 | 描述                      |
| :----- | :----- | :--- | :------------------------ |
| region | string | 是   | 区域                      |
| start  | string | 否   | 开始时间，如 "2025-01-01" |
| end    | string | 否   | 结束时间                  |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "date": "2025Q1", "totalIndex": 75.8 },
    { "date": "2025Q2", "totalIndex": 76.5 }
  ]
}
```

------

## 四、智能供需匹配模块

### 4.1 发布需求

- **URL**: `/api/demand/publish`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（制造企业）
- **请求参数**（JSON Body）:

| 参数名          | 类型    | 必填 | 描述         |
| :-------------- | :------ | :--- | :----------- |
| manuId          | long    | 是   | 制造企业ID   |
| title           | string  | 是   | 需求标题     |
| description     | string  | 否   | 详细描述     |
| expected_budget | decimal | 否   | 预算（万元） |
| deadline        | date    | 否   | 期望完成日期 |
| tags            | long[]  | 否   | 标签ID列表   |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "demandId": 3001
  }
}
```

### 4.2 获取匹配推荐

- **URL**: `/api/match/recommend/{demandId}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `demandId` (需求ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
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
      }
    ]
  }
}
```

### 4.3 匹配结果反馈

- **URL**: `/api/match/feedback`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**（JSON Body）:

| 参数名            | 类型   | 必填 | 描述                        |
| :---------------- | :----- | :--- | :-------------------------- |
| demandId          | long   | 是   | 需求ID                      |
| selectedServiceId | long   | 是   | 被采纳的服务商ID（若无填0） |
| feedbackType      | string | 是   | `accept` 或 `ignore`        |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

------

## 五、信用评价体系模块

### 5.1 获取服务商信用分

- **URL**: `/api/credit/{serviceId}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `serviceId` (服务商ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
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

### 5.2 提交评价

- **URL**: `/api/evaluation/submit`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（制造企业）
- **请求参数**（JSON Body）:

| 参数名       | 类型    | 必填 | 描述                |
| :----------- | :------ | :--- | :------------------ |
| coopId       | long    | 是   | 合作记录ID          |
| score        | int     | 是   | 评分（1-5星）       |
| content      | string  | 否   | 评价内容            |
| is_anonymous | boolean | 否   | 是否匿名，默认false |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "evaluationId": 4001
  }
}
```

### 5.3 获取评价列表

- **URL**: `/api/evaluation/list/{serviceId}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `serviceId` (服务商ID)
- **请求参数**（可选分页）: `page`, `size`
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 20,
    "records": [
      {
        "id": 4001,
        "coopId": 5001,
        "score": 5,
        "content": "服务很好，专业高效",
        "isAnonymous": false,
        "createTime": "2026-03-06 15:20:00",
        "manufactureName": "深圳电子科技"
      }
    ]
  }
}
```

------

## 六、出海服务专区模块

### 6.1 获取出海服务列表

- **URL**: `/api/abroad/services`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（可选）
- **请求参数**（Query）:

| 参数名      | 类型   | 必填 | 描述                         |
| :---------- | :----- | :--- | :--------------------------- |
| serviceType | string | 否   | 服务类型筛选（如"国际认证"） |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
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

### 6.2 获取特定国家准入指南

- **URL**: `/api/abroad/country/{country}`
- **Method**: `GET`
- **路径参数**: `country` (国家，如 "美国")
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "country": "美国",
    "requirements": "FCC认证、UL认证...",
    "process": "1.提交申请 2.测试 3.发证",
    "documents": ["产品说明书", "电路图"]
  }
}
```

### 6.3 获取成功案例

- **URL**: `/api/abroad/cases`
- **Method**: `GET`
- **请求参数**（可选）:

| 参数名      | 类型   | 必填 | 描述     |
| :---------- | :----- | :--- | :------- |
| country     | string | 否   | 目标国家 |
| serviceType | string | 否   | 服务类型 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 7001,
      "title": "某电子公司CE认证成功案例",
      "companyName": "东莞电子",
      "companyType": "manufacture",
      "country": "欧盟",
      "serviceType": "CE认证",
      "description": "通过华测检测服务...",
      "coverImage": "https://...",
      "publishTime": "2026-02-10"
    }
  ]
}
```

------

## 七、数据可视化看板模块

### 7.1 获取统计卡片数据

- **URL**: `/api/dashboard/statistics`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（可选）
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "manufactureCount": 1250,
    "serviceCount": 380,
    "demandCount": 560,
    "cooperationCount": 890
  }
}
```

### 7.2 取热力图数据

- **URL**: `/api/dashboard/heatmap`
- **Method**: `GET`
- **请求参数**（可选）:

| 参数名 | 类型 | 必填 | 描述     |
| :----- | :--- | :--- | :------- |
| start  | date | 否   | 开始日期 |
| end    | date | 否   | 结束日期 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "region": "深圳", "value": 120 },
    { "region": "东莞", "value": 95 }
  ]
}
```

### 7.3 获取热门需求

- **URL**: `/api/dashboard/topDemands`
- **Method**: `GET`
- **请求参数**（Query）:

| 参数名 | 类型 | 必填 | 描述            |
| :----- | :--- | :--- | :-------------- |
| top    | int  | 否   | 返回数量，默认5 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "serviceType": "检测认证", "count": 45 },
    { "serviceType": "工业设计", "count": 32 }
  ]
}
```

### 7.4 获取网络关系数据

- **URL**: `/api/dashboard/network`
- **Method**: `GET`
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "nodes": [
      { "id": "m1001", "name": "深圳电子", "type": "manufacture" },
      { "id": "s2001", "name": "华测检测", "type": "service" }
    ],
    "links": [
      { "source": "m1001", "target": "s2001", "value": 3 }
    ]
  }
}
```