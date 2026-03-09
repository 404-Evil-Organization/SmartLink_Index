<template>
  <!-- 优化：统一全屏居中卡片风格，与登录页一致 -->
  <!-- 修改：移除 :logo-size 和 compact 属性，与登录页完全一致 -->
  <AuthCard>
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

      <!-- 角色选择 -->
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
          class="auth-button register-button"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form-item>

      <!-- 修改：统一跳转方式，使用方法调用而非 $router.push -->
      <div class="auth-link">
        已有账号？
        <el-link type="primary" @click="goToLogin">立即登录</el-link>
      </div>
    </el-form>
  </AuthCard>
</template>

<script setup>
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { register } from "@/api/auth";
import { useRouter } from "vue-router";
// 修改：移除未使用的 Connection 图标
import { User, Lock, Phone, Message } from "@element-plus/icons-vue";
import AuthCard from "@/components/AuthCard.vue";

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

// 修改：新增跳转登录页的方法
const goToLogin = () => {
  router.push('/login');
};

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
/* 修改：移除 !important，通过组合选择器提高权重覆盖基础样式 */
.auth-button.register-button {
  height: 48px;
  font-size: 16px;
  margin-top: 16px;
}
</style>