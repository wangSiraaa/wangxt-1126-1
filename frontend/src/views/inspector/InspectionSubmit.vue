<template>
  <div class="inspection-submit">
    <div class="page-header">
      <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="title">巡检记录</span>
    </div>

    <div class="station-card">
      <div class="station-name">{{ stationInfo.stationName }}</div>
      <div class="station-info">
        <el-tag :type="stationInfo.stationType === 'PRIMARY' ? 'danger' : 'success'" size="small">
          {{ stationInfo.stationType === 'PRIMARY' ? '一次网' : '二次网' }}
        </el-tag>
        <span class="station-code">{{ stationInfo.stationCode }}</span>
      </div>
    </div>

    <div class="form-card">
      <div class="section-title">现场读数</div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="压力 (MPa)" prop="pressure">
          <el-input-number
            v-model="form.pressure"
            :precision="4"
            :step="0.01"
            :min="0"
            :max="10"
            style="width: 100%"
            size="large"
            placeholder="请输入现场压力读数"
          />
          <div class="hint" v-if="stationInfo">
            正常范围: {{ stationInfo.pressureLimitMin }} ~ {{ stationInfo.pressureLimitMax }} MPa
          </div>
        </el-form-item>

        <el-form-item label="供水温度 (℃)" prop="supplyTemp">
          <el-input-number
            v-model="form.supplyTemp"
            :precision="2"
            :step="0.5"
            :min="-50"
            :max="200"
            style="width: 100%"
            size="large"
            placeholder="请输入供水温度"
          />
          <div class="hint" v-if="stationInfo">
            正常范围: {{ stationInfo.tempLimitMin }} ~ {{ stationInfo.tempLimitMax }} ℃
          </div>
        </el-form-item>

        <el-form-item label="回水温度 (℃)" prop="returnTemp">
          <el-input-number
            v-model="form.returnTemp"
            :precision="2"
            :step="0.5"
            :min="-50"
            :max="200"
            style="width: 100%"
            size="large"
            placeholder="请输入回水温度"
          />
          <div class="hint" v-if="stationInfo">
            正常范围: {{ stationInfo.tempLimitMin }} ~ {{ stationInfo.tempLimitMax }} ℃
          </div>
        </el-form-item>

        <el-form-item label="巡检备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入巡检备注（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>

    <div class="warn-card" v-if="hasWarning">
      <div class="warn-icon">
        <el-icon><WarningFilled /></el-icon>
      </div>
      <div class="warn-content">
        <div class="warn-title">检测到异常数据</div>
        <div class="warn-item" v-for="(item, index) in warnings" :key="index">
          · {{ item }}
        </div>
        <div class="warn-tip">提交后将自动创建异常记录，严重异常将自动升级为抢修工单</div>
      </div>
    </div>

    <div class="submit-bar">
      <el-button type="primary" size="large" block @click="handleSubmit" :loading="submitLoading">
        提交巡检
      </el-button>
      <div class="submit-tip">* 压力、供水温度、回水温度为必填项，未填写不能提交</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { submitInspectionRecord } from '@/api/record'
import { getRouteDetail } from '@/api/inspection'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitLoading = ref(false)
const stationInfo = ref({})
const routeInfo = ref({})
const routeStationId = ref(null)

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const form = reactive({
  pressure: null,
  supplyTemp: null,
  returnTemp: null,
  remark: '',
  isException: false
})

const rules = {
  pressure: [
    { required: true, message: '请输入压力读数', trigger: 'blur' },
    { type: 'number', message: '请输入有效的数值', trigger: 'blur' }
  ],
  supplyTemp: [
    { required: true, message: '请输入供水温度', trigger: 'blur' },
    { type: 'number', message: '请输入有效的数值', trigger: 'blur' }
  ],
  returnTemp: [
    { required: true, message: '请输入回水温度', trigger: 'blur' },
    { type: 'number', message: '请输入有效的数值', trigger: 'blur' }
  ]
}

