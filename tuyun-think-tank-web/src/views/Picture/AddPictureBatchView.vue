<template>
  <div id="addPictureBatchPage">
    <h2 style="margin-bottom: 16px">
      批量创建图片
    </h2>
    <a-form layout="vertical"  :model="pictureFBatchData" @finish="handleSubmit">
<!--      name属性用于标识表单元素的名称，在提交表单时会被提交到服务器-->
      <a-form-item label="名称" name="searchText">
        <a-input v-model:value="pictureFBatchData.searchText" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item label="抓取数量" name="count">
        <a-input-number v-model:value="pictureFBatchData.count"
                        placeholder="请输入抓取数量" />
      </a-form-item>
      <a-form-item label="图片前缀名" name="picNam">
        <a-input v-model:value="pictureFBatchData.picName"
                 placeholder="请输入图片前缀名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">开始批量创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
// 正确导入语句应为：
import {  useRouter } from 'vue-router'
import { doPictureBatchUsingPost } from '@/api/pictureController'
import { message } from 'ant-design-vue'

import { reactive, ref } from 'vue'


// 正确初始化路由实例

const router = useRouter()

/*
 * 表单数据
 */
const pictureFBatchData = reactive<API.PictureByBatchRequest>({
  count: 10,

})
const loading = ref(false)


const handleSubmit = async () => {
 loading.value = true

  //await：等待异步操作，暂停当前代码执行，等待服务器返回结果，避免页面卡死。
  const res = await doPictureBatchUsingPost({
    ...pictureFBatchData,

  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功,共创建'+res.data.data+'张图片')
    // 跳转到图片详情页
    router.push({
      path: `/`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
}



</script>

<style scoped>

#addPictureBatchPage {
  background: linear-gradient(145deg, #e0f2f1 0%, #b2dfdb 100%) url('data:image/svg+xml;utf8,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><rect width="100%" height="100%" fill="none" stroke="%23b2dfdb" stroke-width="2" stroke-dasharray="4 6" /></svg>');
  border: 2px solid #8ab7b5;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(42,121,121,0.15);
  padding: 24px 32px;
}

h2 {
  font-family: '华文篆书', 'ZCOOL XiaoWei', serif;
  background: linear-gradient(to right, #2a7979, #3a9e8d);
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
}

#addPictureBatchPage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
