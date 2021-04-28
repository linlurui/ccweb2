import Vue from 'vue';
import Router, {
  RouteConfig
} from 'vue-router';;
Vue.use(Router);
export const routes: RouteConfig[] = [{
    component: () => import('@/pages/home/home.vue'),
    name: 'home',
    path: '/home',
    children: [{
      component: () => import('@/pages/home/workspace/index.vue'),
      name: 'home-workspace',
      path: 'workspace',
    }, ],
  },
  {
    path: '/',
    redirect: '/home'
  },
];
const router = new Router({
  mode: 'hash',
  routes,
});
export default router;