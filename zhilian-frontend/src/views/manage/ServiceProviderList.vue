<template>
  <div class="service-provider-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">服务商管理</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>服务商管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd" :icon="Plus">
          新增服务商
        </el-button>
        <el-button :icon="Download">导出</el-button>
      </div>
    </div>

    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in statistics" :key="stat.label">
        <el-card class="stat-card" :body-style="{ padding: '20px' }" shadow="hover">
          <div class="stat-icon" :style="{ background: stat.bgColor }">
            <el-icon :size="24" :color="stat.color"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-trend" v-if="stat.trend">
              <span :class="stat.trend > 0 ? 'up' : 'down'">
                {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
              </span>
              较上月
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 高级搜索卡片（可折叠） -->
    <el-card class="search-card" shadow="hover">
      <div class="search-header" @click="toggleSearch">
        <span class="search-title">高级筛选</span>
        <el-icon :class="{ 'is-active': searchExpanded }">
          <ArrowDown />
        </el-icon>
      </div>
      <el-collapse-transition>
        <div v-show="searchExpanded">
          <el-form :model="searchForm" label-width="100px" class="search-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="服务商名称">
                  <el-input v-model="searchForm.companyName" placeholder="请输入" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="所在区域">
                  <el-select v-model="searchForm.region" placeholder="全部" clearable filterable>
                    <el-option label="深圳" value="深圳" />
                    <el-option label="东莞" value="东莞" />
                    <el-option label="惠州" value="惠州" />
                    <el-option label="广州" value="广州" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="服务类型">
                  <el-select v-model="searchForm.serviceType" placeholder="全部" clearable filterable>
                    <el-option label="检测认证" value="检测认证" />
                    <el-option label="工业设计" value="工业设计" />
                    <el-option label="物流" value="物流" />
                    <el-option label="翻译" value="翻译" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="状态">
                  <el-select v-model="searchForm.status" placeholder="全部" clearable>
                    <el-option label="启用" :value="1" />
                    <el-option label="禁用" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="评分区间">
                  <el-slider v-model="searchForm.ratingRange" range :min="0" :max="5" :step="0.5" />
                </el-form-item>
              </el-col>
              <el-col :span="8" class="search-actions">
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="hover">
      <div class="table-toolbar">
        <div class="table-title">服务商列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
          <el-tooltip content="密度">
            <el-button :icon="Grid" circle />
          </el-tooltip>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column type="expand" width="40">
          <template #default="{ row }">
            <div class="expanded-detail">
              <p><strong>详细地址：</strong>{{ row.address }}</p>
              <p><strong>邮箱：</strong>{{ row.email }}</p>
              <p><strong>简介：</strong>{{ row.description || '暂无简介' }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="companyName" label="服务商名称" min-width="150" />
        <el-table-column prop="region" label="区域" width="90" />
        <el-table-column prop="serviceType" label="服务类型" min-width="180">
          <template #default="{ row }">
            <el-tag
              v-for="tag in row.serviceType.split(',')"
              :key="tag"
              size="small"
              effect="plain"
              style="margin-right: 5px; margin-bottom: 3px;"
            >
              {{ tag.trim() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
            <el-button size="small" link @click="handleDetail(row)">
              详情
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增服务商' : '编辑服务商'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="服务商名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="所在区域" prop="region">
          <el-select v-model="form.region" placeholder="请选择" style="width:100%">
            <el-option label="深圳" value="深圳" />
            <el-option label="东莞" value="东莞" />
            <el-option label="惠州" value="惠州" />
            <el-option label="广州" value="广州" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-input v-model="form.serviceType" placeholder="多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Download,
  ArrowDown,
  Refresh,
  Grid,
  Edit,
  Delete,
  User,
  Star,
  Shop,
  TrendCharts
} from '@element-plus/icons-vue'

// ========== 导入真实 API 接口 ==========
import {
  getServiceProviderList,
  addServiceProvider,
  updateServiceProvider,
  deleteServiceProvider
} from '@/api/service-provider'

// ---------- 统计卡片（静态数据，后续可从接口获取） ----------
const statistics = ref([
  { icon: Shop, label: '服务商总数', value: 128, trend: 12, bgColor: '#ecf5ff', color: '#409eff' },
  { icon: User, label: '活跃服务商', value: 98, trend: 5, bgColor: '#f0f9eb', color: '#67c23a' },
  { icon: Star, label: '平均评分', value: 4.6, trend: 2, bgColor: '#fdf6ec', color: '#e6a23c' },
  { icon: TrendCharts, label: '服务需求', value: 56, trend: -3, bgColor: '#fef0f0', color: '#f56c6c' }
])

// ---------- 搜索表单 ----------
const searchExpanded = ref(true)
const searchForm = reactive({
  companyName: '',
  region: '',
  serviceType: '',
  status: null,
  ratingRange: [0, 5] // 评分区间，目前接口不支持，仅前端筛选或预留
})

// ---------- 表格数据 ----------
const tableData = ref([])
const loading = ref(false)

// ---------- 分页 ----------
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// ---------- 获取列表数据（调用真实API） ----------
const fetchList = async () => {
  loading.value = true
  try {
    // 构建接口参数（根据接口文档只传支持的字段）
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...(searchForm.companyName && { companyName: searchForm.companyName }),
      ...(searchForm.region && { region: searchForm.region }),
      ...(searchForm.serviceType && { serviceType: searchForm.serviceType })
      // status 和 ratingRange 如果接口不支持则暂不传递
    }
    const res = await getServiceProviderList(params)
    // 假设接口返回格式为 { total: 100, records: [...] }
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取服务商列表失败', error)
    ElMessage.error('获取列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 初始化加载
onMounted(() => {
  fetchList()
})

// ---------- 搜索与重置 ----------
const handleSearch = () => {
  pagination.current = 1
  fetchList()
}

const resetSearch = () => {
  searchForm.companyName = ''
  searchForm.region = ''
  searchForm.serviceType = ''
  searchForm.status = null
  searchForm.ratingRange = [0, 5]
  handleSearch()
}

const toggleSearch = () => {
  searchExpanded.value = !searchExpanded.value
}

// ---------- 分页 ----------
const handleSizeChange = (val) => {
  pagination.size = val
  fetchList()
}
const handleCurrentChange = (val) => {
  pagination.current = val
  fetchList()
}

// ---------- 状态切换（调用更新接口） ----------
const handleStatusChange = async (row, val) => {
  try {
    await updateServiceProvider(row.id, { status: val })
    ElMessage.success(`${row.companyName} 已${val === 1 ? '启用' : '禁用'}`)
    // 可选：刷新列表
    fetchList()
  } catch (error) {
    // 失败时回滚状态
    row.status = val === 1 ? 0 : 1
    ElMessage.error('操作失败')
  }
}

// ---------- 弹窗逻辑 ----------
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const form = reactive({
  id: null,
  companyName: '',
  region: '',
  serviceType: '',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: ''
})

const rules = {
  companyName: [{ required: true, message: '请输入服务商名称', trigger: 'blur' }],
  region: [{ required: true, message: '请选择区域', trigger: 'change' }],
  serviceType: [{ required: true, message: '请输入服务类型', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  // 如果表格数据完整可直接使用，否则可调用详情接口
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除服务商“${row.companyName}”吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteServiceProvider(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (dialogType.value === 'add') {
        await addServiceProvider(form)
        ElMessage.success('新增成功')
      } else {
        await updateServiceProvider(form.id, form)
        ElMessage.success('编辑成功')
      }
      dialogVisible.value = false
      fetchList()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
  form.id = null
  form.companyName = ''
  form.region = ''
  form.serviceType = ''
  form.contactPerson = ''
  form.contactPhone = ''
  form.email = ''
  form.address = ''
}

const handleDetail = (row) => {
  ElMessage.info(`查看详情 ${row.companyName}`)
}
</script>

<style scoped>
.service-provider-dashboard {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

/* 页面头部 */
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

/* 统计卡片 */
.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1f2f3d;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #8590a6;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #8590a6;
  margin-top: 6px;
}

.stat-trend .up {
  color: #f56c6c;
  font-weight: 500;
}

.stat-trend .down {
  color: #67c23a;
  font-weight: 500;
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  background-color: #fafbfc;
  border-bottom: 1px solid #ebeef5;
}

.search-title {
  font-weight: 600;
  color: #1f2f3d;
}

.search-header .el-icon {
  transition: transform 0.3s;
}

.search-header .el-icon.is-active {
  transform: rotate(180deg);
}

.search-form {
  padding: 20px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表格卡片 */
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

/* 展开详情 */
.expanded-detail {
  padding: 12px 40px;
  background-color: #fafbfc;
  font-size: 14px;
  color: #5e6d82;
  line-height: 1.8;
}

.expanded-detail p {
  margin: 0;
}

/* 表格内评分 */
.el-rate :deep(.el-rate__icon) {
  margin-right: 2px;
}

/* 分页容器 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}
</style>

