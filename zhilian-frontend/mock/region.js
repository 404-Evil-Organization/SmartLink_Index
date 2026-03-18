import Mock from "mockjs";

// 模拟区域列表（用于 3.1 接口）
const regions = [
  "深圳",
  "东莞",
  "惠州",
  "广州",
  "佛山",
  "中山",
  "珠海",
  "江门",
  "肇庆",
];

// 生成随机区域指数列表
function generateRegionList() {
  return regions.map((region) => ({
    region,
    coopDensity: Mock.Random.float(0.3, 0.9, 2, 4),
    serviceRate: Mock.Random.float(0.3, 0.8, 2, 4),
    crossRate: Mock.Random.float(0.2, 0.6, 2, 4),
    totalIndex: Mock.Random.float(50, 85, 1, 2),
  }));
}

// 生成特定区域指数详情（带完整字段）
function generateRegionDetail(region) {
  const year = 2026;
  const quarter = Mock.Random.integer(1, 4);
  const month = Mock.Random.integer(1, 12);
  const periodType = Mock.Random.boolean() ? "quarter" : "month";
  const periodValue = periodType === "quarter" ? quarter : month;

  return {
    id: Mock.Random.integer(1, 1000),
    region,
    year,
    periodType,
    periodValue,
    coopDensity: Mock.Random.float(0.3, 0.9, 2, 4),
    serviceRate: Mock.Random.float(0.3, 0.8, 2, 4),
    crossRate: Mock.Random.float(0.2, 0.6, 2, 4),
    totalIndex: Mock.Random.float(50, 85, 1, 2),
    calcTime: Mock.Random.datetime("2026-04-01 HH:mm:ss"),
    createTime: Mock.Random.datetime("2026-04-01 HH:mm:ss"),
    updateTime: Mock.Random.datetime("2026-04-01 HH:mm:ss"),
  };
}

// 生成趋势数据（基于传入的区域）
function generateTrend(region) {
  const quarters = ["2025Q1", "2025Q2", "2025Q3", "2025Q4", "2026Q1"];
  const baseMap = { 深圳: 75, 东莞: 72, 广州: 74, 惠州: 68 };
  const base = baseMap[region] || 70;
  return quarters.map((date, index) => ({
    date,
    totalIndex: Mock.Random.float(base - 2 + index, base + 2 + index, 1, 2),
  }));
}

export default [
  // 3.1 获取所有区域指数
  {
    url: "/api/index/region/list",
    method: "get",
    response: ({ query }) => {
      return {
        code: 200,
        message: "success",
        data: generateRegionList(),
      };
    },
  },

  // 3.2 获取特定区域指数
  {
    url: "/api/index/region/:region", // 支持路径参数
    method: "get",
    response: ({ query, params }) => {
      const region = decodeURI(query.region);
      return {
        code: 200,
        message: "success",
        data: generateRegionDetail(region),
      };
    },
  },

  // 3.3 获取趋势数据
  {
    url: "/api/index/trend",
    method: "get",
    response: ({ query }) => {
      const { region, start, end } = query;
      if (!region) {
        return {
          code: 400,
          message: "region 参数不能为空",
          data: null,
        };
      }
      return {
        code: 200,
        message: "success",
        data: generateTrend(region),
      };
    },
  },
];
