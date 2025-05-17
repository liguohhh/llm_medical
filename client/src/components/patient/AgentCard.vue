<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { patientApi } from '@/services/patientApi'

const props = defineProps({
  agent: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const loading = ref(false)

// 为每个智能体生成一个唯一的颜色
const agentColors = [
  'primary', 'success', 'info', 'error', 'warning', 
  'purple', 'pink', 'indigo', 'cyan', 'teal', 'orange', 'deep-orange'
]

// 根据智能体ID生成一个稳定的颜色（同一个智能体总是相同颜色）
const agentColor = computed(() => {
  const colorIndex = props.agent.id % agentColors.length
  return agentColors[colorIndex]
})

// 点击开始聊天
const startChat = async () => {
  loading.value = true
  try {
    const response = await patientApi.createConversation(
      props.agent.id, 
      `您好，我想咨询一些关于${props.agent.directionName}的问题。`
    )
    if (response.code === 200 && response.data) {
      // 导航到聊天界面
      router.push({
        name: 'PatientChat',
        params: { conversationId: response.data.id }
      })
    }
  } catch (error) {
    console.error('开始聊天失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-card 
    class="agent-card" 
    :elevation="2" 
    hover
  >
    <div class="card-color-stripe" :class="`bg-${agentColor}`"></div>
    <v-card-item>
      <template v-slot:prepend>
        <v-avatar :color="agentColor" variant="tonal" size="large" class="mr-3">
          <v-icon size="24" :color="agentColor">mdi-robot</v-icon>
        </v-avatar>
      </template>
      
      <v-card-title class="text-h6 agent-title">{{ agent.name }}</v-card-title>
      <v-card-subtitle>{{ agent.directionName }}</v-card-subtitle>
    </v-card-item>
    
    <v-card-text class="card-content">
      <div class="agent-description">{{ agent.directionDescription }}</div>
    </v-card-text>
    
    <v-spacer></v-spacer>
    
    <v-card-actions class="pa-4">
      <v-btn 
        :color="agentColor" 
        :loading="loading"
        variant="tonal"
        prepend-icon="mdi-message-text"
        block
        @click="startChat"
      >
        开始咨询
      </v-btn>
    </v-card-actions>
    
    <v-overlay 
      :model-value="loading" 
      contained 
      class="align-center justify-center"
    >
      <v-progress-circular indeterminate :color="agentColor"></v-progress-circular>
    </v-overlay>
  </v-card>
</template>

<style scoped>
.agent-card {
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 12px;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.card-color-stripe {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.agent-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.agent-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.agent-description {
  height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  color: rgba(0, 0, 0, 0.6);
}

/* 颜色类 */
.bg-primary { background-color: var(--v-theme-primary); }
.bg-success { background-color: var(--v-theme-success); }
.bg-info { background-color: var(--v-theme-info); }
.bg-error { background-color: var(--v-theme-error); }
.bg-warning { background-color: var(--v-theme-warning); }
.bg-purple { background-color: #9c27b0; }
.bg-pink { background-color: #e91e63; }
.bg-indigo { background-color: #3f51b5; }
.bg-cyan { background-color: #00bcd4; }
.bg-teal { background-color: #009688; }
.bg-orange { background-color: #ff9800; }
.bg-deep-orange { background-color: #ff5722; }
</style> 