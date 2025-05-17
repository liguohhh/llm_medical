<script setup>
import { ref, onMounted, computed } from 'vue'
import { patientApi } from '@/services/patientApi'
import { useRouter } from 'vue-router'
import AgentCard from './AgentCard.vue'

const router = useRouter()
const agents = ref([])
const loading = ref(true)
const error = ref('')
const showError = ref(false)

// 使用提示数据
const usageTips = [
  { text: '选择专科助手', icon: 'mdi-doctor', color: 'primary' },
  { text: '详述症状问题', icon: 'mdi-text-box-outline', color: 'success' },
  { text: '回答助手提问', icon: 'mdi-chat-question', color: 'info' },
  { text: '生成就诊处方', icon: 'mdi-file-document', color: 'warning' }
]

// 获取智能体颜色
const getAgentColor = (directionId) => {
  // 根据方向ID动态生成颜色
  const colors = [
    'primary', 'secondary', 'success', 'info', 'warning', 'error',
    'indigo', 'deep-purple', 'light-blue', 'teal', 'light-green', 'amber', 'deep-orange'
  ]
  return colors[directionId % colors.length]
}

// 跳转到对话页面
const goToChat = (agentId) => {
  router.push({ 
    name: 'patientChatNew',
    query: { agentId }
  })
}

// 过滤智能体，最多显示6个不同方向的智能体
const filteredAgents = computed(() => {
  if (!agents.value || agents.value.length === 0) return []
  
  const directionMap = new Map() // 用于跟踪已添加的方向
  const result = []
  
  // 遍历所有智能体
  for (const agent of agents.value) {
    // 如果这个方向还没有添加过，或者这个方向的智能体数量小于2
    if (!directionMap.has(agent.directionId) || directionMap.get(agent.directionId) < 1) {
      // 添加智能体
      result.push(agent)
      
      // 更新方向计数
      directionMap.set(agent.directionId, (directionMap.get(agent.directionId) || 0) + 1)
      
      // 如果已经达到6个，就停止添加
      if (result.length >= 6) break
    }
  }
  
  return result
})

// 获取所有智能体
const fetchAgents = async () => {
  loading.value = true
  error.value = ''
  showError.value = false
  
  try {
    const response = await patientApi.getAgents()
    if (response.code === 200 && response.data) {
      agents.value = response.data
    } else {
      error.value = response.message || '获取智能体列表失败'
      showError.value = true
    }
  } catch (err) {
    console.error('获取智能体错误:', err)
    error.value = '网络错误，请稍后重试'
    showError.value = true
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAgents()
})
</script>

