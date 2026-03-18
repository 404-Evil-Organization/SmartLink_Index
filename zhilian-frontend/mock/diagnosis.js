// mock/diagnosis.js
// 数字化诊断模块模拟数据（增强版）

// 模拟制造企业名称映射
const manufactureMap = {
  1001: "深圳电子科技",
  1002: "东莞精密机械",
  1003: "广州汽车配件",
};

// 初始诊断记录
const diagnosisRecords = [
  // 企业 1001 - 深圳电子科技（已有两条，增加一条较旧的记录）
  {
    id: 5001,
    manuId: 1001,
    manuName: "深圳电子科技",
    infoScore: 4,
    autoScore: 3,
    dataScore: 2,
    serviceScore: 3,
    totalScore: 65,
    level: "成熟期",
    suggestions: [
      "建议引入数据分析工具，提升数据应用能力",
      "可考虑将非核心业务外包，聚焦主业",
    ],
    diagnosisDate: "2026-03-07 14:30:00",
    createTime: "2026-03-07 14:30:00",
    updateTime: "2026-03-07 14:30:00",
  },
  {
    id: 5003, // 新增一条较新的记录
    manuId: 1001,
    manuName: "深圳电子科技",
    infoScore: 5,
    autoScore: 4,
    dataScore: 3,
    serviceScore: 4,
    totalScore: 80,
    level: "引领期",
    suggestions: [
      "继续保持信息化优势",
      "可探索智能制造升级",
    ],
    diagnosisDate: "2026-05-20 09:15:00",
    createTime: "2026-05-20 09:15:00",
    updateTime: "2026-05-20 09:15:00",
  },
  // 企业 1002 - 东莞精密机械（原有）
  {
    id: 5002,
    manuId: 1002,
    manuName: "东莞精密机械",
    infoScore: 5,
    autoScore: 4,
    dataScore: 3,
    serviceScore: 4,
    totalScore: 80,
    level: "引领期",
    suggestions: ["继续保持自动化优势", "可拓展海外市场"],
    diagnosisDate: "2026-03-08 10:00:00",
    createTime: "2026-03-08 10:00:00",
    updateTime: "2026-03-08 10:00:00",
  },
  {
    id: 5004, // 新增企业 1002 的另一个报告（较旧）
    manuId: 1002,
    manuName: "东莞精密机械",
    infoScore: 3,
    autoScore: 3,
    dataScore: 2,
    serviceScore: 2,
    totalScore: 50,
    level: "成长期",
    suggestions: [
      "建议提升数据应用能力",
      "加强服务协同",
    ],
    diagnosisDate: "2025-11-12 14:00:00",
    createTime: "2025-11-12 14:00:00",
    updateTime: "2025-11-12 14:00:00",
  },
  // 企业 1003 - 广州汽车配件（新增三条记录，不同时间）
  {
    id: 5005,
    manuId: 1003,
    manuName: "广州汽车配件",
    infoScore: 2,
    autoScore: 3,
    dataScore: 2,
    serviceScore: 2,
    totalScore: 45,
    level: "起步期",
    suggestions: [
      "急需加强信息化建设",
      "引入ERP系统",
      "考虑自动化改造",
    ],
    diagnosisDate: "2026-01-05 11:20:00",
    createTime: "2026-01-05 11:20:00",
    updateTime: "2026-01-05 11:20:00",
  },
  {
    id: 5006,
    manuId: 1003,
    manuName: "广州汽车配件",
    infoScore: 3,
    autoScore: 3,
    dataScore: 3,
    serviceScore: 3,
    totalScore: 60,
    level: "成长期",
    suggestions: [
      "继续推进信息化",
      "可尝试引入自动化设备",
    ],
    diagnosisDate: "2026-04-18 15:45:00",
    createTime: "2026-04-18 15:45:00",
    updateTime: "2026-04-18 15:45:00",
  },
  {
    id: 5007,
    manuId: 1003,
    manuName: "广州汽车配件",
    infoScore: 4,
    autoScore: 4,
    dataScore: 3,
    serviceScore: 4,
    totalScore: 75,
    level: "成熟期",
    suggestions: [
      "保持良好发展态势",
      "探索智能化升级",
    ],
    diagnosisDate: "2026-07-22 10:30:00",
    createTime: "2026-07-22 10:30:00",
    updateTime: "2026-07-22 10:30:00",
  },
];

