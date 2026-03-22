<template>
  <div class="diagnosis-questionnaire">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">数字化诊断问卷</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>数字化诊断</el-breadcrumb-item>
          <el-breadcrumb-item>填写问卷</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 主表单卡片 -->
    <el-card class="form-card" shadow="hover">
      <div class="form-toolbar">
        <div class="form-title">企业数字化水平评估</div>
        <div class="form-actions">
          <el-tooltip content="刷新企业列表">
            <el-button
              :icon="Refresh"
              :loading="loadingEnterprises"
              circle
              @click="refreshEnterprises"
            />
          </el-tooltip>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="diagnosis-form"
        @submit.prevent
      >
        <!-- 企业选择（当有多家企业时显示） -->
        <el-form-item
          label="选择企业"
          prop="manuId"
          v-if="enterpriseOptions.length > 1"
        >
          <el-select
            v-model="form.manuId"
            placeholder="请选择要诊断的企业"
            style="width: 100%"
          >
            <el-option
              v-for="item in enterpriseOptions"
              :key="item.id"
              :label="item.companyName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 只有一家企业时显示企业名称（隐藏字段用于校验） -->
        <el-form-item
          v-else-if="enterpriseOptions.length === 1"
          prop="manuId"
          class="hidden-field"
        >
          <input type="hidden" :value="form.manuId" />
          <span
            >诊断企业：<strong>{{
              enterpriseOptions[0]?.companyName
            }}</strong></span
          >
        </el-form-item>

        <!-- 无企业提示 -->
        <div class="form-tip" v-else-if="enterpriseOptions.length === 0">
          <el-alert
            title="您尚未创建或没有任何已审核通过的制造企业，请先创建企业后再进行诊断"
            type="warning"
            show-icon
            :closable="false"
            style="margin-bottom: 20px"
          >
            <template #default>
              <el-button
                type="primary"
                size="small"
                @click="goToEnterprise"
                style="margin-top: 10px"
              >
                去创建企业
              </el-button>
            </template>
          </el-alert>
        </div>

        <div v-if="enterpriseOptions.length >= 1">
          <!-- 四个评分维度 -->
          <el-form-item
            label="信息化水平"
            prop="infoScore"
            required
            class="score-item"
          >
            <div class="score-slider">
              <el-slider
                v-model="form.infoScore"
                :min="1"
                :max="5"
                :step="1"
                :marks="scoreMarks"
                :format-tooltip="formatTooltip"
              />
            </div>
            <div class="score-desc">评估企业信息系统建设、数据采集等能力</div>
          </el-form-item>

          <el-form-item
            label="自动化水平"
            prop="autoScore"
            required
            class="score-item"
          >
            <div class="score-slider">
              <el-slider
                v-model="form.autoScore"
                :min="1"
                :max="5"
                :step="1"
                :marks="scoreMarks"
                :format-tooltip="formatTooltip"
              />
            </div>
            <div class="score-desc">评估生产线自动化、设备联网等能力</div>
          </el-form-item>

          <el-form-item
            label="数据应用"
            prop="dataScore"
            required
            class="score-item"
          >
            <div class="score-slider">
              <el-slider
                v-model="form.dataScore"
                :min="1"
                :max="5"
                :step="1"
                :marks="scoreMarks"
                :format-tooltip="formatTooltip"
              />
            </div>
            <div class="score-desc">评估数据分析、决策支持等能力</div>
          </el-form-item>

          <el-form-item
            label="服务协同"
            prop="serviceScore"
            required
            class="score-item"
          >
            <div class="score-slider">
              <el-slider
                v-model="form.serviceScore"
                :min="1"
                :max="5"
                :step="1"
                :marks="scoreMarks"
                :format-tooltip="formatTooltip"
              />
            </div>
            <div class="score-desc">评估与外部服务商协同、供应链整合能力</div>
          </el-form-item>

          <div class="form-actions-bottom">
            <el-button
              type="primary"
              size="large"
              @click="submitForm"
              :loading="submitting"
            >
              提交诊断
            </el-button>
            <el-button size="large" @click="resetForm">重置</el-button>
          </div>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from "vue";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { getMyManufactureList } from "@/api/enterprise";
import { getManufactureList } from "@/api/manufacture";
import { submitDiagnosis } from "@/api/diagnosis";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();

// ---------- 企业列表 ----------
const enterprises = ref([]);
const loadingEnterprises = ref(false);

// 获取企业列表（包含所有状态）
const fetchEnterprises = async () => {
  loadingEnterprises.value = true;
  const userRole = userStore.userInfo?.role;
  try {
    let res = {};
    if (userRole === "admin") {
      res = await getManufactureList({ page: 1, size: 100 });
    } else {
      res = await getMyManufactureList({ page: 1, size: 100 });
    }
    enterprises.value = res.records || [];
  } catch (error) {
    console.error("获取企业列表失败", error);
    ElMessage.error("获取企业列表失败，请稍后重试");
  } finally {
    loadingEnterprises.value = false;
  }
};

// 仅显示已审核通过的企业（供用户选择）
const enterpriseOptions = computed(() => {
  return enterprises.value.filter((item) => item.auditStatus === "approved");
});

