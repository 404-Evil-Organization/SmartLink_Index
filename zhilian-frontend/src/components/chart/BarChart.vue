<template>
  <BaseChart :options="chartOptions" v-bind="$attrs" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from './BaseChart.vue'

const props = defineProps({
  xAxisData: { type: Array, required: true },   // x轴类别数据，如 ['周一','周二']
  series: { type: Array, required: true },      // 系列数据，如 [{ name: '销量', data: [5,20] }]
  title: { type: String, default: '' },
  yAxisName: { type: String, default: '' },
  barWidth: { type: [String, Number], default: '60%' },
})

const chartOptions = computed(() => ({
  title: props.title ? { text: props.title, left: 'center' } : undefined,
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: props.xAxisData, axisLabel: { rotate: 0 } },
  yAxis: { type: 'value', name: props.yAxisName },
  series: props.series.map(s => ({
    ...s,
    // 固定为柱状图类型，防止调用方通过 series 覆盖 type 破坏组件语义
    type: 'bar',
    // 统一柱宽，如需允许自定义可改为优先使用 s.barWidth
    barWidth: props.barWidth,
  })),
}))
</script>