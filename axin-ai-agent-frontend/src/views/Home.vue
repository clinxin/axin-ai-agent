<template>
  <div class="home">
    <div class="container">
      <div class="hero-section">
        <h1 class="title">AI智能助手</h1>
        <p class="subtitle">选择您需要的AI应用，开启智能对话之旅</p>
      </div>
      
      <div class="apps-grid">
        <div class="app-card" @click="goToPlanApp">
          <div class="app-icon">📋</div>
          <h3 class="app-title">AI计划大师</h3>
          <p class="app-description">专业的计划制定助手，帮您规划工作和生活</p>
          <button class="btn btn-primary app-btn">开始聊天</button>
        </div>
        
        <div class="app-card" @click="goToManusApp">
          <div class="app-icon">🤖</div>
          <h3 class="app-title">智多星AI智能体</h3>
          <p class="app-description">全能AI助手，解答各种问题，提供智能建议</p>
          <button class="btn btn-primary app-btn">开始聊天</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    
    const goToPlanApp = () => {
      router.push('/plan-app')
    }
    
    const goToManusApp = () => {
      router.push('/manus-app')
    }
    
    return {
      goToPlanApp,
      goToManusApp
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}

.hero-section {
  text-align: center;
  margin-bottom: 60px;
}

.title {
  font-size: 3.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.9);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
  max-width: 800px;
  margin: 0 auto;
}

.app-card {
  background: white;
  border-radius: 16px;
  padding: 40px 30px;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
  cursor: pointer;
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
}

.app-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.app-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.app-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 25px;
  font-size: 1rem;
}

.app-btn {
  width: 100%;
  padding: 14px 24px;
  font-size: 1.1rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .title {
    font-size: 2.5rem;
  }
  
  .subtitle {
    font-size: 1.1rem;
  }
  
  .apps-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .app-card {
    padding: 30px 20px;
  }
}
</style>
