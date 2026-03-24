<template>
  <div class="my-demands">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的需求</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>智能供需匹配</el-breadcrumb-item>
          <el-breadcrumb-item>我的需求</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <!-- 企业选择下拉框 -->
        <el-select
          v-model="selectedManuId"
          placeholder="请选择企业"
          style="width: 200px; margin-right: 12px"
          @change="handleEnterpriseChange"
        >
          <el-option
            v-for="item in enterpriseOptions"
            :key="item.id"
            :label="item.companyName"
            :value="item.id"
          />
        </el-select>
        <el-button type="primary" @click="handleAdd">发布新需求</el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="filter-form-left">
          <el-form-item label="状态">
            <el-select
              v-model="queryParams.status"
              placeholder="全部"
              clearable
              @change="handleSearch"
              style="width: 120px"
            >
              <el-option label="草稿" value="draft" />
              <el-option label="待接单" value="published" />
            </el-select>
          </el-form-item>
          <el-form-item label="标题">
            <el-input
              v-model="queryParams.keyword"
              placeholder="请输入标题关键词"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
        </el-form>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 需求列表 -->
    <el-card class="list-card" shadow="hover">
      <el-table v-loading="loading" :data="demandList" stripe>
        <el-table-column
          prop="title"
          label="需求标题"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="expectedBudget" label="预算(万元)" width="120">
          <template #default="{ row }">
            {{
              row.expectedBudget === null || row.expectedBudget === undefined
                ? "未填写"
                : row.expectedBudget + " 万元"
            }}
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="期望完成日期" width="120">
          <template #default="{ row }">
            {{ row.deadline || "未填写" }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="150">
          <template #default="{ row }">
            <el-tag
              v-for="tag in row.tags"
              :key="tag.id"
              size="small"
              class="tag-item"
            >
              {{ tag.name }}
            </el-tag>
            <span v-if="!row.tags?.length">无标签</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="row.status !== 'draft' && row.status !== 'published'"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              :disabled="row.status !== 'draft' && row.status !== 'published'"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-button
              v-if="row.status === 'matched'"
              link
              type="info"
              @click="viewCooperation(row)"
            >
              查看合作
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="demand-form"
      >
        <el-form-item label="需求标题" prop="title" required>
          <el-input
            v-model="form.title"
            placeholder="请输入需求标题"
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的需求"
            maxlength="500"
            show-word-limit
            resize="none"
          />
        </el-form-item>

        <el-form-item label="预算（万元）" prop="expectedBudget">
          <el-input-number
            v-model="form.expectedBudget"
            :min="0"
            :precision="2"
            :step="1"
            placeholder="请输入预算金额"
            style="width: 100%"
            controls-position="right"
          />
          <div class="field-tip">非必填，可留空</div>
        </el-form-item>

        <el-form-item label="期望完成日期" prop="deadline">
          <el-date-picker
            v-model="form.deadline"
            type="date"
            placeholder="请选择期望完成日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item label="需求标签" prop="tags">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            placeholder="请选择标签（可多选）"
            style="width: 100%"
          >
            <el-option
              v-for="tag in tagOptions"
              :key="tag.id"
              :label="tag.displayName"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ isAdd ? "发布" : "保存" }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import {
  getMyDemandList,
  deleteDemand,
  publishDemand,
  getDemandDetail,
  updateDemand,
} from "@/api/demand";
import { getMyManufactureList } from "@/api/enterprise";
import { getTagList } from "@/api/tag";

const router = useRouter();

// ---------- 当前选中的企业ID ----------
const selectedManuId = ref(null);

// ---------- 查询参数 ----------
const queryParams = reactive({
  page: 1,
  size: 10,
  status: "", // draft, published
  keyword: "",
});

const total = ref(0);
const demandList = ref([]);
const loading = ref(false);

// ---------- 弹窗控制 ----------
const dialogVisible = ref(false);
const isAdd = ref(true);
const currentDemandId = ref(null);
const submitting = ref(false);
const formRef = ref(null);

