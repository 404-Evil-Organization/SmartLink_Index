// mock/admin.js
// 模拟用户管理及出海案例管理相关接口

// ---------- 用户数据 ----------
let userList = [
  {
    id: 1001,
    username: "tech_company",
    role: "manufacture",
    phone: "13800138001",
    email: "tech@example.com",
    status: 1,
    createTime: "2026-03-02T09:00:00Z", // ISO 8601 格式（UTC）
  },
  {
    id: 1002,
    username: "service_provider",
    role: "service",
    phone: "13800138002",
    email: "service@example.com",
    status: 1,
    createTime: "2026-03-02 09:00:00",
  },
  {
    id: 1003,
    username: "park_admin",
    role: "park",
    phone: "13800138003",
    email: "park@example.com",
    status: 1,
    createTime: "2026-03-04T14:20:30+08:00",
  },
  {
    id: 1004,
    username: "admin_user",
    role: "admin",
    phone: "13800138004",
    email: "admin@zhilian.com",
    status: 1,
    createTime: "2026-03-04 14:00:00",
  },
];

// ---------- 操作日志数据 ----------
let logList = [
  {
    id: 10001,
    userId: 1004,
    username: "admin_user",
    operation: "用户登录",
    params: "{}",
    result: "成功",
    ip: "192.168.1.1",
    createTime: "2026-03-20 09:30:00",
  },
  {
    id: 10002,
    userId: 1004,
    username: "admin_user",
    operation: "审核企业",
    params: '{"type":"manufacture","status":"approved"}',
    result: "成功",
    ip: "192.168.1.1",
    createTime: "2026-03-20 10:15:00",
  },
  {
    id: 10003,
    userId: 1004,
    username: "admin_user",
    operation: "重置密码",
    params: '{"userId":1002}',
    result: "成功",
    ip: "192.168.1.1",
    createTime: "2026-03-21 11:00:00",
  },
  {
    id: 10004,
    userId: 1004,
    username: "admin_user",
    operation: "新增指数",
    params: '{"region":"珠海","year":2026,"quarter":2}',
    result: "成功",
    ip: "192.168.1.1",
    createTime: "2026-03-22 14:20:00",
  },
  {
    id: 10005,
    userId: 1004,
    username: "admin_user",
    operation: "禁用用户",
    params: '{"userId":1003}',
    result: "成功",
    ip: "192.168.1.1",
    createTime: "2026-03-23 16:45:00",
  },
];
// 1. 获取用户列表（分页）
{
  // ---------- 出海案例数据 ----------
  let abroadCaseList = [
    {
      id: 1,
      title: "某电子公司CE认证成功案例",
      companyName: "东莞电子",
      companyType: "manufacture",
      country: "欧盟",
      serviceType: "CE认证",
      description: "通过华测检测服务，顺利获得CE认证，产品成功进入欧洲市场。",
      coverImage: "https://picsum.photos/200/150?random=1",
      status: 1,
      publishTime: "2026-02-10 10:00:00",
      createTime: "2026-02-10 09:00:00",
      updateTime: "2026-02-10 09:00:00",
    },
    {
      id: 2,
      title: "某机械公司UL认证案例",
      companyName: "东莞精密机械",
      companyType: "manufacture",
      country: "美国",
      serviceType: "UL认证",
      description: "通过SGS服务，获得UL认证，产品出口美国。",
      coverImage: "https://picsum.photos/200/150?random=2",
      status: 1,
      publishTime: "2026-03-01 14:00:00",
      createTime: "2026-03-01 13:00:00",
      updateTime: "2026-03-01 13:00:00",
    },
    {
      id: 3,
      title: "某电子公司FCC认证案例",
      companyName: "深圳电子科技",
      companyType: "manufacture",
      country: "美国",
      serviceType: "FCC认证",
      description: "通过华测检测，获得FCC认证，产品成功进入美国市场。",
      coverImage: "https://picsum.photos/200/150?random=3",
      status: 0, // 草稿
      publishTime: null,
      createTime: "2026-03-05 11:00:00",
      updateTime: "2026-03-05 11:00:00",
    },
  ];
}

