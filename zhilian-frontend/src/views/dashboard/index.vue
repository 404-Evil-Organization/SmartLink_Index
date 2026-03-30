<template>
  <div class="dashboard-home">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">数据看板</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>数据看板</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-tooltip content="刷新">
          <el-button :icon="Refresh" circle @click="handleRefresh" />
        </el-tooltip>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 主要内容 -->
    <template v-else>
      <!-- 统计卡片区域 -->
      <el-row :gutter="20" class="stat-cards">
        <el-col :span="6">
          <el-card
            class="stat-card"
            :body-style="{ padding: '20px' }"
            shadow="hover"
          >
            <div class="stat-icon" style="background: #ecf5ff">
              <el-icon :size="24" color="#409eff"><Shop /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.manufactureCount }}</div>
              <div class="stat-label">制造企业总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card
            class="stat-card"
            :body-style="{ padding: '20px' }"
            shadow="hover"
          >
            <div class="stat-icon" style="background: #f0f9eb">
              <el-icon :size="24" color="#67c23a"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.serviceCount }}</div>
              <div class="stat-label">服务商总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card
            class="stat-card"
            :body-style="{ padding: '20px' }"
            shadow="hover"
          >
            <div class="stat-icon" style="background: #fdf6ec">
              <el-icon :size="24" color="#e6a23c"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.demandCount }}</div>
              <div class="stat-label">合作需求</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card
            class="stat-card"
            :body-style="{ padding: '20px' }"
            shadow="hover"
          >
            <div class="stat-icon" style="background: #fef0f0">
              <el-icon :size="24" color="#f56c6c"><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.cooperationCount }}</div>
              <div class="stat-label">合作次数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 第二行：热力图 + 热门需求 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card class="table-card" shadow="hover">
            <div class="table-toolbar">
              <div class="table-title">区域合作热力图</div>
            </div>
            <div v-if="heatmap.length" class="chart-container">
              <div ref="heatmapChartRef" class="chart-box"></div>
            </div>
            <div v-else class="placeholder-content">暂无数据</div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="table-card" shadow="hover">
            <div class="table-toolbar">
              <div class="table-title">热门需求</div>
            </div>
            <div class="list-placeholder" v-if="topDemands.length">
              <div
                class="placeholder-item"
                v-for="item in topDemands"
                :key="item.serviceType"
              >
                {{ item.serviceType }}：{{ item.count }} 次
              </div>
            </div>
            <div v-else class="list-placeholder">暂无数据</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 第三行：网络关系 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="24">
          <el-card class="table-card" shadow="hover">
            <div class="table-toolbar">
              <div class="table-title">合作网络关系图</div>
            </div>
            <div
              v-if="network.nodes && network.nodes.length"
              class="chart-container"
              style="height: 320px"
            >
              <div ref="networkChartRef" class="chart-box"></div>
            </div>
            <div v-else class="placeholder-content">暂无数据</div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  onMounted,
  onBeforeUnmount,
  nextTick,
  watch,
} from "vue";
import { ElMessage } from "element-plus";
import {
  Refresh,
  Shop,
  User,
  Document,
  Connection,
} from "@element-plus/icons-vue";
import {
  getStatistics,
  getHeatmap,
  getTopDemands,
  getNetwork,
} from "@/api/dashboard";
import * as echarts from "echarts";

const loading = ref(false);

// 统计卡片数据
const stats = reactive({
  manufactureCount: 0,
  serviceCount: 0,
  demandCount: 0,
  cooperationCount: 0,
});

// 热力图数据
const heatmap = ref([]);
// 热门需求
const topDemands = ref([]);
// 网络关系
const network = ref({
  nodes: [],
  links: [],
});

// 图表实例
let heatmapChart = null;
let networkChart = null;

// 图表 DOM 引用
const heatmapChartRef = ref(null);
const networkChartRef = ref(null);

