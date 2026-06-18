<template>
  <div class="route-create">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="page-title">创建巡检路线</span>
    </div>

    <div class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="路线名称" prop="routeName">
              <el-input v-model="form.routeName" placeholder="请输入路线名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="巡检员" prop="inspectorId">
              <el-select v-model="form.inspectorId" placeholder="请选择巡检员" style="width: 100%">
                <el-option
                  v-for="item in inspectorList"
                  :key="item.id"
                  :label="item.userName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="计划日期" prop="planDate">
              <el-date-picker v-model="form.planDate" type="date" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路线描述">
              <el-input v-model="form.routeDesc" placeholder="请输入路线描述" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
    </div>

    <div class="station-card">
      <div class="card-header">
        <span class="card-title">选择站点</span>
        <el-button type="primary" size="small" :icon="Plus" @click="stationDialogVisible = true">添加站点</el-button>
      </div>
      <el-table :data="selectedStations" border style="width: 100%" max-height="300">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="stationCode" label="站点编码" width="120" />
        <el-table-column prop="stationName" label="站点名称" width="160" />
        <el-table-column prop="stationType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stationType === 'PRIMARY' ? 'danger' : 'success'" size="small">
              {{ row.stationType === 'PRIMARY' ? '一次网' : '二次网' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="压力范围" width="150">
          <template #default="{ row }">
            {{ row.pressureLimitMin }} ~ {{ row.pressureLimitMax }} MPa
          </template>
        </el-table-column>
        <el-table-column label="温度范围" width="140">
          <template #default="{ row }">
            {{ row.tempLimitMin }} ~ {{ row.tempLimitMax }} ℃
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link size="small" @click="removeStation($index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="tip" v-if="selectedStations.length === 0">
        暂无站点，请点击上方按钮添加站点
      </div>
    </div>

    <div class="submit-bar">
      <el-button @click="goBack">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="submitLoading">保存</el-button>
    </div>

    <el-dialog v-model="stationDialogVisible" title="选择站点" width="700px">
      <div class="station-search">
        <el-input v-model="stationKeyword" placeholder="搜索站点名称/编码" clearable style="width: 250px">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-table :data="stationList" border style="width: 100%" max-height="400" @selection-change="handleStationSelection">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="stationCode" label="站点编码" width="120" />
        <el-table-column prop="stationName" label="站点名称" width="160" />
        <el-table-column prop="stationType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stationType === 'PRIMARY' ? 'danger' : 'success'" size="small">
              {{ row.stationType === 'PRIMARY' ? '一次网' : '二次网' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dutyPerson" label="责任人" width="100" />
      </el-table>
      <template #footer>
        <el-button @click="stationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddStations">确定添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Search } from '@element-plus/icons-vue'
import { createRoute } from '@/api/inspection'
import { getStationList } from '@/api/station'
import { getUserListByRole } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const submitLoading = ref(false)
const stationDialogVisible = ref(false)
const stationKeyword = ref('')
const selectedStationIds = ref([])

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const form = reactive({
  routeName: '',
  routeDesc: '',
  inspectorId: null,
  inspectorName: '',
  planDate: new Date(),
  remark: ''
})

const rules = {
  routeName: [{ required: true, message: '请输入路线名称', trigger: 'blur' }],
  inspectorId: [{ required: true, message: '请选择巡检员', trigger: 'change' }],
  planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }]
}

const inspectorList = ref([])
const stationList = ref([])
const selectedStations = ref([])

const filteredStations = computed(() => {
  if (!stationKeyword.value) return stationList.value
  const keyword = stationKeyword.value.toLowerCase()
  return stationList.value.filter(s =>
    s.stationCode.toLowerCase().includes(keyword) ||
    s.stationName.toLowerCase().includes(keyword)
  )
})

const loadInspectors = async () => {
  try {
    const res = await getUserListByRole('INSPECTOR')
    inspectorList.value = res || []
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

const handleStationSelection = (selection) => {
  selectedStationIds.value = selection.map(s => s.id)
}

const confirmAddStations = () => {
  const newStations = stationList.value.filter(s =>
    selectedStationIds.value.includes(s.id) &&
    !selectedStations.value.find(ss => ss.id === s.id)
  )
  selectedStations.value = [...selectedStations.value, ...newStations]
  stationDialogVisible.value = false
}

const removeStation = (index) => {
  selectedStations.value.splice(index, 1)
}

const goBack = () => {
  router.push('/dispatcher/route')
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (selectedStations.value.length === 0) {
        ElMessage.warning('请至少选择一个站点')
        return
      }

      const inspector = inspectorList.value.find(i => i.id === form.inspectorId)
      submitLoading.value = true
      try {
        const stationIds = selectedStations.value.map(s => s.id)
        await createRoute({
          ...form,
          inspectorName: inspector?.userName,
          dispatcherId: userInfo.userId,
          dispatcherName: userInfo.userName,
          stationIds
        })
        ElMessage.success('创建成功')
        router.push('/dispatcher/route')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadInspectors()
  loadStations()
})
</script>

<style scoped>
.route-create {
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

.form-card {
  background: #fff;
  padding: 24px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.station-card {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.tip {
  padding: 30px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.submit-bar {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  text-align: right;
}

.station-search {
  margin-bottom: 12px;
}
</style>
