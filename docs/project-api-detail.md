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
    "token": "eyJhbGciOiJIUzI1NiIs..."
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

---

### 1.2 制造企业管理接口

#### 1.2.1 获取制造企业列表（公共列表）

- **URL**: `/api/manufacture/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需登录）
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
        "contactPhone": "13800138001",
        "auditStatus": "approved"
      }
    ]
  }
}
```

> **说明**：此接口仅返回审核状态为 `approved` 的企业，供所有用户浏览。

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
    "annualRevenue": 8000.0,
    "productType": "PCB",
    "description": "专业PCB制造商",
    "logo": "https://...",
    "establishedDate": "2010-05-01",
    "auditStatus": "approved",
    "auditRemark": null,
    "auditTime": null,
    "createTime": "2026-03-01 10:00:00",
    "updateTime": "2026-03-01 10:00:00"
  }
}
```

> **说明**：若企业审核未通过，仅创建者或管理员可查看详情；其他用户访问将返回404或权限错误。

#### 1.2.3 新增制造企业

- **URL**: `/api/manufacture`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需登录，且只能创建自己的企业）
- **请求参数**（JSON Body）:

| 参数名          | 类型    | 必填 | 描述           |
| :-------------- | :------ | :--- | :------------- |
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

> **注意**：新增企业后，审核状态默认为 `pending`，并关联当前登录用户。

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1010,
    "auditStatus": "pending"
  }
}
```

#### 1.2.4 修改制造企业信息

- **URL**: `/api/manufacture/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`（需具有修改权限：创建者或管理员）
- **路径参数**: `id` (企业ID)
- **请求参数**（JSON Body，全部可选，只传需要修改的字段）:

| 参数名          | 类型    | 必填 | 描述           |
| :-------------- | :------ | :--- | :------------- |
| companyName     | string  | 否   | 企业全称       |
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
  "data": null
}
```

#### 1.2.5 删除制造企业

- **URL**: `/api/manufacture/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`（需具有删除权限：创建者或管理员）
- **路径参数**: `id` (企业ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 1.3 服务商管理接口

#### 1.3.1 获取服务商列表（公共列表）

- **URL**: `/api/service-provider/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需登录）
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
        "qualification": "CNAS、CMA",
        "auditStatus": "approved"
      }
    ]
  }
}
```

> **说明**：此接口仅返回审核状态为 `approved` 的服务商。

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
    "auditStatus": "approved",
    "auditRemark": null,
    "auditTime": null,
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
- **请求头**: `Authorization: Bearer <token>`（需登录，且只能创建自己的服务商）
- **请求参数**（JSON Body）:

| 参数名          | 类型   | 必填 | 描述                               |
| :-------------- | :----- | :--- | :--------------------------------- |
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
    "id": 2010,
    "auditStatus": "pending"
  }
}
```

#### 1.3.4 修改服务商信息

- **URL**: `/api/service-provider/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`（需具有修改权限：创建者或管理员）
- **路径参数**: `id` (服务商ID)
- **请求参数**（JSON Body，全部可选，只传需要修改的字段）:

| 参数名          | 类型   | 必填 | 描述                               |
| :-------------- | :----- | :--- | :--------------------------------- |
| companyName     | string | 否   | 企业全称                           |
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
  "data": null
}
```

#### 1.3.5 删除服务商

- **URL**: `/api/service-provider/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`（需具有删除权限：创建者或管理员）
- **路径参数**: `id` (服务商ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

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
- **请求参数**（JSON Body，全部可选，只传需要修改的字段）:

| 参数名         | 类型   | 必填 | 描述              |
| :------------- | :----- | :--- | :---------------- |
| certName       | string | 否   | 证书名称          |
| certNo         | string | 否   | 证书编号          |
| issueAuthority | string | 否   | 发证机构          |
| issueDate      | date   | 否   | 发证日期          |
| expireDate     | date   | 否   | 有效期至          |
| status         | int    | 否   | 状态：0失效 1有效 |

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

---

### 1.5 辅助接口

#### 1.5.1 获取区域列表

- **URL**: `/api/common/regions`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    "深圳",
    "东莞",
    "惠州",
    "广州",
    "佛山",
    "中山",
    "珠海",
    "江门",
    "肇庆"
  ]
}
```

#### 1.5.2 获取企业规模枚举

