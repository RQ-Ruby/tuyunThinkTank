<template>
  <div id="homePage">
    <section class="hero-panel">
      <div class="hero-glow hero-glow-left"></div>
      <div class="hero-glow hero-glow-right"></div>

      <div class="hero-copy">
        <a-tag class="eyebrow-tag" color="blue">TUYUN VISUAL LIBRARY</a-tag>
        <h1>让好图片，<br /><span>成为好内容。</span></h1>
        <p class="hero-description">
          一个面向创作者与团队的高质量视觉图库。浏览精选灵感，收藏心仪作品，
          协作管理空间，并用 AI 扩图打开更多可能。
        </p>

        <div class="hero-actions">
          <a-button type="primary" size="large" class="primary-action" @click="scrollToGallery">
            浏览精选
            <ArrowRightOutlined />
          </a-button>
          <a-button size="large" class="secondary-action" @click="goUpload">
            <UploadOutlined />
            上传作品
          </a-button>
        </div>

        <div class="hero-stats">
          <div class="hero-stat">
            <strong>{{ totalDisplay }}</strong>
            <span>精选图片</span>
          </div>
          <div class="hero-stat">
            <strong>{{ categoryCountDisplay }}</strong>
            <span>视觉主题</span>
          </div>
          <div class="hero-stat">
            <strong>{{ tagCountDisplay }}</strong>
            <span>灵感标签</span>
          </div>
          <div class="hero-stat hero-stat-last">
            <strong>AI</strong>
            <span>智能扩图</span>
          </div>
        </div>
      </div>

      <div class="hero-visual" :class="{ 'is-loading': loading }">
        <div
          v-for="(picture, index) in heroPictures"
          :key="picture.id"
          class="hero-card"
          :class="`hero-card-${index + 1}`"
          @click="openPicture(picture)"
        >
          <img
            :src="picture.thumbnailUrl ?? picture.url"
            :alt="picture.name"
            loading="eager"
          />
          <div class="hero-card-caption">
            <span>{{ picture.category ?? '精选' }}</span>
            <strong>{{ picture.name }}</strong>
          </div>
        </div>
        <div v-if="loading && heroPictures.length === 0" class="hero-skeleton">
          <div class="skeleton-block"></div>
          <div class="skeleton-block"></div>
          <div class="skeleton-block"></div>
        </div>
      </div>
    </section>

    <section id="gallerySection" class="gallery-section">
      <div class="section-heading">
        <div>
          <span class="section-kicker">EXPLORE COLLECTION</span>
          <h2>从一张图片开始，发现更多灵感</h2>
          <p>按关键词、分类与标签组合检索，快速找到适合当前项目的视觉素材。</p>
        </div>
        <a-button v-if="hasActiveFilter" type="link" class="reset-button" @click="resetFilters">
          清空筛选
        </a-button>
      </div>

      <div class="search-row">
        <a-input-search
          v-model:value="searchParams.searchText"
          size="large"
          enter-button="搜索"
          placeholder="搜索图片名称，例如：城市、海岸、建筑、咖啡..."
          @search="doSearch"
        />
        <div class="search-hint">
          <SearchOutlined />
          支持名称搜索、分类切换与多标签筛选
        </div>
      </div>

      <a-tabs v-model:activeKey="selectedCategory" class="category-tabs" @change="doSearch">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" force-render />
      </a-tabs>

      <div v-if="tagList.length" class="tag-filter">
        <span class="tag-filter-label">灵感标签</span>
        <a-space wrap :size="[8, 8]">
          <a-checkable-tag
            v-for="tag in tagList"
            :key="tag"
            :checked="selectedTags.includes(tag)"
            class="filter-tag"
            @change="(checked: boolean) => toggleTag(tag, checked)"
          >
            {{ tag }}
          </a-checkable-tag>
        </a-space>
      </div>

      <div class="result-meta">
        <div>
          <strong>{{ total }}</strong>
          <span> 张图片</span>
          <span v-if="selectedCategory !== 'all'" class="result-filter">· {{ selectedCategory }}</span>
          <span v-if="selectedTags.length" class="result-filter">· {{ selectedTags.join(' / ') }}</span>
        </div>
        <span class="result-note">实时更新 · 审核通过的公开精选</span>
      </div>

      <PictureList :dataList="dataList" :loading="loading" />

      <a-empty
        v-if="!loading && dataList.length === 0"
        class="gallery-empty"
        description="没有找到匹配的图片，试试更换关键词或清空筛选"
      >
        <a-button type="primary" @click="resetFilters">查看全部图片</a-button>
      </a-empty>

      <a-pagination
        v-if="total > 0"
        v-model:current="searchParams.current"
        v-model:pageSize="searchParams.pageSize"
        :total="total"
        :show-size-changer="true"
        :show-total="(count: number) => `共 ${count} 张`"
        class="gallery-pagination"
        @change="onPageChange"
      />
    </section>

    <section class="capability-section">
      <div class="capability-heading">
        <span class="section-kicker">MORE THAN A GALLERY</span>
        <h2>从灵感到交付，一套完整的视觉工作流</h2>
      </div>
      <div class="capability-grid">
        <article v-for="feature in features" :key="feature.title" class="capability-card">
          <div class="capability-icon">
            <component :is="feature.icon" />
          </div>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.description }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowRightOutlined,
  PictureOutlined,
  RobotOutlined,
  SafetyCertificateOutlined,
  SearchOutlined,
  TeamOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController'
