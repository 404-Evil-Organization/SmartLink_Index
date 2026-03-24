<template>
  <div class="region-index-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">区域数据管理</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理员</el-breadcrumb-item>
          <el-breadcrumb-item>区域数据管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openAddDialog" :icon="Plus">新增指数</el-button>
      </div>
    </div>

    <!-- 搜索卡片（包含区域、年份、周期类型、周期值筛选） -->
    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="5">
            <el-form-item label="区域">
              <el-input v-model="searchForm.region" placeholder="请输入区域" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="年份">
              <el-input v-model="searchForm.year" placeholder="请输入年份" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="周期类型">
              <el-select v-model="searchForm.periodType" placeholder="全部" clearable>
                <el-option label="季度" value="quarter" />
                <el-option label="月度" value="month" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="周期值">
              <el-select
                v-model="searchForm.periodValue"
                placeholder="全部"
                clearable
                :disabled="!searchForm.periodType"
              >
                <el-option
                  v-for="val in periodOptions"
                  :key="val"
                  :label="
                    searchForm.periodType === 'quarter'
                      ? '第' + val + '季度'
                      : val + '月'
                  "
                  :value="val"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6" style="text-align: right">
            <el-form-item label-width="0">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">区域指数列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe row-key="id" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="region" label="区域" min-width="100" />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column label="周期" width="100">
          <template #default="{ row }">
            {{ formatPeriod(row.periodType, row.periodValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="coopDensity" label="合作密度" min-width="100" />
        <el-table-column prop="serviceRate" label="服务渗透率" min-width="100" />
        <el-table-column prop="crossRate" label="跨域协同度" min-width="100" />
        <el-table-column prop="totalIndex" label="综合指数" width="100" />
        <el-table-column label="计算时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.calcTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="openEditDialog(row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      @closed="resetDialog"
    >
      <el-form :model="form" label-width="120px" ref="formRef" :rules="rules">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="区域" prop="region">
              <el-input v-model="form.region" placeholder="请输入区域" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年份" prop="year">
              <el-input-number v-model="form.year" :min="2000" :max="2030" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="周期类型" prop="periodType">
              <el-select v-model="form.periodType" placeholder="请选择" style="width: 100%">
                <el-option label="季度" value="quarter" />
                <el-option label="月" value="month" />
                <el-option label="年" value="year" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="周期值" prop="periodValue">
              <el-input-number v-model="form.periodValue" :min="1" :max="12" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合作密度" prop="coopDensity">
              <el-input-number v-model="form.coopDensity" :min="0" :max="1" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务渗透率" prop="serviceRate">
              <el-input-number v-model="form.serviceRate" :min="0" :max="1" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="跨域协同度" prop="crossRate">
              <el-input-number v-model="form.crossRate" :min="0" :max="1" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="综合指数" prop="totalIndex">
              <el-input-number v-model="form.totalIndex" :min="0" :max="100" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { getRegionIndexList, addRegionIndex, updateRegionIndex, deleteRegionIndex } from '@/api/regionIndex'
import { createTimeConverter } from '@/composables/date'

// 搜索表单
const searchForm = reactive({
  region: '',
  year: '',
  periodType: '',
  periodValue: ''
})

// 根据周期类型动态生成周期值选项
const periodOptions = computed(() => {
  if (searchForm.periodType === 'quarter') {
    return [1, 2, 3, 4]
  } else if (searchForm.periodType === 'month') {
    return Array.from({ length: 12 }, (_, i) => i + 1)
  } else {
    return []
  }
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

// 弹窗数据
const dialog = reactive({
  visible: false,
  title: '',
  isEdit: false,
  editId: null
})

const form = reactive({
  region: '',
  year: null,
  periodType: '',
  periodValue: null,
  coopDensity: null,
  serviceRate: null,
  crossRate: null,
  totalIndex: null
})

const formRef = ref(null)

// 表单校验规则
const rules = {
  region: [{ required: true, message: '请输入区域', trigger: 'blur' }],
  year: [{ required: true, message: '请输入年份', trigger: 'blur' }],
  periodType: [{ required: true, message: '请选择周期类型', trigger: 'change' }],
  periodValue: [{ required: true, message: '请输入周期值', trigger: 'blur' }],
  coopDensity: [{ required: true, message: '请输入合作密度', trigger: 'blur' }],
  serviceRate: [{ required: true, message: '请输入服务渗透率', trigger: 'blur' }],
  crossRate: [{ required: true, message: '请输入跨域协同度', trigger: 'blur' }],
  totalIndex: [{ required: true, message: '请输入综合指数', trigger: 'blur' }]
}

// 格式化周期显示
const formatPeriod = (type, value) => {
  if (!type || !value) return '-'
  const map = { quarter: '季度', month: '月', year: '年' }
  return `${value}${map[type] || ''}`
}

// 格式化日期时间
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
      ...(searchForm.region && { region: searchForm.region }),
      ...(searchForm.year && { year: searchForm.year }),
      ...(searchForm.periodType && { periodType: searchForm.periodType }),
      ...(searchForm.periodValue && { periodValue: searchForm.periodValue })
    }
    const res = await getRegionIndexList(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取列表失败', error)
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
  searchForm.region = ''
  searchForm.year = ''
  searchForm.periodType = ''
  searchForm.periodValue = ''
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

// 重置弹窗状态
const resetDialog = () => {
  formRef.value?.clearValidate()
  formRef.value?.resetFields()
  Object.assign(form, {
    region: '',
    year: null,
    periodType: '',
    periodValue: null,
    coopDensity: null,
    serviceRate: null,
    crossRate: null,
    totalIndex: null
  })
  dialog.isEdit = false
  dialog.editId = null
}

// 打开新增弹窗
const openAddDialog = () => {
  dialog.title = '新增指数'
  dialog.visible = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  dialog.title = '编辑指数'
  dialog.isEdit = true
  dialog.editId = row.id
  // 填充表单
  Object.assign(form, {
    region: row.region,
    year: row.year,
    periodType: row.periodType,
    periodValue: row.periodValue,
    coopDensity: row.coopDensity,
    serviceRate: row.serviceRate,
    crossRate: row.crossRate,
    totalIndex: row.totalIndex
  })
  dialog.visible = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  const data = { ...form }
  try {
    if (dialog.isEdit) {
      await updateRegionIndex(dialog.editId, data)
      ElMessage.success('修改成功')
    } else {
      await addRegionIndex(data)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    fetchList()
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除“${row.region} ${row.year}年”的指数数据吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRegionIndex(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.region-index-manage {
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

.header-right {
  display: flex;
  gap: 12px;
}

/* 搜索卡片样式 */
.search-bar {
  margin-bottom: 16px;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow-x: auto;  /* 窄屏滚动，保持一行 */
}

.search-form {
  width: 100%;
}

.search-form .el-row {
  flex-wrap: nowrap; /* 强制一行显示 */
  min-width: 1000px; /* 保证总宽度足够，可根据实际调整 */
}

.search-bar .el-col {
  margin-bottom: 0;
}

.search-bar .el-form-item {
  margin-bottom: 0;
  width: 100%;
}

.search-bar .el-button {
  margin-left: 8px;
}

.search-bar .el-button:first-child {
  margin-left: 0;
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

/* 操作列按钮在一行显示 */
.action-buttons {
  display: flex;
  gap: 8px;
  white-space: nowrap;
}

.action-buttons .el-button {
  margin: 0;
  padding: 0 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}

/* 响应式调整：小屏幕时搜索项垂直排列 */
@media (max-width: 768px) {
  .search-bar .el-row {
    flex-direction: column;
    align-items: stretch;
    min-width: auto;
  }
  .search-bar .el-col {
    margin-bottom: 12px;
  }
  .search-bar .el-button {
    margin-left: 0;
    margin-right: 8px;
  }
}
</style>