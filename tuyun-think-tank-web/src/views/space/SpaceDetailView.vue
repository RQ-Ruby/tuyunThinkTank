<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <a-flex justify="space-between">
      <h2>{{ space.spaceName }}（{{ SPACE_TYPE_MAP[space.spaceType] }}）</h2>
      <a-space size="middle">
        <a-button
          v-if="canUpload"
          type="primary"
          :href="`/add_picture?spaceId=${id}`"
          target="_blank"
        >
          + 创建图片
        </a-button>
        <a-button
          v-if="canManageUser"
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          成员管理
        </a-button>
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress
            type="circle"
            :size="42"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <PictureSearchForm :onSearch="onSearch" />
    <div style="margin-bottom: 16px" />
    <!-- 图片列表 -->
    <PictureList :dataList="dataList" :loading="loading" />
    <!-- 分页 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import {
  listPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { formatSize } from '@/util'
import { SPACE_TYPE_MAP, SPACE_PERMISSION_ENUM } from '@/constants/space'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const router = useRouter()
const space = ref<API.SpaceVO>({})

// 权限判断
const canUpload = computed(() => {
  return (space.value.permissionList ?? []).includes(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
})
const canManageUser = computed(() => {
  return (space.value.permissionList ?? []).includes(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
})

// -------- 获取空间详情 --------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      // 空间不存在（可能是已删除残留链接）—— 提示并跳走，避免卡死
      message.error('获取空间详情失败，' + (res.data.message ?? '空间不存在'))
      router.replace('/')
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
    router.replace('/')
  }
}

// --------- 获取图片列表 --------

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// 页面加载时获取数据
onMounted(() => {
  fetchSpaceDetail()
  fetchData()
})

// 监听路由参数变化，切换空间时重新加载
watch(
  () => props.id,
  () => {
    searchParams.value.current = 1
    fetchSpaceDetail()
    fetchData()
  },
)

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

</script>

<style scoped>
#spaceDetailPage {
  margin-bottom: 16px;
}
</style>
