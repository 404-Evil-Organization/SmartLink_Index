<template>
  <div class="manufacture-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">制造企业管理</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>制造企业管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button
          v-if="hasPermission('add')"
          type="primary"
          @click="openAddDialog"
          :icon="Plus"
        >
          新增企业
        </el-button>
        <!-- <el-button :icon="Download">导出</el-button> -->
      </div>
    </div>

    <!-- 统计卡片区域（数据静态，可替换为接口数据） -->
    <!-- <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in statistics" :key="stat.label">
        <el-card
          class="stat-card"
          :body-style="{ padding: '20px' }"
          shadow="hover"
        >
          <div class="stat-icon" :style="{ background: stat.bgColor }">
            <el-icon :size="24" :color="stat.color"
              ><component :is="stat.icon"
            /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-trend" v-if="stat.trend">
              <span :class="stat.trend > 0 ? 'up' : 'down'">
                {{ stat.trend > 0 ? "+" : "" }}{{ stat.trend }}%
              </span>
              较上月
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row> -->

    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="区域">
              <el-select
                v-model="searchForm.region"
                placeholder="选择区域"
                clearable
              >
                <el-option label="深圳" value="深圳" />
                <el-option label="华南" value="south" />
                <el-option label="华北" value="north" />
                <el-option label="西南" value="west" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="规模">
              <el-select
                v-model="searchForm.scale"
                placeholder="选择规模"
                clearable
              >
                <el-option label="微型企业" value="micro" />
                <el-option label="小型企业" value="small" />
                <el-option label="中型企业" value="medium" />
                <el-option label="大型企业" value="large" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="主营产品">
              <el-input
                v-model="searchForm.productType"
                placeholder="输入产品类型"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6" style="text-align: right">
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
        <div class="table-title">制造企业列表</div>
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
        <!-- 展开行，显示更多信息 -->
        <!-- <el-table-column type="expand" width="40">
          <template #default="{ row }">
            <div v-loading="row._loading" class="expanded-detail">
              <template v-if="row.detail">
                <p>
                  <strong>详细地址：</strong>{{ row.detail.address || "-" }}
                </p>
                <p>
                  <strong>企业简介：</strong
                  >{{ row.detail.description || "暂无简介" }}
                </p>
                <p>
                  <strong>成立日期：</strong
                  >{{ row.detail.establishedDate || "-" }}
                </p>
              </template>
              <el-empty v-else description="暂无详情" />
            </div>
          </template>
        </el-table-column> -->

        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="companyName" label="企业名称" min-width="150" />
        <el-table-column
          prop="region"
          label="区域"
          width="100"
          :formatter="formatRegion"
        />
        <el-table-column
          prop="scale"
          label="规模"
          width="100"
          :formatter="formatScale"
        />
        <el-table-column prop="productType" label="主营产品" min-width="120" />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130">
          <template #default="{ row }">
            <!-- 对手机号进行脱敏处理，仅展示前3位和后4位，避免直接暴露完整号码 -->
            <span>
              {{
                row.contactPhone
                  ? row.contactPhone.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2")
                  : "-"
              }}
            </span>
          </template>
        </el-table-column>
        <!-- 状态列（假设接口有 status 字段，若无可以暂时隐藏） -->
        <!-- <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
              v-if="row.status !== undefined"
            />
            <span v-else>-</span>
          </template>
        </el-table-column> -->
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openViewDialog(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button
              v-if="hasPermission('edit')"
              size="small"
              @click="openEditDialog(row)"
            >
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              v-if="hasPermission('delete')"
              size="small"
              type="danger"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页（右侧） -->
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
            accept=".png,.jpg,.jpeg"
            :file-list="fileList"
            :http-request="customUpload"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            list-type="picture-card"
          >
            <template #trigger>
              <el-icon><Plus /></el-icon>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                仅支持上传 jpg / jpeg / png 格式的图片，文件大小不能超过 10MB。
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="企业成立日期" prop="establishedDate">
          <el-date-picker
            value-format="YYYY-MM-DD"
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
        <el-descriptions-item label="手机号">{{
          showPhone(detailDialog.data.contactPhone)
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
import {
  Plus,
  Refresh,
  Grid,
  Edit,
  Delete,
  View,
  Shop,
  User,
  Star,
  TrendCharts,
} from "@element-plus/icons-vue";
// 导入原有 API
import {
  getManufactureList,
  getManufactureDetail,
  addManufacture,
  updateManufacture,
  deleteManufacture,
} from "@/api/manufacture";
import { uploadFile, deleteFile } from "@/api/common";
import { usePermission } from "@/composables/usePermission";
const { hasPermission } = usePermission();

// ---------- 统计卡片数据（静态，可改为接口获取） ----------
// const statistics = ref([
//   {
//     icon: Shop,
//     label: "制造企业总数",
//     value: 186,
//     trend: 8,
//     bgColor: "#ecf5ff",
//     color: "#409eff",
//   },
//   {
//     icon: User,
//     label: "活跃企业",
//     value: 142,
//     trend: 5,
//     bgColor: "#f0f9eb",
//     color: "#67c23a",
//   },
//   {
//     icon: Star,
//     label: "平均评分",
//     value: 4.2,
//     trend: 2,
//     bgColor: "#fdf6ec",
//     color: "#e6a23c",
//   },
//   {
//     icon: TrendCharts,
//     label: "合作需求",
//     value: 67,
//     trend: -3,
//     bgColor: "#fef0f0",
//     color: "#f56c6c",
//   },
// ]);

// ---------- 搜索折叠 ----------
// const searchExpanded = ref(true);
// const toggleSearch = () => {
//   searchExpanded.value = !searchExpanded.value;
// };

// 搜索表单
const searchForm = reactive({
  region: "",
  scale: "",
  productType: "",
  // status: null, // 如果接口不支持，可忽略
});

// ---------- 表格数据、分页、加载等（复用原有逻辑） ----------
const loading = ref(false);
const tableData = ref([]);
const pagination = reactive({
  current: 1,
  size: 5,
  total: 0,
});

const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      region: searchForm.region || "",
      scale: searchForm.scale || "",
      productType: searchForm.productType || null,
    };
    const res = await getManufactureList(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
  } catch (error) {
    ElMessage.error("获取列表失败");
    console.log("获取列表失败", error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchList();
};

const resetSearch = () => {
  searchForm.region = "";
  searchForm.scale = "";
  searchForm.productType = "";
  // searchForm.status = null;
  handleSearch();
};

const handlePageChange = () => {
  fetchList();
};

// // ---------- 状态切换（如果接口支持） ----------
// const handleStatusChange = async (row, val) => {
//   try {
//     await updateManufacture(row.id, { status: val });
//     ElMessage.success(`${row.companyName} 已${val === 1 ? "启用" : "禁用"}`);
//     fetchList();
//   } catch (error) {
//     row.status = val === 1 ? 0 : 1; // 回滚
//     ElMessage.error("操作失败");
//   }
// };

// ---------- 新增/编辑弹窗逻辑 ----------
const dialog = reactive({ visible: false, title: "新增企业" });
const form = reactive({
  id: null,
  companyName: "",
  region: "",
  address: "",
  contactPerson: "",
  contactPhone: "",
  scale: "",
  employeeCount: null,
  annualRevenue: null,
  productType: "",
  description: "",
  logo: "",
  establishedDate: "",
});

const formRef = ref(null);
const rules = {
  // 企业名称：必填，触发时机为失焦
  companyName: [{ required: true, message: "请输入企业名称", trigger: "blur" }],
  // 区域：必填，触发时机为选择
  region: [{ required: true, message: "请选择区域", trigger: "change" }],
  // 联系人：必填
  contactPerson: [
    { required: true, message: "请输入联系人姓名", trigger: "blur" },
  ],
  // 联系电话：必填 + 手机号格式校验（中国大陆）
  contactPhone: [
    { required: true, message: "请输入联系电话", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  // 规模：必填
  scale: [{ required: true, message: "请选择规模", trigger: "change" }],
  // 其余字段（地址、员工人数、年收入、主营产品类型、简介、logo、成立日期）为非必填，不添加校验规则
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
  form.employeeCount = null;
  form.annualRevenue = null;
  form.productType = "";
  form.description = "";
  form.logo = "";
  form.establishedDate = "";

  fileList.value = [];
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
    form.employeeCount = detail.employeeCount ?? null;
    form.annualRevenue = detail.annualRevenue ?? null;
    form.productType = detail.productType || "";
    form.description = detail.description || "";
    form.logo = detail.logo || "";
    form.establishedDate = detail.establishedDate || null;

    if (detail.logo) {
      const fileName = detail.logo.split("/").pop() || "logo.jpg";
      fileList.value = [
        {
          name: fileName,
          url: detail.logo,
          status: "success",
        },
      ];
    } else {
      fileList.value = [];
    }

    dialog.visible = true;
  } catch (error) {
    ElMessage.error("获取企业详情失败");
    console.log("获取企业详情失败", error);
  }
};

const submitForm = async () => {
  if (!formRef.value) return;
  try {
    // 表单校验：若校验失败会抛出异常，这里用 try/catch 捕获，防止未捕获 Promise 拒绝
    await formRef.value.validate();
  } catch (error) {
    // 校验未通过时不进入后续提交流程，只输出调试日志
    console.log("表单校验未通过：", error);
    return;
  }

  // 按与后端接口一致的字段一次性提交企业信息
  const payload = {
    companyName: form.companyName,
    region: form.region,
    address: form.address,
    contactPerson: form.contactPerson,
    contactPhone: form.contactPhone,
    scale: form.scale,
    employeeCount:
      form.employeeCount !== null && form.employeeCount !== undefined
        ? String(form.employeeCount)
        : "",
    annualRevenue:
      form.annualRevenue !== null && form.annualRevenue !== undefined
        ? String(form.annualRevenue)
        : "",
    productType: form.productType,
    description: form.description,
    logo: form.logo,
    establishedDate: form.establishedDate,
  };

  try {
    if (form.id) {
      await updateManufacture(form.id, payload);
      ElMessage.success("修改成功");
    } else {
      // 新增
      await addManufacture(payload);
      ElMessage.success("新增成功");
    }
    dialog.visible = false;
    // 重新获取列表（刷新数据）
    fetchList();
  } catch (error) {
    // 错误已由拦截器统一处理，无需额外操作
  }
};

const resetForm = () => {
  formRef.value?.clearValidate();
};

// ---------- 删除 ----------
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除企业“${row.companyName}”吗？`, "提示", {
    type: "warning",
  })
    .then(async () => {
      try {
        await deleteManufacture(row.id);
        ElMessage.success("删除成功");
        fetchList();
      } catch (error) {
        ElMessage.error("删除失败");
        console.log("删除失败", error);
      }
    })
    .catch(() => {});
};

// ---------- 文件上传/删除 ----------
const fileList = ref([]);

const customUpload = async (options) => {
  const { file, onSuccess, onError } = options;
  try {
    // 先上传新文件
    const url = await uploadFile(file);
    // 记录旧的 logo 地址，仅在存在且非空时尝试删除
    const oldLogo = form.logo;
    if (oldLogo && typeof oldLogo === "string" && oldLogo.trim() !== "") {
      try {
        await deleteFile(oldLogo);
      } catch (deleteError) {
        console.warn("删除旧 logo 失败", deleteError);
        ElMessage.warning("新 logo 已上传，但旧 logo 删除失败，请稍后重试");
      }
    }
    form.logo = url;
    // 构造符合 UploadFile 格式的对象
    const uploadedFile = {
      name: file.name,
      url: url,
      uid: file.uid,
      status: "success",
    };
    if (typeof onSuccess === "function") {
      onSuccess({ url });
    }
    fileList.value = [uploadedFile];
    ElMessage.success("上传成功");
  } catch (error) {
    ElMessage.error("上传失败");
    if (typeof onError === "function") {
      onError(error);
    }
    console.log("上传失败", error);
  }
};

// 文件上传前校验
const beforeUpload = (file) => {
  // 1. 校验文件类型
  const allowedTypes = ["image/png", "image/jpeg", "image/jpg"];
  const isImage = allowedTypes.includes(file.type);
  // 同时检查文件后缀（防止某些浏览器 MIME 类型不准确）
  const fileName = file.name;
  const fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
  const allowedExts = [".png", ".jpg", ".jpeg"];
  const isExtValid = allowedExts.includes(fileExt);

  if (!isImage || !isExtValid) {
    ElMessage.error("只能上传 PNG、JPG 或 JPEG 格式的图片");
    return false;
  }

  // 2. 校验文件大小（10MB = 10 * 1024 * 1024）
  const maxSize = 10 * 1024 * 1024;
  if (file.size > maxSize) {
    ElMessage.error("图片大小不能超过 10MB");
    return false;
  }

  return true;
};

const handleRemove = async () => {
  try {
    await deleteFile(form.logo);
    form.logo = "";
    fileList.value = [];
    ElMessage.success("删除成功");
  } catch (error) {
    ElMessage.error("删除失败");
    console.log("删除失败", error);
  }
};

// ---------- 详情弹窗 ----------
const detailDialog = reactive({ visible: false, data: {} });
const openViewDialog = async (row) => {
  try {
    const res = await getManufactureDetail(row.id);
    detailDialog.data = res;
    detailDialog.visible = true;
  } catch (error) {
    ElMessage.error("获取企业详情失败");
    console.log("获取企业详情失败", error);
  }
};

import { maskPhone } from "@/utils/desensitize";
import { useUserStore } from "@/stores/user";
const userStore = useUserStore();
const showPhone = (phone) => {
  return maskPhone(phone, userStore.userInfo.role);
};

// 展开行事件：expandedRows 是当前展开的所有行数据
// const handleExpandChange = async (row, expandedRows) => {
//   if (expandedRows.includes(row) && !row.detail) {
//     // 标记加载中
//     row._loading = true;
//     try {
//       const detail = await getManufactureDetail(row.id);
//       // 将详情数据挂载到 row 上
//       row.detail = detail;
//     } catch (error) {
//       ElMessage.error("加载详情失败");
//       console.log("加载详情失败",error);
//     } finally {
//       row._loading = false;
//     }
//   }
// };

// ---------- 格式化 ----------
const formatRegion = (row) => {
  const map = { 深圳: "深圳", south: "华南", north: "华北", west: "西南" };
  return map[row.region] || row.region;
};
const formatScale = (row) => {
  const map = { micro: "微型", small: "小型", medium: "中型", large: "大型" };
  return map[row.scale] || row.scale;
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped>
/* 直接复制服务商页面的样式，并适当调整类名 */
.manufacture-dashboard {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}
</style>
