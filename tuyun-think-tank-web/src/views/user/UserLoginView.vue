<template>
  <div class="auth-page">
    <header class="auth-topbar">
      <router-link to="/" class="brand-link">
        <img :src="logoUrl" alt="图云智库" class="brand-logo" />
        <span>图云智库</span>
      </router-link>
      <router-link to="/" class="back-home">
        <ArrowLeftOutlined />
        返回首页
      </router-link>
    </header>

    <main class="auth-shell">
      <section class="story-panel">
        <a-tag color="blue" class="story-tag">VISUAL INSPIRATION PLATFORM</a-tag>
        <h1>把灵感，<br />沉淀成团队的视觉资产。</h1>
        <p class="story-description">
          浏览高质量精选图片，管理个人与团队空间，收藏灵感，并通过 AI 扩图与审核机制，
          完成从发现素材到交付内容的完整链路。
        </p>

        <div class="feature-grid">
          <article class="feature-card">
            <PictureOutlined />
            <div>
              <strong>精选图库</strong>
              <span>多维筛选与高质量视觉素材</span>
            </div>
          </article>
          <article class="feature-card">
            <TeamOutlined />
            <div>
              <strong>团队空间</strong>
              <span>成员角色、权限与协作编辑</span>
            </div>
          </article>
          <article class="feature-card">
            <RobotOutlined />
            <div>
              <strong>AI 扩图</strong>
              <span>为不同媒介快速延展画面</span>
            </div>
          </article>
          <article class="feature-card">
            <SafetyCertificateOutlined />
            <div>
              <strong>审核链路</strong>
              <span>草稿、待审、通过与拒绝</span>
            </div>
          </article>
        </div>

        <div class="story-footnote">
          <CheckCircleFilled />
          演示环境已准备 13 位用户、12 个空间与 120+ 张图片
        </div>
      </section>

      <section class="login-panel">
        <div class="login-card">
          <div class="login-heading">
            <span class="login-kicker">WELCOME BACK</span>
            <h2>登录图云智库</h2>
            <p>继续浏览、收藏和管理你的视觉灵感。</p>
          </div>

          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
            <a-form-item name="userAccount">
              <a-input
                v-model:value="formState.userAccount"
                placeholder="请输入账号"
                size="large"
                class="auth-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item name="userPassword">
              <a-input-password
                v-model:value="formState.userPassword"
                placeholder="请输入密码"
                size="large"
                class="auth-input"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <a-button type="primary" html-type="submit" class="submit-btn" :loading="loading">
              登录
              <ArrowRightOutlined />
            </a-button>
          </a-form>

          <div class="demo-box">
            <div class="demo-title">
              <span>演示账号快捷填充</span>
              <small>密码仅用于本地演示环境</small>
            </div>
            <div class="demo-actions">
              <a-button block @click="fillDemo('admin', demoPasswords.admin)">管理员演示</a-button>
              <a-button block @click="fillDemo('linxi', demoPasswords.creator)">创作者演示</a-button>
              <a-button block @click="fillDemo('chenmo', demoPasswords.photographer)">摄影师演示</a-button>
            </div>
          </div>

          <div class="form-footer">
            没有账号？
            <router-link to="/user/register" class="login-link">立即注册</router-link>
          </div>
        </div>

        <p class="auth-copyright">© 2026 图云智库 · 本地测试环境</p>
      </section>
    </main>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  ArrowRightOutlined,
  CheckCircleFilled,
  LockOutlined,
  PictureOutlined,
  RobotOutlined,
  SafetyCertificateOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { userLoginUsingPost } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import logoUrl from '@/assets/logo.png'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const loading = ref(false)

const demoPasswords = {
  admin: import.meta.env.VITE_DEMO_ADMIN_PASSWORD ?? '',
  creator: import.meta.env.VITE_DEMO_CREATOR_PASSWORD ?? '',
  photographer: import.meta.env.VITE_DEMO_PHOTOGRAPHER_PASSWORD ?? '',
}

const fillDemo = (account: string, password: string) => {
  formState.userAccount = account
  formState.userPassword = password
  if (!password) {
    message.warning('演示密码未配置，请在本地 .env.local 中设置')
    return
  }
  message.info('演示账号已填充，点击登录即可')
}

const handleSubmit = async (values: API.UserLoginRequest) => {
  loading.value = true
  try {
    const res = await userLoginUsingPost(values)
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success(`欢迎回来，${res.data.data.userName ?? res.data.data.userAccount}`)
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
      await router.replace(redirect.startsWith('/') ? redirect : '/')
    } else {
      message.error('登录失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('登录异常：' + (error?.message ?? '请稍后重试'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  padding: 0 32px 32px;
  color: #172033;
  background:
    radial-gradient(circle at 15% 12%, rgba(87, 151, 255, 0.18), transparent 32%),
    radial-gradient(circle at 88% 82%, rgba(54, 208, 181, 0.16), transparent 34%),
    linear-gradient(145deg, #f5f8ff 0%, #ffffff 52%, #f0fbf8 100%);
}

.auth-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1440px;
  height: 76px;
  margin: 0 auto;
}

.brand-link,
.back-home {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #24334c;
}

.brand-link {
  font-size: 18px;
  font-weight: 800;
}

.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 10px;
}

.back-home {
  padding: 9px 14px;
  color: #65728a;
  border: 1px solid rgba(66, 99, 154, 0.12);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
}

.auth-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(420px, 0.82fr);
  gap: 44px;
  align-items: center;
  max-width: 1320px;
  min-height: calc(100vh - 128px);
  margin: 0 auto;
}