- **URL**: `/api/common/scales`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
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
- **请求头**: `Authorization: Bearer <token>`
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

#### 1.5.4 OSS文件上传

- **URL**: `/api/common/upload`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`，Content-Type: `multipart/form-data`
- **请求参数**（Form Data）:

| 参数名 | 类型 | 必填 | 描述         |
| :----- | :--- | :--- | :----------- |
| file   | file | 是   | 要上传的文件 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileUrl": "https://oss.example.com/path/to/file.jpg"
  }
}
```

#### 1.5.5 OSS文件删除

- **URL**: `/api/common/delete`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`，Content-Type: `application/json`
- **请求参数**（JSON Body）:

| 参数名  | 类型   | 必填 | 描述                  |
| :------ | :----- | :--- | :-------------------- |
| fileUrl | string | 是   | 要删除的文件的完整URL |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 1.5.6 获取认证类型标签

- **URL**: `/api/common/certification-tags`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**: 无
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "CNAS认证",
      "category": "认证类型"
    },
    {
      "id": 2,
      "name": "CMA认证",
      "category": "认证类型"
    },
    {
      "id": 3,
      "name": "ISO9001",
      "category": "认证类型"
    }
  ]
}
```

> **说明**：从 `tag` 表中筛选 `category` 为 `'certification'` 的标签返回，`category` 字段在返回时转换为中文描述“认证类型”。示例数据仅为演示，实际返回数据库中所有认证类型标签。

#### 1.5.7 获取产品类型标签

- **URL**: `/api/common/product-tags`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**: 无
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 4,
      "name": "PCB电路板",
      "category": "产品类型"
    },
    {
      "id": 5,
      "name": "半导体芯片",
      "category": "产品类型"
    },
    {
      "id": 6,
      "name": "消费电子",
      "category": "产品类型"
    }
  ]
}
```

> **说明**：从 `tag` 表中筛选 `category` 为 `'product'` 的标签返回，`category` 字段在返回时转换为中文描述“产品类型”。示例数据仅为演示，实际返回数据库中所有产品类型标签。

#### 1.5.8 获取其他类型标签

- **URL**: `/api/common/general-tags`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**: 无
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 7,
      "name": "热门推荐",
      "category": "其他类型"
    },
    {
      "id": 8,
      "name": "新品上市",
      "category": "其他类型"
    },
    {
      "id": 9,
      "name": "特惠活动",
      "category": "其他类型"
    }
  ]
}
```

> **说明**：从 `tag` 表中筛选 `category` 为 `'general'` 的标签返回，`category` 字段在返回时转换为中文描述“其他类型”。示例数据仅为演示，实际返回数据库中所有其他类型标签。

#### 1.5.9 获取标签类别选项

- **URL**: `/api/common/tag-categories`
- **Method**: `GET`
- **请求头**: 无需认证（公开接口）

- **请求参数**: 无
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "value": "service",
      "label": "服务类型"
    },
    {
      "value": "certification",
      "label": "认证类型"
    },
    {
      "value": "product",
      "label": "产品类型"
    },
    {
      "value": "rests",
      "label": "其他类型"
    }
  ]
}
```

---

### 1.6 标签管理接口

#### 1.6.1 获取标签列表

- **URL**: `/api/tag/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**（Query）:

| 参数名   | 类型   | 必填 | 描述                 |
| :------- | :----- | :--- | :------------------- |
| page     | int    | 否   | 页码，默认1          |
| size     | int    | 否   | 每页条数，默认10     |
| name     | string | 否   | 标签名称（模糊匹配） |
| category | string | 否   | 类别筛选             |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "records": [
      {
        "id": 1,
        "name": "CNAS认证",
        "category": "certification",
        "description": "中国合格评定国家认可委员会认证",
        "createTime": "2026-03-01 10:00:00",
        "updateTime": "2026-03-01 10:00:00"
      },
      {
        "id": 2,
        "name": "工业设计",
        "category": "service",
        "description": "产品外观、结构设计服务",
        "createTime": "2026-03-01 10:00:00",
        "updateTime": "2026-03-01 10:00:00"
      }
    ]
  }
}
```

#### 1.6.2 获取标签详情

- **URL**: `/api/tag/{id}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (标签ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "CNAS认证",
    "category": "certification",
    "description": "中国合格评定国家认可委员会认证",
    "createTime": "2026-03-01 10:00:00",
    "updateTime": "2026-03-01 10:00:00"
  }
}
```

#### 1.6.3 新增标签

- **URL**: `/api/tag`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**（JSON Body）:

| 参数名      | 类型   | 必填 | 描述     |
| :---------- | :----- | :--- | :------- |
| name        | string | 是   | 标签名称 |
| category    | string | 否   | 类别     |
| description | string | 否   | 标签说明 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101
  }
}
```

