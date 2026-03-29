<template>
  <div class="dashboard">
    <!-- 3D轮播区域（6张卡片） -->
    <div 
      class="carousel-section"
      @mouseenter="handleMouseEnter"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
    >
      <div class="carousel-container">
        <div 
          class="carousel-content" 
          :style="{ transform: `rotateY(${rotationAngle + offsetAngle}deg)` }"
        >
          <div 
            v-for="(card, idx) in carouselCards" 
            :key="idx"
            class="card"
            :class="{ active: idx === currentIndex }"
            :style="{
              backgroundImage: `url(${card.image})`,
              transform: getCardTransform(idx)
            }"
            @click="handleCardClick(idx)"
          >
            <div class="card-overlay">
              <h3>{{ card.title }}</h3>
              <p>{{ card.desc }}</p>
            </div>
          </div>
        </div>

        <button class="control-btn prev" @click="prev">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>
        <button class="control-btn next" @click="next">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </button>

        <div class="dots">
          <span 
            v-for="(_, idx) in carouselCards" 
            :key="idx"
            :class="['dot', { active: currentIndex === idx }]"
            @click="goTo(idx)"
          ></span>
        </div>
      </div>
    </div>

    <!-- 政策新闻面板 -->
    <div class="news-panel">
      <h3>📰 政策新闻</h3>
      <div class="news-columns">
        <div class="news-column">
          <ul>
            <li v-for="n in newsLeft" :key="n.title">
              <a :href="n.link" target="_blank">{{ n.title }}</a>
              <span class="news-date">{{ n.date }}</span>
            </li>
          </ul>
        </div>
        <div class="news-column">
          <ul>
            <li v-for="n in newsRight" :key="n.title">
              <a :href="n.link" target="_blank">{{ n.title }}</a>
              <span class="news-date">{{ n.date }}</span>
            </li>
          </ul>
        </div>
      </div>
      <div class="more-link">
        <a href="https://www.gov.cn/zhengce/" target="_blank">更多政策新闻 →</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

// ---------- 6张卡片数据 ----------
import img1 from '@/assets/1.jpeg'
import img2 from '@/assets/2.png'
import img3 from '@/assets/3.jpeg'
import img4 from '@/assets/4.png'
import img5 from '@/assets/5.png'
import img6 from '@/assets/6.png'

const carouselCards = ref([
  { title: '出海支持', desc: '国家准入指南+认证服务，解决全球市场准入难题', image: img1 },
  { title: '案例赋能', desc: '成功出海案例共享，获取实战经验参考', image: img2 },
  { title: '企业展示', desc: '制造商与服务商信息展示，提升品牌曝光', image: img3 },
  { title: '数字诊断', desc: '企业数字化水平评估，生成多维度得分分析与改进建议报告', image: img4 },
  { title: '合作市场', desc: '拓展全球合作机会，对接海外优质资源', image: img5 },
  { title: '新增企业', desc: '快速创建企业信息，完善企业资料与资质展示', image: img6 }
])
// ---------- 新闻数据 ----------
const allNews = ref([
  { title: '工业和信息化部：中小企业出海服务专项行动', date: '2025-01-15', link: 'https://www.gov.cn/zhengce/zhengceku/202501/content_7005688.htm' },
  { title: '商务部：对外投资合作国别（地区）指南', date: '2024-12-20', link: 'https://www.gov.cn/lianbo/fabu/202601/content_7059434.htm' },
  { title: '国务院新闻办就2024年一季度进出口情况举行发布会', date: '2024-08-23', link: 'https://www.gov.cn/lianbo/fabu/202404/content_6945293.htm' },
  { title: '国家外汇管理局：优化贸易外汇业务管理的通知', date: '2024-04-03', link: 'https://www.gov.cn/zhengce/zhengceku/202404/content_6943880.htm' },
  { title: '国务院：关于进一步优化外商投资环境的意见', date: '2024-08-19', link: 'https://www.gov.cn/zhengce/content/202408/content_6969123.htm' },
  { title: '国务院新闻办举行发布会 介绍市场监管服务经济高质量发展情况', date: '2024-11-28', link: 'https://www.gov.cn/lianbo/fabu/202601/content_7059434.htm' },
  { title: '税务总局：跨境税收服务举措', date: '2024-10-15', link: 'https://www.chinatax.gov.cn/chinatax/n810341/n810825/index.html' },
  { title: '国家发改委：企业参与"一带一路"建设指引', date: '2024-09-22', link: 'https://www.ndrc.gov.cn/wsdwhfz/202411/t20241114_1394466.html' }
])
const mid = Math.ceil(allNews.value.length / 2)
const newsLeft = allNews.value.slice(0, mid)
const newsRight = allNews.value.slice(mid)

