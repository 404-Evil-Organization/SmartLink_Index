// mock/abroad.js

// 模拟服务商数据
const mockServiceProviders = [
  {
    id: 2001,
    companyName: "华测检测认证集团",
    region: "深圳",
    serviceType: "检测认证",
    description: "CNAS认可实验室，提供国际认证服务",
    contactPerson: "王五",
    contactPhone: "13700137003",
    logo: "https://via.placeholder.com/100?text=CTI",
    isAbroad: 1,
    countryCoverage: "欧盟,美国,日本,东南亚",
    auditStatus: "approved",
  },
  {
    id: 2002,
    companyName: "SGS通标标准技术服务",
    region: "广州",
    serviceType: "国际认证",
    description: "全球领先的检验、鉴定、测试和认证机构",
    contactPerson: "赵六",
    contactPhone: "13600136004",
    logo: "https://via.placeholder.com/100?text=SGS",
    isAbroad: 1,
    countryCoverage: "欧盟,美国,加拿大,澳大利亚",
    auditStatus: "approved",
  },
  {
    id: 2003,
    companyName: "德国莱茵TÜV",
    region: "上海",
    serviceType: "检测认证,技术咨询",
    description: "提供产品安全与质量认证服务",
    contactPerson: "李雷",
    contactPhone: "13500135005",
    logo: "https://via.placeholder.com/100?text=TUV",
    isAbroad: 1,
    countryCoverage: "德国,欧盟,美国,日本",
    auditStatus: "approved",
  },
  {
    id: 2004,
    companyName: "中外运物流",
    region: "深圳",
    serviceType: "物流供应链",
    description: "国际货运代理、跨境电商物流",
    contactPerson: "韩梅梅",
    contactPhone: "13400134006",
    logo: "https://via.placeholder.com/100?text=SINOTRANS",
    isAbroad: 1,
    countryCoverage: "美国,欧洲,东南亚,中东",
    auditStatus: "approved",
  },
  {
    id: 2005,
    companyName: "洛可可设计",
    region: "北京",
    serviceType: "工业设计",
    description: "产品设计、品牌设计，助力企业出海",
    contactPerson: "周杰",
    contactPhone: "13300133007",
    logo: "https://via.placeholder.com/100?text=LKK",
    isAbroad: 1,
    countryCoverage: "美国,日本,德国",
    auditStatus: "approved",
  },
  {
    id: 2006,
    companyName: "阿里巴巴国际站",
    region: "杭州",
    serviceType: "跨境电商平台",
    description: "B2B跨境贸易平台，帮助企业拓展海外市场",
    contactPerson: "马云",
    contactPhone: "13200132008",
    logo: "https://via.placeholder.com/100?text=Alibaba",
    isAbroad: 1,
    countryCoverage: "全球",
    auditStatus: "approved",
  },
];

