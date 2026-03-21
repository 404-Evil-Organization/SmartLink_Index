// mock/enterprise.js
// 模拟个人企业列表数据（当前登录用户的企业）

// 模拟当前登录用户ID（假设为1001）
const CURRENT_USER_ID = 1001;

// 个人制造企业数据（包含所有审核状态）
let myManufactureList = [
  {
    id: 1001,
    companyName: "深圳电子科技",
    region: "深圳",
    scale: "medium",
    productType: "PCB电路板",
    contactPerson: "张三",
    contactPhone: "13800138001",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: "2026-03-17 15:44:52",
    createTime: "2026-03-01 10:00:00",
  },
  {
    id: 1004,
    companyName: "深圳华强电子",
    region: "深圳",
    scale: "small",
    productType: "消费电子",
    contactPerson: "赵六",
    contactPhone: "13500135004",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: "2026-03-17 15:44:52",
    createTime: "2026-03-01 10:00:00",
  },
  {
    id: 1005,
    companyName: "深圳赛格科技",
    region: "深圳",
    scale: "medium",
    productType: "半导体",
    contactPerson: "钱七",
    contactPhone: "13600136005",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: "2026-03-17 15:44:52",
    createTime: "2026-03-01 10:00:00",
  },
  {
    id: 1006,
    companyName: "广州新创科技",
    region: "广州",
    scale: "medium",
    productType: "软件开发",
    contactPerson: "孙八",
    contactPhone: "13700137006",
    auditStatus: "pending",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
  },
];

// 个人服务商数据（当前登录用户的服务商）
let myServiceList = [
  {
    id: 2001,
    companyName: "华测检测认证集团",
    region: "深圳",
    serviceType: "检测认证",
    contactPerson: "王五",
    contactPhone: "13700137003",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: "2026-03-17 15:44:52",
    createTime: "2026-03-01 10:00:00",
  },
  {
    id: 2002,
    companyName: "广和咨询",
    region: "广州",
    serviceType: "管理咨询",
    contactPerson: "周九",
    contactPhone: "13800138009",
    auditStatus: "pending",
    auditRemark: null,
    auditTime: null,
    createTime: "2026-03-01 10:00:00",
  },
];

export default [
  {
    url: "/api/enterprise/manufacture/list",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10 } = query;
      // 模拟当前登录用户的企业列表（这里假设 CURRENT_USER_ID 固定，实际可扩展）
      // 如果有多用户，可以根据 token 解析 userId，这里简化为使用固定数据
      let filtered = myManufactureList;

      const start = (page - 1) * size;
      const end = start + parseInt(size);
      const records = filtered.slice(start, end);

      return {
        code: 200,
        message: "success",
        data: {
          total: filtered.length,
          records,
        },
      };
    },
  },
];