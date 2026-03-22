<template>
  <div class="user-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户管理</h2>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理员</el-breadcrumb-item>
          <el-breadcrumb-item>用户管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="角色" >
          <el-select v-model="searchForm.role" placeholder="全部" clearable style="width: 400px">
            <el-option label="制造企业" value="manufacture" />
            <el-option label="服务商" value="service" />
            <el-option label="园区/政府" value="park" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" >
          <el-select v-model="searchForm.status" placeholder="全部"  clearable style="width: 400px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名" clearable style="width: 500px" />
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
        <div class="table-title">用户列表</div>
        <div class="table-actions">
          <el-tooltip content="刷新">
            <el-button :icon="Refresh" circle @click="fetchList" />
          </el-tooltip>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
              {{ getRoleName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 8px; flex-wrap: nowrap;">
            <el-button
              v-if="row.status === 1"
              size="small"
              type="danger"
              plain
              :icon="Close"
              @click="toggleStatus(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              size="small"
              type="success"
              plain
              :icon="Check"
              @click="toggleStatus(row)"
            >
              启用
            </el-button>
            <el-button
              size="small"
              type="warning"
              plain
              :icon="Key"
              @click="resetPassword(row)"
            >
              重置密码
            </el-button>
            <el-button
              size="small"
              @click="viewDetail(row)"
            >
              <el-icon><View /></el-icon> 查看
            </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 重置密码弹窗（增加复制按钮） -->
    <el-dialog v-model="passwordDialog.visible" title="重置密码" width="400px" @closed="clearPassword">
        <p>新密码：<strong>{{ passwordDialog.newPassword }}</strong></p>
        <p>请妥善保管，登录后请立即修改。</p>
        <template #footer>
          <el-button @click="passwordDialog.visible = false">关闭</el-button>
          <el-button type="primary" @click="copyPassword">复制密码</el-button>
        </template>
      </el-dialog>
    </div>
  </template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Check, Close, Key, View, Refresh } from "@element-plus/icons-vue";
import { getUserList, updateUserStatus, resetUserPassword } from "@/api/admin";

// 角色映射
const roleMap = {
  manufacture: "制造企业",
  service: "服务商",
  park: "园区/政府",
  admin: "管理员",
};
const getRoleName = (role) => roleMap[role] || role;

// 搜索表单
const searchForm = reactive({
  role: "",
  status: "",
  keyword: "",
});

// 表格数据
const tableData = ref([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

// 重置密码弹窗
const passwordDialog = reactive({
  visible: false,
  newPassword: ''
})

// 清空弹窗中的密码（降低内存残留风险）
const clearPassword = () => {
  passwordDialog.newPassword = ''
}

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...(searchForm.role && { role: searchForm.role }),
      // 同时排除空字符串/null/undefined，确保 0 等有效状态值可以正常传递
      ...(searchForm.status !== '' && searchForm.status !== null && searchForm.status !== undefined && { status: searchForm.status }),
      ...(searchForm.keyword && { keyword: searchForm.keyword })
    }
    const res = await getUserList(params)
    // 直接使用分页数据（拦截器已剥除外层 code）
    tableData.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (error) {
    // 兜底：清空数据，错误提示已由拦截器统一处理
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
  searchForm.role = ''
  searchForm.status = ''
  searchForm.keyword = ''
  handleSearch()
}

// 分页
const handlePageChange = (val) => {
  pagination.current = val
  fetchList()
}
const handlePageSizeChange = (val) => {
  pagination.size = val
  pagination.current = 1
  fetchList()
}

// 处理时间显示的函数，兼容不同格式的时间字符串，并且在无法解析时返回 '-'
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  // 将 "YYYY-MM-DD HH:mm:ss" 转换为 "YYYY-MM-DDTHH:mm:ss" 以便解析
  let normalized = dateStr
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(dateStr)) {
    normalized = dateStr.replace(' ', 'T')
  }
  const date = new Date(normalized)
  if (isNaN(date.getTime())) return '-'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 启用/禁用
const toggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定${action}用户 "${row.username}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updateUserStatus(row.id, newStatus)
      // 成功条件：响应为 null（代表操作成功且无返回数据）或响应包含 code 且为 200
      if (res === null || (res && (res.code === undefined || res.code === 200))) {
        ElMessage.success(`${action}成功`)
        fetchList()
      } else {
        // 非预期响应（如 res 有 code 且不是 200）
        console.error(`${action}失败，响应数据异常:`, res)
        ElMessage.error(`${action}失败，请稍后重试`)
      }
    } catch (error) {
      console.error(`${action}请求异常:`, error)
      // 判断是否为网络/超时错误（而非业务错误）
      const isNetworkError =
        error?.code === 'ECONNABORTED' ||
        error?.code === 'ERR_NETWORK' ||
        error?.message === 'Network Error' ||
        (error?.message && error.message.includes('timeout'))
      if (isNetworkError) {
        ElMessage.error('网络异常，请检查连接后重试')
      }
      // 业务错误由拦截器统一提示，此处不重复
    }
  }).catch(() => {}) // 用户取消确认，无需处理
}

