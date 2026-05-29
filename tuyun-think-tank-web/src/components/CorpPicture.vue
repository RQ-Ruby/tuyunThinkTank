<template>
  <a-modal
    class="AIOutPicture"
    v-model:open="open"
    title="编辑图片"
    :footer="false"
    @cancel="closeModal"
  >
    <!-- 协同编辑提示 -->
    <div v-if="isTeamSpace" class="collaboration-info">
      <a-alert
        v-if="editingUser && editingUser.id !== loginUser?.id"
        :message="`${editingUser.userName} 正在编辑图片`"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      <a-alert
        v-else-if="editingUser && editingUser.id === loginUser?.id"
        message="你正在编辑图片"
        type="success"
        show-icon
        style="margin-bottom: 16px"
      />
    </div>

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

    <!-- 协同编辑操作 -->
    <div class="image-edit-actions" v-if="isTeamSpace">
      <a-space>
        <a-button v-if="editingUser" disabled>{{ editingUser.userName }}正在编辑</a-button>
        <a-button v-if="canEnterEdit" type="primary" ghost @click="enterEdit">进入编辑</a-button>
        <a-button v-if="canExitEdit" danger ghost @click="exitEdit">退出编辑</a-button>
      </a-space>
    </div>

    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft" :disabled="!canEdit">向左旋转</a-button>
        <a-button @click="rotateRight" :disabled="!canEdit">向右旋转</a-button>
        <a-button @click="changeScale(1)" :disabled="!canEdit">放大</a-button>
        <a-button @click="changeScale(-1)" :disabled="!canEdit">缩小</a-button>
        <a-button type="primary" :loading="loading" @click="handleConfirm" :disabled="!canEdit">
          确认
        </a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, onUnmounted } from 'vue'
import { uploadPictureUsingPost } from '@/api/pictureController'
import { message } from 'ant-design-vue'
import PictureEditWebSocket from '@/util/PictureEditWebSocket'
import { PICTURE_EDIT_MESSAGE_TYPE_ENUM, PICTURE_EDIT_ACTION_ENUM } from '@/constants/picyure'
import { SPACE_TYPE_ENUM } from '@/constants/space'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

interface Props {
  imageUrl?: string
  picture?: API.PictureVO
  spaceId?: number
  space?: API.SpaceVO
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 获取登录用户
const loginUserStore = useLoginUserStore()
const loginUser = computed(() => loginUserStore.loginUser)

// 获取图片裁切器的引用
const cropperRef = ref()

// 是否为团队空间
const isTeamSpace = computed(() => {
  return props.space?.spaceType === SPACE_TYPE_ENUM.TEAM
})

// 正在编辑的用户
const editingUser = ref<API.UserVO>()

// 可以编辑
const canEdit = computed(() => {
  // 不是团队空间，则默认可编辑
  if (!isTeamSpace.value) {
    return true
  }
  return editingUser.value?.id === loginUser.value?.id
})

// 可以进入编辑状态
const canEnterEdit = computed(() => {
  return isTeamSpace.value && !editingUser.value
})

// 可以退出编辑状态
const canExitEdit = computed(() => {
  return isTeamSpace.value && editingUser.value?.id === loginUser.value?.id
})

// WebSocket 实例
let websocket: PictureEditWebSocket | null = null

// 初始化 WebSocket
const initWebsocket = () => {
  // 释放旧连接
  if (websocket) {
    websocket.disconnect()
    websocket = null
  }

  // 团队空间才初始化
  if (!isTeamSpace.value || !props.picture?.id) {
    return
  }

  // 创建 WebSocket 连接
  websocket = new PictureEditWebSocket(props.picture.id)
  websocket.connect()

  // 监听连接成功事件
  websocket.on('open', () => {
    console.log('WebSocket 连接成功')
  })

  // 监听通知消息
  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.INFO, (msg) => {
    console.log('收到通知消息：', msg)
    message.info(msg.message)
  })

  // 监听错误消息
  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ERROR, (msg) => {
    console.log('收到错误消息：', msg)
    message.error(msg.message)
  })

  // 监听进入编辑状态消息
  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT, (msg) => {
    console.log('收到进入编辑状态消息：', msg)
    message.info(msg.message)
    editingUser.value = msg.user
  })

  // 监听编辑操作消息
  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION, (msg) => {
    console.log('收到编辑操作消息：', msg)
    message.info(msg.message)
    switch (msg.editAction) {
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT:
        cropperRef.value.rotateLeft()
        break
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT:
        cropperRef.value.rotateRight()
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_IN:
        cropperRef.value.changeScale(1)
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT:
        cropperRef.value.changeScale(-1)
        break
    }
  })

  // 监听退出编辑状态消息
  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT, (msg) => {
    console.log('收到退出编辑状态消息：', msg)
    message.info(msg.message)
    editingUser.value = undefined
  })
}

watchEffect(() => {
  initWebsocket()
})

onUnmounted(() => {
  // 断开连接
  if (websocket) {
    websocket.disconnect()
  }
  editingUser.value = undefined
})

// 进入编辑状态
const enterEdit = () => {
  if (websocket) {
    // 发送进入编辑状态的消息
    websocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT,
    })
  }
}

// 退出编辑状态
const exitEdit = () => {
  if (websocket) {
    // 发送退出编辑状态的消息
    websocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT,
    })
  }
}

// 编辑图片操作
const editAction = (action: string) => {
  if (websocket) {
    // 发送编辑操作的请求
    websocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION,
      editAction: action,
    })
  }
}

// 缩放比例
const changeScale = (num: number) => {
  if (!canEdit.value) {
    return
  }
  cropperRef.value?.changeScale(num)
  if (isTeamSpace.value) {
    if (num > 0) {
      editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_IN)
    } else {
      editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT)
    }
  }
}

// 向左旋转
const rotateLeft = () => {
  if (!canEdit.value) {
    return
  }
  cropperRef.value.rotateLeft()
  if (isTeamSpace.value) {
    editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT)
  }
}

// 向右旋转
const rotateRight = () => {
  if (!canEdit.value) {
    return
  }
  cropperRef.value.rotateRight()
  if (isTeamSpace.value) {
    editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT)
  }
}

// 确认裁切
const handleConfirm = () => {
  if (!canEdit.value) {
    message.warning('当前无法编辑，请先进入编辑状态')
    return
  }
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

// 弹窗开关
const open = ref(false)

// 打开弹窗
const openModal = () => {
  open.value = true
}

// 关闭弹窗
const closeModal = () => {
  open.value = false
  // 断开连接
  if (websocket) {
    websocket.disconnect()
  }
  editingUser.value = undefined
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

.collaboration-info {
  margin-bottom: 16px;
}

.image-edit-actions {
  text-align: center;
}

.image-cropper-actions {
  text-align: center;
}
</style>