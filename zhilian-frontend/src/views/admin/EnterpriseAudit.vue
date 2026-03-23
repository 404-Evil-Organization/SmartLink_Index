<template>
  <div class="enterprise-audit">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">企业审核</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理员</el-breadcrumb-item>
          <el-breadcrumb-item>企业审核</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20" :wrap="false">
          <el-col :span="8">
            <el-form-item label="企业名称">
              <el-input v-model="searchForm.companyName" placeholder="请输入" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="企业类型">
              <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 100%">
                <el-option label="制造企业" value="manufacture" />
                <el-option label="服务商" value="service" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" style="display: flex; align-items: center;">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">待审核企业列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe row-key="id">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="companyName" label="企业名称" min-width="180" />
        <el-table-column label="企业类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'manufacture' ? 'primary' : 'success'" size="small">
              {{ row.type === 'manufacture' ? '制造企业' : '服务商' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" plain :icon="Check" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button size="small" type="danger" plain :icon="Close" @click="handleReject(row)">
              驳回
            </el-button>
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

    <!-- 通用审核弹窗（支持通过和驳回） -->
    <el-dialog
      v-model="auditDialog.visible"
      :title="auditDialog.title"
      width="500px"
    >
      <el-form ref="auditFormRef" :model="auditDialog" :rules="auditRules">
        <el-form-item label="审核意见" prop="comment">
          <el-input
            v-model="auditDialog.comment"
            type="textarea"
            :rows="3"
            :placeholder="auditDialog.placeholder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">{{ auditDialog.confirmText }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Check, Close } from '@element-plus/icons-vue'
import { getAuditList, auditEnterprise } from '@/api/admin'
import { createTimeConverter } from '@/composables/date'

// 搜索表单
const searchForm = reactive({
  companyName: '',
  type: ''
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

// 审核弹窗数据
const auditDialog = reactive({
  visible: false,
  mode: '',        // 'approve' 或 'reject'
  title: '',
  confirmText: '',
  placeholder: '',
  comment: '',
  currentRow: null
})

// 表单引用
const auditFormRef = ref(null)

// 动态校验规则（驳回时必填，通过时可选）
const auditRules = computed(() => ({
  comment: [
    {
      required: auditDialog.mode === 'reject',
      message: '请填写驳回原因',
      trigger: 'blur'
    }
  ]
}))

// 格式化日期
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
      ...(searchForm.companyName && { companyName: searchForm.companyName }),
      ...(searchForm.type && { type: searchForm.type })
    }
    const res = await getAuditList(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取审核列表失败', error)
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
  searchForm.companyName = ''
  searchForm.type = ''
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

// 打开审核弹窗
const openAuditDialog = (row, mode) => {
  auditDialog.currentRow = row
  auditDialog.mode = mode
  auditDialog.comment = ''
  if (mode === 'approve') {
    auditDialog.title = '审核通过'
    auditDialog.confirmText = '确认通过'
    auditDialog.placeholder = '请输入审核意见（可选）'
  } else {
    auditDialog.title = '驳回申请'
    auditDialog.confirmText = '确认驳回'
    auditDialog.placeholder = '请输入驳回原因（必填）'
  }
  auditDialog.visible = true
}

// 通过
const handleApprove = (row) => {
  openAuditDialog(row, 'approve')
}

// 驳回
const handleReject = (row) => {
  openAuditDialog(row, 'reject')
}

// 提交审核
const submitAudit = async () => {
  if (!auditFormRef.value) return
  try {
    await auditFormRef.value.validate()
  } catch (error) {
    return
  }

  const { currentRow, mode, comment } = auditDialog
  if (!currentRow) return
  const status = mode === 'approve' ? 'approved' : 'rejected'
  try {
    await auditEnterprise(currentRow.id, status, comment)
    ElMessage.success(mode === 'approve' ? '审核通过' : '已驳回')
    auditDialog.visible = false
    fetchList()
  } catch (error) {
    console.error('审核操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.enterprise-audit {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-header {
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

.search-bar {
  margin-bottom: 16px;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow-x: auto;
}

.search-form {
  width: 100%;
}

.search-form .el-row {
  flex-wrap: nowrap;
  min-width: 600px;
}

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
</style>