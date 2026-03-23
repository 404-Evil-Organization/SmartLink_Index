// mock/enterpriseAudit.js
// 模拟待审核企业列表

let auditList = [
  {
    id: 2001,
    companyName: '华测检测认证集团',
    type: 'service',
    contactPerson: '王五',
    contactPhone: '13700137003',
    createTime: '2026-03-15 10:30:00'
  },
  {
    id: 2002,
    companyName: '深圳电子科技',
    type: 'manufacture',
    contactPerson: '张三',
    contactPhone: '13800138001',
    createTime: '2026-03-16 09:15:00'
  },
  {
    id: 2003,
    companyName: '东莞精密机械',
    type: 'manufacture',
    contactPerson: '李四',
    contactPhone: '13900139002',
    createTime: '2026-03-17 14:20:00'
  },
  {
    id: 2004,
    companyName: 'SGS通标',
    type: 'service',
    contactPerson: '赵六',
    contactPhone: '13600136004',
    createTime: '2026-03-18 11:45:00'
  }
];

export default [
  {
    url: '/api/admin/enterprise/audit/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, size = 10, companyName, type } = query;
      let filtered = [...auditList];
      if (companyName) filtered = filtered.filter(item => item.companyName.includes(companyName));
      if (type) filtered = filtered.filter(item => item.type === type);
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
  },
  {
    url: /\/api\/admin\/enterprise\/audit\/\d+/,
    method: 'put',
    response: ({ url, body }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const { status, auditRemark } = body;
      console.log(`审核企业 ${id}，结果：${status}，意见：${auditRemark || '无'}`);
      // 从列表中移除该记录（或更新状态）
      const index = auditList.findIndex(item => item.id === id);
      if (index !== -1) auditList.splice(index, 1);
      return { code: 200, data: null };
    }
  }
];