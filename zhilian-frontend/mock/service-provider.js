export default [
  // 获取服务商列表（分页+筛选）
  {
    url: '/api/service-provider/list',
    method: 'get',
    response: ({ query }) => {
      const { page = 1, size = 10, companyName, region, serviceType, status } = query
      // 分页参数显式转为 number，避免字符串参与加法导致拼接
      const pageNum = Number(page) || 1
      const sizeNum = Number(size) || 10
      // 模拟数据源
      const mockList = [
        {
          id: 1,
          companyName: '宝鸡有一群怀揣着梦想的少年相信在牛大叔的带领下会创造生命的奇迹网络科技有限公司',
          region: '深圳',
          address: '深圳市南山区科技园',
          contactPerson: '王五',
          contactPhone: '13700137003',
          serviceType: '检测认证, CE认证',
          description: 'CNAS认可实验室',
          logo: 'https://picsum.photos/100/100?random=1',
          website: 'https://www.cti.com',
          establishedDate: '2003-12-01',
          employeeCount: 1200,
          qualification: 'CNAS, CMA',
          auditStatus: 'approved',
          auditRemark: null,
          auditTime: null,
          createTime: '2026-03-01 10:00:00',
          updateTime: '2026-03-01 10:00:00',
          email: 'wangwu@cti.com',
          rating: 4.8,
          annualRevenue: 5000,
        },
        {
          id: 2,
          companyName: 'SGS通标标准',
          region: '广州',
          address: '广州市黄埔区科学城',
          contactPerson: '李四',
          contactPhone: '13800138004',
          serviceType: '国际认证, 检验',
          description: '全球领先检测机构',
          logo: 'https://picsum.photos/100/100?random=2',
          website: 'https://www.sgs.com',
          establishedDate: '1991-05-15',
          employeeCount: 2000,
          qualification: 'CNAS, IAAC',
          auditStatus: 'approved',
          auditRemark: null,
          auditTime: null,
          createTime: '2026-03-01 10:00:00',
          updateTime: '2026-03-01 10:00:00',
          email: 'lisi@sgs.com',
          rating: 4.9,
          annualRevenue: 8000,
        },
        {
          id: 3,
          companyName: '东莞精密制造服务',
          region: '东莞',
          address: '东莞市松山湖高新技术区',
          contactPerson: '赵六',
          contactPhone: '13900139005',
          serviceType: '工业设计, 打样',
          description: '精密加工专家',
          logo: 'https://picsum.photos/100/100?random=3',
          website: '',
          establishedDate: '2010-08-20',
          employeeCount: 350,
          qualification: 'ISO9001',
          auditStatus: 'pending',
          auditRemark: '资质文件待补充',
          auditTime: null,
          createTime: '2026-03-02 14:30:00',
          updateTime: '2026-03-02 14:30:00',
          email: 'zhaoliu@dg.com',
          rating: 4.5,
          annualRevenue: 2000,
        }                                                    
      ]

      // 筛选
      let filtered = mockList.filter(item => {
        if (companyName && !item.companyName.includes(companyName)) return false
        if (region && item.region !== region) return false
        if (serviceType && !item.serviceType.includes(serviceType)) return false
        // if (auditStatus !== undefined && item.auditStatus !== auditStatus) return false
        return true
      })

      // 分页
      const start = (pageNum - 1) * sizeNum
      const end = start + sizeNum
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
      const id = parseInt(request.params?.id || request.query?.id);

      const mockList = [
        {
          id: 1,
          companyName: '宝鸡有一群怀揣着梦想的少年相信在牛大叔的带领下会创造生命的奇迹网络科技有限公司',
          region: '深圳',
          address: '深圳市南山区科技园',
          contactPerson: '王五',
          contactPhone: '13700137003',
          serviceType: '检测认证, CE认证',
          description: 'CNAS认可实验室',
          logo: 'https://picsum.photos/100/100?random=1',
          website: 'https://www.cti.com',
          establishedDate: '2003-12-01',
          employeeCount: 1200,
          qualification: 'CNAS, CMA',
          auditStatus: 'approved',
          auditRemark: null,
          auditTime: null,
          createTime: '2026-03-01 10:00:00',
          updateTime: '2026-03-01 10:00:00',
          email: 'wangwu@cti.com',
          rating: 4.8,
          annualRevenue: 5000,
        },
        {
          id: 2,
          companyName: 'SGS通标标准',
          region: '广州',
          address: '广州市黄埔区科学城',
          contactPerson: '李四',
          contactPhone: '13800138004',
          serviceType: '国际认证, 检验',
          description: '全球领先检测机构',
          logo: 'https://picsum.photos/100/100?random=2',
          website: 'https://www.sgs.com',
          establishedDate: '1991-05-15',
          employeeCount: 2000,
          qualification: 'CNAS, IAAC',
          auditStatus: 'approved',
          auditRemark: null,
          auditTime: null,
          createTime: '2026-03-01 10:00:00',
          updateTime: '2026-03-01 10:00:00',
          email: 'lisi@sgs.com',
          rating: 4.9,
          annualRevenue: 8000,
        },
        {
          id: 3,
          companyName: '东莞精密制造服务',
          region: '东莞',
          address: '东莞市松山湖高新技术区',
          contactPerson: '赵六',
          contactPhone: '13900139005',
          serviceType: '工业设计, 打样',
          description: '精密加工专家',
          logo: 'https://picsum.photos/100/100?random=3',
          website: '',
          establishedDate: '2010-08-20',
          employeeCount: 350,
          qualification: 'ISO9001',
          auditStatus: 'pending',
          auditRemark: '资质文件待补充',
          auditTime: null,
          createTime: '2026-03-02 14:30:00',
          updateTime: '2026-03-02 14:30:00',
          email: 'zhaoliu@dg.com',
          rating: 4.5,
          annualRevenue: 2000,
        }
      ];

      const found = mockList.find(item => item.id === id);
      if (found) {
        return {
          code: 200,
          message: 'success',
          data: found
        };
      } else {
        return {
          code: 404,
          message: '服务商不存在',
          data: null
        };
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