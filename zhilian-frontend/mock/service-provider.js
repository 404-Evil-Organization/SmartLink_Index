export default [
  // 获取服务商列表（分页+筛选）
  {
    url: '/api/service-provider/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, size = 10, companyName, region, serviceType, status } = query
      // 模拟数据源
      const mockList = [
        {
          id: 1,
          companyName: '华测检测认证集团',
          region: '深圳',
          serviceType: '检测认证, CE认证',
          contactPerson: '王五',
          contactPhone: '13700137003',
          email: 'wangwu@cti.com',
          address: '深圳市南山区科技园',
          rating: 4.8,
          status: 1,
          description: 'CNAS认可实验室',
          website: 'https://www.cti.com',
          employeeCount: 1200,
          establishedDate: '2003-12-01',
          qualification: 'CNAS, CMA',
          logo: 'https://picsum.photos/100/100?random=1'
        },
        {
          id: 2,
          companyName: 'SGS通标标准',
          region: '广州',
          serviceType: '国际认证, 检验',
          contactPerson: '李四',
          contactPhone: '13800138004',
          email: 'lisi@sgs.com',
          address: '广州市黄埔区',
          rating: 4.9,
          status: 1,
          description: '全球领先检测机构',
          website: 'https://www.sgs.com',
          employeeCount: 2000,
          establishedDate: '1991-05-15',
          qualification: 'CNAS, IAAC',
          logo: 'https://picsum.photos/100/100?random=2'
        },
        {
          id: 3,
          companyName: '东莞精密制造服务',
          region: '东莞',
          serviceType: '工业设计, 打样',
          contactPerson: '赵六',
          contactPhone: '13900139005',
          email: 'zhaoliu@dg.com',
          address: '东莞市松山湖',
          rating: 4.5,
          status: 0,
          description: '精密加工专家',
          website: '',
          employeeCount: 350,
          establishedDate: '2010-08-20',
          qualification: 'ISO9001',
          logo: ''
        }
      ]

      // 筛选
      let filtered = mockList.filter(item => {
        if (companyName && !item.companyName.includes(companyName)) return false
        if (region && item.region !== region) return false
        if (serviceType && !item.serviceType.includes(serviceType)) return false
        if (status !== undefined && item.status !== Number(status)) return false
        return true
      })

      // 分页
      const start = (page - 1) * size
      const end = start + size
      const records = filtered.slice(start, end)

      return {
        code: 200,
        message: 'success',
        data: {
          total: filtered.length,
          records
        }
      }
    }
  },

  // 获取服务商详情
  {
    url: '/api/service-provider/:id',
    method: 'get',
    response: (request) => {
      const id = request.params?.id || request.query?.id
      const mockDetail = {
        id: parseInt(id),
        companyName: '华测检测认证集团',
        region: '深圳',
        serviceType: '检测认证, CE认证',
        contactPerson: '王五',
        contactPhone: '13700137003',
        email: 'wangwu@cti.com',
        address: '深圳市南山区科技园',
        rating: 4.8,
        status: 1,
        description: 'CNAS认可实验室',
        website: 'https://www.cti.com',
        employeeCount: 1200,
        annualRevenue: 5000,
        establishedDate: '2003-12-01',
        qualification: 'CNAS, CMA',
        logo: 'https://picsum.photos/100/100?random=1'
      }
      return {
        code: 200,
        message: 'success',
        data: mockDetail
      }
    }
  },

  // 新增服务商
  {
    url: '/api/service-provider',
    method: 'post',
    response: ({ body }) => {
      return {
        code: 200,
        message: '新增成功',
        data: {
          id: Math.floor(Math.random() * 1000) + 100
        }
      }
    }
  },

  // 修改服务商
  {
    url: '/api/service-provider/:id',
    method: 'put',
    response: ({ params, body }) => {
      return {
        code: 200,
        message: '修改成功',
        data: null
      }
    }
  },

  // 删除服务商
  {
    url: '/api/service-provider/:id',
    method: 'delete',
    response: ({ params }) => {
      return {
        code: 200,
        message: '删除成功',
        data: null
      }
    }
  },

  // 获取区域列表
  {
    url: '/api/common/regions',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: ['深圳', '东莞', '惠州', '广州', '佛山', '中山', '珠海', '江门', '肇庆']
      }
    }
  },

  // 获取服务类型标签
  {
    url: '/api/common/service-tags',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: [
          { id: 1, name: '检测认证', category: '服务类型' },
          { id: 2, name: '工业设计', category: '服务类型' },
          { id: 3, name: '物流供应链', category: '服务类型' }
        ]
      }
    }
  }
]