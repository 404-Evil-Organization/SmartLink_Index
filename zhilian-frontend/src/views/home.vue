<template>
  <div class="dashboard">
    <!-- 轮播区域 -->
    <div class="carousel-wrapper" @mouseenter="handleMouseEnter" @mousemove="handleMouseMove" @mouseleave="handleMouseLeave">
      <div class="carousel-glow"></div>
      <div class="carousel-orb"></div>
      <div class="carousel-container" :style="{ transform: `rotateY(${offsetAngle}deg)` }">
        <div class="carousel-control prev" @click="prev">‹</div>
        <div class="carousel-control next" @click="next">›</div>
        <div class="carousel-track" ref="trackRef">
          <div v-for="(card, idx) in loopCards" :key="idx" class="carousel-card" :class="{ active: idx === currentIndex }" @click="handleCardClick(idx)">
            <div class="card-inner" :style="{ backgroundImage: `url(https://picsum.photos/id/${((idx % originCount) + 1) * 20}/500/400)` }">
              <div class="card-overlay">
                <h3>{{ card.title }}</h3>
                <p>{{ card.desc }}</p>
              </div>
              <div class="card-corner"></div>
            </div>
          </div>
        </div>
      </div>
      <div class="dots">
        <span v-for="(_, idx) in originCards" :key="idx" :class="['dot', { active: currentOriginIndex === idx }]" @click="goTo(idx)"></span>
      </div>
    </div>

    <!-- 新闻面板 -->
    <div class="news-panel">
      <h3>📰 政策新闻</h3>
      <div class="news-columns">
        <div class="news-column"><ul><li v-for="n in newsLeft" :key="n.title"><a :href="n.link" target="_blank">{{ n.title }}</a><span class="news-date">{{ n.date }}</span></li></ul></div>
        <div class="news-column"><ul><li v-for="n in newsRight" :key="n.title"><a :href="n.link" target="_blank">{{ n.title }}</a><span class="news-date">{{ n.date }}</span></li></ul></div>
      </div>
      <div class="more-link"><a href="https://www.gov.cn/zhengce/" target="_blank">更多政策新闻 →</a></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

// 卡片数据
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
const originCount = originCards.value.length
const loopCards = ref([...originCards.value, ...originCards.value, ...originCards.value])

// 新闻数据
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

// 轮播状态
const trackRef = ref(null)
let currentIndex = ref(originCount)
let isAnimating = false, pendingIndex = null, autoPlayTimer = null, autoPlayEnabled = true
let cardElements = [], cardWidth = 320, cardGap = 24
let isMouseInside = false
const offsetAngle = ref(0)
const currentOriginIndex = computed(() => currentIndex.value % originCount)

