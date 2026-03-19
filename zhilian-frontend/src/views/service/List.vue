<template>
  <div class="service-provider-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">服务企业列表</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>服务企业列表</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <!-- 新增按钮已移除 -->
        <!-- <el-button :icon="Download">导出</el-button> -->
      </div>
    </div>

    <!-- 统计卡片区域（注释保留） -->
    <!-- <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in statistics" :key="stat.label">
        <el-card class="stat-card" :body-style="{ padding: '20px' }" shadow="hover">
          <div class="stat-icon" :style="{ background: stat.bgColor }">
            <el-icon :size="24" :color="stat.color"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-trend" v-if="stat.trend">
              <span :class="stat.trend > 0 ? 'up' : 'down'">
                {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
              </span>
              较上月
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row> -->

    <!-- 搜索卡片（可折叠） -->
    <el-card class="search-card" shadow="hover">
      <!-- 折叠头部（注释保留） -->
      <!-- <div class="search-header" @click="toggleSearch">
        <span class="search-title">高级筛选</span>
        <el-icon :class="{ 'is-active': searchExpanded }">
          <ArrowDown />
        </el-icon>
      </div> -->
      <el-collapse-transition>
        <div v-show="searchExpanded">
          <el-form :model="searchForm" label-width="100px" class="search-form">
            <el-row :gutter="20">
              <!-- <el-col :span="8">
                <el-form-item label="服务企业名称">
                  <el-input v-model="searchForm.companyName" placeholder="请输入" clearable />
                </el-form-item>
              </el-col> -->
              <el-col :span="8">
                <el-form-item label="所在区域">
                  <el-select
                    v-model="searchForm.region"
                    placeholder="全部"
                    clearable
                    filterable
                  >
                    <el-option
                      v-for="item in regionOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="服务类型">
                  <el-select
                    v-model="searchForm.serviceType"
                    placeholder="全部"
                    clearable
                    filterable
                  >
                    <el-option
                      v-for="item in serviceTypeOptions"
                      :key="item.id"
                      :label="item.name"
                      :value="item.name"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8" class="search-actions">
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-col>
            </el-row>
            <!-- <el-row :gutter="20">
              <el-col :span="24" class="search-actions">
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-col>
            </el-row> -->
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">服务企业列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
          <!-- <el-tooltip content="密度">
            <el-button :icon="Grid" circle />
          </el-tooltip> -->
        </div>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <!-- <el-table-column prop="id" label="ID" width="70" /> -->
        <!-- 加入序号列 -->
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column
          prop="companyName"
          label="服务企业名称"
          min-width="150"
        />
        <el-table-column prop="region" label="区域" width="90" />
        <el-table-column prop="serviceType" label="服务类型" min-width="180">
          <template #default="{ row }">
            <el-tag
              v-for="(tag, index) in normalizeTags(row.serviceType)"
              :key="tag + '-' + index"
              size="small"
              effect="plain"
              style="margin-right: 5px; margin-bottom: 3px"
            >
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130">
          <template #default="{ row }">
            {{ showPhone(row.contactPhone) }}
          </template>
        </el-table-column>
        <!-- 状态列已移除 -->
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="handleDetail(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 服务商详情弹窗（只读） -->
    <el-dialog
      v-model="detailDialog.visible"
      title="服务企业详情"
      width="600px"
    >
      <el-descriptions :column="2" border class="fixed-label-descriptions">
        <el-descriptions-item label="服务企业名称" :span="2">{{
          detailDialog.data.companyName || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="区域" :span="2" class="region-item-half">{{
          detailDialog.data.region || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">{{
          detailDialog.data.address || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="联系人" :span="2">{{
          detailDialog.data.contactPerson || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="联系电话" :span="2">{{
          showPhone(detailDialog.data.contactPhone)
        }}</el-descriptions-item>
        <el-descriptions-item label="服务类型" :span="2">{{
          detailDialog.data.serviceType || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="企业简介" :span="2">{{
          detailDialog.data.description || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="企业官网" :span="2">{{
          detailDialog.data.website || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="成立日期" :span="2">{{
          formatEstablishedDate(detailDialog.data.establishedDate)
        }}</el-descriptions-item>
        <el-descriptions-item label="员工人数" :span="2">{{
          detailDialog.data.employeeCount || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="资质概述" :span="2">{{
          detailDialog.data.qualification || "-"
        }}</el-descriptions-item>
        <!-- <el-descriptions-item label="年收入(万元)">{{ detailDialog.data.annualRevenue || "-" }}</el-descriptions-item> -->
        <el-descriptions-item label="企业logo" :span="2">
          <el-image
            v-if="detailDialog.data.logo"
            :src="detailDialog.data.logo"
            fit="cover"
            style="width: 100px; height: 100px; border-radius: 4px"
          />
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 证书列表折叠面板（只读，纵向卡片布局） -->
      <div class="certification-list">
        <el-collapse v-model="activeCertCollapse" class="cert-collapse">
          <el-collapse-item>
            <template #title>
              <div class="custom-collapse-title">
                <span>资质证书</span>
                <el-tooltip content="刷新">
                  <el-button
                    :icon="Refresh"
                    size="small"
                    circle
                    @click.stop="refreshCertList"
                    :loading="certLoading"
                  />
                </el-tooltip>
              </div>
            </template>
            <div class="cert-card-list">
              <div
                v-if="certificateList.length === 0 && !certLoading"
                class="empty-placeholder"
              >
                暂无证书
              </div>
              <div
                v-for="cert in certificateList"
                :key="cert.id"
                class="cert-item-card"
              >
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="证书名称">{{
                    cert.certName || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="证书编号">{{
                    cert.certNo || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="发证机构">{{
                    cert.issueAuthority || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="发证日期">{{
                    cert.issueDate || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="有效期">{{
                    cert.expireDate || "-"
                  }}</el-descriptions-item>
                  <el-descriptions-item label="证书文件">
                    <el-button @click="showPreview = true"> 查看 </el-button>
                    <el-image-viewer
                      v-if="showPreview"
                      :url-list="[cert.certFileUrl]"
                      show-progress
                      @close="showPreview = false"
                    />
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElImageViewer } from "element-plus";
import { Refresh, View } from "@element-plus/icons-vue";
import { formatEstablishedDate } from "@/composables/date";
import { normalizeTags } from "@/utils/tagUtils";

// API 接口
import {
  getServiceProviderList,
  getServiceProviderDetail,
} from "@/api/service-provider";
import { getRegions, getServiceTags } from "@/api/common";
import { getCertList } from "@/api/certification";
import { maskPhone } from "@/utils/desensitize";
import { useUserStore } from "@/stores/user";

// 获取用户角色
const userStore = useUserStore();
const showPhone = (phone) => {
  return maskPhone(phone, userStore.userInfo?.role);
};

// 统计卡片（注释保留）
// const statistics = ref([
//   { icon: Shop, label: '服务企业总数', value: 128, trend: 12, bgColor: '#ecf5ff', color: '#409eff' },
//   { icon: User, label: '活跃服务企业', value: 98, trend: 5, bgColor: '#f0f9eb', color: '#67c23a' },
//   { icon: Star, label: '平均评分', value: 4.6, trend: 2, bgColor: '#fdf6ec', color: '#e6a23c' },
//   { icon: TrendCharts, label: '服务需求', value: 56, trend: -3, bgColor: '#fef0f0', color: '#f56c6c' }
// ])

// 搜索表单
const searchExpanded = ref(true);
const searchForm = reactive({
  companyName: "",
  region: "",
  serviceType: "",
});

// 表格数据
const tableData = ref([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

// 详情弹窗
const detailDialog = reactive({ visible: false, data: {} });

// 获取列表
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...(searchForm.companyName && { companyName: searchForm.companyName }),
      ...(searchForm.region && { region: searchForm.region }),
      ...(searchForm.serviceType && { serviceType: searchForm.serviceType }),
    };
    const res = await getServiceProviderList(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    // 统一错误提示已在 request 响应拦截器中处理，这里仅记录日志和恢复状态
    console.error("获取服务企业列表失败", error);
  } finally {
    loading.value = false;
  }
};

// 证书列表相关
const activeCertCollapse = ref(""); // 默认收起
const certificateList = ref([]);
const certLoading = ref(false);

// 获取证书列表
const fetchCertList = async (serviceId) => {
  if (!serviceId) return;
  certLoading.value = true;
  try {
    const res = await getCertList({ serviceId });

    certificateList.value = Array.isArray(res.records) ? res.records : [];
  } catch (error) {
    console.error("获取证书列表失败", error);
    certificateList.value = [];
  } finally {
    certLoading.value = false;
  }
};

// 刷新证书列表
const refreshCertList = () => {
  if (detailDialog.data?.id) {
    fetchCertList(detailDialog.data.id);
  }
};

// 区域下拉静态兜底选项（当接口异常或返回格式错误时使用）
const DEFAULT_REGION_OPTIONS = ["全国", "华北地区", "华东地区", "华南地区"];

const regionOptions = ref([]);

// 获取区域列表
const fetchRegions = async () => {
  try {
    const res = await getRegions();

    if (Array.isArray(res)) {
      regionOptions.value = res;
    } else {
      console.warn("区域接口返回格式异常，使用默认值");
      // 接口返回非数组时使用静态兜底列表，避免下拉框无选项
      regionOptions.value = DEFAULT_REGION_OPTIONS;
    }
  } catch (error) {
    console.error("获取区域列表失败，使用默认选项", error);
    // 接口请求失败时同样使用静态兜底列表
    regionOptions.value = DEFAULT_REGION_OPTIONS;
  }
};

// 服务类型下拉静态兜底选项（当接口异常或返回格式错误时使用）
// 注意：结构需与模板中使用的 { id, name } 保持一致，避免渲染告警
const DEFAULT_SERVICE_TYPE_OPTIONS = [
  { id: "tech_consult", name: "技术咨询" },
  { id: "system_integration", name: "系统集成" },
  { id: "operation_maintenance", name: "运维服务" },
];

const serviceTypeOptions = ref([]);

// 获取服务类型标签
const fetchServiceTags = async () => {
  try {
    const res = await getServiceTags();

    if (Array.isArray(res)) {
      serviceTypeOptions.value = res;
    } else {
      console.warn("服务类型接口返回格式异常，使用默认值");
      // 接口返回非数组时使用静态兜底列表，保证筛选可用
      serviceTypeOptions.value = DEFAULT_SERVICE_TYPE_OPTIONS;
    }
  } catch (error) {
    console.error("获取服务类型标签失败，使用默认选项", error);
    // 接口请求失败时同样使用静态兜底列表
    serviceTypeOptions.value = DEFAULT_SERVICE_TYPE_OPTIONS;
  }
};

// 搜索与重置
const handleSearch = () => {
  pagination.current = 1;
  fetchList();
};

const resetSearch = () => {
  searchForm.companyName = "";
  searchForm.region = "";
  searchForm.serviceType = "";
  handleSearch();
};

// 分页
const handleSizeChange = (val) => {
  pagination.size = val;
  fetchList();
};
const handleCurrentChange = (val) => {
  pagination.current = val;
  fetchList();
};

// 查看详情
const handleDetail = async (row) => {
  try {
    const res = await getServiceProviderDetail(row.id);
    detailDialog.data = res;
    detailDialog.visible = true;
    // 获取证书列表
    fetchCertList(row.id);
  } catch (error) {
    console.error("获取详情失败", error);
  }
};
const showPreview = ref(false);

onMounted(() => {
  // 尝试从接口获取，失败时保持静态默认值
  fetchRegions();
  fetchServiceTags();
  fetchList();
});
</script>

<style scoped>
.service-provider-dashboard {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

/* 页面头部 */
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

/* 统计卡片 */
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

/* 搜索卡片 */
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

/* 表格卡片 */
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

/* 展开详情 */
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

/* 分页容器 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}

/* 固定标签宽度的描述列表 */
.fixed-label-descriptions :deep(.el-descriptions__label) {
  width: 100px; /* 与 label-width 保持一致 */
  min-width: 100px;
  max-width: 100px;
  height: 0%;
  text-align: center; /* 文字居中 */
  white-space: nowrap; /* 强制不换行 */
}

.fixed-label-descriptions :deep(.el-descriptions__content) {
  width: 100px;
  word-break: break-word; /* 内容区域允许换行 */
}

/* 证书卡片列表容器 */
.cert-card-list {
  padding: 16px 20px;
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 单个证书卡片 */
.cert-item-card {
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.cert-item-card:last-child {
  margin-bottom: 0;
}

/* 卡片内的描述列表样式（与上方企业信息统一） */
.cert-item-card .el-descriptions {
  --el-descriptions-item-label-width: 100px; /* 固定标签宽度 */
}

.cert-item-card .el-descriptions :deep(.el-descriptions__label) {
  background-color: #f5f7fa;
  text-align: right;
  font-weight: 600;
  color: #1f2f3d;
  padding: 12px 16px;
  width: 100px;
}

.cert-item-card .el-descriptions :deep(.el-descriptions__content) {
  padding: 12px 16px;
  word-break: break-word;
}
</style>
