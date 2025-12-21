<template>
  <a-form layout="inline" :model="searchParams" @finish="doSearch">
    <a-form-item label="关键词" name="searchText">
      <a-input v-model:value="searchParams.searchText" placeholder="从名称、简介搜索" allow-clear />
    </a-form-item>
    <a-form-item label="分类" name="category">
      <a-input v-model:value="searchParams.category" placeholder="请输入分类" allow-clear />
    </a-form-item>
    <a-form-item label="标签" name="tags">
      <a-input v-model:value="searchParams.tags" placeholder="请输入标签" allow-clear />
    </a-form-item>
    <a-form-item>
      <a-button type="primary" html-type="submit">搜索</a-button>
      <a-button style="margin-left: 8px" @click="doReset">重置</a-button>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

interface Props {
  onSearch?: (searchParams: API.PictureQueryRequest) => void
}

const props = defineProps<Props>()

const searchParams = reactive<API.PictureQueryRequest>({})

const doSearch = () => {
  props.onSearch?.(searchParams)
}

const doReset = () => {
  Object.keys(searchParams).forEach((key) => {
    delete searchParams[key]
  })
  props.onSearch?.(searchParams)
}
</script>
