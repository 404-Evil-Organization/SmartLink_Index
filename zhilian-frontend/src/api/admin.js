import request from '@/utils/request'

// 获取待审核企业列表
export function getAuditList(params) {
  return request({
    url: '/admin/enterprise/audit/list',
    method: 'get',
    params
  })
}

// 审核企业（通过/驳回）
export function auditEnterprise(id, status, rejectReason = '') {
  return request({
    url: `/admin/enterprise/audit/${id}`,
    method: 'put',
    data: { status, rejectReason }
  })
}