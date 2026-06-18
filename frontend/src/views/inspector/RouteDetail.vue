<template>
  <div class="route-detail">
    <div class="page-header">
      <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="title">路线详情</span>
    </div>

    <div class="route-info" v-if="routeInfo">
      <div class="info-top">
        <div class="route-name">{{ routeInfo.routeName }}</div>
        <span class="status-tag" :class="getStatusClass(routeInfo.status)">
          {{ getStatusText(routeInfo.status) }}
        </span>
      </div>
      <div class="info-row">
        <div class="info-item">
          <span class="label">巡检员</span>
          <span class="value">{{ routeInfo.inspectorName }}</span>
        </div>
        <div class="info-item">
          <span class="label">计划日期</span>
          <span class="value">{{ routeInfo.planDate }}</span>
        </div>
      </div>
      <div class="info-row">
        <div class="info-item">
          <span class="label">站点数量</span>
          <span class="value">{{ routeInfo.completedStations || 0 }} / {{ routeInfo.totalStations || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="label">调度员</span>
          <span class="value">{{ routeInfo.dispatcherName }}</span>
        </div>
      </div>
      <div class="info-desc" v-if="routeInfo.routeDesc">
        {{ routeInfo.routeDesc }}
      </div>

      <div class="action-bar" v-if="routeInfo.status === 'ASSIGNED' || routeInfo.status === 'IN_PROGRESS'">
        <el-button type="primary" block @click="handleStart" v-if="routeInfo.status === 'ASSIGNED'">
          开始巡检
        </el-button>
        <el-button type="success" block @click="handleComplete" v-if="routeInfo.status === 'IN_PROGRESS'">
          完成路线
        </el-button>
      </div>
    </div>

    <div class="stations-section">
      <div class="section-title">
        站点列表
        <span class="count">共 {{ stationList.length }} 个</span>
      </div>

      <div class="station-list">
        <div
          class="station-item"
          v-for="(item, index) in stationList"
          :key="item.id"
          :class="{ active: item.status === 'IN_PROGRESS', completed: item.status === 'COMPLETED' }"
          @click="handleStationClick(item)"
        >
          <div class="station-index">
            <div class="index-circle">{{ index + 1 }}</div>
            <div class="index-line" v-if="index < stationList.length - 1"></div>
          </div>
          <div class="station-content">
            <div class="station-top">
              <span class="station-name">{{ item.stationName }}</span>
              <el-tag :type="getStationTagType(item)" size="small">
                {{ getStationStatusText(item.status) }}
              </el-tag>
            </div>
            <div class="station-type">
              <el-tag size="small" :type="item.stationType === 'PRIMARY' ? 'danger' : 'success'">
                {{ item.stationType === 'PRIMARY' ? '一次网' : '二次网' }}
              </el-tag>
            </div>
            <div class="station-data" v-if="item.status === 'COMPLETED'">
              <span>压力: {{ item.pressure }} MPa</span>
              <span>供水: {{ item.supplyTemp }}℃</span>
              <span>回水: {{ item.returnTemp }}℃</span>
            </div>
            <div class="station-exception" v-if="item.hasException">
              <el-tag type="danger" size="small">有异常</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRouteDetail, startRoute, completeRoute } from '@/api/inspection'

const route = useRoute()
const router = useRouter()
const routeInfo = ref({})
const stationList = ref([])
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const getStatusText = (status) => {
  const map = {
    DRAFT: '草稿',
    ASSIGNED: '待开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  const map = {
    DRAFT: 'tag-info',
    ASSIGNED: 'tag-primary',
    IN_PROGRESS: 'tag-warning',
    COMPLETED: 'tag-success',
    CANCELLED: 'tag-info'
  }
  return map[status] || 'tag-info'
}

const getStationStatusText = (status) => {
  const map = {
    PENDING: '待巡检',
    IN_PROGRESS: '巡检中',
    COMPLETED: '已完成',
    SKIPPED: '已跳过'
  }
  return map[status] || status
}

const getStationTagType = (item) => {
  if (item.hasException) return 'danger'
  if (item.status === 'COMPLETED') return 'success'
  if (item.status === 'IN_PROGRESS') return 'primary'
  return 'info'
}

const loadData = async () => {
  try {
    const res = await getRouteDetail(route.params.id)
    routeInfo.value = res.route || {}
    stationList.value = res.stations || []
  } catch (e) {
    console.error(e)
  }
}

const goBack = () => {
  router.back()
}

const handleStart = async () => {
  try {
    await startRoute({ routeId: route.params.id, userId: userInfo.userId })
    ElMessage.success('已开始巡检')
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleComplete = async () => {
  ElMessageBox.confirm('确定完成该巡检路线吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await completeRoute({ routeId: route.params.id, userId: userInfo.userId })
      ElMessage.success('路线已完成')
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    }
  }).catch(() => {})
}

const handleStationClick = (item) => {
  if (item.status === 'PENDING' || item.status === 'IN_PROGRESS') {
    router.push(`/inspector/inspection/${route.params.id}/${item.id}`)
  } else if (item.status === 'COMPLETED') {
    router.push(`/inspector/inspection/${route.params.id}/${item.id}`)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.route-detail {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 10;
}

.title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.route-info {
  background: #fff;
  margin: 12px;
  border-radius: 8px;
  padding: 16px;
}

.info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.route-name {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.status-tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
}

.tag-info {
  background: #f4f4f5;
  color: #909399;
}

.tag-primary {
  background: #ecf5ff;
  color: #409eff;
}

.tag-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.tag-success {
  background: #f0f9eb;
  color: #67c23a;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
}

.info-item {
  flex: 1;
}

.info-item .label {
  font-size: 12px;
  color: #909399;
  margin-right: 6px;
}

.info-item .value {
  font-size: 14px;
  color: #303133;
}

.info-desc {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.action-bar {
  margin-top: 16px;
}

.stations-section {
  background: #fff;
  margin: 12px;
  border-radius: 8px;
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.section-title .count {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-left: 6px;
}

.station-list {
  position: relative;
}

.station-item {
  display: flex;
  padding-bottom: 16px;
  cursor: pointer;
}

.station-item:last-child {
  padding-bottom: 0;
}

.station-index {
  width: 24px;
  margin-right: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.index-circle {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #f0f2f5;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.station-item.active .index-circle {
  background: #409eff;
  color: #fff;
}

.station-item.completed .index-circle {
  background: #67c23a;
  color: #fff;
}

.index-line {
  flex: 1;
  width: 2px;
  background: #f0f2f5;
  margin-top: 4px;
}

.station-item.completed .index-line {
  background: #c2e7b0;
}

.station-content {
  flex: 1;
  background: #f7f8fa;
  border-radius: 6px;
  padding: 10px 12px;
}

.station-item.active .station-content {
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
}

.station-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.station-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.station-type {
  margin-bottom: 6px;
}

.station-data {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #606266;
  margin-top: 6px;
}

.station-exception {
  margin-top: 6px;
}
</style>
