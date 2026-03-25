// mock/admin.js
// 模拟用户管理及国家指南管理相关接口

// ---------- 用户数据 ----------
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

// ---------- 国家指南数据 ----------
let countryGuideList = [
  {
    id: 1,
    country: '美国',
    requirements: 'FCC认证、UL认证，需提供产品测试报告...',
    process: '1.提交申请 → 2.产品测试 → 3.发证',
    documents: '["产品说明书", "电路图", "测试申请表"]',
    createTime: '2026-03-20T10:00:00Z',
    updateTime: '2026-03-20T10:00:00Z'
  },
  {
    id: 2,
    country: '欧盟',
    requirements: 'CE认证、RoHS认证，需符合欧盟标准',
    process: '1.准备技术文档 2.实验室测试 3.签署符合性声明',
    documents: '["技术文件", "测试报告", "符合性声明"]',
    createTime: '2026-03-21T09:30:00Z',
    updateTime: '2026-03-21T09:30:00Z'
  },
];

export default [
  // ========== 用户管理 ==========
  {
    url: '/api/admin/user/list',
    method: 'get',
    response: (req) => {
      const { query } = req;
      const { page = 1, size = 10, role, status, keyword } = query;

      let filtered = [...userList];

      if (role) filtered = filtered.filter(u => u.role === role);
      if (status !== undefined && status !== '') filtered = filtered.filter(u => u.status === Number(status));
      if (keyword) filtered = filtered.filter(u => u.username.includes(keyword) || u.phone.includes(keyword));

      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered.slice(start, end);

      return {
        code: 200,
        message: 'success',
        data: { total: filtered.length, records },
      };
    },
  },
  {
    url: /\/api\/admin\/user\/status\/(\d+)/,
    method: 'put',
    response: (req) => {
      const match = req.url.match(/\/api\/admin\/user\/status\/(\d+)/);
      const id = match ? parseInt(match[1]) : null;
      const { status } = req.body;
      console.log('[Mock] 状态修改请求 ID:', id, '状态:', status);

      const index = userList.findIndex(u => u.id === id);
      if (index !== -1) {
        userList[index].status = status;
        return { code: 200, message: 'success', data: null };
      }
      return { code: 404, message: '用户不存在', data: null };
    },
  },
  {
    url: /\/api\/admin\/user\/reset-password\/(\d+)/,
    method: 'post',
    response: (req) => {
      const match = req.url.match(/\/api\/admin\/user\/reset-password\/(\d+)/);
      const id = match ? parseInt(match[1]) : null;
      console.log('[Mock] 重置密码请求 ID:', id);
      const user = userList.find(u => u.id === id);
      if (user) {
        const newPassword = Math.random().toString(36).slice(-8);
        return { code: 200, message: 'success', data: { newPassword } };
      }
      return { code: 404, message: '用户不存在', data: null };
    },
  },

  // ========== 国家准入指南管理 ==========
  {
    url: '/api/admin/country-guide/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, size = 10, country } = query;
      let filtered = [...countryGuideList];
      if (country) filtered = filtered.filter(item => item.country.includes(country));
      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered.slice(start, end);
      return {
        code: 200,
        data: { total: filtered.length, records }
      };
    }
  },
  {
    url: '/api/admin/country-guide',
    method: 'post',
    response: ({ body }) => {
      const newId = Math.max(...countryGuideList.map(i => i.id), 0) + 1;
      const now = new Date().toISOString();
      const newRecord = {
        id: newId,
        ...body,
        createTime: now,
        updateTime: now
      };
      countryGuideList.push(newRecord);
      return { code: 200, data: null };
    }
  },
  {
    url: /\/api\/admin\/country-guide\/\d+/,
    method: 'put',
    response: ({ url, body }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const index = countryGuideList.findIndex(item => item.id === id);
      if (index !== -1) {
        const now = new Date().toISOString();
        countryGuideList[index] = { ...countryGuideList[index], ...body, updateTime: now };
        return { code: 200, data: null };
      }
      return { code: 404, message: '记录不存在', data: null };
    }
  },
  {
    url: /\/api\/admin\/country-guide\/\d+/,
    method: 'delete',
    response: ({ url }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const index = countryGuideList.findIndex(item => item.id === id);
      if (index !== -1) {
        countryGuideList.splice(index, 1);
        return { code: 200, data: null };
      }
      return { code: 404, message: '记录不存在', data: null };
    }
  }
];