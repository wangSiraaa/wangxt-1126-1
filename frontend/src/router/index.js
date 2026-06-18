import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/dispatcher',
    name: 'DispatcherLayout',
    component: () => import('@/layout/DispatcherLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'DispatcherDashboard',
        component: () => import('@/views/dispatcher/Dashboard.vue')
      },
      {
        path: 'station',
        name: 'StationManage',
        component: () => import('@/views/dispatcher/StationManage.vue')
      },
      {
        path: 'route',
        name: 'RouteManage',
        component: () => import('@/views/dispatcher/RouteManage.vue')
      },
      {
        path: 'route/create',
        name: 'RouteCreate',
        component: () => import('@/views/dispatcher/RouteCreate.vue')
      },
      {
        path: 'route/detail/:id',
        name: 'RouteDetail',
        component: () => import('@/views/dispatcher/RouteDetail.vue')
      },
      {
        path: 'exception',
        name: 'ExceptionManage',
        component: () => import('@/views/dispatcher/ExceptionManage.vue')
      },
      {
        path: 'repair',
        name: 'RepairManage',
        component: () => import('@/views/dispatcher/RepairManage.vue')
      },
      {
        path: 'complaint',
        name: 'ComplaintManage',
        component: () => import('@/views/dispatcher/ComplaintManage.vue')
      },
      {
        path: 'complaint/detail/:id',
        name: 'ComplaintDetail',
        component: () => import('@/views/dispatcher/ComplaintDetail.vue')
      }
    ]
  },
  {
    path: '/inspector',
    name: 'InspectorLayout',
    component: () => import('@/layout/MobileLayout.vue'),
    children: [
      {
        path: 'home',
        name: 'InspectorHome',
        component: () => import('@/views/inspector/Home.vue')
      },
      {
        path: 'routes',
        name: 'InspectorRoutes',
        component: () => import('@/views/inspector/RouteList.vue')
      },
      {
        path: 'route/:id',
        name: 'InspectorRouteDetail',
        component: () => import('@/views/inspector/RouteDetail.vue')
      },
      {
        path: 'inspection/:routeId/:stationId',
        name: 'InspectionSubmit',
        component: () => import('@/views/inspector/InspectionSubmit.vue')
      },
      {
        path: 'exceptions',
        name: 'InspectorExceptions',
        component: () => import('@/views/inspector/ExceptionList.vue')
      },
      {
        path: 'profile',
        name: 'InspectorProfile',
        component: () => import('@/views/inspector/Profile.vue')
      }
    ]
  },
  {
    path: '/repair',
    name: 'RepairLayout',
    component: () => import('@/layout/MobileLayout.vue'),
    children: [
      {
        path: 'home',
        name: 'RepairHome',
        component: () => import('@/views/repair/Home.vue')
      },
      {
        path: 'orders',
        name: 'RepairOrders',
        component: () => import('@/views/repair/OrderList.vue')
      },
      {
        path: 'order/:id',
        name: 'RepairOrderDetail',
        component: () => import('@/views/repair/OrderDetail.vue')
      },
      {
        path: 'profile',
        name: 'RepairProfile',
        component: () => import('@/views/repair/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem('userInfo')
  if (to.path === '/login') {
    next()
  } else {
    if (!userInfo) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
