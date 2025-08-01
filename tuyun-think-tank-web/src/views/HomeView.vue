<template>
  <div id="homePage">
    <a-carousel autoplay class="carousel-container">
      <div class="carousel-item">
        <div class="content-wrapper">
          <h3 class="gradient-text">发现创意视觉</h3>
          <p class="sub-text">探索海量优质图片资源</p>
        </div>
        <img src="/public/access/tu1.png" alt="创意视觉" class="carousel-image" />
      </div>
      <div class="carousel-item">
        <div class="content-wrapper">
          <h3 class="gradient-text">专业图库管理</h3>
          <p class="sub-text">高效分类与检索系统</p>
        </div>
        <img src="/public/access/tu2.png" alt="图库管理" class="carousel-image" />
      </div>
    </a-carousel>
    <!-- 新增的功能按钮和搜索区域 -->
    <div class="search-container">
      <!-- 搜索区域（移动到按钮左侧） -->
      <div class="search-bar">
        <a-input-search
          style="height: 15px!important; padding: 0 10px!important;"
          v-model:value="searchParams.searchText"
          placeholder="请输入要查找的图片名称"
          enter-button
          @search="doSearch"
          size="large"/>
      </div>

      <!-- 左侧功能按钮 -->
      <div class="action-buttons">
        <a-button type="primary" ghost class="custom-button">许愿池</a-button>
        <a-button type="primary" ghost class="custom-button">我的收藏</a-button>
        <a-button type="primary" ghost class="custom-button">给我留言</a-button>
      </div>
    </div>

    </div>
    <!--分类和标签-->
    <a-tabs v-model:activeKey="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane :key="category" :tab="category" v-for="category in categoryList" force-render />
    </a-tabs>

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
                    :src="picture.thumbnailUrl ?? picture.url"
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
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  height: 56px; /* 从48px调整 */
  width: 450px;
  padding: 8px 16px; /* 新增内边距 */
}

#homePage .ant-input-search-button {
  height: 48px !important; /* 匹配新高度 */
  padding: 0 24px !important;
}

#homePage .ant-input-affix-wrapper {
  height: 48px !important;
  font-size: 16px !important; /* 增大字号 */
}

