<template>
  <div class="chart-test-container">
    <h1>📊 图表组件测试页面</h1>
    <p>该页面用于验证封装的 RadarChart、BarChart、LineChart 组件是否正常工作。</p>

    <!-- 雷达图测试 -->
    <el-card class="chart-card" shadow="hover">
      <h2>雷达图 (RadarChart)</h2>
      <RadarChart
        :indicator="[
          { name: '信息化水平', max: 5 },
          { name: '自动化水平', max: 5 },
          { name: '数据应用', max: 5 },
          { name: '服务协同', max: 5 }
        ]"
        :data="[
          { value: [4, 3, 2, 5], name: '企业A', areaStyle: { color: 'rgba(64,158,255,0.2)' } },
          { value: [5, 4, 4, 3], name: '企业B' }
        ]"
        height="350px"
        title="企业数字化能力对比"
      />
    </el-card>

    <!-- 柱状图测试 -->
    <el-card class="chart-card" shadow="hover">
      <h2>柱状图 (BarChart)</h2>
      <BarChart
        :xAxisData="['一月', '二月', '三月', '四月', '五月']"
        :series="[
          { name: '销售额', data: [120, 200, 150, 80, 270] },
          { name: '利润', data: [30, 45, 35, 20, 60] }
        ]"
        title="月度经营数据"
        yAxisName="金额（万元）"
        height="350px"
      />
    </el-card>

    <!-- 折线图测试 -->
    <el-card class="chart-card" shadow="hover">
      <h2>折线图 (LineChart)</h2>
      <LineChart
        :xAxisData="['Q1', 'Q2', 'Q3', 'Q4']"
        :series="[
          { name: '增长率', data: [5, 8, 12, 9] },
          { name: '行业平均', data: [4, 6, 9, 8] }
        ]"
        :smooth="true"
        :areaStyle="true"
        title="年度增长率对比"
        yAxisName="%"
        height="350px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { RadarChart, BarChart, LineChart } from '@/components/chart'
</script>

<style scoped>
.chart-test-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}
.chart-card {
  margin-bottom: 24px;
  border-radius: 12px;
}
.chart-card h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  color: #1f2f3d;
}
</style>