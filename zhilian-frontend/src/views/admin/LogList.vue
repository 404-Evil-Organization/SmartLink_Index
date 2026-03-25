<template>
  <div class="log-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">操作日志</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理员</el-breadcrumb-item>
          <el-breadcrumb-item>操作日志</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" class="search-form" inline>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operation" placeholder="全部" clearable style="width: 120px">
            <el-option label="用户登录" value="用户登录" />
            <el-option label="修改密码" value="修改密码" />
            <el-option label="审核需求" value="审核需求" />
            <el-option label="审核企业" value="审核企业" />
            <el-option label="新增标签" value="新增标签" />
            <el-option label="修改标签" value="修改标签" />
            <el-option label="删除标签" value="删除标签" />
            <el-option label="新增指数" value="新增指数" />
            <el-option label="修改指数" value="修改指数" />
            <el-option label="删除指数" value="删除指数" />
            <el-option label="重置密码" value="重置密码" />
            <el-option label="启用用户" value="启用用户" />
            <el-option label="禁用用户" value="禁用用户" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">操作日志列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe row-key="id" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="username" label="操作人" width="150" />
        <el-table-column prop="operation" label="操作类型" width="140" />
        <el-table-column prop="params" label="请求参数" min-width="180" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" width="80" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column label="操作时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getLogList } from '@/api/admin'
import { createTimeConverter } from '@/composables/date'

// 搜索表单
const searchForm = reactive({
  username: '',
  operation: '',
  dateRange: []  // [startDate, endDate]
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 日期格式化
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  let normalized = dateStr
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(dateStr)) {
    normalized = dateStr.replace(' ', 'T')
  }
  const converter = createTimeConverter(normalized)
  const date = converter.toDate()
  if (!date) return '-'
  return converter.toLocalYMDHMS()
}

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...(searchForm.username && { username: searchForm.username }),
      ...(searchForm.operation && { operation: searchForm.operation }),
      ...(searchForm.dateRange && searchForm.dateRange.length === 2 && {
        startTime: searchForm.dateRange[0],
        endTime: searchForm.dateRange[1]
      })
    }
    const res = await getLogList(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取日志列表失败', error)
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索与重置
const handleSearch = () => {
  pagination.current = 1
  fetchList()
}
const resetSearch = () => {
  searchForm.username = ''
  searchForm.operation = ''
  searchForm.dateRange = []
  handleSearch()
}

// 分页
const handleSizeChange = (val) => {
  pagination.size = val
  pagination.current = 1
  fetchList()
}
const handleCurrentChange = (val) => {
  pagination.current = val
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.log-list {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1f2f3d;
  line-height: 1.2;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  font-weight: 400;
  color: #8590a6;
}

/* 搜索卡片样式 */
.search-bar {
  margin-bottom: 16px;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow-x: auto;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 16px;
}

.search-form .el-form-item {
  margin-bottom: 0;
}

/* 表格卡片样式 */
.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.table-title {
  font-weight: 600;
  color: #1f2f3d;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}

/* 响应式调整：小屏幕时搜索项换行 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  .search-form .el-form-item {
    margin-bottom: 12px;
  }
  .search-form .el-button {
    margin-left: 0;
    margin-right: 8px;
  }
}
</style>