@media (max-width: 576px) {
  #homePage .search-bar {
    height: 48px;
    padding: 6px 12px;
  }
  #homePage .ant-input-search-button {
    height: 40px !important;
  }
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
/* 轮播图容器 */
.carousel-container {
  margin-bottom: 40px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

/* 轮播项 */
:deep(.slick-slide) {
  height: 480px;
  background: linear-gradient(45deg, #2c3e50, #3498db);
  position: relative;
}

.content-wrapper {
  position: absolute;
  left: 15%;
  top: 50%;
  transform: translateY(-50%);
  text-align: left;
  max-width: 600px;
}


.sub-text {
  font-size: 1.4em;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

/* 箭头优化 */
:deep(.slick-arrow.custom-slick-arrow) {
  width: 48px;
  height: 48px;
  font-size: 32px;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.slick-arrow.custom-slick-arrow:hover) {
  color: #fff;
  background: rgba(0, 0, 0, 0.4);
  transform: scale(1.15) translateY(-2px);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .content-wrapper {
    left: 10%;
    right: 10%;
  }

  .gradient-text {
    font-size: 2.2em;
  }

  :deep(.slick-arrow.custom-slick-arrow) {
    width: 36px;
    height: 36px;
    font-size: 24px;
  }
}

/* 轮播图片样式 */
.carousel-image {
  width: 100%;
  height: 480px;
  object-fit: cover;
  display: block;
}

/* 保持原有轮播容器样式 */
.carousel-container {
  margin-bottom: 40px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}


.sub-text {
  font-size: 1.8em;
  font-family: 'ZCOOL KuaiLe', 'YouYuan', cursive;
  color: rgba(255, 255, 255, 0.98);
  text-shadow:
    2px 2px 8px rgba(0, 0, 0, 0.8),
    0 0 15px rgba(255, 255, 255, 0.5);
  line-height: 1.6;
  padding: 12px 24px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  backdrop-filter: blur(8px);
  transform: translateZ(0);
}



  .sub-text {
    font-size: 1.4em;
    padding: 8px 16px;
    border-width: 0.5px;
  }

/* 修改渐变文字样式 */
.gradient-text {
  font-size: 4em;
  font-family: 'Alibaba PuHuiTi', 'Microsoft YaHei', 'PingFang SC', sans-serif;
  background: linear-gradient(45deg, #ffffff 25%, #e6f7ff 75%);
  -webkit-background-clip: text;
  text-shadow:
    3px 3px 8px rgba(0, 0, 0, 0.4),
    -1px -1px 2px rgba(255, 255, 255, 0.3),
    0 0 20px rgba(255, 255, 255, 0.4);
  letter-spacing: 2px;
  line-height: 1.2;
  position: relative;
  animation: textFloat 3s ease-in-out infinite;
}

/* 新增浮动动画 */
@keyframes textFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

/* 修改副标题样式 */
.sub-text {
  font-size: 1.8em;
  font-family: 'YouYuan', 'KaiTi', cursive;
  color: rgba(255, 255, 255, 0.95);
  text-shadow:
    2px 2px 6px rgba(0, 0, 0, 0.7),
    0 0 12px rgba(255, 255, 255, 0.4);
  line-height: 1.6;
  padding: 12px 28px;
  background: linear-gradient(45deg,
    rgba(0, 0, 0, 0.25) 0%,
    rgba(0, 0, 0, 0.15) 100%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  backdrop-filter: blur(6px);
  transition: all 0.3s ease;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .gradient-text {
    font-size: 2.5em;
    text-shadow:
      2px 2px 5px rgba(0, 0, 0, 0.4),
      -1px -1px 1px rgba(255, 255, 255, 0.3);
  }

  .sub-text {
    font-size: 1.3em;
    padding: 8px 16px;
    border-width: 0.8px;
  }
}
.search-container {
  display: flex;
  flex-direction: row-reverse; /* 反转顺序使按钮在右 */
  align-items: center;
  gap: 20px;
  margin: 0 auto 24px;
  max-width: 1200px;
  padding: 0 24px;
}

.action-buttons {
  flex: none;
  display: flex;
  gap: 45px;
}

.search-bar {
  flex: 1;
  max-width: 640px;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .search-container {
    flex-direction: column;
    align-items: stretch;
    padding: 0 16px;
  }

  .action-buttons {
    justify-content: center;
    flex-wrap: wrap;
    order: 2; /* 移动端按钮在下 */
    margin-top: 16px;
  }

  .search-bar {
    order: 1; /* 移动端搜索框在上 */
    max-width: 100%;
  }
}

  /* 搜索框区域自动填充 */
  #homePage .search-bar {
    flex: 1;
    max-width: 640px;
  }

  /* 响应式调整 */
  @media (max-width: 992px) {
    .search-container {
      flex-direction: column;
      align-items: stretch;
      padding: 0 16px;
    }



    #homePage .search-bar {
      max-width: 100%;
    }
  }









/* 调整搜索框高度和样式 */
#homePage .search-bar {
  flex-grow: 1;
  max-width: 540px;
}

#homePage .ant-input-search {
  width: 100%;
  height: 56px; /* 从48px调整到56px */
}

#homePage .ant-input-search-button {
  height: 56px !important; /* 匹配新高度 */
  padding: 0 24px !important;
  font-size: 16px !important; /* 增大字号 */
}

#homePage .ant-input-affix-wrapper {
  height: 70px !important; /* 增大输入框高度 */
  padding: 0 16px !important; /* 增加内边距 */
  font-size: 20px !important; /* 增大字号 */
}

/* 保持功能按钮与搜索框的协调性 */
.custom-button {
  height: 48px; /* 从42px调整到48px */
  padding: 0 24px;
  font-size: 15px;
  border-radius: 24px;
  background: linear-gradient(45deg, #4b57ff, #4dcbfd);
  color: white !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  #homePage .search-bar {
    width: 100%;
  }

  #homePage .ant-input-search {
    height: 52px;
  }

  #homePage .ant-input-affix-wrapper {
    height: 52px !important;
  }

  #homePage .ant-input-search-button {
    height: 52px !important;
  }

  .custom-button {
    height: 44px;
    padding: 0 16px;
    font-size: 14px;
  }
}

@media (max-width: 576px) {
  #homePage .ant-input-search {
    height: 48px;
  }

  #homePage .ant-input-affix-wrapper {
    height: 48px !important;
    font-size: 14px !important;
  }

  #homePage .ant-input-search-button {
    height: 48px !important;
    font-size: 14px !important;
  }

  .custom-button {
    height: 40px;
    padding: 0 12px;
    font-size: 13px;
  }
}


</style>
