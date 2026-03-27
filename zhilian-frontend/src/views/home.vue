<template>
  <div class="dashboard">
    <!-- 轮播区域（现代2D平面卡片 + 鼠标跟随偏移 + 无缝循环） -->
    <div 
      class="carousel-wrapper"
      @mouseenter="handleMouseEnter"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
    >
      <!-- 装饰性光晕元素 - 增加动态氛围 -->
      <div class="carousel-glow"></div>
      <div class="carousel-orb"></div>
      
      <div class="carousel-container" :style="{ transform: `rotateY(${offsetAngle}deg)` }">
        <div class="carousel-control prev" @click="prev">
          <i class="iconfont icon-backward-filled"></i>
        </div>
        <div class="carousel-control next" @click="next">
          <i class="iconfont icon-forward-filled"></i>
        </div>
        
        <div class="carousel-track" ref="trackRef">
          <div 
            v-for="(card, idx) in loopCards" 
            :key="idx"
            class="carousel-card"
            :class="{ 'active': idx === currentIndex }"
            @click="handleCardClick(idx)"
          >
            <div class="card-inner" :style="{ backgroundImage: `url(https://picsum.photos/id/${( (idx % originCount) +1)*20}/500/400)` }">
              <div class="card-overlay">
                <h3>{{ card.title }}</h3>
                <p>{{ card.desc }}</p>
              </div>
              <!-- 装饰性角标 -->
              <div class="card-corner"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 指示点（基于原始卡片数量） -->
      <div class="dots">
        <span 
          v-for="(_, idx) in originCards" 
          :key="idx"
          :class="['dot', { active: currentOriginIndex === idx }]"
          @click="goTo(idx)"
        ></span>
      </div>
    </div>

    <!-- 政策新闻面板 -->
    <div class="news-panel">
      <h3>📰 政策新闻</h3>
      <div class="news-columns">
        <div class="news-column">
          <ul>
            <li v-for="(news, idx) in newsLeftColumn" :key="idx">
              <a :href="news.link" target="_blank" rel="noopener noreferrer">{{ news.title }}</a>
              <span class="news-date">{{ news.date }}</span>
            </li>
          </ul>
        </div>
        <div class="news-column">
          <ul>
            <li v-for="(news, idx) in newsRightColumn" :key="idx">
              <a :href="news.link" target="_blank" rel="noopener noreferrer">{{ news.title }}</a>
              <span class="news-date">{{ news.date }}</span>
            </li>
          </ul>
        </div>
      </div>
      <div class="more-link">
        <a href="https://www.gov.cn/zhengce/" target="_blank" rel="noopener noreferrer">更多政策新闻 →</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

// ---------- 原始卡片数据 ----------
const originCards = ref([
  { title: '出海领航', desc: '出海领航助力企业远航' },
  { title: '认证无忧', desc: '认证无忧畅通出海之路' },
  { title: '案例赋能', desc: '案例赋能加速出海进程' },
  { title: '精准服务', desc: '精准服务贴合企业需求' },
  { title: '合规护航', desc: '合规护航保障出海安全' },
  { title: '智慧出海', desc: '智慧出海引领行业风向' },
  { title: '企业出海', desc: '企业出海拓展全球版图' },
  { title: '全球互联', desc: '全球互联共筑出海生态' }
])

// ---------- 构建三份循环卡片列表 ----------
const originCount = originCards.value.length
const loopCards = ref([...originCards.value, ...originCards.value, ...originCards.value])

// ---------- 新闻数据（真实链接）----------
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

// 新闻分列
const newsLeftColumn = ref([])
const newsRightColumn = ref([])
const splitNews = () => {
  const mid = Math.ceil(allNews.value.length / 2)
  newsLeftColumn.value = allNews.value.slice(0, mid)
  newsRightColumn.value = allNews.value.slice(mid)
}
splitNews()

// ---------- 轮播状态 ----------
const trackRef = ref(null)
let currentIndex = ref(originCount)           // 实际索引（初始指向中间区域的第一张）
let isAnimating = false
let pendingIndex = null
let autoPlayTimer = null
let autoPlayEnabled = true
const autoPlayInterval = 4000

