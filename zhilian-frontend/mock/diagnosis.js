// mock/diagnosis.js
// 数字化诊断模块模拟数据
const diagnosisRecords = [
  {
    id: 5001,
    manuId: 1001,
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
    id: 5002,
    manuId: 1002,
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
];

export default [
  // 2.1 提交诊断问卷
  {
    url: "/api/diagnosis/submit",
    method: "post",
    response: ({ body }) => {
      const { manuId, infoScore, autoScore, dataScore, serviceScore } = body;

      // 模拟计算总分（可根据实际算法，这里简单加权）
      const totalScore = Math.round(
        (infoScore + autoScore + dataScore + serviceScore) * 5,
      );

      // 模拟等级（可根据总分划分）
      let level = "";
      if (totalScore < 40) level = "起步期";
      else if (totalScore < 60) level = "成长期";
      else if (totalScore < 80) level = "成熟期";
      else level = "引领期";

      // 模拟生成建议（根据维度得分简单生成）
      const suggestions = [];
      if (infoScore < 3)
        suggestions.push("建议加强信息化建设，引入ERP/MES系统");
      if (autoScore < 3) suggestions.push("建议提升自动化水平，引入自动化设备");
      if (dataScore < 3) suggestions.push("建议加强数据采集与分析能力");
      if (serviceScore < 3)
        suggestions.push("建议拓展外部服务合作，提升协同能力");
      if (suggestions.length === 0)
        suggestions.push("企业各方面表现良好，建议持续优化");

      const newId = diagnosisRecords.length + 5001;
      const now = new Date().toISOString().replace("T", " ").substring(0, 19);

      const newRecord = {
        id: newId,
        manuId,
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

      return {
        code: 200,
        message: "success",
        data: {
          diagnosisId: newId,
          totalScore,
          level,
          radarData: {
            信息化: infoScore * 20, // 映射为0-100
            自动化: autoScore * 20,
            数据应用: dataScore * 20,
            服务协同: serviceScore * 20,
          },
          suggestions,
        },
      };
    },
  },
  // 2.2 获取诊断报告
  {
    url: "/api/diagnosis/:id",
    method: "get",
    response: ({ params }) => {
      const id = parseInt(params.id);
      const record = diagnosisRecords.find((item) => item.id === id);
      if (record) {
        return {
          code: 200,
          message: "success",
          data: {
            diagnosisId: record.id,
            manuId: record.manuId,
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
        return {
          code: 404,
          message: "诊断记录不存在",
          data: null,
        };
      }
    },
  },
];
