export default [
  {
    url: "/api/common/upload",
    method: "post",
    response: () => {
      return {
        code: 200,
        message: "success",
        data: {
          fileUrl: "https://oss.example.com/path/to/file.jpg",
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
