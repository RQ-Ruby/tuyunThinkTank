<!--图片管理页面-->
<template>
  <div id="PictureManageView">
    <a-flex justify="space-between">
      <h2>图片管理</h2>
      <a-space>
      <a-button type="primary" href="/add_picture" target="_blank">+ 创建图片</a-button>
      <a-button type="primary" href="/add_picture/batch" target="_blank" ghost>+ 批量创建图片</a-button>
      </a-space>
    </a-flex>

    <!--搜索框-->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
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
          placeholder="请输入标签"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="审核状态" name="tags">
        <a-select
          v-model:value="searchParams.reviewStatus"
          :options="PIC_REVIEW_STATUS_OPTIONS"
          placeholder="请选择审核状态"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>

    <div style="margin-bottom: 16px"></div>
    <!--表格-->
    <!--      columns：配置表头及列行为（如排序、筛选）
      data-source：提供表格展示的数据
      paging：管理分页逻辑
      @change：响应表格交互事件-->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="paging"
      @change="doTableChange"
    >
      <!-- 通过解构获取当前列(column)和当前行数据(record) -->
      <template #bodyCell="{ column, record }">
        <!--如果当前列是url才进行渲染        -->
        <template v-if="column.dataIndex === 'url'">
          <!--          <a-image>：Ant Design Vue 的图片组件，专门用于显示图片-->
          <a-image :src="record.url" :width="120" />
        </template>
        <!-- 标签 -->
        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <!--当 <a-space> 容器内子元素的总宽度超过容器宽度时，wrap 属性会触发子元素自动换行到下一行，避免内容溢出或被截断            -->
            <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag">{{ tag }}</a-tag>
          </a-space>
        </template>
        <!-- 图片信息 -->
        <template v-if="column.dataIndex === 'picInfo'">
          <div>格式：{{ record.picFormat }}</div>
          <div>宽度：{{ record.picWidth }}</div>
          <div>高度：{{ record.picHeight }}</div>
          <div>宽高比：{{ record.picScale }}</div>
          <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <!-- 审核信息 -->
        <template v-if="column.dataIndex === 'reviewInfo'">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>审核人：{{ record.reviewer }}</div>
          <div v-if="record.reviewTime!=null">审核时间：{{  dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss')}}</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          <!--dayjs()	调用 Day.js 库，将时间数据转换为可操作对象（支持时间戳、ISO 字符串等格式）          -->
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'" >
          <a-space wrap>
            <a-button type="link" @click="doReview(record,PIC_REVIEW_STATUS_ENUM.PASS)" v-if="record.reviewStatus!=PIC_REVIEW_STATUS_ENUM.PASS">通过</a-button>
            <a-button type="link" danger @click="doReview(record,PIC_REVIEW_STATUS_ENUM.REJECT)" v-if="record.reviewStatus!=PIC_REVIEW_STATUS_ENUM.REJECT">拒绝</a-button>
            <a-button type="link" :href="`/add_picture?id=${record.id}`" target="_blank">编辑</a-button>
            <a-button type="link" danger @click="doDelete(record.id)">删除</a-button>
          </a-space>

        </template>
      </template>
    </a-table>
  </div>


</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  deletePictureUsingPost, doPictureReviewUsingPost,
  getPictureVoByIdUsingGet,
  listPictureByPageUsingPost,
  updatePictureUsingPost
} from '@/api/pictureController'
import { PIC_REVIEW_STATUS_ENUM, PIC_REVIEW_STATUS_MAP, PIC_REVIEW_STATUS_OPTIONS } from '@/contants/picyure'

// 表格列定义
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '审核信息',
    dataIndex: 'reviewInfo'
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 分页参数
const paging = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 获取数据
const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
  })
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 获取数据
const doSearch = () => {
  // 重置搜索条件
  searchParams.current = 1
  fetchData()
}

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

//删除图片
const doDelete = async (id: number) => {
  if(id==null){
    return
  }
  //因为是body传参，所以要传一个对象
  const res = await deletePictureUsingPost({id})
  if(res.data.data){
    message.success('删除成功')
    await fetchData()
  }
  else{
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
    // 重新获取列表
    fetchData()
  } else {
    message.error('审核操作失败，' + res.data.message)
  }
}


</script>

<style scoped>
#PictureManageView {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 24px;
  min-height: 100vh;
}

h2 {
  font-family: 'Poppins', system-ui;
  font-size: 24px;
  color: #2c3e50;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 1.5rem;
}

.a-table :deep(.ant-table-thead > tr > th) {
  background: #f8f9fa !important;
  font-weight: 600;
  color: #2c3e50 !important;
}

.a-table :deep(.ant-table-tbody > tr:nth-child(even)) {
  background: #f8f9fa;
}

.a-table :deep(.ant-table-tbody > tr:hover) {
  background: #f1f3f5 !important;
  transform: translateY(-2px);
  transition: all 0.2s ease;
}

.a-space > .a-button-link:hover::after {
  width: 100%;
}
</style>






