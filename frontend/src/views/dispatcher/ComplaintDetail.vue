<template>
  <div class="complaint-detail">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="page-title">报冷事件详情</span>
    </div>

    <div class="detail-card" v-if="complaintInfo">
      <div class="card-header">
        <span class="complaint-no">{{ complaintInfo.complaintNo }}</span>
        <span class="status-tag" :class="getStatusClass(complaintInfo.status)">
          {{ getStatusText(complaintInfo.status) }}
        </span>
      </div>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="居民姓名">{{ complaintInfo.residentName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ complaintInfo.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="所属站点">{{ complaintInfo.stationName }}</el-descriptions-item>
        <el-descriptions-item label="报冷时间">{{ complaintInfo.complaintTime }}</el-descriptions-item>
        <el-descriptions-item label="报冷类型">{{ getTypeText(complaintInfo.complaintType) }}</el-descriptions-item>
        <el-descriptions-item label="室内温度">
          <span v-if="complaintInfo.indoorTemp">{{ complaintInfo.indoorTemp }}℃</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityTagType(complaintInfo.priority)" size="small">
            {{ getPriorityText(complaintInfo.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="站点指标">
          <el-tag :type="complaintInfo.metricStable === 1 ? 'success' : 'danger'" size="small">
            {{ complaintInfo.metricStable === 1 ? '已回稳' : '未回稳' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ complaintInfo.address }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ complaintInfo.description }}</el-descriptions-item>
        <el-descriptions-item label="优先级升级原因" v-if="complaintInfo.priorityUpgradeReason" :span="2">
          <span style="color: #e6a23c">{{ complaintInfo.priorityUpgradeReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="complaintInfo.handlerName">{{ complaintInfo.handlerName }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="complaintInfo.handleTime">{{ complaintInfo.handleTime }}</el-descriptions-item>
        <el-descriptions-item label="关联抢修工单" v-if="complaintInfo.repairOrderId" :span="2">
          <el-link type="primary" @click="goToRepair">{{ complaintInfo.repairOrderNo || '查看工单' }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="回访结果" v-if="complaintInfo.visitResult" :span="2">
          {{ complaintInfo.visitResult }}
          <span v-if="complaintInfo.satisfaction" style="margin-left: 8px; color: #e6a23c">满意度：{{ complaintInfo.satisfaction }}分</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="timeline-card">
      <div class="card-header">
        <span class="card-title">处理流程</span>
      </div>
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
    </div>

    <div class="action-bar">
      <el-button @click="goBack">返回</el-button>
      <el-button type="warning" @click="handleDispatch" v-if="canDispatch">派抢修工单</el-button>
      <el-button type="info" @click="handleConfirmMetricStable" v-if="canConfirmStable" :loading="stableLoading">
        确认指标回稳
      </el-button>
      <el-button type="success" @click="handleClose" v-if="canClose" :disabled="!canActuallyClose">关闭事件</el-button>
      <div class="close-tip" v-if="showCloseTip" style="color: #e6a23c; font-size: 12px; margin-top: 6px">
        {{ closeTipText }}
      </div>
    </div>

    <el-dialog v-model="dispatchDialogVisible" title="派发抢修工单" width="500px">
      <el-form :model="dispatchForm" label-width="100px">
        <el-form-item label="抢修队">
          <el-select v-model="dispatchForm.repairTeamId" placeholder="请选择抢修队" style="width: 100%">
            <el-option
              v-for="item in repairTeamList"
              :key="item.id"
              :label="item.userName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型">
          <el-select v-model="dispatchForm.faultType" placeholder="请选择" style="width: 100%">
            <el-option label="压力异常" value="PRESSURE" />
            <el-option label="温度异常" value="TEMPERATURE" />
            <el-option label="设备故障" value="DEVICE" />
            <el-option label="管网泄漏" value="PIPE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="派单说明">
          <el-input v-model="dispatchForm.remark" type="textarea" :rows="3" placeholder="请输入派单说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDispatch" :loading="dispatchLoading">派单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getComplaintDetail, dispatchComplaint, closeComplaint, confirmMetricStable } from '@/api/complaint'
import { getOrderFlowLogs } from '@/api/exception'
import { getUserListByRole } from '@/api/user'

const route = useRoute()
const router = useRouter()
const complaintInfo = ref({})
const flowLogs = ref([])
const repairTeamList = ref([])
const dispatchDialogVisible = ref(false)
const dispatchLoading = ref(false)
const stableLoading = ref(false)
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const dispatchForm = reactive({
  repairTeamId: null,
  faultType: 'OTHER',
  remark: ''
})

const canDispatch = computed(() => {
  const status = complaintInfo.value.status
  return status === 'CREATED' || status === 'PROCESSING'
})

const canClose = computed(() => {
  const status = complaintInfo.value.status
  return status === 'REPAIRED' || status === 'VISITED'
})

const canConfirmStable = computed(() => {
  const c = complaintInfo.value
  return c.stationId && c.metricStable !== 1 && c.status !== 'CLOSED'
})

const canActuallyClose = computed(() => {
  const c = complaintInfo.value
  const metricOk = !c.stationId || c.metricStable === 1
  const visitOk = c.status === 'VISITED' || !c.repairOrderId
  return metricOk && visitOk
})

const showCloseTip = computed(() => {
  return canClose.value && !canActuallyClose.value
})

const closeTipText = computed(() => {
  const tips = []
  const c = complaintInfo.value
  if (c.stationId && c.metricStable !== 1) {
    tips.push('站点指标未回稳')
  }
  if (c.repairOrderId && c.status !== 'VISITED') {
    tips.push('未完成回访')
  }
  return tips.join('、') + '，无法关闭'
})

const getPriorityText = (priority) => {
  const map = { 0: '普通', 1: '较高', 2: '紧急', 3: '最高紧急' }
  return map[priority] || '普通'
}

const getPriorityTagType = (priority) => {
  const map = { 0: 'info', 1: '', 2: 'warning', 3: 'danger' }
  return map[priority] || 'info'
}

const getTypeText = (type) => {
  const map = {
    NOT_HOT: '不暖',
    TEMPERATURE_LOW: '温度低',
    LEAKAGE: '漏水',
    NOISE: '噪音',
    OTHER: '其他'
  }
  return map[type] || type
}

const getStatusText = (status) => {
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

const getOperationText = (type) => {
  const map = {
    CREATE: '创建事件',
    DISPATCH: '派单',
    ACCEPT: '接单',
    REPAIR_START: '开始抢修',
    REPAIR_FINISH: '完成抢修',
    VISIT: '回访',
    CLOSE: '关闭',
    REOPEN: '重开'
  }
  return map[type] || type
}

const getTimelineType = (type) => {
  const map = {
    CREATE: 'primary',
    DISPATCH: 'warning',
    ACCEPT: 'warning',
    REPAIR_START: 'danger',
    REPAIR_FINISH: 'success',
    VISIT: 'info',
    CLOSE: 'info',
    REOPEN: 'warning'
  }
  return map[type] || 'primary'
}

const loadRepairTeams = async () => {
  try {
    const res = await getUserListByRole('REPAIR')
    repairTeamList.value = res || []
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  try {
    const res = await getComplaintDetail(route.params.id)
    complaintInfo.value = res.complaint || {}
    flowLogs.value = res.flowLogs || []
  } catch (e) {
    console.error(e)
  }
}

const goBack = () => {
  router.push('/dispatcher/complaint')
}

const goToRepair = () => {
  if (complaintInfo.value.repairOrderId) {
    console.log('跳转到工单详情', complaintInfo.value.repairOrderId)
  }
}

const handleDispatch = () => {
  dispatchForm.repairTeamId = null
  dispatchForm.faultType = 'OTHER'
  dispatchForm.remark = ''
  dispatchDialogVisible.value = true
}

const confirmDispatch = async () => {
  if (!dispatchForm.repairTeamId) {
    ElMessage.warning('请选择抢修队')
    return
  }
  dispatchLoading.value = true
  try {
    const team = repairTeamList.value.find(t => t.id === dispatchForm.repairTeamId)
    await dispatchComplaint(route.params.id, {
      repairTeamId: dispatchForm.repairTeamId,
      repairTeamName: team?.userName,
      faultType: dispatchForm.faultType,
      dispatcherId: userInfo.userId,
      dispatcherName: userInfo.userName,
      remark: dispatchForm.remark
    })
    ElMessage.success('派单成功')
    dispatchDialogVisible.value = false
    loadData()
  } finally {
    dispatchLoading.value = false
  }
}

const handleClose = () => {
  ElMessageBox.confirm('确定关闭该报冷事件吗？关闭前需确认站点指标已回稳且居民回访已完成。', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await closeComplaint(route.params.id, {
        closeUserId: userInfo.userId,
        closeUserName: userInfo.userName
      })
      ElMessage.success('关闭成功')
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '关闭失败')
    }
  }).catch(() => {})
}

const handleConfirmMetricStable = () => {
  ElMessageBox.confirm('确认该站点指标已回稳？系统将检查近1小时的指标数据。', '确认指标回稳', {
    type: 'warning',
    confirmButtonText: '确认回稳',
    cancelButtonText: '取消'
  }).then(async () => {
    stableLoading.value = true
    try {
      await confirmMetricStable(route.params.id, {
        operatorId: userInfo.userId,
        operatorName: userInfo.userName
      })
      ElMessage.success('站点指标已确认回稳')
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '确认失败')
    } finally {
      stableLoading.value = false
    }
  }).catch(() => {})
}

onMounted(() => {
  loadRepairTeams()
  loadData()
})
</script>

<style scoped>
.complaint-detail {
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

.detail-card {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.complaint-no {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.status-tag {
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-tag.status-primary {
  background: #ecf5ff;
  color: #409eff;
}

.status-tag.status-danger {
  background: #fef0f0;
  color: #f56c6c;
}

.status-tag.status-success {
  background: #f0f9eb;
  color: #67c23a;
}

.status-tag.status-info {
  background: #f4f4f5;
  color: #909399;
}

.timeline-card {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
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
  font-size: 13px;
  color: #606266;
}

.action-bar {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  text-align: right;
}
</style>
