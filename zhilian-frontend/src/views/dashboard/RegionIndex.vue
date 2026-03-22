<template>
  <div class="dashboard-home">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">区域协同指数看板</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>数据看板</el-breadcrumb-item>
          <el-breadcrumb-item>区域协同指数</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-tooltip content="刷新">
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </el-tooltip>
      </div>
    </div>

    <!-- 加载状态（整体骨架屏） -->
    <div v-if="loading" class="loading-overlay">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 主要内容 -->
    <template v-else>
      <!-- 第一行：左侧柱状图 + 右侧详情卡片 -->
      <el-row :gutter="20" class="chart-row">
        <!-- 左侧：各区域综合指数（柱状图） -->
        <el-col :span="12">
          <el-card class="table-card" shadow="hover">
            <div class="table-toolbar">
              <div class="table-title">各区域综合指数</div>
              <div class="filter-group">
                <el-select
                  v-model="leftYear"
                  placeholder="年份"
                  size="small"
                  style="width: 90px; margin-right: 8px"
                  @change="handleLeftFilter"
                >
                  <el-option
                    v-for="year in yearOptions"
                    :key="year"
                    :label="year"
                    :value="year"
                  />
                </el-select>
                <el-select
                  v-model="leftPeriodType"
                  placeholder="周期"
                  size="small"
                  style="width: 80px; margin-right: 8px"
                  @change="handleLeftPeriodTypeChange"
                >
                  <el-option label="季度" value="quarter" />
                  <el-option label="月度" value="month" />
                </el-select>
                <el-select
                  v-model="leftPeriodValue"
                  placeholder="值"
                  size="small"
                  style="width: 90px; margin-right: 8px"
                  :disabled="!leftPeriodType"
                  @change="handleLeftFilter"
                >
                  <el-option
                    v-for="value in leftPeriodOptions"
                    :key="value"
                    :label="
                      leftPeriodType === 'quarter'
                        ? '第' + value + '季'
                        : value + '月'
                    "
                    :value="value"
                  />
                </el-select>
                <el-button
                  type="primary"
                  size="small"
                  @click="handleLeftFilter"
                >
                  查询
                </el-button>
                <el-button size="small" @click="resetLeftFilter"
                  >重置</el-button
                >
              </div>
            </div>
            <div
              ref="barChartRef"
              class="chart-placeholder"
              style="height: 300px"
            ></div>
          </el-card>
        </el-col>

        <!-- 右侧：区域指数详情（接口3.2） -->
        <el-col :span="12">
          <el-card class="table-card" shadow="hover">
            <div class="table-toolbar">
              <div class="table-title">区域指数详情</div>
              <div class="filter-group">
                <el-select
                  v-model="rightYear"
                  placeholder="年份"
                  size="small"
                  style="width: 90px; margin-right: 8px"
                  @change="handleRightFilter"
                >
                  <el-option
                    v-for="year in yearOptions"
                    :key="year"
                    :label="year"
                    :value="year"
                  />
                </el-select>
                <el-select
                  v-model="rightPeriodType"
                  placeholder="周期"
                  size="small"
                  style="width: 80px; margin-right: 8px"
                  @change="handleRightPeriodTypeChange"
                >
                  <el-option label="季度" value="quarter" />
                  <el-option label="月度" value="month" />
                </el-select>
                <el-select
                  v-model="rightPeriodValue"
                  placeholder="值"
                  size="small"
                  style="width: 90px; margin-right: 8px"
                  :disabled="!rightPeriodType"
                  @change="handleRightFilter"
                >
                  <el-option
                    v-for="value in rightPeriodOptions"
                    :key="value"
                    :label="
                      rightPeriodType === 'quarter'
                        ? '第' + value + '季'
                        : value + '月'
                    "
                    :value="value"
                  />
                </el-select>
                <el-button
                  type="primary"
                  size="small"
                  @click="handleRightFilter"
                >
                  查询
                </el-button>
                <el-button size="small" @click="resetRightFilter"
                  >重置</el-button
                >
              </div>
            </div>
            <!-- 区域选择下拉 -->
            <div
              style="
                padding: 0 20px 10px;
                display: flex;
                justify-content: flex-end;
              "
            >
              <el-select
                v-model="selectedRegion"
                placeholder="请选择区域"
                size="small"
                style="width: 120px"
                @change="handleRegionChange"
              >
                <el-option
                  v-for="item in regionOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </div>
            <!-- 详情卡片 -->
            <div
              v-if="regionDetail"
              class="detail-card"
              style="margin: 0 20px 20px"
            >
              <h4>{{ selectedRegion }} 最新指数</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">综合指数</span>
                  <span class="value">{{ regionDetail.totalIndex }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">合作密度</span>
                  <span class="value">{{ regionDetail.coopDensity }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">服务渗透率</span>
                  <span class="value">{{ regionDetail.serviceRate }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">跨域协同度</span>
                  <span class="value">{{ regionDetail.crossRate }}</span>
                </div>
              </div>
              <div class="detail-meta">
                {{ regionDetail.periodType === "quarter" ? "季度" : "月度" }}：
                {{ regionDetail.year }}年
                {{
                  regionDetail.periodType === "quarter"
                    ? "第" + regionDetail.periodValue + "季度"
                    : regionDetail.periodValue + "月"
                }}
                （计算时间：{{ regionDetail.calcTime }}）
              </div>
            </div>
            <div
              v-else
              class="detail-card"
              style="margin: 0 20px 20px; text-align: center; color: #999"
            >
              请选择区域并查询
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 第二行：区域指数趋势（接口3.3）- 独立区块 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="24">
          <el-card class="table-card" shadow="hover">
            <div class="table-toolbar">
              <div class="table-title">区域指数趋势</div>
              <div class="filter-group">
                <el-select
                  v-model="trendRegion"
                  placeholder="选择区域"
                  size="small"
                  style="width: 120px; margin-right: 8px"
                >
                  <el-option
                    v-for="item in regionOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
                <span style="margin-right: 4px; color: #606266"
                  >开始日期：</span
                >
                <el-date-picker
                  v-model="trendStartDate"
                  type="date"
                  placeholder="开始日期"
                  size="small"
                  style="width: 130px; margin-right: 8px"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disabledStartDate"
                />
                <span style="margin-right: 4px; color: #606266"
                  >结束日期：</span
                >
                <el-date-picker
                  v-model="trendEndDate"
                  type="date"
                  placeholder="结束日期"
                  size="small"
                  style="width: 130px; margin-right: 8px"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disabledEndDate"
                />
                <el-button
                  type="primary"
                  size="small"
                  @click="fetchTrendByDateRange"
                >
                  查询
                </el-button>
              </div>
            </div>
            <div
              v-loading="trendLoading"
              class="chart-placeholder"
              style="height: 350px; position: relative"
            >
              <div ref="trendChartRef" style="width: 100%; height: 100%"></div>
              <div
                v-if="trendData.length === 0 && !trendLoading"
                class="chart-placeholder-text"
              >
                暂无趋势数据
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, watch, computed } from "vue";
import * as echarts from "echarts";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import { getRegionIndexList, getRegionIndex, getTrendData } from "@/api/region";

// 图表实例
const barChartRef = ref(null);
const trendChartRef = ref(null);
let barChart = null;
let trendChart = null;

// 数据状态
const regionList = ref([]);
const regionOptions = ref([]);
const selectedRegion = ref("");
const regionDetail = ref(null);
const loading = ref(false);

// 左侧筛选状态
const leftYear = ref(new Date().getFullYear());
const leftPeriodType = ref("quarter");
const leftPeriodValue = ref(1);

// 右侧筛选状态
const rightYear = ref(new Date().getFullYear());
const rightPeriodType = ref("quarter");
const rightPeriodValue = ref(1);

// 趋势区块状态
const trendRegion = ref("");
const trendStartDate = ref("");
const trendEndDate = ref("");
const trendData = ref([]);
const trendLoading = ref(false);

// 年份选项（近5年）
const yearOptions = ref([]);
for (let i = 0; i < 5; i++) {
  yearOptions.value.push(new Date().getFullYear() - i);
}

// 左侧周期选项
const leftPeriodOptions = computed(() => {
  if (leftPeriodType.value === "quarter") {
    return [1, 2, 3, 4];
  } else {
    return Array.from({ length: 12 }, (_, i) => i + 1);
  }
});

// 右侧周期选项
const rightPeriodOptions = computed(() => {
  if (rightPeriodType.value === "quarter") {
    return [1, 2, 3, 4];
  } else {
    return Array.from({ length: 12 }, (_, i) => i + 1);
  }
});

// 处理左侧周期类型变化
const handleLeftPeriodTypeChange = () => {
  // 切换周期类型时将周期值重置为 1，保持与原有行为一致
  leftPeriodValue.value = 1;
  handleLeftFilter();
};
// 处理右侧周期类型变化
const handleRightPeriodTypeChange = () => {
  // 切换周期类型时将周期值重置为 1，保持与原有行为一致
  rightPeriodValue.value = 1;
  handleRightFilter();
};

// 获取左侧筛选参数
const getLeftParams = () => {
  const params = { year: leftYear.value };
  if (leftPeriodType.value === "quarter") {
    params.quarter = leftPeriodValue.value;
  } else {
    params.month = leftPeriodValue.value;
  }
  return params;
};

// 获取右侧筛选参数
const getRightParams = () => {
  const params = { year: rightYear.value };
  if (rightPeriodType.value === "quarter") {
    params.quarter = rightPeriodValue.value;
  } else {
    params.month = rightPeriodValue.value;
  }
  return params;
};

// 左侧筛选查询
const handleLeftFilter = async () => {
  const params = getLeftParams();
  await fetchRegionList(params);
};

// 重置左侧筛选
const resetLeftFilter = () => {
  leftYear.value = new Date().getFullYear();
  leftPeriodType.value = "quarter";
  leftPeriodValue.value = 1;
  handleLeftFilter();
};

// 右侧筛选查询
const handleRightFilter = async () => {
  if (selectedRegion.value) {
    await fetchRegionDetail(selectedRegion.value, getRightParams());
  } else {
    ElMessage.warning("请先选择区域");
  }
};

// 重置右侧筛选
const resetRightFilter = () => {
  rightYear.value = new Date().getFullYear();
  rightPeriodType.value = "quarter";
  rightPeriodValue.value = 1;
  handleRightFilter();
};

// 获取所有区域指数（接口3.1）
const fetchRegionList = async (params = {}) => {
  try {
    loading.value = true;
    const res = await getRegionIndexList(params);
    regionList.value = res;
    regionOptions.value = res.map((item) => item.region);
    if (regionOptions.value.length > 0 && !selectedRegion.value) {
      selectedRegion.value = regionOptions.value[0];
      trendRegion.value = regionOptions.value[0]; // 默认同步趋势区域
    }
    loading.value = false;
    await nextTick();
    initBarChart(true);

    if (selectedRegion.value) {
      await fetchRegionDetail(selectedRegion.value, getRightParams());
    }
  } catch (error) {
    console.error("获取区域指数失败", error);
    ElMessage.error(error.message || "获取区域指数失败");
    loading.value = false;
  }
};

// 获取特定区域指数（接口3.2）
const fetchRegionDetail = async (region, params = {}) => {
  if (!region) return;
  try {
    const res = await getRegionIndex(region, params);
    regionDetail.value = res;
  } catch (error) {
    console.error("获取区域详情失败", error);
    ElMessage.error(error.message || "获取区域详情失败");
  }
};

// 日期禁用规则：开始日期不能晚于结束日期
const disabledStartDate = (time) => {
  if (trendEndDate.value) {
    const end = new Date(trendEndDate.value);
    return time.getTime() > end.getTime();
  }
  return false;
};

const disabledEndDate = (time) => {
  if (trendStartDate.value) {
    const start = new Date(trendStartDate.value);
    return time.getTime() < start.getTime();
  }
  return false;
};

// 获取趋势数据（接口3.3）- 基于日期范围
const fetchTrendByDateRange = async () => {
  if (!trendRegion.value) {
    ElMessage.warning("请选择区域");
    return;
  }
  if (!trendStartDate.value || !trendEndDate.value) {
    ElMessage.warning("请选择开始和结束日期");
    return;
  }

  // 校验结束日期不能早于开始日期
  const start = new Date(trendStartDate.value);
  const end = new Date(trendEndDate.value);
  if (end < start) {
    ElMessage.error("结束日期不能早于开始日期");
    return;
  }

  trendLoading.value = true;
  try {
    const res = await getTrendData({
      region: trendRegion.value,
      start: trendStartDate.value,
      end: trendEndDate.value,
    });
    trendData.value = res;
  } catch (error) {
    console.error("获取趋势数据失败", error);
    ElMessage.error(error.message || "获取趋势数据失败");
  } finally {
    trendLoading.value = false;
  }
};

// 初始化柱状图
const initBarChart = (force = false) => {
  if (!barChartRef.value || regionList.value.length === 0) return;
  if (force && barChart) {
    barChart.dispose();
    barChart = null;
  }
  if (!barChart) {
    barChart = echarts.init(barChartRef.value);
  }

  const xAxisData = regionList.value.map((item) => item.region);
  const seriesData = regionList.value.map((item) => item.totalIndex);

  // 动态计算 y 轴最小值
  let yMin = Math.min(...seriesData);
  let yMax = Math.max(...seriesData);
  // 如果最小值大于 0，为最小值留出 10% 的下边距
  if (yMin > 0) {
    const padding = (yMax - yMin) * 0.1;
    yMin = Math.max(0, yMin - padding); // 确保不变成负数
  } else {
    // 如果最小值 <= 0，则下边距使用绝对值的一定比例
    const padding = (yMax - yMin) * 0.1;
    yMin = yMin - padding;
  }
  // 避免所有数据相等时范围过窄
  if (yMin === yMax) {
    yMin = yMin - 1;
    yMax = yMax + 1;
  }

  const option = {
    tooltip: {
      trigger: "item",
      formatter: (params) => {
        const data = regionList.value[params.dataIndex];
        return `${data.region}<br/>
                综合指数：${data.totalIndex}<br/>
                合作密度：${data.coopDensity}<br/>
                服务渗透率：${data.serviceRate}<br/>
                跨域协同度：${data.crossRate}`;
      },
    },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: {
      type: "category",
      data: xAxisData,
      axisLabel: { rotate: 30 },
    },
    yAxis: {
      type: "value",
      name: "综合指数",
      min: yMin,
      max: yMax,
    },
    series: [
      {
        name: "综合指数",
        type: "bar",
        data: seriesData,
        itemStyle: { color: "#409EFF" },
        barWidth: 30,
      },
    ],
  };
  barChart.setOption(option);
  barChart.resize();

  barChart.off("click");
  barChart.on("click", (params) => {
    const region = params.name;
    if (region && region !== selectedRegion.value) {
      selectedRegion.value = region;
      handleRegionChange();
    }
  });
};
// 初始化趋势折线图
const initTrendChart = (force = false) => {
  if (!trendChartRef.value) return;

  // 如果强制重建且已有实例，先销毁
  if (force && trendChart) {
    trendChart.dispose();
    trendChart = null;
  }

  // 数据为空时的处理
  if (trendData.value.length === 0) {
    // 如果图表实例存在，则清空数据，不销毁实例
    if (trendChart) {
      trendChart.setOption({
        series: [],
        xAxis: { data: [] },
        yAxis: { data: [] },
      });
    }
    // 占位层由模板中的 v-if 显示，这里直接返回
    return;
  }

  // 有数据时，确保实例存在
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value);
  }

  // 正常绘制折线图
  const xAxisData = trendData.value.map((item) => item.date);
  const seriesData = trendData.value.map((item) => item.totalIndex);

  const option = {
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
    xAxis: { type: "category", data: xAxisData },
    yAxis: { type: "value", name: "综合指数" },
    series: [
      {
        name: trendRegion.value || "综合指数",
        type: "line",
        data: seriesData,
        smooth: true,
        lineStyle: { color: "#67C23A", width: 3 },
        symbol: "circle",
        symbolSize: 8,
      },
    ],
  };
  trendChart.setOption(option);
  trendChart.resize();
};

