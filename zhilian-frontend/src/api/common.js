import request from "@/utils/request";

// 上传文件到 OSS
export function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);

  return request({
    url: "/common/upload",
    method: "post",
    headers: {
      "Content-Type": "multipart/form-data",
    },
    data: formData,
  }).then((res) => {
    return res.fileUrl;
  });
}

// 删除文件
export function deleteFile(fileUrl) {
  return request({
    url: "/common/delete",
    method: "post",
    data: { fileUrl },
  });
}
