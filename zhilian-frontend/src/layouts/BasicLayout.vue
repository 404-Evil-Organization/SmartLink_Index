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

        <el-sub-menu index="1">
          <template #title>
            <el-icon><Avatar /></el-icon>
            <span>数字化诊断</span>
          </template>
          <el-menu-item index="/diagnosis/questionnaire">
            <el-icon><Tickets /></el-icon>
            <span>诊断问卷</span>
          </el-menu-item>

          <el-menu-item index="/diagnosis/report" @click="goToLatestReport">
          <el-icon><DataLine /></el-icon>
          <span>诊断报告</span>
        </el-menu-item>

        </el-sub-menu>
        <el-sub-menu v-if="isAdmin" index="2">
        <el-menu-item index="/manufacture/list">
          <el-icon><OfficeBuilding /></el-icon>
          <span>制造企业列表</span>
        </el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="isAdmin" index="1">
          <template #title>
            <el-icon><Avatar /></el-icon>
            <span>管理员</span>
          </template>
          <el-menu-item index="/admin/tag">
            <el-icon><Collection /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
        </el-sub-menu>
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
import {
  HomeFilled,
  Avatar,
  Collection,
  Tickets,
  OfficeBuilding,
  DataLine ,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 计算当前激活菜单
const activeMenu = computed(() => route.path);

// 判断当前用户是否为管理员
const isAdmin = computed(() => userStore.userInfo?.role === "admin");

// 跳转到最新诊断报告
const goToLatestReport = () => {
  const latestId = localStorage.getItem('latestDiagnosisId')
  if (latestId) {
    router.push(`/diagnosis/report/${latestId}`)
  } else {
    ElMessage.warning('暂无诊断报告，请先提交问卷')
  }
}
// 判断当前用户是否为制造企业
const isManufacture = computed(
  () => userStore.userInfo?.role === "manufacture",
);

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
