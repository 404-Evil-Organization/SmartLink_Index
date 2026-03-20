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
              @click="fetchEnterprises"
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
        <!-- 企业选择（仅当有多家企业时显示） -->
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

        <!-- 隐藏的企业ID（当只有一家企业时自动填充） -->
        <el-form-item
          v-else-if="enterpriseOptions.length === 1"
          prop="manuId"
          class="hidden-field"
        >
          <span
            >诊断企业：<strong>{{
              enterpriseOptions[0]?.companyName
            }}</strong></span
          >
        </el-form-item>

        <!-- 无企业提示 -->
        <div class="form-tip" v-else-if="enterpriseOptions.length === 0">
          <el-alert
            title="您尚未创建制造企业，请先创建企业后再进行诊断"
            type="warning"
            show-icon
            :closable="false"
            style="margin-bottom: 20px"
          />
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
import { ref, reactive, onMounted, computed } from "vue";
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
  } finally {
    loadingEnterprises.value = false;
  }
};

const enterpriseOptions = computed(() => {
  return enterprises.value.filter((item) => item.auditStatus === "approved"); // 仅显示已审核企业
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

// 当只有一家企业时自动选中
onMounted(() => {
  fetchEnterprises().then(() => {
    if (enterpriseOptions.value.length === 1) {
      form.manuId = enterpriseOptions.value[0].id;
    }
  });
});

// 评分标记
const scoreMarks = {
  1: "1分",
  2: "2分",
  3: "3分",
  4: "4分",
  5: "5分",
};

// 校验规则
const rules = {
  manuId: [
    { required: true, message: "请选择要诊断的企业", trigger: "change" },
  ],
  infoScore: [
    { required: true, message: "请选择信息化得分", trigger: "change" },
    {
      type: "number",
      min: 1,
      max: 5,
      message: "得分必须在1-5之间",
      trigger: "change",
    },
  ],
  autoScore: [
    { required: true, message: "请选择自动化得分", trigger: "change" },
    {
      type: "number",
      min: 1,
      max: 5,
      message: "得分必须在1-5之间",
      trigger: "change",
    },
  ],
  dataScore: [
    { required: true, message: "请选择数据应用得分", trigger: "change" },
    {
      type: "number",
      min: 1,
      max: 5,
      message: "得分必须在1-5之间",
      trigger: "change",
    },
  ],
  serviceScore: [
    { required: true, message: "请选择服务协同得分", trigger: "change" },
    {
      type: "number",
      min: 1,
      max: 5,
      message: "得分必须在1-5之间",
      trigger: "change",
    },
  ],
};

// 提交
const submitForm = async () => {
  if (enterpriseOptions.value.length === 0) {
    ElMessage.warning("请先创建制造企业");
    // router.push("/enterprise"); //后续补上
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
    // 当前路由表尚未提供诊断报告页（/diagnosis/report/:id），因此仅在本页提示成功，避免跳转到不存在的页面
    ElMessage.success(
      "诊断提交成功，报告正在后台生成，请稍后在诊断记录中查看。",
    );
    // TODO: 后续若补齐诊断报告页与路由（/diagnosis/report/:id），可在此根据 res.diagnosisId 进行跳转
    // router.push(`/diagnosis/report/${res.diagnosisId}`);
    router.push("/");
  } catch (error) {
    console.error("提交失败", error);
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const resetForm = () => {
  form.manuId =
    enterpriseOptions.value.length === 1 ? enterpriseOptions.value[0].id : null;
  form.infoScore = 3;
  form.autoScore = 3;
  form.dataScore = 3;
  form.serviceScore = 3;
  formRef.value?.clearValidate();
};
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
