<template>
  <el-container>
    <el-aside width="200px">
      <div style="padding:20px; color:#fff; background:#304156; height:100%;">侧边栏</div>
    </el-aside>
    <el-container>
      <el-header>
        <div style="line-height:60px; border-bottom:1px solid #eee;">头部</div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ElContainer, ElAside, ElHeader, ElMain } from 'element-plus'
</script>

<style scoped>
.el-aside { background-color: #304156; min-height: 100vh; }
.el-header { background-color: #fff; }
</style>