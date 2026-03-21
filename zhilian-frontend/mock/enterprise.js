export default [
  // 1.7.1 获取个人制造企业列表
  {
    url: "/api/enterprise/manufacture/list",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10 } = query;
      let records = [
        {
          id: 1001,
          companyName: "深圳电子科技",
          region: "深圳",
          scale: "medium",
          productType: "PCB",
          contactPerson: "张三",
          contactPhone: "13800138001",
          auditStatus: "approved",
          auditRemark: null,
          auditTime: "2026-03-17 15:44:52",
          createTime: "2026-03-01 10:00:00",
        },
        {
          id: 1002,
          companyName: "东莞精密制造",
          region: "东莞",
          scale: "small",
          productType: "精密零部件",
          contactPerson: "李四",
          contactPhone: "13900139002",
          auditStatus: "rejected",
          auditRemark: "营业执照不清晰，请重新上传",
          auditTime: "2026-03-18 09:30:00",
          createTime: "2026-03-02 14:20:00",
        },
        {
          id: 1003,
          companyName: "广州电子科技",
          region: "广州",
          scale: "large",
          productType: "消费电子",
          contactPerson: "王五",
          contactPhone: "13600136003",
          auditStatus: "approved",
          auditRemark: null,
          auditTime: "2026-03-19 10:00:00",
          createTime: "2026-03-05 09:00:00",
        },
      ];
      const start = (page - 1) * size;
      const end = start + size;
      return {
        code: 200,
        message: "success",
        data: {
          total: records.length,
          records: records.slice(start, end),
        },
      };
    },
  },
  // 1.7.2 获取个人服务商列表
  {
    url: "/api/enterprise/service/list",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10 } = query;
      let records = [
        {
          id: 2001,
          companyName: "华测检测",
          region: "深圳",
          serviceType: "检测认证",
          contactPerson: "王五",
          contactPhone: "13700137003",
          auditStatus: "approved",
          auditRemark: null,
          auditTime: "2026-03-17 15:44:52",
          createTime: "2026-03-02 14:00:00",
        },
        {
          id: 2002,
          companyName: "SGS通标",
          region: "广州",
          serviceType: "国际认证",
          contactPerson: "赵六",
          contactPhone: "13600136004",
          auditStatus: "rejected",
          auditRemark: "资质证书过期，请更新后重新提交",
          auditTime: "2026-03-17 11:20:00",
          createTime: "2026-03-03 09:15:00",
        },
        {
          id: 2003,
          companyName: "东莞设计中心",
          region: "东莞",
          serviceType: "工业设计",
          contactPerson: "陈七",
          contactPhone: "13500135005",
          auditStatus: "approved",
          auditRemark: null,
          auditTime: "2026-03-18 14:00:00",
          createTime: "2026-03-04 10:30:00",
        },
      ];
      const start = (page - 1) * size;
      const end = start + size;
      return {
        code: 200,
        message: "success",
        data: {
          total: records.length,
          records: records.slice(start, end),
        },
      };
    },
  },
];
