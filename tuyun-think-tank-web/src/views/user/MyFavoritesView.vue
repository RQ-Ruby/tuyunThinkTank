<template>
  <div id="myFavoritesPage">
    <a-card title="我的收藏" :bordered="false">
      <PictureList :dataList="dataList" :loading="loading" />
      <a-pagination
        v-if="total > 0"
        style="text-align: right; margin-top: 20px"
        v-model:current="searchParams.current"
        v-model:pageSize="searchParams.pageSize"
        :total="total"
        @change="onPageChange"
      />
      <a-empty v-if="!loading && dataList.length === 0" description="暂无收藏图片" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listMyFavoritePictureByPageUsingPost } from '@/api/pictureFavoriteController'
import { message } from 'ant-design-vue'
import PictureList from '@/components/PictureList.vue'

const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

const searchParams = reactive({
  current: 1,
  pageSize: 12,
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listMyFavoritePictureByPageUsingPost(searchParams)
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    } else {
      message.error(res.data.message || '获取收藏列表失败')
    }
  } catch (error) {
    message.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#myFavoritesPage {
  padding: 24px;
}
</style>