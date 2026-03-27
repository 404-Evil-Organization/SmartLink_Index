export default [
  {
    path: "abroad/country",
    name: "AbroadCountry",
    component: () => import("@/views/abroad/CountryGuide.vue"),
  },
  {
    path: "abroad/services",
    name: "AbroadServiceList",
    component: () => import("@/views/abroad/ServiceList.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
  {
    path: "abroad/cases",
    name: "AbroadCases",
    component: () => import("@/views/abroad/Cases.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
];
