<template>
  <div class="country-guide-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">国家准入指南</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/abroad' }"
            >出海服务</el-breadcrumb-item
          >
          <el-breadcrumb-item>国家准入指南</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="国家名称">
              <el-input
                v-model="searchForm.keyword"
                placeholder="请输入国家名称（如：美国、欧盟）"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="16" class="search-actions">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">
          <el-icon><Guide /></el-icon>
          各国市场准入要求
        </div>
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
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="country" label="国家/地区" width="150" />
        <el-table-column
          prop="requirements"
          label="准入要求摘要"
          min-width="300"
        >
          <template #default="{ row }">
            <div class="requirement-summary">
              {{ truncateText(row.requirements, 100) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetailDialog(row)">
              <el-icon><View /></el-icon> 查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 国家指南详情弹窗 -->
    <el-dialog
      v-model="detailDialog.visible"
      :title="`${detailDialog.data.country || ''} 市场准入指南`"
      width="700px"
      class="guide-dialog"
    >
      <el-scrollbar max-height="60vh">
        <div class="guide-detail">
          <div class="detail-section">
            <h3>准入要求</h3>
            <p class="requirements-text">
              {{ detailDialog.data.requirements || "暂无信息" }}
            </p>
          </div>
          <div class="detail-section">
            <h3>办理流程</h3>
            <div class="process-text" v-if="detailDialog.data.process">
              <div
                v-for="(step, idx) in detailDialog.data.process.split('\n')"
                :key="idx"
              >
                {{ step }}
              </div>
            </div>
            <div v-else>{{ detailDialog.data.process || "暂无信息" }}</div>
          </div>
          <div class="detail-section">
            <h3>所需材料</h3>
            <el-table
              v-if="
                detailDialog.data.documents &&
                detailDialog.data.documents.length
              "
              :data="
                detailDialog.data.documents.map((doc, idx) => ({
                  idx: idx + 1,
                  name: doc,
                }))
              "
              border
              size="small"
              style="width: 100%"
            >
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="name" label="材料名称" />
            </el-table>
            <div v-else class="empty-tip">暂无信息</div>
          </div>
        </div>
      </el-scrollbar>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Refresh, View, Guide } from "@element-plus/icons-vue";
import { getCountryGuideList, getCountryGuideDetail } from "@/api/abroad";

// 搜索表单
const searchForm = reactive({
  keyword: "",
});

// 表格数据相关
const loading = ref(false);
const tableData = ref([]);
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

// 获取列表数据
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
    };
    const res = await getCountryGuideList(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    ElMessage.error("获取国家指南列表失败");
    console.error("获取国家指南列表失败", error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  fetchList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = "";
  handleSearch();
};

// 分页变化
const handlePageChange = () => {
  fetchList();
};

// 详情弹窗
const detailDialog = reactive({
  visible: false,
  data: {},
});

// 打开详情弹窗
const openDetailDialog = async (row) => {
  try {
    // 调用详情接口获取完整数据
    const detail = await getCountryGuideDetail(row.country);
    detailDialog.data = detail;
    detailDialog.visible = true;
  } catch (error) {
    ElMessage.error("获取国家指南详情失败");
    console.error("获取国家指南详情失败", error);
  }
};

// 文本截断辅助函数
const truncateText = (text, maxLen) => {
  if (!text) return "-";
  if (text.length <= maxLen) return text;
  return text.substring(0, maxLen) + "...";
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.country-guide-dashboard {
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
  padding: 16px 20px 4px;
}

.search-actions {
  display: flex;
  justify-content: flex-start;
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.requirement-summary {
  line-height: 1.5;
  color: #5e6d82;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}

.guide-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
  padding-bottom: 20px;
}

.guide-detail {
  padding: 0 8px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2f3d;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.requirements-text,
.process-text {
  line-height: 1.7;
  color: #5e6d82;
  white-space: pre-wrap;
  background-color: #fafbfc;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 0;
}

.empty-tip {
  color: #c0c4cc;
  padding: 20px 0;
  text-align: center;
}
</style>
