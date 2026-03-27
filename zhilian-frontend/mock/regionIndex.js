// mock/regionIndex.js
let regionIndexList = [
  {
    id: 1,
    region: '深圳',
    year: 2026,
    periodType: 'quarter',
    periodValue: 1,
    coopDensity: 0.85,
    serviceRate: 0.72,
    crossRate: 0.45,
    totalIndex: 75.8,
    calcTime: '2026-03-20 10:00:00'
  },
  {
    id: 2,
    region: '东莞',
    year: 2026,
    periodType: 'quarter',
    periodValue: 1,
    coopDensity: 0.78,
    serviceRate: 0.68,
    crossRate: 0.52,
    totalIndex: 73.2,
    calcTime: '2026-03-20 10:00:00'
  },
  {
    id: 3,
    region: '广州',
    year: 2026,
    periodType: 'quarter',
    periodValue: 1,
    coopDensity: 0.82,
    serviceRate: 0.70,
    crossRate: 0.48,
    totalIndex: 74.5,
    calcTime: '2026-03-20 10:00:00'
  }
];

export default [
  // 获取列表
  {
    url: '/api/region-index/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, size = 10, region, year, periodType, periodValue } = query;
      let filtered = [...regionIndexList];
      if (region) filtered = filtered.filter(item => item.region.includes(region));
      if (year) filtered = filtered.filter(item => item.year === Number(year));
      if (periodType) filtered = filtered.filter(item => item.periodType === periodType);
      if (periodValue) filtered = filtered.filter(item => item.periodValue === Number(periodValue));
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
  // 新增
  {
    url: '/api/region-index',
    method: 'post',
    response: ({ body }) => {
      const newId = Math.max(...regionIndexList.map(i => i.id), 0) + 1;
      const newRecord = {
        id: newId,
        ...body,
        calcTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
      };
      regionIndexList.push(newRecord);
      return {
        code: 200,
        data: null
      };
    }
  },
  // 修改
  {
    url: /\/api\/region-index\/\d+/,
    method: 'put',
    response: ({ url, body }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const index = regionIndexList.findIndex(item => item.id === id);
      if (index !== -1) {
        regionIndexList[index] = { ...regionIndexList[index], ...body, id };
        return { code: 200, data: null };
      }
      return { code: 404, message: '记录不存在' };
    }
  },
  // 删除
  {
    url: /\/api\/region-index\/\d+/,
    method: 'delete',
    response: ({ url }) => {
      const id = parseInt(url.match(/\d+/)[0]);
      const index = regionIndexList.findIndex(item => item.id === id);
      if (index !== -1) {
        regionIndexList.splice(index, 1);
        return { code: 200, data: null };
      }
      return { code: 404, message: '记录不存在' };
    }
  }
];