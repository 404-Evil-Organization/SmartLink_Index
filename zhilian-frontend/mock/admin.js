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

// ---------- 出海案例数据 ----------
let abroadCaseList = [
  {
    id: 1,
    title: '某电子公司CE认证成功案例',
    companyName: '东莞电子',
    companyType: 'manufacture',
    country: '欧盟',
    serviceType: 'CE认证',
    description: '通过华测检测服务，顺利获得CE认证，产品成功进入欧洲市场。',
    coverImage: 'https://picsum.photos/200/150?random=1', 
    status: 1,
    publishTime: '2026-02-10 10:00:00',
    createTime: '2026-02-10 09:00:00',
    updateTime: '2026-02-10 09:00:00'
  },
  {
    id: 2,
    title: '某机械公司UL认证案例',
    companyName: '东莞精密机械',
    companyType: 'manufacture',
    country: '美国',
    serviceType: 'UL认证',
    description: '通过SGS服务，获得UL认证，产品出口美国。',
    coverImage: 'https://picsum.photos/200/150?random=2',
    status: 1,
    publishTime: '2026-03-01 14:00:00',
    createTime: '2026-03-01 13:00:00',
    updateTime: '2026-03-01 13:00:00'
  },
  {
    id: 3,
    title: '某电子公司FCC认证案例',
    companyName: '深圳电子科技',
    companyType: 'manufacture',
    country: '美国',
    serviceType: 'FCC认证',
    description: '通过华测检测，获得FCC认证，产品成功进入美国市场。',
    coverImage: 'https://picsum.photos/200/150?random=3',
    status: 0,  // 草稿
    publishTime: null,
    createTime: '2026-03-05 11:00:00',
    updateTime: '2026-03-05 11:00:00'
  }
];

export default [
  // ---------- 用户管理 ----------
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
      return { code: 200, message: 'success', data: { total: filtered.length, records } };
    }
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
    }
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
    }
  },

  // ---------- 出海案例管理 ----------
  {
    url: '/api/admin/abroad-case/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, size = 10, country, status } = query;
      let filtered = [...abroadCaseList];
      if (country) filtered = filtered.filter(item => item.country.includes(country));
      if (status !== undefined && status !== '') filtered = filtered.filter(item => item.status === Number(status));
      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered.slice(start, end);
      return { code: 200, data: { total: filtered.length, records } };
    }
  },
  {
    url: '/api/admin/abroad-case',
    method: 'post',
    response: ({ body }) => {
      const newId = Math.max(...abroadCaseList.map(i => i.id), 0) + 1;
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19);
      const newRecord = {
        id: newId,
        ...body,
        publishTime: body.status === 1 ? now : null,
        createTime: now,
        updateTime: now
      };
      abroadCaseList.push(newRecord);
      return { code: 200, data: null };
    }
  },
  {
    url: /\/api\/admin\/abroad-case\/\d+/,
    method: 'put',
    response: ({ url, body }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const index = abroadCaseList.findIndex(item => item.id === id);
      if (index !== -1) {
        abroadCaseList[index] = { ...abroadCaseList[index], ...body, updateTime: new Date().toISOString().replace('T', ' ').substring(0, 19) };
        return { code: 200, data: null };
      }
      return { code: 404, message: '案例不存在', data: null };
    }
  },
  {
    url: /\/api\/admin\/abroad-case\/\d+/,
    method: 'delete',
    response: ({ url }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const index = abroadCaseList.findIndex(item => item.id === id);
      if (index !== -1) {
        abroadCaseList.splice(index, 1);
        return { code: 200, data: null };
      }
      return { code: 404, message: '案例不存在', data: null };
    }
  }
];