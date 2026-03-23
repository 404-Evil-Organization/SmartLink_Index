export default [
  // 5.4 获取我的合作记录列表
  {
    url: "/api/cooperation/my-list",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10, status, enterpriseId } = query;
      // 模拟数据（可根据 enterpriseId 和 status 过滤）
      const allRecords = [
        {
          id: 5001,
          opponentName: "华测检测",
          demandTitle: "寻求PCB设计服务",
          amount: 10.0,
          startDate: "2026-03-01",
          endDate: "2026-06-30",
          status: "ongoing",
          createTime: "2026-03-01 10:00:00",
          hasEvaluated: false,
        },
        {
          id: 5002,
          opponentName: "SGS通标",
          demandTitle: "CE认证需求",
          amount: 5.0,
          startDate: "2026-02-01",
          endDate: "2026-05-30",
          status: "completed",
          createTime: "2026-02-01 09:00:00",
          hasEvaluated: true,
        },
        {
          id: 5003,
          opponentName: "东莞精密制造",
          demandTitle: "工业设计外包",
          amount: 8.0,
          startDate: "2026-01-15",
          endDate: "2026-04-15",
          status: "completed",
          createTime: "2026-01-15 14:30:00",
          hasEvaluated: false,
        },
        {
          id: 5004,
          opponentName: "广州检测中心",
          demandTitle: "产品测试服务",
          amount: 3.5,
          startDate: "2026-04-01",
          endDate: "2026-07-01",
          status: "ongoing",
          createTime: "2026-04-01 11:00:00",
          hasEvaluated: false,
        },
      ];
      let filtered = allRecords;
      if (status) filtered = filtered.filter((item) => item.status === status);
      // 可根据 enterpriseId 过滤（这里简化为不过滤，真实后端需实现）
      const start = (page - 1) * size;
      const end = start + size;
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
  // 5.5 获取合作记录详情
  {
    url: "/api/cooperation/:id",
    method: "get",
    response: ({ query }) => {
      const { id } = query;
      const detailMap = {
        5001: {
          id: 5001,
          manuId: 1001,
          manuName: "深圳电子科技",
          serviceId: 2001,
          serviceName: "华测检测",
          demandId: 3001,
          demandTitle: "寻求PCB设计服务",
          demandDescription: "需要专业PCB设计公司，有高速PCB设计经验者优先。",
          amount: 10.0,
          startDate: "2026-03-01",
          endDate: "2026-06-30",
          description:
            "合作内容简述：提供PCB设计服务，包括原理图设计、PCB布局、样板测试等。",
          status: "ongoing",
          createTime: "2026-03-01 10:00:00",
          hasEvaluated: false,
        },
        5002: {
          id: 5002,
          manuId: 1002,
          manuName: "东莞精密制造",
          serviceId: 2002,
          serviceName: "SGS通标",
          demandId: 3002,
          demandTitle: "CE认证需求",
          demandDescription: "产品需要获得CE认证，进入欧洲市场。",
          amount: 5.0,
          startDate: "2026-02-01",
          endDate: "2026-05-30",
          description:
            "合作内容简述：提供CE认证咨询服务，协助准备资料、测试协调。",
          status: "completed",
          createTime: "2026-02-01 09:00:00",
          hasEvaluated: true,
        },
        5003: {
          id: 5003,
          manuId: 1003,
          manuName: "广州电子",
          serviceId: 2003,
          serviceName: "东莞精密制造",
          demandId: 3003,
          demandTitle: "工业设计外包",
          demandDescription: "产品外观设计和结构设计。",
          amount: 8.0,
          startDate: "2026-01-15",
          endDate: "2026-04-15",
          description:
            "合作内容简述：提供工业设计服务，包括外观设计、3D建模、样品制作。",
          status: "completed",
          createTime: "2026-01-15 14:30:00",
          hasEvaluated: false,
        },
        5004: {
          id: 5004,
          manuId: 1001,
          manuName: "深圳电子科技",
          serviceId: 2004,
          serviceName: "广州检测中心",
          demandId: 3004,
          demandTitle: "产品测试服务",
          demandDescription: "需要进行EMC测试和安规测试。",
          amount: 3.5,
          startDate: "2026-04-01",
          endDate: "2026-07-01",
          description: "合作内容简述：提供EMC和安规测试服务，出具测试报告。",
          status: "ongoing",
          createTime: "2026-04-01 11:00:00",
          hasEvaluated: false,
        },
      };
      const data = detailMap[id];
      if (data) {
        return { code: 200, message: "success", data };
      }
      return { code: 404, message: "合作记录不存在", data: null };
    },
  },
];
