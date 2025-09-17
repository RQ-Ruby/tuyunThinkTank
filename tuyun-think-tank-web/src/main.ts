/**
 * 全局入口文件
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import App from './App.vue'
import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'
import router from './router'
import '@/permission.ts'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(VueCropper)

app.mount('#app')










