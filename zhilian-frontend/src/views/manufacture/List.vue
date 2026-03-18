<template>
  <div class="manufacture-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">制造企业列表</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>制造企业列表</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 统计卡片区域（数据静态，可替换为接口数据） -->
    <!-- <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in statistics" :key="stat.label">
        <el-card
          class="stat-card"
          :body-style="{ padding: '20px' }"
          shadow="hover"
        >
          <div class="stat-icon" :style="{ background: stat.bgColor }">
            <el-icon :size="24" :color="stat.color"
              ><component :is="stat.icon"
            /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-trend" v-if="stat.trend">
              <span :class="stat.trend > 0 ? 'up' : 'down'">
                {{ stat.trend > 0 ? "+" : "" }}{{ stat.trend }}%
              </span>
              较上月
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row> -->

    <!-- 搜索卡片 -->
    <el-card class="search-card" shadow="hover">
      <el-collapse-transition>
        <div v-show="searchExpanded">
          <el-form :model="searchForm" label-width="100px" class="search-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="区域">
                  <el-select
                    v-model="searchForm.region"
                    placeholder="选择区域"
                    clearable
                    filterable
                  >
                    <el-option
                      v-for="region in regionOptions"
                      :key="region"
                      :label="region"
                      :value="region"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="规模">
                  <el-select
                    v-model="searchForm.scale"
                    placeholder="选择规模"
                    clearable
                    filterable
                  >
                    <el-option
                      v-for="item in scaleOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="主营产品">
                  <el-select
                    v-model="searchForm.productType"
                    placeholder="选择主营产品"
                    clearable
                    filterable
                  >
                    <el-option
                      v-for="item in productTagsOptions"
                      :key="item.name"
                      :label="item.name"
                      :value="item.name"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="24" class="search-actions">
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">制造企业列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="companyName" label="企业名称" min-width="150" />
        <el-table-column
          prop="region"
          label="区域"
          width="100"
          :formatter="formatRegion"
        />
        <el-table-column
          prop="scale"
          label="规模"
          width="100"
          :formatter="formatScale"
        />
        <el-table-column prop="productType" label="主营产品" min-width="120">
          <template #default="{ row }">
            <el-tag
              v-for="tag in row.productType
                ? String(row.productType).split(',')
                : []"
              :key="tag"
              size="small"
              effect="plain"
              style="margin-right: 5px; margin-bottom: 3px"
            >
              {{ tag.trim() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130">
          <template #default="{ row }">
            <span>
              {{ showPhone(row.contactPhone) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button @click="openViewDialog(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页（右侧） -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="
            (size) => {
              pagination.current = 1;
              handlePageChange(size);
            }
          "
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 企业详情弹窗（只读） -->
    <el-dialog v-model="detailDialog.visible" title="企业详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="企业名称">{{
          detailDialog.data.companyName || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="区域">{{
          formatRegion(detailDialog.data)
        }}</el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">{{
          detailDialog.data.address || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{
          detailDialog.data.contactPerson || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{
          showPhone(detailDialog.data.contactPhone)
        }}</el-descriptions-item>
        <el-descriptions-item label="规模">{{
          formatScale(detailDialog.data)
        }}</el-descriptions-item>
        <el-descriptions-item label="员工人数">{{
          detailDialog.data.employeeCount || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="年收入(万元)">{{
          detailDialog.data.annualRevenue || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="主营产品类型">{{
          detailDialog.data.productType || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="企业简介" :span="2">{{
          detailDialog.data.description || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="企业logo">
          <el-image
            v-if="detailDialog.data.logo"
            :src="detailDialog.data.logo"
            fit="cover"
            style="width: 100px; height: 100px; border-radius: 4px"
          />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="成立日期">{{
          formatEstablishedDate(detailDialog.data.establishedDate)
        }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Refresh,
  Grid,
  Edit,
  Delete,
  View,
  Shop,
  User,
  Star,
  TrendCharts,
} from "@element-plus/icons-vue";
import { getManufactureList, getManufactureDetail } from "@/api/manufacture";
import { getRegions, getScales, getProductTags } from "@/api/common";
import { formatEstablishedDate } from "@/composables/date";

// ---------- 统计卡片数据（静态，可改为接口获取） ----------
// const statistics = ref([
//   {
//     icon: Shop,
//     label: "制造企业总数",
//     value: 186,
//     trend: 8,
//     bgColor: "#ecf5ff",
//     color: "#409eff",
//   },
//   {
//     icon: User,
//     label: "活跃企业",
//     value: 142,
//     trend: 5,
//     bgColor: "#f0f9eb",
//     color: "#67c23a",
//   },
//   {
//     icon: Star,
//     label: "平均评分",
//     value: 4.2,
//     trend: 2,
//     bgColor: "#fdf6ec",
//     color: "#e6a23c",
//   },
//   {
//     icon: TrendCharts,
//     label: "合作需求",
//     value: 67,
//     trend: -3,
//     bgColor: "#fef0f0",
//     color: "#f56c6c",
//   },
// ]);

// 搜索表单
const searchForm = reactive({
  region: "",
  scale: "",
  productType: "",
});

// ---------- 表格数据、分页、加载等 ----------
const loading = ref(false);
const tableData = ref([]);
const pagination = reactive({
  current: 1,
  size: 5,
  total: 0,
});

const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      region: searchForm.region || "",
      scale: searchForm.scale || "",
      productType: searchForm.productType || "",
    };
    const res = await getManufactureList(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    ElMessage.error("获取列表失败");
    console.error("获取列表失败", error);
  } finally {
    loading.value = false;
  }
};

// 选项数据
const regionOptions = ref([]);
const scaleOptions = ref([]);
const productTagsOptions = ref([]);

// 获取选项数据
const fetchOptions = async () => {
  try {
    const [regions, scales, productTags] = await Promise.all([
      getRegions(),
      getScales(),
      getProductTags(),
    ]);
    regionOptions.value = regions;
    scaleOptions.value = scales;
    productTagsOptions.value = productTags;
  } catch (error) {
    ElMessage.error("获取选项数据失败");
    console.error(error);
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchList();
};

const resetSearch = () => {
  searchForm.region = "";
  searchForm.scale = "";
  searchForm.productType = "";
  // searchForm.status = null;
  handleSearch();
};

const handlePageChange = () => {
  fetchList();
};

// ---------- 详情弹窗 ----------
const detailDialog = reactive({ visible: false, data: {} });
const openViewDialog = async (row) => {
  try {
    const res = await getManufactureDetail(row.id);
    detailDialog.data = res;
    detailDialog.visible = true;
  } catch (error) {
    ElMessage.error("获取企业详情失败");
    console.error("获取企业详情失败", error);
  }
};

import { maskPhone } from "@/utils/desensitize";
import { useUserStore } from "@/stores/user";
const userStore = useUserStore();
const showPhone = (phone) => {
  return maskPhone(phone, userStore.userInfo.role);
};

// ---------- 格式化 ----------
const formatRegion = (row) => {
  const map = { 深圳: "深圳", south: "华南", north: "华北", west: "西南" };
  return map[row.region] || row.region;
};
const formatScale = (row) => {
  const map = { micro: "微型", small: "小型", medium: "中型", large: "大型" };
  return map[row.scale] || row.scale;
};

const searchExpanded = ref(true);

onMounted(() => {
  fetchOptions();
  fetchList();
});
</script>

<style scoped>
.manufacture-dashboard {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
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

.stat-trend {
  font-size: 12px;
  color: #8590a6;
  margin-top: 6px;
}

.stat-trend .up {
  color: #f56c6c;
  font-weight: 500;
}

.stat-trend .down {
  color: #67c23a;
  font-weight: 500;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  background-color: #fafbfc;
  border-bottom: 1px solid #ebeef5;
}

.search-title {
  font-weight: 600;
  color: #1f2f3d;
}

.search-header .el-icon {
  transition: transform 0.3s;
}

.search-header .el-icon.is-active {
  transform: rotate(180deg);
}

.search-form {
  padding: 20px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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

.table-actions {
  display: flex;
  gap: 8px;
}

.expanded-detail {
  padding: 12px 40px;
  background-color: #fafbfc;
  font-size: 14px;
  color: #5e6d82;
  line-height: 1.8;
}

.expanded-detail p {
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}
</style>
