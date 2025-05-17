<script setup>
import { ref, computed, onMounted } from 'vue'
import { patientApi } from '@/services/patientApi'
import DirectionAgents from '@/components/patient/DirectionAgents.vue'
import AgentSearch from '@/components/patient/AgentSearch.vue'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'

// 数据状态
const agents = ref([])
const directions = ref([])
const loading = ref(true)
const error = ref('')
const searchQuery = ref('')
const selectedDirections = ref([])
const showIntro = ref(true)

// 按方向分组智能体，过滤掉智能体数量为0的方向
const directionGroups = computed(() => {
  // 获取所有非重复的方向
  const uniqueDirections = [...new Set(agents.value.map(agent => agent.directionId))]
    .map(dirId => {
      const direction = directions.value.find(dir => dir.id === dirId)
      if (direction) {
        // 计算该方向下的智能体数量
        const agentCount = filteredAgents.value.filter(agent => agent.directionId === dirId).length
        return { ...direction, agentCount }
      }
      return null
    })
    .filter(Boolean) // 过滤掉undefined
    .filter(dir => dir.agentCount > 0) // 过滤掉没有智能体的方向
  
  // 如果有选定的方向，则只返回选中的
  if (selectedDirections.value.length > 0) {
    return uniqueDirections.filter(dir => selectedDirections.value.includes(dir.id))
  }
  
  return uniqueDirections
})

// 搜索过滤后的智能体
const filteredAgents = computed(() => {
  if (!searchQuery.value) {
    return agents.value
  }
  
  const query = searchQuery.value.toLowerCase()
  return agents.value.filter(agent => {
    const direction = directions.value.find(dir => dir.id === agent.directionId)
    return agent.name.toLowerCase().includes(query) || 
           (direction && direction.name.toLowerCase().includes(query))
  })
})

// 获取所有智能体
const fetchAgents = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await patientApi.getAgents()
    if (response.code === 200) {
      agents.value = response.data
      
      // 提取所有方向信息
      const dirMap = new Map()
      agents.value.forEach(agent => {
        if (agent.directionId && !dirMap.has(agent.directionId)) {
          dirMap.set(agent.directionId, {
            id: agent.directionId,
            name: agent.directionName || `未知方向${agent.directionId}`,
            description: agent.directionDescription || ''
          })
        }
      })
      
      directions.value = Array.from(dirMap.values())
    } else {
      error.value = response.message || '获取智能体列表失败'
    }
  } catch (err) {
    console.error('获取智能体错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = (query) => {
  searchQuery.value = query
  if (query) {
    showIntro.value = false
  } else {
    showIntro.value = true
  }
}

// 处理方向筛选
const handleFilterDirection = (dirIds) => {
  selectedDirections.value = dirIds
  if (dirIds.length > 0) {
    showIntro.value = false
  } else if (!searchQuery.value) {
    showIntro.value = true
  }
}

// 返回顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAgents()
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
                <v-icon icon="mdi-robot" size="large" color="white" class="me-3"></v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1 d-inline-block">智能医疗助手</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  浏览和搜索专科医疗智能助手，获取专业的医疗咨询
                </p>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 搜索区域 -->
    <v-row>
      <v-col cols="12">
        <agent-search 
          @search="handleSearch" 
          @filter-direction="handleFilterDirection"
          :directions="directions"
          :showIntro="showIntro"
          class="mb-4"
        />
      </v-col>
    </v-row>
    
    <!-- 主要内容 -->
    <v-row>
      <v-col cols="12">
        <div class="position-relative">
          <loading-indicator :loading="loading" />
          
          <v-alert
            v-if="error"
            type="error"
            variant="tonal"
            class="mb-4"
          >
            {{ error }}
            <template v-slot:append>
              <v-btn 
                variant="text" 
                color="error" 
                @click="fetchAgents"
              >
                重试
              </v-btn>
            </template>
          </v-alert>
          
          <div v-if="!loading && !error">
            <!-- 搜索结果摘要 -->
            <div v-if="searchQuery" class="mb-6">
              <h2 class="text-h5 font-weight-bold mb-3">
                搜索结果: {{ filteredAgents.length }} 个智能助手
              </h2>
              
              <v-slide-group show-arrows class="search-results-slider">
                <v-slide-group-item v-for="agent in filteredAgents" :key="agent.id">
                  <v-card 
                    class="agent-card ma-2" 
                    width="220" 
                    height="200" 
                    ripple 
                    :to="{ name: 'patientChatNew', query: { agentId: agent.id } }"
                  >
                    <div class="agent-header bg-primary">
                      <v-avatar class="agent-avatar" size="50">
                        <v-icon size="28" color="white">mdi-robot</v-icon>
                      </v-avatar>
                    </div>
                    
                    <v-card-text class="text-center pb-0">
                      <h3 class="font-weight-bold text-subtitle-1 mb-1">{{ agent.name }}</h3>
                      <v-chip size="x-small" color="primary" variant="flat" class="mb-2">
                        {{ agent.directionName || '未知方向' }}
                      </v-chip>
                    </v-card-text>
                    
                    <v-card-actions class="pt-0 pb-4">
                      <v-btn 
                        variant="tonal" 
                        block 
                        color="primary"
                        prepend-icon="mdi-message-text"
                        size="small"
                      >
                        开始咨询
                      </v-btn>
                    </v-card-actions>
                  </v-card>
                </v-slide-group-item>
              </v-slide-group>
            </div>
            
            <!-- 没有结果提示 -->
            <v-alert
              v-if="(searchQuery || selectedDirections.length) && filteredAgents.length === 0"
              type="info"
              variant="tonal"
              class="mb-6"
            >
              没有找到匹配的智能助手。请尝试调整搜索条件。
            </v-alert>
            
            <!-- 按医疗方向分组展示 -->
            <template v-if="!loading && !error">
              <div v-if="directionGroups.length > 0" class="direction-grid">
                <direction-agents
                  v-for="direction in directionGroups"
                  :key="direction.id"
                  :direction="direction"
                  :agents="filteredAgents"
                />
              </div>
              
              <!-- 无医疗方向提示 -->
              <v-alert
                v-else-if="!searchQuery && !selectedDirections.length"
                type="info"
                variant="tonal"
                class="mt-6"
              >
                暂无可用的医疗方向智能助手。
              </v-alert>
            </template>
          </div>
        </div>
      </v-col>
    </v-row>
    
    <!-- 回到顶部按钮 -->
    <v-btn
      icon
      color="primary"
      class="back-to-top"
      fixed
      bottom
      right
      @click="scrollToTop"
    >
      <v-icon>mdi-arrow-up</v-icon>
    </v-btn>
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
  min-height: 100vh;
  margin: 0 auto;
}

.help-card {
  border-radius: 12px;
  height: 100%;
  transition: all 0.3s ease;
  background: linear-gradient(to bottom right, rgba(255,255,255,0.95), rgba(255,255,255,0.98));
}

.help-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.position-relative {
  position: relative;
}

.search-results-slider {
  margin-left: -8px;
  margin-right: -8px;
}

.agent-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
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
}

.direction-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.back-to-top {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 100;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.back-to-top:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

@media (max-width: 600px) {
  .back-to-top {
    bottom: 16px;
    right: 16px;
  }
}
</style> 