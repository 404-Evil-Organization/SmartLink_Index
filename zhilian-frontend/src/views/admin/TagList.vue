<template>
  <div class="tag-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">标签管理</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>标签管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openAddDialog" :icon="Plus">
          新增标签
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="标签名称">
              <el-input
                v-model="searchForm.name"
                placeholder="请输入标签名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="类别">
              <el-select
                v-model="searchForm.category"
                placeholder="请选择类别"
                clearable
              >
                <el-option label="认证" value="certification" />
                <el-option label="服务" value="service" />
                <el-option label="产品" value="product" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" style="text-align: right">
            <el-form-item label-width="0">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">标签列表</div>
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
        <el-table-column prop="name" label="标签名称" min-width="150" />
        <el-table-column
          prop="category"
          label="类别"
          width="120"
          :formatter="formatCategory"
        />
        <el-table-column
          prop="description"
          label="描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
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
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      @closed="resetForm"
    >
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select
            v-model="form.category"
            placeholder="请选择类别"
            style="width: 100%"
          >
            <el-option label="认证" value="certification" />
            <el-option label="服务" value="service" />
            <el-option label="产品" value="product" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            type="textarea"
            v-model="form.description"
            placeholder="请输入标签描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Refresh, Edit, Delete } from "@element-plus/icons-vue";
import { getTagList, addTag, updateTag, deleteTag } from "@/api/tag";

// ---------- 加载状态 ----------
const loading = ref(false);

// ---------- 表格数据 ----------
const tableData = ref([]);
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

// ---------- 搜索表单 ----------
const searchForm = reactive({
  name: "",
  category: "",
});

// 获取列表
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...(searchForm.name && { name: searchForm.name }),
      ...(searchForm.category && { category: searchForm.category }),
    };
    const res = await getTagList(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  fetchList();
};

// 重置
const resetSearch = () => {
  searchForm.name = "";
  searchForm.category = "";
  handleSearch();
};

// 分页变化 - 当前页改变
const handlePageChange = (page) => {
  // 更新当前页并拉取列表
  pagination.current = page;
  fetchList();
};
// 分页变化 - 每页条数改变
const handlePageSizeChange = (pageSize) => {
  // 更新每页条数时重置到第一页，避免请求到不存在的页码
  pagination.size = pageSize;
  pagination.current = 1;
  fetchList();
};

// ---------- 新增/编辑弹窗 ----------
const dialog = reactive({
  visible: false,
  title: "新增标签",
});

const form = reactive({
  id: null,
  name: "",
  category: "",
  description: "",
});

const formRef = ref(null);

const rules = {
  name: [{ required: true, message: "请输入标签名称", trigger: "blur" }],
  category: [{ required: true, message: "请选择类别", trigger: "change" }],
};

const openAddDialog = () => {
  dialog.title = "新增标签";
  form.id = null;
  form.name = "";
  form.category = "";
  form.description = "";
  dialog.visible = true;
};

const openEditDialog = (row) => {
  dialog.title = "编辑标签";
  form.id = row.id;
  form.name = row.name;
  form.category = row.category;
  form.description = row.description;
  dialog.visible = true;
};

const resetForm = () => {
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch (error) {
    console.log("表单校验未通过：", error);
    return;
  }

  try {
    if (form.id) {
      // 编辑
      await updateTag(form.id, {
        name: form.name,
        category: form.category,
        description: form.description,
      });
      ElMessage.success("修改成功");
    } else {
      // 新增
      await addTag({
        name: form.name,
        category: form.category,
        description: form.description,
      });
      ElMessage.success("新增成功");
    }
    dialog.visible = false;
    fetchList(); // 刷新列表
  } catch (error) {
    // 错误已由拦截器统一处理，无需额外操作
  }
};

// ---------- 删除 ----------
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除标签“${row.name}”吗？`, "提示", {
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteTag(row.id);
        ElMessage.success("删除成功");
        fetchList();
      } catch (error) {
        // 错误已处理
      }
    })
    .catch(() => {});
};

// ---------- 格式化显示（类别代码转中文） ----------
const formatCategory = (row) => {
  const map = {
    certification: "认证",
    service: "服务",
    product: "产品",
    other: "其他",
  };
  return map[row.category] || row.category;
};

// 初始化
onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.tag-dashboard {
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

.search-bar {
  margin-bottom: 16px;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.search-form {
  width: 100%;
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
