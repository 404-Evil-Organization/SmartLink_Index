<template>
  <div class="abroad-service-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">出海服务</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>出海服务</el-breadcrumb-item>
          <el-breadcrumb-item>服务商列表</el-breadcrumb-item>
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
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">出海服务商列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column
          prop="companyName"
          label="服务商名称"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column prop="region" label="区域" width="100" />
        <el-table-column prop="serviceType" label="服务类型" min-width="150">
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
        <el-table-column label="覆盖国家/地区" min-width="180">
          <template #default="{ row }">
            <el-tag
              v-for="(country, index) in parseCountryCoverage(
                row.countryCoverage,
              )"
              :key="country + '-' + index"
              size="small"
              type="success"
              effect="plain"
              style="margin-right: 5px; margin-bottom: 3px"
            >
              {{ country }}
            </el-tag>
            <span v-if="!row.countryCoverage">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130">
          <template #default="{ row }">
            {{ maskPhone(row.contactPhone, userStore.userInfo?.role) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="handleDetail(row.id)">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { Refresh, View } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { maskPhone } from "@/utils/desensitize";
import { normalizeTags } from "@/utils/tagUtils";
import { getAbroadServiceList } from "@/api/abroad";
import { getRegions, getServiceTags } from "@/api/common";

const router = useRouter();
const userStore = useUserStore();

// 搜索折叠状态
const searchExpanded = ref(true);

// 搜索表单
const searchForm = reactive({
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

// 下拉选项
const regionOptions = ref([]);
const serviceTypeOptions = ref([]);

// 解析国家覆盖字段（支持逗号分隔字符串或数组）
const parseCountryCoverage = (coverage) => {
  if (!coverage) return [];
  if (Array.isArray(coverage)) return coverage;
  if (typeof coverage === "string") {
    return coverage
      .split(",")
      .map((c) => c.trim())
      .filter(Boolean);
  }
  return [];
};

// 获取出海服务商列表
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...(searchForm.region && { region: searchForm.region }),
      ...(searchForm.serviceType && { serviceType: searchForm.serviceType }),
    };
    const res = await getAbroadServiceList(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error("获取出海服务商列表失败", error);
  } finally {
    loading.value = false;
  }
};

// 获取区域列表
const fetchRegions = async () => {
  try {
    const res = await getRegions();
    regionOptions.value = Array.isArray(res) ? res : [];
  } catch (error) {
    console.error("获取区域列表失败", error);
    regionOptions.value = [];
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

// 查看详情 - 跳转到服务商详情页
const handleDetail = (id) => {
  if (!id) {
    ElMessage.error("服务商信息无效，请刷新重试");
    return;
  }
  router.push({
    path: `/credit/service/detail/${id}`,
    query: { from: "abroad" },
  });
};

onMounted(() => {
  fetchRegions();
  fetchServiceTags();
  fetchList();
});
</script>

<style scoped>
.abroad-service-list {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}
</style>