export default [
  // 2.1 提交诊断问卷
  {
    url: "/api/diagnosis/submit",
    method: "post",
    response: ({ body }) => {
      const { manuId, infoScore, autoScore, dataScore, serviceScore } = body;

      // 根据 manuId 获取企业名称
      const manuName = manufactureMap[manuId] || `企业${manuId}`;

      // 计算总分
      const totalScore = Math.round(
        (infoScore + autoScore + dataScore + serviceScore) * 5,
      );

      // 确定等级
      let level = "";
      if (totalScore < 40) level = "起步期";
      else if (totalScore < 60) level = "成长期";
      else if (totalScore < 80) level = "成熟期";
      else level = "引领期";

      // 生成建议
      const suggestions = [];
      if (infoScore < 3)
        suggestions.push("建议加强信息化建设，引入ERP/MES系统");
      if (autoScore < 3) suggestions.push("建议提升自动化水平，引入自动化设备");
      if (dataScore < 3) suggestions.push("建议加强数据采集与分析能力");
      if (serviceScore < 3)
        suggestions.push("建议拓展外部服务合作，提升协同能力");
      if (suggestions.length === 0)
        suggestions.push("企业各方面表现良好，建议持续优化");

      // 生成新 ID（确保唯一）
      const newId = Math.max(...diagnosisRecords.map(r => r.id), 5000) + 1;
      const now = new Date().toISOString().replace("T", " ").substring(0, 19);

      const newRecord = {
        id: newId,
        manuId,
        manuName,
        infoScore,
        autoScore,
        dataScore,
        serviceScore,
        totalScore,
        level,
        suggestions,
        diagnosisDate: now,
        createTime: now,
        updateTime: now,
      };

      diagnosisRecords.push(newRecord);
      console.log("[mock] 新诊断记录已添加:", newRecord);

      return {
        code: 200,
        message: "success",
        data: {
          diagnosisId: newId,
          totalScore,
          level,
          radarData: {
            信息化: infoScore * 20,
            自动化: autoScore * 20,
            数据应用: dataScore * 20,
            服务协同: serviceScore * 20,
          },
          suggestions,
        },
      };
    },
  },

  // 2.2 获取诊断报告（根据ID）
  {
    url: "/api/diagnosis/result/:id",
    method: "get",
    response: ({ params, query }) => {
      // 兼容从 params 或 query 获取 id
      const id = parseInt(params?.id || query?.id);
      console.log("[mock] 请求诊断报告 ID:", id);
      console.log("[mock] 当前诊断记录:", diagnosisRecords.map(r => r.id));

      if (isNaN(id)) {
        return {
          code: 400,
          message: "无效的报告ID",
          data: null,
        };
      }

      const record = diagnosisRecords.find((item) => item.id === id);
      if (record) {
        return {
          code: 200,
          message: "success",
          data: {
            diagnosisId: record.id,
            manuId: record.manuId,
            manuName: record.manuName,
            infoScore: record.infoScore,
            autoScore: record.autoScore,
            dataScore: record.dataScore,
            serviceScore: record.serviceScore,
            totalScore: record.totalScore,
            level: record.level,
            suggestions: record.suggestions,
            diagnosisDate: record.diagnosisDate,
          },
        };
      } else {
        // ✅ 直接返回 404，不再返回第一条记录
        return {
          code: 404,
          message: "诊断记录不存在",
          data: null,
        };
      }
    },
  },

  // 2.3 获取企业最新诊断报告
  {
    url: "/api/diagnostics/latest",
    method: "get",
    response: ({ query }) => {
      const manuId = parseInt(query.manuId);
      console.log("[mock] 请求企业最新报告，manuId:", manuId);

      if (isNaN(manuId)) {
        return {
          code: 400,
          message: "无效的企业ID",
          data: null,
        };
      }

      // 筛选出该企业的所有诊断记录，按诊断日期倒序取第一条
      const records = diagnosisRecords.filter(record => record.manuId === manuId);
      if (records.length === 0) {
        return {
          code: 404,
          message: "该企业暂无诊断报告",
          data: null,
        };
      }

      // 按诊断日期倒序排序（最近的在最前）
      const sorted = [...records].sort((a, b) => 
        new Date(b.diagnosisDate) - new Date(a.diagnosisDate)
      );
      const latest = sorted[0];

      return {
        code: 200,
        message: "success",
        data: {
          diagnosisId: latest.id,
          manuId: latest.manuId,
          infoScore: latest.infoScore,
          autoScore: latest.autoScore,
          dataScore: latest.dataScore,
          serviceScore: latest.serviceScore,
          totalScore: latest.totalScore,
          level: latest.level,
          suggestions: latest.suggestions,
          diagnosisDate: latest.diagnosisDate,
        },
      };
    },
  },
];