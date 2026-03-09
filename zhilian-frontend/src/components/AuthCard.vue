<!-- 公共认证卡片组件，包含 Logo、标语和基础样式，供登录/注册页使用 -->
<template>
  <div class="auth-container">
    <el-card class="auth-card" :class="{ compact }">
      <!-- 新增 Logo 区域：统一品牌形象 -->
      <div class="logo-wrapper">
        <!-- 修改 Logo ：为特定图标 -->
        <img src="@/assets/logo.png" :style="{ height: logoSize + 'px', width: 'auto' }" alt="智链指数" />
        <span class="logo-text">智链指数</span>
      </div>

      <!-- 新增标语：丰富页面层次，传递产品理念 -->
      <p class="slogan">连接 · 握手 · 智造</p>
      <p class="subtitle">智能制造 · 协同服务</p>

      <!-- 插槽：放置具体表单内容（登录/注册表单） -->
      <slot></slot>
    </el-card>
  </div>
</template>

<script setup>
import { Connection } from "@element-plus/icons-vue";

defineProps({
  logoSize: {
    type: Number,
    default: 32  // ← 将 40 改为 32 ，更适合整体设计。
  },
  compact: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
/* 完全重写样式：全屏居中、渐变背景、卡片精致风格 */
.auth-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.auth-card {
  width: 420px;
  max-width: 94%;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  padding: 30px 32px 40px;
  background-color: #ffffff;
}

.auth-card.compact {
  padding: 24px 28px 32px;
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

.el-form-item {
  margin-bottom: 22px;
}

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

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #34495e;
  padding-bottom: 6px;
}

:deep(.el-select) {
  width: 100%;
}
:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  height: 38px;
}

:deep(.auth-button) {
  width: 100%;
  height: 40px;
  font-size: 15px;
  font-weight: 500;
  margin-top: 12px;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
}
:deep(.auth-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}
:deep(.auth-button:active) {
  transform: translateY(0);
}

:deep(.auth-link) {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #666;
}
:deep(.auth-link .el-link) {
  font-weight: 500;
  font-size: 13px;
}
</style>