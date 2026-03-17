// mock/dashboard.js
export default [
  // 7.1 获取统计卡片数据
  {
    url: "/api/dashboard/statistics",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: {
        manufactureCount: 1250,
        serviceCount: 380,
        demandCount: 560,
        cooperationCount: 890,
      },
    }),
  },
  // 7.2 获取热力图数据
  {
    url: "/api/dashboard/heatmap",
    method: "get",
    response: ({ query }) => {
      const { start, end } = query;
      // 可根据参数返回不同数据，此处简单模拟
      return {
        code: 200,
        message: "success",
        data: [
          { region: "深圳", value: 120 },
          { region: "东莞", value: 95 },
          { region: "惠州", value: 78 },
          { region: "广州", value: 110 },
          { region: "佛山", value: 82 },
          { region: "中山", value: 65 },
          { region: "珠海", value: 70 },
        ],
      };
    },
  },
  // 7.3 获取热门需求
  {
    url: "/api/dashboard/topDemands",
    method: "get",
    response: ({ query }) => {
      const top = parseInt(query.top) || 5;
      const all = [
        { serviceType: "检测认证", count: 45 },
        { serviceType: "工业设计", count: 32 },
        { serviceType: "物流供应链", count: 28 },
        { serviceType: "CE认证", count: 21 },
        { serviceType: "FCC认证", count: 18 },
        { serviceType: "PCB设计", count: 15 },
      ];
      return {
        code: 200,
        message: "success",
        data: all.slice(0, top),
      };
    },
  },
  // 7.4 获取网络关系数据
  {
    url: "/api/dashboard/network",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: {
        nodes: [
          { id: "m1001", name: "深圳电子", type: "manufacture" },
          { id: "m1002", name: "东莞精密", type: "manufacture" },
          { id: "s2001", name: "华测检测", type: "service" },
          { id: "s2002", name: "深圳物流", type: "service" },
        ],
        links: [
          { source: "m1001", target: "s2001", value: 3 },
          { source: "m1001", target: "s2002", value: 1 },
          { source: "m1002", target: "s2001", value: 2 },
        ],
      },
    }),
  },
];
