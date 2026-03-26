export default [
  {
    path: "abroad/country",
    name: "AbroadCountry",
    component: () => import("@/views/abroad/CountryGuide.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
];
