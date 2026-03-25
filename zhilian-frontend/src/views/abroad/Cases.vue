<template>
  <div class="abroad-cases">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">出海成功案例</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>出海服务</el-breadcrumb-item>
          <el-breadcrumb-item>成功案例</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <el-card class="search-card" shadow="hover">
      <el-collapse-transition>
        <div v-show="searchExpanded">
          <el-form :model="searchForm" label-width="100px" class="search-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="目标国家">
                  <el-select
                    v-model="searchForm.country"
                    placeholder="全部"
                    clearable
                    filterable
                  >
                    <el-option
                      v-for="item in countryOptions"
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
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 案例卡片列表 -->
    <el-card class="cases-card" shadow="hover">
      <div class="cases-toolbar">
        <div class="cases-title">精选案例</div>
        <div class="cases-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <div v-loading="loading" class="cases-grid">
        <el-empty
          v-if="!loading && tableData.length === 0"
          description="暂无案例"
        />
        <div
          v-for="item in tableData"
          :key="item.id"
          class="case-card"
          @click="openDetail(item)"
        >
          <div class="case-cover">
            <el-image :src="item.coverImage || defaultCover" fit="cover" lazy>
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="32"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="case-tag">
              <el-tag size="small" type="info">{{ item.country }}</el-tag>
            </div>
          </div>
          <div class="case-info">
            <h3 class="case-title">{{ item.title }}</h3>
            <div class="case-meta">
              <span class="company">{{ item.companyName }}</span>
              <span class="service-type">
                <el-tag size="small" effect="plain">{{
                  item.serviceType
                }}</el-tag>
              </span>
            </div>
            <div class="case-desc">
              {{ truncateDescription(item.description) }}
            </div>
            <div class="case-footer">
              <span class="publish-time">{{
                createTimeConverter(item.publishTime).toLocalYMDHMS()
              }}</span>
              <el-button
                link
                type="primary"
                size="small"
                @click.stop="openDetail(item)"
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[6, 12, 18, 24]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 案例详情弹窗 -->
    <el-dialog
      v-model="detailDialog.visible"
      :title="detailDialog.data?.title || '案例详情'"
      width="600px"
      class="case-detail-dialog"
    >
      <div class="detail-cover">
        <el-image
          :src="detailDialog.data?.coverImage || defaultCover"
          fit="contain"
          style="width: 100%; max-height: 300px"
        >
          <template #error>
            <div class="image-placeholder-large">
              <el-icon :size="48"><Picture /></el-icon>
            </div>
          </template>
        </el-image>
      </div>
      <el-descriptions :column="2" border class="detail-descriptions">
        <el-descriptions-item label="企业名称">
          {{ detailDialog.data?.companyName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="企业类型">
          {{ formatCompanyType(detailDialog.data?.companyType) }}
        </el-descriptions-item>
        <el-descriptions-item label="目标国家">
          {{ detailDialog.data?.country || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="服务类型">
          {{ detailDialog.data?.serviceType || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">
          {{
            createTimeConverter(detailDialog.data?.publishTime).toLocalYMDHMS()
          }}
        </el-descriptions-item>
        <el-descriptions-item label="案例详情" :span="2">
          <div class="detail-description">
            {{ detailDialog.data?.description || "-" }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Refresh, Picture } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { getAbroadCaseList } from "@/api/abroad";
import { getCountries, getServiceTags } from "@/api/common";
import { createTimeConverter } from "@/composables/date";

// 搜索折叠状态
const searchExpanded = ref(true);

// 搜索表单
const searchForm = reactive({
  country: "",
  serviceType: "",
});

// 案例数据
const tableData = ref([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  current: 1,
  size: 12,
  total: 0,
});

// 下拉选项
const countryOptions = ref([]);
const serviceTypeOptions = ref([]);

// 默认封面图片
const defaultCover = "https://via.placeholder.com/300x200?text=No+Image";

// 详情弹窗
const detailDialog = reactive({
  visible: false,
  data: null,
});

// 截断描述文本
const truncateDescription = (desc, len = 80) => {
  if (!desc) return "";
  return desc.length > len ? desc.slice(0, len) + "..." : desc;
};

// 格式化企业类型
const formatCompanyType = (type) => {
  const map = {
    manufacture: "制造企业",
    service: "服务企业",
  };
  return map[type] || type || "-";
};

// 获取案例列表
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...(searchForm.country && { country: searchForm.country }),
      ...(searchForm.serviceType && { serviceType: searchForm.serviceType }),
    };
    const res = await getAbroadCaseList(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error("获取出海案例列表失败", error);
  } finally {
    loading.value = false;
  }
};

// 获取国家列表
const fetchCountries = async () => {
  try {
    const res = await getCountries();
    countryOptions.value = Array.isArray(res) ? res : [];
  } catch (error) {
    console.error("获取国家列表失败", error);
    countryOptions.value = [];
  }
};

// 获取服务类型标签
const fetchServiceTags = async () => {
  try {
    const res = await getServiceTags();
    serviceTypeOptions.value = Array.isArray(res) ? res : [];
  } catch (error) {
    console.error("获取服务类型失败", error);
    serviceTypeOptions.value = [];
  }
};

// 搜索与重置
const handleSearch = () => {
  pagination.current = 1;
  fetchList();
};

const resetSearch = () => {
  searchForm.country = "";
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

// 打开详情弹窗
const openDetail = (item) => {
  detailDialog.data = item;
  detailDialog.visible = true;
};

onMounted(() => {
  fetchCountries();
  fetchServiceTags();
  fetchList();
});
</script>

<style scoped>
.abroad-cases {
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

.cases-card {
  border-radius: 12px;
  overflow: hidden;
}

.cases-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.cases-title {
  font-weight: 600;
  color: #1f2f3d;
}

.cases-actions {
  display: flex;
  gap: 8px;
}

.cases-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 20px;
}

.case-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition:
    transform 0.3s,
    box-shadow 0.3s;
  cursor: pointer;
}

.case-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.case-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.case-cover .el-image {
  width: 100%;
  height: 100%;
}

.case-cover .image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #c0c4cc;
}

.case-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}

.case-info {
  padding: 16px;
}

.case-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2f3d;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.case-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
  color: #8590a6;
}

.company {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140px;
}

.case-desc {
  font-size: 13px;
  color: #5e6d82;
  line-height: 1.5;
  margin-bottom: 12px;
  min-height: 40px;
}

.case-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #a3a9b8;
}

.publish-time {
  font-size: 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}

/* 详情弹窗样式 */
.case-detail-dialog :deep(.el-dialog__body) {
  padding-top: 0;
}

.detail-cover {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f7fa;
  text-align: center;
}

.image-placeholder-large {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #c0c4cc;
}

.detail-descriptions {
  margin-bottom: 16px;
}

.detail-description {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