// 表单数据
const form = reactive({
  title: "",
  description: "",
  expectedBudget: null,
  deadline: "",
  tags: [],
});

// ---------- 企业列表 ----------
const enterprises = ref([]);
const loadingEnterprises = ref(false);
const currentEnterpriseName = ref("");

const fetchEnterprises = async () => {
  loadingEnterprises.value = true;
  try {
    const res = await getMyManufactureList({ page: 1, size: 100 });
    enterprises.value = res.records || [];
  } catch (error) {
    console.error("获取企业列表失败", error);
    ElMessage.error("获取企业列表失败");
  } finally {
    loadingEnterprises.value = false;
  }
};

const enterpriseOptions = computed(() =>
  enterprises.value.filter((item) => item.auditStatus === "approved"),
);

// 初始化默认选中第一个企业
const initSelectedEnterprise = () => {
  if (enterpriseOptions.value.length > 0) {
    selectedManuId.value = enterpriseOptions.value[0].id;
  }
};

// 切换企业时重新加载列表
const handleEnterpriseChange = () => {
  fetchList();
};

// ---------- 标签列表 ----------
const tagOptions = ref([]);
const categoryMap = {
  service: "服务类型",
  certification: "认证类型",
  product: "产品类型",
  general: "其他类型",
};

const fetchTags = async () => {
  try {
    const res = await getTagList({ page: 1, size: 500 });
    const tags = res.records || [];
    tagOptions.value = tags.map((tag) => ({
      id: tag.id,
      name: tag.name,
      category: tag.category,
      displayName: `${tag.name} (${categoryMap[tag.category] || tag.category || "其他"})`,
    }));
  } catch (error) {
    console.error("获取标签列表失败", error);
    ElMessage.error("获取标签列表失败");
  }
};

// ---------- 表单校验规则 ----------
const rules = {
  title: [
    { required: true, message: "请输入需求标题", trigger: "blur" },
    { min: 2, max: 100, message: "标题长度在2-100个字符", trigger: "blur" },
  ],
  description: [
    { max: 500, message: "描述不能超过500个字符", trigger: "blur" },
  ],
  expectedBudget: [
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === "") {
          callback();
        } else if (typeof value === "number" && value >= 0) {
          callback();
        } else {
          callback(new Error("预算必须为大于等于0的数字"));
        }
      },
      trigger: "blur",
    },
  ],
  deadline: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
        } else {
          const date = new Date(value);
          if (isNaN(date.getTime())) {
            callback(new Error("日期格式不正确"));
          } else {
            callback();
          }
        }
      },
      trigger: "change",
    },
  ],
  tags: [{ type: "array", required: false }],
};

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7;
};

// ---------- 状态映射 ----------
const statusText = (status) => {
  const map = {
    draft: "草稿",
    published: "待接单",
    matched: "已接单",
    closed: "已完成",
  };
  return map[status] || status;
};

const statusTagType = (status) => {
  const map = {
    draft: "info",
    published: "success",
    matched: "warning",
    closed: "",
  };
  return map[status] || "";
};

// ---------- 获取需求列表 ----------
const fetchList = async () => {
  if (!selectedManuId.value) return;
  loading.value = true;
  try {
    const res = await getMyDemandList({
      page: queryParams.page,
      size: queryParams.size,
      status: queryParams.status,
      keyword: queryParams.keyword,
      manuId: selectedManuId.value, // 传递选中的企业ID
    });

    demandList.value = res.records || [];
    total.value = res.total || 0;
  } catch (error) {
    console.error("获取需求列表失败", error);
    ElMessage.error("获取需求列表失败");
  } finally {
    loading.value = false;
  }
};

// ---------- 搜索与分页 ----------
const handleSearch = () => {
  queryParams.page = 1;
  fetchList();
};

const resetSearch = () => {
  queryParams.status = "";
  queryParams.keyword = "";
  handleSearch();
};

const handlePageChange = (page) => {
  queryParams.page = page > 0 ? page : 1;
  fetchList();
};

