<template>
  <el-container>
    <!-- 侧边栏 -->
    <el-aside width="200px">
      <div class="logo">智链指数</div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :router="true"
        :collapse="false"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/manage/manufacture">
          <el-icon><OfficeBuilding /></el-icon>
          <span>制造商管理页</span>
        </el-menu-item>
        <!-- 后续可继续添加其他菜单项 -->
      </el-menu>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container>
      <el-header>
        <div class="header-content">
          <span class="welcome"
            >欢迎，{{ userStore.userInfo?.username || "用户" }}</span
          >
          <el-button type="info" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import {
  ElContainer,
  ElAside,
  ElHeader,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElButton,
} from "element-plus";
import { HomeFilled, OfficeBuilding } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 计算当前激活菜单
const activeMenu = computed(() => route.path);

// 退出登录
const handleLogout = async () => {
  try {
    userStore.clearToken(); // 自定义方法清除 store 和 localStorage
    ElMessage.success("已退出登录");
    await router.push("/login");
  } catch (error) {
    ElMessage.error("退出失败");
    console.error("退出登录失败", error);
  }
};
</script>

<style scoped>
.el-aside {
  background-color: #304156;
  min-height: 98vh;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #1f2d3d;
}
.sidebar-menu {
  border-right: none;
}
.el-header {
  background-color: rgb(252, 252, 252);
  border-bottom: 1px solid #e6e9f0;
}
.header-content {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
  padding-right: 20px;
  gap: 15px;
}
.welcome {
  font-size: 14px;
  color: #606266;
}
</style>
