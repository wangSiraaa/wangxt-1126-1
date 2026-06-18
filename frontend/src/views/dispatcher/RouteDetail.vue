<template>
  <div class="route-detail">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="page-title">路线详情</span>
    </div>

    <div class="info-card" v-if="routeInfo">
      <div class="info-header">
        <div>
          <span class="route-name">{{ routeInfo.routeName }}</span>
          <span class="status-tag" :class="getStatusClass(routeInfo.status)">
            {{ getStatusText(routeInfo.status) }}
          </span>
        </div>
        <div>
          <el-button type="success" size="small" @click="handleStart" v-if="routeInfo.status === 'ASSIGNED'">开始巡检</el-button>
          <el-button type="primary" size="small" @click="handleComplete" v-if="routeInfo.status === 'IN_PROGRESS'">完成路线</el-button>
        </div>
      </div>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="路线编号">{{ routeInfo.routeCode }}</el-descriptions-item>
        <el-descriptions-item label="巡检员">{{ routeInfo.inspectorName }}</el-descriptions-item>
        <el-descriptions-item label="调度员">{{ routeInfo.dispatcherName }}</el-descriptions-item>
        <el-descriptions-item label="计划日期">{{ routeInfo.planDate }}</el-descriptions-item>
        <el-descriptions-item label="站点数量">
          {{ routeInfo.completedStations || 0 }} / {{ routeInfo.totalStations || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="异常数">
          <span style="color: #f56c6c" v-if="routeInfo.exceptionCount > 0">
            {{ routeInfo.exceptionCount }}
          </span>
          <span v-else>0</span>
        </el-descriptions-item>
        <el-descriptions-item label="路线描述" :span="3">
          {{ routeInfo.routeDesc || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="stations-card">
      <div class="card-header">
        <span class="card-title">站点列表</span>
      </div>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in stationList"
          :key="item.id"
          :type="getTimelineType(item)"
          :timestamp="item.recordTime || ''"
        >
          <div class="station-item">
            <div class="station-info">
              <span class="station-name">{{ item.stationName }}</span>
              <el-tag :type="item.stationType === 'PRIMARY' ? 'danger' : 'success'" size="small">
                {{ item.stationType === 'PRIMARY' ? '一次网' : '二次网' }}
              </el-tag>
              <span class="status-badge" :class="getItemStatusClass(item.status)">
                {{ getItemStatusText(item.status) }}
              </span>
            </div>
            <div class="station-details" v-if="item.status === 'COMPLETED'">
              <span>压力: {{ item.pressure }} MPa</span>
              <span>供水温度: {{ item.supplyTemp }}℃</span>
              <span>回水温度: {{ item.returnTemp }}℃</span>
            </div>
            <div class="station-exception" v-if="item.hasException">
              <el-tag type="danger" size="small">有异常</el-tag>
            </div>
            <div class="station-actions">
              <el-button type="primary" size="small" link @click="goInspection(item)"
                v-if="item.status === 'PENDING' || item.status === 'IN_PROGRESS'">
                前往巡检
              </el-button>
              <el-button type="primary" size="small" link @click="viewRecord(item)"
                v-if="item.status === 'COMPLETED'">
                查看记录
              </el-button>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRouteDetail, startRoute, completeRoute } from '@/api/inspection'

const route = useRoute()
const router = useRouter()
const routeInfo = ref({})
const stationList = ref([])
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const loadData = async () => {
  try {
    const res = await getRouteDetail(route.params.id)
    routeInfo.value = res.route || {}
    stationList.value = res.stations || []
  } catch (e) {
    console.error(e)
  }
}

const getStatusText = (status) => {
  const map = {
    DRAFT: '草稿',
    ASSIGNED: '已下发',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  const map = {
    DRAFT: 'status-info',
    ASSIGNED: 'status-primary',
    IN_PROGRESS: 'status-warning',
    COMPLETED: 'status-success',
    CANCELLED: 'status-info'
  }
  return map[status] || 'status-info'
}

const getItemStatusText = (status) => {
  const map = {
    PENDING: '待巡检',
    IN_PROGRESS: '巡检中',
    COMPLETED: '已完成',
    SKIPPED: '已跳过'
  }
  return map[status] || status
}

const getItemStatusClass = (status) => {
  const map = {
    PENDING: 'badge-pending',
    IN_PROGRESS: 'badge-progress',
    COMPLETED: 'badge-success',
    SKIPPED: 'badge-skipped'
  }
  return map[status] || 'badge-pending'
}

const getTimelineType = (item) => {
  if (item.status === 'COMPLETED') return item.hasException ? 'danger' : 'success'
  if (item.status === 'IN_PROGRESS') return 'primary'
  return ''
}

const goBack = () => {
  router.push('/dispatcher/route')
}

const handleStart = async () => {
  await startRoute({ routeId: route.params.id, userId: userInfo.userId })
  ElMessage.success('已开始巡检')
  loadData()
}

const handleComplete = async () => {
  await completeRoute({ routeId: route.params.id, userId: userInfo.userId })
  ElMessage.success('路线已完成')
  loadData()
}

const goInspection = (item) => {
  router.push(`/dispatcher/inspection/${route.params.id}/${item.id}`)
}

const viewRecord = (item) => {
  router.push(`/dispatcher/inspection/${route.params.id}/${item.id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.route-detail {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.info-card {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.route-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-right: 12px;
}

.status-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.status-primary {
  background: #ecf5ff;
  color: #409eff;
}

.status-tag.status-success {
  background: #f0f9eb;
  color: #67c23a;
}

.status-tag.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-tag.status-info {
  background: #f4f4f5;
  color: #909399;
}

.stations-card {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.card-header {
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.station-item {
  padding: 12px 0;
}

.station-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.station-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.status-badge {
  margin-left: auto;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.badge-pending {
  background: #f4f4f5;
  color: #909399;
}

.badge-progress {
  background: #ecf5ff;
  color: #409eff;
}

.badge-success {
  background: #f0f9eb;
  color: #67c23a;
}

.badge-skipped {
  background: #f0f9eb;
  color: #909399;
}

.station-details {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.station-exception {
  margin-bottom: 8px;
}

.station-actions {
  text-align: right;
}
</style>
