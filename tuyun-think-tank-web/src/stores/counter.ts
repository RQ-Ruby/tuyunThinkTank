
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
//一个状态就存一类要共享的数据
export const useCounterStore = defineStore('counter', () => {
  //定义初始值
  //定义变量的计算逻辑
  const count = ref(0)
  const doubleCount = computed(() => count.value * 2)
//定义怎么更改状态
  function increment() {
    count.value++
  }
//返回
  return { count, doubleCount, increment }
})
