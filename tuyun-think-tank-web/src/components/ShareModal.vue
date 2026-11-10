<template>
  <a-modal v-model:open="open" title="分享图片" :footer="null" width="440px">
    <p class="share-tip">复制下面的链接，分享给需要查看这张图片的人。</p>
    <a-space-compact class="share-link">
      <a-input :value="link" readonly />
      <a-button type="primary" @click="copyLink">复制链接</a-button>
    </a-space-compact>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'

interface Props {
  link?: string
}

const props = defineProps<Props>()
const open = ref(false)

const openModal = () => {
  open.value = true
}

const copyLink = async () => {
  if (!props.link) {
    message.warning('分享链接尚未生成')
    return
  }
  try {
    await navigator.clipboard.writeText(props.link)
    message.success('分享链接已复制')
  } catch {
    message.error('复制失败，请手动选择链接复制')
  }
}

defineExpose({ openModal })
</script>

<style scoped>
.share-tip {
  margin-bottom: 16px;
  color: #666;
}

.share-link {
  display: flex;
  width: 100%;
}

.share-link :deep(.ant-input) {
  flex: 1;
}
</style>
