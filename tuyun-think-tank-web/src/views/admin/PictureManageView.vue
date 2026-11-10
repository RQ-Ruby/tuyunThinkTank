<template>
  <div id="PictureManageView">
    <div class="page-header">
      <div>
        <span class="page-kicker">CONTENT REVIEW</span>
        <h2>图片管理</h2>
        <p>审核公开图片、维护元数据，并快速进入创建或批量导入流程。</p>
      </div>
      <a-space wrap>
        <a-button type="primary" href="/add_picture" target="_blank">创建图片</a-button>
        <a-button href="/add_picture/batch" target="_blank">批量创建</a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline" :model="searchParams" class="filter-form" @finish="doSearch">
        <a-form-item label="关键词" name="searchText">
          <a-input
            v-model:value="searchParams.searchText"
            placeholder="从名称和简介搜索"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="类型" name="category">
          <a-input v-model:value="searchParams.category" placeholder="请输入类型" allow-clear />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="searchParams.tags"
            mode="tags"
            placeholder="选择或输入标签"
            class="filter-select"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="审核状态" name="reviewStatus">
          <a-select
            v-model:value="searchParams.reviewStatus"
            :options="PIC_REVIEW_STATUS_OPTIONS"
            placeholder="请选择审核状态"
            class="filter-select"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false" class="table-card">
      <div class="table-summary">
        <span>共 <strong>{{ total }}</strong> 张公开图片</span>
        <span>状态说明：待审核 / 通过 / 拒绝</span>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="paging"
        :scroll="{ x: 1050 }"
        row-key="id"
        class="picture-table"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'picture'">
            <div class="picture-cell">
              <a-image :src="record.thumbnailUrl ?? record.url" :width="78" :height="58" />
              <div class="picture-copy">
                <strong :title="record.name">{{ record.name }}</strong>
                <span :title="record.introduction">{{ record.introduction ?? '暂无简介' }}</span>
                <small>ID {{ String(record.id).slice(-8) }}</small>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'category'">
            <div class="category-cell">
              <a-tag color="blue">{{ record.category ?? '未分类' }}</a-tag>
              <div class="tag-list">
                <a-tag v-for="tag in parseTags(record.tags)" :key="tag">{{ tag }}</a-tag>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'picInfo'">
            <div class="compact-info">
              <strong>{{ record.picWidth }} × {{ record.picHeight }}</strong>
              <span>{{ formatSize(record.picSize) }} · {{ record.picFormat?.toUpperCase() ?? '-' }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'reviewInfo'">
            <div class="review-cell">
              <a-tag :color="getReviewColor(record.reviewStatus)">
                {{ PIC_REVIEW_STATUS_MAP[record.reviewStatus ?? -1] ?? '未审核' }}
              </a-tag>
              <span v-if="record.reviewTime">
                {{ dayjs(record.reviewTime).format('MM-DD HH:mm') }}
              </span>
              <span v-else>{{ record.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '自动审核' : '等待处理' }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'createTime'">
            <div class="time-cell">
              <strong>{{ dayjs(record.createTime).format('YYYY-MM-DD') }}</strong>
              <span>{{ dayjs(record.createTime).format('HH:mm') }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space wrap :size="2">
              <a-button
                v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
                type="link"
                size="small"
                @click="doReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
              >
                通过
              </a-button>
              <a-button
                v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
                type="link"
                size="small"
                danger
                @click="doReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
              >
                拒绝
              </a-button>
              <a-button type="link" size="small" :href="`/add_picture?id=${record.id}`" target="_blank">
                编辑
              </a-button>
              <a-button type="link" size="small" danger @click="doDelete(record.id)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  listPictureByPageUsingPost,
} from '@/api/pictureController'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS,
} from '@/constants/picyure'
import { formatSize } from '@/util'

const columns = [
  { title: '图片', key: 'picture', width: 340 },
  { title: '分类 / 标签', key: 'category', width: 180 },
  { title: '图片信息', key: 'picInfo', width: 150 },
  { title: '审核状态', key: 'reviewInfo', width: 150 },
  { title: '创建时间', key: 'createTime', width: 130 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const },
]

const dataList = ref<API.Picture[]>([])
const total = ref(0)

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const paging = computed(() => ({
  current: searchParams.current ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count: number) => `共 ${count} 条`,
}))

