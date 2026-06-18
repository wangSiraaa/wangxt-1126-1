<template>
  <div class="route-manage">
    <div class="filter-bar">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="路线状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已下发" value="ASSIGNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="巡检员">
          <el-select v-model="queryForm.inspectorId" placeholder="全部" clearable style="width: 140px">
            <el-option
              v-for="item in inspectorList"
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
        <el-button type="primary" @click="handleCreate" :icon="Plus">创建路线</el-button>
      </div>
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="routeCode" label="路线编号" width="140" />
        <el-table-column prop="routeName" label="路线名称" min-width="150" />
        <el-table-column prop="inspectorName" label="巡检员" width="100" />
        <el-table-column prop="planDate" label="计划日期" width="120" />
        <el-table-column prop="totalStations" label="站点数" width="80" align="center" />
        <el-table-column prop="completedStations" label="已完成" width="80" align="center" />
        <el-table-column prop="exceptionCount" label="异常数" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.exceptionCount > 0" style="color: #f56c6c; font-weight: 600">
              {{ row.exceptionCount }}
            </span>
            <span v-else>{{ row.exceptionCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dispatcherName" label="调度员" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-tag" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
            <el-button type="success" link size="small" @click="handleAssign(row)" v-if="row.status === 'DRAFT'">下发</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)" v-if="row.status === 'DRAFT'">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getRoutePage, assignRoute } from '@/api/inspection'
import { getUserListByRole } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const inspectorList = ref([])
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  inspectorId: ''
})

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

const loadInspectors = async () => {
  try {
    const res = await getUserListByRole('INSPECTOR')
    inspectorList.value = res || []
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (!params.inspectorId) delete params.inspectorId
    const res = await getRoutePage(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.status = ''
  queryForm.inspectorId = ''
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
  router.push('/dispatcher/route/create')
}

const handleDetail = (row) => {
  router.push(`/dispatcher/route/detail/${row.id}`)
}

const handleAssign = (row) => {
  ElMessageBox.confirm('确定要下发该巡检路线吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await assignRoute({
      routeId: row.id,
      dispatcherId: userInfo.userId,
      dispatcherName: userInfo.userName
    })
    ElMessage.success('下发成功')
    loadData()
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该路线吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadInspectors()
  loadData()
})
</script>

<style scoped>
.route-manage {
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
</style>
