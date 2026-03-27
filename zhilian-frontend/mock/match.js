// mock/demand.js
import Mock from "mockjs";

const Random = Mock.Random;

// 模拟需求数据存储
let demandList = [
  {
    id: 3001,
    manuId: 1001,
    title: "寻求PCB设计服务",
    description: "需要专业PCB设计公司，有高速PCB设计经验者优先。",
    expectedBudget: 10.0,
    deadline: "2026-06-01",
    createTime: "2026-03-01 10:00:00",
    auditStatus: "approved",
    status: "published",
    manufacture: {
      id: 1001,
      companyName: "深圳电子科技",
      region: "深圳",
      contactPerson: "张三",
      contactPhone: "13800138001",
    },
    tags: [
      { id: 1, name: "PCB设计" },
      { id: 2, name: "高速电路" },
    ],
  },
  {
    id: 3002,
    manuId: 1002,
    title: "寻求工业设计服务",
    description: "产品外观设计，要求简约现代风格。",
    expectedBudget: 5.0,
    deadline: "2026-07-15",
    createTime: "2026-03-05 14:30:00",
    auditStatus: "approved",
    status: "published",
    manufacture: {
      id: 1002,
      companyName: "东莞精密制造",
      region: "东莞",
      contactPerson: "李四",
      contactPhone: "13900139002",
    },
    tags: [{ id: 3, name: "工业设计" }],
  },
  {
    id: 3003,
    manuId: 1003,
    title: "CE认证服务需求",
    description: "需要CE认证服务，产品为消费电子。",
    expectedBudget: 8.0,
    deadline: "2026-08-20",
    createTime: "2026-03-10 09:15:00",
    auditStatus: "approved",
    status: "published",
    manufacture: {
      id: 1003,
      companyName: "广州智能科技",
      region: "广州",
      contactPerson: "王五",
      contactPhone: "13700137003",
    },
    tags: [{ id: 4, name: "国际认证" }],
  },
  {
    id: 3004,
    manuId: 1004,
    title: "物流供应链优化",
    description: "寻求供应链优化方案，降低物流成本。",
    expectedBudget: 20.0,
    deadline: "2026-09-10",
    createTime: "2026-03-12 11:00:00",
    auditStatus: "approved",
    status: "published",
    manufacture: {
      id: 1004,
      companyName: "惠州电子",
      region: "惠州",
      contactPerson: "赵六",
      contactPhone: "13600136004",
    },
    tags: [{ id: 5, name: "物流供应链" }],
  },
];

// 已接单的需求ID（用于模拟接单后的状态变更）
const acceptedDemandIds = new Set();

export default [
  // 4.5 获取合作市场需求列表
  {
    url: "/api/demand/market",
    method: "get",
    response: ({ query }) => {
      let {
        page = 1,
        size = 10,
        keyword,
        tagIds,
        expectedBudgetMin,
        expectedBudgetMax,
        deadlineStart,
        deadlineEnd,
      } = query;

      // 转换为数字
      page = Number(page);
      size = Number(size);
      expectedBudgetMin = expectedBudgetMin ? Number(expectedBudgetMin) : null;
      expectedBudgetMax = expectedBudgetMax ? Number(expectedBudgetMax) : null;

      // 过滤数据（仅返回审核通过且未匹配的需求）
      let filteredList = demandList.filter(
        (item) =>
          item.auditStatus === "approved" && item.status === "published",
      );

      // 关键词筛选
      if (keyword) {
        filteredList = filteredList.filter((item) =>
          item.title.includes(keyword),
        );
      }

      // 标签筛选（任意匹配）
      if (tagIds) {
        const tagIdArray = tagIds.split(",").map(Number);
        filteredList = filteredList.filter((item) =>
          item.tags.some((tag) => tagIdArray.includes(tag.id)),
        );
      }

      // 预算范围筛选
      if (expectedBudgetMin !== null) {
        filteredList = filteredList.filter(
          (item) => item.expectedBudget >= expectedBudgetMin,
        );
      }
      if (expectedBudgetMax !== null) {
        filteredList = filteredList.filter(
          (item) => item.expectedBudget <= expectedBudgetMax,
        );
      }

      // 截止日期范围筛选
      if (deadlineStart) {
        filteredList = filteredList.filter(
          (item) => item.deadline >= deadlineStart,
        );
      }
      if (deadlineEnd) {
        filteredList = filteredList.filter(
          (item) => item.deadline <= deadlineEnd,
        );
      }

      // 分页
      const total = filteredList.length;
      const start = (page - 1) * size;
      const end = start + size;
      const records = filteredList.slice(start, end);

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

  // 4.6 服务商接取需求
  {
    url: "/api/demand/accept",
    method: "post",
    response: ({ body, headers }) => {
      const { demandId, serviceId } = body;

      // 模拟简单的 token 校验（实际项目中应从 headers 中解析）
      if (!headers.authorization) {
        return {
          code: 401,
          message: "未授权，请先登录",
          data: null,
        };
      }

      // 查找需求
      const demandIndex = demandList.findIndex((d) => d.id === demandId);
      if (demandIndex === -1) {
        return {
          code: 404,
          message: "需求不存在",
          data: null,
        };
      }

      const demand = demandList[demandIndex];

      // 校验需求状态
      if (demand.status !== "published") {
        return {
          code: 400,
          message: "需求状态异常，无法接单",
          data: null,
        };
      }

      // 校验需求审核状态
      if (demand.auditStatus !== "approved") {
        return {
          code: 400,
          message: "需求未通过审核，无法接单",
          data: null,
        };
      }

      // 防止重复接单（内存模拟）
      if (acceptedDemandIds.has(demandId)) {
        return {
          code: 400,
          message: "该需求已被其他服务商接取",
          data: null,
        };
      }

      // 模拟校验服务商企业是否属于当前用户（这里简化，假设传入了正确的 serviceId）
      // 实际项目中需要从 token 解析当前用户，并验证 serviceId 是否属于该用户且审核通过
      // 此处简单模拟：假设 serviceId 有效

      // 更新需求状态为已匹配
      demand.status = "matched";
      acceptedDemandIds.add(demandId);

      // 模拟生成合作记录ID（简单的自增ID）
      const cooperationId = Random.integer(5000, 5999);

      return {
        code: 200,
        message: "success",
        data: {
          cooperationId,
        },
      };
    },
  },
];
