<template>
  <div style="max-width: 400px; margin: 100px auto">
    <h2>注册</h2>
    <el-form :model="form" label-width="80px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="form.confirmPassword" type="password" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleRegister">注册</el-button>
        <el-button @click="$router.push('/login')">返回登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'
import router from '@/router'

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const handleRegister = async () => {
  try {
    // 表单校验
    if (!form.username || !form.password || !form.confirmPassword) {
      ElMessage.warning('请填写完整信息')
      return
    }

    if (form.password !== form.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return
    }

    // 调用注册接口
    await register({
      username: form.username,
      password: form.password
    })

    ElMessage.success('注册成功')

    // 跳转登录页
    router.push('/login')

  } catch (err) {
    ElMessage.error(err.response?.data?.message || '注册失败')
  }
}
</script>
