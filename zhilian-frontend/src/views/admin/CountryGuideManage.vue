<template>
  <div class="country-guide-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">国家准入指南管理</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理员</el-breadcrumb-item>
          <el-breadcrumb-item>国家准入指南管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openAddDialog" :icon="Plus">新增指南</el-button>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="国家名称">
              <el-input v-model="searchForm.country" placeholder="请输入国家名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8" style="text-align: right">
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
        <div class="table-title">国家准入指南列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe row-key="id" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="country" label="国家" width="120" />
        <el-table-column prop="requirements" label="准入要求" min-width="200" show-overflow-tooltip />
        <el-table-column prop="process" label="办理流程" min-width="180" show-overflow-tooltip />
        <el-table-column label="所需文件" min-width="150">
          <template #default="{ row }">
            <div v-if="row.documents && row.documents.length">
              <el-tag v-for="(doc, idx) in row.documents" :key="idx" size="small" style="margin: 2px">
                {{ doc }}
              </el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ createTimeConverter(row.updateTime).toLocalYMDHMS() || '-' }}
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
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="国家名称" prop="country">
          <el-input v-model="form.country" placeholder="请输入国家名称" />
        </el-form-item>
        <el-form-item label="准入要求" prop="requirements">
          <el-input
            v-model="form.requirements"
            type="textarea"
            :rows="3"
            placeholder="请输入准入要求，如FCC认证、UL认证等"
          />
        </el-form-item>
        <el-form-item label="办理流程" prop="process">
          <el-input
            v-model="form.process"
            type="textarea"
            :rows="3"
            placeholder="请输入办理流程，如1.提交申请 2.测试 3.发证"
          />
        </el-form-item>
        <el-form-item label="所需文件" prop="documents">
          <el-select
            v-model="form.documents"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请输入所需文件，按回车添加"
            style="width: 100%"
          >
            <el-option
              v-for="doc in documentOptions"
              :key="doc"
              :label="doc"
              :value="doc"
            />
          </el-select>
          <div class="el-upload__tip">可输入自定义文件名称，按回车添加</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { getCountryGuideList, addCountryGuide, updateCountryGuide, deleteCountryGuide } from '@/api/admin'
import { createTimeConverter } from '@/composables/date'

// 搜索表单
const searchForm = reactive({
  country: ''
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
  country: '',
  requirements: '',
  process: '',
  documents: []      // 前端使用数组，提交时转为 JSON 字符串
})

const formRef = ref(null)

// 所需文件预设选项（仅用于展示，实际可任意输入）
const documentOptions = ref([
  '产品说明书',
  '电路图',
  '测试报告',
  '营业执照',
  '认证申请表'
])

// 表单校验规则
const rules = {
  country: [{ required: true, message: '请输入国家名称', trigger: 'blur' }],
  requirements: [{ required: true, message: '请输入准入要求', trigger: 'blur' }],
  process: [{ required: true, message: '请输入办理流程', trigger: 'blur' }]
}

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...(searchForm.country && { country: searchForm.country })
    }
    const res = await getCountryGuideList(params)
    // 将后端返回的 documents 字符串解析为数组
    const records = (res.records || []).map(item => ({
      ...item,
      documents: item.documents ? (Array.isArray(item.documents) ? item.documents : JSON.parse(item.documents)) : []
    }))
    tableData.value = records
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取指南列表失败', error)
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
  searchForm.country = ''
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
    country: '',
    requirements: '',
    process: '',
    documents: []
  })
  dialog.isEdit = false
  dialog.editId = null
}

// 打开新增弹窗
const openAddDialog = () => {
  resetDialog()
  dialog.title = '新增指南'
  dialog.visible = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  resetDialog()
  dialog.title = '编辑指南'
  dialog.isEdit = true
  dialog.editId = row.id
  // 将后端返回的 documents 字符串解析为数组
  const parsedDocs = row.documents
    ? (Array.isArray(row.documents) ? row.documents : JSON.parse(row.documents))
    : []
  Object.assign(form, {
    country: row.country,
    requirements: row.requirements,
    process: row.process,
    documents: parsedDocs
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

  // 将 documents 数组转为 JSON 字符串
  const data = {
    ...form,
    documents: JSON.stringify(form.documents)
  }
  try {
    if (dialog.isEdit) {
      await updateCountryGuide(dialog.editId, data)
      ElMessage.success('修改成功')
    } else {
      await addCountryGuide(data)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    pagination.current = 1
    fetchList()
  } catch (error) {
    console.error('提交失败', error)
    // 错误提示由拦截器统一处理
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除国家“${row.country}”的准入指南吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCountryGuide(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('删除失败', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.country-guide-manage {
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
  overflow-x: auto;
}

.search-form {
  width: 100%;
}

.search-form .el-row {
  flex-wrap: nowrap;
  min-width: 600px;
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