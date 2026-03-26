// mock/admin.js
// 模拟用户管理相关接口

// 模拟用户数据
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

export default [
  // 1. 获取用户列表（分页）
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

  // 2. 修改用户状态（启用/禁用）使用正则匹配动态ID
  {
    url: /\/api\/admin\/user\/status\/(\d+)/,
    method: "put",
    response: (req) => {
      // 从 URL 中提取 ID
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

  // 3. 重置用户密码 使用正则匹配动态ID
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
