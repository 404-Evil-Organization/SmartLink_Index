<template>
  <div style="max-width: 400px; margin: 100px auto">
    <h2>登录</h2>
    <el-form :model="form" label-width="80px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码">
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
//创建loading状态放置用户重复请求登录
const loginLoading = ref(false);

const form = reactive({
  username: "",
  password: "",
});

const handleLogin = async () => {
  if (loginLoading.value) return;

  if (!form.username || !form.password) {
    ElMessage.warning("请输入用户名和密码");
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
