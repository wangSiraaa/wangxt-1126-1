<template>
  <div class="repair-home">
    <div class="header">
      <div class="user-info">
        <el-avatar :size="48" class="avatar">
          {{ userInfo.userName?.charAt(0) || 'R' }}
        </el-avatar>
        <div class="info">
          <div class="name">{{ userInfo.userName }}</div>
          <div class="role">抢修队</div>
        </div>
      </div>
    </div>

    <div class="stats">
      <div class="stat-item">
        <div class="stat-value stat-warn">{{ stats.pendingCount }}</div>
        <div class="stat-label">待接单</div>
      </div>
      <div class="stat-item">
        <div class="stat-value stat-primary">{{ stats.processingCount }}</div>
        <div class="stat-label">抢修中</div>
      </div>
      <div class="stat-item">
        <div class="stat-value stat-success">{{ stats.completedCount }}</div>
        <div class="stat-label">已完成</div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <span class="section-title">待接单工单</span>
        <el-button type="primary" link size="small" @click="$router.push('/repair/orders')">全部工单</el-button>
      </div>
      <div class="order-card" v-if="pendingOrder" @click="goOrderDetail(pendingOrder)">
        <div class="order-top">
          <span class="order-no">{{ pendingOrder.orderNo }}</span>
          <el-tag type="warning" size="small">待接单</el-tag>
        </div>
        <div class="order-station">
          <el-icon><Location /></el-icon>
          <span>{{ pendingOrder.stationName }}</span>
        </div>
        <div class="order-info">
          <span>{{ getFaultTypeText(pendingOrder.faultType) }}</span>
          <el-tag :type="getLevelTagType(pendingOrder.faultLevel)" size="small">
            {{ getLevelText(pendingOrder.faultLevel) }}
          </el-tag>
        </div>
        <div class="order-desc">
          {{ pendingOrder.faultDesc || '暂无描述' }}
        </div>
        <div class="order-time">
          派单时间: {{ pendingOrder.dispatchTime }}
        </div>
      </div>
      <div class="empty-card" v-else>
        <el-empty description="暂无待接单工单" :image-size="60" />
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <span class="section-title">今日完成</span>
      </div>
      <div class="summary-card">
        <div class="summary-item">
          <el-icon class="icon-success"><CircleCheck /></el-icon>
          <span>今日完成 {{ stats.todayCompleted }} 单</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Location, CircleCheck } from '@element-plus/icons-vue'
import { getMyRepairOrders } from '@/api/repair'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const stats = reactive({
  pendingCount: 0,
  processingCount: 0,
  completedCount: 0,
  todayCompleted: 0
})

const pendingOrder = ref(null)

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
    const res = await getMyRepairOrders({ pageNum: 1, pageSize: 20 })
    const orders = res.records || []
    stats.pendingCount = orders.filter(o => o.status === 'DISPATCHED').length
    stats.processingCount = orders.filter(o => o.status === 'ACCEPTED' || o.status === 'IN_PROGRESS').length
    stats.completedCount = orders.filter(o => o.status === 'FINISHED' || o.status === 'CONFIRMED' || o.status === 'CLOSED').length

    pendingOrder.value = orders.find(o => o.status === 'DISPATCHED') || null

    const today = new Date().toDateString()
    stats.todayCompleted = orders.filter(o => {
      if (o.finishTime) {
        return new Date(o.finishTime).toDateString() === today
      }
      return false
    }).length
  } catch (e) {
    console.error(e)
  }
}

const goOrderDetail = (order) => {
  router.push(`/repair/order/${order.id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.repair-home {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.header {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
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

.stat-value.stat-warn {
  color: #e6a23c;
}

.stat-value.stat-primary {
  color: #409eff;
}

.stat-value.stat-success {
  color: #67c23a;
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

.order-card {
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-no {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.order-station {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.order-station .el-icon {
  font-size: 14px;
  color: #e6a23c;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #606266;
}

.order-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.order-time {
  font-size: 11px;
  color: #c0c4cc;
  padding-top: 8px;
  border-top: 1px dashed #ffd591;
}

.empty-card {
  padding: 20px 0;
}

.summary-card {
  background: #f0f9eb;
  border-radius: 6px;
  padding: 14px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #67c23a;
}

.summary-item .icon-success {
  font-size: 20px;
}
</style>
