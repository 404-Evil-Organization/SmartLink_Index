<template>
  <div class="enterprise-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的企业</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>我的企业</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon> 创建企业
        </el-button>
      </div>
    </div>

    <!-- 选项卡 -->
    <el-card class="tab-card" shadow="hover">
      <el-tabs
        v-model="activeTab"
        @tab-click="handleTabClick"
        :class="{ 'hide-tabs-header': !isAdmin }"
      >
        <!-- 制造企业选项卡 -->
        <el-tab-pane label="制造企业" name="manufacture">
          <div class="tab-content">
            <div class="search-bar">
              <el-input
                v-model="manuSearchKeyword"
                placeholder="企业名称"
                clearable
                style="width: 200px; margin-right: 10px"
                @clear="fetchManufactureList"
                @keyup.enter="fetchManufactureList"
              />
              <el-button type="primary" @click="fetchManufactureList"
                >查询</el-button
              >
              <el-button @click="resetManuSearch">重置</el-button>
              <el-tooltip content="刷新">
                <el-button
                  :icon="Refresh"
                  circle
                  @click="fetchManufactureList"
                  style="margin-left: auto"
                />
              </el-tooltip>
            </div>

            <el-table
              v-loading="manuLoading"
              :data="manufactureList"
              border
              stripe
              style="width: 100%"
            >
              <el-table-column
                type="index"
                label="序号"
                width="70"
                align="center"
              />
              <el-table-column
                prop="companyName"
                label="企业名称"
                min-width="150"
              />
              <el-table-column prop="region" label="区域" width="100" />
              <el-table-column prop="scale" label="规模" width="100">
                <template #default="{ row }">
                  {{ formatScale(row.scale) }}
                </template>
              </el-table-column>
              <el-table-column
                prop="productType"
                label="主营产品"
                min-width="120"
              >
                <template #default="{ row }">
                  <el-tag
                    v-for="tag in row.productType
                      ? String(row.productType).split(',')
                      : []"
                    :key="tag"
                    size="small"
                    effect="plain"
                    style="margin-right: 5px; margin-bottom: 3px"
                  >
                    {{ tag.trim() }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="contactPerson"
                label="联系人"
                width="100"
              />
              <el-table-column prop="contactPhone" label="联系电话" width="130">
                <template #default="{ row }">
                  {{ showPhone(row.contactPhone) }}
                </template>
              </el-table-column>
              <el-table-column prop="auditStatus" label="审核状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getAuditStatusType(row.auditStatus)">
                    {{ getAuditStatusText(row.auditStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button
                    size="small"
                    @click="openDetail(row, 'manufacture')"
                  >
                    <el-icon><View /></el-icon> 查看
                  </el-button>
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    @click="openEditDialog(row, 'manufacture')"
                    :disabled="row.auditStatus === 'rejected'"
                  >
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    @click="deleteEnterprise(row, 'manufacture')"
                    :disabled="row.auditStatus !== 'pending'"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                v-model:current-page="manuPagination.current"
                v-model:page-size="manuPagination.size"
                :page-sizes="[5, 10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="manuPagination.total"
                @size-change="
                  (size) => {
                    manuPagination.size = size;
                    manuPagination.current = 1;
                    fetchManufactureList();
                  }
                "
                @current-change="fetchManufactureList"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 服务商选项卡 -->
        <el-tab-pane label="服务商" name="service">
          <div class="tab-content">
            <div class="search-bar">
              <el-input
                v-model="serviceSearchKeyword"
                placeholder="企业名称"
                clearable
                style="width: 200px; margin-right: 10px"
                @clear="fetchServiceList"
                @keyup.enter="fetchServiceList"
              />
              <el-button type="primary" @click="fetchServiceList"
                >查询</el-button
              >
              <el-button @click="resetServiceSearch">重置</el-button>
              <el-tooltip content="刷新">
                <el-button
                  :icon="Refresh"
                  circle
                  @click="fetchServiceList"
                  style="margin-left: auto"
                />
              </el-tooltip>
            </div>

            <el-table
              v-loading="serviceLoading"
              :data="serviceList"
              border
              stripe
              style="width: 100%"
            >
              <el-table-column
                type="index"
                label="序号"
                width="70"
                align="center"
              />
              <el-table-column
                prop="companyName"
                label="企业名称"
                min-width="150"
              />
              <el-table-column prop="region" label="区域" width="100" />
              <el-table-column
                prop="serviceType"
                label="服务类型"
                min-width="180"
              >
                <template #default="{ row }">
                  <el-tag
                    v-for="tag in normalizeTags(row.serviceType)"
                    :key="tag"
                    size="small"
                    effect="plain"
                    style="margin-right: 5px; margin-bottom: 3px"
                  >
                    {{ tag }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="contactPerson"
                label="联系人"
                width="100"
              />
              <el-table-column prop="contactPhone" label="联系电话" width="130">
                <template #default="{ row }">
                  {{ showPhone(row.contactPhone) }}
                </template>
              </el-table-column>
              <el-table-column prop="auditStatus" label="审核状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getAuditStatusType(row.auditStatus)">
                    {{ getAuditStatusText(row.auditStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="openDetail(row, 'service')">
                    <el-icon><View /></el-icon> 查看
                  </el-button>
                  <el-button
                    size="small"
                    type="primary"
                    plain
                    @click="openEditDialog(row, 'service')"
                    :disabled="row.auditStatus === 'rejected'"
                  >
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    @click="deleteEnterprise(row, 'service')"
                    :disabled="row.auditStatus !== 'pending'"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                v-model:current-page="servicePagination.current"
                v-model:page-size="servicePagination.size"
                :page-sizes="[5, 10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="servicePagination.total"
                @size-change="
                  (size) => {
                    servicePagination.size = size;
                    servicePagination.current = 1;
                    fetchServiceList();
                  }
                "
                @current-change="fetchServiceList"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 企业详情弹窗（只读） -->
    <el-dialog
      v-model="detailDialog.visible"
      :title="detailDialog.title"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <template v-if="detailDialog.type === 'manufacture'">
          <el-descriptions-item label="企业名称">{{
            detailDialog.data.companyName || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="区域">{{
            detailDialog.data.region || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">{{
            detailDialog.data.address || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{
            detailDialog.data.contactPerson || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{
            showPhone(detailDialog.data.contactPhone)
          }}</el-descriptions-item>
          <el-descriptions-item label="规模">{{
            formatScale(detailDialog.data.scale)
          }}</el-descriptions-item>
          <el-descriptions-item label="员工人数">{{
            detailDialog.data.employeeCount || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="年收入(万元)">{{
            detailDialog.data.annualRevenue || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="主营产品类型">
            <template v-if="detailDialog.data.productType">
              <el-tag
                v-for="tag in String(detailDialog.data.productType).split(',')"
                :key="tag"
                size="small"
                effect="plain"
                style="margin-right: 5px; margin-bottom: 3px"
              >
                {{ tag.trim() }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="企业简介" :span="2">{{
            detailDialog.data.description || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="成立日期">{{
            detailDialog.data.establishedDate || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getAuditStatusType(detailDialog.data.auditStatus)">
              {{ getAuditStatusText(detailDialog.data.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="detailDialog.data.auditRemark"
            label="审核意见"
            :span="2"
          >
            {{ detailDialog.data.auditRemark }}
          </el-descriptions-item>
          <el-descriptions-item label="企业logo">
            <el-image
              v-if="detailDialog.data.logo"
              :src="detailDialog.data.logo"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 4px"
            />
            <span v-else>-</span>
          </el-descriptions-item>
        </template>
        <template v-else>
          <el-descriptions-item label="企业名称">{{
            detailDialog.data.companyName || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="区域">{{
            detailDialog.data.region || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">{{
            detailDialog.data.address || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{
            detailDialog.data.contactPerson || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{
            showPhone(detailDialog.data.contactPhone)
          }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{
            detailDialog.data.serviceType || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="企业简介" :span="2">{{
            detailDialog.data.description || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="企业官网">{{
            detailDialog.data.website || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="成立日期">{{
            detailDialog.data.establishedDate || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="员工人数">{{
            detailDialog.data.employeeCount || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="资质概述">{{
            detailDialog.data.qualification || "-"
          }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getAuditStatusType(detailDialog.data.auditStatus)">
              {{ getAuditStatusText(detailDialog.data.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="detailDialog.data.auditRemark"
            label="审核意见"
            :span="2"
          >
            {{ detailDialog.data.auditRemark }}
          </el-descriptions-item>
          <el-descriptions-item label="企业logo">
            <el-image
              v-if="detailDialog.data.logo"
              :src="detailDialog.data.logo"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 4px"
            />
            <span v-else>-</span>
          </el-descriptions-item>
        </template>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗（动态表单） -->
    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.title"
      width="600px"
      @closed="resetForm"
    >
      <el-form
        :model="form"
        label-width="100px"
        ref="formRef"
        :rules="currentRules"
      >
        <!-- 通用字段 -->
        <el-form-item label="企业名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="区域" prop="region">
          <el-select
            v-model="form.region"
            placeholder="请选择区域"
            style="width: 100%"
          >
            <el-option
              v-for="region in regionOptions"
              :key="region"
              :label="region"
              :value="region"
            />
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

        <!-- 制造企业特有字段 -->
        <template v-if="formDialog.enterpriseType === 'manufacture'">
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
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="年收入(万元)" prop="annualRevenue">
            <el-input-number
              v-model="form.annualRevenue"
              :controls="false"
              :precision="4"
              :max="10000000"
              placeholder="请输入年收入"
              style="width: 100%"
            />
          </el-form-item>
          <!-- 主营产品类型改为多选下拉，支持多选、可搜索、可创建 -->
          <el-form-item label="主营产品类型" prop="productType">
            <el-select
              v-model="form.productType"
              placeholder="请选择主营产品类型"
              style="width: 100%"
              multiple
              filterable
              allow-create
              default-first-option
              :reserve-keyword="false"
            >
              <el-option
                v-for="item in productTypeOptions"
                :key="item.id"
                :label="item.name"
                :value="item.name"
              />
            </el-select>
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
        </template>

        <!-- 服务商特有字段 -->
        <template v-else>
          <el-form-item label="服务类型" prop="serviceType">
            <el-select
              v-model="form.serviceType"
              placeholder="请选择服务类型"
              style="width: 100%"
              multiple
              filterable
              allow-create
              default-first-option
              :reserve-keyword="false"
            >
              <el-option
                v-for="item in serviceTypeOptions"
                :key="item.id"
                :label="item.name"
                :value="item.name"
              />
            </el-select>
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
          <el-form-item label="企业官网" prop="website">
            <el-input v-model="form.website" placeholder="请输入企业官网地址" />
          </el-form-item>
          <el-form-item label="成立日期" prop="establishedDate">
            <el-date-picker
              value-format="YYYY-MM-DD"
              v-model="form.establishedDate"
              placeholder="选择日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="员工人数" prop="employeeCount">
            <el-input-number
              v-model="form.employeeCount"
              :controls="false"
              :precision="0"
              :max="10000000"
              placeholder="请输入员工人数"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="资质概述" prop="qualification">
            <el-input
              type="textarea"
              maxlength="500"
              :autosize="{ minRows: 2, maxRows: 5 }"
              show-word-limit
              v-model="form.qualification"
              placeholder="请输入资质概述"
            />
          </el-form-item>
        </template>

        <!-- 通用字段（续） -->
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

        <el-form-item
          label="成立日期"
          prop="establishedDate"
          v-if="formDialog.enterpriseType === 'manufacture'"
        >
          <el-date-picker
            value-format="YYYY-MM-DD"
            v-model="form.establishedDate"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus, Refresh, View } from "@element-plus/icons-vue";
import { getMyManufactureList, getMyServiceList } from "@/api/enterprise";
import {
  getManufactureDetail,
  addManufacture,
  updateManufacture,
  deleteManufacture,
} from "@/api/manufacture";
import {
  getServiceProviderDetail,
  addServiceProvider,
  updateServiceProvider,
  deleteServiceProvider,
} from "@/api/service-provider";
import {
  getServiceTags,
  getProductTags,
  getRegions,
  uploadFile,
  deleteFile,
} from "@/api/common";
import { useUserStore } from "@/stores/user";
import { maskPhone } from "@/utils/desensitize";
import { normalizeTags, joinTags } from "@/utils/tagUtils";

const userStore = useUserStore();
const userRole = computed(() => userStore.userInfo?.role);
const showPhone = (phone) => maskPhone(phone, userStore.userInfo?.role);
const isAdmin = computed(() => userRole.value === "admin");

// 当前激活选项卡（根据角色设置默认值）
const activeTab = ref(
  isAdmin.value
    ? "manufacture"
    : userRole.value === "manufacture"
      ? "manufacture"
      : "service",
);

// 制造企业相关
const manufactureList = ref([]);
const manuLoading = ref(false);
const manuSearchKeyword = ref("");
const manuPagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

// 服务商相关
const serviceList = ref([]);
const serviceLoading = ref(false);
const serviceSearchKeyword = ref("");
const servicePagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

// 选项数据
const regionOptions = ref([]);
const serviceTypeOptions = ref([]);
const productTypeOptions = ref([]);

// 详情弹窗
const detailDialog = reactive({
  visible: false,
  title: "",
  type: "", // manufacture / service
  data: {},
});

// 新增/编辑弹窗
const formDialog = reactive({
  visible: false,
  title: "",
  type: "", // 'add' / 'edit'
  enterpriseType: "", // 'manufacture' / 'service'
});

const form = reactive({
  id: null,
  companyName: "",
  region: "",
  address: "",
  contactPerson: "",
  contactPhone: "",
  logo: "",
  establishedDate: "",
  // 制造企业特有
  scale: "",
  employeeCount: null,
  annualRevenue: null,
  productType: [], // 改为数组
  description: "",
  // 服务商特有
  serviceType: [],
  website: "",
  qualification: "",
});

const formRef = ref(null);
const fileList = ref([]);
// 记录原始 logo，用于编辑时对比
const originalLogo = ref("");
// 记录新上传的 logo，用于删除
const uploadedNewLogo = ref("");

// 获取制造企业列表
const fetchManufactureList = async () => {
  manuLoading.value = true;
  try {
    const params = {
      page: manuPagination.current,
      size: manuPagination.size,
    };
    if (manuSearchKeyword.value) {
      params.companyName = manuSearchKeyword.value;
    }
    const res = await getMyManufactureList(params);
    let records = res.records || [];
    manufactureList.value = records;
    manuPagination.total = res.total || 0;
  } catch (error) {
    console.error("获取制造企业列表失败", error);
    ElMessage.error("获取制造企业列表失败");
  } finally {
    manuLoading.value = false;
  }
};

// 获取服务商列表
const fetchServiceList = async () => {
  serviceLoading.value = true;
  try {
    const params = {
      page: servicePagination.current,
      size: servicePagination.size,
    };
    if (serviceSearchKeyword.value) {
      params.companyName = serviceSearchKeyword.value;
    }
    const res = await getMyServiceList(params);
    let records = res.records || [];
    serviceList.value = records;
    servicePagination.total = res.total || 0;
  } catch (error) {
    console.error("获取服务商列表失败", error);
    ElMessage.error("获取服务商列表失败");
  } finally {
    serviceLoading.value = false;
  }
};

// 获取区域选项
const fetchRegions = async () => {
  try {
    const res = await getRegions();
    regionOptions.value = Array.isArray(res) ? res : [];
  } catch (error) {
    console.error("获取区域列表失败", error);
  }
};

// 获取服务类型选项
const fetchServiceTypeOptions = async () => {
  try {
    const res = await getServiceTags();
    serviceTypeOptions.value = Array.isArray(res) ? res : [];
  } catch (error) {
    console.error("获取服务类型标签失败", error);
  }
};

// 获取产品类型选项
const fetchProductTypeOptions = async () => {
  try {
    const res = await getProductTags();
    productTypeOptions.value = Array.isArray(res) ? res : [];
  } catch (error) {
    console.error("获取产品类型标签失败", error);
  }
};

// 重置搜索
const resetManuSearch = () => {
  manuSearchKeyword.value = "";
  manuPagination.current = 1;
  fetchManufactureList();
};
const resetServiceSearch = () => {
  serviceSearchKeyword.value = "";
  servicePagination.current = 1;
  fetchServiceList();
};

// 切换选项卡（仅管理员有效）
const handleTabClick = () => {
  if (isAdmin.value) {
    if (activeTab.value === "manufacture") {
      fetchManufactureList();
    } else {
      fetchServiceList();
    }
  }
};

// 打开详情弹窗
const openDetail = async (row, type) => {
  try {
    let data;
    if (type === "manufacture") {
      data = await getManufactureDetail(row.id);
      detailDialog.title = "制造企业详情";
    } else {
      data = await getServiceProviderDetail(row.id);
      detailDialog.title = "服务商详情";
    }
    detailDialog.data = data;
    detailDialog.type = type;
    detailDialog.visible = true;
  } catch (error) {
    console.error("获取详情失败", error);
    ElMessage.error("获取详情失败");
  }
};

// 创建企业弹窗，直接使用 activeTab 作为企业类型
const openCreateDialog = () => {
  formDialog.type = "add";
  formDialog.enterpriseType = activeTab.value;
  formDialog.title =
    activeTab.value === "manufacture" ? "新增制造企业" : "新增服务商";
  formDialog.visible = true;
  resetFormData();
};

// 打开编辑弹窗
const openEditDialog = async (row, type) => {
  try {
    let detail;
    if (type === "manufacture") {
      detail = await getManufactureDetail(row.id);
    } else {
      detail = await getServiceProviderDetail(row.id);
    }
    formDialog.type = "edit";
    formDialog.enterpriseType = type;
    formDialog.title = type === "manufacture" ? "编辑制造企业" : "编辑服务商";
    formDialog.visible = true;

    // 填充表单
    form.id = detail.id;
    form.companyName = detail.companyName || "";
    form.region = detail.region || "";
    form.address = detail.address || "";
    form.contactPerson = detail.contactPerson || "";
    form.contactPhone = detail.contactPhone || "";
    form.logo = detail.logo || "";
    form.establishedDate = detail.establishedDate || null;

    if (type === "manufacture") {
      form.scale = detail.scale || "";
      form.employeeCount = detail.employeeCount ?? null;
      form.annualRevenue = detail.annualRevenue ?? null;
      // 将字符串转换为数组
      form.productType = detail.productType
        ? detail.productType.split(",").map((item) => item.trim())
        : [];
      form.description = detail.description || "";
    } else {
      form.serviceType = normalizeTags(detail.serviceType);
      form.website = detail.website || "";
      form.employeeCount = detail.employeeCount ?? null;
      form.qualification = detail.qualification || "";
      form.description = detail.description || "";
    }

    if (detail.logo) {
      const fileName = detail.logo.split("/").pop() || "logo.jpg";
      fileList.value = [
        { name: fileName, url: detail.logo, status: "success" },
      ];
      originalLogo.value = detail.logo;
    } else {
      fileList.value = [];
      originalLogo.value = "";
    }
  } catch (error) {
    console.error("获取详情失败", error);
    ElMessage.error("获取详情失败");
  }
};

// 重置表单数据
const resetFormData = () => {
  form.id = null;
  form.companyName = "";
  form.region = "";
  form.address = "";
  form.contactPerson = "";
  form.contactPhone = "";
  form.logo = "";
  form.establishedDate = null;
  form.scale = "";
  form.employeeCount = null;
  form.annualRevenue = null;
  form.productType = [];
  form.description = "";
  form.serviceType = [];
  form.website = "";
  form.qualification = "";
  fileList.value = [];
  // 重置 logo 相关的状态
  originalLogo.value = "";
  uploadedNewLogo.value = "";
  formRef.value?.clearValidate();
};

// 动态表单校验规则
const baseRules = {
  companyName: [{ required: true, message: "请输入企业名称", trigger: "blur" }],
  region: [{ required: true, message: "请选择区域", trigger: "change" }],
  contactPerson: [
    { required: true, message: "请输入联系人姓名", trigger: "blur" },
  ],
  contactPhone: [
    { required: true, message: "请输入联系电话", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
};

const manufactureRules = {
  scale: [{ required: true, message: "请选择规模", trigger: "change" }],
};

const serviceRules = {
  serviceType: [
    { required: true, message: "请选择服务类型", trigger: "change" },
  ],
};

const currentRules = computed(() => {
  if (formDialog.enterpriseType === "manufacture") {
    return { ...baseRules, ...manufactureRules };
  } else {
    return { ...baseRules, ...serviceRules };
  }
});

// 文件上传
const customUpload = async (options) => {
  const { file, onSuccess, onError } = options;
  try {
    const url = await uploadFile(file);
    // 如果之前有新上传的 logo 未保存，先清理掉
    if (uploadedNewLogo.value) {
      try {
        await deleteFile(uploadedNewLogo.value);
      } catch (e) {
        console.log("清理旧的未保存logo失败", e);
      }
    }
    form.logo = url;
    uploadedNewLogo.value = url;
    const uploadedFile = {
      name: file.name,
      url: url,
      uid: file.uid,
      status: "success",
    };
    if (typeof onSuccess === "function") onSuccess({ url });
    fileList.value = [uploadedFile];
    ElMessage.success("上传成功");
  } catch (error) {
    ElMessage.error("上传失败");
    if (typeof onError === "function") onError(error);
    console.log("上传失败", error);
  }
};

const beforeUpload = (file) => {
  const allowedTypes = ["image/png", "image/jpeg", "image/jpg"];
  const isImage = allowedTypes.includes(file.type);
  const fileName = file.name;
  const fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
  const allowedExts = [".png", ".jpg", ".jpeg"];
  const isExtValid = allowedExts.includes(fileExt);
  if (!isImage || !isExtValid) {
    ElMessage.error("只能上传 PNG、JPG 或 JPEG 格式的图片");
    return false;
  }
  const maxSize = 10 * 1024 * 1024;
  if (file.size > maxSize) {
    ElMessage.error("图片大小不能超过 10MB");
    return false;
  }
  return true;
};

const handleRemove = async () => {
  try {
    // 只有在删除的是本次新上传的图片时，才直接调用 deleteFile 删除云端文件
    if (form.logo === uploadedNewLogo.value && uploadedNewLogo.value !== "") {
      await deleteFile(form.logo);
      uploadedNewLogo.value = "";
    }
    // 如果删除的是历史原始 logo，则仅清空表单数据，等提交成功后再决定是否删除云端文件
    form.logo = "";
    fileList.value = [];
    ElMessage.success("移除成功");
  } catch (error) {
    ElMessage.error("移除失败");
    console.log("移除失败", error);
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch (error) {
    console.log("表单校验未通过", error);
    return;
  }

  let payload;
  if (formDialog.enterpriseType === "manufacture") {
    payload = {
      companyName: form.companyName,
      region: form.region,
      address: form.address,
      contactPerson: form.contactPerson,
      contactPhone: form.contactPhone,
      scale: form.scale,
      employeeCount: form.employeeCount ?? null,
      annualRevenue: form.annualRevenue ?? null,
      productType: joinTags(form.productType), // 数组转逗号分隔字符串
      description: form.description,
      logo: form.logo,
      establishedDate: form.establishedDate,
    };
  } else {
    payload = {
      companyName: form.companyName,
      region: form.region,
      address: form.address,
      contactPerson: form.contactPerson,
      contactPhone: form.contactPhone,
      serviceType: joinTags(form.serviceType),
      description: form.description,
      logo: form.logo,
      website: form.website,
      establishedDate: form.establishedDate,
      employeeCount: form.employeeCount ?? null,
      qualification: form.qualification,
    };
  }

  try {
    if (formDialog.type === "add") {
      if (formDialog.enterpriseType === "manufacture") {
        await addManufacture(payload);
        ElMessage.success("新增成功");
        fetchManufactureList();
      } else {
        await addServiceProvider(payload);
        ElMessage.success("新增成功");
        fetchServiceList();
      }
    } else {
      if (formDialog.enterpriseType === "manufacture") {
        await updateManufacture(form.id, payload);
        ElMessage.success("修改成功");
        fetchManufactureList();
      } else {
        await updateServiceProvider(form.id, payload);
        ElMessage.success("修改成功");
        fetchServiceList();
      }
      
      // 提交成功后，如果新上传了 logo 或者删除了 logo，需要清理旧的原始 logo
      if (originalLogo.value && originalLogo.value !== form.logo) {
        try {
          await deleteFile(originalLogo.value);
        } catch (e) {
          console.log("清理被替换的旧logo失败", e);
        }
      }
    }
    // 提交成功后，清除上传记录，防止弹窗关闭时误删
    uploadedNewLogo.value = "";
    formDialog.visible = false;
  } catch (error) {
    // 错误由拦截器统一处理
  }
};

const resetForm = async () => {
  // 如果弹窗关闭时（比如取消），存在已上传但未提交的新 logo，则删除它
  if (uploadedNewLogo.value) {
    try {
      await deleteFile(uploadedNewLogo.value);
    } catch (e) {
      console.log("清理未保存的新logo失败", e);
    }
  }
  resetFormData();
};

// 删除企业
const deleteEnterprise = async (row, type) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除企业“${row.companyName}”吗？删除后不可恢复。`,
      "提示",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );
    if (type === "manufacture") {
      await deleteManufacture(row.id);
      ElMessage.success("删除成功");
      fetchManufactureList();
    } else {
      await deleteServiceProvider(row.id);
      ElMessage.success("删除成功");
      fetchServiceList();
    }
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      console.error("删除失败", error);
      ElMessage.error("删除失败");
    }
  }
};

// 辅助函数
const formatScale = (scale) => {
  const map = { micro: "微型", small: "小型", medium: "中型", large: "大型" };
  return map[scale] || scale;
};
const getAuditStatusType = (status) => {
  const map = { pending: "warning", approved: "success", rejected: "danger" };
  return map[status] || "info";
};
const getAuditStatusText = (status) => {
  const map = { pending: "待审核", approved: "已通过", rejected: "已驳回" };
  return map[status] || status;
};

// 在 onMounted 中只加载当前 activeTab 对应的数据
onMounted(() => {
  fetchRegions();
  fetchServiceTypeOptions();
  fetchProductTypeOptions();
  if (activeTab.value === "manufacture") {
    fetchManufactureList();
  } else {
    fetchServiceList();
  }
});
</script>

<style scoped>
.hide-tabs-header :deep(.el-tabs__header) {
  display: none;
}
.enterprise-dashboard {
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
.tab-card {
  border-radius: 12px;
  overflow: hidden;
}
.tab-content {
  padding: 20px;
}
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
