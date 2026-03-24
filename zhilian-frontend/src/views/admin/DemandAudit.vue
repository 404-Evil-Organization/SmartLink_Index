<template>
  <div class="demand-audit">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">需求审核</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理员</el-breadcrumb-item>
          <el-breadcrumb-item>需求审核</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 搜索卡片（仅保留刷新按钮，符合接口无搜索参数） -->
    <div class="search-bar">
      <div class="search-toolbar">
        <div class="search-toolbar-left">
          <span class="info-tip">待审核需求列表</span>
        </div>
        <div class="search-toolbar-right">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="90" align="center" />
        <el-table-column
          prop="manuName"
          label="企业名称"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column
          prop="title"
          label="需求标题"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="expectedBudget"
          label="预算(万元)"
          width="110"
          align="right"
        >
          <template #default="{ row }">
            {{
              row.expectedBudget !== undefined && row.expectedBudget !== null
                ? row.expectedBudget
                : "-"
            }}
          </template>
        </el-table-column>
        <el-table-column
          prop="deadline"
          label="期望完成日期"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            {{ row.deadline || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ createTimeConverter(row.createTime).toLocalYMDHMS() }}
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="150">
          <template #default="{ row }">
            <div class="tag-list" v-if="row.tags && row.tags.length">
              <el-tag
                v-for="tag in row.tags.slice(0, 3)"
                :key="tag.id"
                size="small"
                type="info"
                style="margin-right: 4px; margin-bottom: 2px"
              >
                {{ tag.name }}
              </el-tag>
              <span
                v-if="row.tags.length > 3"
                style="font-size: 12px; color: #909399"
              >
                +{{ row.tags.length - 3 }}
              </span>
            </div>
            <span v-else class="empty-text">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <div style="display: flex; gap: 8px; justify-content: center">
              <el-button
                size="small"
                type="primary"
                plain
                :icon="View"
                @click="viewDetail(row)"
              >
                详情
              </el-button>
              <el-button
                size="small"
                type="warning"
                plain
                :icon="Edit"
                @click="openAuditDialog(row)"
              >
                审核
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 需求详情弹窗 -->
    <el-dialog
      v-model="detailDialog.visible"
      title="需求详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-loading="detailDialog.loading" class="demand-detail-container">
        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="需求标题">{{
            detailDialog.data.title
          }}</el-descriptions-item>
          <el-descriptions-item label="制造企业">{{
            detailDialog.data.manuName
          }}</el-descriptions-item>
          <el-descriptions-item label="预算(万元)">
            {{
              detailDialog.data.expectedBudget !== undefined &&
              detailDialog.data.expectedBudget !== null
                ? detailDialog.data.expectedBudget
                : "-"
            }}
          </el-descriptions-item>
          <el-descriptions-item label="期望完成日期">{{
            detailDialog.data.deadline || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{
              createTimeConverter(detailDialog.data.createTime).toLocalYMDHMS()
            }}
          </el-descriptions-item>
          <el-descriptions-item label="标签">
            <div
              class="tag-list"
              v-if="detailDialog.data.tags && detailDialog.data.tags.length"
            >
              <el-tag
                v-for="tag in detailDialog.data.tags"
                :key="tag.id"
                size="small"
                type="info"
                style="margin-right: 4px"
              >
                {{ tag.name }}
              </el-tag>
            </div>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="需求描述">
            <div class="description-text">
              {{ detailDialog.data.description || "暂无描述" }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核操作弹窗 -->
    <el-dialog
      v-model="auditDialog.visible"
      title="需求审核"
      width="500px"
      :close-on-click-modal="false"
      @closed="resetAuditForm"
    >
      <el-form :model="auditForm" label-width="100px" ref="auditFormRef">
        <el-form-item label="需求标题">
          <span>{{ auditDialog.demandTitle }}</span>
        </el-form-item>
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="auditForm.status">
            <el-radio label="approved">通过</el-radio>
            <el-radio label="rejected">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="驳回理由"
          v-if="auditForm.status === 'rejected'"
          prop="remark"
          :rules="[
            { required: true, message: '请填写驳回理由', trigger: 'blur' },
          ]"
        >
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请填写驳回理由，便于企业修改后重新提交"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="auditDialog.submitting"
          @click="submitAudit"
          >确 定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { View, Edit, Refresh } from "@element-plus/icons-vue";
import { getPendingDemands, approveDemand } from "@/api/admin";
import { createTimeConverter } from "@/composables/date";

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
const detailDialog = reactive({
  visible: false,
  loading: false,
  data: {},
});

// 审核弹窗
const auditDialog = reactive({
  visible: false,
  demandId: null,
  demandTitle: "",
  submitting: false,
});
const auditForm = reactive({
  status: "approved", // approved / rejected
  remark: "",
});
const auditFormRef = ref(null);

// 获取待审核列表
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
    };
    const res = await getPendingDemands(params);
    tableData.value = res?.records || [];
    pagination.total = res?.total || 0;
  } catch (error) {
    console.error("获取待审核需求列表失败:", error);
    tableData.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 分页
const handlePageChange = (val) => {
  pagination.current = val;
  fetchList();
};
const handlePageSizeChange = (val) => {
  pagination.size = val;
  pagination.current = 1;
  fetchList();
};

// 查看详情
const viewDetail = (row) => {
  // 先打开弹窗，再控制加载态，便于未来扩展为异步获取详情数据
  detailDialog.visible = true;
  detailDialog.loading = true;
  detailDialog.data = { ...row }; // 复制行数据
  detailDialog.loading = false;
};

// 打开审核弹窗
const openAuditDialog = (row) => {
  auditDialog.demandId = row.id;
  auditDialog.demandTitle = row.title;
  auditForm.status = "approved";
  auditForm.remark = "";
  auditDialog.visible = true;
};

// 重置审核表单（弹窗关闭后）
const resetAuditForm = () => {
  auditForm.status = "approved";
  auditForm.remark = "";
  if (auditFormRef.value) {
    auditFormRef.value.clearValidate();
  }
};

// 提交审核
const submitAudit = async () => {
  // 统一走表单校验，确保 el-form rules 在点击“确定”时正确触发
  if (!auditFormRef.value) {
    return;
  }
  try {
    // validate 校验不通过会抛出异常，这里直接中断提交流程
    await auditFormRef.value.validate();
  } catch (e) {
    return;
  }

  auditDialog.submitting = true;
  try {
    await approveDemand(auditDialog.demandId, {
      status: auditForm.status,
      remark: auditForm.status === "rejected" ? auditForm.remark.trim() : "",
    });
    ElMessage.success(
      auditForm.status === "approved" ? "审核通过成功" : "已驳回需求",
    );
    auditDialog.visible = false;
    // 刷新列表
    fetchList();
  } catch (error) {
    console.error("审核操作失败:", error);
    // 错误提示由拦截器统一处理，这里不再重复提示
  } finally {
    auditDialog.submitting = false;
  }
};

// 初始化加载
onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.demand-audit {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-header {
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

/* 搜索卡片样式（与用户管理保持一致，无表单仅工具条） */
.search-bar {
  margin-bottom: 16px;
  background-color: #fff;
  border-radius: 12px;
  padding: 12px 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.search-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-toolbar-left {
  font-size: 14px;
  color: #606266;
}

.info-tip {
  font-weight: 500;
  color: #1f2f3d;
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.empty-text {
  color: #909399;
  font-size: 12px;
}

/* 详情弹窗样式 */
.demand-detail-container {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.detail-descriptions {
  margin-bottom: 0;
}

.description-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  max-height: 300px;
  overflow-y: auto;
}
</style>
