<template>
  <div class="login-page">
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
    <div class="login-container">
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

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <div class="form-card">
          <div class="form-header">
            <h2 class="welcome-text">欢迎回来</h2>
            <p class="welcome-subtitle">请登录您的账户继续操作</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <div class="input-group">
                <label class="input-label">用户名</label>
                <div class="input-wrapper">
                  <el-icon class="input-icon"><User /></el-icon>
                  <el-input
                    v-model="form.username"
                    placeholder="请输入用户名"
                    size="large"
                    class="custom-input"
                    clearable
                  />
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-group">
                <label class="input-label">密码</label>
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input
                    v-model="form.password"
                    type="password"
                    placeholder="请输入密码"
                    size="large"
                    class="custom-input"
                    show-password
                    clearable
                  />
                </div>
              </div>
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="rememberMe" class="remember-check">
                <span class="check-label">记住我</span>
              </el-checkbox>
              <el-link type="primary" :underline="'never'" class="forgot-link">
                忘记密码？
              </el-link>
            </div>

            <el-button
              type="primary"
              size="large"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              <span class="btn-text">{{ loading ? '登录中...' : '立即登录' }}</span>
              <el-icon v-if="!loading" class="btn-arrow"><ArrowRight /></el-icon>
            </el-button>
          </el-form>

          <!-- 注册入口 -->
          <div class="register-section">
            <div class="divider">
              <span>还没有账户？</span>
            </div>
            <el-button 
              type="primary" 
              link 
              class="register-link"
              @click="$router.push('/register')"
            >
              <el-icon><Plus /></el-icon>
              <span>立即注册</span>
            </el-button>
          </div>

          <div class="form-footer">
            <p class="copyright">© 2026 智慧林场管理系统 v3.0</p>
            <div class="tech-badges">
              <span class="badge">JWT</span>
              <span class="badge">HTTPS</span>
              <span class="badge">AES-256</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  User, 
  Lock, 
  ArrowRight,
  Plus,
  MapLocation,
  DataLine,
  TrendCharts,
  Monitor
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
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

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  
  try {
    const success = await userStore.userLogin({
      username: form.username,
      password: form.password
    })
    
    if (success) {
      ElMessage.success('登录成功，欢迎回来！')
      const redirect = route.query.redirect || '/'
      window.location.replace(redirect)
    }
  } catch (err) {
    console.error('登录失败:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ========== 页面基础 ========== */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0d3328 0%, #1a5c3f 50%, #2d7d32 100%);
}

/* ========== 森林背景动画 ========== */
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
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

@keyframes particleFall {
  0% {
    transform: translateY(-10px) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.8;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(calc(100vh + 10px)) rotate(360deg);
    opacity: 0;
  }
}

/* ========== 主容器 ========== */
.login-container {
  display: flex;
  width: 1000px;
  height: 600px;
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
  flex: 1;
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
  max-width: 400px;
}

/* Logo 动画 */
.logo-animation {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 25px;
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

.tree-trunk {
  fill: #8d6e63;
}

.tree-leaves-1 {
  fill: #4caf50;
  transform-origin: center bottom;
}

.tree-leaves-2 {
  fill: #66bb6a;
  transform-origin: center bottom;
}

.tree-leaves-3 {
  fill: #81c784;
  transform-origin: center bottom;
}

@keyframes treeFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* 光环效果 */
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

.ring:nth-child(2) {
  animation-delay: 1s;
  border-color: rgba(129, 199, 132, 0.2);
}

.ring:nth-child(3) {
  animation-delay: 2s;
  border-color: rgba(129, 199, 132, 0.1);
}

@keyframes ringExpand {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.3); opacity: 0; }
}

/* 标题 - 清晰的白色 */
.brand-title {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 10px;
  letter-spacing: 12px;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.brand-subtitle {
  font-size: 13px;
  color: #a5d6a7;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-bottom: 35px;
  font-weight: 500;
}

/* 功能特性 */
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 25px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s;
  width: fit-content;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.feature-icon {
  color: #81c784;
  display: flex;
  align-items: center;
}

.feature-text {
  font-size: 13px;
  color: #e8f5e9;
  font-weight: 500;
}

/* ========== 表单区 ========== */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px;
  background: #fff;
}

/* 修复2: 表单卡片宽度改为400px，确保有足够空间 */
.form-card {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-header {
  text-align: center;
  margin-bottom: 10px;
}

.welcome-text {
  font-size: 28px;
  font-weight: 700;
  color: #1b5e20;
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: 14px;
  color: #81c784;
}

/* 输入框组 */
.input-group {
  margin-bottom: 5px;
  width: 100%; /* 确保占满 */
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #2e7d32;
  margin-bottom: 8px;
}

/* 修复1: 输入框包装器宽度改为100% */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%; /* 从 auto 改为 100% */
}

