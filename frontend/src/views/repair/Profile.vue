<template>
  <div class="profile-page">
    <div class="header">
      <el-avatar :size="64" class="avatar">
        {{ userInfo.userName?.charAt(0) || 'R' }}
      </el-avatar>
      <div class="user-info">
        <div class="name">{{ userInfo.userName }}</div>
        <div class="role">抢修队</div>
      </div>
    </div>

    <div class="stats">
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalCount }}</div>
        <div class="stat-label">总工单</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.processingCount }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.completedCount }}</div>
        <div class="stat-label">已完成</div>
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-item" @click="handleMenu('profile')">
        <el-icon><User /></el-icon>
        <span>个人信息</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="handleMenu('password')">
        <el-icon><Lock /></el-icon>
        <span>修改密码</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="handleMenu('about')">
        <el-icon><InfoFilled /></el-icon>
        <span>关于</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="logout-btn">
      <el-button type="danger" block @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, InfoFilled, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const stats = reactive({
  totalCount: 0,
  processingCount: 0,
  completedCount: 0
})

const handleMenu = (type) => {
  ElMessage.info(`功能开发中: ${type}`)
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.replace('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.header {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  padding: 30px 20px 60px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  background: rgba(255,255,255,0.2);
  border: 3px solid rgba(255,255,255,0.3);
  margin-bottom: 12px;
}

.user-info {
  text-align: center;
}

.name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.role {
  font-size: 13px;
  opacity: 0.85;
}

.stats {
  background: #fff;
  margin: -30px 16px 16px;
  border-radius: 10px;
  padding: 18px 10px;
  display: flex;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.menu-list {
  background: #fff;
  margin: 0 16px 16px;
  border-radius: 8px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item .el-icon {
  font-size: 20px;
  color: #e74c3c;
  margin-right: 12px;
}

.menu-item span {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.menu-item .arrow {
  font-size: 14px;
  color: #c0c4cc;
  margin-right: 0;
}

.logout-btn {
  padding: 0 16px;
  margin-top: 24px;
}
</style>
