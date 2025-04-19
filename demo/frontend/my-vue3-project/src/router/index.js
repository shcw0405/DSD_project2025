import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from '../components/Login.vue';
import UserPage from '../components/UserPage.vue';
import PatientManage from '../components/PatientManage.vue';

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginPage,
  },
  {
    path: '/user',
    name: 'user',
    component: UserPage,
  },
  {
    path: '/patient-manage',
    name: 'patient-manage',
    component: PatientManage,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