// ---------- 轮播状态 ----------
const currentIndex = ref(0)
const totalCards = carouselCards.value.length  // 6张
let autoTimer = null
let isMouseInside = false
const offsetAngle = ref(0)

// 基础旋转角度（基于当前索引，每张间隔 60°）
const rotationAngle = computed(() => -currentIndex.value * (360 / totalCards))

// 获取每个卡片的 3D 变换（包含旋转、平移，激活卡片额外放大）
const getCardTransform = (idx) => {
  const angle = (idx * 360) / totalCards
  // 半径等比例放大20%：桌面504px，平板432px，手机336px
  let radius = 504
  if (window.innerWidth < 1024) radius = 432
  if (window.innerWidth < 768) radius = 336
  let transform = `rotateY(${angle}deg) translateZ(${radius}px)`
  if (idx === currentIndex.value) {
    transform += ` scale(1.25)`
  }
  return transform
}

// 切换方法
const prev = () => {
  const newIndex = (currentIndex.value - 1 + totalCards) % totalCards
  setIndex(newIndex)
}
const next = () => {
  const newIndex = (currentIndex.value + 1) % totalCards
  setIndex(newIndex)
}
const goTo = (idx) => {
  if (idx === currentIndex.value) return
  setIndex(idx)
}
const handleCardClick = (idx) => {
  goTo(idx)
}
const setIndex = (newIndex) => {
  currentIndex.value = newIndex
  if (!isMouseInside) resetAutoTimer()
}

// 自动旋转控制
const startAutoRotate = () => {
  if (autoTimer) clearInterval(autoTimer)
  autoTimer = setInterval(() => {
    if (!isMouseInside) next()
  }, 4000)
}
const pauseAutoRotate = () => {
  if (autoTimer) {
    clearInterval(autoTimer)
    autoTimer = null
  }
}
const resetAutoTimer = () => {
  pauseAutoRotate()
  startAutoRotate()
}

// 鼠标跟随（仅中间区域生效）
const handleMouseEnter = () => {
  isMouseInside = true
  pauseAutoRotate()
  offsetAngle.value = 0
}
const handleMouseMove = (e) => {
  if (!isMouseInside) return
  const container = e.currentTarget
  const rect = container.getBoundingClientRect()
  const mouseX = e.clientX - rect.left
  const width = rect.width
  const percent = mouseX / width
  const minRange = 0.3
  const maxRange = 0.7
  if (percent >= minRange && percent <= maxRange) {
    const t = (percent - minRange) / (maxRange - minRange)
    const newOffset = (t * 40) - 20
    offsetAngle.value = Math.min(20, Math.max(-20, newOffset))
  } else {
    offsetAngle.value = 0
  }
}
const handleMouseLeave = () => {
  isMouseInside = false
  offsetAngle.value = 0
  resetAutoTimer()
}

// ---------- 白色发光粒子 ----------
const createParticles = () => {
  document.querySelectorAll('.bg-particle').forEach(p => p.remove())
  const count = window.innerWidth < 768 ? 250 : 500
  for (let i = 0; i < count; i++) {
    const p = document.createElement('div')
    p.className = 'bg-particle'
    const size = 1 + Math.random() * 4
    p.style.cssText = `width:${size}px;height:${size}px;left:${Math.random() * 100}%;top:100vh;background:rgba(255,255,255,${0.7 + Math.random() * 0.3});box-shadow:0 0 ${size * 2}px rgba(255,255,255,0.8),0 0 ${size * 3}px rgba(255,200,100,0.4);animation:floatParticleUp ${8 + Math.random() * 14}s linear infinite;animation-delay:${Math.random() * 12}s`
    p.style.setProperty('--drift-x', `${(Math.random() - 0.5) * 300}px`)
    document.body.appendChild(p)
  }
}

// 窗口尺寸变化时重新渲染
let resizeTimer
const handleResize = () => {
  // 简单触发视图更新（依赖 window.innerWidth 的 transform 会重新计算）
  offsetAngle.value = offsetAngle.value
}
window.addEventListener('resize', handleResize)

