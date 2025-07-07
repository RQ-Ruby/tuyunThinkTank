<template>
  <div class="auth-container">
    <div class="sci-fi-bg">
      <div class="geometric-pattern"></div>
    </div>

    <div class="content-wrapper">
      <!-- 左侧文字区 -->
      <div class="left-content">
        <h1 class="slogan">企业级智能协同云图库</h1>

        <div class="jieshi">
          释放 AI 驱动的创意潜能，
          将灵感转化为精致艺术作品。
          让创作更智能，让设计更出众。
        </div>
        <div class="feature-list">
          <div class="feature-item">
            <div class="icon-wrapper">
              <CloudOutlined class="feature-icon" />
            </div>
            <div class="feature-text">
              <h3>AI智能分类</h3>

            </div>
          </div>

          <div class="feature-item">
            <div class="icon-wrapper">
              <TeamOutlined class="feature-icon" />
            </div>
            <div class="feature-text">
              <h3>团队协作</h3>

            </div>
          </div>

          <div class="feature-item">
            <div class="icon-wrapper">
              <LockOutlined class="feature-icon" />
            </div>
            <div class="feature-text">
              <h3>军工级安全</h3>

            </div>
          </div>

          <div class="feature-item">
            <div class="icon-wrapper">
              <RocketOutlined class="feature-icon" />
            </div>
            <div class="feature-text">
              <h3>极速检索</h3>

            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="right-content">
        <div class="auth-card">
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
            <h2 class="form-title">用户登录</h2>

            <!-- 账号输入 -->
            <a-form-item name="userAccount">
              <a-input
                v-model:value="formState.userAccount"
                placeholder="请输入账号"
                size="large"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <!-- 密码输入 -->
            <a-form-item name="userPassword">
              <a-input-password
                v-model:value="formState.userPassword"
                placeholder="请输入密码"
                size="large"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <!-- 登录按钮 -->
            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                class="submit-btn"
                :loading="loading"
              >
                登录
              </a-button>
            </a-form-item>

            <!-- 底部链接 -->
            <div class="form-footer">
              没有账号？
              <router-link to="/user/register" class="login-link">立即注册</router-link>
            </div>
          </a-form>

          <!-- 统一底部链接 -->
          <div class="footer-links">
            <a href="#">帮助</a>
            <a href="#">隐私</a>
            <a href="#">条款</a>
          </div>
          <p class="copyright">Copyright © 2024 Crispix AI. All rights reserved.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>

import { reactive } from 'vue';
import { userLoginUsingPost } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { CloudOutlined, LockOutlined, RocketOutlined,
  TeamOutlined,UserOutlined } from '@ant-design/icons-vue'



const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',

});


const router = useRouter()
const loginUserStore = useLoginUserStore()
/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLoginUsingPost(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}



</script>




<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', system-ui, sans-serif;
}

/* ==================== 背景样式 ==================== */
.sci-fi-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #6a8ef7 0%, #4b6cf0 100%);
  z-index: 0;
  overflow: hidden;
}

.geometric-pattern {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background-image:
    radial-gradient(circle at 70% 30%, rgba(255,255,255,0.15) 0%, transparent 20%),
    radial-gradient(circle at 30% 70%, rgba(255,255,255,0.1) 0%, transparent 20%),
    linear-gradient(120deg, transparent 50%, rgba(255,255,255,0.03) 50%),
    linear-gradient(60deg, transparent 50%, rgba(255,255,255,0.03) 50%);
  transform: rotate(30deg);
  animation: patternMove 25s linear infinite;
}

@keyframes patternMove {
  0% { transform: rotate(30deg) translate(0, 0); }
  25% { transform: rotate(30deg) translate(-5%, 5%); }
  50% { transform: rotate(30deg) translate(0, 0); }
  75% { transform: rotate(30deg) translate(5%, -5%); }
  100% { transform: rotate(30deg) translate(0, 0); }
}

/* ==================== 内容区域 ==================== */
.content-wrapper {
  position: relative;
  z-index: 2;
  display: flex;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 100vh;
  align-items: center;
}

