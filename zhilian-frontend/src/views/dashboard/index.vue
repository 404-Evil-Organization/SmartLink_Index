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
            <div class="chart-placeholder">
              <div class="placeholder-content" v-if="heatmap.length">
                已获取 {{ heatmap.length }} 个区域数据（待渲染图表）
              </div>
              <div v-else class="placeholder-content">暂无数据</div>
            </div>
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
            <div class="chart-placeholder" style="height: 300px">
              <div class="placeholder-content" v-if="network.nodes.length">
                节点数：{{ network.nodes.length }}，连接数：{{
                  network.links.length
                }}（待渲染图表）
              </div>
              <div v-else class="placeholder-content">暂无数据</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
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

// 统一获取所有数据
const fetchAllData = async () => {
  loading.value = true;
  try {
    // 并行请求所有接口
    const [statsRes, heatmapRes, topDemandsRes, networkRes] = await Promise.all(
      [getStatistics(), getHeatmap(), getTopDemands({ top: 5 }), getNetwork()],
    );

    // 更新数据
    stats.manufactureCount = statsRes.manufactureCount;
    stats.serviceCount = statsRes.serviceCount;
    stats.demandCount = statsRes.demandCount;
    stats.cooperationCount = statsRes.cooperationCount;

    heatmap.value = heatmapRes;
    topDemands.value = topDemandsRes;
    network.value = networkRes;

    ElMessage.success("数据更新成功");
  } catch (error) {
    console.error("获取看板数据失败", error);
    ElMessage.error("获取数据失败");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchAllData();
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

.chart-placeholder {
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
