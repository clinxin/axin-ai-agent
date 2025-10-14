import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import PlanApp from '../views/PlanApp.vue'
import ManusApp from '../views/ManusApp.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/plan-app',
    name: 'PlanApp',
    component: PlanApp
  },
  {
    path: '/manus-app',
    name: 'ManusApp',
    component: ManusApp
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
