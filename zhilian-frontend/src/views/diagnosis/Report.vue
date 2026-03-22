<template>
  <div class="diagnosis-report">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon :size="28" color="#409EFF">
            <DataLine />
          </el-icon>
          数字化诊断报告
        </h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>数字化诊断</el-breadcrumb-item>
          <el-breadcrumb-item>诊断报告</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 加载中状态（企业列表） -->
    <el-card v-if="loadingEnterprises" class="enterprise-card" shadow="hover">
      <el-skeleton :rows="1" animated />
    </el-card>

    <!-- 企业选择卡片：只要存在企业列表即可显示，用于随时切换企业查看报告 -->
    <el-card
      v-else-if="enterpriseOptions.length > 0"
      class="enterprise-card fancy-card"
      shadow="hover"
    >
      <div class="enterprise-selector">
        <div class="selector-left">
          <el-icon size="20" color="#409EFF">
            <OfficeBuilding />
          </el-icon>
          <span class="label">选择企业：</span>
        </div>
        <el-select
          v-model="selectedManuId"
          placeholder="请选择企业"
          style="width: 300px"
          :loading="loadingEnterprises"
          clearable
          filterable
        >
          <el-option
            v-for="item in enterpriseOptions"
            :key="item.id"
            :label="item.companyName"
            :value="item.id"
          />
        </el-select>
        <el-button
          type="primary"
          :loading="viewLoading"
          @click="handleViewReport"
          :icon="View"
          round
        >
          查看报告
        </el-button>
      </div>
    </el-card>

    <!-- 无企业提示卡片：仅当没有企业且没有报告数据时显示 -->
    <el-card
      v-else-if="enterpriseOptions.length === 0 && !reportData && !showNoReport"
      class="enterprise-card fancy-card"
      shadow="hover"
    >
      <el-result
        icon="warning"
        title="您尚未创建制造企业"
        sub-title="请先创建企业后再查看诊断报告"
      >
        <template #extra>
          <el-button type="primary" @click="goToEnterpriseManage" round
            >前往创建</el-button
          >
        </template>
      </el-result>
    </el-card>

    <!-- 报告卡片：仅在有报告数据或需要显示“无报告”提示时渲染 -->
    <el-card
      v-if="reportData || showNoReport"
      class="report-card fancy-card"
      shadow="hover"
      v-loading="loadingReport"
    >
      <!-- 有报告时显示报告内容 -->
      <div class="report-content" v-if="reportData">
        <!-- 基本信息卡片 -->
        <el-card shadow="never" class="info-card">
          <div class="basic-info">
            <div class="info-item">
              <el-icon>
                <OfficeBuilding />
              </el-icon>
              <span class="label">诊断企业：</span>
              <span class="value">{{ reportData.manuName || "未知" }}</span>
            </div>
            <div class="info-item">
              <el-icon>
                <Calendar />
              </el-icon>
              <span class="label">诊断时间：</span>
              <span class="value">{{
                formatDate(reportData.diagnosisDate) || "-"
              }}</span>
            </div>
          </div>
        </el-card>

        <!-- 综合得分与等级 -->
        <div class="score-section">
          <el-progress
            type="circle"
            :percentage="reportData.totalScore"
            :width="120"
            :stroke-width="8"
            color="#409EFF"
          >
            <span class="score-value">{{ reportData.totalScore }}</span>
          </el-progress>
          <div class="level-tag">
            <el-tag
              :type="getLevelType(reportData.level)"
              size="large"
              effect="dark"
              round
            >
              {{ reportData.level }}
            </el-tag>
          </div>
        </div>

        <!-- 雷达图 -->
        <el-card shadow="never" class="chart-card">
          <div class="chart-title">
            <el-icon>
              <TrendCharts />
            </el-icon>
            <span>各维度得分雷达图</span>
          </div>
          <div ref="radarChartRef" style="height: 300px; width: 100%"></div>
        </el-card>

        <!-- 各维度得分卡片 -->
        <div class="dimension-scores">
          <el-row :gutter="20">
            <el-col :span="6" v-for="dim in dimensions" :key="dim.name">
              <el-card
                shadow="hover"
                class="dimension-card"
                :body-style="{ padding: '16px' }"
              >
                <div class="dimension-header">
                  <el-icon :size="24" :color="dim.color">
                    <component :is="dim.icon" />
                  </el-icon>
                  <span class="dimension-label">{{ dim.label }}</span>
                </div>
                <div class="dimension-value">
                  {{ reportData[dim.field] || 0 }} / 5
                </div>
                <el-progress
                  :percentage="(reportData[dim.field] || 0) * 20"
                  :color="dim.color"
                  :stroke-width="8"
                  :show-text="false"
                  striped
                  striped-flow
                />
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 改进建议卡片 -->
        <el-card shadow="never" class="suggestions-card">
          <div class="suggestions-header">
            <el-icon>
              <ChatLineSquare />
            </el-icon>
            <h3>改进建议</h3>
          </div>
          <ul>
            <li v-for="(item, index) in reportData.suggestions" :key="index">
              <el-icon>
                <Check />
              </el-icon>
              {{ item }}
            </li>
          </ul>
        </el-card>
      </div>

      <!-- 无报告时的空状态（企业有但无报告） -->
      <div v-else-if="!loadingReport && showNoReport" class="no-report">
        <el-result
          icon="info"
          title="该企业暂无诊断报告"
          sub-title="请先完成诊断问卷"
        >
          <template #extra>
            <el-button type="primary" @click="goToQuestionnaire" round
              >前往诊断问卷</el-button
            >
          </template>
        </el-result>
      </div>

      <!-- 多个企业但未选择时的占位提示（可选） -->
      <div
        v-else-if="
          !loadingReport && enterpriseOptions.length > 1 && !showNoReport
        "
        class="no-report"
      >
        <el-result
          icon="info"
          title="请先选择企业"
          sub-title="从上方下拉框选择企业后点击查看报告"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {
  ref,
  onMounted,
  nextTick,
  computed,
  watch,
  onBeforeUnmount,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import {
  DataLine,
  OfficeBuilding,
  View,
  // InfoFilled,
  Calendar,
  TrendCharts,
  ChatLineSquare,
  Check,
  Aim,
  Monitor,
  DataBoard,
  Connection,
} from "@element-plus/icons-vue";
import { getDiagnosisResult, getLatestDiagnosis } from "@/api/diagnosis";
import { getManufactureList } from "@/api/manufacture";
import { getMyManufactureList } from "@/api/enterprise";
import { useUserStore } from "@/stores/user";
import { createTimeConverter } from "@/composables/date";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loadingReport = ref(false);
const reportData = ref(null);
const radarChartRef = ref(null);
const viewLoading = ref(false);
const showNoReport = ref(false);

// ---------- 企业列表 ----------
const enterprises = ref([]);
const loadingEnterprises = ref(true);
const selectedManuId = ref(null);

/**
 * 获取当前用户拥有的所有审核通过的企业
 */
const fetchEnterprises = async () => {
  loadingEnterprises.value = true;
  try {
    const userRole = userStore.userInfo?.role;
    let res;

    if (userRole === "admin") {
      // 管理员使用公共列表
      res = await getManufactureList({ page: 1, size: 100 });
      enterprises.value = res.records || [];
    } else {
      // 普通用户：优先使用个人企业接口
      res = await getMyManufactureList({ page: 1, size: 100 });
      let enterprisesTemp = (res.records || []).filter(
        (item) => item.auditStatus === "approved",
      );

      enterprises.value = enterprisesTemp;
    }

    // 单企业自动加载（仅当没有通过 URL 参数加载报告时）
    const hasUrlParams = !!(
      route.query.id ||
      route.query.manuId ||
      route.params.id ||
      route.params.manuId
    );
    if (
      enterprises.value.length === 1 &&
      !hasUrlParams &&
      !reportData.value &&
      !loadingReport.value
    ) {
      const singleManuId = enterprises.value[0].id;
      selectedManuId.value = singleManuId;
      await fetchLatestReportByManuId(singleManuId);
    }
  } catch (error) {
    ElMessage.error("获取企业列表失败");
  } finally {
    loadingEnterprises.value = false;
  }
};
const enterpriseOptions = computed(() => enterprises.value);

// ---------- 报告详情 ----------
const dimensions = [
  {
    name: "info",
    label: "信息化水平",
    field: "infoScore",
    icon: Aim,
    color: "#409EFF",
  },
  {
    name: "auto",
    label: "自动化水平",
    field: "autoScore",
    icon: Monitor,
    color: "#67C23A",
  },
  {
    name: "data",
    label: "数据应用",
    field: "dataScore",
    icon: DataBoard,
    color: "#E6A23C",
  },
  {
    name: "service",
    label: "服务协同",
    field: "serviceScore",
    icon: Connection,
    color: "#F56C6C",
  },
];

const getLevelType = (level) => {
  const map = {
    起步期: "info",
    成长期: "warning",
    成熟期: "success",
    引领期: "danger",
  };
  return map[level] || "info";
};

const formatDate = (dateStr) => {
  if (!dateStr) return "-";
  let normalized = dateStr;
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(dateStr)) {
    normalized = dateStr.replace(" ", "T");
  }
  const converter = createTimeConverter(normalized);
  return converter.toLocalYMDHMS() || "-";
};
/**
 * 统一错误处理（403 跳转，404 显示无报告，其他弹窗）
 *
 * 说明：当前 axios 封装在业务码非 200 时会抛出 Error 对象，
 * 我们已经修改 request.js 将后端的 code 附加在了 error.code 上。
 */
