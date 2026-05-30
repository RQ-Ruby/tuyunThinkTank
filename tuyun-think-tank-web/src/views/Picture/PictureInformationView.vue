<template>
  <div id="PictureInformationView">
    <a-row :gutter="[24, 24]">
      <a-col :sm="24" :md="16" :xl="18">
        <a-card :bordered="false" class="preview-card">
          <div class="image-wrapper">
            <a-image
              :src="picture.url"
              :preview="true"
              class="preview-image"
            />
          </div>
        </a-card>
      </a-col>
      <a-col :sm="24" :md="8" :xl="6">
        <a-card title="图片信息" class="info-card">
          <a-descriptions :column="1" class="descriptions">
            <a-descriptions-item label="作者">
              <a-space>
                <a-avatar :size="24" :src="picture.user?.userAvatar" />
                <div>{{ picture.user?.userName }}</div>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="名称">
              {{ picture.name ?? '未命名' }}
            </a-descriptions-item>
            <a-descriptions-item label="简介">
              {{ picture.introduction ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="分类">
              {{ picture.category ?? '默认' }}
            </a-descriptions-item>
            <a-descriptions-item label="标签">
              <a-tag v-for="tag in picture.tags" :key="tag">
                {{ tag }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="格式">
              {{ picture.picFormat ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="宽度">
              {{ picture.picWidth ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="高度">
              {{ picture.picHeight ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="宽高比">
              {{ picture.picScale ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="大小">
              {{ formatSize(picture.picSize) }}
            </a-descriptions-item>
          </a-descriptions>

          <a-space wrap class="action-buttons">
            <a-button type="primary" @click="doDownload" class="action-btn">
              <template #icon><DownloadOutlined class="btn-icon" /></template>
              下载
            </a-button>
            <a-button
              :class="['action-btn', 'favorite-btn', { 'is-favorite': isFavorite }]"
              :title="isFavorite ? '取消收藏' : '收藏图片'"
              @click="toggleFavorite"
            >
              <template #icon><HeartOutlined v-if="!isFavorite" class="btn-icon" /><HeartFilled v-else class="btn-icon" /></template>
              {{ isFavorite ? '' : '' }}
            </a-button>
            <a-button v-if="canEdit" @click="doEdit" class="action-btn">
              <template #icon><EditOutlined class="btn-icon" /></template>
              编辑
            </a-button>
            <a-button v-if="canEdit" danger @click="doDelete" class="action-btn">
              <template #icon><DeleteOutlined class="btn-icon" /></template>
              删除
            </a-button>
            <!-- 新增管理员审核按钮 -->
<!--            <a-button
              v-if="isAdmin"
              type="primary"
              ghost
              @click="doReview(PIC_REVIEW_STATUS_ENUM.PASS)"
              class="action-btn">
              <template #icon><CheckOutlined class="btn-icon" /></template>
              通过
            </a-button>-->
            <a-button
              v-if="isAdmin"
              danger
              @click="doReview(PIC_REVIEW_STATUS_ENUM.REJECT)"
              class="action-btn">
              <template #icon><CloseOutlined class="btn-icon" /></template>
              拒绝
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>



<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { deletePictureUsingPost, doPictureReviewUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController'
import { addFavoriteUsingPost, removeFavoriteUsingPost, checkFavoriteUsingGet } from '@/api/pictureFavoriteController'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import router from '@/router'
import { downloadImage, formatSize } from '@/util'
import { PIC_REVIEW_STATUS_ENUM } from '@/constants/picyure'
import { HeartOutlined, HeartFilled } from '@ant-design/icons-vue'
/*图片下载  */
const doDownload = () => {
  downloadImage(picture.value.url)
}
//增管理员判断
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 审核方法
const doReview = async (reviewStatus: number) => {
  try {
    const res = await doPictureReviewUsingPost({
      id: picture.value.id,
      reviewStatus,
      reviewMessage: reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '审核通过' : '审核拒绝'
    })
    if (res.data.code === 0) {
      message.success(`已${reviewStatus === 1 ? '通过' : '拒绝'}审核`)
      router.push('/')
      fetchPictureDetail() // 刷新详情数据
    }
  } catch (e) {
    message.error('审核操作失败')
  }
}


/*判断是否具有编辑权限（基于权限列表）*/
const loginUserStore = useLoginUserStore()
// 是否具有编辑权限
const canEdit = computed(() => {
  const loginUser = loginUserStore.loginUser;
  // 未登录不可编辑
  if (!loginUser.id) {
    return false
  }
  // 优先使用权限列表（团队空间 / 私有空间）
  const permissionList = picture.value.permissionList ?? []
  if (permissionList.length > 0) {
    return permissionList.includes('picture:edit')
  }
  // 公共图库：仅本人或管理员可编辑
  const user = picture.value.user || {}
  return loginUser.id === user.id || loginUser.userRole === 'admin'
})

/* 编辑  */
const doEdit = () => {
  router.push('/add_picture?id=' + picture.value.id)
}
// 删除
const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
  } else {
    message.error('删除失败')
  }
}


// 使用 Vue 3 的 defineProps 函数声明组件接收的属性
const props = defineProps<{
  id: string | number  // 声明一个名为 id 的 prop，支持字符串或数字类型
}>()
const picture = ref<API.PictureVO>({})

// 收藏功能
const isFavorite = ref(false)

// 检查是否已收藏
const checkFavorite = async () => {
  if (!loginUserStore.loginUser.id) {
    isFavorite.value = false
    return
  }
  try {
    const res = await checkFavoriteUsingGet(props.id)
    if (res.data.code === 0) {
      isFavorite.value = res.data.data || false
    }
  } catch (error) {
    console.error('检查收藏状态失败', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    return
  }

  const pictureId = props.id

  try {
    if (isFavorite.value) {
      // 取消收藏
      const res = await removeFavoriteUsingPost(pictureId)
      if (res.data.code === 0) {
        message.success('已取消收藏')
        isFavorite.value = false
      } else {
        message.error(res.data.message || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const res = await addFavoriteUsingPost(pictureId)
      if (res.data.code === 0) {
        message.success('收藏成功')
        isFavorite.value = true
      } else {
        message.error(res.data.message || '收藏失败')
      }
    }
  } catch (error) {
    message.error('操作失败')
  }
}

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id as any,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchPictureDetail()
  checkFavorite()
})

</script>

<style scoped>
#PictureInformationView {
}
 .preview-card {
   box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
   border-radius: 12px;
 }

.image-wrapper {
  padding: 8px;
  background: #fff;
  border-radius: 8px;
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.05);
}

.preview-image {
  max-height: 70vh;
  border-radius: 4px;
  object-fit: contain;
  transition: transform 0.2s;
}

.info-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.info-card :deep(.ant-card-head-title) {
  font-size: 18px;
  font-weight: 500;
  position: relative;
  padding-left: 12px;
}

.info-card :deep(.ant-card-head-title)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  background: #1890ff;
  border-radius: 2px;
}

.descriptions :deep(.ant-descriptions-item-label) {
  color: #666;
  min-width: 60px;
}

.descriptions :deep(.ant-descriptions-item-content) {
  color: #333;
}

#PictureInformationView .action-buttons {
  margin-top: 24px;
  gap: 16px !important;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  padding: 10px 12px;
  font-size: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-btn {
  width: 48px;
  height: 48px;
  padding: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-btn:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.action-btn:active {
  transform: translateY(0) scale(0.95);
}

.favorite-btn {
  color: #ff4d4f;
  border-color: #d9d9d9;
  background: #fff;
}

.favorite-btn:hover {
  color: #ff4d4f;
  border-color: #ff4d4f;
  background: rgba(255, 77, 79, 0.06);
}

.favorite-btn.is-favorite {
  color: #ff4d4f;
  border-color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

.favorite-btn.is-favorite:hover {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.16);
}

.favorite-btn :deep(.anticon) {
  display: inline-flex;
}

.btn-icon {
  font-size: 20px;
  margin-right: 0;
}

a-tag {
  margin: 4px;
}


.action-btn.ant-btn-danger {
  background: rgba(255, 77, 79, 0.1);
}

</style>