#### 1.6.4 修改标签

- **URL**: `/api/tag/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (标签ID)
- **请求参数**（JSON Body）: 同新增接口字段（全部可选，只传需要修改的字段）

| 参数名      | 类型   | 必填 | 描述     |
| :---------- | :----- | :--- | :------- |
| name        | string | 否   | 标签名称 |
| category    | string | 否   | 类别     |
| description | string | 否   | 标签说明 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 1.6.5 删除标签

- **URL**: `/api/tag/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (标签ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 1.7 个人企业管理接口

#### 1.7.1 获取个人制造企业列表

- **URL**: `/api/enterprise/manufacture/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需登录）
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
    "total": 5,
    "records": [
      {
        "id": 1001,
        "companyName": "深圳电子科技",
        "region": "深圳",
        "scale": "medium",
        "productType": "PCB",
        "contactPerson": "张三",
        "contactPhone": "13800138001",
        "auditStatus": "approved",
        "auditRemark": null, // 审核通过时无意见
        "auditTime": "2026-03-17 15:44:52",
        "createTime": "2026-03-01 10:00:00"
      },
      {
        "id": 1002,
        "companyName": "东莞精密制造",
        "region": "东莞",
        "scale": "small",
        "productType": "精密零部件",
        "contactPerson": "李四",
        "contactPhone": "13900139002",
        "auditStatus": "rejected",
        "auditRemark": "营业执照不清晰，请重新上传", // 驳回时填写意见
        "auditTime": "2026-03-18 09:30:00",
        "createTime": "2026-03-02 14:20:00"
      }
    ]
  }
}
```

> **说明**：此接口仅返回当前登录用户创建的制造企业（包含所有审核状态）。

#### 1.7.2 获取个人服务商列表

- **URL**: `/api/enterprise/service/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需登录）
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
    "total": 3,
    "records": [
      {
        "id": 2001,
        "companyName": "华测检测",
        "region": "深圳",
        "serviceType": "检测认证",
        "contactPerson": "王五",
        "contactPhone": "13700137003",
        "auditStatus": "pending",
        "auditRemark": null,
        "auditTime": null,
        "createTime": "2026-03-02 14:00:00"
      },
      {
        "id": 2002,
        "companyName": "SGS通标",
        "region": "广州",
        "serviceType": "国际认证",
        "contactPerson": "赵六",
        "contactPhone": "13600136004",
        "auditStatus": "rejected",
        "auditRemark": "资质证书过期，请更新后重新提交",
        "auditTime": "2026-03-17 11:20:00",
        "createTime": "2026-03-03 09:15:00"
      }
    ]
  }
}
```

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

- **URL**: `/api/diagnosis/{id}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需制造企业角色）
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

### 2.3 获取企业最新诊断报告

- **URL**: `/api/diagnosis/latest`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需制造企业角色）
- **请求参数**（Query）:

| 参数名 | 类型 | 必填 | 描述       |
| :----- | :--- | :--- | :--------- |
| manuId | long | 是   | 制造企业ID |

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

---

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
- **请求头**: `Authorization: Bearer <token>`（可选）
- **路径参数**:

| 参数名 | 类型   | 必填 | 描述               |
| :----- | :----- | :--- | :----------------- |
| region | string | 是   | 区域名称，如“深圳” |

- **请求参数**（Query，可选）:

| 参数名  | 类型 | 必填 | 描述                                                   |
| :------ | :--- | :--- | :----------------------------------------------------- |
| year    | int  | 否   | 年份，如2026，与 `quarter` 或 `month` 配合使用         |
| quarter | int  | 否   | 季度（1-4），与 `year` 配合使用，此时不能传 `month`    |
| month   | int  | 否   | 月份（1-12），与 `year` 配合使用，此时不能传 `quarter` |

> **说明**：
>
> - 若同时提供了 `year` 和 `quarter`，则返回该区域指定季度的指数。
> - 若同时提供了 `year` 和 `month`，则返回该区域指定月份的指数。
> - 若未提供任何时间参数，则返回该区域最新一期的指数。
> - 不支持同时提供 `quarter` 和 `month`。

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101,
    "region": "深圳",
    "year": 2026,
    "periodType": "quarter",
    "periodValue": 1,
    "coopDensity": 0.85,
    "serviceRate": 0.72,
    "crossRate": 0.45,
    "totalIndex": 75.8,
    "calcTime": "2026-04-01 00:00:00",
    "createTime": "2026-04-01 00:10:00",
    "updateTime": "2026-04-01 00:10:00"
  }
}
```

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

---

## 四、智能供需匹配模块

### 4.1 发布需求

- **URL**: `/api/demand/publish`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（制造企业）
- **请求参数**（JSON Body）:

| 参数名         | 类型     | 必填 | 描述         |
| :------------- | :------- | :--- | :----------- |
| manuId         | long     | 是   | 制造企业ID   |
| title          | string   | 是   | 需求标题     |
| description    | string   | 否   | 详细描述     |
| expectedBudget | decimal  | 否   | 预算（万元） |
| deadline       | date     | 否   | 期望完成日期 |
| tags           | String[] | 否   | 标签ID列表   |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "demandId": 3001,
    "auditStatus": "pending"
  }
}
```

---

### 4.2 需求编辑

- **URL**: `/api/demand/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`（需求发布者）
- **路径参数**: `id` (需求ID)
- **请求参数**（JSON Body，全部可选）:

| 参数名         | 类型    | 必填 | 描述         |
| :------------- | :------ | :--- | :----------- |
| title          | string  | 否   | 需求标题     |
| description    | string  | 否   | 详细描述     |
| expectedBudget | decimal | 否   | 预算（万元） |
| deadline       | date    | 否   | 期望完成日期 |
| tags           | long[]  | 否   | 标签ID列表   |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

> **业务逻辑**：
>
> - 仅允许编辑 `status` 为 `draft` 或 `published` 的需求。
> - 更新字段，标签先删后增。

---

### 4.3需求删除

- **URL**: `/api/demand/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`（需求发布者）
- **路径参数**: `id` (需求ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

> **业务逻辑**：
>
> - 仅允许删除 `status` 为 `draft` 或 `published` 的需求。
> - 逻辑删除（设置 `deleted` 字段）。

---

### 4.4 获取我的需求列表

- **URL**: `/api/demand/my-list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（制造企业）
- **请求参数**（Query）:

