<script setup>
import { defineProps, computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  direction: {
    type: Object,
    required: true
  },
  agents: {
    type: Array,
    required: true
  }
})

const router = useRouter()

// 获取该医疗方向下的智能体
const directionAgents = computed(() => {
  return props.agents.filter(agent => agent.directionId === props.direction.id)
})

// 方向背景色
const directionColor = computed(() => {
  // 根据方向ID动态生成颜色
  const colors = [
    'primary', 'secondary', 'success', 'info', 'warning', 'error',
    'indigo', 'deep-purple', 'light-blue', 'teal', 'light-green', 'amber', 'deep-orange'
  ]
  return colors[props.direction.id % colors.length]
})

// 方向图标
const directionIcon = computed(() => {
  // 根据方向ID动态选择图标
  const icons = [
    'mdi-heart-pulse', 'mdi-brain', 'mdi-bone', 'mdi-stomach', 
    'mdi-eye', 'mdi-ear-hearing', 'mdi-tooth', 'mdi-needle', 
    'mdi-baby-face', 'mdi-human-pregnant', 'mdi-face-man', 'mdi-hospital'
  ]
  return icons[props.direction.id % icons.length]
})

// 跳转到对话页面
const goToChat = (agentId) => {
  router.push({ 
    name: 'patientChatNew',
    query: { agentId }
  })
}

// 是否使用网格布局
const useGridLayout = computed(() => {
  return directionAgents.value.length <= 3
})
</script>

<template>
  <div class="direction-agents">
    <!-- 方向标题 -->
    <div class="d-flex align-center mb-3 direction-header">
      <v-avatar 
        :color="directionColor" 
        size="36" 
        class="mr-3 direction-icon elevation-1"
        v-ripple
      >
        <v-icon :icon="directionIcon" color="white"></v-icon>
      </v-avatar>
      <h2 class="text-h5 font-weight-bold">{{ direction.name }}</h2>
      <v-chip 
        size="small" 
        class="ml-3" 
        :color="directionColor" 
        variant="tonal"
      >
        {{ directionAgents.length }} 位助手
      </v-chip>
    </div>
    
    <!-- 方向描述 -->
    <p v-if="direction.description" class="text-body-2 text-medium-emphasis mb-3 pl-2">
      {{ direction.description }}
    </p>
    
    <!-- 智能体列表 - 网格布局（智能体少时使用） -->
    <v-row v-if="useGridLayout">
      <v-col 
        v-for="agent in directionAgents" 
        :key="agent.id"
        cols="12" sm="6" md="4" lg="4" xl="3"
      >
        <v-card 
          class="agent-card" 
          height="200" 
          ripple 
          @click="goToChat(agent.id)"
          :elevation="2"
        >
          <div 
            class="agent-header" 
            :class="`bg-${directionColor}`"
          >
            <v-avatar class="agent-avatar" size="50">
              <v-icon size="28" color="white">mdi-robot</v-icon>
            </v-avatar>
          </div>
          
          <v-card-text class="text-center pb-0">
            <h3 class="font-weight-bold text-subtitle-1 mb-1">{{ agent.name }}</h3>
            <v-chip 
              size="x-small" 
              :color="directionColor" 
              variant="flat"
              class="mb-2"
            >
              {{ direction.name }}
            </v-chip>
          </v-card-text>
          
          <v-card-actions class="pt-0 pb-4">
            <v-btn 
              variant="tonal" 
              block 
              :color="directionColor"
              prepend-icon="mdi-message-text"
              size="small"
            >
              开始咨询
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 智能体列表 - 滑动布局（智能体多时使用） -->
    <v-slide-group 
      v-else
      show-arrows 
      class="agents-slider"
    >
      <v-slide-group-item v-for="agent in directionAgents" :key="agent.id">
        <v-card 
          class="agent-card ma-2" 
          width="220" 
          height="200" 
          ripple 
          @click="goToChat(agent.id)"
        >
          <div 
            class="agent-header" 
            :class="`bg-${directionColor}`"
          >
            <v-avatar class="agent-avatar" size="50">
              <v-icon size="28" color="white">mdi-robot</v-icon>
            </v-avatar>
          </div>
          
          <v-card-text class="text-center pb-0">
            <h3 class="font-weight-bold text-subtitle-1 mb-1">{{ agent.name }}</h3>
            <v-chip 
              size="x-small" 
              :color="directionColor" 
              variant="flat"
              class="mb-2"
            >
              {{ direction.name }}
            </v-chip>
          </v-card-text>
          
          <v-card-actions class="pt-0 pb-4">
            <v-btn 
              variant="tonal" 
              block 
              :color="directionColor"
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
</template>

<style scoped>
.direction-agents {
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.direction-agents:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background-color: rgba(255, 255, 255, 0.9);
}

.direction-header {
  position: relative;
}

.direction-header::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 60px;
  height: 3px;
  background-color: var(--v-theme-primary);
  opacity: 0.7;
  border-radius: 3px;
}

.direction-icon {
  transition: transform 0.3s ease;
}

.direction-icon:hover {
  transform: rotate(15deg);
}

.agents-slider {
  margin-left: -8px;
  margin-right: -8px;
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
</style> 