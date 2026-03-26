// mock/common.js
// 辅助接口模拟数据（根据接口文档 v1.0）

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

const scales = [
  { value: "micro", label: "微型企业" },
  { value: "small", label: "小型企业" },
  { value: "medium", label: "中型企业" },
  { value: "large", label: "大型企业" },
];

const serviceTags = [
  { id: 1, name: "检测认证", category: "服务类型" },
  { id: 2, name: "工业设计", category: "服务类型" },
  { id: 3, name: "物流供应链", category: "服务类型" },
];

const certificationTags = [
  { id: 4, name: "CNAS认证", category: "认证类型" },
  { id: 5, name: "CMA认证", category: "认证类型" },
  { id: 6, name: "ISO9001", category: "认证类型" },
];

const productTags = [
  { id: 7, name: "PCB电路板", category: "产品类型" },
  { id: 8, name: "半导体芯片", category: "产品类型" },
  { id: 9, name: "消费电子", category: "产品类型" },
];

const restsTags = [
  { id: 10, name: "热门推荐", category: "其他类型" },
  { id: 11, name: "新品上市", category: "其他类型" },
  { id: 12, name: "特惠活动", category: "其他类型" },
];

const tagCategories = [
  { value: "service", label: "服务类型" },
  { value: "certification", label: "认证类型" },
  { value: "product", label: "产品类型" },
  { value: "rests", label: "其他类型" },
];

const countries = ["美国", "欧盟", "日本", "东南亚"];

export default [
  // 1.5.1 获取区域列表
  {
    url: "/api/common/regions",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: regions,
    }),
  },
  // 1.5.2 获取企业规模枚举
  {
    url: "/api/common/scales",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: scales,
    }),
  },
  // 1.5.3 获取服务类型标签
  {
    url: "/api/common/service-tags",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: serviceTags,
    }),
  },
  // 1.5.4 OSS文件上传
  {
    url: "/api/common/upload",
    method: "post",
    response: () => ({
      code: 200,
      message: "success",
      data: {
        fileUrl:
          "https://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar.png?Expires=1773474080&OSSAccessKeyId=TMP.3Kmbu3cqd3X3CuAuyhCnwKtcndLrwRV8LHHrYtC6aq4Z9VYWF7cF1Rvo82xUssbRCKbhF8X5bHSQuEqQyymAf3B7eu3XDw&Signature=2Q3neetVJh1HTgVxUahkVwCJ5iM%3D",
      },
    }),
  },
  // 1.5.5 OSS文件删除
  {
    url: "/api/common/delete",
    method: "post",
    response: () => ({
      code: 200,
      message: "success",
      data: null,
    }),
  },
  // 1.5.6 获取认证类型标签
  {
    url: "/api/common/certification-tags",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: certificationTags,
    }),
  },
  // 1.5.7 获取产品类型标签
  {
    url: "/api/common/product-tags",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: productTags,
    }),
  },
  // 1.5.8 获取其他类型标签
  {
    url: "/api/common/rests-tags",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: restsTags,
    }),
  },
  // 1.5.9 获取标签类别选项
  {
    url: "/api/common/tag-categories",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: tagCategories,
    }),
  },
  // 1.5.10 获取所有国家列表
  {
    url: "/api/common/countries",
    method: "get",
    response: () => ({
      code: 200,
      message: "success",
      data: countries,
    }),
  },
];