const fetchData = async () => {
  try {
    const res = await listPictureByPageUsingPost({
      ...searchParams,
      nullSpaceId: true,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      total.value = Number(res.data.data.total ?? 0)
    } else {
      message.error('获取数据失败：' + res.data.message)
    }
  } catch {
    message.error('获取图片数据失败')
  }
}

const parseTags = (tags?: string) => {
  try {
    return JSON.parse(tags || '[]')
  } catch {
    return []
  }
}

const getReviewColor = (status?: number) => {
  if (status === PIC_REVIEW_STATUS_ENUM.PASS) return 'green'
  if (status === PIC_REVIEW_STATUS_ENUM.REJECT) return 'red'
  return 'orange'
}

const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doDelete = async (id: string) => {
  if (id == null) return
  const res = await deletePictureUsingPost({ id })
  if (res.data.data) {
    message.success('删除成功')
    await fetchData()
  } else {
    message.error('删除失败')
  }
}

const doReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage = reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '审核通过' : '审核拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核操作成功')
    await fetchData()
  } else {
    message.error('审核操作失败：' + res.data.message)
  }
}

onMounted(fetchData)
</script>

<style scoped>
#PictureManageView {
  min-height: calc(100vh - 64px);
  padding: 28px;
  background: #f6f8fc;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 22px;
  max-width: 1420px;
  margin: 0 auto 20px;
}

.page-kicker {
  display: block;
  margin-bottom: 7px;
  color: #3577e8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 2px;
}

.page-header h2 {
  margin: 0;
  color: #23324a;
  font-size: 30px;
}

.page-header p {
  margin: 7px 0 0;
  color: #8491a5;
  font-size: 13px;
}

.filter-card,
.table-card {
  max-width: 1420px;
  margin: 0 auto 16px;
  border: 1px solid rgba(65, 99, 153, 0.08);
  border-radius: 18px;
  box-shadow: 0 10px 28px rgba(53, 78, 118, 0.06);
}

.filter-form :deep(.ant-form-item) {
  margin-bottom: 10px;
}

.filter-form :deep(.ant-input),
.filter-form :deep(.ant-select-selector) {
  border-radius: 9px !important;
}

.filter-select {
  min-width: 170px;
}

.table-card :deep(.ant-card-body) {
  padding: 0;
}

.table-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  color: #8793a5;
  font-size: 12px;
  border-bottom: 1px solid #eef1f6;
}

.table-summary strong {
  color: #2d3d57;
  font-size: 16px;
}

.picture-table :deep(.ant-table) {
  border-radius: 0 0 18px 18px;
}

.picture-table :deep(.ant-table-thead > tr > th) {
  padding: 13px 16px;
  color: #596a82;
  font-size: 12px;
  font-weight: 700;
  background: #fbfcfe;
  border-bottom: 1px solid #edf1f6;
}

.picture-table :deep(.ant-table-tbody > tr > td) {
  padding: 14px 16px;
  vertical-align: middle;
  border-bottom: 1px solid #f0f3f8;
}

.picture-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #f8fbff;
}

.picture-cell {
  display: flex;
  align-items: center;
  gap: 13px;
  min-width: 0;
}

.picture-cell :deep(.ant-image) {
  flex: none;
  overflow: hidden;
  border-radius: 10px;
}

.picture-cell :deep(.ant-image-img) {
  object-fit: cover;
  border-radius: 10px;
}

.picture-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.picture-copy strong,
.picture-copy span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picture-copy strong {
  color: #26354d;
  font-size: 14px;
}

.picture-copy span {
  max-width: 230px;
  color: #8793a6;
  font-size: 11px;
}

.picture-copy small {
  color: #aab3c1;
  font-size: 10px;
}

.category-cell {
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  gap: 5px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
}

.tag-list :deep(.ant-tag) {
  margin: 0;
  color: #67768c;
  font-size: 10px;
  border: 0;
  border-radius: 999px;
  background: #f1f5fa;
}

.compact-info,
.review-cell,
.time-cell {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.compact-info strong,
.time-cell strong {
  color: #4a5b73;
  font-size: 12px;
  white-space: nowrap;
}

.compact-info span,
.time-cell span,
.review-cell span {
  color: #9aa5b5;
  font-size: 10px;
  white-space: nowrap;
}

.review-cell {
  align-items: flex-start;
}

.review-cell :deep(.ant-tag) {
  margin: 0;
}

@media (max-width: 900px) {
  #PictureManageView {
    padding: 18px 14px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .table-summary {
    align-items: flex-start;
    flex-direction: column;
    gap: 5px;
  }
}
</style>