<template>
  <div id="homePage">
    <!--    搜索框-->
    <div class="search-bar">
      <a-input-search
        v-model:value="searchParams.searchText"
        placeholder="请输入要查找的图片名称"
        enter-button
        @search="doSearch"
      />
    </div>
    <!--分类和标签-->
    <a-tabs v-model:activeKey="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane :key="category" :tab="category" v-for="category in categoryList" force-render />
    </a-tabs>
<div class="tag-bar">
  <span style="margin-right: 8px">标签 : </span>
  <a-space :size="[0, 8]" wrap>
    <a-checkable-tag
      v-for="(tag, index) in tagList"
      :key="tag"
      v-model:checked="selectedTagList[index]"
      @change="doSearch"
    >
      {{ tag }}
    </a-checkable-tag>
  </a-space>
</div>
    <!--   图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 4 }"
      :data-source="dataList"
      :pagination="paging"
      :loading="loading"

    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <a-list-item style="padding: 0">
            <!-- 单张图片 -->
            <a-card hoverable @click="doPicture(picture)" class="picture-card">
              <template #cover>
                <div class="image-container">
                  <img
                    style="height: 300px; object-fit: cover"
                    :alt="picture.name"
                    :src="picture.url"
                  />
                  <div class="image-overlay">
                    <div class="meta-content">
                      <h4 class="title">{{ picture.name }}</h4>
                      <a-flex vertical :gap="4">
                        <a-tag color="green" class="category-tag">
                          {{ picture.category ?? '默认' }}
                        </a-tag>
                        <a-flex wrap :gap="4">
                          <a-tag v-for="tag in picture.tags" :key="tag" class="tag-item">
                            {{ tag }}
                          </a-tag>
                        </a-flex>
                      </a-flex>
                    </div>
                  </div>
                </div>
              </template>
            </a-card>
          </a-list-item>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

//点击图片跳转图片详情页
const router = useRouter()
const doPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`, // 这里是反引号
  })
}

//分类和标签选项
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    //map的作用把数组中的每个元素都按照固定规则转换成新形式，生成一个新数组（原数组不变）。
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    message.error('加载选项失败' + res.data.message)
  }
}
//钩子函数：在组件挂载后执行
onMounted(() => {
  //在页面加载时获取标签和分类选项
  getTagCategoryOptions()
})

// 获取数据,搜索框进行搜索
const doSearch = () => {
  // 重置搜索条件，重新搜索时要让页面回到第一页
  searchParams.current = 1
  fetchData()
}

// 数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 分页参数
const paging = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    // 切换页号时，会修改搜索参数并获取数据
    onChange: (page: number, pageSize: number) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchData()
    },
  }
})

const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    ...searchParams,
    tags: [],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#homePage {
}

#homePage .search-bar {
  width: 480px;
  margin: 0 auto 16px;
}
#homePage .tag-bar {
  margin-bottom: 20px;
}

.picture-card {
  overflow: hidden;
  position: relative;
}

.image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 16/9;
}

.picture-card img {
  height: 100%;
  width: 100%;
  object-fit: cover;
  min-width: 100%;
  min-height: 100%;
}

.image-container {
  overflow: hidden;
}
.image-overlay {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  height: 40%;
  background: linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,0.8) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.picture-card:hover .image-overlay {
  opacity: 1;
}

.meta-content {
  width: 100%;
  color: white;
}

.title {
  font-size: 14px;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-tag {
  padding: 0 4px;
  width: max-content;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: linear-gradient(45deg, #13c2c2, #06b48b) !important;
  border: none;
  color: white !important;
  transition: all 0.3s cubic-bezier(0.34, 0.69, 0.1, 1);
}

.picture-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: all 0.3s cubic-bezier(0.34, 0.69, 0.1, 1);
}

.picture-card:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.image-overlay {
  align-items: center;
  padding: 16px;
}

.image-container {
  aspect-ratio: 4/3;
}

.picture-card {
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.12);
  transition: all 0.3s cubic-bezier(0.34, 0.69, 0.1, 1.2);
}

.picture-card:hover {
  transform: scale(1.05) rotate(1deg);
  box-shadow: 0 12px 32px rgba(0,0,0,0.18);
}

.title {
  font-size: 16px;
  margin-bottom: 12px;
}

.category-tag {
  padding: 2px 8px;
  border-radius: 8px;
  background: linear-gradient(45deg, #1890ff, #0050b3) !important;
}

.picture-card {
  transition: all 0.25s ease-out;
  transform-origin: center;
}

.picture-card:hover {
  transform: scale(1.03);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.tag-item {
  background-color: rgba(255, 255, 255, 0.9);
  color: #333;
  padding: 2px 8px;
  border-radius: 4px;
}
</style>