const warnings = computed(() => {
  const list = []
  if (!stationInfo.value || form.pressure == null || form.supplyTemp == null || form.returnTemp == null) {
    return list
  }

  const { pressureLimitMin, pressureLimitMax, tempLimitMin, tempLimitMax } = stationInfo.value

  if (form.pressure < pressureLimitMin) {
    list.push(`压力 ${form.pressure} MPa 低于下限 ${pressureLimitMin} MPa`)
  }
  if (form.pressure > pressureLimitMax) {
    list.push(`压力 ${form.pressure} MPa 超过上限 ${pressureLimitMax} MPa`)
  }
  if (form.supplyTemp < tempLimitMin) {
    list.push(`供水温度 ${form.supplyTemp}℃ 低于下限 ${tempLimitMin}℃`)
  }
  if (form.supplyTemp > tempLimitMax) {
    list.push(`供水温度 ${form.supplyTemp}℃ 超过上限 ${tempLimitMax}℃`)
  }
  if (form.returnTemp < tempLimitMin) {
    list.push(`回水温度 ${form.returnTemp}℃ 低于下限 ${tempLimitMin}℃`)
  }
  if (form.returnTemp > tempLimitMax) {
    list.push(`回水温度 ${form.returnTemp}℃ 超过上限 ${tempLimitMax}℃`)
  }

  return list
})

const hasWarning = computed(() => warnings.value.length > 0)

const loadData = async () => {
  try {
    const res = await getRouteDetail(route.params.routeId)
    routeInfo.value = res.route || {}
    const stations = res.stations || []
    const station = stations.find(s => s.id == route.params.stationId)
    if (station) {
      stationInfo.value = station
      routeStationId.value = station.routeStationId || station.id

      if (station.status === 'COMPLETED') {
        form.pressure = station.pressure
        form.supplyTemp = station.supplyTemp
        form.returnTemp = station.returnTemp
        form.remark = station.remark || ''
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const goBack = () => {
  router.back()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请填写完整的现场读数')
      return
    }

    const confirmMsg = hasWarning.value
      ? '检测到异常数据，提交后将创建异常记录，严重异常将自动升级为抢修工单，是否确认提交？'
      : '确认提交本次巡检记录吗？'

    ElMessageBox.confirm(confirmMsg, '提示', {
      type: hasWarning.value ? 'warning' : 'info',
      confirmButtonText: '确认提交',
      cancelButtonText: '取消'
    }).then(async () => {
      submitLoading.value = true
      try {
        await submitInspectionRecord({
          ...form,
          routeId: route.params.routeId,
          routeStationId: routeStationId.value,
          stationId: stationInfo.value.id,
          stationName: stationInfo.value.stationName,
          inspectorId: userInfo.userId,
          inspectorName: userInfo.userName,
          stationType: stationInfo.value.stationType,
          pressureLimitMin: stationInfo.value.pressureLimitMin,
          pressureLimitMax: stationInfo.value.pressureLimitMax,
          tempLimitMin: stationInfo.value.tempLimitMin,
          tempLimitMax: stationInfo.value.tempLimitMax
        })
        ElMessage.success('提交成功')
        router.back()
      } catch (e) {
        ElMessage.error(e.message || '提交失败')
      } finally {
        submitLoading.value = false
      }
    }).catch(() => {})
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.inspection-submit {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 100px;
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

.station-card {
  background: linear-gradient(135deg, #409eff 0%, #1e88e5 100%);
  margin: 12px;
  border-radius: 8px;
  padding: 16px;
  color: #fff;
}

.station-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.station-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.station-code {
  font-size: 12px;
  opacity: 0.8;
}

.form-card {
  background: #fff;
  margin: 12px;
  border-radius: 8px;
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.hint {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.warn-card {
  display: flex;
  gap: 12px;
  background: #fef0f0;
  margin: 12px;
  border-radius: 8px;
  padding: 14px;
  border-left: 4px solid #f56c6c;
}

.warn-icon {
  font-size: 22px;
  color: #f56c6c;
  flex-shrink: 0;
}

.warn-content {
  flex: 1;
}

.warn-title {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
  margin-bottom: 8px;
}

.warn-item {
  font-size: 12px;
  color: #e6a23c;
  margin-bottom: 4px;
  line-height: 1.6;
}

.warn-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #fbc4c4;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px 16px;
  box-shadow: 0 -2px 12px rgba(0,0,0,0.06);
}

.submit-tip {
  text-align: center;
  font-size: 11px;
  color: #909399;
  margin-top: 8px;
}
</style>
