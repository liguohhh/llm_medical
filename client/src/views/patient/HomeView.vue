<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import AgentsList from '@/components/patient/AgentsList.vue'
import RecentConversations from '@/components/patient/RecentConversations.vue'

const userStore = useUserStore()
const user = ref(userStore.user)

// 欢迎信息
const greetingMessage = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
}

onMounted(() => {
  // 页面初始化
})
</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card class="mb-6" flat>
          <div 
            class="pa-5 rounded-t-lg" 
            style="background: linear-gradient(to right, #1976D2, #64B5F6);"
          >
            <div class="d-flex align-center">
              <div>
                <v-icon icon="mdi-home" size="large" color="white" class="me-3"></v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1 d-inline-block">病人主页</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  {{ greetingMessage() }}，{{ user?.realName || '病友' }}，欢迎使用医疗智能问答系统
                </p>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 主要内容区域 -->
    <v-row class="main-content-row">
      <!-- 智能体列表 -->
      <v-col cols="12" md="9" class="d-flex flex-column pb-md-0">
        <v-card class="content-card">
          <div class="card-color-stripe primary-stripe"></div>
          <div class="card-inner">
            <v-card-title class="d-flex align-center pb-2 pt-4 px-4">
              <v-icon class="mr-2">mdi-robot</v-icon>
              为您推荐的智能医疗助手
            </v-card-title>
            
            <v-divider></v-divider>
            
            <v-card-text class="pa-0 pt-2 flex-grow-1 d-flex flex-column">
              <p class="text-body-2 text-medium-emphasis pa-4 pb-0 mb-2">
                选择一个专科方向的智能助手，开始您的医疗咨询
              </p>
              
              <agents-list></agents-list>
            </v-card-text>
          </div>
        </v-card>
      </v-col>
      
      <!-- 侧边栏 -->
      <v-col cols="12" md="3" class="d-flex flex-column pt-md-0">
        <!-- 最近对话 -->
        <v-card class="content-card">
          <div class="card-color-stripe info-stripe"></div>
          <div class="card-inner">
            <recent-conversations></recent-conversations>
          </div>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
  margin: 0 auto;
}

.main-content-row {
  min-height: 500px;
}

.content-card {
  border-radius: 16px !important;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
  background: linear-gradient(to bottom, rgba(255,255,255,0.95), rgba(255,255,255,0.98)) !important;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.card-color-stripe {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  z-index: 1;
}

.primary-stripe {
  background-color: var(--v-theme-primary);
}

.info-stripe {
  background: linear-gradient(to right, var(--v-theme-info), var(--v-theme-primary));
}

.card-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.content-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1) !important;
}

@media (max-width: 960px) {
  .main-content-row {
    height: auto;
  }
  
  .content-card {
    margin-bottom: 16px;
  }
}
</style> 