let cardElements = []
let cardWidth = 320
let cardGap = 24

// 鼠标跟随
let isMouseInside = false
const offsetAngle = ref(0)

// 当前显示的原始索引（用于指示点高亮）
const currentOriginIndex = computed(() => currentIndex.value % originCount)

// ---------- 辅助函数 ----------
const updateCardSizes = () => {
  if (!trackRef.value) return
  const container = trackRef.value.parentElement
  const containerRect = container.getBoundingClientRect()
  const containerWidth = containerRect.width

  // 响应式卡片宽度
  if (containerWidth < 640) {
    cardWidth = 260
    cardGap = 16
  } else if (containerWidth < 1024) {
    cardWidth = 280
    cardGap = 20
  } else {
    cardWidth = 320
    cardGap = 24
  }

  // 设置每张卡片的宽度和右边距
  cardElements.forEach(card => {
    card.style.width = `${cardWidth}px`
    card.style.marginRight = `${cardGap}px`
  })

  // 设置 track 总宽度
  const totalWidth = cardElements.length * (cardWidth + cardGap)
  trackRef.value.style.width = `${totalWidth}px`

  // 重新定位当前卡片（无动画）
  if (!isAnimating) {
    const offset = calculateOffset(currentIndex.value)
    trackRef.value.style.transform = `translateX(${offset}px)`
  }
}

const calculateOffset = (index) => {
  if (!trackRef.value || cardElements.length === 0) return 0
  const container = trackRef.value.parentElement
  const containerCenter = container.clientWidth / 2
  const card = cardElements[index]
  if (!card) return 0

  const cardRect = card.getBoundingClientRect()
  const trackRect = trackRef.value.getBoundingClientRect()
  const cardCenterRelative = cardRect.left + cardRect.width / 2 - trackRect.left
  return containerCenter - cardCenterRelative
}

// 核心移动方法（带边界重置，实现无缝循环）
const moveToIndex = (targetIndex, fromUser = false) => {
  if (isAnimating) {
    pendingIndex = targetIndex
    return
  }
  if (targetIndex < 0 || targetIndex >= cardElements.length) return
  if (currentIndex.value === targetIndex && !fromUser) return

  isAnimating = true
  currentIndex.value = targetIndex
  const offset = calculateOffset(targetIndex)
  trackRef.value.style.transform = `translateX(${offset}px)`

  const onTransitionEnd = () => {
    isAnimating = false
    trackRef.value.removeEventListener('transitionend', onTransitionEnd)

    // 边界重置：如果滑到前置副本或后置副本，立即跳转到中间区域对应位置（无动画）
    const lowerBound = originCount
    const upperBound = loopCards.value.length - originCount
    if (currentIndex.value <= lowerBound - 1 || currentIndex.value >= upperBound) {
      let newIndex
      if (currentIndex.value <= lowerBound - 1) {
        newIndex = currentIndex.value + originCount
      } else {
        newIndex = currentIndex.value - originCount
      }
      // 无动画重置位置
      trackRef.value.style.transition = 'none'
      currentIndex.value = newIndex
      const newOffset = calculateOffset(newIndex)
      trackRef.value.style.transform = `translateX(${newOffset}px)`
      // 强制重绘后恢复过渡
      trackRef.value.offsetHeight
      trackRef.value.style.transition = 'transform 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1)'
    }

    if (pendingIndex !== null) {
      const idx = pendingIndex
      pendingIndex = null
      moveToIndex(idx)
    }
  }
  trackRef.value.addEventListener('transitionend', onTransitionEnd)

  if (fromUser && autoPlayEnabled) {
    stopAutoPlay()
    startAutoPlay()
  }
}

// 上一张 / 下一张
const prev = () => {
  let newIndex = currentIndex.value - 1
  moveToIndex(newIndex, true)
}

const next = () => {
  let newIndex = currentIndex.value + 1
  moveToIndex(newIndex, true)
}