// 区域切换处理（右侧详情）
const handleRegionChange = async () => {
  await fetchRegionDetail(selectedRegion.value, getRightParams());
};

// 刷新全部数据
const handleRefresh = async () => {
  await fetchRegionList(getLeftParams());
  if (selectedRegion.value) {
    await fetchRegionDetail(selectedRegion.value, getRightParams());
  }
  if (trendRegion.value && trendStartDate.value && trendEndDate.value) {
    await fetchTrendByDateRange();
  } else {
    // 如果趋势筛选条件不全，可以选择不清求或提示
  }
  ElMessage.success("刷新成功");
};

// 监听趋势数据变化，无论是否为空都重新渲染
watch(
  trendData,
  () => {
    nextTick(() => {
      initTrendChart(true);
    });
  },
  { immediate: true },
);

// 窗口大小变化调整图表
const handleResize = () => {
  barChart?.resize();
  trendChart?.resize();
};

onMounted(async () => {
  await fetchRegionList(getLeftParams());
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  barChart?.dispose();
  trendChart?.dispose();
});
</script>

<style scoped>
.dashboard-home {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
  position: relative;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  z-index: 10;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  font-weight: 400;
  color: #8590a6;
}

.header-right {
  display: flex;
  gap: 12px;
}

.chart-row {
  margin-bottom: 24px;
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  flex-wrap: wrap;
  gap: 10px;
}

.table-title {
  font-weight: 600;
  color: #1f2f3d;
}

.filter-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.chart-placeholder {
  width: 100%;
  background-color: #fafbfc;
  color: #909399;
  font-size: 14px;
}

.detail-card {
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.detail-card h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px dashed #eee;
}

.detail-item .label {
  color: #999;
  font-size: 13px;
}

.detail-item .value {
  font-weight: bold;
  color: #333;
}

.detail-meta {
  margin-top: 12px;
  font-size: 12px;
  color: #999;
  text-align: right;
}
.chart-placeholder-text {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafbfc;
  color: #909399;
  font-size: 14px;
  pointer-events: none;
  z-index: 1;
}
</style>
