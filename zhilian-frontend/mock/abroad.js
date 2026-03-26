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
];