// 重置密码
const resetPassword = (row) => {
  ElMessageBox.confirm(`确定重置用户 "${row.username}" 的密码吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await resetUserPassword(row.id)
      const newPassword = res?.newPassword || res?.data?.newPassword
      if (newPassword) {
        passwordDialog.newPassword = newPassword
        passwordDialog.visible = true
        ElMessage.success('密码重置成功')
      } else {
        // 接口成功但未返回新密码 → 提示异常
        console.error('重置密码失败，返回数据异常:', res)
        ElMessage.error('重置密码失败，响应数据异常，请稍后重试')
      }
    } catch (error) {
      // 拦截器已处理错误提示，这里只做日志记录和兜底（避免用户无反馈）
      console.error('重置密码请求异常:', error)
      // 如果拦截器未弹出错误（如自定义情况），可兜底提示

      // 判断是否为网络/超时等典型异常（而非业务错误）
      const isNetworkError =
        error?.code === 'ECONNABORTED' ||
        error?.code === 'ERR_NETWORK' ||
        error?.message === 'Network Error' ||
        (error?.message && error.message.includes('timeout'))

      // 仅在网络异常时兜底提示，业务错误由拦截器统一处理
      if (isNetworkError) {
        ElMessage.error('网络异常，请检查连接后重试')
      }
    }
  }).catch(() => {}) // 用户取消确认，无需处理
}

// 复制密码
const copyPassword = async () => {
  const text = passwordDialog.newPassword
  if (!text) {
    ElMessage.warning('没有可复制的密码')
    return
  }

  // 优先使用现代 Clipboard API
  if (navigator.clipboard && navigator.clipboard.writeText) {
    try {
      await navigator.clipboard.writeText(text)
      ElMessage.success('密码已复制到剪贴板')
    } catch (err) {
      console.error('Clipboard API 复制失败', err)
      fallbackCopyTextToClipboard(text)
    }
  } else {
    fallbackCopyTextToClipboard(text)
  }
}

// 降级方案（使用传统 execCommand）
const fallbackCopyTextToClipboard = (text) => {
  const textarea = document.createElement('textarea')
  textarea.value = text
  // 样式：完全不可见，不占位，不影响布局
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  textarea.style.top = '0'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)

  // 保存当前聚焦元素
  const activeElement = document.activeElement

  // 选中文本
  textarea.select()
  textarea.setSelectionRange(0, text.length) // 移动端兼容

  let success = false
  try {
    success = document.execCommand('copy')
  } catch (err) {
    console.error('降级复制失败', err)
  } finally {
    document.body.removeChild(textarea)
    // 恢复焦点
    if (activeElement && activeElement.focus) {
      activeElement.focus()
    }
  }

  if (success) {
    ElMessage.success('密码已复制到剪贴板')
  } else {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 查看详情（预留）
const viewDetail = (row) => {
  ElMessage.info(`查看用户 ${row.username} 详情功能开发中`)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.user-manage {
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

/* 搜索卡片样式（与标签管理一致） */
.search-bar {
  margin-bottom: 16px;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
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