const handleReportError = (error) => {
  const status = error?.response?.status || error?.code;
  if (status === 403) {
    router.push("/403");
  } else if (status === 404) {
    // 处理无报告的特殊状态
    reportData.value = null;
    showNoReport.value = true;
  } else {
    // 其他错误（含 400/500 等），无论是否有 response 都给出友好提示
    const backendMessage =
      error?.response?.data?.message || error?.response?.data?.msg || "";
    const rawMessage = backendMessage || error?.message || "";
    const friendlyMessage =
      rawMessage && rawMessage !== "Network Error"
        ? rawMessage
        : "获取诊断报告失败，请检查网络后重试";
    ElMessage.error(friendlyMessage);
    reportData.value = null;
  }
};

const fetchReportById = async (id) => {
  if (!id) return;
  loadingReport.value = true;
  showNoReport.value = false;
  try {
    const res = await getDiagnosisResult(id);
    if (!res) {
      // 无报告
      reportData.value = null;
      showNoReport.value = true;
    } else {
      reportData.value = res;
      if (res.manuId) {
        selectedManuId.value = res.manuId;
      }
      localStorage.setItem("latestDiagnosisId", id);
      await nextTick();
      initRadarChart();
    }
  } catch (error) {
    handleReportError(error);
  } finally {
    loadingReport.value = false;
  }
};

