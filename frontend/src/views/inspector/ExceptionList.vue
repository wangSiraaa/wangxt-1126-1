<template>
  <div class="exception-list">
    <div class="page-header">
      <div class="title">异常记录</div>
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
      <div class="exception-card" v-for="item in listData" :key="item.id" @click="goDetail(item)">
        <div class="card-header">
          <span class="exception-no">{{ item.exceptionNo }}</span>
          <el-tag :type="getLevelTagType(item.exceptionLevel)" size="small">
            {{ getLevelText(item.exceptionLevel) }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="label">站点</span>
            <span class="value">{{ item.stationName }}</span>
          </div>
          <div class="info-row">
            <span class="label">类型</span>
            <span class="value">{{ getTypeText(item.exceptionType) }}</span>
          </div>
          <div class="info-row" v-if="item.description">
            <span class="label">描述</span>
            <span class="value desc">{{ item.description }}</span>
          </div>
        </div>
        <div class="card-footer">
          <span class="status-tag" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </span>
          <span class="time">{{ item.reportTime }}</span>
        </div>
        <div class="card-repair" v-if="item.repairOrderNo">
          <el-icon><Tools /></el-icon>
          <span>关联工单: {{ item.repairOrderNo }}</span>
        </div>
      </div>

      <div class="empty-state" v-if="listData.length === 0 && !loading">
        <el-empty description="暂无异常记录" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Tools } from '@element-plus/icons-vue'
import { getMyExceptions } from '@/api/exception'

const loading = ref(false)
const listData = ref([])
const activeTab = ref('ALL')

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: ''
})

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '已上报', value: 'REPORTED' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已关闭', value: 'CLOSED' }
]

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
    REPORTED: 'tag-warning',
    CONFIRMED: 'tag-primary',
    DISPATCHED: 'tag-primary',
    IN_REPAIR: 'tag-danger',
    REPAIRED: 'tag-success',
    CLOSED: 'tag-info'
  }
  return map[status] || 'tag-info'
}

const switchTab = (value) => {
  activeTab.value = value
  queryForm.status = value === 'ALL' ? '' : value
  queryForm.pageNum = 1
  listData.value = []
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (!params.status) delete params.status
    const res = await getMyExceptions(params)
    listData.value = res.records || []
  } finally {
    loading.value = false
  }
}

const goDetail = (item) => {
  console.log('异常详情', item)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.exception-list {
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
  color: #409eff;
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
  background: #409eff;
  border-radius: 2px;
}

.list-content {
  padding: 0 12px;
}

.exception-card {
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

.exception-no {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.card-body {
  padding: 12px 14px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #909399;
  width: 60px;
  flex-shrink: 0;
}

.info-row .value {
  color: #303133;
  flex: 1;
}

.info-row .value.desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #fafafa;
  border-top: 1px solid #f0f2f5;
}

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.tag-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.tag-primary {
  background: #ecf5ff;
  color: #409eff;
}

.tag-danger {
  background: #fef0f0;
  color: #f56c6c;
}

.tag-success {
  background: #f0f9eb;
  color: #67c23a;
}

.tag-info {
  background: #f4f4f5;
  color: #909399;
}

.time {
  font-size: 12px;
  color: #c0c4cc;
}

.card-repair {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #ecf5ff;
  border-top: 1px solid #b3d8ff;
  font-size: 12px;
  color: #409eff;
}

.card-repair .el-icon {
  font-size: 14px;
}

.empty-state {
  padding: 40px 0;
}
</style>