// 同步选中的企业：自动选中唯一企业，或清空无效选中
const syncSelectedEnterprise = () => {
  const options = enterpriseOptions.value;
  if (options.length === 1) {
    // 只有一家企业时自动选中
    if (form.manuId !== options[0].id) {
      form.manuId = options[0].id;
    }
  } else if (options.length === 0) {
    // 没有企业时清空选中
    form.manuId = null;
  } else {
    // 多企业时，检查当前选中是否在列表中
    const exists = options.some((item) => item.id === form.manuId);
    if (!exists) {
      form.manuId = null;
    }
  }
};

// 刷新企业列表（保留现有评分，仅更新企业数据）
const refreshEnterprises = async () => {
  await fetchEnterprises();
  // fetch 完成后已调用同步，但 syncSelectedEnterprise 在 watch 中也会执行
  // 为保证立即生效，再调用一次
  syncSelectedEnterprise();
};

// 基于企业列表派生出仅包含 id 的数组，用于轻量监听
const enterpriseOptionIds = computed(() =>
  enterpriseOptions.value.map((item) => item.id),
);

// 监听企业选项变化（通过 id 列表），自动同步选中状态
watch(enterpriseOptionIds, () => {
  syncSelectedEnterprise();
});
// ---------- 表单 ----------
const formRef = ref(null);
const submitting = ref(false);

const form = reactive({
  manuId: null,
  infoScore: 3,
  autoScore: 3,
  dataScore: 3,
  serviceScore: 3,
});

// 评分标记（固定）
const scoreMarks = {
  1: "1分",
  2: "2分",
  3: "3分",
  4: "4分",
  5: "5分",
};

// 评分滑块提示格式
const formatTooltip = (val) => `${val}分`;

// 生成评分校验规则（减少重复代码）
const createScoreRule = (fieldName) => [
  { required: true, message: `请选择${fieldName}得分`, trigger: "change" },
  {
    type: "number",
    min: 1,
    max: 5,
    message: "得分必须在1-5之间",
    trigger: "change",
  },
];

// 校验规则
const rules = {
  manuId: [
    { required: true, message: "请选择要诊断的企业", trigger: "change" },
  ],
  infoScore: createScoreRule("信息化水平"),
  autoScore: createScoreRule("自动化水平"),
  dataScore: createScoreRule("数据应用"),
  serviceScore: createScoreRule("服务协同"),
};

// 提交
const submitForm = async () => {
  if (enterpriseOptions.value.length === 0) {
    ElMessage.warning("请先创建并确保至少有一家企业审核通过");
    goToEnterprise();
    return;
  }

  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch (err) {
    console.log("表单校验失败", err);
    return;
  }

  submitting.value = true;
  try {
    const res = await submitDiagnosis({
      manuId: form.manuId,
      infoScore: form.infoScore,
      autoScore: form.autoScore,
      dataScore: form.dataScore,
      serviceScore: form.serviceScore,
    });
    ElMessage.success("诊断提交成功，正在生成报告...");
    if (res.diagnosisId && router.hasRoute("DiagnosisReport")) {
      router.push(`/diagnosis/${res.diagnosisId}`);
    } else {
      router.push("/");
    }
  } catch (error) {
    console.error("提交失败", error);
    ElMessage.error("提交失败，请稍后重试");
  } finally {
    submitting.value = false;
  }
};

// 重置表单（仅重置评分，保留企业选择）
const resetForm = () => {
  form.infoScore = 3;
  form.autoScore = 3;
  form.dataScore = 3;
  form.serviceScore = 3;
  // 不清空 form.manuId
  formRef.value?.clearValidate();
};

// 跳转至企业创建页面（需根据实际路由调整）
const goToEnterprise = () => {
  router.push("/enterprise");
};

// 初始化
onMounted(async () => {
  await fetchEnterprises();
  // 同步选中状态在 watch 中自动完成，但 fetch 完成后可能 watch 还未触发，手动调用一次确保初始状态
  syncSelectedEnterprise();
});
</script>

<style scoped>
.diagnosis-questionnaire {
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

.form-card {
  border-radius: 12px;
  overflow: hidden;
}

.form-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.form-title {
  font-weight: 600;
  color: #1f2f3d;
}

.form-actions {
  display: flex;
  gap: 8px;
}

.diagnosis-form {
  padding: 30px 20px 20px;
}

.score-item {
  margin-bottom: 25px;
}

.score-item :deep(.el-form-item__label) {
  font-size: 18px;
}

.score-item :deep(.el-form-item__content) {
  margin-left: 0 !important;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.score-slider {
  width: 80%;
  margin-bottom: 25px;
}

.score-desc {
  font-size: 14px;
  color: #8590a6;
  text-align: center;
  width: 100%;
}

.form-actions-bottom {
  margin-top: 40px;
  text-align: center;
}

.form-actions-bottom .el-button {
  min-width: 120px;
}

.hidden-field :deep(.el-form-item__content) {
  margin-left: 0 !important;
  padding-left: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 32px;
}

.form-tip {
  font-size: 12px;
  color: #e6a23c;
  margin-top: 4px;
}

.form-tip a {
  color: #409eff;
  text-decoration: none;
}
</style>
