<template>
  <div class="forbidden-container">
    <div class="content">
      <h1>403</h1>
      <h2>无权限访问</h2>
      <p>抱歉，您没有权限访问该页面，请确认您的账号角色或联系管理员。</p>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.forbidden-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f0f2f5;
  text-align: center;
}

.content h1 {
  font-size: 72px;
  color: #f56c6c;
  margin: 0;
  line-height: 1.2;
}

.content h2 {
  font-size: 24px;
  color: #606266;
  margin: 16px 0 8px;
  font-weight: 500;
}

.content p {
  color: #909399;
  margin-bottom: 24px;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>