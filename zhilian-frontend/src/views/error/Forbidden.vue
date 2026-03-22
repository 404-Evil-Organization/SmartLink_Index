<template>
  <div class="forbidden-container">
    <el-card class="forbidden-card" shadow="hover">
      <div class="content">
        <!-- 错误代码 403 -->
        <h1 class="error-code">403</h1>
        <!-- 标题 -->
        <h2 class="error-title">抱歉，您无权访问此页面</h2>
        <!-- 描述 -->
        <p class="error-desc">
          您的账号权限不足，无法查看当前内容。请联系管理员或切换账号后重试。
        </p>
        <!-- 操作按钮组 -->
        <div class="actions">
          <el-button type="primary" @click="goHome">返回首页</el-button>
          <el-button @click="goLogin">重新登录</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";

const router = useRouter();

// 返回首页
const goHome = () => {
  router.push("/");
};

// 跳转登录页
const goLogin = () => {
  const userStore = useUserStore();
  userStore.clearToken();
  router.push("/login");
};
</script>

<style scoped>
.forbidden-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.forbidden-card {
  max-width: 500px;
  width: 100%;
  border-radius: 8px;
}

.content {
  text-align: center;
  padding: 30px 20px;
}

.error-code {
  font-size: 80px;
  font-weight: 600;
  color: #f56c6c; /* Element Plus 危险色 */
  margin: 0 0 10px;
  line-height: 1;
}

.error-title {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 15px;
}

.error-desc {
  font-size: 16px;
  color: #606266;
  margin: 0 0 30px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .error-code {
    font-size: 60px;
  }
  .error-title {
    font-size: 20px;
  }
  .error-desc {
    font-size: 14px;
  }
}
</style>