// 生命周期
onMounted(() => {
  startAutoRotate()
  createParticles()
  window.addEventListener('resize', () => {
    clearTimeout(resizeTimer)
    resizeTimer = setTimeout(createParticles, 300)
  })
})
onBeforeUnmount(() => {
  if (autoTimer) clearInterval(autoTimer)
  document.querySelectorAll('.bg-particle').forEach(p => p.remove())
  window.removeEventListener('resize', handleResize)
})
</script>

<style>
/* 全局背景与粒子（同前） */
html, body { margin: 0; padding: 0; width: 100%; min-height: 100vh; }
body {
  background: radial-gradient(ellipse at 50% 30%, #1a2a4f, #030617);
  background-attachment: fixed;
  overflow-x: hidden;
}
.bg-particle {
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
  will-change: transform, opacity;
  animation: floatParticleUp linear infinite;
  opacity: 0;
}
@keyframes floatParticleUp {
  0% { transform: translateY(0) translateX(0) rotate(0deg) scale(0.8); opacity: 0; }
  10% { opacity: 0.6; }
  30% { opacity: 1; transform: translateY(-30vh) translateX(calc(var(--drift-x) * 0.3)) rotate(180deg) scale(1.2); }
  60% { opacity: 0.9; transform: translateY(-70vh) translateX(calc(var(--drift-x) * 0.7)) rotate(360deg) scale(1); }
  80% { opacity: 0.5; }
  100% { transform: translateY(-120vh) translateX(var(--drift-x)) rotate(540deg) scale(0.5); opacity: 0; }
}
@media (max-width: 768px) {
  .bg-particle { animation-duration: 10s !important; }
  @keyframes floatParticleUp {
    0% { transform: translateY(0) translateX(0); opacity: 0; }
    15% { opacity: 0.5; }
    30% { transform: translateY(-40vh) translateX(calc(var(--drift-x) * 0.5)); opacity: 0.9; }
    70% { opacity: 0.6; }
    100% { transform: translateY(-100vh) translateX(var(--drift-x)); opacity: 0; }
  }
}
body::before {
  content: '';
  position: fixed;
  top: -50%; left: -50%;
  width: 200%; height: 200%;
  background: radial-gradient(circle at 30% 40%, rgba(0,200,255,0.2) 0%, rgba(0,0,0,0) 60%);
  pointer-events: none;
  animation: rotateGlow 30s linear infinite;
  z-index: 1;
}
@keyframes rotateGlow { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
body::after {
  content: '';
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background-image: linear-gradient(rgba(0,255,255,0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(0,255,255,0.06) 1px, transparent 1px);
  background-size: 60px 60px;
  pointer-events: none;
  animation: gridMove 20s linear infinite;
  z-index: 1;
}
@keyframes gridMove { 0% { background-position: 0 0; } 100% { background-position: 60px 60px; } }
</style>

<style scoped>
/* 主容器 */
.dashboard {
  max-width: 1600px;
  margin: 0 auto;
  padding: 24px 32px;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  border-radius: 48px;
  border: 1px solid rgba(255,255,255,0.12);
  position: relative;
  z-index: 2;
}

/* 3D轮播区域 */
.carousel-section {
  width: 100%;
  height: 60vh;
  min-height: 480px;
  background: radial-gradient(circle at center, rgba(30, 40, 60, 0.7), rgba(10, 20, 35, 0.9));
  border-radius: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  margin-bottom: 40px;
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255,255,255,0.15);
}
.carousel-container {
  position: relative;
  width: 100%;
  height: 100%;
  perspective: 1600px;
  perspective-origin: 50% 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.carousel-content {
  position: absolute;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: transform 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1);
}

/* 卡片尺寸等比例放大20% */
.card {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 408px;        
  height: 252px;       
  margin-left: -204px; 
  margin-top: -126px;  
  border-radius: 24px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  box-shadow: 0 25px 40px -12px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.2) inset;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  overflow: hidden;
}
.card.active {
  box-shadow: 0 35px 55px -12px rgba(0, 0, 0, 0.8), 0 0 0 2px rgba(255, 255, 255, 0.6) inset;
  z-index: 10;
}
.card-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.5) 70%, transparent 100%);
  padding: 20px 16px 16px;
  transition: transform 0.3s ease;
}
.card:hover .card-overlay {
  transform: translateY(-4px);
}
.card-overlay h3 {
  margin: 0 0 6px;
  font-size: 1.4rem;
  font-weight: 700;
  color: white;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}
.card-overlay p {
  margin: 0;
  font-size: 0.85rem;
  color: rgba(255,255,255,0.9);
  line-height: 1.3;
}

