<template>
  <div class="order-list">
    <div class="page-header">
      <div class="title">我的工单</div>
    </div>

    <div class="filter-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </div>
    </div>

    <div class="list-content">
      <div class="order-card" v-for="item in listData" :key="item.id" @click="goDetail(item)">
        <div class="card-header">
          <span class="order-no">{{ item.orderNo }}</span>
          <el-tag :type="getStatusTagType(item.status)" size="small">
            {{ getStatusText(item.status) }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="station-row">
            <el-icon><Location /></el-icon>
            <span class="station-name">{{ item.stationName }}</span>
            <el-tag :type="getLevelTagType(item.faultLevel)" size="small" class="level-tag">
              {{ getLevelText(item.faultLevel) }}
            </el-tag>
          </div>
          <div class="fault-row">
            <span class="fault-type">{{ getFaultTypeText(item.faultType) }}</span>
          </div>
          <div class="desc-row" v-if="item.faultDesc">
            {{ item.faultDesc }}
          </div>
        </div>
        <div class="card-footer">
          <span class="time">{{ getTimeText(item) }}</span>
          <div class="actions">
            <el-button type="primary" size="small" v-if="item.status === 'DISPATCHED'" @click.stop="handleAccept(item)">
              接单
            </el-button>
            <el-button type="success" size="small" v-if="item.status === 'ACCEPTED'" @click.stop="handleArrive(item)">
              到达现场
            </el-button>
            <el-button type="warning" size="small" v-if="item.status === 'IN_PROGRESS'" @click.stop="handleFinish(item)">
              完成抢修
            </el-button>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="listData.length === 0 && !loading">
        <el-empty description="暂无工单数据" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Location } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyRepairOrders, acceptOrder, arriveSite, finishRepair } from '@/api/repair'

const router = useRouter()
const loading = ref(false)
const listData = ref([])
const activeTab = ref('ALL')

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20
})

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待接单', value: 'DISPATCHED' },
  { label: '进行中', value: 'PROCESSING' },
  { label: '已完成', value: 'FINISHED' }
]

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

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

const getStatusText = (status) => {
  const map = {
    CREATED: '已创建',
    DISPATCHED: '待接单',
    ACCEPTED: '已接单',
    IN_PROGRESS: '抢修中',
    FINISHED: '已完成',
    CONFIRMED: '已确认',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    CREATED: 'info',
    DISPATCHED: 'warning',
    ACCEPTED: 'primary',
    IN_PROGRESS: 'danger',
    FINISHED: 'success',
    CONFIRMED: 'success',
    CLOSED: 'info'
  }
  return map[status] || 'info'
}

const getTimeText = (item) => {
  if (item.dispatchTime) return `派单: ${item.dispatchTime}`
  if (item.createTime) return `创建: ${item.createTime}`
  return ''
}

const switchTab = (value) => {
  activeTab.value = value
  queryForm.pageNum = 1
  listData.value = []
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (activeTab.value === 'PROCESSING') {
      // 进行中包括已接单和抢修中
    } else if (activeTab.value !== 'ALL') {
      params.status = activeTab.value
    }
    const res = await getMyRepairOrders(params)
    let records = res.records || []

    if (activeTab.value === 'PROCESSING') {
      records = records.filter(r => r.status === 'ACCEPTED' || r.status === 'IN_PROGRESS')
    }

    listData.value = records
  } finally {
    loading.value = false
  }
}

const goDetail = (item) => {
  router.push(`/repair/order/${item.id}`)
}

const handleAccept = (item) => {
  ElMessageBox.confirm('确定接单吗？', '提示', {
    type: 'warning',
    confirmButtonText: '接单',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await acceptOrder(item.id, {
        repairTeamId: userInfo.userId,
        repairTeamName: userInfo.userName
      })
      ElMessage.success('接单成功')
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    }
  }).catch(() => {})
}

const handleArrive = (item) => {
  ElMessageBox.confirm('确认已到达现场？', '提示', {
    type: 'warning',
    confirmButtonText: '确认到达',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await arriveSite(item.id, {
        repairTeamId: userInfo.userId,
        repairTeamName: userInfo.userName
      })
      ElMessage.success('已确认到达')
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    }
  }).catch(() => {})
}

const handleFinish = (item) => {
  ElMessageBox.prompt('请输入抢修结果描述', '完成抢修', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPlaceholder: '请描述抢修情况',
    inputType: 'textarea'
  }).then(async ({ value }) => {
    try {
      await finishRepair(item.id, {
        repairTeamId: userInfo.userId,
        repairTeamName: userInfo.userName,
        repairResult: value
      })
      ElMessage.success('抢修完成')
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-list {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.page-header {
  background: #fff;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.filter-tabs {
  display: flex;
  background: #fff;
  padding: 0 8px;
  margin-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 14px;
  color: #606266;
  position: relative;
  cursor: pointer;
}

.tab-item.active {
  color: #e74c3c;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 2px;
  background: #e74c3c;
  border-radius: 2px;
}

.list-content {
  padding: 0 12px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  cursor: pointer;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid #f0f2f5;
}

.order-no {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.card-body {
  padding: 12px 14px;
}

.station-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.station-row .el-icon {
  font-size: 14px;
  color: #e74c3c;
}

.station-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  flex: 1;
}

.level-tag {
  margin-left: auto;
}

.fault-row {
  margin-bottom: 6px;
}

.fault-type {
  font-size: 13px;
  color: #409eff;
}

.desc-row {
  font-size: 12px;
  color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #fafafa;
  border-top: 1px solid #f0f2f5;
}

.time {
  font-size: 11px;
  color: #c0c4cc;
}

.actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  padding: 40px 0;
}
</style>
