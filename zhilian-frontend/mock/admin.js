// mock/admin.js
// 模拟用户管理及操作日志相关接口

// 模拟用户数据
let userList = [
  {
    id: 1001,
    username: "tech_company",
    role: "manufacture",
    phone: "13800138001",
    email: "tech@example.com",
    status: 1,
    createTime: "2026-03-02T09:00:00Z",
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
          (u) => u.username.includes(keyword) || u.phone.includes(keyword)
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

  // ---------- 操作日志接口 ----------
  {
    url: '/api/admin/log/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, size = 10, username, operation, result, startTime, endTime } = query;
      let filtered = [...logList];

      if (username) {
        filtered = filtered.filter(item => item.username.includes(username));
      }
      if (operation) {
        filtered = filtered.filter(item => item.operation.includes(operation));  // 模糊匹配
      }
      if (result) {
        filtered = filtered.filter(item => item.result === result);
      }
      if (startTime && endTime) {
        filtered = filtered.filter(item => {
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
          records
        }
      };
    }
  }
];