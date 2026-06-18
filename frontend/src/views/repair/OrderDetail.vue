<template>
  <div class="order-detail">
    <div class="page-header">
      <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="title">工单详情</span>
    </div>

    <div class="status-card" v-if="orderInfo">
      <div class="status-main">
        <span class="order-no">{{ orderInfo.orderNo }}</span>
        <span class="status-tag" :class="getStatusClass(orderInfo.status)">
          {{ getStatusText(orderInfo.status) }}
        </span>
      </div>
      <div class="status-sub">
        <el-tag :type="getLevelTagType(orderInfo.faultLevel)" size="small">
          {{ getLevelText(orderInfo.faultLevel) }}
        </el-tag>
        <span class="fault-type">{{ getFaultTypeText(orderInfo.faultType) }}</span>
      </div>
    </div>

    <div class="info-card" v-if="orderInfo">
      <div class="card-title">站点信息</div>
      <div class="info-row">
        <span class="label">站点名称</span>
        <span class="value">{{ orderInfo.stationName }}</span>
      </div>
      <div class="info-row" v-if="orderInfo.stationType">
        <span class="label">站点类型</span>
        <span class="value">{{ orderInfo.stationType === 'PRIMARY' ? '一次网' : '二次网' }}</span>
      </div>
    </div>

    <div class="info-card" v-if="orderInfo">
      <div class="card-title">故障信息</div>
      <div class="info-row">
        <span class="label">故障描述</span>
        <span class="value desc">{{ orderInfo.faultDesc || '暂无描述' }}</span>
      </div>
      <div class="info-row" v-if="orderInfo.remark">
        <span class="label">派单备注</span>
        <span class="value desc">{{ orderInfo.remark }}</span>
      </div>
      <div class="info-row" v-if="orderInfo.repairResult">
        <span class="label">抢修结果</span>
        <span class="value desc">{{ orderInfo.repairResult }}</span>
      </div>
    </div>

    <div class="info-card">
      <div class="card-title">处理流程</div>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in flowLogs"
          :key="item.id"
          :type="getTimelineType(item.operationType)"
          :timestamp="item.operationTime"
        >
          <div class="flow-item">
            <div class="flow-title">{{ getOperationText(item.operationType) }}</div>
            <div class="flow-operator">操作人：{{ item.operatorName }}</div>
            <div class="flow-remark" v-if="item.remark">{{ item.remark }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <div class="empty-flow" v-if="flowLogs.length === 0">
        暂无流转记录
      </div>
    </div>

    <div class="action-bar" v-if="showActions">
      <el-button type="primary" size="large" block @click="handleAccept" v-if="orderInfo.status === 'DISPATCHED'">
        立即接单
      </el-button>
      <el-button type="success" size="large" block @click="handleArrive" v-if="orderInfo.status === 'ACCEPTED'">
        确认到达
      </el-button>
      <el-button type="warning" size="large" block @click="handleFinish" v-if="orderInfo.status === 'IN_PROGRESS'">
        完成抢修
      </el-button>
    </div>

    <el-dialog v-model="finishDialogVisible" title="完成抢修" width="90%">
      <el-form :model="finishForm" label-width="80px">
        <el-form-item label="抢修结果">
          <el-input
            v-model="finishForm.repairResult"
            type="textarea"
            :rows="4"
            placeholder="请输入抢修结果描述"
          />
        </el-form-item>
        <el-form-item label="故障原因">
          <el-input v-model="finishForm.faultCause" placeholder="请输入故障原因（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmFinish" :loading="finishLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRepairOrderDetail, acceptOrder, arriveSite, finishRepair } from '@/api/repair'
import { getOrderFlowLogs } from '@/api/exception'

const route = useRoute()
const router = useRouter()
const orderInfo = ref({})
const flowLogs = ref([])
const finishDialogVisible = ref(false)
const finishLoading = ref(false)

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const finishForm = reactive({
  repairResult: '',
  faultCause: ''
})

const showActions = computed(() => {
  const status = orderInfo.value.status
  return status === 'DISPATCHED' || status === 'ACCEPTED' || status === 'IN_PROGRESS'
})

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

const getStatusClass = (status) => {
  const map = {
    CREATED: 'tag-info',
    DISPATCHED: 'tag-warning',
    ACCEPTED: 'tag-primary',
    IN_PROGRESS: 'tag-danger',
    FINISHED: 'tag-success',
    CONFIRMED: 'tag-success',
    CLOSED: 'tag-info'
  }
  return map[status] || 'tag-info'
}

const getOperationText = (type) => {
  const map = {
    CREATE: '创建工单',
    DISPATCH: '派单',
    ACCEPT: '接单',
    ARRIVE: '到达现场',
    START_REPAIR: '开始抢修',
    FINISH_REPAIR: '完成抢修',
    CONFIRM: '确认完成',
    CLOSE: '关闭工单',
    REOPEN: '重开工单'
  }
  return map[type] || type
}

const getTimelineType = (type) => {
  const map = {
    CREATE: 'primary',
    DISPATCH: 'warning',
    ACCEPT: 'warning',
    ARRIVE: 'warning',
    START_REPAIR: 'danger',
    FINISH_REPAIR: 'success',
    CONFIRM: 'success',
    CLOSE: 'info',
    REOPEN: 'warning'
  }
  return map[type] || 'primary'
}

const loadData = async () => {
  try {
    const res = await getRepairOrderDetail(route.params.id)
    orderInfo.value = res.order || {}
    flowLogs.value = res.flowLogs || []
  } catch (e) {
    console.error(e)
  }
}

const goBack = () => {
  router.back()
}

const handleAccept = () => {
  ElMessageBox.confirm('确定接收此工单吗？', '提示', {
    type: 'warning',
    confirmButtonText: '接单',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await acceptOrder(route.params.id, {
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

const handleArrive = () => {
  ElMessageBox.confirm('确认已到达现场？', '提示', {
    type: 'warning',
    confirmButtonText: '确认到达',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await arriveSite(route.params.id, {
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

const handleFinish = () => {
  finishForm.repairResult = ''
  finishForm.faultCause = ''
  finishDialogVisible.value = true
}

const confirmFinish = async () => {
  if (!finishForm.repairResult.trim()) {
    ElMessage.warning('请输入抢修结果描述')
    return
  }
  finishLoading.value = true
  try {
    await finishRepair(route.params.id, {
      repairTeamId: userInfo.userId,
      repairTeamName: userInfo.userName,
      repairResult: finishForm.repairResult,
      faultCause: finishForm.faultCause
    })
    ElMessage.success('抢修完成')
    finishDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    finishLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-detail {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
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

.status-card {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  margin: 12px;
  border-radius: 8px;
  padding: 18px 16px;
  color: #fff;
}

.status-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-no {
  font-size: 17px;
  font-weight: 600;
}

.status-tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  background: rgba(255,255,255,0.2);
}

.status-sub {
  display: flex;
  align-items: center;
  gap: 10px;
}

.fault-type {
  font-size: 13px;
  opacity: 0.9;
}

.info-card {
  background: #fff;
  margin: 12px;
  border-radius: 8px;
  padding: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f2f5;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 13px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.info-row .value {
  color: #303133;
  flex: 1;
}

.info-row .value.desc {
  line-height: 1.6;
}

.flow-item {
  padding: 4px 0;
}

.flow-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.flow-operator {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.flow-remark {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
}

.empty-flow {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px;
  box-shadow: 0 -2px 12px rgba(0,0,0,0.06);
}
</style>
