<template>
  <div class="picture-upload">
    <!-- 新增输入框容器 -->
    <div class="input-container">
      <a-input-group compact>
        <a-input v-model:value="value19" style="width: calc(100% - 200px)"  placeholder="请输入图片URL"/>
        <a-tooltip title="Upload pictures">
          <a-button    type="primary"
                       @click="handleUpload"
                       :loading="loading"
                       class="custom-btn">
            <template #icon><UploadOutlined /></template>
          </a-button>
        </a-tooltip>
      </a-input-group>
    </div>

    <!-- 图片展示容器 -->
    <div class="url-picture">
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { message } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost } from '@/api/pictureController'
import { UploadOutlined } from '@ant-design/icons-vue';

// 上传成功后返回的图片
interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
}
// 接收父组件传递的图片
const props = defineProps<Props>()
// 上传图片的url
const value19 = ref<String>()
//状态
const loading = ref<boolean>(false)

/**
 * 上传
 * @param file
 */
const handleUpload = async () => {
  loading.value = true
  try {
 const params:API.PictureUploadRequest={url: value19.value}
    if (props.picture) {
      params.id = props.picture.id
    }
// 调用上传API（POST请求）
    const res = await uploadPictureByUrlUsingPost(params)
    // 处理API响应：状态码为0且data存在时视为成功
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    loading.value = false
  }

}


</script>

<style scoped>
/* 整体容器样式 */
.picture-upload {
  max-width: 800px;
  margin: 20px auto;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

/* 输入区域容器 */
.input-container {
  margin-bottom: 24px;
}

/* 输入框样式 */
.ant-input {
  height: 42px;
  padding: 0 16px;
  border-radius: 6px 0 0 6px !important;
  font-size: 15px;
  border: 1px solid #d9d9d9;
  transition: border-color 0.3s;
}

.ant-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 上传按钮美化 */
.custom-btn {
  height: 42px !important;
  border-radius: 0 6px 6px 0 !important;
  background: #1890ff !important;
  border-color: #1890ff !important;
  transition: all 0.3s ease;
}

.custom-btn:hover {
  background: #40a9ff !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
}

/* 图片预览区域 */
.url-picture {
  margin-top: 24px;
  text-align: center;
  border: 1px dashed #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.url-picture img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.url-picture img:hover {
  transform: scale(1.02);
}

/* 空状态提示 */
.url-picture:empty::before {
  content: "暂无图片，请上传URL";
  color: #bfbfbf;
  font-size: 14px;
}
</style>
