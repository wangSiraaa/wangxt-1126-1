<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon><Flame /></el-icon>
        <span>换热站巡检系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#b7c0cc"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dispatcher/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>调度概览</span>
        </el-menu-item>
        <el-menu-item index="/dispatcher/station">
          <el-icon><OfficeBuilding /></el-icon>
          <span>站点管理</span>
        </el-menu-item>
        <el-menu-item index="/dispatcher/route">
          <el-icon><Route /></el-icon>
          <span>巡检路线</span>
        </el-menu-item>
        <el-menu-item index="/dispatcher/exception">
          <el-icon><Warning /></el-icon>
          <span>异常管理</span>
        </el-menu-item>
        <el-menu-item index="/dispatcher/repair">
          <el-icon><Tools /></el-icon>
          <span>抢修工单</span>
        </el-menu-item>
        <el-menu-item index="/dispatcher/complaint">
          <el-icon><ChatDotRound /></el-icon>
          <span>报冷事件</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userInfo.userName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const userInfo = ref({})

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => {
  const titleMap = {
    '/dispatcher/dashboard': '调度概览',
    '/dispatcher/station': '站点管理',
    '/dispatcher/route': '巡检路线',
    '/dispatcher/route/create': '创建路线',
    '/dispatcher/exception': '异常管理',
    '/dispatcher/repair': '抢修工单',
    '/dispatcher/complaint': '报冷事件'
  }
  return titleMap[route.path] || '换热站巡检系统'
})

onMounted(() => {
  const info = localStorage.getItem('userInfo')
  if (info) {
    userInfo.value = JSON.parse(info)
  }
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background: #001529;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #002140;
}

.logo .el-icon {
  font-size: 24px;
  margin-right: 8px;
  color: #409eff;
}

.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  cursor: pointer;
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
