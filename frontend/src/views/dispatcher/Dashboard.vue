<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stats-cards">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon icon-blue">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.stationCount }}</div>
            <div class="stat-label">换热站总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon icon-green">
            <el-icon><Route /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayRouteCount }}</div>
            <div class="stat-label">今日巡检路线</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon icon-orange">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingExceptionCount }}</div>
            <div class="stat-label">待处理异常</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon icon-red">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.repairingCount }}</div>
            <div class="stat-label">抢修中工单</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :span="12">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">待处理异常</span>
            <el-button type="primary" link @click="$router.push('/dispatcher/exception')">查看全部</el-button>
          </div>
          <el-table :data="pendingExceptions" stripe style="width: 100%" size="small">
            <el-table-column prop="exceptionNo" label="异常编号" width="130" />
            <el-table-column prop="stationName" label="站点" width="120" />
            <el-table-column prop="exceptionType" label="类型" width="100">
              <template #default="{ row }">
                <span>{{ getExceptionTypeText(row.exceptionType) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="exceptionLevel" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="getLevelTagType(row.exceptionLevel)" size="small">
                  {{ getLevelText(row.exceptionLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reporterName" label="上报人" width="80" />
            <el-table-column prop="reportTime" label="上报时间" width="160" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <span class="status-tag status-warning">{{ getStatusText(row.status) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">进行中抢修工单</span>
            <el-button type="primary" link @click="$router.push('/dispatcher/repair')">查看全部</el-button>
          </div>
          <el-table :data="repairingOrders" stripe style="width: 100%" size="small">
            <el-table-column prop="orderNo" label="工单号" width="140" />
            <el-table-column prop="stationName" label="站点" width="120" />
            <el-table-column prop="faultType" label="故障类型" width="100">
              <template #default="{ row }">
                <span>{{ getFaultTypeText(row.faultType) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="faultLevel" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="getLevelTagType(row.faultLevel)" size="small">
                  {{ getLevelText(row.faultLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="repairTeamName" label="抢修队" width="100" />
            <el-table-column prop="dispatchTime" label="派单时间" width="160" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <span class="status-tag status-primary">{{ getRepairStatusText(row.status) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :span="12">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">今日报冷事件</span>
            <el-button type="primary" link @click="$router.push('/dispatcher/complaint')">查看全部</el-button>
          </div>
          <el-table :data="todayComplaints" stripe style="width: 100%" size="small">
            <el-table-column prop="complaintNo" label="事件编号" width="130" />
            <el-table-column prop="residentName" label="居民" width="80" />
            <el-table-column prop="stationName" label="所属站点" width="120" />
            <el-table-column prop="complaintType" label="类型" width="100">
              <template #default="{ row }">
                <span>{{ getComplaintTypeText(row.complaintType) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="indoorTemp" label="室内温度" width="90">
              <template #default="{ row }">
                <span v-if="row.indoorTemp">{{ row.indoorTemp }}℃</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="complaintTime" label="报冷时间" width="160" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <span class="status-tag" :class="getStatusClass(row.status)">{{ getComplaintStatusText(row.status) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">今日巡检路线</span>
            <el-button type="primary" link @click="$router.push('/dispatcher/route')">查看全部</el-button>
          </div>
          <el-table :data="todayRoutes" stripe style="width: 100%" size="small">
            <el-table-column prop="routeCode" label="路线编号" width="140" />
            <el-table-column prop="routeName" label="路线名称" />
            <el-table-column prop="inspectorName" label="巡检员" width="80" />
            <el-table-column prop="totalStations" label="站点数" width="70" align="center" />
            <el-table-column prop="completedStations" label="已完成" width="70" align="center" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <span class="status-tag" :class="getRouteStatusClass(row.status)">
                  {{ getRouteStatusText(row.status) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExceptionPage } from '@/api/exception'
import { getRepairOrderPage } from '@/api/repair'
import { getComplaintPage } from '@/api/complaint'
import { getRoutePage, getMyRoutes } from '@/api/inspection'
import { getStationList } from '@/api/station'

const stats = reactive({
  stationCount: 0,
  todayRouteCount: 0,
  pendingExceptionCount: 0,
  repairingCount: 0
})

const pendingExceptions = ref([])
const repairingOrders = ref([])
const todayComplaints = ref([])
const todayRoutes = ref([])

const loadData = async () => {
  try {
    const [stationRes, exceptionRes, repairRes, complaintRes, routeRes] = await Promise.all([
      getStationList(),
      getExceptionPage({ pageNum: 1, pageSize: 5, status: 'REPORTED' }),
      getRepairOrderPage({ pageNum: 1, pageSize: 5, status: 'IN_PROGRESS' }),
      getComplaintPage({ pageNum: 1, pageSize: 5 }),
      getRoutePage({ pageNum: 1, pageSize: 5 })
    ])

    stats.stationCount = stationRes?.length || 0
    stats.pendingExceptionCount = exceptionRes?.total || 0
    stats.repairingCount = repairRes?.total || 0
    stats.todayRouteCount = routeRes?.total || 0

    pendingExceptions.value = exceptionRes?.records || []
    repairingOrders.value = repairRes?.records || []
    todayComplaints.value = complaintRes?.records || []
    todayRoutes.value = routeRes?.records || []
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const getExceptionTypeText = (type) => {
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

const getStatusText = (status) => {
  const map = {
    REPORTED: '已上报',
    CONFIRMED: '已确认',
    DISPATCHED: '已派单',
    IN_REPAIR: '抢修中',
    REPAIRED: '已修复',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const getFaultTypeText = (type) => {
  const map = {
    PRESSURE: '压力异常',
    TEMPERATURE: '温度异常',
    DEVICE: '设备故障',
    PIPE: '管网泄漏',
    OTHER: '其他'
  }
  return map[type] || type
}

const getRepairStatusText = (status) => {
  const map = {
    CREATED: '已创建',
    DISPATCHED: '已派单',
    ACCEPTED: '已接单',
    IN_PROGRESS: '抢修中',
    FINISHED: '已完成',
    CONFIRMED: '已确认',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const getComplaintTypeText = (type) => {
  const map = {
    NOT_HOT: '不暖',
    TEMPERATURE_LOW: '温度低',
    LEAKAGE: '漏水',
    NOISE: '噪音',
    OTHER: '其他'
  }
  return map[type] || type
}

const getComplaintStatusText = (status) => {
  const map = {
    CREATED: '已创建',
    PROCESSING: '处理中',
    REPAIRING: '抢修中',
    REPAIRED: '已修复',
    VISITED: '已回访',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  const map = {
    CREATED: 'status-warning',
    PROCESSING: 'status-primary',
    REPAIRING: 'status-danger',
    REPAIRED: 'status-success',
    VISITED: 'status-info',
    CLOSED: 'status-info'
  }
  return map[status] || 'status-info'
}

const getRouteStatusText = (status) => {
  const map = {
    DRAFT: '草稿',
    ASSIGNED: '已下发',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getRouteStatusClass = (status) => {
  const map = {
    DRAFT: 'status-info',
    ASSIGNED: 'status-primary',
    IN_PROGRESS: 'status-warning',
    COMPLETED: 'status-success',
    CANCELLED: 'status-info'
  }
  return map[status] || 'status-info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  height: 100%;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 16px;
}

.icon-blue {
  background: linear-gradient(135deg, #409eff 0%, #1e88e5 100%);
}

.icon-green {
  background: linear-gradient(135deg, #67c23a 0%, #52c41a 100%);
}

.icon-orange {
  background: linear-gradient(135deg, #e6a23c 0%, #fa8c16 100%);
}

.icon-red {
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.content-row {
  margin-bottom: 20px;
}

.panel {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
