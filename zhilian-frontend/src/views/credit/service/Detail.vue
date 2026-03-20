<template>
  <div class="service-detail">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">服务商详情</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/service/list' }">服务企业列表</el-breadcrumb-item>
          <el-breadcrumb-item>服务商详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <el-row :gutter="20" class="top-row equal-height-row">
      <!-- 左侧：基本信息 -->
      <el-col :span="12" class="col-item">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header"><span>企业信息</span></div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="企业名称">{{ detailData.companyName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="区域">{{ detailData.region || '-' }}</el-descriptions-item>
            <el-descriptions-item label="详细地址">{{ detailData.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detailData.contactPerson || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="服务类型">{{ detailData.serviceType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="企业简介">{{ detailData.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="企业官网">{{ detailData.website || '-' }}</el-descriptions-item>
            <el-descriptions-item label="成立日期">{{ detailData.establishedDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="员工人数">{{ detailData.employeeCount || '-' }}</el-descriptions-item>
            <el-descriptions-item label="资质概述">{{ detailData.qualification || '-' }}</el-descriptions-item>
            <el-descriptions-item label="企业logo">
              <el-image v-if="detailData.logo" :src="detailData.logo" fit="cover" style="width: 100px; height: 100px; border-radius: 4px" />
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右侧：证书列表（支持分页） -->
      <el-col :span="12" class="col-item">
        <el-card class="cert-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>资质证书</span>
              <el-tooltip content="刷新"><el-button :icon="Refresh" circle size="small" @click="fetchCertList" /></el-tooltip>
            </div>
          </template>
          <el-table :data="certList" v-loading="certLoading" border stripe style="width: 100%">
            <el-table-column prop="certName" label="证书名称" min-width="150" />
            <el-table-column prop="certNo" label="证书编号" min-width="120" />
            <el-table-column prop="issueAuthority" label="发证机构" min-width="160" />
            <el-table-column prop="issueDate" label="发证日期" width="100" />
            <el-table-column prop="expireDate" label="有效期" width="100" />
            <el-table-column label="证书文件" width="70">
              <template #default="{ row }">
                <el-button
                    v-if="row.certFileUrl"
                    type="primary"
                    link
                    @click="openCertPreview(row.certFileUrl)"
                  >
                    查看
                  </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="certPage"
              v-model:page-size="certPageSize"
              :page-sizes="[5, 10, 20]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="certTotal"
              @size-change="handleCertSizeChange"
              @current-change="fetchCertList"
            />
          </div>
        </el-card>
        <el-image-viewer
          v-if="previewVisible"
          :url-list="[previewImage]"
          @close="previewVisible = false"
        />
      </el-col>
    </el-row>

    <!-- 下方：评价列表（支持分页） -->
    <el-row class="bottom-row">
      <el-col :span="24">
        <el-card class="evaluation-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户评价</span>
              <el-tooltip content="刷新"><el-button :icon="Refresh" circle size="small" @click="fetchEvalList" /></el-tooltip>
            </div>
          </template>
          <el-table :data="evalList" v-loading="evalLoading" border stripe>
            <el-table-column prop="manufactureName" label="评价企业" width="120" />
            <el-table-column prop="score" label="评分" width="200" align="center">
              <template #default="{ row }">
                <el-rate :model-value="row.score" disabled :texts="['1分', '2分', '3分', '4分', '5分']" show-text />
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" min-width="200" />
            <el-table-column prop="createTime" label="评价时间" width="160" />
            <el-table-column prop="isAnonymous" label="匿名" width="60" align="center">
              <template #default="{ row }">{{ row.isAnonymous ? '是' : '否' }}</template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="evalPage"
              v-model:page-size="evalPageSize"
              :page-sizes="[5, 10, 20]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="evalTotal"
              @size-change="handleEvalSizeChange"
              @current-change="fetchEvalList"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted,watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getServiceProviderDetail } from '@/api/service-provider'
import { getCertList } from '@/api/certification'
import { getEvaluationList } from '@/api/evaluation'

// 预览相关
const previewVisible = ref(false)
const previewImage = ref('')

const openCertPreview = (url) => {
  previewImage.value = url
  previewVisible.value = true
}

const route = useRoute()
const serviceId = ref(route.params.id)

// ---------- 基本信息 ----------
const detailData = ref({})
const fetchDetail = async () => {
  try {
    const res = await getServiceProviderDetail(serviceId.value )
    detailData.value = res
  } catch (error) {
    ElMessage.error('获取服务商详情失败')
  }
}

// ---------- 证书列表（分页） ----------
const certList = ref([])
const certLoading = ref(false)
const certPage = ref(1)
const certPageSize = ref(5)
const certTotal = ref(0)

// 修正 fetchCertList
const fetchCertList = async () => {
  if (!serviceId) return;
  certLoading.value = true;
  try {
    const res = await getCertList({
      serviceId,
      page: certPage.value,
      size: certPageSize.value
    });
    certList.value = res.records || [];
    certTotal.value = res.total || 0;
  } catch (error) {
    ElMessage.error('获取证书列表失败');
  } finally {
    certLoading.value = false;
  }
};

// 证书分页 size 变化处理
const handleCertSizeChange = (size) => {
  certPage.value = 1
  certPageSize.value = size
  fetchCertList()
}

// ---------- 评价列表（分页） ----------
const evalList = ref([])
const evalLoading = ref(false)
const evalPage = ref(1)
const evalPageSize = ref(5)
const evalTotal = ref(0)


const fetchEvalList = async () => {
  if (!serviceId) return
  evalLoading.value = true
  try {
    const res = await getEvaluationList(serviceId, {
      page: evalPage.value,
      size: evalPageSize.value
    })
    evalList.value = res.records || []
    evalTotal.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取评价列表失败')
  } finally {
    evalLoading.value = false
  }
}

// 评价分页 size 变化处理
const handleEvalSizeChange = (size) => {
  evalPage.value = 1
  evalPageSize.value = size
  fetchEvalList()
}

// 监听路由参数变化，重新加载数据并重置分页
watch(() => route.params.id, (newId) => {
  if (!newId) return
  serviceId.value = newId
  // 重置分页
  certPage.value = 1
  evalPage.value = 1
  // 重新获取数据
  fetchDetail()
  fetchCertList()
  fetchEvalList()
}, { immediate: true })

// onMounted(() => {
//   fetchDetail()
//   fetchCertList()
//   fetchEvalList()
// })
</script>

<style scoped>
/* 样式保持不变（原样复制） */
.service-detail {
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
.top-row {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #1f2f3d;
}
.info-card :deep(.el-descriptions__label) {
  width: 100px;
  background-color: #f5f7fa;
  text-align: right;
  font-weight: 600;
  color: #1f2f3d;
}
.info-card :deep(.el-descriptions__content) {
  padding: 12px 16px;
  word-break: break-word;
}
.cert-card,
.evaluation-card,
.info-card {
  width: 100%;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.el-table :deep(.el-rate) {
  display: inline-block;
}
.el-rate {
  display: inline-flex;
  gap: 2px;
}
.el-rate .el-rate__item {
  margin-right: 2px;
}
.equal-height-row {
  display: flex;
  align-items: stretch;
}
.equal-height-row .col-item {
  display: flex;
  flex-direction: column;
}
.equal-height-row .el-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.equal-height-row .el-card__body {
  flex: 1;
}
.el-table .el-table__cell .el-rate {
  white-space: nowrap;
  display: inline-flex;
  flex-wrap: nowrap;
  align-items: center;
}
.el-table .el-rate__text {
  margin-left: 4px;
  white-space: nowrap;
}
/* 等高布局 */
.equal-height-row {
  display: flex;
  align-items: stretch;    /* 让子元素拉伸到相同高度 */
}

.equal-height-row .col-item {
  display: flex;
  flex-direction: column;
}

.equal-height-row .el-card {
  flex: 1;                /* 卡片自动填充剩余高度 */
  display: flex;
  flex-direction: column;
}

.equal-height-row .el-card__body {
  flex: 1;                /* 卡片内容区域也撑满，让内部内容可以正常滚动或布局 */
}
.cert-card .el-table .cell {
  word-break: break-word;
  white-space: normal;
}
</style>