<template>
  <a-modal
    class="AIOutPicture"
    v-model:visible="visible"
    title="编辑图片"
    :footer="false"
    @cancel="closeModal"
  >
    <!-- 图片裁切组件 -->
    <vue-cropper
      ref="cropperRef"
      :img="imageUrl"
      output-type="png"
      :info="true"
      :can-move-box="true"
      :fixed-box="false"
      :auto-crop="true"
      :center-box="true"
    />
    <div style="margin-bottom: 16px" />
    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft">向左旋转</a-button>
        <a-button @click="rotateRight">向右旋转</a-button>
        <a-button @click="changeScale(1)">放大</a-button>
        <a-button @click="changeScale(-1)">缩小</a-button>
        <a-button type="primary" :loading="loading" @click="handleConfirm">确认</a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import {uploadPictureUsingPost } from '@/api/pictureController'
import { message } from 'ant-design-vue'

interface Props {
  imageUrl?: string
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 获取图片裁切器的引用
const cropperRef = ref()

// 缩放比例
const changeScale = (num) => {
  cropperRef.value?.changeScale(num)
}

// 向左旋转
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
}

// 向右旋转
const rotateRight = () => {
  cropperRef.value.rotateRight()
}

// 确认裁切
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    // blob 为已经裁切好的文件
    const fileName = (props.picture?.name || 'image') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    // 上传图片
    handleUpload({ file })
  })
}

const loading = ref(false)

/**
 * 上传
 * @param file
 */
const handleUpload = async ({ file }: { file: File }) => {
  loading.value = true
  try {
    // 构建上传参数
    const params: API.uploadPictureUsingPOSTParams = {}

    // 如果是编辑已有图片，添加图片ID
    if (props.picture?.id) {
      params.id = props.picture.id
    }

    // 如果有图片名称，使用原名称或生成新名称
    if (props.picture?.name) {
      params.picName = props.picture.name
    }

    // 调用文件上传API
    const res = await uploadPictureUsingPost(params, {}, file)

    // 处理API响应：状态码为0且data存在时视为成功
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('图片上传失败：' + (res.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    message.error('图片上传失败')
  } finally {
    loading.value = false
  }
}


// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
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

.AIOutPicture .vue-cropper {
  height: 400px !important;
}
</style>

