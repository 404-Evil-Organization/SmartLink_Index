export default [
  {
    url: "/api/common/upload",
    method: "post",
    response: () => {
      return {
        code: 200,
        message: "success",
        data: {
          fileUrl:
            "https://smartlink-index.oss-cn-guangzhou.aliyuncs.com/avatar.png?Expires=1773302233&OSSAccessKeyId=TMP.3Ko3D4CEunQzVEaENPJkqpo2RPcm2AUkRCsHH8SFsBFSKTeTYWtqjZ1DZPrAZxfZkmfVZRHTJRJXVNBZJHS86ZZzG5zs7i&Signature=lFXGhCZ4RN7kp1Y60fdWnW0XjlA%3D",
        },
      };
    },
  },
  {
    url: "/api/common/delete",
    method: "post",
    response: () => {
      return {
        code: 200,
        message: "success",
        data: null,
      };
    },
  },
];
