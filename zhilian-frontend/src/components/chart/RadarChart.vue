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
  series: props.data.map(item => ({
    type: 'radar',
    data: [item.value],
    name: item.name,
    areaStyle: item.areaStyle ?? { color: 'rgba(64,158,255,0.2)' },
    lineStyle: { color: '#409EFF', width: 2 },
    itemStyle: { color: '#409EFF' },
  })),
}))
</script>