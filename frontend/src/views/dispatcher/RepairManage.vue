<template>
  <div class="repair-manage">
    <div class="filter-bar">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="工单状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="已创建" value="CREATED" />
            <el-option label="已派单" value="DISPATCHED" />
            <el-option label="已接单" value="ACCEPTED" />
            <el-option label="抢修中" value="IN_PROGRESS" />
            <el-option label="已完成" value="FINISHED" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型">
          <el-select v-model="queryForm.faultType" placeholder="全部" clearable style="width: 140px">
            <el-option label="压力异常" value="PRESSURE" />
            <el-option label="温度异常" value="TEMPERATURE" />
            <el-option label="设备故障" value="DEVICE" />
            <el-option label="管网泄漏" value="PIPE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="抢修队">
          <el-select v-model="queryForm.repairTeamId" placeholder="全部" clearable style="width: 140px">
            <el-option
              v-for="item in repairTeamList"
              :key="item.id"
              :label="item.userName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :icon="Search">查询</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-wrapper">
      <div class="table-header">
        <el-button type="primary" @click="handleCreate" :icon="Plus">创建工单</el-button>
      </div>
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="工单号" width="150" />
        <el-table-column prop="stationName" label="站点" width="130" />
        <el-table-column prop="faultType" label="故障类型" width="100">
          <template #default="{ row }">
            {{ getFaultTypeText(row.faultType) }}
          </template>
        </el-table-column>
        <el-table-column prop="faultLevel" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.faultLevel)" size="small">
              {{ getLevelText(row.faultLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" show-overflow-tooltip min-width="180" />
        <el-table-column prop="repairTeamName" label="抢修队" width="100" />
        <el-table-column prop="dispatcherName" label="派单人" width="80" />
        <el-table-column prop="dispatchTime" label="派单时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-tag" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" link size="small" @click="handleDispatch(row)" v-if="row.status === 'CREATED'">派单</el-button>
            <el-button type="success" link size="small" @click="handleConfirm(row)" v-if="row.status === 'FINISHED'">确认</el-button>
            <el-button type="info" link size="small" @click="handleClose(row)" v-if="row.status === 'CONFIRMED'">关闭</el-button>
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pagination"
        :current-page="queryForm.pageNum"
        :page-size="queryForm.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="createDialogVisible" title="创建抢修工单" width="600px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="站点" prop="stationId">
              <el-select v-model="createForm.stationId" placeholder="请选择站点" style="width: 100%" filterable>
                <el-option
                  v-for="item in stationList"
                  :key="item.id"
                  :label="item.stationName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障类型" prop="faultType">
              <el-select v-model="createForm.faultType" placeholder="请选择" style="width: 100%">
                <el-option label="压力异常" value="PRESSURE" />
                <el-option label="温度异常" value="TEMPERATURE" />
                <el-option label="设备故障" value="DEVICE" />
                <el-option label="管网泄漏" value="PIPE" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="故障级别" prop="faultLevel">
              <el-select v-model="createForm.faultLevel" placeholder="请选择" style="width: 100%">
                <el-option label="严重" value="CRITICAL" />
                <el-option label="警告" value="WARNING" />
                <el-option label="一般" value="NORMAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抢修队" prop="repairTeamId">
              <el-select v-model="createForm.repairTeamId" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in repairTeamList"
                  :key="item.id"
                  :label="item.userName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="createForm.faultDesc" type="textarea" :rows="3" placeholder="请输入故障描述" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate" :loading="createLoading">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dispatchDialogVisible" title="派单" width="500px">
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
        <el-form-item label="派单说明">
          <el-input v-model="dispatchForm.remark" type="textarea" :rows="3" placeholder="请输入派单说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDispatch" :loading="dispatchLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getRepairOrderPage,
  createRepairOrder,
  dispatchRepairOrder,
  confirmRepairOrder,
  closeRepairOrder
} from '@/api/repair'
import { getUserListByRole } from '@/api/user'
import { getStationList } from '@/api/station'

const loading = ref(false)
const createLoading = ref(false)
const dispatchLoading = ref(false)
const createDialogVisible = ref(false)
const dispatchDialogVisible = ref(false)
const createFormRef = ref(null)
const currentOrder = ref(null)
const tableData = ref([])
const total = ref(0)
const repairTeamList = ref([])
const stationList = ref([])
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  faultType: '',
  repairTeamId: ''
})

