<template>
  <div id="myUploadsPage">
    <a-card title="我的上传" :bordered="false">
      <PictureList :dataList="dataList" :loading="loading" />
      <a-pagination
        v-if="total > 0"
        style="text-align: right; margin-top: 20px"
        v-model:current="searchParams.current"
        v-model:pageSize="searchParams.pageSize"
        :total="total"
        @change="onPageChange"
      />
      <a-empty v-if="!loading && dataList.length === 0" description="暂无上传图片" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listPictureVoByPageUsingPost } from '@/api/pictureController'
import { message } from 'ant-design-vue'
import PictureList from '@/components/PictureList.vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const loginUserStore = useLoginUserStore()
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
  userId: loginUserStore.loginUser.id,
  nullSpaceId: true, // 只查询公共图库
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

const fetchData = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    loading.value = false
    return
  }

  loading.value = true
  searchParams.userId = loginUserStore.loginUser.id
  
  const res = await listPictureVoByPageUsingPost(searchParams)
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#myUploadsPage {
  padding: 24px;
}
</style>