<template>
  <div class="agents-list">
    <!-- 使用提示 -->
    <v-card class="usage-tips-card mb-6" variant="flat">
      <v-card-text class="pa-4">
        <div class="d-flex align-center mb-3">
          <v-avatar color="primary" variant="tonal" size="36" class="mr-3">
            <v-icon>mdi-lightbulb</v-icon>
          </v-avatar>
          <div class="text-subtitle-1 font-weight-medium">使用提示</div>
        </div>
        
        <div class="usage-tips-container">
          <div 
            v-for="(tip, index) in usageTips" 
            :key="index"
            class="usage-tip-item"
            :class="`tip-${tip.color}`"
          >
            <v-avatar :color="tip.color" size="28" class="tip-number">
              {{ index + 1 }}
            </v-avatar>
            <div class="tip-content">
              <div class="tip-icon">
                <v-icon :color="tip.color" :icon="tip.icon"></v-icon>
              </div>
              <div class="tip-text">{{ tip.text }}</div>
            </div>
          </div>
        </div>
      </v-card-text>
    </v-card>
    
    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <v-progress-circular 
        indeterminate 
        color="primary" 
        size="64"
      ></v-progress-circular>
      <div class="text-h6 mt-4">正在加载智能助手...</div>
    </div>
    
    <!-- 错误提示 -->
    <v-alert
      v-if="showError"
      type="error"
      variant="tonal"
      closable
      class="mb-6"
      @click:close="showError = false"
    >
      {{ error }}
    </v-alert>
    
    <!-- 智能体列表 -->
    <v-row v-if="!loading" class="agents-grid">
      <v-col 
        v-for="agent in filteredAgents" 
        :key="agent.id"
        cols="12" sm="6" md="4" lg="4" xl="3"
        class="agent-item"
      >
        <transition name="card-fade" appear>
          <v-card 
            class="agent-card" 
            height="200" 
            ripple 
            @click="goToChat(agent.id)"
            :elevation="2"
          >
            <div 
              class="agent-header" 
              :class="`bg-${getAgentColor(agent.directionId)}`"
            >
              <v-avatar class="agent-avatar" size="50">
                <v-icon size="28" color="white">mdi-robot</v-icon>
              </v-avatar>
            </div>
            
            <v-card-text class="text-center pb-0">
              <h3 class="font-weight-bold text-subtitle-1 mb-1">{{ agent.name }}</h3>
              <v-chip 
                size="x-small" 
                :color="getAgentColor(agent.directionId)" 
                variant="flat"
                class="mb-2"
              >
                {{ agent.directionName }}
              </v-chip>
            </v-card-text>
            
            <v-card-actions class="pt-0 pb-4">
              <v-btn 
                variant="tonal" 
                block 
                :color="getAgentColor(agent.directionId)"
                prepend-icon="mdi-message-text"
                size="small"
              >
                开始咨询
              </v-btn>
            </v-card-actions>
          </v-card>
        </transition>
      </v-col>
      
      <!-- 更多智能体提示 -->
      <v-col v-if="agents.length > filteredAgents.length && !loading" cols="12" class="text-center">
        <v-btn 
          color="primary" 
          variant="text"
          to="/patient/agents"
          prepend-icon="mdi-arrow-right-circle"
        >
          查看更多智能助手 ({{ agents.length - filteredAgents.length }}+)
        </v-btn>
      </v-col>
      
      <!-- 无数据提示 -->
      <v-col v-if="agents.length === 0 && !loading" cols="12">
        <div class="pa-4 d-flex flex-column justify-center align-center empty-state">
          <v-avatar color="grey-lighten-3" size="80" class="mb-3">
            <v-icon icon="mdi-robot-off" size="40" color="grey-lighten-1"></v-icon>
          </v-avatar>
          <div class="text-body-1 text-medium-emphasis">暂无可用的智能助手</div>
          <div class="text-caption text-medium-emphasis mt-2">请联系管理员添加智能助手</div>
        </div>
      </v-col>
    </v-row>
  </div>
</template>

<style scoped>
.agents-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.usage-tips-card {
  background: linear-gradient(to right, rgba(255,255,255,0.95), rgba(255,255,255,0.98));
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.usage-tips-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 8px;
}

.usage-tip-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 12px;
  transition: all 0.2s ease;
  flex: 1;
  min-width: 160px;
  border-left: 3px solid transparent;
}

.tip-primary { border-left-color: var(--v-theme-primary); }
.tip-success { border-left-color: var(--v-theme-success); }
.tip-info { border-left-color: var(--v-theme-info); }
.tip-warning { border-left-color: var(--v-theme-warning); }

.usage-tip-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.tip-number {
  font-size: 14px;
  font-weight: bold;
  color: white;
  margin-right: 12px;
}

.tip-content {
  display: flex;
  align-items: center;
}

.tip-icon {
  margin-right: 8px;
}

.tip-text {
  font-weight: 500;
}

.agents-grid {
  flex: 1;
  overflow-y: auto;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  flex: 1;
}

.agent-item {
  transition: all 0.3s ease;
}

.empty-state {
  opacity: 0.8;
  min-height: 300px;
}

.card-fade-enter-active, .card-fade-leave-active {
  transition: all 0.5s ease;
}

.card-fade-enter-from, .card-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.card-fade-enter-to, .card-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.agent-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.agent-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.agent-header {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.agent-header::before {
  content: "";
  position: absolute;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 70%);
  top: -40px;
  right: -40px;
}

.agent-avatar {
  border: 3px solid #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.5);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(255, 255, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0);
  }
}

/* 颜色类 */
.bg-primary { background-color: var(--v-theme-primary); }
.bg-secondary { background-color: var(--v-theme-secondary); }
.bg-success { background-color: var(--v-theme-success); }
.bg-info { background-color: var(--v-theme-info); }
.bg-warning { background-color: var(--v-theme-warning); }
.bg-error { background-color: var(--v-theme-error); }
.bg-indigo { background-color: #3f51b5; }
.bg-deep-purple { background-color: #673ab7; }
.bg-light-blue { background-color: #03a9f4; }
.bg-teal { background-color: #009688; }
.bg-light-green { background-color: #8bc34a; }
.bg-amber { background-color: #ffc107; }
.bg-deep-orange { background-color: #ff5722; }
</style> 