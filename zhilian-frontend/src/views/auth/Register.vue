<template>
  <!-- 优化：统一全屏居中卡片风格，与登录页一致 -->
  <div class="register-container">
    <el-card class="register-card">
      <!-- 相同 Logo 区域，保证品牌统一 -->
      <div class="logo-wrapper">
        <el-icon :size="36" color="#409eff">
          <Connection />
        </el-icon>
        <span class="logo-text">智链指数</span>
      </div>

      <p class="slogan">连接 · 握手 · 智造</p>
      <p class="subtitle">智能制造 · 协同服务</p>

      <!-- 优化：改为垂直单列布局，所有字段顺序排列（原两列布局已移除） -->
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <!-- 优化：添加 prefix-icon 图标；改为 :prefix-icon 绑定图标组件 -->
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <!-- 角色选择（原始注释保留） -->
        <el-form-item label="角色" prop="role">
          <el-select
            v-model="form.role"
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <!-- 优化：添加 prefix-icon 图标；改为 :prefix-icon 绑定图标组件 -->
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <!-- 优化：添加 prefix-icon 图标；改为 :prefix-icon 绑定图标组件 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <!-- 优化：添加 prefix-icon 图标；改为 :prefix-icon 绑定图标组件 -->
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <!-- 优化：添加 prefix-icon 图标；改为 :prefix-icon 绑定图标组件 -->
        <el-form-item label="电子邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="registerLoading"
            class="register-button"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <!-- 新增登录入口链接，便于已注册用户跳转 -->
        <div class="login-link">
          已有账号？
          <el-link type="primary" @click="$router.push('/login')"
            >立即登录</el-link
          >
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { register } from "@/api/auth";
import { useRouter } from "vue-router";
// 新增：引入图标，与登录页统一
import { Connection, User, Lock, Phone, Message } from "@element-plus/icons-vue";

const router = useRouter();
const registerLoading = ref(false);
const formRef = ref(null);

//表单数据
const form = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  role: "",
  phone: "",
  email: "",
});

//角色选项
const options = [
  {
    value: "manufacture",
    label: "制造企业",
  },
  {
    value: "service",
    label: "服务商",
  },
  {
    value: "park",
    label: "园区/政府",
  },
  {
    value: "admin",
    label: "管理员",
  },
];

// 手机号正则（中国大陆手机号）
const phoneRegex = /^1[3-9]\d{9}$/;
// 邮箱正则
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// 表单验证规则
const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
  phone: [
    { required: true, message: "请输入联系电话", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (!phoneRegex.test(value)) {
          callback(new Error("请输入正确的手机号"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  email: [
    { required: true, message: "请输入电子邮箱", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (!emailRegex.test(value)) {
          callback(new Error("请输入正确的邮箱格式"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

//注册行为
const handleRegister = async () => {
  if (registerLoading.value) return;

  // 表单验证
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  registerLoading.value = true;
  try {
    // 调用注册接口
    await register({
      username: form.username,
      password: form.password,
      role: form.role,
      phone: form.phone,
      email: form.email,
    });
    ElMessage.success("注册成功");

    // 跳转登录页
    router.push("/login");
  } catch (err) {
    ElMessage.error("注册失败，请稍后重试");
    console.error("注册失败", err);
  } finally {
    registerLoading.value = false;
  }
};
</script>

<style scoped>
/* 与登录页保持完全一致的视觉风格，尺寸调小以更紧凑 */
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.register-card {
  width: 420px;                /* 收窄至与登录页一致 */
  max-width: 94%;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  padding: 24px 28px 32px;      /* 缩小内边距，更紧凑 */
  background-color: #ffffff;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;                   /* 略微缩小间距 */
  margin: 0 0 16px;            /* 减少下方留白 */
}

.logo-text {
  font-size: 28px;             /* 从32px减小 */
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 1px;
}

.slogan {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin: 2px 0 4px;           /* 紧凑化 */
  letter-spacing: 1px;
}

.subtitle {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-bottom: 24px;          /* 从32px减小 */
}

/* 表单项间距压缩 */
.el-form-item {
  margin-bottom: 16px;          /* 从22px减小 */
}

/* 输入框样式优化 - 高度略减 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e4e7ed inset;
  border-radius: 12px;
  transition: box-shadow 0.2s, border-color 0.2s;
  padding: 2px 12px;            /* 上下内边距减小 */
  height: 38px;                 /* 固定稍小高度（原默认40px） */
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409eff inset;
}

/* 标签样式微调 - 字号略减 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #34495e;
  padding-bottom: 4px;          /* 减少间距 */
  font-size: 13px;              /* 从默认14px稍减 */
  line-height: 1.4;
}

/* 下拉框统一样式 */
:deep(.el-select) {
  width: 100%;
}
:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  height: 38px;
}

/* 注册按钮 - 高度减小 */
.register-button {
  width: 100%;
  height: 40px;                 /* 从48px减小 */
  font-size: 15px;              /* 略微减小 */
  font-weight: 500;
  margin-top: 12px;             /* 减小上边距 */
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
}
.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}
.register-button:active {
  transform: translateY(0);
}

/* 登录链接 - 紧凑 */
.login-link {
  text-align: center;
  margin-top: 20px;             /* 从24px减小 */
  font-size: 13px;              /* 略微减小 */
  color: #666;
}
.login-link .el-link {
  font-weight: 500;
  font-size: 13px;
}
</style>