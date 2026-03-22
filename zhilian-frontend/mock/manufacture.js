// mock/manufacture.js
// 模拟制造企业数据（增强版，支持多企业归属）

let manufactureList = [
  // 企业 1001 - 属于用户 1001（已审核）
  {
    id: 1001,
    userId: 1001,
    companyName: "深圳电子科技",
    region: "深圳",
    address: "深圳市南山区",
    contactPerson: "张三",
    contactPhone: "13800138001",
    scale: "medium",
    employeeCount: 500,
    annualRevenue: 8000.0,
    productType: "PCB电路板",
    description: "专业PCB制造商",
    logo: "http://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar.png",
    establishedDate: "2010-05-01",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  // 企业 1002 - 属于用户 1002（已审核）
  {
    id: 1002,
    userId: 1002,
    companyName: "东莞精密机械",
    region: "东莞",
    address: "东莞市长安镇",
    contactPerson: "李四",
    contactPhone: "13900139002",
    scale: "large",
    employeeCount: 1200,
    annualRevenue: 15000.0,
    productType: "精密模具",
    description: "高端模具制造商",
    logo: "http://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar.png",
    establishedDate: "2008-08-08",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  // 企业 1003 - 属于用户 1003（待审核，用于测试无企业用户）
  {
    id: 1003,
    userId: 1003,
    companyName: "广州汽车配件",
    region: "广州",
    address: "广州市番禺区",
    contactPerson: "王五",
    contactPhone: "13700137003",
    scale: "medium",
    employeeCount: 600,
    annualRevenue: 9000.0,
    productType: "汽车配件",
    description: "汽车零部件供应商",
    logo: "http://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar.png",
    establishedDate: "2012-03-15",
    auditStatus: "pending",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  // 企业 1004 - 属于用户 1001（新增，已审核）
  {
    id: 1004,
    userId: 1001,
    companyName: "深圳华强电子",
    region: "深圳",
    address: "深圳市福田区",
    contactPerson: "赵六",
    contactPhone: "13500135004",
    scale: "small",
    employeeCount: 80,
    annualRevenue: 1200.0,
    productType: "消费电子",
    description: "电子产品分销商",
    logo: "http://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar.png",
    establishedDate: "2018-09-20",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  // 企业 1005 - 属于用户 1001（新增，已审核，但无诊断报告）
  {
    id: 1005,
    userId: 1001,
    companyName: "深圳赛格科技",
    region: "深圳",
    address: "深圳市龙岗区",
    contactPerson: "钱七",
    contactPhone: "13600136005",
    scale: "medium",
    employeeCount: 350,
    annualRevenue: 5000.0,
    productType: "半导体",
    description: "芯片设计公司",
    logo: "http://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar.png",
    establishedDate: "2015-11-02",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
];

export default [
  // 1.2.1 获取制造企业列表
  {
    url: "/api/manufacture/list",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10, region, scale, productType } = query;
      let filtered = manufactureList;

      
      // 公共列表仅返回审核通过的企业（前端还会再按userId过滤，但mock里先过滤掉未审核的）
      filtered = filtered.filter((item) => item.auditStatus === "approved");

      // 前端筛选
      if (region) {
        filtered = filtered.filter((item) => item.region === region);
      }
      if (scale) {
        filtered = filtered.filter((item) => item.scale === scale);
      }
      if (productType) {
        filtered = filtered.filter((item) =>
          item.productType.includes(productType)
        );
      }

      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered.slice(start, end).map(
        ({
          id,
          userId,
          companyName,
          region,
          scale,
          productType,
          contactPerson,
          contactPhone,
          auditStatus,
        }) => ({
          id,
          userId,
          companyName,
          region,
          scale,
          productType,
          contactPerson,
          contactPhone,
          auditStatus,
        })
      );

      return {
        code: 200,
        message: "success",
        data: {
          total: filtered.length,
          records,
        },
      };
    },
  },

  // 1.2.2 获取制造企业详情
  {
    url: "/api/manufacture/:id",
    method: "get",
    response: ({ query }) => {
      const id = parseInt(query.id);
      const item = manufactureList.find((item) => item.id === id);
      if (item) {
        return {
          code: 200,
          message: "success",
          data: item,
        };
      } else {
        return {
          code: 404,
          message: "企业不存在",
          data: null,
        };
      }
    },
  },

  // 1.2.3 新增制造企业
  {
    url: "/api/manufacture",
    method: "post",
    response: ({ body }) => {
      const newId = Math.max(...manufactureList.map((i) => i.id)) + 1;
      const now = new Date().toISOString().replace("T", " ").substring(0, 19);
      const newItem = {
        id: newId,
        ...body,
        auditStatus: "pending",
        auditRemark: null,
        auditTime: null,
        createTime: now,
        updateTime: now,
      };
      manufactureList.push(newItem);
      return {
        code: 200,
        message: "success",
        data: {
          id: newId,
          auditStatus: "pending",
        },
      };
    },
  },

  // 1.2.4 修改制造企业
  {
    url: "/api/manufacture/:id",
    method: "put",
    response: ({ query, body }) => {
      const id = parseInt(query.id);
      const index = manufactureList.findIndex((item) => item.id === id);
      if (index !== -1) {
        const now = new Date().toISOString().replace("T", " ").substring(0, 19);
        manufactureList[index] = {
          ...manufactureList[index],
          ...body,
          updateTime: now,
        };
        return {
          code: 200,
          message: "success",
          data: null,
        };
      } else {
        return {
          code: 404,
          message: "企业不存在",
          data: null,
        };
      }
    },
  },

  // 1.2.5 删除制造企业
  {
    url: "/api/manufacture/:id",
    method: "delete",
    response: ({ query }) => {
      const id = parseInt(query.id);
      const index = manufactureList.findIndex((item) => item.id === id);
      if (index !== -1) {
        manufactureList.splice(index, 1);
        return {
          code: 200,
          message: "success",
          data: null,
        };
      } else {
        return {
          code: 404,
          message: "企业不存在",
          data: null,
        };
      }
    },
  },
];