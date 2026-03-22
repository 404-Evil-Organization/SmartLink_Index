export default [
  {
    url: "/api/evaluation/list/:serviceId",
    method: "get",
    response: ({ params, query }) => {
      // 兼容从 params 或 query 获取 serviceId
      const serviceId = parseInt(params?.serviceId || query?.serviceId);
      const { page = 1, size = 10 } = query;

      const allEvals = [
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
          content: "响应迅速，技术能力强",
          isAnonymous: true,
          createTime: "2026-03-07 09:10:00",
          manufactureName: "东莞精密制造",
        },
        {
          id: 4003,
          coopId: 5003,
          score: 5,
          content: "非常满意，下次继续合作",
          isAnonymous: false,
          createTime: "2026-03-08 11:30:00",
          manufactureName: "广州汽车配件",
        },
      ];

      // 暂时返回全部数据，不按 serviceId 过滤（便于测试）
      const filtered = allEvals;
      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered.slice(start, end);

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
];
