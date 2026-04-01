<template>
  <BaseChart :options="chartOptions" v-bind="$attrs" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from './BaseChart.vue'

const props = defineProps({
  indicator: { type: Array, required: true }, // 指示器，例如 [{ name: '信息化', max: 5 }]
  data: { type: Array, required: true },      // 数据系列，例如 [{ value: [4,3,2,5], name: '企业A', areaStyle: {...} }]
  title: { type: String, default: '' },
  radarShape: { type: String, default: 'circle' }, // 'circle' 或 'polygon'
})

const chartOptions = computed(() => ({
  title: props.title ? { text: props.title, left: 'center' } : undefined,
  tooltip: { trigger: 'item' },
  radar: {
    indicator: props.indicator,
    shape: props.radarShape,
    center: ['50%', '50%'],
    radius: '65%',
  },
  // 使用单个 radar series，将每个数据项映射为 data 数组中的一个元素
  series: [
    {
      type: 'radar',
      data: props.data.map(item => ({
        // 每条雷达线的数据值
        value: item.value,
        // 用于 legend / tooltip 显示的名称
        name: item.name,
        // 若调用方在 props.data 中传入自定义样式，则透传给 ECharts；
        // 否则使用 ECharts 默认调色板，不再强制统一为蓝色
        areaStyle: item.areaStyle,
        lineStyle: item.lineStyle,
        itemStyle: item.itemStyle,
      })),
    },
  ],
}))
</script>