import PictureList from '@/components/PictureList.vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const categoryList = ref<string[]>([])
const tagList = ref<string[]>([])
const selectedCategory = ref('all')
const selectedTags = ref<string[]>([])
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const libraryTotal = ref(0)
const loading = ref(true)

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const heroPictures = computed(() => dataList.value.slice(0, 4))
const totalDisplay = computed(() => (total.value > 0 ? `${total.value}` : '100+'))
const categoryCountDisplay = computed(() => `${categoryList.value.length || 5}`)
const tagCountDisplay = computed(() => `${tagList.value.length || 9}`)
const hasActiveFilter = computed(
  () => selectedCategory.value !== 'all' || selectedTags.value.length > 0 || !!searchParams.searchText,
)

const features = [
  {
    title: '高质量精选图库',
    description: '覆盖模板、电商、海报、素材与表情包，支持关键词、分类和标签多级筛选。',
    icon: PictureOutlined,
  },
  {
    title: '个人与团队空间',
    description: '私有空间独立管理，团队空间支持成员角色、上传、编辑与实时协作。',
    icon: TeamOutlined,
  },
  {
    title: 'AI 智能扩图',
    description: '基于现有图片发起扩图任务，快速生成更适合横幅、海报与社交媒体的构图。',
    icon: RobotOutlined,
  },
  {
    title: '内容审核机制',
    description: '从草稿、待审核到通过或拒绝，完整保留审核状态与后台管理链路。',
    icon: SafetyCertificateOutlined,
  },
]

const getTagCategoryOptions = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      tagList.value = res.data.data.tagList ?? []
      categoryList.value = res.data.data.categoryList ?? []
    }
  } catch {
    message.warning('分类与标签加载失败，已展示默认内容')
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: API.PictureQueryRequest = {
      ...searchParams,
      tags: selectedTags.value.length ? [...selectedTags.value] : [],
    }
    if (selectedCategory.value !== 'all') {
      params.category = selectedCategory.value
    } else {
      delete params.category
    }
    const res = await listPictureVoByPageUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      total.value = Number(res.data.data.total ?? 0)
      if (!hasActiveFilter.value) {
        libraryTotal.value = total.value
      }
    } else {
      message.error(res.data.message || '获取图片失败')
    }
  } catch (error) {
    const err = error as any
    console.error('[HomeView] fetchData failed ' + JSON.stringify({ message: err?.message, code: err?.code, status: err?.response?.status, data: err?.response?.data, url: err?.config?.url }))
    message.error('网络异常，图片加载失败')
  } finally {
    loading.value = false
  }
}

