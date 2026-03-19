export default [
  // 获取证书列表
  {
    url: '/api/certification/list',
    method: 'get',
    response: ({ query }) => {
      const { serviceId } = query

      // 静态证书数据（字段与接口文档一致）
      const mockList = [
        {
          id: 10001,
          serviceId: 1,
          certName: 'CNAS实验室认可证书',
          certNo: 'CNAS L1234',
          issueAuthority: '中国合格评定国家认可委员会',
          issueDate: '2023-05-01',
          expireDate: '2028-04-30',
          certFileUrl: 'https://picsum.photos/200/100?random=101',
          status: 1,
          createTime: '2026-03-01 10:00:00'
        },
        {
          id: 10002,
          serviceId: 1,
          certName: 'CMA资质认定证书',
          certNo: 'CMA 2023123456',
          issueAuthority: '国家市场监督管理总局',
          issueDate: '2023-08-15',
          expireDate: '2026-08-14',
          certFileUrl: 'https://picsum.photos/200/100?random=102',
          status: 1,
          createTime: '2026-03-01 10:00:00'
        },
        {
          id: 10003,
          serviceId: 2,
          certName: 'ISO9001质量管理体系',
          certNo: 'ISO 9001:2025',
          issueAuthority: 'SGS',
          issueDate: '2025-01-10',
          expireDate: '2028-01-09',
          certFileUrl: 'https://picsum.photos/200/100?random=103',
          status: 1,
          createTime: '2026-03-02 14:30:00'
        },
        {
          id: 10004,
          serviceId: 3,
          certName: 'CE认证证书',
          certNo: 'CE-2024-001',
          issueAuthority: '欧盟公告机构',
          issueDate: '2024-02-20',
          expireDate: '2027-02-19',
          certFileUrl: 'https://picsum.photos/200/100?random=104',
          status: 1,
          createTime: '2026-03-03 09:15:00'
        }
      ]

      // 如果传了 serviceId，筛选返回；否则返回全部（用于管理后台）
      const filtered = serviceId
        ? mockList.filter(item => item.serviceId === parseInt(serviceId))
        : mockList

      return {
        code: 200,
        message: 'success',
        data: filtered
      }
    }
  }
]