.story-panel {
  padding: 20px 0 40px;
}

.story-tag {
  margin-bottom: 20px;
  padding: 5px 12px;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1.7px;
  border: 0;
  border-radius: 999px;
}

.story-panel h1 {
  max-width: 760px;
  margin: 0;
  color: #17233b;
  font-size: clamp(44px, 5vw, 76px);
  font-weight: 800;
  line-height: 1.12;
  letter-spacing: -3px;
}

.story-description {
  max-width: 680px;
  margin: 25px 0 0;
  color: #66748c;
  font-size: 16px;
  line-height: 1.95;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  max-width: 720px;
  margin-top: 34px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 17px 18px;
  border: 1px solid rgba(65, 101, 158, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 8px 24px rgba(54, 83, 129, 0.06);
  backdrop-filter: blur(8px);
}

.feature-card > :first-child {
  flex: none;
  color: #2f6fed;
  font-size: 22px;
}

.feature-card div {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.feature-card strong {
  color: #26354e;
  font-size: 14px;
}

.feature-card span {
  color: #8a96aa;
  font-size: 11px;
}

.story-footnote {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 24px;
  color: #61718a;
  font-size: 12px;
}

.story-footnote > :first-child {
  color: #16a58d;
}

.login-panel {
  position: relative;
}

.login-card {
  padding: 34px;
  border: 1px solid rgba(67, 100, 153, 0.1);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 26px 70px rgba(50, 78, 124, 0.14);
  backdrop-filter: blur(16px);
}

.login-heading {
  margin-bottom: 26px;
}

.login-kicker {
  display: block;
  margin-bottom: 9px;
  color: #3577e8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 2.2px;
}

.login-heading h2 {
  margin: 0;
  color: #1d2b45;
  font-size: 28px;
}

.login-heading p {
  margin: 9px 0 0;
  color: #8491a5;
  font-size: 13px;
}

.auth-input {
  height: 49px;
  border-radius: 13px;
}

.auth-input :deep(.ant-input-affix-wrapper),
.auth-input:deep(.ant-input-affix-wrapper) {
  height: 49px;
  border-radius: 13px;
}

.input-icon {
  color: #6f809b;
}

.submit-btn {
  width: 100%;
  height: 49px;
  margin-top: 6px;
  font-weight: 800;
  border: 0;
  border-radius: 13px;
  background: linear-gradient(96deg, #3069e8, #16a58d);
  box-shadow: 0 12px 24px rgba(47, 111, 237, 0.24);
}

.demo-box {
  margin-top: 25px;
  padding: 16px;
  border: 1px solid rgba(72, 106, 160, 0.1);
  border-radius: 15px;
  background: #f7faff;
}

.demo-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.demo-title span {
  color: #485a73;
  font-size: 12px;
  font-weight: 700;
}

.demo-title small {
  color: #a0aabb;
  font-size: 10px;
}

.demo-actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.demo-actions :deep(.ant-btn) {
  height: 34px;
  padding-inline: 5px;
  color: #59708f;
  font-size: 11px;
  border-color: rgba(72, 106, 160, 0.12);
  border-radius: 10px;
  background: #fff;
}

.form-footer {
  margin-top: 22px;
  color: #8b97a9;
  font-size: 13px;
  text-align: center;
}

.login-link {
  margin-left: 4px;
  color: #3577e8;
  font-weight: 700;
}

.auth-copyright {
  margin: 18px 0 0;
  color: #a2acbb;
  font-size: 11px;
  text-align: center;
}

@media (max-width: 1000px) {
  .auth-shell {
    grid-template-columns: 1fr;
    gap: 20px;
    max-width: 720px;
    padding: 28px 0 40px;
  }

  .story-panel {
    text-align: center;
  }

  .story-panel h1,
  .story-description,
  .feature-grid,
  .story-footnote {
    margin-right: auto;
    margin-left: auto;
  }

  .feature-grid {
    text-align: left;
  }
}

@media (max-width: 640px) {
  .auth-page {
    padding: 0 16px 24px;
  }

  .auth-topbar {
    height: 66px;
  }

  .brand-link {
    font-size: 16px;
  }

  .brand-logo {
    width: 32px;
    height: 32px;
  }

  .back-home {
    padding: 7px 11px;
    font-size: 12px;
  }

  .story-panel h1 {
    font-size: 40px;
    letter-spacing: -1.8px;
  }

  .story-description {
    font-size: 14px;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .login-card {
    padding: 24px 18px;
    border-radius: 20px;
  }

  .demo-actions {
    grid-template-columns: 1fr;
  }
}
</style>