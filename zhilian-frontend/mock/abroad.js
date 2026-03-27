// mock/modules/abroad.js
// 出海服务模块 Mock 数据

import Mock from "mockjs";

// 模拟国家指南数据
const countryGuides = [
  {
    id: 1,
    country: "美国",
    requirements:
      "FCC认证（强制性）、UL认证（安全）、能源之星（能效），部分产品需FDA或EPA认证。测试标准遵循ANSI/UL、ASTM等。",
    process:
      "1. 确定产品类别与适用标准\n2. 选择认证机构（如UL、FCC授权实验室）\n3. 提交样品与技术文档\n4. 实验室测试\n5. 出具报告，获取证书\n6. 后续市场监督与年度审核",
    documents: [
      "产品说明书（英文）",
      "电路原理图与PCB layout",
      "关键元器件清单及认证证书",
      "产品标签与包装图样",
      "测试申请表",
    ],
  },
  {
    id: 2,
    country: "欧盟",
    requirements:
      "CE认证（强制性），涵盖LVD、EMC、RED、MD等指令；RoHS/REACH环保要求；能效ErP指令。部分产品需欧盟公告机构介入。",
    process:
      "1. 确定适用指令与协调标准\n2. 产品测试与风险评估\n3. 技术文件编制（TCF）\n4. 签署符合性声明（DoC）\n5. 加贴CE标志\n6. 欧盟代表（如非欧盟制造商）",
    documents: [
      "产品技术文件（设计图、规格书）",
      "风险评估报告",
      "测试报告",
      "符合性声明（DoC）",
      "欧盟授权代表协议（若适用）",
    ],
  },
  {
    id: 3,
    country: "日本",
    requirements:
      "PSE认证（圆形或菱形，取决于产品类别）、电波法/TELEC认证、安全法JIS认证。对于无线电设备需取得技术基准符合性认证。",
    process:
      "1. 确定产品类别（特定电气用品或非特定）\n2. 选择日本METI认可的实验室\n3. 提交样品及日文技术资料\n4. 测试及工厂检查（特定电气用品）\n5. 注册METI备案\n6. 加贴PSE标志",
    documents: [
      "日文说明书及标签",
      "产品结构图、电路图",
      "关键零部件清单",
      "工厂检查报告（如适用）",
      "METI备案申请表",
    ],
  },
  {
    id: 4,
    country: "东南亚",
    requirements:
      "不同国家差异较大：泰国TISI、马来西亚SIRIM、新加坡PSB/IMDA、越南CR等。多数要求安全、EMC、能效认证。",
    process:
      "1. 根据目标国确定认证方案\n2. 委托当地代表（如印尼需当地进口商）\n3. 样品测试（当地或国际实验室）\n4. 工厂检查（部分国家）\n5. 获取证书并维持年度费",
    documents: [
      "产品技术规格书",
      "测试报告（IEC/CISPR标准）",
      "当地代理授权书",
      "产品照片及标签",
      "进口商营业执照",
    ],
  },
  {
    id: 5,
    country: "韩国",
    requirements:
      "KC安全认证（强制性）、KCC（EMC/电信）、能效标签MEPS。涉及消费品、电气电子产品。",
    process:
      "1. 指定韩国当地代理商\n2. 申请KC/KCC认证\n3. 送样至韩国认可实验室测试\n4. 工厂检查（KC认证）\n5. 获取证书，进口报关时提交",
    documents: [
      "韩国代理商合同",
      "产品规格书（韩文）",
      "电路图、零部件清单",
      "测试报告（CB转KC可加速）",
      "工厂质量体系文件",
    ],
  },
];

// 处理分页及搜索
function getPaginatedGuides({ page = 1, size = 10, keyword = "" }) {
  let filtered = [...countryGuides];
  if (keyword) {
    filtered = filtered.filter((item) => item.country.includes(keyword));
  }
  const total = filtered.length;
  const start = (page - 1) * size;
  const records = filtered.slice(start, start + size);
  return {
    total,
    records: records.map((item) => ({
      ...item,
      // 摘要字段，仅返回前100字
      requirements:
        item.requirements.substring(0, 100) +
        (item.requirements.length > 100 ? "..." : ""),
    })),
  };
}

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
  // 获取国家准入指南列表（公开）
  {
    url: "/api/abroad/country-guide/list",
    method: "get",
    response: (req) => {
      const { page = 1, size = 10, keyword = "" } = req.query;
      const { total, records } = getPaginatedGuides({
        page: parseInt(page),
        size: parseInt(size),
        keyword: keyword.trim(),
      });
    },
  },

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
  // 获取特定国家准入指南（公开，接口文档6.2）
  {
    url: "/api/abroad/country/:country",
    method: "get",
    response: (res) => {
      const rawCountry = res.query.country;
      const country = decodeURIComponent(rawCountry);
      const guide = countryGuides.find(
        (g) => g.country === decodeURIComponent(country),
      );
      if (guide) {
        return {
          code: 200,
          message: "success",
          data: {
            id: guide.id,
            country: guide.country,
            requirements: guide.requirements,
            process: guide.process,
            documents: guide.documents,
          },
        };
      }
      return {
        code: 404,
        message: "国家指南不存在",
        data: null,
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
