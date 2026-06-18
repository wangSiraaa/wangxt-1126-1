<template>
  <div class="mobile-layout">
    <div class="mobile-content">
      <router-view />
    </div>
    <div class="tabbar" v-if="showTabbar">
      <div
        v-for="item in tabs"
        :key="item.path"
        class="tab-item"
        :class="{ active: activeTab === item.path }"
        @click="handleTabClick(item.path)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const inspectorTabs = [
  { path: '/inspector/home', name: '首页', icon: 'HomeFilled' },
  { path: '/inspector/routes', name: '路线', icon: 'List' },
  { path: '/inspector/exceptions', name: '异常', icon: 'Warning' },
  { path: '/inspector/profile', name: '我的', icon: 'User' }
]

const repairTabs = [
  { path: '/repair/home', name: '首页', icon: 'HomeFilled' },
  { path: '/repair/orders', name: '工单', icon: 'Document' },
  { path: '/repair/profile', name: '我的', icon: 'User' }
]

const tabs = computed(() => {
  if (userInfo.value.roleCode === 'INSPECTOR') {
    return inspectorTabs
  }
  if (userInfo.value.roleCode === 'REPAIR') {
    return repairTabs
  }
  return inspectorTabs
})

const activeTab = computed(() => {
  const path = route.path
  for (const tab of tabs.value) {
    if (path.startsWith(tab.path) || tab.path.includes(path.split('/').slice(0, 3).join('/'))) {
      return tab.path
    }
  }
  return tabs.value[0]?.path || ''
})

const showTabbar = computed(() => {
  const path = route.path
  if (path.includes('/route/') && path !== '/inspector/routes') return false
  if (path.includes('/inspection/')) return false
  if (path.includes('/order/') && path !== '/repair/orders') return false
  return true
})

const handleTabClick = (path) => {
  router.push(path)
}
</script>

<style scoped>
.mobile-layout {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 60px;
}

.mobile-content {
  min-height: calc(100vh - 60px);
}

.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  display: flex;
  border-top: 1px solid #ebeef5;
  z-index: 100;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
  cursor: pointer;
}

.tab-item .el-icon {
  font-size: 22px;
  margin-bottom: 2px;
}

.tab-item.active {
  color: #409eff;
}
</style>