const fetchLatestReportByManuId = async (manuId) => {
  if (!manuId) return;
  loadingReport.value = true;
  showNoReport.value = false;
  try {
    const res = await getLatestDiagnosis(manuId);
    if (!res) {
      reportData.value = null;
      showNoReport.value = true;
    } else {
      reportData.value = res;
      if (res.manuId) {
        selectedManuId.value = res.manuId;
      }
      localStorage.setItem("latestDiagnosisId", res.diagnosisId);
      await nextTick();
      initRadarChart();
    }
  } catch (error) {
    handleReportError(error);
  } finally {
    loadingReport.value = false;
  }
};

// /**
//  * 根据 URL 参数加载报告（优先于企业列表自动加载）
const loadReport = () => {
  let id = route.query.id || route.params.id;
  let manuId = route.query.manuId || route.params.manuId;

  // 如果路由路径是 /diagnosis/report，Vue Router 的动态参数（如 path: '/diagnosis/:id'）
  // 可能会错误地把 'report' 当作 id 的值
  if (id === "report") {
    id = null;
  }

  if (id) {
    fetchReportById(id);
  } else if (manuId) {
    fetchLatestReportByManuId(manuId);
  } else {
    reportData.value = null;
  }
};

watch(
  () => [
    route.query.id,
    route.query.manuId,
    route.params.id,
    route.params.manuId,
  ],
  () => {
    // 如果当前正在自动加载或手动加载报告，则跳过（避免重复）
    if (loadingReport.value) return;
    loadReport();
  },
  { immediate: true },
);

const handleViewReport = async () => {
  if (!selectedManuId.value) {
    ElMessage.warning("请先选择企业");
    return;
  }
  viewLoading.value = true;
  try {
    const latest = await getLatestDiagnosis(selectedManuId.value);
    if (!latest) {
      reportData.value = null;
      showNoReport.value = true;
    } else {
      reportData.value = latest;
      if (latest.manuId) {
        selectedManuId.value = latest.manuId;
      }
      localStorage.setItem("latestDiagnosisId", latest.diagnosisId);
      await nextTick();
      initRadarChart();
    }
  } catch (error) {
    handleReportError(error);
  } finally {
    viewLoading.value = false;
  }
};
const goToQuestionnaire = () => {
  if (selectedManuId.value) {
    router.push(`/diagnosis/questionnaire?manuId=${selectedManuId.value}`);
  } else {
    router.push("/diagnosis/questionnaire");
  }
};

const goToEnterpriseManage = () => {
  router.push("/enterprise");
};

// ---------- 雷达图实例管理 ----------

let radarChartInstance = null;

// 窗口大小改变时让图表自适应
const handleRadarResize = () => {
  radarChartInstance?.resize();
};