// 统一获取所有数据
const fetchAllData = async () => {
  loading.value = true;
  try {
    const [statsRes, heatmapRes, topDemandsRes, networkRes] = await Promise.all(
      [getStatistics(), getHeatmap(), getTopDemands({ top: 5 }), getNetwork()],
    );

    // 统一提取 data 字段（兼容包装和直接返回）
    const statsData = statsRes.data || statsRes;
    const heatmapData = heatmapRes.data || heatmapRes;
    const topDemandsData = topDemandsRes.data || topDemandsRes;
    const networkData = networkRes.data || networkRes;

    // 统计卡片
    stats.manufactureCount = statsData.manufactureCount ?? 0;
    stats.serviceCount = statsData.serviceCount ?? 0;
    stats.demandCount = statsData.demandCount ?? 0;
    stats.cooperationCount = statsData.cooperationCount ?? 0;

    // 热力图数据（兼容字段名）
    heatmap.value = (heatmapData || []).map((item) => ({
      region: item.region || item.name || item.area || "未知",
      value: item.value ?? item.count ?? item.heat ?? 0,
    }));

    // 热门需求
    topDemands.value = (topDemandsData || []).map((item) => ({
      serviceType: item.serviceType || item.type || "其他",
      count: item.count ?? item.value ?? 0,
    }));

    // 网络图数据
    const rawNodes = networkData.nodes || [];
    const rawLinks = networkData.links || [];

    // 建立 id 到 name 的映射
    const idToName = {};
    rawNodes.forEach((node) => {
      idToName[node.id] = node.name;
    });

    network.value = {
      nodes: rawNodes.map((node) => ({
        name: node.name,
        symbolSize: node.symbolSize ?? 30,
        category: node.category || 0,
        value: node.value ?? 1,
        id: node.id,
      })),
      links: rawLinks.map((link) => ({
        source: idToName[link.source] || link.source,
        target: idToName[link.target] || link.target,
        value: link.value ?? 1,
      })),
    };
  } catch (error) {
    console.error("获取看板数据失败", error);
    ElMessage.error("获取数据失败");
  } finally {
    loading.value = false;
  }
};

// 渲染区域合作热力图（柱状图）
const renderHeatmapChart = () => {
  try {
    // 数据为空时销毁图表（即使容器已卸载）
    if (!heatmap.value.length) {
      if (heatmapChart) {
        heatmapChart.dispose();
        heatmapChart = null;
      }
      return;
    }

    if (!heatmapChartRef.value) {
      console.warn("热力图容器未找到");
      return;
    }

    // 检查现有实例是否绑定到当前 DOM 元素
    if (heatmapChart && heatmapChart.getDom() !== heatmapChartRef.value) {
      heatmapChart.dispose();
      heatmapChart = null;
    }

    // 初始化或重置图表
    if (!heatmapChart) {
      heatmapChart = echarts.init(heatmapChartRef.value);
    }

    const regions = heatmap.value.map((item) => item.region);
    const values = heatmap.value.map((item) => item.value);

    const option = {
      tooltip: {
        trigger: "axis",
        axisPointer: { type: "shadow" },
        formatter: (params) => {
          const data = params[0];
          // 使用 ECharts 提供的 encodeHTML 对区域名称进行 HTML 转义，防止 XSS
          const safeName = echarts.format.encodeHTML(data?.name ?? "");
          return `${safeName}<br/>合作热度: ${data.value}`;
        },
      },
      grid: {
        left: "8%",
        right: "5%",
        top: "15%",
        bottom: "5%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        data: regions,
        axisLabel: {
          rotate: regions.length > 5 ? 25 : 0,
          interval: 0,
          fontSize: 11,
        },
        axisLine: { lineStyle: { color: "#909399" } },
      },
      yAxis: {
        type: "value",
        name: "合作次数",
        nameStyle: { fontSize: 12, color: "#606266" },
        splitLine: { lineStyle: { type: "dashed", color: "#e9e9e9" } },
      },
      series: [
        {
          name: "合作热度",
          type: "bar",
          data: values,
          barWidth: "40%",
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: {
              type: "linear",
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: "#f56c6c" },
                { offset: 0.5, color: "#e6a23c" },
                { offset: 1, color: "#67c23a" },
              ],
            },
            shadowColor: "rgba(0, 0, 0, 0.1)",
            shadowBlur: 4,
          },
          label: {
            show: true,
            position: "top",
            color: "#1f2f3d",
            fontSize: 11,
          },
        },
      ],
      backgroundColor: "transparent",
    };

    heatmapChart.setOption(option, true);
    heatmapChart.resize();
  } catch (error) {
    console.error("热力图渲染失败:", error);
  }
};

