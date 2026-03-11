export default [
  {
    url: "/api/manufacture/list",
    method: "get",
    response: () => {
      return {
        code: 200,
        message: "success",
        data: {
          total: 200,
          records: [
            {
              id: 1001,
              companyName: "深圳电子科技",
              region: "深圳",
              scale: "medium",
              productType: "PCB",
              contactPerson: "张三",
              contactPhone: "13800138001",
            },
          ],
        },
      };
    },
  },
  {
    url: `/api/manufacture/:id`,
    method: "get",
    response: () => {
      return {
        code: 200,
        message: "success",
        data: {
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
          logo: "https://...",
          establishedDate: "2010-05-01",
          createTime: "2026-03-01 10:00:00",
          updateTime: "2026-03-01 10:00:00",
        },
      };
    },
  },
];
