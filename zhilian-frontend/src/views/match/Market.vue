<template>
  <div class="market-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">合作市场</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>合作市场</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-tooltip content="刷新">
          <el-button :icon="Refresh" circle @click="refreshList" />
        </el-tooltip>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <el-card class="filter-card" shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="需求标题关键词"
          clearable
          style="width: 220px; margin-right: 12px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="searchForm.tagIds"
          placeholder="选择标签"
          clearable
          filterable
          multiple
          collapse-tags
          collapse-tags-tooltip
          style="width: 260px; margin-right: 12px"
        >
          <el-option
            v-for="tag in tagOptions"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>

        <div class="budget-range" style="margin-right: 12px">
          <el-input-number
            v-model="searchForm.expectedBudgetMin"
            :controls="false"
            placeholder="最低预算"
            :precision="2"
            style="width: 110px"
            @change="handleBudgetMinChange"
          />
          <span class="separator">-</span>
          <el-input-number
            v-model="searchForm.expectedBudgetMax"
            :controls="false"
            placeholder="最高预算"
            :precision="2"
            style="width: 110px"
            @change="handleBudgetMaxChange"
          />
          <span class="unit">万元</span>
          <div v-if="budgetError" class="budget-error">{{ budgetError }}</div>
        </div>

        <el-date-picker
          v-model="deadlineRange"
          type="daterange"
          range-separator="至"
          start-placeholder="截止日期起"
          end-placeholder="截止日期止"
          value-format="YYYY-MM-DD"
          style="width: 260px; margin-right: 12px"
          @change="handleDeadlineChange"
        />

        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 需求卡片列表 -->
    <div v-loading="loading" class="demand-list">
      <el-row :gutter="20">
        <el-col
          v-for="demand in demandList"
          :key="demand.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="8"
          :xl="6"
          style="margin-bottom: 20px"
        >
          <el-card
            class="demand-card"
            shadow="hover"
            :body-style="{ padding: '0px' }"
          >
            <div class="card-content">
              <!-- 需求标题和预算 -->
              <div class="card-header">
                <div class="title-wrap">
                  <span class="demand-title">{{ demand.title }}</span>
                  <el-tag
                    v-if="demand.expectedBudget != null"
                    type="warning"
                    size="small"
                    class="budget-tag"
                  >
                    ¥{{ demand.expectedBudget }}万
                  </el-tag>
                </div>
              </div>

              <!-- 需求描述（截断） -->
              <div class="demand-desc">
                {{ demand.description }}
              </div>

              <!-- 标签区域 -->
              <div class="tags-wrap" v-if="demand.tags && demand.tags.length">
                <el-tag
                  v-for="tag in demand.tags.slice(0, 3)"
                  :key="tag.id"
                  size="small"
                  effect="plain"
                  class="demand-tag"
                >
                  {{ tag.name }}
                </el-tag>
                <el-tag
                  v-if="demand.tags.length > 3"
                  size="small"
                  type="info"
                  effect="plain"
                >
                  +{{ demand.tags.length - 3 }}
                </el-tag>
              </div>

              <!-- 截止日期 -->
              <div class="deadline-info" v-if="demand.deadline">
                <el-icon><Calendar /></el-icon>
                <span>截止：{{ demand.deadline }}</span>
              </div>

              <!-- 发布企业信息 -->
              <div class="publisher-info">
                <div class="company-name">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>{{
                    demand.manufacture?.companyName || "未知企业"
                  }}</span>
                </div>
                <div
                  class="contact-info"
                  v-if="demand.manufacture?.contactPerson"
                >
                  <span>联系人：{{ demand.manufacture.contactPerson }}</span>
                  <span
                    v-if="demand.manufacture?.contactPhone"
                    class="contact-phone"
                  >
                    电话：{{ showPhone(demand.manufacture.contactPhone) }}
                  </span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="card-footer">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="!canAcceptDemand"
                  @click="handleAccept(demand)"
                  :loading="acceptingId === demand.id"
                >
                  <el-icon><Connection /></el-icon> 接单
                </el-button>
                <span class="publish-time">
                  {{ createTimeConverter(demand.createTime).toLocalYMDHMS() }}
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && demandList.length === 0"
        description="暂无符合条件的需求"
        :image-size="120"
      />
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[6, 12, 18, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="
          (size) => {
            pagination.size = size;
            pagination.current = 1;
            fetchDemandList();
          }
        "
        @current-change="fetchDemandList"
      />
    </div>

    <!-- 接单时选择服务商企业弹窗（当用户有多个服务商企业时） -->
    <el-dialog
      v-model="selectServiceDialog.visible"
      title="选择接单企业"
      width="400px"
    >
      <el-form>
        <el-form-item label="请选择服务商企业">
          <el-select
            v-model="selectServiceDialog.selectedServiceId"
            placeholder="请选择"
            style="width: 100%"
          >
            <el-option
              v-for="sp in availableServiceProviders"
              :key="sp.id"
              :label="sp.companyName"
              :value="sp.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="selectServiceDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmAccept"
          :loading="acceptingId === selectServiceDialog.demandId"
        >
          确认接单
        </el-button>
      </template>
    </el-dialog>

    <!-- 接单确认弹窗 -->
    <el-dialog
      v-model="acceptConfirmDialog.visible"
      title="确认接单"
      width="400px"
    >
      <div class="confirm-content">
        <el-alert
          title="确认接单"
          type="warning"
          description="接单后将无法撤销，请确认是否接取该需求？"
          show-icon
          :closable="false"
        />
        <div class="demand-info" style="margin-top: 16px">
          <p>
            <strong>需求标题：</strong>{{ acceptConfirmDialog.demandTitle }}
          </p>
        </div>
      </div>
      <template #footer>
        <el-button @click="acceptConfirmDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmAcceptDemand"
          :loading="acceptingId === acceptConfirmDialog.demandId"
        >
          确认接单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
  Refresh,
  Search,
  Calendar,
  OfficeBuilding,
  Connection,
} from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";
import { maskPhone } from "@/utils/desensitize";
import { createTimeConverter } from "@/composables/date";
// import { formatDate, truncateText } from "@/utils/format";

