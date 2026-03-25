<template>
  <div class="abroad-case-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">出海案例管理</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理员</el-breadcrumb-item>
          <el-breadcrumb-item>出海案例管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openAddDialog" :icon="Plus">新增案例</el-button>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="目标国家">
              <el-input v-model="searchForm.country" placeholder="请输入国家名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="全部" clearable>
                <el-option label="发布" :value="1" />
                <el-option label="草稿" :value="0" />
              </el-select>
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
        <div class="table-title">出海案例列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe row-key="id" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="title" label="案例标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="companyName" label="企业名称" min-width="120" />
        <el-table-column prop="country" label="目标国家" width="120" />
        <el-table-column prop="serviceType" label="服务类型" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
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
        <el-form-item label="案例标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入案例标题" />
        </el-form-item>
        <el-form-item label="企业名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="企业类型" prop="companyType">
          <el-select v-model="form.companyType" placeholder="请选择" style="width: 100%">
            <el-option label="制造企业" value="manufacture" />
            <el-option label="服务商" value="service" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标国家" prop="country">
          <el-input v-model="form.country" placeholder="请输入目标国家" />
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-input v-model="form.serviceType" placeholder="请输入服务类型，如CE认证" />
        </el-form-item>
        <el-form-item label="案例详情" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入案例详情"
          />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            class="cover-upload"
            drag
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :file-list="fileList"
            list-type="picture"
            :limit="1"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 jpg/png 格式，大小不超过 2MB
              </div>
            </template>
          </el-upload>
          <div v-if="form.coverImage" class="cover-preview">
            <img :src="form.coverImage" style="max-height: 100px" />
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
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
import { Plus, Refresh, Edit, Delete, UploadFilled } from '@element-plus/icons-vue'
import { getAbroadCaseList, addAbroadCase, updateAbroadCase, deleteAbroadCase } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import { createTimeConverter } from '@/composables/date'

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  country: '',
  status: ''
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
  title: '',
  companyName: '',
  companyType: '',
  country: '',
  serviceType: '',
  description: '',
  coverImage: '',
  status: 1
})

const formRef = ref(null)

// 文件上传相关
const uploadUrl = '/api/common/upload'
const uploadHeaders = {
  Authorization: `Bearer ${userStore.token}`
}
const fileList = ref([])

// 表单校验规则
const rules = {
  title: [{ required: true, message: '请输入案例标题', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  companyType: [{ required: true, message: '请选择企业类型', trigger: 'change' }],
  country: [{ required: true, message: '请输入目标国家', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请输入服务类型', trigger: 'blur' }],
  description: [{ required: true, message: '请输入案例详情', trigger: 'blur' }]
}

// 日期格式化函数（兼容空格格式和 ISO 格式）
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
      ...(searchForm.country && { country: searchForm.country }),
      ...(searchForm.status !== '' && { status: searchForm.status })
    }
    const res = await getAbroadCaseList(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取案例列表失败', error)
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
  searchForm.status = ''
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
    title: '',
    companyName: '',
    companyType: '',
    country: '',
    serviceType: '',
    description: '',
    coverImage: '',
    status: 1
  })
  fileList.value = []
  dialog.isEdit = false
  dialog.editId = null
}

// 打开新增弹窗
const openAddDialog = () => {
  resetDialog()
  dialog.title = '新增案例'
  dialog.visible = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  resetDialog()
  dialog.title = '编辑案例'
  dialog.isEdit = true
  dialog.editId = row.id
  // 填充表单
  Object.assign(form, {
    title: row.title,
    companyName: row.companyName,
    companyType: row.companyType,
    country: row.country,
    serviceType: row.serviceType,
    description: row.description,
    coverImage: row.coverImage || '',
    status: row.status
  })
  if (row.coverImage) {
    fileList.value = [{ name: '封面', url: row.coverImage }]
  }
  dialog.visible = true
}

// 文件上传成功回调
const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    form.coverImage = response.data.fileUrl
    fileList.value = [{ name: file.name, url: response.data.fileUrl }]
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}
const handleUploadError = (error) => {
  console.error('上传失败', error)
  ElMessage.error('上传失败，请稍后重试')
}
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
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
      await updateAbroadCase(dialog.editId, data)
      ElMessage.success('修改成功')
    } else {
      await addAbroadCase(data)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    pagination.current = 1
    fetchList()
  } catch (error) {
    console.error('提交失败', error)
    // 错误提示由拦截器统一处理，不再重复
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除案例“${row.title}”吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAbroadCase(row.id)
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
.abroad-case-manage {
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

/* 封面上传样式 */
.cover-upload {
  width: 100%;
}
.cover-preview {
  margin-top: 12px;
  text-align: center;
}
.cover-preview img {
  max-width: 100%;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
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