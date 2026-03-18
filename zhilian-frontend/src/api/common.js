import request from "@/utils/request";

/**
 * 上传文件到OSS
 * @param {File} file 要上传的文件对象
 * @returns {Promise<string>} 上传成功后的文件URL
 */
export function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    url: "/common/upload",
    method: "post",
    data: formData,
    headers: { "Content-Type": "multipart/form-data" },
  }).then((res) => res.fileUrl);
}

/**
 * 删除OSS文件
 * @param {string} fileUrl 要删除的文件的完整URL
 * @returns {Promise<null>} 无返回数据
 */
export function deleteFile(fileUrl) {
  return request({
    url: "/common/delete",
    method: "post",
    data: { fileUrl },
  });
}

/**
 * 获取区域列表
 * @returns {Promise<Array<string>>} 区域名称数组
 */
export function getRegions() {
  return request({
    url: "/common/regions",
    method: "get",
  });
}

/**
 * 获取企业规模枚举
 * @returns {Promise<Array<{value: string, label: string}>>} 规模选项列表
 */
export function getScales() {
  return request({
    url: "/common/scales",
    method: "get",
  });
}

/**
 * 获取服务类型标签
 * @returns {Promise<Array<{id: number, name: string, category: string}>>} 服务类型标签列表
 */
export function getServiceTags() {
  return request({
    url: "/common/service-tags",
    method: "get",
  });
}

/**
 * 获取认证类型标签
 * @returns {Promise<Array<{id: number, name: string, category: string}>>} 认证类型标签列表
 */
export function getCertificationTags() {
  return request({
    url: "/common/certification-tags",
    method: "get",
  });
}

/**
 * 获取产品类型标签
 * @returns {Promise<Array<{id: number, name: string, category: string}>>} 产品类型标签列表
 */
export function getProductTags() {
  return request({
    url: "/common/product-tags",
    method: "get",
  });
}

/**
 * 获取其他类型标签
 * @returns {Promise<Array<{id: number, name: string, category: string}>>} 其他类型标签列表
 */
export function getRestsTags() {
  return request({
    url: "/common/rests-tags",
    method: "get",
  });
}

/**
 * 获取标签类别选项
 * @returns {Promise<Array<{value: string, label: string}>>} 标签类别下拉选项
 */
export function getTagCategories() {
  return request({
    url: "/common/tag-categories",
    method: "get",
  });
}
