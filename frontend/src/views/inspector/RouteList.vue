<template>
  <div class="route-list">
    <div class="page-header">
      <div class="title">我的路线</div>
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
      <div class="route-card" v-for="item in listData" :key="item.id" @click="goDetail(item)">
        <div class="card-top">
          <span class="route-name">{{ item.routeName }}</span>
          <span class="status-tag" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </span>
        </div>
        <div class="card-info">
          <div class="info-item">
            <el-icon><MapLocation /></el-icon>
            <span>{{ item.totalStations || 0 }} 个站点</span>
          </div>
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ item.planDate }}</span>
          </div>
        </div>
        <div class="card-progress" v-if="item.status === 'IN_PROGRESS' || item.status === 'COMPLETED'">
          <div class="progress-info">
            <span>完成进度</span>
            <span>{{ item.completedStations || 0 }}/{{ item.totalStations || 0 }}</span>
          </div>
          <el-progress
            :percentage="getProgress(item)"
            :show-text="false"
            stroke-width="6"
            :color="item.status === 'COMPLETED' ? '#67c23a' : '#409eff'"
          />
        </div>
        <div class="card-footer">
          <span class="dispatcher">调度员: {{ item.dispatcherName }}</span>
        </div>
      </div>

      <div class="empty-state" v-if="listData.length === 0 && !loading">
        <el-empty description="暂无路线数据" :image-size="80" />
      </div>

      <div class="load-more" v-if="hasMore && !loading">
        <el-button type="primary" plain size="small" @click="loadMore">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MapLocation, Calendar } from '@element-plus/icons-vue'
import { getMyRoutes } from '@/api/inspection'

const router = useRouter()
const loading = ref(false)
const listData = ref([])
const activeTab = ref('ALL')
const hasMore = ref(false)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: ''
})

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待开始', value: 'ASSIGNED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' }
]

const getStatusText = (status) => {
  const map = {
    DRAFT: '草稿',
    ASSIGNED: '待开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  const map = {
    DRAFT: 'tag-info',
    ASSIGNED: 'tag-primary',
    IN_PROGRESS: 'tag-warning',
    COMPLETED: 'tag-success',
    CANCELLED: 'tag-info'
  }
  return map[status] || 'tag-info'
}

const getProgress = (item) => {
  if (!item.totalStations) return 0
  return Math.round((item.completedStations / item.totalStations) * 100)
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
    const res = await getMyRoutes(params)
    if (queryForm.pageNum === 1) {
      listData.value = res.records || []
    } else {
      listData.value = [...listData.value, ...(res.records || [])]
    }
    hasMore.value = listData.value.length < (res.total || 0)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  queryForm.pageNum++
  loadData()
}

const goDetail = (item) => {
  router.push(`/inspector/route/${item.id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.route-list {
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

.route-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  cursor: pointer;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.route-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.tag-info {
  background: #f4f4f5;
  color: #909399;
}

.tag-primary {
  background: #ecf5ff;
  color: #409eff;
}

.tag-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.tag-success {
  background: #f0f9eb;
  color: #67c23a;
}

.card-info {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.info-item .el-icon {
  font-size: 14px;
}

.card-progress {
  margin-bottom: 10px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.card-footer {
  padding-top: 10px;
  border-top: 1px solid #f0f2f5;
}

.dispatcher {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  padding: 40px 0;
}

.load-more {
  text-align: center;
  padding: 16px 0;
}
</style>
