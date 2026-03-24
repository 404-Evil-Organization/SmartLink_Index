// mock/demand.js
import Mock from "mockjs";

// ---------- 模拟内存数据 ----------
let demands = [
  {
    id: 3001,
    manuId: 1001,
    manuName: "深圳电子科技",
    title: "寻求PCB设计服务",
    description: "需要专业PCB设计公司，有高速PCB设计经验者优先。",
    expectedBudget: 10.0,
    deadline: "2026-06-01",
    status: "published",
    auditStatus: "approved",
    auditRemark: null,
    auditTime: "2026-03-20 10:00:00",
    auditUserId: 1,
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
    deleted: null,
    tags: [
      { id: 1, name: "PCB设计" },
      { id: 2, name: "高速电路" },
    ],
  },
  {
    id: 3002,
    manuId: 1001,
    manuName: "深圳电子科技",
    title: "寻求SMT贴片加工服务",
    description: "需要高精度SMT贴片，批量1000片。",
    expectedBudget: 5.0,
    deadline: "2026-07-01",
    status: "draft",
    auditStatus: "pending",
    auditRemark: null,
    auditTime: null,
    auditUserId: null,
    createTime: "2026-03-10 14:30:00",
    updateTime: "2026-03-10 14:30:00",
    deleted: null,
    tags: [{ id: 3, name: "SMT贴片" }],
  },
];

// 辅助函数：获取当前登录用户（模拟制造企业）
const getCurrentUser = () => {
  return { id: 1001, role: "manufacture", username: "tech_company" };
};

// ---------- 接口 Mock ----------
export default [
  // 1. 发布需求
  {
    url: "/api/demand/publish",
    method: "post",
    response: ({ body }) => {
      const { manuId, title, description, expectedBudget, deadline, tags } =
        body;
      if (!manuId || !title) {
        return {
          code: 400,
          message: "制造企业ID和需求标题不能为空",
          data: null,
        };
      }
      const newId = demands.length + 3001;
      const now = new Date().toISOString().slice(0, 19).replace("T", " ");
      const newDemand = {
        id: newId,
        manuId,
        manuName: `企业${manuId}`,
        title,
        description: description || "",
        expectedBudget: expectedBudget || null,
        deadline: deadline || null,
        status: "draft",
        auditStatus: "pending",
        auditRemark: null,
        auditTime: null,
        auditUserId: null,
        createTime: now,
        updateTime: now,
        deleted: null,
        tags: tags ? tags.map((id) => ({ id, name: `标签${id}` })) : [],
      };
      demands.push(newDemand);
      return {
        code: 200,
        message: "success",
        data: { demandId: newId, auditStatus: "pending" },
      };
    },
  },

  // 2. 我的需求列表
  {
    url: "/api/demand/my-list",
    method: "get",
    response: ({ query }) => {
      const { page = 1, size = 10, status, keyword } = query;
      let list = demands.filter((d) => d.deleted === null);
      // 模拟当前用户（假设用户1001是制造企业）
      const currentUser = getCurrentUser();
      list = list.filter((d) => d.manuId === currentUser.id);
      if (status) {
        const statuses = status.split(",");
        list = list.filter((d) => statuses.includes(d.status));
      }
      if (keyword) {
        list = list.filter((d) => d.title.includes(keyword));
      }
      const total = list.length;
      const start = (page - 1) * size;
      const records = list.slice(start, start + size);
      return {
        code: 200,
        message: "success",
        data: { total, records },
      };
    },
  },

  // 3. 获取需求详情
  {
    url: "/api/demand/:id",
    method: "get",
    response: ({ query }) => {
      const id = parseInt(query.id);
      const demand = demands.find((d) => d.id === id && d.deleted === null);
      if (!demand) {
        return { code: 404, message: "需求不存在", data: null };
      }
      // 模拟权限：只能查看自己的需求
      const currentUser = getCurrentUser();
      if (demand.manuId !== currentUser.id) {
        return { code: 403, message: "无权限查看", data: null };
      }
      return {
        code: 200,
        message: "success",
        data: demand,
      };
    },
  },

  // 4. 更新需求
  {
    url: "/api/demand/:id",
    method: "put",
    response: ({ query, body }) => {
      const id = parseInt(query.id);
      const demand = demands.find((d) => d.id === id && d.deleted === null);
      if (!demand) {
        return { code: 404, message: "需求不存在", data: null };
      }
      // 校验状态
      if (demand.status !== "draft" && demand.status !== "published") {
        return { code: 400, message: "当前状态不允许编辑", data: null };
      }
      // 更新字段
      const { title, description, expectedBudget, deadline, tags } = body;
      if (title !== undefined) demand.title = title;
      if (description !== undefined) demand.description = description;
      if (expectedBudget !== undefined) demand.expectedBudget = expectedBudget;
      if (deadline !== undefined) demand.deadline = deadline;
      if (tags !== undefined) {
        demand.tags = tags.map((id) => ({ id, name: `标签${id}` }));
      }
      demand.updateTime = new Date()
        .toISOString()
        .slice(0, 19)
        .replace("T", " ");
      return {
        code: 200,
        message: "success",
        data: null,
      };
    },
  },

  // 5. 删除需求
  {
    url: "/api/demand/:id",
    method: "delete",
    response: ({ query }) => {
      const id = parseInt(query.id);
      const demand = demands.find((d) => d.id === id && d.deleted === null);
      if (!demand) {
        return { code: 404, message: "需求不存在", data: null };
      }
      if (demand.status !== "draft" && demand.status !== "published") {
        return { code: 400, message: "当前状态不允许删除", data: null };
      }
      demand.deleted = new Date().toISOString().slice(0, 19).replace("T", " ");
      return {
        code: 200,
        message: "success",
        data: null,
      };
    },
  },
];
