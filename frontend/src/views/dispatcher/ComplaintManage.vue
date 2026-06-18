<template>
  <div class="complaint-manage">
    <div class="filter-bar">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="事件状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="已创建" value="CREATED" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="抢修中" value="REPAIRING" />
            <el-option label="已修复" value="REPAIRED" />
            <el-option label="已回访" value="VISITED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="报冷类型">
          <el-select v-model="queryForm.complaintType" placeholder="全部" clearable style="width: 140px">
            <el-option label="不暖" value="NOT_HOT" />
            <el-option label="温度低" value="TEMPERATURE_LOW" />
            <el-option label="漏水" value="LEAKAGE" />
            <el-option label="噪音" value="NOISE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="居民/地址" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :icon="Search">查询</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-wrapper">
      <div class="table-header">
        <el-button type="primary" @click="handleCreate" :icon="Plus">新增报冷</el-button>
      </div>
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="complaintNo" label="事件编号" width="140" />
        <el-table-column prop="residentName" label="居民" width="80" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="stationName" label="所属站点" width="130" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip min-width="160" />
        <el-table-column prop="complaintType" label="类型" width="100">
          <template #default="{ row }">
            {{ getTypeText(row.complaintType) }}
          </template>
        </el-table-column>
        <el-table-column prop="indoorTemp" label="室内温度" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.indoorTemp">{{ row.indoorTemp }}℃</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="complaintTime" label="报冷时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-tag" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
            <el-button type="warning" link size="small" @click="handleDispatch(row)" v-if="row.status === 'CREATED' || row.status === 'PROCESSING'">派单</el-button>
            <el-button type="success" link size="small" @click="handleClose(row)" v-if="row.status === 'REPAIRED' || row.status === 'VISITED'">关闭</el-button>
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

    <el-dialog v-model="createDialogVisible" title="新增报冷事件" width="600px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="居民姓名" prop="residentName">
              <el-input v-model="createForm.residentName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="createForm.contactPhone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所属站点" prop="stationId">
          <el-select v-model="createForm.stationId" placeholder="请选择站点" style="width: 100%" filterable>
            <el-option
              v-for="item in stationList"
              :key="item.id"
              :label="item.stationName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="createForm.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="报冷类型" prop="complaintType">
              <el-select v-model="createForm.complaintType" placeholder="请选择" style="width: 100%">
                <el-option label="不暖" value="NOT_HOT" />
                <el-option label="温度低" value="TEMPERATURE_LOW" />
                <el-option label="漏水" value="LEAKAGE" />
                <el-option label="噪音" value="NOISE" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="室内温度">
              <el-input-number v-model="createForm.indoorTemp" :precision="1" :step="0.5" :min="-10" :max="40" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请描述问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate" :loading="createLoading">创建</el-button>
      </template>
    </el-dialog>

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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getComplaintPage,
  createComplaint,
  dispatchComplaint,
  closeComplaint
} from '@/api/complaint'
import { getStationList } from '@/api/station'
import { getUserListByRole } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const createLoading = ref(false)
const dispatchLoading = ref(false)
const createDialogVisible = ref(false)
const dispatchDialogVisible = ref(false)
const createFormRef = ref(null)
const currentComplaint = ref(null)
const tableData = ref([])
const total = ref(0)
const stationList = ref([])
const repairTeamList = ref([])
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  complaintType: '',
  keyword: ''
})

const createForm = reactive({
  residentName: '',
  contactPhone: '',
  stationId: null,
  stationName: '',
  address: '',
  complaintType: '',
  indoorTemp: null,
  description: ''
})

const createRules = {
  residentName: [{ required: true, message: '请输入居民姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  stationId: [{ required: true, message: '请选择站点', trigger: 'change' }],
  complaintType: [{ required: true, message: '请选择报冷类型', trigger: 'change' }]
}

const dispatchForm = reactive({
  repairTeamId: null,
  faultType: 'OTHER',
  remark: ''
})

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

const loadStations = async () => {
  try {
    const res = await getStationList()
    stationList.value = res || []
  } catch (e) {
    console.error(e)
  }
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
  loading.value = true
  try {
    const params = { ...queryForm }
    if (!params.status) delete params.status
    if (!params.complaintType) delete params.complaintType
    if (!params.keyword) delete params.keyword
    const res = await getComplaintPage(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.status = ''
  queryForm.complaintType = ''
  queryForm.keyword = ''
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
    residentName: '',
    contactPhone: '',
    stationId: null,
    stationName: '',
    address: '',
    complaintType: '',
    indoorTemp: null,
    description: ''
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
        await createComplaint({
          ...createForm,
          stationName: station?.stationName,
          handlerId: userInfo.userId,
          handlerName: userInfo.userName
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

const handleDetail = (row) => {
  router.push(`/dispatcher/complaint/detail/${row.id}`)
}

const handleDispatch = (row) => {
  currentComplaint.value = row
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
    await dispatchComplaint(currentComplaint.value.id, {
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

const handleClose = (row) => {
  ElMessageBox.confirm('确定关闭该报冷事件吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await closeComplaint(row.id, {
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

onMounted(() => {
  loadStations()
  loadRepairTeams()
  loadData()
})
</script>

<style scoped>
.complaint-manage {
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
</style>