| 参数名 | 类型   | 必填 | 描述                                             |
| :----- | :----- | :--- | :----------------------------------------------- |
| page   | int    | 否   | 页码，默认1                                      |
| size   | int    | 否   | 每页条数，默认10                                 |
| status | string | 否   | 筛选状态：`draft`/`published`/`matched`/`closed` |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "records": [
      {
        "id": 3001,
        "title": "寻求PCB设计服务",
        "description": "...",
        "expectedBudget": 10.0,
        "deadline": "2026-06-01",
        "status": "published",
        "createTime": "2026-03-01 10:00:00",
        "matchedServiceProvider": null,  // 若状态为matched，返回服务商信息
        "tags": [
          { "id": 1, "name": "PCB设计" }
        ]
      }
    ]
  }
}
```

---

### 4.5 获取合作市场需求列表

- **URL**: `/api/demand/market`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（服务商、管理员）
- **请求参数**（Query）:

| 参数名            | 类型    | 必填 | 描述                 |
| :---------------- | :------ | :--- | :------------------- |
| page              | int     | 否   | 页码，默认1          |
| size              | int     | 否   | 每页条数，默认10     |
| keyword           | string  | 否   | 标题关键词模糊搜索   |
| tagIds            | string  | 否   | 标签ID，逗号分隔     |
| expectedBudgetMin | decimal | 否   | 预算最小值（万元）   |
| expectedBudgetMax | decimal | 否   | 预算最大值（万元）   |
| deadlineStart     | date    | 否   | 期望完成日期开始范围 |
| deadlineEnd       | date    | 否   | 期望完成日期结束范围 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "records": [
      {
        "id": 3001,
        "title": "寻求PCB设计服务",
        "description": "需要专业PCB设计公司...",
        "expectedBudget": 10.0,
        "deadline": "2026-06-01",
        "createTime": "2026-03-01 10:00:00",
        "manufacture": {
          "id": 1001,
          "companyName": "深圳电子科技",
          "region": "深圳",
          "contactPerson": "张三",
          "contactPhone": "13800138001"
        },
        "tags": [
          { "id": 1, "name": "PCB设计" },
          { "id": 2, "name": "高速电路" }
        ]
      }
    ]
  }
}
```