// 辅助函数
const updateCardSizes = () => {
  if (!trackRef.value) return
  const w = trackRef.value.parentElement.clientWidth
  if (w < 640) { cardWidth = 260; cardGap = 16 }
  else if (w < 1024) { cardWidth = 280; cardGap = 20 }
  else { cardWidth = 320; cardGap = 24 }
  cardElements.forEach(c => { c.style.width = `${cardWidth}px`; c.style.marginRight = `${cardGap}px` })
  trackRef.value.style.width = `${cardElements.length * (cardWidth + cardGap)}px`
  if (!isAnimating) trackRef.value.style.transform = `translateX(${calculateOffset(currentIndex.value)}px)`
}
const calculateOffset = (idx) => {
  if (!trackRef.value || !cardElements[idx]) return 0
  const container = trackRef.value.parentElement
  const cardCenter = cardElements[idx].getBoundingClientRect().left + cardWidth / 2 - trackRef.value.getBoundingClientRect().left
  return container.clientWidth / 2 - cardCenter
}
const moveToIndex = (target, fromUser = false) => {
  if (isAnimating) { pendingIndex = target; return }
  if (target < 0 || target >= cardElements.length || (currentIndex.value === target && !fromUser)) return
  isAnimating = true
  currentIndex.value = target
  trackRef.value.style.transform = `translateX(${calculateOffset(target)}px)`
  const onEnd = () => {
    isAnimating = false
    trackRef.value.removeEventListener('transitionend', onEnd)
    const lower = originCount, upper = loopCards.value.length - originCount
    if (currentIndex.value <= lower - 1 || currentIndex.value >= upper) {
      const newIdx = currentIndex.value <= lower - 1 ? currentIndex.value + originCount : currentIndex.value - originCount
      trackRef.value.style.transition = 'none'
      currentIndex.value = newIdx
      trackRef.value.style.transform = `translateX(${calculateOffset(newIdx)}px)`
      trackRef.value.offsetHeight
      trackRef.value.style.transition = 'transform 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1)'
    }
    if (pendingIndex !== null) { const idx = pendingIndex; pendingIndex = null; moveToIndex(idx) }
  }
  trackRef.value.addEventListener('transitionend', onEnd)
  if (fromUser && autoPlayEnabled) { stopAutoPlay(); startAutoPlay() }
}
const prev = () => moveToIndex(currentIndex.value - 1, true)
const next = () => moveToIndex(currentIndex.value + 1, true)
const goTo = (originIdx) => {
  const base = Math.floor(currentIndex.value / originCount) * originCount
  moveToIndex(base + originIdx, true)
}
const handleCardClick = (idx) => currentIndex.value === idx ? stopAutoPlay() : moveToIndex(idx, true)
const stopAutoPlay = () => { if (autoPlayTimer) clearInterval(autoPlayTimer); autoPlayTimer = null; autoPlayEnabled = false }
const startAutoPlay = () => { if (!autoPlayEnabled) return; if (autoPlayTimer) clearInterval(autoPlayTimer); autoPlayTimer = setInterval(() => { if (!isAnimating && !isMouseInside) next() }, 4000) }
const handleMouseEnter = () => { isMouseInside = true; offsetAngle.value = 0 }
const handleMouseMove = (e) => {
  if (!isMouseInside) return
  const percent = (e.clientX - e.currentTarget.getBoundingClientRect().left) / e.currentTarget.clientWidth * 2 - 1
  offsetAngle.value = Math.min(15, Math.max(-15, percent * 15))
}
const handleMouseLeave = () => { isMouseInside = false; offsetAngle.value = 0; if (!autoPlayEnabled) { autoPlayEnabled = true; startAutoPlay() } }

// 白色发光粒子
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

// 生命周期
onMounted(() => {
  if (trackRef.value) {
    cardElements = Array.from(trackRef.value.children)
    if (cardElements.length) {
      currentIndex.value = originCount
      updateCardSizes()
      trackRef.value.style.transition = 'transform 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1)'
      trackRef.value.style.transform = `translateX(${calculateOffset(currentIndex.value)}px)`
      startAutoPlay()
    }
  }
  window.addEventListener('resize', () => !isAnimating && updateCardSizes())
  createParticles()
  let timer
  window.addEventListener('resize', () => { clearTimeout(timer); timer = setTimeout(createParticles, 300) })
})
onBeforeUnmount(() => {
  if (autoPlayTimer) clearInterval(autoPlayTimer)
  window.removeEventListener('resize', updateCardSizes)
  document.querySelectorAll('.bg-particle').forEach(p => p.remove())
})
</script>

