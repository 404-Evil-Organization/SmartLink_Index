export default [
  {
    url: "/api/auth/login",
    method: "post",
    response: () => {
      return {
        code: 200,
        message: "success",
        data: {
          token: "eyJhbGciOiJIUzI1NiIs...",
        },
      };
    },
  },
  {
    url: "/api/auth/register",
    method: "post",
    response: () => {
      return {
        code: 200,
        message: "success",
        data: {
          userId: 1001,
          username: "tech_company",
          role: "admin",
        },
      };
    },
  },
  {
    url: "/api/auth/me",
    method: "get",
    response: () => {
      return {
        code: 200,
        message: "success",
        data: {
          id: 1001,
          username: "tech_company",
          role: "manufacture",
          phone: "13800138001",
          email: "test@example.com",
          status: 1,
          createTime: "2026-03-01 10:00:00",
        },
      };
    },
  },
];