// ---------- 用户管理 ----------
export default [
  {
    url: "/api/admin/user/list",
    method: "get",
    response: (req) => {
      const { query } = req;
      const { page = 1, size = 10, role, status, keyword } = query;
      let filtered = [...userList];

      if (role) {
        filtered = filtered.filter((u) => u.role === role);
      }
      if (status !== undefined && status !== "") {
        filtered = filtered.filter((u) => u.status === Number(status));
      }
      if (keyword) {
        filtered = filtered.filter(
          (u) => u.username.includes(keyword) || u.phone.includes(keyword),
        );
      }

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
  {
    url: /\/api\/admin\/user\/status\/(\d+)/,
    method: "put",
    response: (req) => {
      const match = req.url.match(/\/api\/admin\/user\/status\/(\d+)/);
      const id = match ? parseInt(match[1]) : null;
      const { status } = req.body;
      console.log("[Mock] 状态修改请求 ID:", id, "状态:", status);

      const index = userList.findIndex((u) => u.id === id);
      if (index !== -1) {
        userList[index].status = status;
        return {
          code: 200,
          message: "success",
          data: null,
        };
      }
      return {
        code: 404,
        message: "用户不存在",
        data: null,
      };
    },
  },
  {
    url: /\/api\/admin\/user\/reset-password\/(\d+)/,
    method: "post",
    response: (req) => {
      const match = req.url.match(/\/api\/admin\/user\/reset-password\/(\d+)/);
      const id = match ? parseInt(match[1]) : null;
      console.log("[Mock] 重置密码请求 ID:", id);

      const user = userList.find((u) => u.id === id);
      if (user) {
        const newPassword = Math.random().toString(36).slice(-8);
        return {
          code: 200,
          message: "success",
          data: { newPassword },
        };
      }
      return {
        code: 404,
        message: "用户不存在",
        data: null,
      };
    },
  },

  // ---------- 操作日志接口 ----------
  {
    url: "/api/admin/log/list",
    method: "get",
    response: ({ query }) => {
      const {
        page = 1,
        size = 10,
        username,
        operation,
        result,
        startTime,
        endTime,
      } = query;
      let filtered = [...logList];

      if (username) {
        filtered = filtered.filter((item) => item.username.includes(username));
      }
      if (operation) {
        filtered = filtered.filter((item) =>
          item.operation.includes(operation),
        ); // 模糊匹配
      }
      if (result) {
        filtered = filtered.filter((item) => item.result === result);
      }
      if (startTime && endTime) {
        filtered = filtered.filter((item) => {
          // 直接比较字符串，因为格式统一为 YYYY-MM-DD HH:mm:ss
          return item.createTime >= startTime && item.createTime <= endTime;
        });
      }

      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered.slice(start, end);
      return {
        code: 200,
        data: {
          total: filtered.length,
          records,
        },
      };
    },
  },

  // ---------- 出海案例管理 ----------
  {
    url: "/api/admin/abroad-case/list",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10, country, status } = query;
      let filtered = [...abroadCaseList];
      if (country)
        filtered = filtered.filter((item) => item.country.includes(country));
      if (status !== undefined && status !== "")
        filtered = filtered.filter((item) => item.status === Number(status));
      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered.slice(start, end);
      return { code: 200, data: { total: filtered.length, records } };
    },
  },
  {
    url: "/api/admin/abroad-case",
    method: "post",
    response: ({ body }) => {
      const newId = Math.max(...abroadCaseList.map((i) => i.id), 0) + 1;
      const now = new Date().toISOString().replace("T", " ").substring(0, 19);
      const newRecord = {
        id: newId,
        ...body,
        publishTime: body.status === 1 ? now : null,
        createTime: now,
        updateTime: now,
      };
      abroadCaseList.push(newRecord);
      return { code: 200, data: null };
    },
  },
  {
    url: /\/api\/admin\/abroad-case\/\d+/,
    method: "put",
    response: ({ url, body }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const index = abroadCaseList.findIndex((item) => item.id === id);
      if (index !== -1) {
        abroadCaseList[index] = {
          ...abroadCaseList[index],
          ...body,
          updateTime: new Date()
            .toISOString()
            .replace("T", " ")
            .substring(0, 19),
        };
        return { code: 200, data: null };
      }
      return { code: 404, message: "案例不存在", data: null };
    },
  },
  {
    url: /\/api\/admin\/abroad-case\/\d+/,
    method: "delete",
    response: ({ url }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const index = abroadCaseList.findIndex((item) => item.id === id);
      if (index !== -1) {
        abroadCaseList.splice(index, 1);
        return { code: 200, data: null };
      }
      return { code: 404, message: "案例不存在", data: null };
    },
  },
  {
    url: "/api/admin/demand/pending",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10 } = query;
      // 模拟数据池
      const allRecords = [
        {
          id: 3001,
          manuId: 1001,
          manuName: "深圳电子科技",
          title: "寻求PCB设计服务",
          description: "需要专业PCB设计公司，有高速PCB设计经验者优先。",
          expectedBudget: 10.0,
          deadline: "2026-04-01",
          createTime: "2026-03-07 14:30:00",
          tags: [
            { id: 1, name: "PCB设计" },
            { id: 2, name: "高速电路" },
          ],
        },
        {
          id: 3002,
          manuId: 1002,
          manuName: "东莞精密制造",
          title: "自动化产线改造咨询",
          description: "寻求自动化产线改造方案，包括机器人和MES系统集成。",
          expectedBudget: 50.0,
          deadline: "2026-05-15",
          createTime: "2026-03-08 09:20:00",
          tags: [
            { id: 3, name: "自动化" },
            { id: 4, name: "MES系统" },
          ],
        },
        {
          id: 3003,
          manuId: 1003,
          manuName: "广州智能设备",
          title: "工业物联网平台开发",
          description:
            "需要开发一套工业物联网平台，支持设备数据采集和远程监控。",
          expectedBudget: 80.0,
          deadline: "2026-06-30",
          createTime: "2026-03-09 11:15:00",
          tags: [
            { id: 5, name: "物联网" },
            { id: 6, name: "数据采集" },
          ],
        },
        {
          id: 3004,
          manuId: 1004,
          manuName: "佛山陶瓷企业",
          title: "能耗优化系统开发",
          description: "需要开发能源管理系统，对窑炉能耗进行优化。",
          expectedBudget: 30.0,
          deadline: "2026-05-01",
          createTime: "2026-03-10 15:45:00",
          tags: [
            { id: 7, name: "能源管理" },
            { id: 8, name: "工业节能" },
          ],
        },
        {
          id: 3005,
          manuId: 1005,
          manuName: "中山电器厂",
          title: "产品外观设计服务",
          description: "为新款家电设计外观，要求现代简约风格。",
          expectedBudget: 5.0,
          deadline: "2026-04-20",
          createTime: "2026-03-11 10:00:00",
          tags: [
            { id: 9, name: "工业设计" },
            { id: 10, name: "外观设计" },
          ],
        },
      ];

      // 分页处理
      const start = (page - 1) * size;
      const end = start + Number(size);
      const records = allRecords.slice(start, end);

      return {
        code: 200,
        message: "success",
        data: {
          total: allRecords.length,
          records,
        },
      };
    },
  },

  // 审核需求（通过/驳回）
  {
    url: "/api/admin/demand/approve/:id",
    method: "post",
    response: ({ body, query }) => {
      const { id } = query;
      const { status, remark } = body;

      // 模拟校验（仅演示，实际可根据需求做逻辑）
      if (!id) {
        return {
          code: 400,
          message: "需求ID不能为空",
          data: null,
        };
      }

      if (status !== "approved" && status !== "rejected") {
        return {
          code: 400,
          message: "审核状态无效",
          data: null,
        };
      }

      // 模拟审核成功
      console.log(
        `[Mock] 审核需求 ${id}，结果：${status}，理由：${remark || "无"}`,
      );

      return {
        code: 200,
        message: "success",
        data: null,
      };
    },
  },
];