> **说明**：仅返回 `audit_status='approved'` 且 `status='published'` 的需求。

------

### 4.6 服务商接取需求

- **URL**: `/api/demand/accept`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（服务商角色）
- **请求参数**（JSON Body）:

| 参数名    | 类型 | 必填 | 描述                     |
| :-------- | :--- | :--- | :----------------------- |
| demandId  | long | 是   | 需求ID                   |
| serviceId | long | 是   | 接取该需求的服务商企业ID |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "cooperationId": 5001
  }
}
```

> **业务逻辑**：
>
> 1. 校验当前用户（从token获取）是否拥有该`serviceId`对应的服务商企业（即`service_provider.id = serviceId`且`user_id = 当前用户ID`），且企业审核状态为`approved`。
> 2. 校验需求存在且`status='published'`、`audit_status='approved'`。
> 3. 使用行锁（`SELECT ... FOR UPDATE`）锁定需求记录，防止并发接单。
> 4. 更新需求`status='matched'`。
> 5. 插入合作记录`cooperation`：
>    - `manu_id`：需求表中的`manu_id`
>    - `service_id`：传入的`serviceId`
>    - `demand_id`：需求ID
>    - `status`：`ongoing`
> 6. 返回合作记录ID。

------

### 4.7 取消合作

- **URL**: `/api/cooperation/cancel/{id}`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（合作双方）
- **路径参数**: `id` (合作记录ID)
- **请求参数**（JSON Body，可选）:

| 参数名 | 类型   | 必填 | 描述     |
| :----- | :----- | :--- | :------- |
| reason | string | 否   | 取消原因 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

> **业务逻辑**：
>
> 1. 校验合作记录存在且状态为 `ongoing`。
> 2. 校验当前用户是合作双方之一（制造企业或服务商）。
> 3. 更新合作记录 `status='cancelled'`。
> 4. 将关联的需求状态恢复为 `published`。

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

| 参数名      | 类型    | 必填 | 描述                |
| :---------- | :------ | :--- | :------------------ |
| coopId      | long    | 是   | 合作记录ID          |
| score       | int     | 是   | 评分（1-5星）       |
| content     | string  | 否   | 评价内容            |
| isAnonymous | boolean | 否   | 是否匿名，默认false |

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
- **请求参数**: 

| 参数名 | 类型 | 必填 | 描述             |
| :----- | :--- | :--- | :--------------- |
| page   | int  | 否   | 页码，默认1      |
| size   | int  | 否   | 每页条数，默认10 |

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

### 5.4 获取我的合作记录列表

- **URL**: `/api/cooperation/my-list`

- **Method**: `GET`

- **请求头**: `Authorization: Bearer <token>`（需登录）

- **请求参数**（Query）:

  | 参数名       | 类型   | 必填 | 描述                                                         |
  | :----------- | :----- | :--- | :----------------------------------------------------------- |
  | status       | string | 否   | 合作状态筛选：`ongoing`/`completed`/`cancelled`，默认返回所有 |
  | enterpriseId | long   | 否   | 企业ID（制造企业ID 或 服务商ID），不传时自动关联当前用户的默认企业 |
  | page         | int    | 否   | 页码，默认1                                                  |
  | size         | int    | 否   | 每页条数，默认10                                             |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 20,
    "records": [
      {
        "id": 5001,
        "opponentName": "华测检测",              // 合作对方企业名称
        "demandTitle": "寻求PCB设计服务",       // 需求标题
        "amount": 10.0,                         // 合作金额（万元）
        "startDate": "2026-03-01",               // 开始日期
        "endDate": "2026-06-30",                 // 结束日期
        "status": "ongoing",                    // 合作状态
        "createTime": "2026-03-01 10:00:00",    // 创建时间
        "hasEvaluated": false                   // 当前用户是否已评价
      }
    ]
  }
}
```

