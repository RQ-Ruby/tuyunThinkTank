<template>
  <a-list
    :grid="{ gutter: 18, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 4 }"
    :data-source="dataList"
    :loading="loading"
  >
    <template #renderItem="{ item: picture }">
      <a-list-item class="picture-list-item">
        <a-card hoverable class="picture-card" @click="doPicture(picture)">
          <template #cover>
            <div class="image-container">
              <img
                :src="picture.thumbnailUrl ?? picture.url"
                :alt="picture.name"
                loading="lazy"
              />
              <span class="category-pill">{{ picture.category ?? '精选' }}</span>
            </div>
          </template>

          <div class="picture-copy">
            <div class="picture-meta-row">
              <span class="author-name">{{ picture.user?.userName ?? '图云创作者' }}</span>
              <span class="picture-format">{{ picture.picFormat?.toUpperCase() ?? 'IMAGE' }}</span>
            </div>
            <h3 class="picture-title" :title="picture.name">{{ picture.name }}</h3>
            <p class="picture-introduction" :title="picture.introduction ?? ''">
              {{ picture.introduction ?? '一张值得被收藏与分享的视觉灵感。' }}
            </p>
            <div class="picture-tags">
              <a-tag v-for="tag in (picture.tags ?? []).slice(0, 3)" :key="tag" class="tag-item">
                {{ tag }}
              </a-tag>
            </div>
          </div>
        </a-card>
      </a-list-item>
    </template>
  </a-list>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Props {
  dataList: API.PictureVO[]
  loading: boolean
}

defineProps<Props>()

const router = useRouter()

const doPicture = (picture: API.PictureVO) => {
  router.push(`/picture/${picture.id}`)
}
</script>

<style scoped>
.picture-list-item {
  padding: 0 !important;
}

.picture-card {
  overflow: hidden;
  height: 100%;
  border: 1px solid rgba(69, 102, 155, 0.1);
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 8px 26px rgba(44, 73, 119, 0.08);
  transition: transform 0.28s ease, box-shadow 0.28s ease;
}

.picture-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 38px rgba(44, 73, 119, 0.15);
}

.image-container {
  position: relative;
  overflow: hidden;
  aspect-ratio: 4 / 3;
  background: #edf2f8;
}

.image-container img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.picture-card:hover .image-container img {
  transform: scale(1.04);
}

.category-pill {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 5px 10px;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  border-radius: 999px;
  background: rgba(17, 30, 52, 0.65);
  backdrop-filter: blur(8px);
}

.picture-copy {
  display: flex;
  flex-direction: column;
  min-height: 150px;
  padding: 16px 17px 15px;
}

.picture-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 7px;
  color: #94a0b2;
  font-size: 11px;
}

.author-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picture-format {
  flex: none;
  letter-spacing: 0.8px;
}

.picture-title {
  overflow: hidden;
  margin: 0 0 7px;
  color: #24334b;
  font-size: 16px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picture-introduction {
  display: -webkit-box;
  overflow: hidden;
  min-height: 38px;
  margin: 0;
  color: #7d899c;
  font-size: 12px;
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.picture-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: auto;
  padding-top: 12px;
}

.tag-item {
  margin-inline-end: 0;
  color: #50617a;
  font-size: 10px;
  border: 0;
  border-radius: 999px;
  background: #f2f6fb;
}

@media (max-width: 768px) {
  .picture-copy {
    min-height: 142px;
  }
}
</style>