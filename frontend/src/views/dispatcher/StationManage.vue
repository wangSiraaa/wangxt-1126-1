<template>
  <div class="station-manage">
    <div class="filter-bar">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="站点名称">
          <el-input v-model="queryForm.keyword" placeholder="请输入站点名称/编码" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="站点类型">
          <el-select v-model="queryForm.stationType" placeholder="全部" clearable style="width: 140px">
            <el-option label="一次网" value="PRIMARY" />
            <el-option label="二次网" value="SECONDARY" />
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
        <el-button type="primary" @click="handleAdd" :icon="Plus">新增站点</el-button>
      </div>
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="stationCode" label="站点编码" width="120" />
        <el-table-column prop="stationName" label="站点名称" width="140" />
        <el-table-column prop="stationAddress" label="地址" show-overflow-tooltip />
        <el-table-column prop="stationType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stationType === 'PRIMARY' ? 'danger' : 'success'" size="small">
              {{ row.stationType === 'PRIMARY' ? '一次网' : '二次网' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="压力范围(MPa)" width="140">
          <template #default="{ row }">
            {{ row.pressureLimitMin }} ~ {{ row.pressureLimitMax }}
          </template>
        </el-table-column>
        <el-table-column label="温度范围(℃)" width="130">
          <template #default="{ row }">
            {{ row.tempLimitMin }} ~ {{ row.tempLimitMax }}
          </template>
        </el-table-column>
        <el-table-column prop="dutyPerson" label="责任人" width="100" />
        <el-table-column prop="dutyPhone" label="电话" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑站点' : '新增站点'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="站点编码" prop="stationCode">
              <el-input v-model="form.stationCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="站点名称" prop="stationName">
              <el-input v-model="form.stationName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="站点地址" prop="stationAddress">
          <el-input v-model="form.stationAddress" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="站点类型" prop="stationType">
              <el-select v-model="form.stationType" style="width: 100%">
                <el-option label="一次网" value="PRIMARY" />
                <el-option label="二次网" value="SECONDARY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序号" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="压力下限" prop="pressureLimitMin">
              <el-input-number v-model="form.pressureLimitMin" :precision="4" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="压力上限" prop="pressureLimitMax">
              <el-input-number v-model="form.pressureLimitMax" :precision="4" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="温度下限" prop="tempLimitMin">
              <el-input-number v-model="form.tempLimitMin" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度上限" prop="tempLimitMax">
              <el-input-number v-model="form.tempLimitMax" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="责任人" prop="dutyPerson">
              <el-input v-model="form.dutyPerson" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="dutyPhone">
              <el-input v-model="form.dutyPhone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getStationPage, saveStation, deleteStation } from '@/api/station'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  stationType: ''
})

const form = reactive({
  id: null,
  stationCode: '',
  stationName: '',
  stationAddress: '',
  stationType: 'PRIMARY',
  pressureLimitMin: 0.6,
  pressureLimitMax: 1.2,
  tempLimitMin: 60,
  tempLimitMax: 95,
  dutyPerson: '',
  dutyPhone: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  stationCode: [{ required: true, message: '请输入站点编码', trigger: 'blur' }],
  stationName: [{ required: true, message: '请输入站点名称', trigger: 'blur' }],
  stationType: [{ required: true, message: '请选择站点类型', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStationPage(queryForm)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.keyword = ''
  queryForm.stationType = ''
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

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    stationCode: '',
    stationName: '',
    stationAddress: '',
    stationType: 'PRIMARY',
    pressureLimitMin: 0.6,
    pressureLimitMax: 1.2,
    tempLimitMin: 60,
    tempLimitMax: 95,
    dutyPerson: '',
    dutyPhone: '',
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await saveStation(form)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadData()
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该站点吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteStation(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.station-manage {
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