const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

const toggleTag = (tag: string, checked: boolean) => {
  if (checked && !selectedTags.value.includes(tag)) {
    selectedTags.value = [...selectedTags.value, tag]
  } else if (!checked) {
    selectedTags.value = selectedTags.value.filter((item) => item !== tag)
  }
  doSearch()
}

const resetFilters = () => {
  selectedCategory.value = 'all'
  selectedTags.value = []
  searchParams.searchText = ''
  doSearch()
}

const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

const scrollToGallery = () => {
  document.getElementById('gallerySection')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const goUpload = () => {
  if (loginUserStore.loginUser.id) {
    router.push('/add_picture')
    return
  }
  message.info('登录后即可上传作品')
  router.push({ path: '/user/login', query: { redirect: '/add_picture' } })
}

const openPicture = (picture: API.PictureVO) => {
  router.push(`/picture/${picture.id}`)
}

onMounted(async () => {
  await Promise.all([getTagCategoryOptions(), fetchData()])
})
</script>

<style scoped>
#homePage {
  max-width: 1440px;
  margin: 0 auto;
  color: #172033;
}

.hero-panel {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.02fr) minmax(440px, 0.98fr);
  gap: 38px;
  min-height: 510px;
  padding: 56px 56px 42px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 32px;
  background:
    radial-gradient(circle at 18% 16%, rgba(84, 150, 255, 0.22), transparent 34%),
    radial-gradient(circle at 82% 75%, rgba(76, 210, 190, 0.2), transparent 34%),
    linear-gradient(135deg, #f4f8ff 0%, #ffffff 48%, #eefaf7 100%);
  box-shadow: 0 24px 70px rgba(61, 89, 141, 0.13);
}

.hero-panel::after {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background-image: linear-gradient(rgba(80, 118, 175, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(80, 118, 175, 0.045) 1px, transparent 1px);
  background-size: 34px 34px;
  mask-image: linear-gradient(to right, rgba(0, 0, 0, 0.38), transparent 76%);
}

.hero-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(10px);
  pointer-events: none;
}

.hero-glow-left {
  top: -130px;
  left: -100px;
  width: 320px;
  height: 320px;
  background: rgba(100, 166, 255, 0.13);
}

.hero-glow-right {
  right: -120px;
  bottom: -150px;
  width: 360px;
  height: 360px;
  background: rgba(55, 211, 184, 0.12);
}

.hero-copy {
  position: relative;
  z-index: 2;
  align-self: center;
}

.eyebrow-tag {
  margin-bottom: 18px;
  padding: 5px 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.8px;
  border: 0;
  border-radius: 999px;
}

.hero-copy h1 {
  max-width: 680px;
  margin: 0;
  color: #15213a;
  font-family: 'Alibaba PuHuiTi', 'Microsoft YaHei', sans-serif;
  font-size: clamp(42px, 4.5vw, 70px);
  font-weight: 800;
  line-height: 1.12;
  letter-spacing: -2.5px;
}