const createForm = reactive({
  stationId: null,
  stationName: '',
  faultType: '',
  faultLevel: 'WARNING',
  faultDesc: '',
  repairTeamId: null,
  repairTeamName: '',
  remark: ''
})

const createRules = {
  stationId: [{ required: true, message: '请选择站点', trigger: 'change' }],
  faultType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  faultLevel: [{ required: true, message: '请选择故障级别', trigger: 'change' }]
}

const dispatchForm = reactive({
  repairTeamId: null,
  remark: ''
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
    DISPATCHED: '已派单',
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
    CREATED: 'status-info',
    DISPATCHED: 'status-primary',
    ACCEPTED: 'status-warning',
    IN_PROGRESS: 'status-danger',
    FINISHED: 'status-success',
    CONFIRMED: 'status-success',
    CLOSED: 'status-info'
  }
  return map[status] || 'status-info'
}

const loadRepairTeams = async () => {
  try {
    const res = await getUserListByRole('REPAIR')
    repairTeamList.value = res || []
  } catch (e) {
    console.error(e)
  }
}

const loadStations = async () => {
  try {
    const res = await getStationList()
    stationList.value = res || []
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (!params.status) delete params.status
    if (!params.faultType) delete params.faultType
    if (!params.repairTeamId) delete params.repairTeamId
    const res = await getRepairOrderPage(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.status = ''
  queryForm.faultType = ''
  queryForm.repairTeamId = ''
  queryForm.pageNum = 1
  loadData()
}

const handlePageChange = (page) => {
  queryForm.pageNum = page
  loadData()
}

const handleSizeChange = (size) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadData()
}

const handleCreate = () => {
  Object.assign(createForm, {
    stationId: null,
    stationName: '',
    faultType: '',
    faultLevel: 'WARNING',
    faultDesc: '',
    repairTeamId: null,
    repairTeamName: '',
    remark: ''
  })
  createDialogVisible.value = true
}

const confirmCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      createLoading.value = true
      try {
        const station = stationList.value.find(s => s.id === createForm.stationId)
        const team = repairTeamList.value.find(t => t.id === createForm.repairTeamId)
        await createRepairOrder({
          ...createForm,
          stationName: station?.stationName,
          repairTeamName: team?.userName,
          dispatcherId: userInfo.userId,
          dispatcherName: userInfo.userName
        })
        ElMessage.success('创建成功')
        createDialogVisible.value = false
        loadData()
      } finally {
        createLoading.value = false
      }
    }
  })
}

const handleDispatch = (row) => {
  currentOrder.value = row
  dispatchForm.repairTeamId = null
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
    await dispatchRepairOrder(currentOrder.value.id, {
      repairTeamId: dispatchForm.repairTeamId,
      repairTeamName: team?.userName,
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

const handleConfirm = (row) => {
  ElMessageBox.confirm('确认该抢修工单已完成？', '提示', {
    type: 'warning'
  }).then(async () => {
    await confirmRepairOrder(row.id, {
      confirmUserId: userInfo.userId,
      confirmUserName: userInfo.userName
    })
    ElMessage.success('确认成功')
    loadData()
  }).catch(() => {})
}

const handleClose = (row) => {
  ElMessageBox.confirm('确定关闭该工单吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await closeRepairOrder(row.id, {
      closeUserId: userInfo.userId,
      closeUserName: userInfo.userName
    })
    ElMessage.success('关闭成功')
    loadData()
  }).catch(() => {})
}

const handleDetail = (row) => {
  console.log('详情', row)
}

onMounted(() => {
  loadRepairTeams()
  loadStations()
  loadData()
})
</script>

<style scoped>
.repair-manage {
  height: 100%;
}

.filter-bar {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.table-wrapper {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

.table-header {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.status-info {
  background: #f4f4f5;
  color: #909399;
}

.status-tag.status-primary {
  background: #ecf5ff;
  color: #409eff;
}

.status-tag.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-tag.status-danger {
  background: #fef0f0;
  color: #f56c6c;
}

.status-tag.status-success {
  background: #f0f9eb;
  color: #67c23a;
}
</style>