// 初始化雷达图（创建实例、绑定事件、绘制）
const initRadarChart = () => {
  if (!radarChartRef.value || !reportData.value) return;

  // 如果已有实例，先销毁
  if (radarChartInstance) {
    radarChartInstance.dispose();
    radarChartInstance = null;
  }

  // 创建新实例
  radarChartInstance = echarts.init(radarChartRef.value);

  // 确保 resize 事件只绑定一次
  window.removeEventListener("resize", handleRadarResize);
  window.addEventListener("resize", handleRadarResize);

  // 绘制图表
  updateRadarChart();
};

// 更新图表配置（仅用于绘制，不改变实例）
const updateRadarChart = () => {
  if (!radarChartInstance || !reportData.value) return;

  const indicator = dimensions.map((d) => ({ name: d.label, max: 5 }));
  const value = dimensions.map((d) => reportData.value[d.field] || 0);

  const option = {
    radar: {
      indicator,
      center: ["50%", "50%"],
      radius: "65%",
      shape: "circle",
      axisName: { color: "#606266", fontSize: 12 },
      splitArea: {
        areaStyle: {
          color: ["rgba(64,158,255,0.02)", "rgba(64,158,255,0.05)"],
        },
      },
    },
    series: [
      {
        type: "radar",
        data: [value],
        areaStyle: { color: "rgba(64,158,255,0.2)" },
        lineStyle: { color: "#409EFF", width: 2 },
        itemStyle: { color: "#409EFF" },
      },
    ],
  };

  radarChartInstance.setOption(option);
};

// 销毁雷达图实例，移除监听
const destroyRadarChart = () => {
  if (radarChartInstance) {
    radarChartInstance.dispose();
    radarChartInstance = null;
  }
  window.removeEventListener("resize", handleRadarResize);
};

// 监听 reportData 的变化，自动更新雷达图；实例的创建/销毁由外部逻辑控制
watch(
  reportData,
  async (newVal) => {
    if (newVal) {
      // 等待 DOM 更新后，仅更新图表配置，避免重复 dispose/init 与重复绑定 resize 事件
      await nextTick();
      updateRadarChart();
    } else {
      destroyRadarChart();
    }
  },
  { immediate: false },
); // 如果不需要立即执行，可以不写 immediate

// 组件卸载时清理
onBeforeUnmount(() => {
  destroyRadarChart();
});

onMounted(() => {
  //  if (route.query.id || route.query.manuId || route.params.id || route.params.manuId) {
  //   router.replace({ query: {} });
  // }
  fetchEnterprises();
});
</script>

<style scoped>
.diagnosis-report {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1f2f3d;
  line-height: 1.2;
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  font-weight: 400;
  color: #8590a6;
}

.fancy-card {
  border-radius: 16px;
  overflow: hidden;
  transition:
    transform 0.3s,
    box-shadow 0.3s;
}

.fancy-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1) !important;
}

.enterprise-card {
  margin-bottom: 20px;
  border: none;
}

.enterprise-selector {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  padding: 8px 0;
}

.selector-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selector-left .label {
  font-weight: 500;
  color: #303133;
}

.tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #909399;
}

.report-card {
  border: none;
}

.report-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  background: linear-gradient(to right, #f9f9fc, #ffffff);
  border: 1px solid #ebeef5;
  border-radius: 12px;
}

.basic-info {
  display: flex;
  gap: 40px;
  padding: 8px 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .el-icon {
  color: #409eff;
  font-size: 18px;
}

.info-item .label {
  color: #909399;
}

.info-item .value {
  font-weight: 500;
  color: #303133;
}

.score-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 48px;
  margin: 16px 0;
  padding: 24px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.score-value {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
}

.level-tag .el-tag {
  font-size: 28px;
  padding: 12px 32px;
  border-radius: 40px;
  font-weight: 600;
}

.chart-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-weight: 600;
  color: #1f2f3d;
}

.chart-title .el-icon {
  font-size: 20px;
  color: #409eff;
}

.dimension-scores {
  margin: 16px 0;
}

.dimension-card {
  border-radius: 12px;
  transition: transform 0.2s;
}

.dimension-card:hover {
  transform: scale(1.02);
}

.dimension-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.dimension-label {
  font-weight: 600;
  color: #303133;
}

.dimension-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin: 8px 0;
}

.suggestions-card {
  background: #f9f9fc;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.suggestions-header .el-icon {
  font-size: 20px;
  color: #409eff;
}

.suggestions-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2f3d;
}

.suggestions-card ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.suggestions-card li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0;
  color: #606266;
  font-size: 15px;
}

.suggestions-card li .el-icon {
  color: #67c23a;
  font-size: 16px;
}

.no-report {
  padding: 40px 0;
}
</style>
