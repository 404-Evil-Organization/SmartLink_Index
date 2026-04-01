<template>
  <BaseChart :options="chartOptions" v-bind="$attrs" />
</template>

<script setup>
import { computed } from "vue";
import BaseChart from "./BaseChart.vue";

const props = defineProps({
  xAxisData: { type: Array, required: true },
  series: { type: Array, required: true },
  title: { type: String, default: "" },
  yAxisName: { type: String, default: "" },
  smooth: { type: Boolean, default: false },    // 是否平滑曲线
  areaStyle: { type: Boolean, default: false }, // 是否填充面积
});

const chartOptions = computed(() => ({
  title: props.title ? { text: props.title, left: "center" } : undefined,
  tooltip: { trigger: "axis" },
  grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
  xAxis: { type: "category", data: props.xAxisData },
  yAxis: { type: "value", name: props.yAxisName },
  series: props.series.map(s => ({
    // 先展开调用方传入的配置，再覆盖组件固定字段，避免被外部篡改
    ...s,
    type: "line",
    smooth: props.smooth,
    areaStyle: props.areaStyle ? {} : undefined,
  })),
}));
</script>