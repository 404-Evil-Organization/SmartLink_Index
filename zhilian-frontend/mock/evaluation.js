export default [
  // 5.2 提交评价
  {
    url: "/api/evaluation/submit",
    method: "post",
    response: ({ body }) => {
      // 模拟成功，返回随机评价ID
      return {
        code: 200,
        message: "success",
        data: {
          evaluationId: Math.floor(Math.random() * 10000) + 4000,
        },
      };
    },
  },
  // 5.3 获取评价列表
  {
    url: "/api/evaluation/list/:serviceId",
    method: "get",
    response: ({ params, query }) => {
      const { serviceId } = params;
      const { page = 1, size = 10 } = query;
      // 模拟评价数据，可根据 serviceId 过滤（这里简化为不过滤）
      const allEvaluations = [
        {
          id: 4001,
          coopId: 5001,
          score: 5,
          content: "服务很好，专业高效",
          isAnonymous: false,
          createTime: "2026-03-06 15:20:00",
          manufactureName: "深圳电子科技",
        },
        {
          id: 4002,
          coopId: 5002,
          score: 4,
          content: "服务态度好，但交付稍慢",
          isAnonymous: true,
          createTime: "2026-03-07 10:15:00",
          manufactureName: "匿名用户",
        },
        {
          id: 4003,
          coopId: 5003,
          score: 5,
          content: "设计超出预期，非常满意",
          isAnonymous: false,
          createTime: "2026-03-08 09:45:00",
          manufactureName: "广州电子",
        },
        {
          id: 4004,
          coopId: 5004,
          score: 3,
          content: "沟通不够顺畅",
          isAnonymous: false,
          createTime: "2026-03-09 14:20:00",
          manufactureName: "深圳电子科技",
        },
      ];
      const start = (page - 1) * size;
      const end = start + size;
      const records = allEvaluations.slice(start, end);
      return {
        code: 200,
        message: "success",
        data: {
          total: allEvaluations.length,
          records,
        },
      };
    },
  },
];