/* 控制按钮 */
.control-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
}
.control-btn:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: translateY(-50%) scale(1.05);
}
.control-btn svg {
  width: 28px;
  height: 28px;
  stroke: white;
  stroke-width: 2;
}
.prev { left: 24px; }
.next { right: 24px; }

/* 指示点 */
.dots {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  z-index: 10;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(8px);
  padding: 8px 18px;
  border-radius: 40px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.2s ease;
}
.dot.active {
  background-color: #ffffff;
  width: 26px;
  border-radius: 12px;
  box-shadow: 0 0 6px rgba(255, 255, 255, 0.8);
}

/* 新闻面板 */
.news-panel {
  background: rgba(15, 25, 45, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 32px;
  margin: 40px 0 30px;
  padding: 28px 32px 32px;
  border: 1px solid rgba(255,255,255,0.15);
  transition: all 0.3s;
}
.news-panel:hover { transform: translateY(-2px); border-color: rgba(255,255,255,0.25); }
.news-panel h3 {
  font-size: 1.75rem;
  font-weight: 600;
  color: white;
  margin-bottom: 24px;
  border-left: 5px solid #3b82f6;
  padding-left: 18px;
  position: relative;
}
.news-panel h3::after {
  content: '';
  position: absolute;
  bottom: -8px; left: 18px;
  width: 60px; height: 3px;
  background: linear-gradient(90deg, #3b82f6, rgba(59,130,246,0.3));
  border-radius: 3px;
}
.news-columns { display: flex; gap: 48px; }
.news-column { flex: 1; }
.news-column ul { list-style: none; padding: 0; margin: 0; }
.news-column li {
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  transition: all 0.25s;
  position: relative;
}
.news-column li::before {
  content: '📌';
  position: absolute;
  left: -24px;
  opacity: 0;
  transition: opacity 0.2s, transform 0.2s;
  font-size: 12px;
  color: #3b82f6;
}
.news-column li:hover { transform: translateX(6px); border-bottom-color: #3b82f6; }
.news-column li:hover::before { opacity: 0.8; transform: translateX(-2px); }
.news-column a {
  font-size: 1rem;
  color: #e2e8f0;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}
.news-column a:hover { color: #60a5fa; text-decoration: underline; }
.news-date {
  font-size: 0.75rem;
  color: #cbd5e1;
  background: rgba(255,255,255,0.12);
  padding: 4px 10px;
  border-radius: 20px;
  white-space: nowrap;
}
.news-column li:hover .news-date { background: rgba(59,130,246,0.3); color: white; }
.more-link {
  text-align: right;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
}
.more-link a {
  font-size: 0.9rem;
  color: #60a5fa;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
}
.more-link a:hover { text-decoration: underline; letter-spacing: 0.5px; transform: translateX(4px); color: #93c5fd; }

/* 响应式适配 */
@media (max-width: 1400px) {
  .card {
    width: 360px;        
    height: 222px;      
    margin-left: -180px;
    margin-top: -111px;
  }
  .carousel-section { height: 55vh; min-height: 450px; }
}
@media (max-width: 1024px) {
  .card {
    width: 312px;        
    height: 194px;       
    margin-left: -156px;
    margin-top: -97px;
  }
  .carousel-section { height: 50vh; min-height: 400px; }
}
@media (max-width: 768px) {
  .dashboard { padding: 16px; }
  .carousel-section { margin-bottom: 24px; height: 45vh; min-height: 360px; }
  .card {
    width: 264px;        
    height: 166px;       
    margin-left: -132px;
    margin-top: -83px;
  }
  .control-btn { width: 44px; height: 44px; }
  .control-btn svg { width: 24px; height: 24px; }
  .dots { bottom: 16px; gap: 10px; padding: 6px 14px; }
  .card-overlay h3 { font-size: 1.1rem; }
  .card-overlay p { font-size: 0.7rem; }
  .news-panel { padding: 20px; }
  .news-columns { flex-direction: column; gap: 24px; }
  .news-column li { flex-direction: column; align-items: flex-start; gap: 6px; }
  .news-date { white-space: normal; align-self: flex-start; }
  .news-column li::before { display: none; }
}
@media (max-width: 480px) {
  .card {
    width: 216px;        
    height: 134px;       
    margin-left: -108px;
    margin-top: -67px;
  }
  .carousel-section { height: 40vh; min-height: 320px; }
  .control-btn { width: 38px; height: 38px; }
  .control-btn svg { width: 20px; height: 20px; }
}
</style>