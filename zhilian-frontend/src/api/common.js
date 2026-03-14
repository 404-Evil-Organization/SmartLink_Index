import request from '@/utils/request'

/**
 * 上传文件
 * @param {File} file 文件对象
 * @returns {Promise<string>} 返回文件URL（拦截器已提取 data 字段）
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 * @param {string} fileUrl 文件URL
 * @returns {Promise}
 */
export function deleteFile(fileUrl) {
  return request({
    url: '/delete',
    method: 'post',
    data: { url: fileUrl }
  })
}

/**
 * 获取区域列表
 * @returns {Promise<Array<string>>} 返回区域名称数组
 */
export function getRegionList() {
  return request({
    url: '/common/regions',
    method: 'get'
  })
}

/**
 * 获取服务类型标签
 * @returns {Promise<Array<{id: number, name: string, category: string}>>} 返回服务类型数组
 */
export function getServiceTagList() {
  return request({
    url: '/common/service-tags',
    method: 'get'
  })
}