<style>
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
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  border-radius: 48px;
  border: 1px solid rgba(255,255,255,0.12);
  position: relative;
  z-index: 2;
}
.carousel-wrapper {
  position: relative;
  margin: 20px 0 40px;
  background: linear-gradient(135deg, #0a4b8a, #2b0b3a);
  border-radius: 48px;
  padding: 40px 0;
  box-shadow: 0 20px 35px -10px rgba(0,0,0,0.5), inset 0 1px 2px rgba(255,255,255,0.2);
  overflow: hidden;
  isolation: isolate;
}
.carousel-glow, .carousel-orb {
  position: absolute;
  pointer-events: none;
}
.carousel-glow {
  top: -50%; left: -20%;
  width: 140%; height: 200%;
  background: radial-gradient(circle at 30% 50%, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 70%);
  animation: floatGlow 12s ease-in-out infinite;
}
.carousel-orb {
  bottom: -30%; right: -10%;
  width: 300px; height: 300px;
  background: radial-gradient(circle, rgba(0,255,255,0.2) 0%, rgba(0,255,255,0) 70%);
  filter: blur(40px);
  animation: orbPulse 8s ease-in-out infinite;
}
@keyframes floatGlow { 0%,100% { transform: translateX(-5%) translateY(-5%); opacity: 0.6; } 50% { transform: translateX(5%) translateY(5%); opacity: 1; } }
@keyframes orbPulse { 0%,100% { transform: scale(1); opacity: 0.3; } 50% { transform: scale(1.2); opacity: 0.6; } }
.carousel-container {
  position: relative;
  overflow: hidden;
  transition: transform 0.2s ease-out;
  z-index: 2;
}
.carousel-track {
  display: flex;
  align-items: center;
  transition: transform 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  cursor: pointer;
}
.carousel-card {
  flex-shrink: 0;
  transition: all 0.5s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  filter: brightness(0.8) saturate(0.8);
  opacity: 0.6;
  transform: scale(0.88);
}
.carousel-card.active { filter: brightness(1) saturate(1.1); opacity: 1; transform: scale(1); z-index: 10; }
.card-inner {
  position: relative;
  border-radius: 28px;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  aspect-ratio: 4 / 3;
  box-shadow: 0 20px 35px -12px rgba(0,0,0,0.3);
  transition: all 0.4s cubic-bezier(0.2,0.9,0.4,1.1);
  border: 1px solid rgba(255,255,255,0.2);
  backdrop-filter: blur(2px);
}
.carousel-card.active .card-inner { box-shadow: 0 30px 45px -12px rgba(0,0,0,0.5), 0 0 0 2px rgba(255,255,255,0.3); transform: translateY(-6px); }
.card-corner {
  position: absolute;
  top: 0; right: 0;
  width: 60px; height: 60px;
  background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0) 70%);
  clip-path: polygon(0 0, 100% 0, 100% 100%);
  opacity: 0;
  transition: opacity 0.3s;
}
.carousel-card.active .card-corner { opacity: 0.6; }
.card-overlay {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.9) 0%, rgba(0,0,0,0.5) 60%, transparent 100%);
  padding: 28px 20px 20px;
  backdrop-filter: blur(4px);
}
.carousel-card.active .card-overlay { backdrop-filter: blur(6px); background: linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.4) 70%, transparent 100%); }
.card-overlay h3 {
  margin: 0 0 8px;
  font-size: 1.6rem;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 5px rgba(0,0,0,0.5);
}
.card-overlay p {
  margin: 0;
  font-size: 0.9rem;
  color: rgba(255,255,255,0.95);
}
.carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 52px; height: 52px;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(12px);
  border-radius: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 20;
  transition: all 0.3s;
  color: white;
  font-size: 24px;
  border: 1px solid rgba(255,255,255,0.3);
}
.carousel-control:hover { background: white; color: #1e293b; transform: translateY(-50%) scale(1.1); }
.carousel-control.prev { left: 24px; }
.carousel-control.next { right: 24px; }
.dots {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(12px);
  padding: 10px 24px;
  border-radius: 60px;
  z-index: 15;
}
.dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: rgba(255,255,255,0.5);
  cursor: pointer;
  transition: all 0.3s;
}
.dot:hover { background: white; transform: scale(1.3); }
.dot.active { background: white; width: 28px; border-radius: 12px; box-shadow: 0 0 12px rgba(255,255,255,0.6); }
.news-panel {
  background: rgba(15,25,45,0.7);
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
@media (max-width: 768px) {
  .carousel-wrapper { padding: 20px 0; border-radius: 32px; }
  .carousel-control { width: 44px; height: 44px; font-size: 20px; }
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