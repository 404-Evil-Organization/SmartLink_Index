// mock/tag.js
// 模拟标签数据
let tagList = [
  {
    id: 1,
    name: "CNAS认证",
    category: "certification",
    description: "中国合格评定国家认可委员会认证",
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  {
    id: 2,
    name: "工业设计",
    category: "service",
    description: "产品外观、结构设计服务",
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  {
    id: 3,
    name: "PCB设计",
    category: "service",
    description: "印刷电路板设计服务",
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  {
    id: 4,
    name: "ISO9001",
    category: "certification",
    description: "质量管理体系认证",
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  {
    id: 5,
    name: "模具制造",
    category: "product",
    description: "模具设计与制造",
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  {
    id: 6,
    name: "物流服务",
    category: "service",
    description: "供应链物流服务",
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
  {
    id: 7,
    name: "其他标签",
    category: "other",
    description: "其他类别标签",
    createTime: "2026-03-01 10:00:00",
    updateTime: "2026-03-01 10:00:00",
  },
];

export default [
  // 获取标签列表（分页+筛选）
  {
    url: "/api/tag/list",
    method: "get",
    response: (req) => {
      const { page = 1, size = 10, name, category } = req.query;

      let filtered = tagList;

      if (name) {
        filtered = filtered.filter((item) => item.name.includes(name));
      }

      if (category) {
        filtered = filtered.filter((item) => item.category === category);
      }

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

  // 获取标签详情
  {
    url: "/api/tag/:id",
    method: "get",
    response: (req) => {
      const id = parseInt(req.query.id);

      const item = tagList.find((item) => item.id === id);

      if (item) {
        return {
          code: 200,
          message: "success",
          data: item,
        };
      }

      return {
        code: 404,
        message: "标签不存在",
        data: null,
      };
    },
  },

  // 新增标签
  {
    url: "/api/tag",
    method: "post",
    response: (req) => {
      const body = req.body;

      const newId = tagList.length
        ? Math.max(...tagList.map((i) => i.id)) + 1
        : 1;

      const now = new Date().toISOString().replace("T", " ").substring(0, 19);

      const newTag = {
        id: newId,
        name: body.name,
        category: body.category || "",
        description: body.description || "",
        createTime: now,
        updateTime: now,
      };

      tagList.push(newTag);

      return {
        code: 200,
        message: "success",
        data: { id: newId },
      };
    },
  },

  // 修改标签
  {
    url: "/api/tag/:id",
    method: "put",
    response: (req) => {
      const id = parseInt(req.query.id);
      const body = req.body;

      const index = tagList.findIndex((item) => item.id === id);

      if (index !== -1) {
        const now = new Date().toISOString().replace("T", " ").substring(0, 19);

        tagList[index] = {
          ...tagList[index],
          ...body,
          updateTime: now,
        };

        return {
          code: 200,
          message: "success",
          data: null,
        };
      }

      return {
        code: 404,
        message: "标签不存在",
        data: null,
      };
    },
  },

  // 删除标签
  {
    url: "/api/tag/:id",
    method: "delete",
    response: (req) => {
      const id = parseInt(req.query.id);

      const index = tagList.findIndex((item) => item.id === id);

      if (index !== -1) {
        tagList.splice(index, 1);

        return {
          code: 200,
          message: "success",
          data: null,
        };
      }

      return {
        code: 404,
        message: "标签不存在",
        data: null,
      };
    },
  },
];
