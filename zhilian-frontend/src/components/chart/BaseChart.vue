<template>
  <div ref="chartRef" :style="{ width, height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  options: { type: Object, required: true },    // ECharts 配置项
  theme: { type: String, default: '' },         // 主题名称
  width: { type: String, default: '100%' },
  height: { type: String, default: '400px' },
  resize: { type: Boolean, default: true },     // 是否监听窗口大小变化
})

const chartRef = ref(null)
let chartInstance = null

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  // 如果已存在实例，先销毁
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value, props.theme)
  chartInstance.setOption(props.options)
}

// 更新图表（不重新创建实例）
const updateChart = () => {
  if (chartInstance) {
    chartInstance.setOption(props.options, { notMerge: false })
  } else {
    initChart()
  }
}

// 监听 options 变化
watch(() => props.options, updateChart, { deep: true })

// 窗口大小变化处理
const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  nextTick(() => {
    initChart()
  })
  if (props.resize) {
    window.addEventListener('resize', handleResize)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>