// API 导入
import { getDemandMarketList, acceptDemand } from "@/api/match";
import { getTagList } from "@/api/tag";
import { getMyServiceList } from "@/api/enterprise";

const userStore = useUserStore();
const userRole = computed(() => userStore.userInfo?.role);
const isService = computed(() => userRole.value === "service");

// 脱敏显示电话（服务商可看完整，其他角色脱敏）
const showPhone = (phone) => {
  if (!phone) return "-";
  return maskPhone(phone, userRole.value);
};

// 搜索表单
const searchForm = reactive({
  keyword: "",
  tagIds: [],
  expectedBudgetMin: null,
  expectedBudgetMax: null,
  deadlineStart: "",
  deadlineEnd: "",
});
const deadlineRange = ref(null);

// 分页
const pagination = reactive({
  current: 1,
  size: 12,
});
const total = ref(0);
const demandList = ref([]);
const loading = ref(false);

// 标签选项
const tagOptions = ref([]);

// 接单相关
const acceptingId = ref(null);
// 当前用户审核通过的服务商企业列表
const availableServiceProviders = ref([]);
// 选择服务商弹窗
const selectServiceDialog = reactive({
  visible: false,
  demandId: null,
  demandTitle: "",
  selectedServiceId: null,
});
// 接单确认弹窗
const acceptConfirmDialog = reactive({
  visible: false,
  demandId: null,
  demandTitle: "",
  serviceId: null,
});

// 预算错误提示
const budgetError = ref("");

// 校验预算范围（min ≤ max）
const validateBudget = () => {
  const min = searchForm.expectedBudgetMin;
  const max = searchForm.expectedBudgetMax;
  if (min !== null && max !== null && min > max) {
    budgetError.value = "最低预算不能大于最高预算";
    return false;
  }
  budgetError.value = "";
  return true;
};

// 处理最低预算变化
const handleBudgetMinChange = () => {
  if (
    searchForm.expectedBudgetMin !== null &&
    searchForm.expectedBudgetMax !== null
  ) {
    if (searchForm.expectedBudgetMin > searchForm.expectedBudgetMax) {
      searchForm.expectedBudgetMax = searchForm.expectedBudgetMin;
    }
  }
  validateBudget();
};