/* ==================== 左侧内容区 ==================== */
.left-content {
  flex: 1;
  padding-right: 200px;
  color: white;
  position: relative;
  animation: fadeInLeft 0.8s ease-out;
  /* 新增定位调整 */
  margin: -150px 0 0 -200px;
  transform: translateY(-4%);
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式调整 */
@media (max-width: 992px) {
  .left-content {
    padding-right: 0;
    margin: 0 0 60px 0 !important; /* 移动端重置定位 */
    transform: none;
  }
}

/* 品牌标识 */
.brand {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  margin-bottom: 30px;
  position: relative;
  display: inline-block;
}

.brand::after {
  content: "";
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 50px;
  height: 3px;
  background: linear-gradient(to right, #fff, transparent);
  border-radius: 2px;
}

/* 大标题 */
.slogan {
  font-size: 48px;
  font-weight: 900;
  line-height: 1.15;
  margin: 40px 0 20px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  position: relative;
  letter-spacing: -0.5px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  background: linear-gradient(to bottom, #fff, rgba(255, 255, 255, 0.95));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  max-width: 90%;
}

/* 描述文字 */
.jieshi {
  font-size: 20px;
  line-height: 1.6;
  margin-bottom: 50px;
  max-width: 85%;
  position: relative;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  font-weight: 300;
}

.jieshi::before {
  content: "";
  position: absolute;
  left: -15px;
  top: 0;
  height: 100%;
  width: 3px;
  background: linear-gradient(to bottom, #32c5ff, #b620e0);
  border-radius: 2px;
}

/* 特性列表 */
.feature-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  padding: 20px;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transform: perspective(500px) translateZ(0);
  will-change: transform, box-shadow;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: perspective(500px) translateZ(20px) translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #6a8ef7, #4b6cf0);
  border-radius: 14px;
  margin-right: 20px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.feature-item:hover .icon-wrapper {
  transform: scale(1.1) rotate(5deg);
}

.feature-icon {
  font-size: 28px;
  color: white;
}

.feature-text h3 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 6px 0;
  letter-spacing: 0.3px;
  background: linear-gradient(to bottom, #fff, rgba(255, 255, 255, 0.95));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.feature-text p {
  font-size: 16px;
  margin: 0;
  line-height: 1.4;
  opacity: 0.85;
  font-weight: 300;
}

/* ==================== 右侧注册卡片 ==================== */
.right-content {
  min-width: 440px;
  z-index: 3;
  animation: fadeInRight 0.8s ease-out;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.auth-card {
  padding: 48px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24px;
  box-shadow:
    0 16px 40px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(0, 0, 0, 0.05),
    0 0 0 4px rgba(106, 142, 247, 0.1);
  animation: cardEnter 0.6s cubic-bezier(0.22, 1, 0.36, 1);
  position: relative;
  overflow: hidden;
}

.auth-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(106, 142, 247, 0.05) 0%, transparent 70%);
  z-index: 0;
}

.form-title {
  color: #1a1a1a;
  font-size: 24px;
  font-weight: 600;
  text-align: left;
  margin: 0 0 32px 0;
  position: relative;
  z-index: 1;
}

/* ==================== 表单元素 ==================== */
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #f0f2f5;
}

.background-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 380px;
  background: linear-gradient(135deg, #4a90e2 0%, #8e54e9 100%);
}

/* 输入框样式 */
:deep(.ant-input) {
  border-radius: 12px !important;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  height: 48px;
  padding-left: 40px !important;
  position: relative;
  z-index: 1;
}

:deep(.ant-input):hover {
  border-color: #6a8ef7;
  box-shadow: 0 0 0 2px rgba(106, 142, 247, 0.2);
}

:deep(.ant-input):focus {
  border-color: #4b6cf0;
  box-shadow: 0 0 0 3px rgba(75, 108, 240, 0.2);
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 12px !important;
  overflow: hidden;
}

.input-icon {
  color: #6a8ef7;
  font-size: 16px;
  opacity: 0.8;
}

/* 按钮样式 */
.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #4a90e2, #8e54e9);
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #6a8ef7, #4b6cf0);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.submit-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(106, 142, 247, 0.4);
}

.submit-btn:hover::before {
  opacity: 1;
}

.submit-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(106, 142, 247, 0.3);
}

/* ==================== 底部链接 ==================== */
.form-footer {
  text-align: center;
  margin-top: 30px;
  color: #666;

  /* 响应式调整 */
  @media (max-width: 992px) {
    .left-content {
      padding-right: 0;
      margin: 0 0 60px 0 !important; /* 移动端重置定位 */
      transform: none;
    }
  }  font-size: 15px;
  position: relative;
  z-index: 1;

  /* 响应式调整 */
  @media (max-width: 992px) {
    .left-content {
      padding-right: 0;
      margin: 0 0 60px 0 !important; /* 移动端重置定位 */
      transform: none;
    }
  }}

.login-link {
  color: #4b6cf0;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
}

.login-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #4b6cf0;
  transition: width 0.3s ease;
}

.login-link:hover {
  opacity: 0.9;
  color: #3a5bd9;
  text-decoration: none;
}

.login-link:hover::after {
  width: 100%;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 50px;
  color: rgba(0, 0, 0, 0.7);
}

.footer-links a {
  color: inherit;
  font-size: 13px;
  transition: color 0.2s, transform 0.2s;
  position: relative;
}

.footer-links a::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: #6a8ef7;
  transition: width 0.3s ease;
}

.footer-links a:hover {
  color: #4b6cf0;
  text-decoration: none;
  transform: translateY(-2px);
}

.footer-links a:hover::after {
  width: 100%;
}

.copyright {
  text-align: center;
  color: rgba(0, 0, 0, 0.7);
  font-size: 13px;
  margin-top: 20px;
  opacity: 0.8;
}

/* ==================== 动画效果 ==================== */
@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 992px) {
  .content-wrapper {
    flex-direction: column;
    padding: 40px 20px 80px;
  }

  .left-content {
    padding-right: 0;
    margin-bottom: 60px;
    text-align: center;
    animation: none;
    opacity: 1;
    transform: none;
  }

  .jieshi::before {
    display: none;
  }

  .feature-item {
    justify-content: center;
  }

  .right-content {
    min-width: auto;
    width: 100%;
    max-width: 500px;
    animation: none;
    opacity: 1;
    transform: none;
  }

  .slogan {
    font-size: 36px;
    max-width: 100%;
    text-align: center;
    margin-left: auto;
    margin-right: auto;
  }

  .jieshi {
    max-width: 100%;
    text-align: center;
  }
}

@media (max-width: 576px) {
  .auth-card {
    padding: 32px 24px;
  }

  .slogan {
    font-size: 28px;
  }

  .icon-wrapper {
    width: 48px;
    height: 48px;
    margin-right: 15px;
  }

  .feature-icon {
    font-size: 24px;
  }

  .feature-list {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .form-title {
    font-size: 20px;
    margin-bottom: 24px;
  }

  :deep(.ant-input) {
    height: 44px;
  }

  .submit-btn {
    height: 48px;
  }
}
</style>
