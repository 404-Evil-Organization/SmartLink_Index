<template>
  <div style="max-width: 400px; margin: 100px auto">
    <h2>登录</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loginLoading" @click="handleLogin"
          >登录</el-button
        >
        <el-button @click="$router.push('/register')">去注册</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login } from "@/api/auth";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);
//创建loading状态放置用户重复请求登录
const loginLoading = ref(false);

const form = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
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
    router.push("/"); // 跳转到首页
  } catch (error) {
    console.error("登录失败", error);
  } finally {
    loginLoading.value = false;
  }
};
</script>
