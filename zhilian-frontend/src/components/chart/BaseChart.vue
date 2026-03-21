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
  resize: { type: Boolean, default: true },     // 是否监听窗口大小变化（包括窗口和容器）
})

const chartRef = ref(null)
let chartInstance = null
let resizeObserver = null

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

// 监听 options 变化：约定上层以不可变方式整体替换 options（浅监听即可）
watch(() => props.options, updateChart)

// 监听宽高 props 变化，触发图表自适应
watch(
  () => [props.width, props.height],
  () => {
    // 等待 DOM 根据新的宽高样式更新后再 resize，避免测量不准确
    nextTick(() => {
      chartInstance?.resize()
    })
  }
)

// 窗口大小 / 容器大小变化处理
const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  nextTick(() => {
    initChart()
  })

  if (props.resize) {
    // 1. 继续监听 window.resize，兼容老逻辑
    window.addEventListener('resize', handleResize)

    // 2. 使用 ResizeObserver 监听容器尺寸变化，处理父容器宽高变化但未触发 window.resize 的情况
    if (window.ResizeObserver && chartRef.value) {
      resizeObserver = new ResizeObserver(() => {
        chartInstance?.resize()
      })
      resizeObserver.observe(chartRef.value)
    }
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)

  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }

  chartInstance?.dispose()
  chartInstance = null
})
</script>