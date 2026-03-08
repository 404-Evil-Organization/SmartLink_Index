<template>
  <!-- 优化：将简单的 div 改为全屏居中卡片，增加渐变背景和品牌标识，提升视觉体验 -->
  <div class="login-container">
    <el-card class="login-card">
      <!-- 新增 Logo 区域：统一品牌形象 -->
      <div class="logo-wrapper">
        <el-icon size="40" color="#409eff">
          <Connection />
        </el-icon>
        <span class="logo-text">智链指数</span>
      </div>

      <!-- 新增标语：丰富页面层次，传递产品理念 -->
      <p class="slogan">连接 · 握手 · 智造</p>
      <p class="subtitle">智能制造 · 协同服务</p>

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
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <!-- 新增注册引导链接，便于用户切换操作 -->
        <div class="register-link">
          还没有账号？
          <el-link type="primary" @click="$router.push('/register')"
            >立即注册</el-link
          >
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login } from "@/api/auth";
import { useUserStore } from "@/stores/user";
// 新增：显式引入图标，保证按需加载
import { Connection, User, Lock } from "@element-plus/icons-vue";

const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);
// 创建loading状态防止用户重复请求登录（原始注释保留）
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
  ElMessage.info('忘记密码功能开发中，敬请期待');
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
    // 跳转到首页（原始注释保留）
    router.push("/");
  } catch (error) {
    console.error("登录失败", error);
  } finally {
    loginLoading.value = false;
  }
};
</script>

<style scoped>
/* 完全重写样式：全屏居中、渐变背景、卡片精致风格 */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.login-card {
  width: 420px;                /* 登录卡片稍窄，适合少量内容 */
  max-width: 94%;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  padding: 30px 32px 40px;
  background-color: #ffffff;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin: 0 0 20px;
}

.logo-text {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 1px;
}

.slogan {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin: 5px 0 6px;
  letter-spacing: 1px;
}

.subtitle {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-bottom: 32px;
}

/* 表单项间距 */
.el-form-item {
  margin-bottom: 22px;
}

/* 输入框样式优化 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e4e7ed inset;
  border-radius: 12px;
  transition: box-shadow 0.2s, border-color 0.2s;
  padding: 4px 12px;
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409eff inset;
}

/* 标签样式微调 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #34495e;
  padding-bottom: 6px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 16px;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
}
.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}
.login-button:active {
  transform: translateY(0);
}

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}
.register-link .el-link {
  font-weight: 500;
}
</style>