const handleSizeChange = (size) => {
  queryParams.size = size;
  queryParams.page = 1;
  fetchList();
};

// ---------- 新增/编辑弹窗 ----------
const dialogTitle = computed(() => (isAdd.value ? "发布新需求" : "编辑需求"));

const handleAdd = () => {
  if (!selectedManuId.value) {
    ElMessage.warning("请先选择一个企业");
    return;
  }
  isAdd.value = true;
  currentDemandId.value = null;
  resetForm();
  dialogVisible.value = true;
};

const handleEdit = async (row) => {
  if (row.manuId !== selectedManuId.value) {
    ElMessage.warning("当前选中的企业与需求所属企业不一致，无法编辑");
    return;
  }
  isAdd.value = false;
  currentDemandId.value = row.id;
  try {
    const res = await getDemandDetail(row.id);
    const data = res;
    form.title = data.title;
    form.description = data.description || "";
    form.expectedBudget =
      data.expectedBudget !== undefined ? data.expectedBudget : null;
    form.deadline = data.deadline || "";
    form.tags = data.tags?.map((tag) => tag.id) || [];
    dialogVisible.value = true;
  } catch (error) {
    console.error("获取需求详情失败", error);
    ElMessage.error("获取需求详情失败");
  }
};

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
  form.title = "";
  form.description = "";
  form.expectedBudget = null;
  form.deadline = "";
  form.tags = [];
};

const handleDialogClose = () => {
  resetForm();
};

const submitForm = async () => {
  if (!selectedManuId.value) {
    ElMessage.warning("请先选择一个企业");
    return;
  }
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch (err) {
    ElMessage.warning("请完整填写必填项");
    return;
  }

  submitting.value = true;
  try {
    const params = {
      manuId: selectedManuId.value, // 直接使用选中的企业ID
      title: form.title.trim(),
      description: form.description?.trim() || "",
      expectedBudget:
        form.expectedBudget !== null ? form.expectedBudget : undefined,
      deadline: form.deadline || undefined,
      tags: form.tags && form.tags.length ? form.tags : [],
    };

    if (isAdd.value) {
      await publishDemand(params);
      ElMessage.success("需求发布成功，等待管理员审核");
    } else {
      await updateDemand(currentDemandId.value, params);
      ElMessage.success("需求修改成功");
    }
    dialogVisible.value = false;
    fetchList();
  } catch (error) {
    console.error(isAdd.value ? "发布失败" : "修改失败", error);
    ElMessage.error(
      error.message ||
        (isAdd.value ? "发布失败，请稍后重试" : "修改失败，请稍后重试"),
    );
  } finally {
    submitting.value = false;
  }
};

// ---------- 删除需求 ----------
const handleDelete = (row) => {
  if (row.manuId !== selectedManuId.value) {
    ElMessage.warning("当前选中的企业与需求所属企业不一致，无法删除");
    return;
  }
  ElMessageBox.confirm("确定删除该需求吗？删除后不可恢复。", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteDemand(row.id);
        ElMessage.success("删除成功");
        fetchList();
      } catch (error) {
        console.error("删除失败", error);
        ElMessage.error("删除失败，请稍后重试");
      }
    })
    .catch(() => {});
};

// 查看合作（跳转到我的合作页面）
const viewCooperation = (row) => {
  router.push({ path: "/cooperation/my", query: { demandId: row.id } });
};

// 初始化
onMounted(async () => {
  await fetchEnterprises();
  await fetchTags();
  initSelectedEnterprise();
  if (selectedManuId.value) {
    fetchList();
  }
});
</script>

<style scoped>
.my-demands {
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

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.list-card {
  border-radius: 12px;
  overflow: hidden;
}

.tag-item {
  margin-right: 6px;
  margin-bottom: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.demand-form {
  margin-right: 20px;
}
.field-tip {
  font-size: 12px;
  color: #8590a6;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .my-demands {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }
}

.filter-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-form-left {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-actions {
    justify-content: flex-end;
  }
}

.filter-form-left .el-select {
  width: 120px;
}
</style>
