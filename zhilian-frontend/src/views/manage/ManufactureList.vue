<template>
  <div class="manufacture-list">
    <!-- 搜索栏 -->
    <el-row :gutter="10" class="search-bar">
      <el-col :span="5">
        <el-select
          v-model="searchForm.region"
          placeholder="选择区域"
          clearable
          multiple
          @change="handleSearch"
        >
          <el-option label="深圳" value="深圳" />
          <el-option label="华南" value="south" />
          <el-option label="华北" value="north" />
          <el-option label="西南" value="west" />
        </el-select>
      </el-col>
      <el-col :span="5">
        <el-select
          v-model="searchForm.scale"
          placeholder="选择规模"
          clearable
          multiple
          @change="handleSearch"
        >
          <el-option label="微型企业" value="micro" />
          <el-option label="小型企业" value="small" />
          <el-option label="中型企业" value="medium" />
          <el-option label="大型企业" value="large" />
        </el-select>
      </el-col>
      <el-col :span="5">
        <el-input
          v-model="searchForm.name"
          placeholder="主营产品类型"
          clearable
          @clear="handleSearch"
        />
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-col>
      <el-col :span="5" style="text-align: right">
        <el-button type="success" @click="openAddDialog">新增企业</el-button>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="companyName" label="企业名称" min-width="180" />
      <el-table-column
        prop="region"
        label="区域"
        width="120"
        :formatter="formatRegion"
      />
      <el-table-column
        prop="scale"
        label="规模"
        width="120"
        :formatter="formatScale"
      />
      <el-table-column
        prop="productType"
        label="主营产品类型"
        width="120"
        :formatter="formatProductType"
      />
      <el-table-column
        prop="contactPerson"
        label="联系人"
        width="120"
        :formatter="formatContactPerson"
      />
      <el-table-column
        prop="contactPhone"
        label="联系电话"
        width="120"
        :formatter="formatContactPhone"
      />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openViewDialog(row)">查看</el-button>
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      class="pagination"
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :page-sizes="[5, 10, 20]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="pagination.total"
      @size-change="handlePageChange"
      @current-change="handlePageChange"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      @closed="resetForm"
    >
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="企业名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="区域" prop="region">
          <el-select
            v-model="form.region"
            placeholder="请选择区域"
            style="width: 100%"
          >
            <el-option label="华东" value="east" />
            <el-option label="华南" value="south" />
            <el-option label="华北" value="north" />
            <el-option label="西南" value="west" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input
            type="textarea"
            resize="none"
            style="height: 60px"
            v-model="form.address"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input
            v-model="form.contactPerson"
            placeholder="请输入联系人姓名"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="规模" prop="scale">
          <el-select
            v-model="form.scale"
            placeholder="请选择规模"
            style="width: 100%"
          >
            <el-option label="微型企业" value="micro" />
            <el-option label="小型企业" value="small" />
            <el-option label="中型企业" value="medium" />
            <el-option label="大型企业" value="large" />
          </el-select>
        </el-form-item>
        <el-form-item label="员工人数" prop="employeeCount">
          <el-input-number
            v-model="form.employeeCount"
            :controls="false"
            :precision="0"
            :max="10000000"
            placeholder="请输入员工人数"
          />
        </el-form-item>
        <el-form-item label="年收入(万元)" prop="annualRevenue">
          <el-input-number
            v-model="form.annualRevenue"
            :controls="false"
            :precision="4"
            :max="10000000"
            placeholder="请输入公司年收入"
          />
        </el-form-item>
        <el-form-item label="主营产品类型" prop="productType">
          <el-input
            v-model="form.productType"
            placeholder="请输入主营产品类型"
          />
        </el-form-item>
        <el-form-item label="企业简介" prop="description">
          <el-input
            type="textarea"
            maxlength="1000"
            :autosize="{ minRows: 2, maxRows: 10 }"
            show-word-limit
            v-model="form.description"
            placeholder="请输入企业简介"
          />
        </el-form-item>
        <el-form-item label="企业logo" prop="logo">
          <el-upload
            action="#"
            :limit="1"
            accept=".png"
            v-model="form.logo"
            :http-request="customUpload"
            :on-remove="handleRemove"
            list-type="picture-card"
          >
            <template #trigger>
              <el-icon><Plus /></el-icon>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                jpg/png files with a size less than 500KB.
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="企业成立日期" prop="establishedDate">
          <el-date-picker
            v-model="form.establishedDate"
            placeholder="选择日期"
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
        <el-descriptions-item label="联系电话">{{
          detailDialog.data.contactPhone || "-"
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
          detailDialog.data.establishedDate || "-"
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
import { Plus } from "@element-plus/icons-vue";
import {
  getManufactureList,
  getManufactureDetail,
  addManufacture,
  updateManufacture,
  deleteManufacture,
} from "@/api/manufacture";
import { uploadFile, deleteFile } from "@/api/common";

// 加载状态
const loading = ref(false);

// 表格数据（直接存储后端返回的列表）
const tableData = ref([]);

// 搜索表单
const searchForm = reactive({
  region: "",
  scale: "",
  productType: "",
});

// 分页信息
const pagination = reactive({
  current: 1,
  size: 5,
  total: 0,
});

// ---------- 获取列表数据 ----------
const fetchList = async () => {
  loading.value = true;
  try {
    // 构建请求参数：分页 + 筛选
    const params = {
      page: pagination.current,
      size: pagination.size,
      region: searchForm.region || null,
      scale: searchForm.scale || null,
      productType: searchForm.productType || null,
    };
    const res = await getManufactureList(params);

    pagination.total = res.total;
    tableData.value = res.records;
  } catch (error) {
    ElMessage.error("获取列表失败");
    console.log("获取列表失败", error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1; // 重置到第一页
  fetchList();
};

// 重置
const resetSearch = () => {
  searchForm.region = "";
  searchForm.scale = "";
  searchForm.productType = "";
  handleSearch();
};

// 分页变化
const handlePageChange = () => {
  fetchList();
};

// ---------- 新增/编辑 ----------
const dialog = reactive({
  visible: false,
  title: "新增企业",
});

const form = reactive({
  id: null,
  companyName: "",
  region: "",
  address: "",
  contactPerson: "",
  contactPhone: "",
  scale: "",
  employeeCount: "",
  annualRevenue: "",
  productType: "",
  description: "",
  logo: "",
  establishedDate: "",
});

const formRef = ref(null);
const rules = {
  /* 保持不变 */
};

const openAddDialog = () => {
  dialog.title = "新增企业";
  form.id = null;
  form.companyName = "";
  form.region = "";
  form.address = "";
  form.contactPerson = "";
  form.contactPhone = "";
  form.scale = "";
  form.employeeCount = "";
  form.annualRevenue = "";
  form.productType = "";
  form.description = "";
  form.logo = "";
  form.establishedDate = "";
  dialog.visible = true;
};

const openEditDialog = async (row) => {
  try {
    // 调用详情接口，获取完整数据
    const detail = await getManufactureDetail(row.id);

    dialog.title = "编辑企业";
    // 将详情数据填充到表单
    form.id = detail.id;
    form.companyName = detail.companyName || "";
    form.region = detail.region || "";
    form.address = detail.address || "";
    form.contactPerson = detail.contactPerson || "";
    form.contactPhone = detail.contactPhone || "";
    form.scale = detail.scale || "";
    form.employeeCount = detail.employeeCount || "";
    form.annualRevenue = detail.annualRevenue || "";
    form.productType = detail.productType || "";
    form.description = detail.description || "";
    form.logo = detail.logo || "";
    form.establishedDate = detail.establishedDate || "";

    dialog.visible = true;
  } catch (error) {
    ElMessage.error("获取企业详情失败");
  } finally {
    editLoading.value = false;
  }
};

// 只读详情弹窗
const detailDialog = reactive({
  visible: false,
  data: {},
});

const openViewDialog = async (row) => {
  try {
    const res = await getManufactureDetail(row.id);
    detailDialog.data = res;
    detailDialog.visible = true;
  } catch (error) {
    ElMessage.error("获取企业详情失败");
    console.log("获取企业详情失败", error);
  } finally {
    detailDialog.loading = false;
  }
};

const resetForm = () => {
  formRef.value?.clearValidate();
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate();

  try {
    if (form.id) {
      // 编辑
      await updateManufacture(form.id, {
        name: form.name,
        region: form.region,
        scale: form.scale,
      });
      ElMessage.success("修改成功");
    } else {
      // 新增
      await addManufacture({
        name: form.name,
        region: form.region,
        scale: form.scale,
      });
      ElMessage.success("新增成功");
    }
    dialog.visible = false;
    // 重新获取列表（刷新数据）
    fetchList();
  } catch (error) {
    // 错误已由拦截器统一处理，无需额外操作
  }
};

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除企业“${row.name}”吗？`, "提示", {
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteManufacture(row.id);
        ElMessage.success("删除成功");
        fetchList();
      } catch (error) {
        // 错误已处理
      }
    })
    .catch(() => {});
};

// 文件上传
const customUpload = async (options) => {
  const file = options.file;
  try {
    // 调用上传函数，指定目录为 'logo/'（可选）
    const url = await uploadFile(file);
    // 将返回的 URL 存入表单
    form.logo = url;
    ElMessage.success("上传成功");
  } catch (error) {
    ElMessage.error("上传失败");
    console.log("上传失败", error);
  }
};

//文件删除
const handleRemove = async () => {
  try {
    // 调用后端删除接口
    await deleteFile(form.logo);
    // 清空本地 URL
    form.logo = "";
    ElMessage.success("删除成功");
  } catch (error) {
    ElMessage.error("删除失败");
    console.log("删除失败", error);
  }
};

// 格式化函数
const formatRegion = (row) => {
  const map = { 深圳: "深圳", south: "华南", north: "华北", west: "西南" };
  return map[row.region] || row.region;
};
const formatScale = (row) => {
  const map = { micro: "微型", small: "小型", medium: "中型", large: "大型" };
  return map[row.scale] || row.scale;
};

// 初始化
onMounted(() => {
  fetchList();
});
</script>

<style scoped>
.manufacture-list {
  padding: 20px;
}
.search-bar {
  margin-bottom: 20px;
}
.pagination {
  margin: 5px;
}
</style>
