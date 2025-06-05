<!--用户管理页面-->
<template>
  <div id="userManagePage">
    <!--搜索框-->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="请输入账号" allow-clear />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="请输入用户名" allow-clear />
      </a-form-item>
      <a-form-item label="用户角色">
        <a-select
          v-model:value="searchParams.userRole"
          placeholder="请选择用户身份"
          allow-clear
          style="width: 200px"
        >
          <a-select-option value="">全部身份</a-select-option>
          <a-select-option value="user">普通用户</a-select-option>
          <a-select-option value="admin">管理员</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
<div style="margin-bottom: 16px"></div>
    <!--表格-->
    <!--      columns：配置表头及列行为（如排序、筛选）
      data-source：提供表格展示的数据
      paging：管理分页逻辑
      @change：响应表格交互事件-->
    <a-table :columns="columns" :data-source="dataList" :pagination="paging" @change="doTableChange">
      <!-- 通过解构获取当前列(column)和当前行数据(record) -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="80" />
        </template>
        <template v-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'user'">
            <a-tag color="blue">普通用户</a-tag>
          </div>
          <div v-else-if="record.userRole === 'admin'">
            <a-tag color="red">管理员</a-tag>
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          <!--格式化时间-->
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'updateTime'">
          <!--格式化时间-->
          {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <!-- 操作  -->
        <template v-else-if="column.key === 'action'">
          <a-button danger @click="doDelete(record.id)">删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUserUsingPost, listUserVoByPageUsingPost } from '@/api/userController'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
// 表格列定义
const columns = [
  {
    title: 'id',
    //dataIndex 是数据中的字段名，用于从数据中获取对应的值
    // 这个字段名是后端返回的数据中的字段名，用于从数据中获取对应的值
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]


// 数据
const dataList = ref([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  userName: '',
  userAccount: '',
  userRole: '',
})

// 获取数据
const fetchData = async () => {
  // 调用接口获取数据
  // 这里使用了展开运算符...，将响应式对象searchParams展开为普通对象
  const res = await listUserVoByPageUsingPost({
    //使用展开运算符...传递响应式对象（自动解构当前值）
    // 这样，当响应式对象发生变化时，会自动更新到函数的参数中
    ...searchParams,
  })
  if (res.data.data) {
    // ?? 空值合并运算符提供默认值，增强健壮性
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
    console.log(res.data.data.records)
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const paging = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    // 是否显示快速跳转
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})
// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  // 调用数据获取函数，使用更新后的参数重新加载数据
  fetchData()
}
// 获取数据
const doSearch = () => {
  // 重置页码, 每次搜索都从第一页开始(不然会保留上一次的页码，导致查不出数据)
  searchParams.current = 1
  // 调用数据获取函数，使用更新后的参数重新加载数据
  fetchData()
}

//删除用户
const doDelete = async (id: number) => {
 if(id==null){
   return
 }
 //因为是body传参，所以要传一个对象
 const res = await deleteUserUsingPost({id})
 if(res.data.data){
   message.success('删除成功')
   fetchData()
 }
 else{
   message.error('删除失败')
 }

}

</script>
