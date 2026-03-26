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
          <el-icon>
            <HomeFilled />
          </el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 可视化看板菜单栏 -->
        <el-sub-menu v-if="isAdmin || isPark" index="dashboard">
          <template #title>
            <el-icon><DataBoard /></el-icon>
            <span>可视化看板</span>
          </template>
          <el-menu-item index="/dashboard/index">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/region">
            <el-icon><DataLine /></el-icon>
            <span>区域协同指数看板</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 数字化诊断菜单栏 -->
        <el-sub-menu v-if="isManufacture || isAdmin" index="diagnosis">
          <template #title>
            <el-icon>
              <Avatar />
            </el-icon>
            <span>数字化诊断</span>
          </template>
          <el-menu-item index="/diagnosis/questionnaire">
            <el-icon>
              <Tickets />
            </el-icon>
            <span>诊断问卷</span>
          </el-menu-item>
          <el-menu-item index="/diagnosis/report">
            <el-icon>
              <DataLine />
            </el-icon>
            <span>诊断报告</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 我的需求页面 -->
        <el-menu-item v-if="isManufacture || isAdmin" index="/match/my">
          <el-icon><DocumentAdd /></el-icon>
          <span>我的需求</span>
        </el-menu-item>

        <!-- 制造企业列表页面 -->
        <el-menu-item index="/manufacture/list">
          <el-icon>
            <OfficeBuilding />
          </el-icon>
          <span>制造企业列表</span>
        </el-menu-item>

        <!-- 服务企业列表页面 -->
        <el-menu-item index="/service/list">
          <el-icon>
            <OfficeBuilding />
          </el-icon>
          <span>服务企业列表</span>
        </el-menu-item>

        <!-- 出海服务菜单栏 -->
        <el-sub-menu v-if="isManufacture || isAdmin" index="abroad">
          <template #title>
            <el-icon><Van /></el-icon>
            <span>出海服务</span>
          </template>
          <el-menu-item index="/abroad/services">
            <el-icon>
              <OfficeBuilding />
            </el-icon>
            <span>出海服务商列表</span>
          </el-menu-item>
          <el-menu-item index="/abroad/cases">
            <el-icon><Checked /></el-icon>
            <span>出海成功案例</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 我的企业页面 -->
        <el-menu-item
          v-if="isAdmin || isManufacture || isService"
          index="/cooperation/my"
        >
          <el-icon><List /></el-icon>
          <span>我的合作</span>
        </el-menu-item>

        <el-menu-item
          v-if="isAdmin || isManufacture || isService"
          index="/enterprise"
        >
          <el-icon>
            <OfficeBuilding />
          </el-icon>
          <span>我的企业</span>
        </el-menu-item>

        <!-- 管理员菜单栏 -->
        <el-sub-menu v-if="isAdmin" index="admin">
          <template #title>
            <el-icon>
              <Avatar />
            </el-icon>
            <span>管理员</span>
          </template>
          <el-menu-item index="/admin/tag">
            <el-icon>
              <Collection />
            </el-icon>
            <span>标签管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/user">
            <el-icon><Avatar /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/demand">
            <el-icon><Avatar /></el-icon>
            <span>需求审核</span>
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
  DataBoard,
  DataAnalysis,
  Tickets,
  OfficeBuilding,
  List,
  DataLine,
  Van,
  Checked,
  DocumentAdd,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 计算当前激活菜单
const activeMenu = computed(() => route.path);

// 判断当前用户是否为管理员
const isAdmin = computed(() => userStore.userInfo?.role === "admin");
const isManufacture = computed(
  () => userStore.userInfo?.role === "manufacture",
);
const isService = computed(() => userStore.userInfo?.role === "service");
const isPark = computed(() => userStore.userInfo?.role === "park");

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
