<template>
  <!-- 优化：将简单的 div 改为全屏居中卡片，增加渐变背景和品牌标识，提升视觉体验 -->
  <AuthCard>
    <!-- 优化：label-position="top" 使标签在输入框上方，适配移动端；添加键盘回车事件 -->
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      @keyup.enter="handleLogin"
    >
      <el-form-item label="用户名" prop="username">
        <!-- 优化：添加 prefix-icon 图标，提升可识别性；改为 :prefix-icon 绑定图标组件（原字符串形式需全局注册，现按需引入） -->
        <el-input
          v-model="form.username"
          placeholder="请输入用户名"
          :prefix-icon="User"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <!-- 优化：添加 show-password 密码可见切换，提升用户体验；图标绑定同上 -->
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          :prefix-icon="Lock"
          show-password
        />
      </el-form-item>

      <!-- 新增“记住密码”和“忘记密码”选项，功能预留，界面更完整 -->
      <div class="login-options">
        <el-checkbox v-model="remember">记住密码</el-checkbox>
        <!-- 优化：为“忘记密码”添加点击事件，避免无反馈链接（功能暂未实现，预留提示） -->
        <el-link type="primary" :underline="false" @click="handleForgotPassword">忘记密码？</el-link>
      </div>

      <el-form-item>
        <el-button
          type="primary"
          :loading="loginLoading"
          class="auth-button login-button"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form-item>

      <!-- 新增注册引导链接，便于用户切换操作 -->
      <div class="auth-link">
        还没有账号？
        <el-link type="primary" @click="$router.push('/register')"
          >立即注册</el-link
        >
      </div>
    </el-form>
  </AuthCard>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login } from "@/api/auth";
import { useUserStore } from "@/stores/user";
// 新增：显式引入图标，保证按需加载
import { Connection, User, Lock } from "@element-plus/icons-vue";
// 引入公共卡片组件
import AuthCard from "@/components/AuthCard.vue";

const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);
// 创建loading状态防止用户重复请求登录
const loginLoading = ref(false);
// 新增：记住密码状态（功能暂未实现，UI预留）
const remember = ref(false);

const form = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

// 新增：忘记密码点击处理（功能预留）
const handleForgotPassword = () => {
  ElMessage.info("忘记密码功能开发中，敬请期待");
};

const handleLogin = async () => {
  if (loginLoading.value) return;

  // 表单验证
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loginLoading.value = true;
  try {
    const res = await login(form);
    const { token } = res;
    userStore.setToken(token);
    await userStore.fetchUserInfo();

    ElMessage.success("登录成功");
    // 跳转到首页
    router.push("/");
  } catch (error) {
    console.error("登录失败", error);
  } finally {
    loginLoading.value = false;
  }
};
</script>

<style scoped>
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.login-button {
  height: 48px !important;
  font-size: 16px !important;
  margin-top: 16px !important;
}
</style>