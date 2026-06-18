<template>
  <div class="inspector-home">
    <div class="header">
      <div class="user-info">
        <el-avatar :size="48" class="avatar">
          {{ userInfo.userName?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="info">
          <div class="name">{{ userInfo.userName }}</div>
          <div class="role">巡检员</div>
        </div>
      </div>
    </div>

    <div class="stats">
      <div class="stat-item">
        <div class="stat-value">{{ stats.todayRouteCount }}</div>
        <div class="stat-label">今日路线</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.completedCount }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.exceptionCount }}</div>
        <div class="stat-label">异常数</div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <span class="section-title">当前巡检</span>
        <el-button type="primary" link size="small" @click="$router.push('/inspector/routes')">全部路线</el-button>
      </div>
      <div class="route-card" v-if="currentRoute" @click="goRouteDetail(currentRoute)">
        <div class="route-top">
          <span class="route-name">{{ currentRoute.routeName }}</span>
          <span class="status-tag status-warning">进行中</span>
        </div>
        <div class="route-info">
          <span>站点: {{ currentRoute.completedStations || 0 }} / {{ currentRoute.totalStations || 0 }}</span>
          <span>{{ currentRoute.planDate }}</span>
        </div>
        <div class="route-progress">
          <el-progress :percentage="routeProgress" :show-text="false" stroke-width="6" />
        </div>
      </div>
      <div class="empty-card" v-else>
        <el-empty description="暂无进行中的路线" :image-size="80">
          <el-button type="primary" size="small" @click="$router.push('/inspector/routes')">查看路线</el-button>
        </el-empty>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <span class="section-title">最近异常</span>
        <el-button type="primary" link size="small" @click="$router.push('/inspector/exceptions')">全部</el-button>
      </div>
      <div class="exception-list">
        <div class="exception-item" v-for="item in recentExceptions" :key="item.id" @click="goException(item)">
          <div class="exception-top">
            <el-tag :type="getLevelTagType(item.exceptionLevel)" size="small">
              {{ getLevelText(item.exceptionLevel) }}
            </el-tag>
            <span class="exception-type">{{ getTypeText(item.exceptionType) }}</span>
          </div>
          <div class="exception-station">{{ item.stationName }}</div>
          <div class="exception-time">{{ item.reportTime }}</div>
        </div>
        <div class="empty-text" v-if="recentExceptions.length === 0">
          暂无异常记录
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyRoutes } from '@/api/inspection'
import { getMyExceptions } from '@/api/exception'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const stats = reactive({
  todayRouteCount: 0,
  completedCount: 0,
  exceptionCount: 0
})

const currentRoute = ref(null)
const recentExceptions = ref([])

const routeProgress = computed(() => {
  if (!currentRoute.value || !currentRoute.value.totalStations) return 0
  return Math.round((currentRoute.value.completedStations / currentRoute.value.totalStations) * 100)
})

const getTypeText = (type) => {
  const map = {
    PRESSURE_HIGH: '压力过高',
    PRESSURE_LOW: '压力过低',
    TEMP_HIGH: '温度过高',
    TEMP_LOW: '温度过低',
    DEVICE_FAULT: '设备故障',
    OTHER: '其他'
  }
  return map[type] || type
}

const getLevelTagType = (level) => {
  const map = { CRITICAL: 'danger', WARNING: 'warning', NORMAL: 'success' }
  return map[level] || 'info'
}

const getLevelText = (level) => {
  const map = { CRITICAL: '严重', WARNING: '警告', NORMAL: '一般' }
  return map[level] || level
}

const loadData = async () => {
  try {
    const [routeRes, exceptionRes] = await Promise.all([
      getMyRoutes({ pageNum: 1, pageSize: 10 }),
      getMyExceptions({ pageNum: 1, pageSize: 5 })
    ])

    const routes = routeRes.records || []
    stats.todayRouteCount = routes.length
    stats.completedCount = routes.filter(r => r.status === 'COMPLETED').length
    currentRoute.value = routes.find(r => r.status === 'IN_PROGRESS') || routes.find(r => r.status === 'ASSIGNED') || null

    recentExceptions.value = exceptionRes.records || []
    stats.exceptionCount = exceptionRes.total || 0
  } catch (e) {
    console.error(e)
  }
}

const goRouteDetail = (route) => {
  router.push(`/inspector/route/${route.id}`)
}

const goException = (item) => {
  console.log('异常详情', item)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.inspector-home {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.header {
  background: linear-gradient(135deg, #409eff 0%, #1e88e5 100%);
  padding: 20px 16px 40px;
  color: #fff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.3);
}

.info .name {
  font-size: 18px;
  font-weight: 600;
}

.info .role {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 2px;
}

.stats {
  background: #fff;
  margin: -24px 16px 16px;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.section {
  margin: 0 16px 16px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.route-card {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;
}

.route-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.route-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.status-tag.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.route-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.empty-card {
  padding: 20px 0;
}

.exception-list {
  max-height: 300px;
  overflow-y: auto;
}

.exception-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
}

.exception-item:last-child {
  border-bottom: none;
}

.exception-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.exception-type {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.exception-station {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.exception-time {
  font-size: 12px;
  color: #c0c4cc;
}

.empty-text {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
</style>