.hero-copy h1 span {
  color: transparent;
  background: linear-gradient(96deg, #2f6fed 2%, #16a58d 96%);
  background-clip: text;
  -webkit-background-clip: text;
}

.hero-description {
  max-width: 600px;
  margin: 24px 0 0;
  color: #60708c;
  font-size: 16px;
  line-height: 1.9;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 30px;
}

.primary-action,
.secondary-action {
  height: 48px;
  padding: 0 24px;
  font-weight: 700;
  border-radius: 14px;
}

.primary-action {
  border: 0;
  background: linear-gradient(96deg, #3069e8, #16a58d);
  box-shadow: 0 12px 25px rgba(47, 111, 237, 0.25);
}

.secondary-action {
  color: #2b3b55;
  border-color: rgba(51, 88, 151, 0.18);
  background: rgba(255, 255, 255, 0.76);
  backdrop-filter: blur(8px);
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  margin-top: 40px;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  min-width: 105px;
  padding: 0 22px;
  border-right: 1px solid rgba(70, 105, 161, 0.14);
}

.hero-stat:first-child {
  padding-left: 0;
}

.hero-stat-last {
  border-right: 0;
}

.hero-stat strong {
  color: #1d2d4b;
  font-size: 26px;
  line-height: 1.1;
}

.hero-stat span {
  margin-top: 6px;
  color: #7d8aa1;
  font-size: 12px;
}

.hero-visual {
  position: relative;
  z-index: 2;
  min-height: 410px;
}

.hero-card {
  position: absolute;
  overflow: hidden;
  cursor: pointer;
  border: 5px solid rgba(255, 255, 255, 0.9);
  border-radius: 22px;
  background: #dfe9f8;
  box-shadow: 0 22px 48px rgba(38, 65, 111, 0.2);
  transition: transform 0.35s ease, box-shadow 0.35s ease;
}

.hero-card:hover {
  z-index: 10;
  transform: translateY(-7px) rotate(0deg) scale(1.025);
  box-shadow: 0 30px 60px rgba(38, 65, 111, 0.28);
}

.hero-card img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-card-caption {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 36px 15px 14px;
  color: white;
  background: linear-gradient(transparent, rgba(8, 17, 33, 0.82));
}

.hero-card-caption span {
  font-size: 10px;
  letter-spacing: 1.3px;
  opacity: 0.78;
}

.hero-card-caption strong {
  overflow: hidden;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero-card-1 {
  top: 12px;
  right: 8px;
  width: 58%;
  height: 58%;
  transform: rotate(3deg);
}

.hero-card-2 {
  bottom: 0;
  left: 0;
  width: 54%;
  height: 52%;
  transform: rotate(-4deg);
}

.hero-card-3 {
  right: 0;
  bottom: 8px;
  width: 39%;
  height: 36%;
  transform: rotate(4deg);
}

.hero-card-4 {
  top: 0;
  left: 4%;
  width: 32%;
  height: 29%;
  transform: rotate(-3deg);
}

.hero-skeleton {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  height: 100%;
}

.skeleton-block {
  border-radius: 22px;
  background: linear-gradient(100deg, #e5edf8 20%, #f5f9ff 42%, #e5edf8 60%);
  background-size: 220% 100%;
  animation: skeletonShine 1.5s infinite linear;
}

@keyframes skeletonShine {
  to {
    background-position: -220% 0;
  }
}

.gallery-section {
  padding: 76px 16px 24px;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  max-width: 1120px;
  margin: 0 auto 28px;
}

.section-kicker {
  display: block;
  margin-bottom: 10px;
  color: #3577e8;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 2.4px;
}

.section-heading h2,
.capability-heading h2 {
  margin: 0;
  color: #18243c;
  font-size: clamp(26px, 3vw, 38px);
  line-height: 1.3;
}

.section-heading p {
  max-width: 760px;
  margin: 12px 0 0;
  color: #7a879b;
  line-height: 1.8;
}

.reset-button {
  flex: none;
  padding: 0;
  font-weight: 600;
}

.search-row {
  max-width: 1120px;
  margin: 0 auto 20px;
}

.search-row :deep(.ant-input-affix-wrapper) {
  height: 52px;
  padding-inline: 18px;
  border-color: rgba(62, 100, 162, 0.16);
  border-radius: 15px 0 0 15px;
}

.search-row :deep(.ant-input-search-button) {
  height: 52px;
  padding-inline: 28px;
  font-weight: 700;
  border-radius: 0 15px 15px 0;
}

.search-hint {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 9px;
  color: #98a2b3;
  font-size: 12px;
}

.category-tabs {
  max-width: 1120px;
  margin: 0 auto;
}

.category-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 14px;
}

.category-tabs :deep(.ant-tabs-tab) {
  padding: 10px 2px;
  font-weight: 600;
}

.tag-filter {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  max-width: 1120px;
  margin: 8px auto 26px;
  padding: 16px 18px;
  border: 1px solid rgba(70, 106, 166, 0.1);
  border-radius: 16px;
  background: rgba(248, 250, 255, 0.86);
}

.tag-filter-label {
  flex: none;
  padding-top: 5px;
  color: #596980;
  font-size: 13px;
  font-weight: 700;
}

.filter-tag {
  margin-inline-end: 0;
  padding: 5px 12px;
  color: #5d6b82;
  border-radius: 999px;
  background: #eef3fa;
  transition: all 0.2s ease;
}

.filter-tag:hover {
  color: #2f6fed;
  background: #e4edff;
}

.filter-tag.ant-tag-checkable-checked {
  color: #fff;
  background: linear-gradient(96deg, #3069e8, #16a58d);
}

.result-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  max-width: 1120px;
  margin: 0 auto 18px;
  color: #7d8aa1;
  font-size: 13px;
}

.result-meta strong {
  color: #24334d;
  font-size: 20px;
}

.result-filter {
  margin-left: 8px;
  color: #3577e8;
}

.result-note {
  font-size: 12px;
}

.gallery-pagination {
  margin-top: 30px;
  text-align: right;
}

.gallery-empty {
  padding: 70px 0;
}

.capability-section {
  max-width: 1152px;
  margin: 72px auto 24px;
  padding: 0 16px;
}

.capability-heading {
  margin-bottom: 26px;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.capability-card {
  min-height: 210px;
  padding: 24px;
  border: 1px solid rgba(65, 100, 158, 0.1);
  border-radius: 20px;
  background: linear-gradient(145deg, #ffffff, #f8fbff);
  box-shadow: 0 10px 30px rgba(64, 95, 144, 0.07);
}

.capability-icon {
  display: grid;
  width: 42px;
  height: 42px;
  margin-bottom: 18px;
  color: #2f6fed;
  font-size: 20px;
  border-radius: 12px;
  background: #eaf1ff;
  place-items: center;
}

.capability-card h3 {
  margin: 0 0 10px;
  color: #26344c;
  font-size: 17px;
}

.capability-card p {
  margin: 0;
  color: #7a879b;
  font-size: 13px;
  line-height: 1.8;
}

@media (max-width: 1100px) {
  .hero-panel {
    grid-template-columns: 1fr;
    padding: 46px 38px 38px;
  }

  .hero-visual {
    min-height: 420px;
  }

  .capability-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-panel {
    min-height: auto;
    padding: 34px 22px 28px;
    border-radius: 24px;
  }

  .hero-copy h1 {
    font-size: 43px;
    letter-spacing: -1.8px;
  }

  .hero-description {
    font-size: 14px;
  }

  .hero-stats {
    margin-top: 28px;
  }

  .hero-stat {
    min-width: 50%;
    margin-bottom: 18px;
    padding: 0 12px;
  }

  .hero-stat:nth-child(2) {
    border-right: 0;
  }

  .hero-visual {
    min-height: 330px;
  }

  .hero-card-caption {
    display: none;
  }

  .gallery-section {
    padding-top: 54px;
  }

  .section-heading,
  .result-meta {
    align-items: flex-start;
    flex-direction: column;
  }

  .section-heading {
    padding: 0 4px;
  }

  .tag-filter {
    flex-direction: column;
  }

  .result-note {
    display: none;
  }

  .capability-section {
    margin-top: 54px;
  }

  .capability-grid {
    grid-template-columns: 1fr;
  }

  .capability-card {
    min-height: auto;
  }
}

@media (max-width: 480px) {
  .hero-actions {
    flex-direction: column;
  }

  .primary-action,
  .secondary-action {
    width: 100%;
  }

  .hero-visual {
    min-height: 280px;
  }

  .hero-card {
    border-width: 3px;
    border-radius: 16px;
  }
}
</style>