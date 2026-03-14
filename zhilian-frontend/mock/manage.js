// mock/manufacture.js
// 模拟制造企业数据
let manufactureList = [
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
    productType: "PCB",
    description: "专业PCB制造商",
    logo: "https://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar.png?Expires=1773302233&OSSAccessKeyId=TMP.3Ko3D4CEunQzVEaENPJkqpo2RPcm2AUkRCsHH8SFsBFSKTeTYWtqjZ1DZPrAZxfZkmfVZRHTJRJXVNBZJHS86ZZzG5zs7i&Signature=lFXGhCZ4RN7kp1Y60fdWnW0XjlA%3D",
    establishedDate: "2010-05-01",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
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
    logo: "https://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar2.png",
    establishedDate: "2008-08-08",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
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
    logo: "https://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar3.png",
    establishedDate: "2012-03-15",
    auditStatus: "pending", // 新增数据为待审核
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
];

export default [
  // 1.2.1 获取制造企业列表（公共列表）
  {
    url: "/api/manufacture/list",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10, region, scale, productType } = query;
      let filtered = manufactureList;

      // 公共列表仅返回审核通过的企业
      filtered = filtered.filter((item) => item.auditStatus === "approved");

      if (region) {
        filtered = filtered.filter((item) => item.region.includes(region));
      }
      if (scale) {
        filtered = filtered.filter((item) => item.scale === scale);
      }
      if (productType) {
        filtered = filtered.filter((item) =>
          item.productType.includes(productType),
        );
      }

      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered
        .slice(start, end)
        .map(
          ({
            id,
            companyName,
            region,
            scale,
            productType,
            contactPerson,
            contactPhone,
            auditStatus,
          }) => ({
            id,
            companyName,
            region,
            scale,
            productType,
            contactPerson,
            contactPhone,
            auditStatus,
          }),
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
        auditStatus: "pending", // 新增默认待审核
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
        // 更新时，审核状态可能重置为待审核（根据业务需求决定，此处保持原有状态或按需修改）
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
