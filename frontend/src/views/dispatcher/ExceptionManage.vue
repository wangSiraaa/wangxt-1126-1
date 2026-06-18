<template>
  <div class="exception-manage">
    <div class="filter-bar">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="异常状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="已上报" value="REPORTED" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已派单" value="DISPATCHED" />
            <el-option label="抢修中" value="IN_REPAIR" />
            <el-option label="已修复" value="REPAIRED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="queryForm.exceptionType" placeholder="全部" clearable style="width: 140px">
            <el-option label="压力过高" value="PRESSURE_HIGH" />
            <el-option label="压力过低" value="PRESSURE_LOW" />
            <el-option label="温度过高" value="TEMP_HIGH" />
            <el-option label="温度过低" value="TEMP_LOW" />
            <el-option label="设备故障" value="DEVICE_FAULT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常级别">
          <el-select v-model="queryForm.exceptionLevel" placeholder="全部" clearable style="width: 120px">
            <el-option label="严重" value="CRITICAL" />
            <el-option label="警告" value="WARNING" />
            <el-option label="一般" value="NORMAL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :icon="Search">查询</el-button>
          <el-button @click="resetQuery" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-wrapper">
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="exceptionNo" label="异常编号" width="140" />
        <el-table-column prop="stationName" label="站点" width="130" />
        <el-table-column prop="exceptionType" label="类型" width="100">
          <template #default="{ row }">
            {{ getTypeText(row.exceptionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="exceptionLevel" label="级别" width="90">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.exceptionLevel)" size="small">
              {{ getLevelText(row.exceptionLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="180" />
        <el-table-column prop="reporterName" label="上报人" width="80" />
        <el-table-column prop="reportTime" label="上报时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-tag" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="repairOrderNo" label="关联工单" width="130">
          <template #default="{ row }">
            <span v-if="row.repairOrderNo">{{ row.repairOrderNo }}</span>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleConfirm(row)" v-if="row.status === 'REPORTED'">确认</el-button>
            <el-button type="warning" link size="small" @click="handleDispatch(row)" v-if="row.status === 'CONFIRMED'">派单</el-button>
            <el-button type="success" link size="small" @click="handleClose(row)" v-if="row.status === 'REPAIRED'">关闭</el-button>
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
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getExceptionPage,
  confirmException,
  dispatchException,
  closeException
} from '@/api/exception'
import { getUserListByRole } from '@/api/user'

const loading = ref(false)
const dispatchLoading = ref(false)
const dispatchDialogVisible = ref(false)
const currentException = ref(null)
const tableData = ref([])
const total = ref(0)
const repairTeamList = ref([])
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  exceptionType: '',
  exceptionLevel: ''
})

const dispatchForm = reactive({
  repairTeamId: null,
  remark: ''
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

const getStatusClass = (status) => {
  const map = {
    REPORTED: 'status-warning',
    CONFIRMED: 'status-primary',
    DISPATCHED: 'status-primary',
    IN_REPAIR: 'status-danger',
    REPAIRED: 'status-success',
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

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (!params.status) delete params.status
    if (!params.exceptionType) delete params.exceptionType
    if (!params.exceptionLevel) delete params.exceptionLevel
    const res = await getExceptionPage(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.status = ''
  queryForm.exceptionType = ''
  queryForm.exceptionLevel = ''
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

const handleConfirm = (row) => {
  ElMessageBox.confirm('确认该异常吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await confirmException(row.id, { userId: userInfo.userId, userName: userInfo.userName })
    ElMessage.success('确认成功')
    loadData()
  }).catch(() => {})
}

const handleDispatch = (row) => {
  currentException.value = row
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
    await dispatchException(currentException.value.id, {
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

const handleClose = (row) => {
  ElMessageBox.confirm('确定关闭该异常吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await closeException(row.id, { userId: userInfo.userId, userName: userInfo.userName })
    ElMessage.success('关闭成功')
    loadData()
  }).catch(() => {})
}

const handleDetail = (row) => {
  console.log('详情', row)
}

onMounted(() => {
  loadRepairTeams()
  loadData()
})
</script>

<style scoped>
.exception-manage {
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
