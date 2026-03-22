<template>
  <div class="dashboard-home">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">评价服务商</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/cooperation/my' }"
            >我的合作</el-breadcrumb-item
          >
          <el-breadcrumb-item>发表评价</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <el-card class="form-card" shadow="hover">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="evaluation-form"
        v-loading="detailLoading"
      >
        <el-form-item label="合作信息">
          <div class="coop-info">
            <div v-if="coopInfo" class="info-text">
              <p>服务商：{{ coopInfo.serviceName }}</p>
              <p>需求：{{ coopInfo.demandTitle }}</p>
              <p>合作金额：{{ coopInfo.amount }} 万元</p>
              <p>
                合作时间：{{ coopInfo.startDate }} 至 {{ coopInfo.endDate }}
              </p>
            </div>
            <div v-else class="loading-info">加载合作信息中...</div>
          </div>
        </el-form-item>

        <el-form-item label="评分" prop="score">
          <el-rate
            v-model="form.score"
            :texts="['1星', '2星', '3星', '4星', '5星']"
            show-text
          />
        </el-form-item>

        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="请写下您对服务商的评价（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="匿名评价" prop="isAnonymous">
          <el-switch v-model="form.isAnonymous" />
          <span class="anonymous-tip"
            >开启后，评价人姓名将隐藏为“匿名用户”</span
          >
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="detailLoading || !coopInfo"
            @click="handleSubmit"
          >
            提交评价
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { submitEvaluation } from "@/api/evaluation";
import { getCooperationDetail } from "@/api/cooperation";

const route = useRoute();
const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);
const detailLoading = ref(false);

// 表单数据
const form = reactive({
  score: null,
  content: "",
  isAnonymous: false,
});

// 合作信息（用于页面展示）
const coopInfo = ref(null);
let coopId = null; // 存储有效的数字ID

// 校验 coopId 是否为有效正整数
const isValidCoopId = (id) => {
  const num = Number(id);
  return Number.isFinite(num) && num > 0 && id.toString() === num.toString();
};

// 获取合作详情（通过 coopId）
const fetchCoopDetail = async () => {
  const rawCoopId = route.query.coopId;
  if (!rawCoopId || !isValidCoopId(rawCoopId)) {
    ElMessage.error("无效的合作记录ID");
    router.push("/cooperation/my");
    return;
  }
  coopId = Number(rawCoopId);
  detailLoading.value = true; // 开始加载
  try {
    const res = await getCooperationDetail(coopId);
    // 校验合作是否已完成且未评价
    if (res.status !== "completed") {
      ElMessage.error("只有已完成的合作才能评价");
      router.push("/cooperation/my");
      return;
    }
    if (res.hasEvaluated) {
      ElMessage.error("您已经评价过该合作");
      router.push("/cooperation/my");
      return;
    }
    coopInfo.value = res;
  } catch (error) {
    console.error("获取合作详情失败", error);
    ElMessage.error("获取合作信息失败");
    router.push("/cooperation/my");
  } finally {
    detailLoading.value = false; // 结束加载
  }
};

// 提交评价（增加二次校验）
const handleSubmit = async () => {
  // 二次校验合作信息是否满足条件（防止异步竞态）
  if (!coopInfo.value) {
    ElMessage.error("合作信息未加载完成，请稍后重试");
    return;
  }
  if (coopInfo.value.status !== "completed") {
    ElMessage.error("只有已完成的合作才能评价");
    router.push("/cooperation/my");
    return;
  }
  if (coopInfo.value.hasEvaluated) {
    ElMessage.error("您已经评价过该合作");
    router.push("/cooperation/my");
    return;
  }

  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch (error) {
    // 校验失败，无需额外处理
    return;
  }

  submitting.value = true;
  try {
    await submitEvaluation({
      coopId,
      score: form.score,
      content: form.content,
      isAnonymous: form.isAnonymous,
    });
    ElMessage.success("评价提交成功");
    router.push("/cooperation/my");
  } catch (error) {
    console.error("提交评价失败", error);
    ElMessage.error("提交失败，请稍后重试");
  } finally {
    submitting.value = false;
  }
};

// 表单校验规则
const rules = {
  score: [
    { required: true, message: "请选择评分", trigger: "change" },
    {
      type: "number",
      min: 1,
      max: 5,
      message: "评分范围为 1~5 星",
      trigger: "change",
    },
  ],
  content: [{ max: 500, message: "评价内容不能超过500字", trigger: "blur" }],
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchCoopDetail();
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
.form-card {
  border-radius: 12px;
  overflow: hidden;
}
.evaluation-form {
  padding: 20px;
}
.coop-info {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  width: 100%;
}
.info-text p {
  margin: 6px 0;
  line-height: 1.5;
  color: #606266;
}
.loading-info {
  color: #909399;
  font-style: italic;
}
.anonymous-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}
</style>