// 跳转到指定原始索引
const goTo = (originIdx) => {
  // 找到当前中间区域中对应原始索引的实际卡片索引
  const base = Math.floor(currentIndex.value / originCount) * originCount
  let targetIndex = base + originIdx
  // 边界保护（确保在有效范围内）
  if (targetIndex < 0) targetIndex = 0
  if (targetIndex >= loopCards.value.length) targetIndex = loopCards.value.length - 1
  moveToIndex(targetIndex, true)
}

const handleCardClick = (idx) => {
  if (currentIndex.value === idx) {
    stopAutoPlay()
  } else {
    moveToIndex(idx, true)
  }
}

// 自动播放控制
const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
  autoPlayEnabled = false
}

const startAutoPlay = () => {
  if (!autoPlayEnabled) return
  if (autoPlayTimer) clearInterval(autoPlayTimer)
  autoPlayTimer = setInterval(() => {
    if (isAnimating || isMouseInside) return
    next()
  }, autoPlayInterval)
}

const resumeAutoPlay = () => {
  if (!autoPlayEnabled) {
    autoPlayEnabled = true
    startAutoPlay()
  } else {
    startAutoPlay()
  }
}

// 鼠标跟随
const handleMouseEnter = () => {
  isMouseInside = true
  offsetAngle.value = 0
}

const handleMouseMove = (e) => {
  if (!isMouseInside) return
  const container = e.currentTarget
  const rect = container.getBoundingClientRect()
  const mouseX = e.clientX - rect.left
  const width = rect.width
  let percent = (mouseX / width) * 2 - 1 // -1 到 1
  const maxOffset = 15
  let newOffset = percent * maxOffset
  newOffset = Math.min(maxOffset, Math.max(-maxOffset, newOffset))
  offsetAngle.value = newOffset
}

const handleMouseLeave = () => {
  isMouseInside = false
  offsetAngle.value = 0
  if (!autoPlayEnabled) {
    autoPlayEnabled = true
    startAutoPlay()
  }
}

// ---------- 生命周期 ----------
onMounted(() => {
  if (trackRef.value) {
    cardElements = Array.from(trackRef.value.children)
    if (cardElements.length) {
      // 初始索引指向中间区域的第一张卡片
      currentIndex.value = originCount
      updateCardSizes()
      trackRef.value.style.transition = 'transform 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1)'
      const initOffset = calculateOffset(currentIndex.value)
      trackRef.value.style.transform = `translateX(${initOffset}px)`
      startAutoPlay()
    }
  }
  window.addEventListener('resize', () => {
    if (!isAnimating) {
      updateCardSizes()
    }
  })

  // 添加漂浮粒子
  for (let i = 0; i < 80; i++) {
    const particle = document.createElement('div')
    particle.classList.add('particle')
    particle.style.left = Math.random() * 100 + '%'
    particle.style.animationDelay = Math.random() * 15 + 's'
    particle.style.animationDuration = 10 + Math.random() * 10 + 's'
    document.body.appendChild(particle)
  }
})

onBeforeUnmount(() => {
  if (autoPlayTimer) clearInterval(autoPlayTimer)
  window.removeEventListener('resize', updateCardSizes)
})
</script>

<!-- 全局样式：深空背景 + 粒子动画 -->
<style>
/* 全局背景 - 确保全屏且覆盖所有内容 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
}

body {
  background: radial-gradient(ellipse at 50% 30%, #1a2a4f, #030617);
  background-attachment: fixed;
  position: relative;
  overflow-x: hidden;
}

/* 巨型光晕层 - 缓慢旋转 */
body::before {
  content: '';
  position: fixed;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 40%, rgba(0, 200, 255, 0.25) 0%, rgba(0, 0, 0, 0) 60%);
  pointer-events: none;
  animation: rotateGlow 30s linear infinite;
  z-index: 0;
}