> **说明**：
> - 若传入 `enterpriseId`，后端需校验该企业是否属于当前用户（通过 `manufacture` 或 `service_provider` 表的 `user_id` 字段）。
> - 根据企业ID对应的类型（制造企业/服务商），自动使用 `manu_id` 或 `service_id` 进行合作记录查询。
> - 若未传入 `enterpriseId`，后端根据当前用户的角色（从token中获取）自动选择其关联的第一个企业（若同一用户有多个同类型企业，可返回默认企业，或由业务逻辑决定，推荐返回最近创建或审核通过的企业）。

### 5.5 获取合作记录详情

- **URL**: `/api/cooperation/{id}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`
- **路径参数**: `id` (合作记录ID)
- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 5001,
    "manuId": 1001,
    "manuName": "深圳电子科技",
    "serviceId": 2001,
    "serviceName": "华测检测",
    "demandId": 3001,
    "demandTitle": "寻求PCB设计服务",
    "demandDescription": "需要专业PCB设计公司，有高速PCB设计经验者优先。",  // 需求详情
    "amount": 10.0,
    "startDate": "2026-03-01",
    "endDate": "2026-06-30",
    "description": "合作内容简述：提供PCB设计服务...",                // 合作内容描述
    "status": "ongoing",
    "createTime": "2026-03-01 10:00:00",
    "hasEvaluated": false
  }
}
```

---

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

---

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
    "links": [{ "source": "m1001", "target": "s2001", "value": 3 }]
  }
}
```

---

## 八、管理员后台管理模块

### 8.1 管理员用户管理接口

#### 8.1.1 获取用户列表（管理员）

- **URL**: `/api/admin/user/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **请求参数**（Query）:

| 参数名  | 类型   | 必填 | 描述                                             |
| :------ | :----- | :--- | :----------------------------------------------- |
| page    | int    | 否   | 页码，默认1                                      |
| size    | int    | 否   | 每页条数，默认10                                 |
| role    | string | 否   | 角色筛选：`manufacture`/`service`/`park`/`admin` |
| status  | int    | 否   | 状态筛选：0禁用 1正常                            |
| keyword | string | 否   | 用户名/手机号模糊搜索                            |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 150,
    "records": [
      {
        "id": 1001,
        "username": "tech_company",
        "role": "manufacture",
        "phone": "13800138001",
        "email": "test@example.com",
        "status": 1,
        "createTime": "2026-03-01 10:00:00"
      }
    ]
  }
}
```

#### 8.1.2 获取用户详情（管理员）

- **URL**: `/api/admin/user/{id}`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 用户ID |

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
    "createTime": "2026-03-01 10:00:00",
    "manufactureInfo": {
      // 如果是制造企业，返回关联的企业信息
      "id": 2001,
      "companyName": "深圳电子科技",
      "region": "深圳",
      "scale": "medium"
    }
  }
}
```

#### 8.1.3 修改用户状态（启用/禁用）

- **URL**: `/api/admin/user/status/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 用户ID |

- **请求参数**（JSON Body）:

| 参数名 | 类型 | 必填 | 描述        |
| :----- | :--- | :--- | :---------- |
| status | int  | 是   | 0禁用 1正常 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 8.1.4 重置用户密码（管理员）

- **URL**: `/api/admin/user/reset-password/{id}`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 用户ID |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "newPassword": "temp123456" // 系统生成的临时密码
  }
}
```

---

### 8.2 需求审核接口

#### 8.2.1 获取待审核需求列表

- **URL**: `/api/admin/demand/pending`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **请求参数**（Query）:

| 参数名 | 类型 | 必填 | 描述             |
| :----- | :--- | :--- | :--------------- |
| page   | int  | 否   | 页码，默认1      |
| size   | int  | 否   | 每页条数，默认10 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 20,
    "records": [
      {
        "id": 3001,
        "manuId": 1001,
        "manuName": "深圳电子科技",
        "title": "寻求PCB设计服务",
        "description": "需要专业PCB设计公司，有高速PCB设计经验者优先。",
        "expectedBudget": 10.0,
        "deadline": "2026-04-01",
        "createTime": "2026-03-07 14:30:00",
        "tags": [
          { "id": 1, "name": "PCB设计" },
          { "id": 2, "name": "高速电路" }
        ]
      }
    ]
  }
}
```

#### 8.2.2 审核需求（通过/驳回）

- **URL**: `/api/admin/demand/approve/{id}`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 需求ID |

