<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">
        <h2>换热站巡检抢修系统</h2>
        <p>Heat Station Inspection & Repair System</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" @submit.prevent="handleLogin">
        <el-form-item prop="userCode">
          <el-input v-model="loginForm.userCode" placeholder="请输入用户编码" size="large" prefix-icon="User">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" prefix-icon="Lock" show-password>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" @click="handleLogin" :loading="loading">
          登 录
        </el-button>
      </el-form>
      <div class="login-tips">
        <p>测试账号：</p>
        <p>调度员：dispatcher01 / 123456</p>
        <p>巡检员：inspector01 / 123456</p>
        <p>抢修队：repair01 / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()
const loginForm = reactive({
  userCode: '',
  password: ''
})

const rules = {
  userCode: [{ required: true, message: '请输入用户编码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loading = ref(false)

const handleLogin = async () => {
  try {
    loading.value = true
    const res = await login(loginForm)
    localStorage.setItem('token', res.token)
    localStorage.setItem('userInfo', JSON.stringify({
      userId: res.userId,
      userCode: res.userCode,
      userName: res.userName,
      roleCode: res.roleCode
    }))

    ElMessage.success('登录成功')

    if (res.roleCode === 'DISPATCHER') {
      router.push('/dispatcher/dashboard')
    } else if (res.roleCode === 'INSPECTOR') {
      router.push('/inspector/home')
    } else if (res.roleCode === 'REPAIR') {
      router.push('/repair/home')
    } else {
      router.push('/dispatcher/dashboard')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #1e88e5 0%, #1565c0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 400px;
  background: #fff;
  border-radius: 8px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}

.login-title {
  text-align: center;
  margin-bottom: 32px;
}

.login-title h2 {
  color: #303133;
  font-size: 24px;
  margin-bottom: 8px;
}

.login-title p {
  color: #909399;
  font-size: 12px;
}

.login-tips {
  margin-top: 24px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}
</style>
