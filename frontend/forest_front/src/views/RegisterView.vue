<!-- /src/views/RegisterView.vue -->
<template>
  <div class="register-page">
    <!-- 动态森林背景 -->
    <div class="forest-background">
      <div class="bg-layer mountains"></div>
      <div class="bg-layer trees"></div>
      <div class="bg-layer fog"></div>
    </div>

    <!-- 浮动粒子效果 -->
    <div class="particles">
      <span v-for="n in 20" :key="n" class="particle" :style="getParticleStyle(n)"></span>
    </div>

    <!-- 主容器 -->
    <div class="register-container">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-animation">
            <div class="tree-icon">
              <svg viewBox="0 0 100 100" class="tree-svg">
                <path class="tree-trunk" d="M45 100 L55 100 L55 60 L45 60 Z" />
                <path class="tree-leaves-1" d="M50 20 L20 60 L80 60 Z" />
                <path class="tree-leaves-2" d="M50 40 L25 75 L75 75 Z" />
                <path class="tree-leaves-3" d="M50 10 L30 45 L70 45 Z" />
              </svg>
            </div>
            <div class="rings">
              <span v-for="n in 3" :key="n" class="ring"></span>
            </div>
          </div>

          <h1 class="brand-title">智慧林场</h1>
          <p class="brand-subtitle">Smart Forest Management Platform</p>

          <div class="brand-features">
            <div class="feature-item" v-for="(feature, index) in features" :key="index">
              <div class="feature-icon">
                <el-icon :size="18">
                  <component :is="feature.icon" />
                </el-icon>
              </div>
              <span class="feature-text">{{ feature.text }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="form-section">
        <div class="form-card">
          <div class="form-header">
            <h2 class="welcome-text">创建账户</h2>
            <p class="welcome-subtitle">填写以下信息完成注册</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="register-form"
            @keyup.enter="handleRegister"
          >
            <!-- 第一行：用户名 + 真实姓名 -->
            <div class="form-row">
              <el-form-item prop="username" class="half-width">
                <div class="input-group">
                  <label class="input-label">用户名</label>
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><User /></el-icon>
                    <el-input
                      v-model="form.username"
                      placeholder="3-20位字母数字"
                      size="large"
                      class="custom-input"
                      clearable
                    />
                  </div>
                </div>
              </el-form-item>

              <el-form-item prop="realName" class="half-width">
                <div class="input-group">
                  <label class="input-label">真实姓名</label>
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><UserFilled /></el-icon>
                    <el-input
                      v-model="form.realName"
                      placeholder="请输入真实姓名"
                      size="large"
                      class="custom-input"
                      clearable
                    />
                  </div>
                </div>
              </el-form-item>
            </div>

            <!-- 第二行：密码 + 确认密码 -->
            <div class="form-row">
              <el-form-item prop="password" class="half-width">
                <div class="input-group">
                  <label class="input-label">密码</label>
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Lock /></el-icon>
                    <el-input
                      v-model="form.password"
                      type="password"
                      placeholder="6-20位密码"
                      size="large"
                      class="custom-input"
                      show-password
                      clearable
                    />
                  </div>
                </div>
              </el-form-item>

              <el-form-item prop="confirmPassword" class="half-width">
                <div class="input-group">
                  <label class="input-label">确认密码</label>
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Lock /></el-icon>
                    <el-input
                      v-model="form.confirmPassword"
                      type="password"
                      placeholder="再次输入密码"
                      size="large"
                      class="custom-input"
                      show-password
                      clearable
                    />
                  </div>
                </div>
              </el-form-item>
            </div>

            <!-- 第三行：邮箱 + 手机 -->
            <div class="form-row">
              <el-form-item prop="email" class="half-width">
                <div class="input-group">
                  <label class="input-label">电子邮箱</label>
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Message /></el-icon>
                    <el-input
                      v-model="form.email"
                      placeholder="example@email.com"
                      size="large"
                      class="custom-input"
                      clearable
                    />
                  </div>
                </div>
              </el-form-item>

              <el-form-item prop="phone" class="half-width">
                <div class="input-group">
                  <label class="input-label">手机号码</label>
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Phone /></el-icon>
                    <el-input
                      v-model="form.phone"
                      placeholder="11位手机号"
                      size="large"
                      class="custom-input"
                      clearable
                    />
                  </div>
                </div>
              </el-form-item>
            </div>

            <!-- 协议勾选 -->
            <el-form-item prop="agreement" class="agreement-item">
              <el-checkbox v-model="form.agreement" class="agreement-check">
                <span class="check-text">
                  我已阅读并同意
                  <el-link type="primary" :underline="'never'" @click.prevent="showAgreement">
                    用户协议
                  </el-link>
                  和
                  <el-link type="primary" :underline="'never'" @click.prevent="showPrivacy">
                    隐私政策
                  </el-link>
                </span>
              </el-checkbox>
            </el-form-item>

            <!-- 注册按钮 - 与输入框对齐 -->
            <el-button
              type="primary"
              size="large"
              class="register-button"
              :loading="loading"
              @click="handleRegister"
            >
              <span class="btn-text">{{ loading ? '注册中...' : '立即注册' }}</span>
              <el-icon v-if="!loading" class="btn-arrow"><ArrowRight /></el-icon>
            </el-button>
          </el-form>

          <!-- 返回登录 - 居中 -->
          <div class="login-section">
            <el-button 
              type="primary" 
              link 
              class="login-link"
              @click="$router.push('/login')"
            >
              <el-icon><ArrowLeft /></el-icon>
              <span>已有账户？返回登录</span>
            </el-button>
          </div>

          <div class="form-footer">
            <p class="copyright">© 2026 智慧林场管理系统 v3.0</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, 
  UserFilled,
  Lock, 
  ArrowRight,
  ArrowLeft,
  Message,
  Phone,
  MapLocation,
  DataLine,
  TrendCharts,
  Monitor
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  agreement: false
})