// 模拟出海案例数据
const mockCases = [
  {
    id: 7001,
    title: "某电子公司CE认证成功案例",
    companyName: "东莞电子",
    companyType: "manufacture",
    country: "欧盟",
    serviceType: "CE认证",
    description:
      "通过华测检测服务，顺利获得CE认证，产品成功进入欧洲市场。该企业主要生产消费电子产品，经过3个月的认证流程，产品符合欧盟安全标准，现已出口德国、法国等多个国家。",
    coverImage: "https://via.placeholder.com/300x200?text=CE+Case",
    publishTime: "2026-02-10 10:00:00",
    status: 1,
  },
  {
    id: 7002,
    title: "智能家居产品FCC认证案例",
    companyName: "深圳智家科技",
    companyType: "manufacture",
    country: "美国",
    serviceType: "FCC认证",
    description:
      "借助SGS的专业服务，智能插座产品顺利通过FCC认证，成功进入美国市场。该产品符合美国联邦通信委员会标准，获得市场准入。",
    coverImage: "https://via.placeholder.com/300x200?text=FCC+Case",
    publishTime: "2026-01-15 14:30:00",
    status: 1,
  },
  {
    id: 7003,
    title: "新能源电池出海物流方案",
    companyName: "宁德时代",
    companyType: "manufacture",
    country: "德国",
    serviceType: "物流供应链",
    description:
      "中外运物流为宁德时代提供定制化的新能源电池国际运输方案，采用专业危险品运输标准，确保产品安全送达德国工厂。",
    coverImage: "https://via.placeholder.com/300x200?text=Logistics+Case",
    publishTime: "2025-12-20 09:00:00",
    status: 1,
  },
  {
    id: 7004,
    title: "工业设计助力扫地机器人出海",
    companyName: "石头科技",
    companyType: "manufacture",
    country: "日本",
    serviceType: "工业设计",
    description:
      "洛可可设计为石头科技提供符合日本市场审美的产品外观设计，产品上市后获得日本消费者好评，市场占有率提升15%。",
    coverImage: "https://via.placeholder.com/300x200?text=Design+Case",
    publishTime: "2026-02-28 11:00:00",
    status: 1,
  },
  {
    id: 7005,
    title: "跨境电商平台助力工厂出海",
    companyName: "义乌小商品",
    companyType: "manufacture",
    country: "东南亚",
    serviceType: "跨境电商平台",
    description:
      "通过阿里巴巴国际站，该工厂将产品销往泰国、越南、马来西亚等东南亚国家，年出口额增长200%。",
    coverImage: "https://via.placeholder.com/300x200?text=E-commerce+Case",
    publishTime: "2026-03-05 16:20:00",
    status: 1,
  },
  {
    id: 7006,
    title: "医疗器械欧盟MDR认证案例",
    companyName: "迈瑞医疗",
    companyType: "manufacture",
    country: "欧盟",
    serviceType: "检测认证",
    description:
      "华测检测协助迈瑞医疗完成欧盟MDR新法规认证，多款监护仪、超声设备获得CE证书，顺利进入欧洲高端市场。",
    coverImage: "https://via.placeholder.com/300x200?text=MDR+Case",
    publishTime: "2026-01-20 13:45:00",
    status: 1,
  },
  {
    id: 7007,
    title: "消费电子UL认证案例",
    companyName: "安克创新",
    companyType: "manufacture",
    country: "美国",
    serviceType: "UL认证",
    description:
      "安克创新的充电产品通过UL认证，获得美国市场安全认可，产品在亚马逊平台销量领先。",
    coverImage: "https://via.placeholder.com/300x200?text=UL+Case",
    publishTime: "2026-02-18 10:00:00",
    status: 1,
  },
  {
    id: 7008,
    title: "汽车零部件出口物流优化案例",
    companyName: "福耀玻璃",
    companyType: "manufacture",
    country: "美国",
    serviceType: "物流供应链",
    description:
      "中外运为福耀玻璃提供中美专线海运方案，缩短运输周期30%，降低物流成本15%。",
    coverImage: "https://via.placeholder.com/300x200?text=Auto+Case",
    publishTime: "2025-11-10 08:30:00",
    status: 1,
  },
];

// 筛选服务商列表（支持分页和筛选）
const filterServiceProviders = (params) => {
  let list = [...mockServiceProviders];
  const { region, serviceType, page = 1, size = 10 } = params;

  // 区域筛选
  if (region) {
    list = list.filter((item) => item.region === region);
  }
  // 服务类型筛选（模糊匹配）
  if (serviceType) {
    list = list.filter(
      (item) => item.serviceType && item.serviceType.includes(serviceType),
    );
  }

  const total = list.length;
  const start = (page - 1) * size;
  const records = list.slice(start, start + size);

  return { total, records };
};

// 筛选案例列表（支持分页和筛选）
const filterCases = (params) => {
  let list = [...mockCases];
  const { country, serviceType, page = 1, size = 10 } = params;

  // 国家筛选
  if (country) {
    list = list.filter((item) => item.country === country);
  }
  // 服务类型筛选
  if (serviceType) {
    list = list.filter(
      (item) => item.serviceType && item.serviceType.includes(serviceType),
    );
  }

  const total = list.length;
  const start = (page - 1) * size;
  const records = list.slice(start, start + size);

  return { total, records };
};

export default [
  // 获取出海服务列表
  {
    url: "/api/abroad/services",
    method: "get",
    response: ({ query }) => {
      const { total, records } = filterServiceProviders(query);
      return {
        code: 200,
        message: "success",
        data: {
          total,
          records,
        },
      };
    },
  },
  // 获取出海成功案例列表
  {
    url: "/api/abroad/cases",
    method: "get",
    response: ({ query }) => {
      const { total, records } = filterCases(query);
      return {
        code: 200,
        message: "success",
        data: {
          total,
          records,
        },
      };
    },
  },
];
