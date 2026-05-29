<template>
  <div id="addSpaceView">
    <div style="margin-bottom: 24px">
      <h2>{{ isEdit ? '编辑空间' : route.query.type == SPACE_TYPE_ENUM.TEAM ? '创建团队' : '创建空间' }}</h2>
      <div style="color: #666; margin-top: 8px">
        {{ isEdit ? '修改空间基础信息' : route.query.type == SPACE_TYPE_ENUM.TEAM ? '创建团队空间，邀请成员共同协作管理图片' : '创建个人空间，管理您的私有图片资源' }}
      </div>
    </div>

    <a-card :bordered="false">
      <a-form
        :model="formData"
        name="addSpace"
        layout="vertical"
        @finish="handleSubmit"
        :style="{ maxWidth: '600px' }"
      >
        <a-form-item
          label="空间名称"
          name="spaceName"
          :rules="[{ required: true, message: '请输入空间名称' }]"
        >
          <a-input v-model:value="formData.spaceName" placeholder="请输入空间名称" />
        </a-form-item>

        <a-form-item label="空间级别" name="spaceLevel">
          <a-select
            v-model:value="formData.spaceLevel"
            :options="SPACE_LEVEL_OPTIONS"
            placeholder="请选择空间级别"
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%">
            {{ isEdit ? '保存修改' : '立即创建' }}
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  addSpaceUsingPost,
  getSpaceByIdUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController'
import { SPACE_LEVEL_OPTIONS, SPACE_TYPE_ENUM } from '@/constants/space'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const spaceId = computed(() => route.params.id as string | undefined)
const isEdit = computed(() => !!spaceId.value)

const formData = reactive<API.SpaceAddRequest>({
  spaceName: '',
  spaceLevel: 0,
  spaceType: undefined,
})

onMounted(async () => {
  if (isEdit.value && spaceId.value) {
    await fetchSpaceDetail(spaceId.value)
    return
  }
  const type = Number(route.query.type)
  if (!isNaN(type)) {
    formData.spaceType = type
  }
})

const fetchSpaceDetail = async (id: string) => {
  loading.value = true
  try {
    const res = await getSpaceByIdUsingGet({ id })
    if (res.data.code === 0 && res.data.data) {
      formData.spaceName = res.data.data.spaceName
      formData.spaceLevel = res.data.data.spaceLevel
      formData.spaceType = res.data.data.spaceType
    } else {
      message.error('获取空间信息失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('获取空间信息失败，' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async (values: API.SpaceAddRequest) => {
  loading.value = true
  try {
    if (isEdit.value && spaceId.value) {
      const res = await updateSpaceUsingPost({
        id: spaceId.value,
        ...formData,
        ...values,
      })
      if (res.data.code === 0) {
        message.success('修改成功')
        router.push('/admin/spaceManage')
      } else {
        message.error('修改失败，' + res.data.message)
      }
      return
    }
    const res = await addSpaceUsingPost({
      ...formData,
      ...values,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('创建成功')
      router.push(`/space/${res.data.data}`)
    } else {
      message.error('创建失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error((isEdit.value ? '修改失败，' : '创建失败，') + error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
#addSpaceView {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
