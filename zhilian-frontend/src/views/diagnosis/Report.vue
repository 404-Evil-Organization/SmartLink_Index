<template>
  <div class="diagnosis-report">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">数字化诊断报告</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/diagnosis/questionnaire' }">数字化诊断</el-breadcrumb-item>
          <el-breadcrumb-item>诊断报告</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 企业选择卡片 -->
    <el-card class="enterprise-card" shadow="hover" v-if="enterpriseOptions.length > 0">
      <div class="enterprise-selector">
        <span class="label">选择企业：</span>
        <el-select
          v-model="selectedManuId"
          placeholder="请选择企业"
          style="width: 300px"
          :loading="loadingEnterprises"
          @change="handleEnterpriseChange"
        >
          <el-option
            v-for="item in enterpriseOptions"
            :key="item.id"
            :label="item.companyName"
            :value="item.id"
          />
        </el-select>
        <el-button type="primary" :loading="viewLoading" @click="handleViewReport" style="margin-left: 16px;">
          查看报告
        </el-button>
        <span v-if="enterpriseOptions.length === 1 && !selectedManuId" class="tip">（当前默认企业）</span>
      </div>
    </el-card>

    <!-- 无企业提示 + 占位跳转按钮 -->
    <el-card class="enterprise-card" shadow="hover" v-else-if="!loadingEnterprises">
      <el-alert
        title="您尚未创建制造企业，请先创建企业"
        type="warning"
        :closable="false"
        show-icon
      />
      <el-button type="primary" size="small" @click="goToEnterpriseManage" style="margin-top: 12px;">
        前往创建
      </el-button>
    </el-card>

    <!-- 报告卡片 -->
    <el-card class="report-card" shadow="hover" v-loading="loadingReport">
      <div class="report-content" v-if="reportData">
        <!-- 基本信息 -->
        <div class="basic-info">
          <div class="info-item">
            <span class="label">诊断企业：</span>
            <span class="value">{{ reportData.manuName || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="label">诊断时间：</span>
            <span class="value">{{ reportData.diagnosisDate || '-' }}</span>
          </div>
        </div>

        <!-- 综合得分与等级 -->
        <div class="score-section">
          <div class="total-score">
            <span class="score-value">{{ reportData.totalScore }}</span>
            <span class="score-unit">分</span>
            <div class="score-label">综合得分</div>
          </div>
          <div class="level-tag">
            <el-tag :type="getLevelType(reportData.level)" size="large" effect="dark">
              {{ reportData.level }}
            </el-tag>
          </div>
        </div>

        <!-- 雷达图 -->
        <div class="radar-container">
          <div ref="radarChartRef" style="height: 300px; width: 100%;"></div>
        </div>

        <!-- 各维度得分 -->
        <div class="dimension-scores">
          <el-row :gutter="20">
            <el-col :span="6" v-for="dim in dimensions" :key="dim.name">
              <div class="dimension-item">
                <div class="dimension-label">{{ dim.label }}</div>
                <div class="dimension-value">{{ reportData[dim.field] || 0 }} / 5</div>
                <el-progress :percentage="(reportData[dim.field] || 0) * 20" :show-text="false" status="success" />
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 改进建议 -->
        <div class="suggestions">
          <h3>改进建议</h3>
          <ul>
            <li v-for="(item, index) in reportData.suggestions" :key="index">{{ item }}</li>
          </ul>
        </div>
      </div>

      <!-- 无报告时的空状态 -->
      <div v-else-if="!loadingReport && showNoReport" class="no-report">
        <el-empty description="该企业暂无诊断报告">
          <el-button type="primary" @click="goToQuestionnaire">
            前往诊断问卷
          </el-button>
        </el-empty>
      </div>

      <!-- 初始未选择企业或等待时的占位 -->
      <div v-else-if="!loadingReport && enterpriseOptions.length > 0" class="no-report">
        <el-empty description="请先选择企业并点击查看报告" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDiagnosisResult, getLatestDiagnosis } from '@/api/diagnosis'
// TODO: 后续替换为个人制造企业列表接口
import { getManufactureList } from '@/api/manufacture'

const route = useRoute()
const router = useRouter()
const loadingReport = ref(false)
const reportData = ref(null)
const radarChartRef = ref(null)
const viewLoading = ref(false)
const showNoReport = ref(false)

// ---------- 企业列表 ----------
const enterprises = ref([])
const loadingEnterprises = ref(false)
const selectedManuId = ref(null)

// 获取企业列表（占位用公共接口）
const fetchEnterprises = async () => {
  loadingEnterprises.value = true
  try {
    // TODO: 替换为 getMyManufactureList()
    const res = await getManufactureList({ page: 1, size: 100 })
    enterprises.value = (res.records || []).filter(item => item.auditStatus === 'approved')
    if (enterprises.value.length === 1 && !selectedManuId.value) {
      selectedManuId.value = enterprises.value[0].id
    }
  } catch (error) {
    console.error('获取企业列表失败', error)
  } finally {
    loadingEnterprises.value = false
  }
}

const enterpriseOptions = computed(() => enterprises.value)

// 切换企业时的处理（用户手动选择）
const handleEnterpriseChange = (manuId) => {
  // 只更新选中值，不自动加载报告
}

// ---------- 报告详情 ----------
const dimensions = [
  { name: 'info', label: '信息化水平', field: 'infoScore' },
  { name: 'auto', label: '自动化水平', field: 'autoScore' },
  { name: 'data', label: '数据应用', field: 'dataScore' },
  { name: 'service', label: '服务协同', field: 'serviceScore' },
]

const getLevelType = (level) => {
  const map = {
    '起步期': 'info',
    '成长期': 'warning',
    '成熟期': 'success',
    '引领期': 'danger',
  }
  return map[level] || 'info'
}

// 处理报告错误
const handleReportError = (error) => {
  if (error.response?.status === 403) {
    router.push('/403')
  } else if (error.response?.status === 404) {
    reportData.value = null
    showNoReport.value = true
  } else {
    ElMessage.error('获取报告失败，请稍后重试')
    reportData.value = null
  }
}

// 根据诊断ID获取报告
const fetchReportById = async (id) => {
  if (!id) return
  loadingReport.value = true
  showNoReport.value = false
  try {
    const res = await getDiagnosisResult(id)
    reportData.value = res
    if (res.manuId) {
      selectedManuId.value = res.manuId
    }
    localStorage.setItem('latestDiagnosisId', id)
    nextTick(() => renderRadarChart())
  } catch (error) {
    handleReportError(error)
  } finally {
    loadingReport.value = false
  }
}

// 根据企业ID获取最新报告
const fetchLatestReportByManuId = async (manuId) => {
  if (!manuId) return
  loadingReport.value = true
  showNoReport.value = false
  try {
    const res = await getLatestDiagnosis(manuId)
    reportData.value = res
    if (res.manuId) {
      selectedManuId.value = res.manuId
    }
    localStorage.setItem('latestDiagnosisId', res.diagnosisId)
    nextTick(() => renderRadarChart())
  } catch (error) {
    handleReportError(error)
  } finally {
    loadingReport.value = false
  }
}

// 根据路由加载报告（统一使用 query 参数）
const loadReport = () => {
  const id = route.query.id
  const manuId = route.query.manuId

  if (id) {
    fetchReportById(id)
  } else if (manuId) {
    fetchLatestReportByManuId(manuId)
  } else {
    reportData.value = null
  }
}

// 查看报告按钮点击事件
const handleViewReport = async () => {
  if (!selectedManuId.value) {
    ElMessage.warning('请先选择企业')
    return
  }
  viewLoading.value = true
  try {
    const latest = await getLatestDiagnosis(selectedManuId.value)
    if (latest && latest.diagnosisId) {
      // 使用 query 方式跳转，携带诊断ID
      router.push(`/diagnosis/report?id=${latest.diagnosisId}`)
    } else {
      // 无报告，显示空状态（但这里会被 catch 捕获404，所以通常不会执行到这里）
      reportData.value = null
      showNoReport.value = true
    }
  } catch (error) {
    if (error.response?.status === 404) {
      // 无最新报告，显示空状态
      reportData.value = null
      showNoReport.value = true
    } else {
      ElMessage.error('获取报告失败，请稍后重试')
    }
  } finally {
    viewLoading.value = false
  }
}

// 前往诊断问卷
const goToQuestionnaire = () => {
  if (selectedManuId.value) {
    router.push(`/diagnosis/questionnaire?manuId=${selectedManuId.value}`)
  } else {
    router.push('/diagnosis/questionnaire')
  }
}

// 前往企业管理页面（占位）
const goToEnterpriseManage = () => {
  // TODO: 替换为真实的企业管理页面路由
  ElMessage.info('企业管理页面开发中，即将跳转')
  // router.push('/enterprise')
}

// 渲染雷达图
const renderRadarChart = () => {
  if (!radarChartRef.value || !reportData.value) return
  const chart = echarts.init(radarChartRef.value)
  const indicator = dimensions.map(d => ({ name: d.label, max: 5 }))
  const value = dimensions.map(d => reportData.value[d.field] || 0)
  const option = {
    radar: { indicator, center: ['50%', '50%'], radius: '65%' },
    series: [{
      type: 'radar',
      data: [value],
      areaStyle: { color: 'rgba(64,158,255,0.2)' },
      lineStyle: { color: '#409EFF', width: 2 },
      itemStyle: { color: '#409EFF' },
    }],
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

// 监听路由参数变化（只监听 query 参数）
watch(() => [route.query.id, route.query.manuId], () => {
  loadReport()
}, { immediate: true })

onMounted(() => {
  fetchEnterprises()
})
</script>

<style scoped>
.diagnosis-report {
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

.enterprise-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.enterprise-selector {
  display: flex;
  align-items: center;
  padding: 12px 20px;
}

.enterprise-selector .label {
  margin-right: 16px;
  font-weight: 500;
  color: #303133;
}

.enterprise-selector .tip {
  margin-left: 12px;
  font-size: 14px;
  color: #909399;
}

.report-card {
  border-radius: 12px;
  overflow: hidden;
}

.report-content {
  padding: 20px;
}

.basic-info {
  display: flex;
  gap: 40px;
  margin-bottom: 30px;
  background-color: #f9f9f9;
  padding: 16px 20px;
  border-radius: 8px;
}

.info-item .label {
  color: #909399;
  margin-right: 8px;
}

.info-item .value {
  font-weight: 500;
  color: #303133;
}

.score-section {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 30px;
  padding: 0 16px;
}

.total-score {
  text-align: center;
}

.score-value {
  font-size: 48px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
}

.score-unit {
  font-size: 16px;
  color: #909399;
  margin-left: 4px;
}

.score-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.level-tag .el-tag {
  font-size: 24px;
  padding: 12px 24px;
  border-radius: 40px;
}

.radar-container {
  margin: 30px 0;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  background-color: #ffffff;
}

.dimension-scores {
  margin: 30px 0;
}

.dimension-item {
  text-align: center;
  background-color: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
}

.dimension-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.dimension-value {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 10px;
}

.suggestions {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px 30px;
  margin-top: 30px;
}

.suggestions h3 {
  margin: 0 0 16px 0;
  color: #1f2f3d;
  font-weight: 600;
}

.suggestions ul {
  margin: 0;
  padding-left: 20px;
}

.suggestions li {
  margin: 8px 0;
  color: #606266;
  font-size: 15px;
}

.no-report {
  padding: 40px 0;
}
</style>