@keyframes rotateGlow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 流动光线网格 - 动态扫描效果 */
body::after {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(0, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 255, 0.08) 1px, transparent 1px);
  background-size: 60px 60px;
  pointer-events: none;
  animation: gridMove 20s linear infinite;
  z-index: 0;
}

@keyframes gridMove {
  0% { background-position: 0 0; }
  100% { background-position: 60px 60px; }
}

/* 漂浮粒子 */
.particle {
  position: fixed;
  width: 2px;
  height: 2px;
  background: rgba(0, 200, 255, 0.7);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
  animation: floatParticle 15s linear infinite;
}

@keyframes floatParticle {
  0% {
    transform: translateY(100vh) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-20vh) translateX(100px);
    opacity: 0;
  }
}
</style>

<style scoped>
/* 主容器 - 半透明玻璃，降低模糊让背景更清晰 */
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  border-radius: 48px;
  box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.12);
  position: relative;
  z-index: 2;
}

/* 轮播外层 - 保持不变，但增强阴影 */
.carousel-wrapper {
  position: relative;
  width: 100%;
  margin: 20px 0 40px;
  background: linear-gradient(135deg, #0a4b8a 0%, #2b0b3a 100%);
  border-radius: 48px;
  padding: 40px 0;
  box-shadow: 0 20px 35px -10px rgba(0, 0, 0, 0.5), inset 0 1px 2px rgba(255, 255, 255, 0.2);
  overflow: hidden;
  isolation: isolate;
}

/* 轮播内部光晕动画 */
.carousel-glow {
  position: absolute;
  top: -50%;
  left: -20%;
  width: 140%;
  height: 200%;
  background: radial-gradient(circle at 30% 50%, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0) 70%);
  pointer-events: none;
  animation: floatGlow 12s ease-in-out infinite;
  z-index: 0;
}

.carousel-orb {
  position: absolute;
  bottom: -30%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(0, 255, 255, 0.2) 0%, rgba(0, 255, 255, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
  filter: blur(40px);
  animation: orbPulse 8s ease-in-out infinite;
  z-index: 0;
}

@keyframes floatGlow {
  0%, 100% { transform: translateX(-5%) translateY(-5%) rotate(0deg); opacity: 0.6; }
  50% { transform: translateX(5%) translateY(5%) rotate(2deg); opacity: 1; }
}

@keyframes orbPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.2); opacity: 0.6; }
}

.carousel-container {
  position: relative;
  width: 100%;
  overflow: hidden;
  transform-style: preserve-3d;
  transition: transform 0.2s ease-out;
  z-index: 2;
}

.carousel-track {
  display: flex;
  align-items: center;
  will-change: transform;
  transition: transform 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  cursor: pointer;
}

/* 卡片样式 */
.carousel-card {
  flex-shrink: 0;
  transition: all 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  filter: blur(0px) brightness(0.8) saturate(0.8);
  opacity: 0.6;
  transform: scale(0.88);
  will-change: transform, filter, opacity;
}

.carousel-card.active {
  filter: blur(0px) brightness(1) saturate(1.1);
  opacity: 1;
  transform: scale(1);
  z-index: 10;
}

.card-inner {
  position: relative;
  border-radius: 28px;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  aspect-ratio: 4 / 3;
  box-shadow: 0 20px 35px -12px rgba(0, 0, 0, 0.3);
  transition: all 0.4s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(2px);
}

.carousel-card.active .card-inner {
  box-shadow: 0 30px 45px -12px rgba(0, 0, 0, 0.5), 0 0 0 2px rgba(255, 255, 255, 0.3);
  transform: translateY(-6px);
}

.card-corner {
  position: absolute;
  top: 0;
  right: 0;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 70%);
  clip-path: polygon(0 0, 100% 0, 100% 100%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 2;
}

.carousel-card.active .card-corner {
  opacity: 0.6;
}

.card-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9) 0%, rgba(0, 0, 0, 0.5) 60%, transparent 100%);
  padding: 28px 20px 20px;
  backdrop-filter: blur(4px);
  transition: all 0.3s;
}