- **请求参数**（JSON Body）:

| 参数名 | 类型   | 必填 | 描述                              |
| :----- | :----- | :--- | :-------------------------------- |
| status | string | 是   | `approved` 通过 / `rejected` 驳回 |
| remark | string | 否   | 审核意见（驳回时建议填写）        |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 8.3 区域指数管理接口

#### 8.3.1 获取区域指数列表（管理员）

- **URL**: `/api/admin/region-index/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **请求参数**（Query）:

| 参数名 | 类型   | 必填 | 描述             |
| :----- | :----- | :--- | :--------------- |
| page   | int    | 否   | 页码，默认1      |
| size   | int    | 否   | 每页条数，默认10 |
| region | string | 否   | 区域筛选         |
| year   | int    | 否   | 年份筛选         |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "records": [
      {
        "id": 101,
        "region": "深圳",
        "year": 2026,
        "quarter": 1,
        "coopDensity": 0.85,
        "serviceRate": 0.72,
        "crossRate": 0.45,
        "totalIndex": 75.8,
        "calcTime": "2026-04-01 00:00:00",
        "createTime": "2026-04-01 00:10:00",
        "updateTime": "2026-04-01 00:10:00"
      }
    ]
  }
}
```

#### 8.3.2 新增区域指数

- **URL**: `/api/admin/region-index`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **请求参数**（JSON Body）:

| 参数名      | 类型    | 必填 | 说明               |
| :---------- | :------ | :--- | :----------------- |
| region      | string  | 是   | 区域，如“深圳”     |
| year        | int     | 是   | 年份，如2026       |
| quarter     | int     | 是   | 季度（1-4）        |
| coopDensity | decimal | 否   | 合作密度，如0.85   |
| serviceRate | decimal | 否   | 服务渗透率，如0.72 |
| crossRate   | decimal | 否   | 跨域协同度，如0.45 |
| totalIndex  | decimal | 否   | 综合得分，如75.8   |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 102
  }
}
```

#### 8.3.3 修改区域指数

- **URL**: `/api/admin/region-index/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 记录ID |

- **请求参数**（JSON Body，全部可选，只需传需要修改的字段）:

| 参数名      | 类型    | 必填 | 说明       |
| :---------- | :------ | :--- | :--------- |
| region      | string  | 否   | 区域       |
| year        | int     | 否   | 年份       |
| quarter     | int     | 否   | 季度       |
| coopDensity | decimal | 否   | 合作密度   |
| serviceRate | decimal | 否   | 服务渗透率 |
| crossRate   | decimal | 否   | 跨域协同度 |
| totalIndex  | decimal | 否   | 综合得分   |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 8.3.4 删除区域指数

- **URL**: `/api/admin/region-index/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 记录ID |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 8.4 出海案例管理接口

#### 8.4.1 获取出海案例列表（管理员）

- **URL**: `/api/admin/abroad-case/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **请求参数**（Query）:

| 参数名  | 类型   | 必填 | 描述              |
| :------ | :----- | :--- | :---------------- |
| page    | int    | 否   | 页码，默认1       |
| size    | int    | 否   | 每页条数，默认10  |
| country | string | 否   | 目标国家筛选      |
| status  | int    | 否   | 状态：0草稿 1发布 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 30,
    "records": [
      {
        "id": 7001,
        "title": "某电子公司CE认证成功案例",
        "companyName": "东莞电子",
        "companyType": "manufacture",
        "country": "欧盟",
        "serviceType": "CE认证",
        "description": "通过华测检测服务，顺利获得CE认证，产品成功进入欧洲市场。",
        "coverImage": "https://zhilian-cert.oss-cn-shenzhen.aliyuncs.com/cases/2026/03/abc.jpg",
        "publishTime": "2026-02-10 10:00:00",
        "status": 1,
        "createTime": "2026-02-10 09:00:00",
        "updateTime": "2026-02-10 09:00:00"
      }
    ]
  }
}
```

#### 8.4.2 新增出海案例

- **URL**: `/api/admin/abroad-case`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色），Content-Type: `multipart/form-data`
- **请求参数**（Form Data）:

| 参数名      | 类型   | 必填 | 描述                        |
| :---------- | :----- | :--- | :-------------------------- |
| title       | string | 是   | 案例标题                    |
| companyName | string | 是   | 企业名称                    |
| companyType | string | 是   | `manufacture` 或 `service`  |
| country     | string | 是   | 目标国家                    |
| serviceType | string | 是   | 涉及服务类型                |
| description | string | 是   | 案例详情                    |
| coverImage  | file   | 否   | 封面图片文件（支持jpg/png） |
| status      | int    | 否   | 0草稿 1发布，默认1          |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 7002
  }
}
```

