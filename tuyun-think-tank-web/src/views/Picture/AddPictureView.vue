<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <PictureUpload :picture="picture" :onSuccess="onSuccess" />
    <a-form layout="vertical" v-if="picture" :model="pictureForm" @finish="handleSubmit">
<!--      name属性用于标识表单元素的名称，在提交表单时会被提交到服务器-->
      <a-form-item label="名称" name="name">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item label="简介" name="introduction">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          autoSize
          :auto-size="{ minRows: 3, maxRows: 6 }"
          allowClear
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete v-model:value="pictureForm.category"
                         placeholder="请输入分类"
                         :options="categoryOptions"
                         allowClear />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="pictureForm.tags"
        mode="tags"
        placeholder="请输入标签"
        :options="tagOptions"
        allowClear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { onMounted, reactive, ref } from 'vue'
// 正确导入语句应为：
import { useRoute, useRouter } from 'vue-router'
import { editPictureUsingPost, getPictureVoByIdUsingGet, listPictureTagCategoryUsingGet } from '@/api/pictureController'
import { message } from 'ant-design-vue'

// 正确初始化路由实例
const route = useRoute()
const router = useRouter()

/*
 * 表单数据
 */
const pictureForm = reactive<API.PictureEditRequest>({})

/*
 * 接收父组件传递的图片
 */
const picture = ref<API.PictureVO>()
/*
 * 上传成功后返回的图片，以及回显图片名称
 */
// newPicture：这是函数的参数名称（变量名），表示传入的对象。你可以随意命名，例如data、img等。
// API.PictureVO；这是 类型注解**，强制要求newPicture必须是API.PictureVO类型的对象（否则代码会报错）。
const onSuccess = (newPicture: API.PictureVO) => {
  // 赋值给pictureForm对象的picture属性
  picture.value = newPicture
  // 赋值给pictureForm对象的name属性
  pictureForm.name = newPicture.name
}
/**
 * 路由跳转组件
 */
//pictureouter 是一个钩子函数，用于在组件中获取路由对象，管理页面跳转、参数传递等导航功能


/**
 * 提交表单
 * @param values
 */
/**
 * async 关键字：声明异步函数，内部可使用 await 等待异步操作（如API请求）
 * values: any：接收表单数据的参数,any 表示任意类型
 */
const handleSubmit = async (values: any) => {
  const pictureId = picture.value.id
  if (!pictureId) {
    return
  }
  //await：等待异步操作，暂停当前代码执行，等待服务器返回结果，避免页面卡死。
  const res = await editPictureUsingPost({
    id: pictureId,
    //...values → 图片填写的表单数据（如新标题、描述等），... 是展开运算符，表示合并所有字段
    ...values,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // 跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
}
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    //map的作用把数组中的每个元素都按照固定规则转换成新形式，生成一个新数组（原数组不变）。
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })

  } else {
    message.error('加载选项失败' + res.data.message)
  }
}
//钩子函数：在组件挂载后执行
onMounted(() => {
  //在页面加载时获取标签和分类选项
  getTagCategoryOptions()
})

// 获取老数据
const getOldPicture = async () => {
  // 获取数据
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id: id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getOldPicture()
})

</script>

<style scoped>

#addPicturePage {
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

#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
