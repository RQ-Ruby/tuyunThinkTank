<template>
  <a-modal
    class="AIOutPicture"
    v-model:open="open"
    title="AI扩图"
    :footer="false"
    @cancel="closeModal"
  >
    <a-row :gutter="24">
      <!-- 原始图像 -->
      <a-col :span="12">
        <div class="image-container">
          <h4>原始图像
            <span v-if="props.picture?.picWidth && props.picture?.picHeight" class="image-info">
              ({{ props.picture.picWidth }}×{{ props.picture.picHeight }}px)
            </span>
          </h4>
          <img :src="props.picture?.url" class="preview-image" />
          <div v-if="props.picture?.picWidth && props.picture?.picHeight" class="size-tips">
            <a-tag v-if="isValidSize" color="green">尺寸符合要求</a-tag>
            <a-tag v-else color="red">尺寸不符合要求</a-tag>
            <div class="tips-text">
              支持尺寸：512px-4096px，长宽比不超过4:1
            </div>
          </div>
        </div>
      </a-col>

      <!-- 生成图像 -->
      <a-col :span="12">
        <div class="image-container">
          <h4>生成结果
            <a-button
              type="link"
              @click="handleGenerate"
              :loading="loading"
              :disabled="!isValidSize"
            >
              {{ generatedImageUrl ? '重新生成' : '开始生成' }}
            </a-button>
          </h4>
          <img
            v-if="generatedImageUrl"
            :src="generatedImageUrl"
            class="preview-image"
          />
          <div v-else class="empty-preview">
            <a-empty description="点击生成按钮获取结果" />
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <a-space :size="24">
<!--        <a-button
          type="primary"
          @click="handleGenerate"
          :loading="loading"
        >
          生成结果
        </a-button>-->
        <a-button
          type="primary"
          @click="handleApply"
          :disabled="!generatedImageUrl"
        >
          应用结果
        </a-button>
      </a-space>
    </div>

  </a-modal>
</template>

<script lang="ts" setup>
import { onUnmounted, ref, computed } from 'vue'
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet,
  uploadPictureByUrlUsingPost
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

const loading = ref(false)
//任务id
const taskId =ref<string>()
// 生成的图片URL
const generatedImageUrl = ref<string>()

// 计算图片尺寸是否符合要求
const isValidSize = computed(() => {
  if (!props.picture?.picWidth || !props.picture?.picHeight) {
    return false
  }

  const { picWidth, picHeight } = props.picture
  const minSize = 512
  const maxSize = 4096

  // 检查尺寸范围
  if (picWidth < minSize || picHeight < minSize || picWidth > maxSize || picHeight > maxSize) {
    return false
  }

  // 检查长宽比
  const aspectRatio = Math.max(picWidth, picHeight) / Math.min(picWidth, picHeight)
  if (aspectRatio > 4) {
    return false
  }

  return true
})


const handleGenerate =async ()  =>{
  if (!props.picture) {
    message.error('请先上传图片')
    return
  }

  // 检查图片尺寸
  const { picWidth, picHeight } = props.picture

  if (!picWidth || !picHeight) {
    message.error('无法获取图片尺寸信息')
    console.error('图片尺寸信息缺失:', { picWidth, picHeight })
    return
  }

  // 图片尺寸验证 - 根据AI扩图服务的要求
  const minSize = 512 // 最小尺寸512px
  const maxSize = 4096 // 最大尺寸4096px

  if (picWidth < minSize || picHeight < minSize || picWidth > maxSize || picHeight > maxSize) {
    message.error(`图片尺寸不符合要求，请上传尺寸在${minSize}px到${maxSize}px之间的图片。当前尺寸：${picWidth}x${picHeight}px`)
    return
  }

  // 检查图片比例 - 避免过于极端的长宽比
  const aspectRatio = Math.max(picWidth, picHeight) / Math.min(picWidth, picHeight)

  if (aspectRatio > 4) {
    message.error(`图片长宽比过于极端（${aspectRatio.toFixed(1)}:1），建议使用长宽比不超过4:1的图片`)
    return
  }

  loading.value = true

  try {
    // 准备API请求参数
    const requestParams = {
      pictureId: props.picture.id,
      //需要设置的扩图参数
      parameters:{
        xScale: 2,
        yScale: 2,
        limitImageSize: true, // 限制输出图像文件大小，避免尺寸错误
        bestQuality: false, // 关闭最佳质量模式以提高速度
        addWatermark: false // 不添加水印
      }
    }

    // 调用文件上传API
    const res = await createPictureOutPaintingTaskUsingPost(requestParams)

    // 处理API响应：状态码为0且data存在时视为成功
    if (res.data.code === 0 && res.data.data) {
      message.success('图片正在扩图，请耐心等待，不要退出界面')
      taskId.value = res.data.data.output?.taskId
      //开启轮询
      startPolling()
    } else {
      message.error('图片扩图失败：' + (res.data.message || '未知错误'))
      loading.value = false
    }
  } catch (error) {
    message.error('创建扩图任务失败，请检查网络连接或稍后重试')
    loading.value = false
  }
}

// 轮询定时器
let pollingTimer: number | null = null

// 清理轮询定时器
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = undefined
  }
}

// 开始轮询
const startPolling = () => {
  if (!taskId.value) return

  pollingTimer = setInterval(async () => {
    try {
      // 调用文件上传API
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: taskId.value,
      })

      // 处理API响应：状态码为0且data存在时视为成功
      if (res.data.code === 0 && res.data.data) {
        const taskResult = res.data.data.output

        if (taskResult?.taskStatus === 'SUCCEEDED') {
          message.success('图片扩图成功')
          generatedImageUrl.value = taskResult.outputImageUrl
          loading.value = false
          //清理轮询
          clearPolling()
        } else if (taskResult?.taskStatus === 'FAILED') {
          // 构建详细的错误信息，包含错误代码和消息
          let errorMsg = '未知错误'
          if (taskResult?.code && taskResult?.message) {
            errorMsg = `${taskResult.code}: ${taskResult.message}`
          } else if (taskResult?.message) {
            errorMsg = taskResult.message
          } else if (res.data.message) {
            errorMsg = res.data.message
          }

          console.error('扩图任务失败:', taskResult)
          message.error('图片扩图失败: ' + errorMsg)
          loading.value = false
          //清理轮询
          clearPolling()
        }
        // 如果状态是RUNNING或其他状态，继续轮询
      } else {
        message.error('获取任务状态失败：' + (res.data.message || '未知错误'))
        loading.value = false
        clearPolling()
      }
    }
    catch (error) {
      message.error('轮询图片扩图失败：' + (error || '未知错误'))
      loading.value = false
      //清理轮询
      clearPolling()
    }
}, 3000) // 每隔 3 秒轮询一次
}

// 清理定时器，避免内存泄漏
onUnmounted(() => {
  clearPolling()
})


const uploadLoading = ref<boolean>(false)

const handleApply = async () => {
  uploadLoading.value = true
  try {
    const params: API.PictureUploadRequest = {
      url: generatedImageUrl.value
    }
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    uploadLoading.value = false
  }
}

// 是否可见
const open = ref(false)
// 打开弹窗
const openModal = () => {
  open.value = true
}

// 关闭弹窗
const closeModal = () => {
  open.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal,
})
</script>

<style>
.AIOutPicture {
  text-align: center;
}

.image-container {
  text-align: center;
}

.image-info {
  font-size: 12px;
  color: #666;
  font-weight: normal;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.size-tips {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.tips-text {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.action-buttons {
  margin-top: 24px;
  text-align: center;
}

.empty-preview {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}
</style>