// 处理最高预算变化
const handleBudgetMaxChange = () => {
  if (
    searchForm.expectedBudgetMin !== null &&
    searchForm.expectedBudgetMax !== null
  ) {
    if (searchForm.expectedBudgetMin > searchForm.expectedBudgetMax) {
      searchForm.expectedBudgetMin = searchForm.expectedBudgetMax;
    }
  }
  validateBudget();
};

// 是否可以接单：服务商角色 且 有至少一个审核通过的服务商企业
const canAcceptDemand = computed(() => {
  return isService.value && availableServiceProviders.value.length > 0;
});

// 获取需求列表
const fetchDemandList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      tagIds: searchForm.tagIds.length
        ? searchForm.tagIds.join(",")
        : undefined,
      expectedBudgetMin: searchForm.expectedBudgetMin ?? undefined,
      expectedBudgetMax: searchForm.expectedBudgetMax ?? undefined,
      deadlineStart: searchForm.deadlineStart || undefined,
      deadlineEnd: searchForm.deadlineEnd || undefined,
    };
    const res = await getDemandMarketList(params);
    console.log(res);

    demandList.value = res.records || [];
    total.value = res.total || 0;
  } catch (error) {
    console.error("获取需求列表失败", error);
    ElMessage.error("获取需求列表失败");
  } finally {
    loading.value = false;
  }
};

// 处理截止日期范围变化
const handleDeadlineChange = (range) => {
  if (range && range.length === 2) {
    searchForm.deadlineStart = range[0];
    searchForm.deadlineEnd = range[1];
  } else {
    searchForm.deadlineStart = "";
    searchForm.deadlineEnd = "";
  }
  pagination.current = 1;
  fetchDemandList();
};

// 搜索
const handleSearch = () => {
  if (!validateBudget()) {
    return; // 预算无效，阻止搜索
  }
  pagination.current = 1;
  fetchDemandList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = "";
  searchForm.tagIds = [];
  searchForm.expectedBudgetMin = null;
  searchForm.expectedBudgetMax = null;
  searchForm.deadlineStart = "";
  searchForm.deadlineEnd = "";
  budgetError.value = "";
  deadlineRange.value = null;
  pagination.current = 1;
  fetchDemandList();
};

// 刷新列表
const refreshList = () => {
  fetchDemandList();
};

// 获取标签列表（用于筛选）
const fetchTags = async () => {
  try {
    let allTags = [];
    let page = 1;
    const size = 100;
    let hasMore = true;

    while (hasMore) {
      const res = await getTagList({ page, size });
      const currentTags = res.records || [];
      allTags = allTags.concat(currentTags);
      
      const tagTotal = res.total || 0;
      if (allTags.length >= tagTotal || currentTags.length < size) {
        hasMore = false;
      } else {
        page++;
      }
    }
    tagOptions.value = allTags;
  } catch (error) {
    console.error("获取标签列表失败", error);
  }
};

// 获取当前用户审核通过的服务商企业列表
const fetchMyServiceProviders = async () => {
  if (!isService.value) return;
  try {
    const res = await getMyServiceList({
      page: 1,
      size: 100,
      status: "approved",
    });
    // 过滤出审核通过的服务商
    const approvedList = (res.records || []).filter(
      (item) => item.auditStatus === "approved",
    );
    availableServiceProviders.value = approvedList;
  } catch (error) {
    console.error("获取服务商企业列表失败", error);
  }
};

// 接单操作
const handleAccept = async (demand) => {
  if (!canAcceptDemand.value) {
    if (isService.value && availableServiceProviders.value.length === 0) {
      ElMessage.warning("您还没有审核通过的服务商企业，无法接单");
    } else if (!isService.value) {
      ElMessage.warning("当前角色无接单权限，仅支持浏览");
    }
    return;
  }

  // 如果只有一个服务商企业，弹出确认弹窗
  if (availableServiceProviders.value.length === 1) {
    const serviceId = availableServiceProviders.value[0].id;
    openAcceptConfirmDialog(demand.id, demand.title, serviceId);
  } else {
    // 多个服务商企业，弹出选择框
    selectServiceDialog.demandId = demand.id;
    selectServiceDialog.demandTitle = demand.title;
    selectServiceDialog.selectedServiceId =
      availableServiceProviders.value[0]?.id || null;
    selectServiceDialog.visible = true;
  }
};