#### 8.4.3 修改出海案例

- **URL**: `/api/admin/abroad-case/{id}`
- **Method**: `PUT`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色），Content-Type: `multipart/form-data`
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 案例ID |

- **请求参数**（Form Data，全部可选，只需传需要修改的字段）:

| 参数名      | 类型   | 必填 | 描述           |
| :---------- | :----- | :--- | :------------- |
| title       | string | 否   | 案例标题       |
| companyName | string | 否   | 企业名称       |
| companyType | string | 否   | 企业类型       |
| country     | string | 否   | 目标国家       |
| serviceType | string | 否   | 服务类型       |
| description | string | 否   | 案例详情       |
| coverImage  | file   | 否   | 新封面图片文件 |
| status      | int    | 否   | 状态           |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 8.4.4 删除出海案例

- **URL**: `/api/admin/abroad-case/{id}`
- **Method**: `DELETE`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 案例ID |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 8.5 操作日志接口

#### 8.5.1 获取操作日志列表

- **URL**: `/api/admin/log/list`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **请求参数**（Query）:

| 参数名    | 类型   | 必填 | 描述                               |
| :-------- | :----- | :--- | :--------------------------------- |
| page      | int    | 否   | 页码，默认1                        |
| size      | int    | 否   | 每页条数，默认10                   |
| username  | string | 否   | 操作人用户名（模糊匹配）           |
| operation | string | 否   | 操作类型（如“用户登录”）           |
| startTime | string | 否   | 开始时间，格式 yyyy-MM-dd HH:mm:ss |
| endTime   | string | 否   | 结束时间，格式同上                 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 500,
    "records": [
      {
        "id": 10001,
        "userId": 1001,
        "username": "admin",
        "operation": "用户登录",
        "params": "{}",
        "result": "成功",
        "ip": "192.168.1.1",
        "createTime": "2026-03-12 09:30:00"
      },
      {
        "id": 10002,
        "userId": 1002,
        "username": "tech_company",
        "operation": "修改密码",
        "params": "{\"userId\":1001}",
        "result": "成功",
        "ip": "192.168.1.2",
        "createTime": "2026-03-12 10:15:00"
      }
    ]
  }
}
```

### 8.6 企业审核接口

#### 8.6.1 获取待审核企业列表

- **URL**: `/api/admin/enterprise/pending`
- **Method**: `GET`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **请求参数**（Query）:

| 参数名 | 类型 | 必填 | 描述             |
| :----- | :--- | :--- | :--------------- |
| page   | int  | 否   | 页码，默认1      |
| size   | int  | 否   | 每页条数，默认10 |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "records": [
      {
        "id": 2001,
        "type": "manufacture",
        "companyName": "深圳电子科技",
        "region": "深圳",
        "contactPerson": "张三",
        "contactPhone": "13800138001",
        "auditStatus": "pending",
        "createTime": "2026-03-13 10:00:00"
      },
      {
        "id": 3001,
        "type": "service",
        "companyName": "华测检测",
        "region": "深圳",
        "contactPerson": "王五",
        "contactPhone": "13700137003",
        "auditStatus": "pending",
        "createTime": "2026-03-13 11:00:00"
      }
    ]
  }
}
```

#### 8.6.2 审核企业

- **URL**: `/api/admin/enterprise/approve/{id}`
- **Method**: `POST`
- **请求头**: `Authorization: Bearer <token>`（需 admin 角色）
- **路径参数**:

| 参数名 | 类型 | 必填 | 描述   |
| :----- | :--- | :--- | :----- |
| id     | long | 是   | 企业ID |

- **请求参数**（JSON Body）:

| 参数名 | 类型   | 必填 | 描述                                 |
| :----- | :----- | :--- | :----------------------------------- |
| type   | string | 是   | 企业类型：`manufacture` 或 `service` |
| status | string | 是   | `approved` 通过 / `rejected` 驳回    |
| remark | string | 否   | 审核意见（驳回时建议填写）           |

- **返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```
