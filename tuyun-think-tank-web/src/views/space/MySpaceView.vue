<template>
  <div id="mySpace">
    <!-- 可扩展的加载反馈 -->
    <p v-if="loading">正在跳转，请稍候...</p>
    <p v-else-if="error" style="color: red">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import type { SpaceVO } from '@/types/space'

// 组合式状态
const router = useRouter()
const loginUserStore = useLoginUserStore()

// UI 状态
const loading = ref(true)
const error = ref<string | null>(null)

/**
 * 跳转逻辑：检查用户空间并自动跳转
 */
const redirectToUserSpace = async () => {
  loading.value = true
  error.value = null

  try {
    const loginUser = loginUserStore.loginUser
    if (!loginUser?.id) {
      message.warn('请先登录')
      router.replace('/user/login')
      return
    }

    // 请求用户空间列表（仅取第一页第一条）
    const response = await listSpaceVoByPageUsingPost({
      userId: loginUser.id,
      current: 1,
      pageSize: 1,
    })

    const { code, data, message: errorMsg } = response.data

    if (code === 0 && data?.records?.length > 0) {
      const space: SpaceVO = data.records[0]
      router.replace(`/space/${space.id}`)
    } else {
      router.replace('/add_space')
      message.warn('您还没有创建空间，即将前往创建页面')
    }
  } catch (err: any) {
    error.value = '网络异常，加载空间失败'
    message.error('网络错误，请稍后重试')
    console.error('[MySpace Redirect Error]:', err)
  } finally {
    loading.value = false
  }
}

// 页面挂载后执行跳转逻辑
onMounted(() => {
  redirectToUserSpace()
})
</script>

<style scoped>
#mySpace {
  text-align: center;
  padding: 20px;
  font-size: 16px;
  color: #666;
}
</style>
