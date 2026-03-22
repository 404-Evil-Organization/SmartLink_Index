<template>
  <div class="dashboard-home">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的合作记录</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>我的合作</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-tooltip content="刷新">
          <el-button :icon="Refresh" circle @click="fetchList" />
        </el-tooltip>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" label-width="100px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="合作企业">
              <el-select
                v-model="searchForm.enterpriseId"
                placeholder="请选择企业"
                clearable
                filterable
                @change="handleEnterpriseChange"
              >
                <el-option
                  v-for="item in enterpriseOptions"
                  :key="item.id"
                  :label="`${item.companyName} (${item.type === 'manufacture' ? '制造企业' : '服务商'})`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合作状态">
              <el-select
                v-model="searchForm.status"
                placeholder="请选择状态"
                clearable
              >
                <el-option label="进行中" value="ongoing" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" class="search-actions">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">合作记录列表</div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        row-key="id"
      >
        <el-table-column type="index" label="序号" width="70" align="center" />
        <!-- 修改：使用新接口的 opponentName 字段，不再根据角色判断 -->
        <el-table-column label="合作对方" min-width="150">
          <template #default="{ row }">
            {{ row.opponentName }}
          </template>
        </el-table-column>
        <el-table-column prop="demandTitle" label="需求标题" min-width="180" />
        <el-table-column prop="amount" label="合作金额(万元)" width="130">
          <template #default="{ row }">
            {{ row.amount != null ? row.amount + " 万" : "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 新增：查看详情按钮 -->
            <el-button size="small" @click="openDetailDialog(row.id)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 'completed' && !row.hasEvaluated"
              type="primary"
              size="small"
              @click="goToEvaluation(row.id)"
            >
              去评价
            </el-button>
            <el-button
              v-else-if="row.status === 'completed' && row.hasEvaluated"
              disabled
              size="small"
            >
              已评价
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
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 合作详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" title="合作详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="制造企业">{{
          detailDialog.data.manuName || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="服务商">{{
          detailDialog.data.serviceName || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="需求标题">{{
          detailDialog.data.demandTitle || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="需求描述">{{
          detailDialog.data.demandDescription || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="合作内容">{{
          detailDialog.data.description || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="合作金额">{{
          detailDialog.data.amount != null
            ? detailDialog.data.amount + " 万元"
            : "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{
          detailDialog.data.startDate || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{
          detailDialog.data.endDate || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailDialog.data.status)">
            {{ getStatusText(detailDialog.data.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          detailDialog.data.createTime || "-"
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
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import { getMyCooperationList, getCooperationDetail } from "@/api/cooperation";
import { getMyManufactureList, getMyServiceList } from "@/api/enterprise";

const router = useRouter();

// 搜索表单
const searchForm = reactive({
  enterpriseId: null,
  status: "",
});

// 企业列表（用于下拉选择）
const enterpriseOptions = ref([]);

// 表格数据
const loading = ref(false);
const tableData = ref([]);
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

// 详情弹窗
const detailDialog = reactive({
  visible: false,
  data: {},
});

// 获取个人企业列表（制造企业 + 服务商）
const fetchMyEnterprises = async () => {
  try {
    const [manufactureRes, serviceRes] = await Promise.all([
      getMyManufactureList({ page: 1, size: 100 }),
      getMyServiceList({ page: 1, size: 100 }),
    ]);
    const manufactureList = (manufactureRes.records || []).map((item) => ({
      ...item,
      type: "manufacture",
    }));
    const serviceList = (serviceRes.records || []).map((item) => ({
      ...item,
      type: "service",
    }));
    enterpriseOptions.value = [...manufactureList, ...serviceList];
    // 默认选中第一个企业
    if (enterpriseOptions.value.length > 0 && !searchForm.enterpriseId) {
      searchForm.enterpriseId = enterpriseOptions.value[0].id;
      fetchList(); // 自动加载列表
    }
  } catch (error) {
    console.error("获取个人企业列表失败", error);
    ElMessage.error("获取企业列表失败");
  }
};

// 获取合作列表
const fetchList = async () => {
  if (!searchForm.enterpriseId) {
    ElMessage.warning("请先选择企业");
    return;
  }
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      enterpriseId: searchForm.enterpriseId,
      status: searchForm.status || undefined,
    };
    const res = await getMyCooperationList(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error("获取合作列表失败", error);
    ElMessage.error("获取合作列表失败");
  } finally {
    loading.value = false;
  }
};

// 查询
const handleSearch = () => {
  pagination.current = 1;
  fetchList();
};

// 重置
const resetSearch = () => {
  searchForm.status = "";
  pagination.current = 1;
  fetchList();
};

// 企业切换
const handleEnterpriseChange = () => {
  pagination.current = 1;
  fetchList();
};

// 分页变化（页码切换时触发）
const handlePageChange = (page) => {
  // 使用分页组件回调的页码更新当前页码，避免每次都重置为 1
  pagination.current = page && page > 0 ? page : 1;
  fetchList();
};

// 打开详情弹窗
const openDetailDialog = async (coopId) => {
  try {
    const res = await getCooperationDetail(coopId);
    detailDialog.data = res;
    detailDialog.visible = true;
  } catch (error) {
    console.error("获取合作详情失败", error);
    ElMessage.error("获取合作详情失败");
  }
};

// 跳转评价页面
const goToEvaluation = (coopId) => {
  router.push({
    path: "/evaluation/add",
    query: { coopId: coopId },
  });
};

// 状态标签样式
const getStatusType = (status) => {
  const map = {
    ongoing: "primary",
    completed: "success",
    cancelled: "danger",
  };
  return map[status] || "info";
};

const getStatusText = (status) => {
  const map = {
    ongoing: "进行中",
    completed: "已完成",
    cancelled: "已取消",
  };
  return map[status] || status;
};

onMounted(() => {
  fetchMyEnterprises();
});
</script>

<style scoped>
.dashboard-home {
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
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}
</style>