.input-icon {
  position: absolute;
  left: 14px;
  font-size: 18px;
  color: #81c784;
  z-index: 1;
  transition: color 0.3s;
}

:deep(.custom-input .el-input__wrapper) {
  padding-left: 45px !important;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e8f5e9 inset;
  transition: all 0.3s;
  background: #f8faf8;
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
  height: 46px;
  font-size: 14px;
  color: #2e7d32;
}

.input-wrapper:focus-within .input-icon {
  color: #2e7d32;
}

/* 选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 5px 0 10px;
  width: 100%; /* 确保占满 */
}

.remember-check :deep(.el-checkbox__label) {
  padding-left: 6px;
}

.check-label {
  font-size: 13px;
  color: #666;
}

.forgot-link {
  font-size: 13px;
  color: #2e7d32;
  font-weight: 500;
}

.forgot-link:hover {
  color: #1b5e20;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(46, 125, 50, 0.3);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(46, 125, 50, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.btn-text {
  letter-spacing: 1px;
}

.btn-arrow {
  font-size: 18px;
  transition: transform 0.3s;
}

.login-button:hover .btn-arrow {
  transform: translateX(4px);
}

/* 注册区域 */
.register-section {
  text-align: center;
  padding: 15px 0;
  border-top: 1px solid #e8f5e9;
  border-bottom: 1px solid #e8f5e9;
}

.divider {
  position: relative;
  margin-bottom: 10px;
}

.divider span {
  font-size: 13px;
  color: #999;
}

.register-link {
  font-size: 14px;
  color: #2e7d32;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.register-link:hover {
  color: #1b5e20;
}

/* 底部 */
.form-footer {
  text-align: center;
  margin-top: 5px;
}

.copyright {
  font-size: 12px;
  color: #81c784;
  margin-bottom: 10px;
}

.tech-badges {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.badge {
  padding: 3px 8px;
  background: #f1f8e9;
  border-radius: 10px;
  font-size: 10px;
  color: #558b2f;
  font-weight: 600;
  border: 1px solid #dcedc8;
}

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .login-container {
    width: 90%;
    max-width: 900px;
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    width: 95%;
    height: auto;
    max-height: 92vh;
    overflow-y: auto;
    margin: 20px auto;
  }
  
  .brand-section {
    flex: none;
    padding: 30px 20px;
    min-height: 200px;
  }
  
  .brand-title {
    font-size: 32px;
    letter-spacing: 6px;
  }
  
  .brand-subtitle {
    font-size: 11px;
  }
  
  .brand-features {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
  }
  
  .feature-item {
    padding: 6px 12px;
  }
  
  .feature-text {
    font-size: 11px;
  }
  
  .form-section {
    padding: 30px 24px;
  }
  
  .form-card {
    max-width: 100%; /* 移动端占满 */
    padding: 0;
  }
  
  .welcome-text {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 0;
    align-items: flex-start;
    padding-top: 10px;
    padding-bottom: 10px;
    height: auto;
    min-height: 100vh;
    overflow-y: auto;
  }
  
  .login-container {
    width: 100%;
    margin: 0;
    border-radius: 0;
    max-height: none;
    box-shadow: none;
  }
  
  .brand-section {
    min-height: 160px;
    padding: 24px 16px;
  }
  
  .logo-animation {
    width: 70px;
    height: 70px;
    margin-bottom: 16px;
  }
  
  .brand-title {
    font-size: 26px;
    letter-spacing: 4px;
    margin-bottom: 6px;
  }
  
  .brand-subtitle {
    font-size: 10px;
    margin-bottom: 20px;
  }
  
  .brand-features {
    display: none;
  }
  
  .form-section {
    padding: 24px 20px;
  }
  
  .welcome-text {
    font-size: 22px;
    margin-bottom: 6px;
  }
  
  .welcome-subtitle {
    font-size: 13px;
  }
  
  /* 增大触摸目标 */
  :deep(.custom-input .el-input__inner) {
    height: 48px;
    font-size: 16px;
  }
  
  .input-wrapper {
    min-height: 48px;
  }
  
  .login-button {
    height: 48px;
    font-size: 16px;
  }
  
  .form-options {
    margin: 8px 0 16px;
  }
  
  .register-section {
    padding: 12px 0;
  }
}

/* 修复3: 添加表单和输入框宽度控制 */
/* 确保表单占满 */
.login-form {
  width: 100%;
}

/* 确保表单项占满 */
.login-form :deep(.el-form-item) {
  width: 100%;
}

.login-form :deep(.el-form-item__content) {
  width: 100% !important;
}

/* 确保 Element Plus 输入框占满 */
.login-form :deep(.el-input) {
  width: 100% !important;
}

.login-form :deep(.el-input__wrapper) {
  width: 100% !important;
}


</style>