import { createRouter, createWebHistory } from "vue-router";

import "element-plus/dist/index.css";
// 导入组件
import LoginPage from "../components/Login.vue";
import UserPage from "../components/UserPage.vue";
import PatientManage from "../components/PatientManage.vue";
import PatientPage from "../components/PatientPage.vue"; // 确保这里是正确的文件名
import AdminPage from "../components/AdminPage.vue"; // 确保这里是正确的文件名

const routes = [
  {
    path: "/",
    name: "login",
    component: LoginPage,
  },
  {
    path: "/user",
    name: "user",
    component: UserPage,
  },
  {
    path: "/patient-manage",
    name: "PatientManage",
    component: PatientManage,
  },
  {
    path: "/patient/:id", // 修改这里，添加动态路由参数 :id
    name: "PatientPage",
    component: PatientPage,
    props: true, // 将路由参数作为 props 传递给组件
  },
  {
    path: "/admin",
    name: "AdminPage",
    component: AdminPage,
  },
  {
    path: "/bluetooth",
    name: "BlueTooth",
    component: () => import("@/components/BlueTooth.vue"),
  },
  // 移除重复的 patient-manage 路由
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