.carousel-card.active .card-overlay {
  backdrop-filter: blur(6px);
  background: linear-gradient(to top, rgba(0, 0, 0, 0.85) 0%, rgba(0, 0, 0, 0.4) 70%, transparent 100%);
}

.card-overlay h3 {
  margin: 0 0 8px;
  font-size: 1.6rem;
  font-weight: 700;
  color: white;
  letter-spacing: -0.01em;
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);
  transition: transform 0.2s;
}

.carousel-card.active .card-overlay h3 {
  transform: translateY(-2px);
}

.card-overlay p {
  margin: 0;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.4;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 左右箭头 */
.carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 52px;
  height: 52px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12px);
  border-radius: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 20;
  transition: all 0.3s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  color: white;
  font-size: 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.carousel-control:hover {
  background: white;
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.25);
  color: #1e293b;
  border-color: transparent;
}

.carousel-control.prev { left: 24px; }
.carousel-control.next { right: 24px; }

/* 指示点 */
.dots {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  z-index: 15;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(12px);
  padding: 10px 24px;
  border-radius: 60px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.2, 0.9, 0.4, 1.1);
}

.dot:hover {
  background: white;
  transform: scale(1.3);
}

.dot.active {
  background: white;
  width: 28px;
  border-radius: 12px;
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.6);
}

/* 新闻面板 - 深色玻璃，微调透明度让背景可见 */
.news-panel {
  background: rgba(15, 25, 45, 0.65);
  backdrop-filter: blur(8px);
  border-radius: 32px;
  margin: 40px 0 30px;
  padding: 28px 32px 32px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s ease;
}

.news-panel:hover {
  box-shadow: 0 25px 40px -12px rgba(0, 0, 0, 0.3);
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.25);
}

.news-panel h3 {
  font-size: 1.75rem;
  font-weight: 600;
  color: white;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-left: 5px solid #3b82f6;
  padding-left: 18px;
  position: relative;
}

.news-panel h3::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 18px;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #3b82f6, rgba(59, 130, 246, 0.3));
  border-radius: 3px;
}

.news-columns {
  display: flex;
  gap: 48px;
}

.news-column {
  flex: 1;
}

.news-column ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.news-column li {
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  transition: all 0.25s ease;
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

.news-column li:hover {
  transform: translateX(6px);
  border-bottom-color: #3b82f6;
}

.news-column li:hover::before {
  opacity: 0.8;
  transform: translateX(-2px);
}

.news-column a {
  font-size: 1rem;
  color: #e2e8f0;
  text-decoration: none;
  font-weight: 500;
  line-height: 1.4;
  transition: color 0.2s;
}

.news-column a:hover {
  color: #60a5fa;
  text-decoration: underline;
}

.news-date {
  font-size: 0.75rem;
  color: #cbd5e1;
  background: rgba(255, 255, 255, 0.12);
  padding: 4px 10px;
  border-radius: 20px;
  white-space: nowrap;
  transition: all 0.2s;
}

.news-column li:hover .news-date {
  background: rgba(59, 130, 246, 0.3);
  color: white;
}

.more-link {
  text-align: right;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.more-link a {
  font-size: 0.9rem;
  color: #60a5fa;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.more-link a:hover {
  text-decoration: underline;
  letter-spacing: 0.5px;
  transform: translateX(4px);
  color: #93c5fd;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .carousel-wrapper {
    padding: 20px 0;
    border-radius: 32px;
  }
  .carousel-control {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }
  .carousel-control.prev { left: 12px; }
  .carousel-control.next { right: 12px; }
  .card-overlay h3 { font-size: 1.2rem; }
  .card-overlay p { font-size: 0.75rem; }
  .news-panel { padding: 20px; }
  .news-columns { flex-direction: column; gap: 24px; }
  .news-column li { flex-direction: column; align-items: flex-start; gap: 6px; }
  .news-date { white-space: normal; align-self: flex-start; }
  .dots { bottom: 16px; gap: 10px; padding: 6px 16px; }
  .news-column li::before { display: none; }
}
</style>