// 确认接单（从弹窗调用）
const confirmAccept = async () => {
  if (!selectServiceDialog.selectedServiceId) {
    ElMessage.warning("请选择服务商企业");
    return;
  }
  // 弹出确认弹窗
  openAcceptConfirmDialog(
    selectServiceDialog.demandId,
    selectServiceDialog.demandTitle,
    selectServiceDialog.selectedServiceId,
  );
  selectServiceDialog.visible = false;
};

// 打开接单确认弹窗
const openAcceptConfirmDialog = (demandId, demandTitle, serviceId) => {
  acceptConfirmDialog.demandId = demandId;
  acceptConfirmDialog.demandTitle = demandTitle;
  acceptConfirmDialog.serviceId = serviceId;
  acceptConfirmDialog.visible = true;
};

// 确认接单
const confirmAcceptDemand = async () => {
  if (!acceptConfirmDialog.demandId || !acceptConfirmDialog.serviceId) return;

  try {
    if (
      await doAccept(
        acceptConfirmDialog.demandId,
        acceptConfirmDialog.serviceId,
        acceptConfirmDialog.demandTitle,
      )
    ) {
      acceptConfirmDialog.visible = false;
    }
  } catch (error) {
    ElMessage.error("接单失败");
    console.error("接单失败", error);
  }
};

// 执行接单请求
const doAccept = async (demandId, serviceId, demandTitle) => {
  acceptingId.value = demandId;
  try {
    await acceptDemand({
      demandId,
      serviceId,
    });
    ElMessage.success(`已成功接取需求"${demandTitle}"`);
    // 刷新列表，移除已匹配的需求
    await fetchDemandList();
    return true;
  } catch (error) {
    console.error("接单失败", error);
    return false;
    // 错误消息由拦截器处理，这里不重复提示
  } finally {
    acceptingId.value = null;
  }
};

onMounted(() => {
  fetchTags();
  fetchMyServiceProviders();
  fetchDemandList();
});
</script>

<style scoped>
.budget-error {
  color: #f56c6c;
  font-size: 12px;
  margin-left: 8px;
  white-space: nowrap;
}

.market-page {
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

.filter-card {
  border-radius: 12px;
  margin-bottom: 24px;
  overflow: visible;
}

.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.budget-range {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #f5f7fa;
  padding: 4px 12px;
  border-radius: 8px;
}

.budget-range .separator {
  color: #909399;
}

.budget-range .unit {
  color: #606266;
  font-size: 13px;
  margin-left: 4px;
}

.demand-list {
  margin-top: 8px;
}

.demand-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  height: auto;
  display: flex;
  flex-direction: column;
}

.demand-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.card-header {
  margin-bottom: 20px;
}

.title-wrap {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.demand-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  flex: 1;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.budget-tag {
  flex-shrink: 0;
  margin-top: 2px;
}

.demand-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.demand-tag {
  background-color: #f4f4f5;
  border: none;
  color: #606266;
}

.deadline-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #e6a23c;
  margin-bottom: 12px;
  background-color: #fdf6ec;
  padding: 4px 8px;
  border-radius: 6px;
  width: fit-content;
}

.deadline-info .el-icon {
  font-size: 14px;
}

.publisher-info {
  background-color: #f8f9fa;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.company-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
}

.company-name .el-icon {
  font-size: 14px;
  color: #409eff;
}

.contact-info {
  font-size: 12px;
  color: #909399;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.contact-phone {
  color: #606266;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.publish-time {
  font-size: 11px;
  color: #c0c4cc;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .market-page {
    padding: 16px;
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-bar > *,
  .budget-range {
    width: 100%;
    margin-right: 0 !important;
  }

  .budget-range {
    justify-content: space-between;
  }

  .budget-range .el-input-number {
    flex: 1;
  }
}
</style>