// 自定义验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度6-20位'))
  } else {
    if (form.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请同意用户协议'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度3-20位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能字母数字下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度2-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式错误', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

const features = [
  { icon: MapLocation, text: 'GIS 林分可视化' },
  { icon: DataLine, text: '实时数据监测' },
  { icon: TrendCharts, text: '智能生长预测' },
  { icon: Monitor, text: '多维度报表分析' }
]

const getParticleStyle = (n) => {
  const left = Math.random() * 100
  const delay = Math.random() * 15
  const duration = 10 + Math.random() * 10
  const size = 2 + Math.random() * 4
  
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size}px`
  }
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  
  try {
    const registerData = {
      username: form.username,
      realName: form.realName,
      password: form.password,
      confirmPassword: form.confirmPassword,
      email: form.email,
      phone: form.phone
    }
    
    await register(registerData)
    
    ElMessage.success('注册成功！请登录')
    router.push('/login')
    
  } catch (err) {
    console.error('注册失败:', err)
    ElMessage.error(err.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const showAgreement = () => {
  ElMessageBox.alert('用户协议内容...', '用户协议', { confirmButtonText: '我知道了' })
}

const showPrivacy = () => {
  ElMessageBox.alert('隐私政策内容...', '隐私政策', { confirmButtonText: '我知道了' })
}
</script>

<style scoped>
/* ========== 页面基础 ========== */
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0d3328 0%, #1a5c3f 50%, #2d7d32 100%);
  padding: 20px;
}

/* ========== 森林背景 ========== */
.forest-background {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-layer {
  position: absolute;
  inset: 0;
  opacity: 0.15;
}

.mountains {
  background: 
    radial-gradient(ellipse at 50% 100%, rgba(0,0,0,0.4) 0%, transparent 60%),
    linear-gradient(180deg, transparent 60%, rgba(13, 51, 40, 0.8) 100%);
  animation: mountainMove 30s ease-in-out infinite;
}

.trees {
  background-image: 
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 100px,
      rgba(46, 125, 50, 0.1) 100px,
      rgba(46, 125, 50, 0.1) 102px
    );
  animation: treeSway 20s ease-in-out infinite;
}

.fog {
  background: 
    radial-gradient(circle at 30% 70%, rgba(255,255,255,0.1) 0%, transparent 40%),
    radial-gradient(circle at 70% 30%, rgba(255,255,255,0.08) 0%, transparent 35%);
  animation: fogMove 25s ease-in-out infinite;
}

@keyframes mountainMove {
  0%, 100% { transform: translateX(0) scale(1); }
  50% { transform: translateX(-20px) scale(1.02); }
}

@keyframes treeSway {
  0%, 100% { transform: skewX(0deg); }
  25% { transform: skewX(0.5deg); }
  75% { transform: skewX(-0.5deg); }
}

@keyframes fogMove {
  0%, 100% { transform: translateX(0) translateY(0); }
  33% { transform: translateX(30px) translateY(-10px); }
  66% { transform: translateX(-20px) translateY(5px); }
}

/* ========== 粒子效果 ========== */
.particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.particle {
  position: absolute;
  top: -10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: particleFall linear infinite;
}

@keyframes particleFall {
  0% { transform: translateY(-10px); opacity: 0; }
  10% { opacity: 0.8; }
  90% { opacity: 0.6; }
  100% { transform: translateY(calc(100vh + 10px)); opacity: 0; }
}

/* ========== 主容器 ========== */
.register-container {
  display: flex;
  width: 1000px;
  min-height: 600px;
  max-height: 90vh;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  box-shadow: 
    0 25px 80px -20px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 10;
}

/* ========== 品牌区 ========== */
.brand-section {
  flex: 0 0 420px;
  background: linear-gradient(160deg, #0d4b33 0%, #1b6b3d 50%, #2e7d32 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(129, 199, 132, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(129, 199, 132, 0.1) 0%, transparent 40%);
}

.brand-content {
  text-align: center;
  color: white;
  padding: 40px;
  position: relative;
  z-index: 1;
  width: 100%;
}

.logo-animation {
  position: relative;
  width: 90px;
  height: 90px;
  margin: 0 auto 20px;
}

.tree-icon {
  position: relative;
  z-index: 2;
  animation: treeFloat 3s ease-in-out infinite;
}

.tree-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.3));
}

.tree-trunk { fill: #8d6e63; }
.tree-leaves-1 { fill: #4caf50; transform-origin: center bottom; }
.tree-leaves-2 { fill: #66bb6a; transform-origin: center bottom; }
.tree-leaves-3 { fill: #81c784; transform-origin: center bottom; }

@keyframes treeFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.rings {
  position: absolute;
  inset: -15px;
  z-index: 1;
}

.ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(129, 199, 132, 0.3);
  border-radius: 50%;
  animation: ringExpand 3s ease-out infinite;
}

.ring:nth-child(2) { animation-delay: 1s; border-color: rgba(129, 199, 132, 0.2); }
.ring:nth-child(3) { animation-delay: 2s; border-color: rgba(129, 199, 132, 0.1); }

@keyframes ringExpand {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.3); opacity: 0; }
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: 10px;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans SC", sans-serif;
}

.brand-subtitle {
  font-size: 12px;
  color: #a5d6a7;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-bottom: 30px;
  font-weight: 500;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s;
  width: fit-content;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.feature-icon { color: #81c784; display: flex; align-items: center; }
.feature-text { font-size: 12px; color: #e8f5e9; font-weight: 500; }

/* ========== 表单区 ========== */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px 40px;
  background: #fff;
  overflow-y: auto;
}

.form-card {
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-header {
  text-align: center;
  margin-bottom: 5px;
}

.welcome-text {
  font-size: 26px;
  font-weight: 700;
  color: #1b5e20;
  margin-bottom: 6px;
}

.welcome-subtitle {
  font-size: 13px;
  color: #81c784;
}

/* 表单布局 - 双列 */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-row {
  display: flex;
  gap: 15px;
  width: 100%;
}

.half-width {
  flex: 1;
  margin-bottom: 0;
}

:deep(.half-width .el-form-item__content) {
  width: 100%;
}

.input-group {
  width: 100%;
}

.input-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #2e7d32;
  margin-bottom: 6px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 12px;
  font-size: 16px;
  color: #81c784;
  z-index: 1;
}

:deep(.custom-input .el-input__wrapper) {
  padding-left: 40px !important;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e8f5e9 inset;
  transition: all 0.3s;
  background: #f8faf8;
  width: 100%;
}

:deep(.custom-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #4caf50 inset;
  background: #fff;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #2e7d32 inset;
  background: #fff;
}

:deep(.custom-input .el-input__inner) {
  height: 40px;
  font-size: 13px;
  color: #2e7d32;
}

/* 协议勾选 */
.agreement-item {
  margin: 5px 0;
  width: 100%;
}

.agreement-item :deep(.el-form-item__content) {
  justify-content: center;
}

.agreement-check :deep(.el-checkbox__label) {
  padding-left: 6px;
  font-size: 12px;
  color: #666;
}

.check-text {
  font-size: 12px;
  color: #666;
}

.check-text :deep(.el-link) {
  font-size: 12px;
}

/* 注册按钮 - 与输入框对齐 */
.register-button {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(46, 125, 50, 0.3);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 5px;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(46, 125, 50, 0.4);
}

.btn-text { letter-spacing: 1px; }
.btn-arrow { font-size: 16px; transition: transform 0.3s; }
.register-button:hover .btn-arrow { transform: translateX(4px); }

/* 返回登录 - 居中 */
.login-section {
  display: flex;
  justify-content: center;
  padding: 10px 0;
  border-top: 1px solid #e8f5e9;
  margin-top: 5px;
}

.login-link {
  font-size: 13px;
  color: #2e7d32;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
}

.login-link:hover { color: #1b5e20; }

/* 底部 */
.form-footer {
  text-align: center;
  margin-top: 5px;
}

.copyright {
  font-size: 11px;
  color: #81c784;
}

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .register-container {
    width: 95%;
    max-width: 900px;
  }
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
    height: auto;
    max-height: 95vh;
  }
  
  .brand-section {
    flex: none;
    padding: 25px;
    min-height: 220px;
  }
  
  .brand-title {
    font-size: 32px;
    letter-spacing: 6px;
  }
  
  .form-section {
    padding: 25px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>