// 渲染合作网络关系图（力导向图）
const renderNetworkChart = () => {
  try {
    // 数据为空时销毁图表（即使容器已卸载）
    if (!network.value.nodes.length) {
      if (networkChart) {
        networkChart.dispose();
        networkChart = null;
      }
      return;
    }

    if (!networkChartRef.value) {
      console.warn("网络图容器未找到");
      return;
    }

    // 检查实例与 DOM 是否匹配
    if (networkChart && networkChart.getDom() !== networkChartRef.value) {
      networkChart.dispose();
      networkChart = null;
    }

    if (!networkChart) {
      networkChart = echarts.init(networkChartRef.value);
    }

    // 验证节点和链接数据
    const nodes = network.value.nodes.map((node) => ({
      name: node.name,
      symbolSize: node.symbolSize || 25,
      category: node.category || 0,
      value: node.value || 1,
    }));

    const links = network.value.links.map((link) => ({
      source: link.source,
      target: link.target,
      value: link.value || 1,
    }));

    /**
     * 基于 category 生成确定性的 HSL 颜色，避免每次渲染颜色随机变化
     * 使用简单字符串哈希将类别映射到 0-359 的色相值
     */
    const getCategoryColor = (category) => {
      const str = String(category);
      let hash = 0;
      for (let i = 0; i < str.length; i++) {
        hash = (hash << 5) - hash + str.charCodeAt(i);
        hash |= 0; // 保持为 32 位整数
      }
      const hue = Math.abs(hash) % 360;
      return `hsl(${hue}, 70%, 60%)`;
    };
    // 去重分类，并为每个分类分配稳定的颜色
    const categories = [...new Set(nodes.map((n) => n.category))].map(
      (cat) => ({
        name: String(cat),
        itemStyle: { color: getCategoryColor(cat) },
      }),
    );

    const option = {
      tooltip: {
        trigger: "item",
        renderMode: "richText", // 关键配置
        formatter: (params) => {
          if (params.dataType === "node") {
            return `企业/机构: ${params.name}\n合作次数: ${params.value || "-"}`;
          } else if (params.dataType === "edge") {
            return `合作关联: ${params.data.source} → ${params.data.target}\n强度: ${params.data.value}`;
          }
          return "";
        },
      },
      series: [
        {
          type: "graph",
          layout: "force",
          force: {
            repulsion: 300,
            edgeLength: 120,
            gravity: 0.1,
            friction: 0.1,
            layoutAnimation: true,
          },
          roam: true,
          draggable: true,
          data: nodes,
          links: links,
          categories: categories,
          label: {
            show: true,
            position: "right",
            fontSize: 11,
            offset: [5, 0],
            formatter: (params) => params.name,
          },
          emphasis: {
            focus: "adjacency",
            label: { show: true, fontWeight: "bold" },
          },
          lineStyle: {
            color: "source",
            curveness: 0.3,
            width: 1.5,
            opacity: 0.6,
          },
          edgeSymbol: ["none", "arrow"],
          edgeSymbolSize: [0, 8],
          itemStyle: {
            borderColor: "#fff",
            borderWidth: 1,
            shadowBlur: 8,
            shadowColor: "rgba(0, 0, 0, 0.2)",
          },
          symbolSize: 25,
          focusNodeAdjacency: true,
        },
      ],
      backgroundColor: "transparent",
    };

    networkChart.setOption(option, true);
    networkChart.resize();
  } catch (error) {
    console.error("网络图渲染失败:", error);
  }
};

// 窗口大小自适应
const handleResize = () => {
  if (heatmapChart) heatmapChart.resize();
  if (networkChart) networkChart.resize();
};

// 监听 loading 与数据变化，确保在加载完成且容器挂载后再渲染图表
watch([loading, heatmap], ([loadingVal, heatmapVal]) => {
  // 加载中或数据不存在时不渲染，避免容器未挂载
  if (loadingVal || !heatmapVal) return;
  nextTick(() => {
    renderHeatmapChart();
  });
}, { deep: true });

watch([loading, network], ([loadingVal, networkVal]) => {
  if (loadingVal || !networkVal) return;
  nextTick(() => {
    renderNetworkChart();
  });
}, { deep: true });

onMounted(() => {
  fetchAllData();
  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  if (heatmapChart) {
    heatmapChart.dispose();
    heatmapChart = null;
  }
  if (networkChart) {
    networkChart.dispose();
    networkChart = null;
  }
});

// 刷新按钮
const handleRefresh = fetchAllData;
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
.stat-cards {
  margin-bottom: 24px;
}
.stat-card {
  border-radius: 12px;
  transition:
    transform 0.3s,
    box-shadow 0.3s;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1f2f3d;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #8590a6;
  margin-top: 4px;
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
}
.table-title {
  font-weight: 600;
  color: #1f2f3d;
}
.chart-container {
  width: 100%;
  height: 250px;
  padding: 8px;
  background-color: #fafbfc;
}
.chart-box {
  width: 100%;
  height: 100%;
}
.placeholder-content {
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafbfc;
  color: #909399;
  font-size: 14px;
}
.list-placeholder {
  padding: 16px 20px;
  min-height: 218px;
  background-color: #fafbfc;
}
.placeholder-item {
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
  color: #606266;
  font-size: 14px;
}
.placeholder-item:last-child {
  border-bottom: none;
}
</style>
