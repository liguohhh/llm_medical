<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { patientApi } from '@/services/patientApi'

const router = useRouter()
const conversations = ref([])
const loading = ref(true)
const error = ref('')
const showError = ref(false)

// 智能体颜色映射（与AgentCard组件保持一致）
const agentColors = [
  'primary', 'success', 'info', 'error', 'warning', 
  'purple', 'pink', 'indigo', 'cyan', 'teal', 'orange', 'deep-orange'
]

// 根据智能体ID获取颜色
const getAgentColor = (agentId) => {
  const colorIndex = agentId % agentColors.length
  return agentColors[colorIndex]
}

// 获取最近对话
const fetchRecentConversations = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await patientApi.getConversations()
    if (response.code === 200) {
      conversations.value = (response.data || []).slice(0, 15) // 限制最多显示15条
    } else {
      error.value = response.message || '获取对话列表失败'
    }
  } catch (err) {
    console.error('获取对话列表错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 进入对话界面
const goToConversation = (conversation) => {
  router.push({
    name: 'patientChat',
    params: { conversationId: conversation.id }
  })
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  
  try {
    // 处理后端返回的ISO格式日期字符串
    const date = new Date(timeStr)
    if (isNaN(date.getTime())) {
      // 如果日期无效，尝试解析其他格式
      const parts = timeStr.split('T')
      if (parts.length === 2) {
        const datePart = parts[0].replace(/-/g, '/')
        const timePart = parts[1].split('.')[0]
        const dateTimeStr = `${datePart} ${timePart}`
        const parsedDate = new Date(dateTimeStr)
        if (!isNaN(parsedDate.getTime())) {
          // 成功解析
          const now = new Date()
          const diff = now - parsedDate
          
          // 一天内显示小时和分钟
          if (diff < 24 * 60 * 60 * 1000) {
            return `今天 ${parsedDate.getHours().toString().padStart(2, '0')}:${parsedDate.getMinutes().toString().padStart(2, '0')}`
          }
          
          // 一周内显示星期几
          if (diff < 7 * 24 * 60 * 60 * 1000) {
            const weekdays = ['日', '一', '二', '三', '四', '五', '六']
            return `星期${weekdays[parsedDate.getDay()]}`
          }
          
          // 其他情况显示年月日
          return `${parsedDate.getFullYear()}-${(parsedDate.getMonth() + 1).toString().padStart(2, '0')}-${parsedDate.getDate().toString().padStart(2, '0')}`
        }
      }
      return '未知时间'
    }
    
    const now = new Date()
    const diff = now - date
    
    // 一天内显示小时和分钟
    if (diff < 24 * 60 * 60 * 1000) {
      return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
    
    // 一周内显示星期几
    if (diff < 7 * 24 * 60 * 60 * 1000) {
      const weekdays = ['日', '一', '二', '三', '四', '五', '六']
      return `星期${weekdays[date.getDay()]}`
    }
    
    // 其他情况显示年月日
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  } catch (e) {
    console.error('日期格式化错误:', e, timeStr)
    return '未知时间'
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchRecentConversations()
})
</script>

<template>
  <div class="conversations-wrapper">
    <v-card-title class="d-flex align-center pb-2 pt-4 px-4">
      <v-icon class="mr-2" color="primary">mdi-history</v-icon>
      最近对话
    </v-card-title>
    
    <v-divider></v-divider>
    
    <v-card-text class="pa-0 conversation-content">
      <!-- 加载中状态 -->
      <div v-if="loading" class="pa-4 d-flex justify-center align-center h-100">
        <v-progress-circular indeterminate color="primary"></v-progress-circular>
      </div>
      
      <!-- 错误提示 -->
      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        density="compact"
        class="ma-2"
        closable
        @click:close="error = ''"
      >
        {{ error }}
      </v-alert>
      
      <!-- 对话列表 -->
      <v-list 
        v-if="!loading && conversations.length > 0" 
        lines="two"
        class="conversation-list"
        density="compact"
      >
        <v-list-item
          v-for="conversation in conversations"
          :key="conversation.id"
          @click="goToConversation(conversation)"
          class="conversation-item py-2"
          :class="{ 'conversation-finished': conversation.isFinished === 1 }"
          :ripple="true"
        >
          <template v-slot:prepend>
            <v-avatar 
              :color="getAgentColor(conversation.agentId)" 
              class="mr-3"
            >
              <v-icon color="white">mdi-robot</v-icon>
            </v-avatar>
          </template>
          
          <v-list-item-title class="font-weight-medium mb-1 d-flex align-center">
            <div class="d-flex align-center">
              <span class="conversation-title">{{ conversation.agentName }}</span>
              <v-chip
                v-if="conversation.isFinished === 1"
                size="x-small"
                color="success"
                variant="tonal"
                class="ml-2"
              >
                已完成
              </v-chip>
              <v-chip
                v-else
                size="x-small"
                color="info"
                variant="tonal"
                class="ml-2"
              >
                进行中
              </v-chip>
              <v-chip
                v-if="conversation.prescriptionId"
                size="x-small"
                color="warning"
                variant="tonal"
                class="ml-2"
              >
                处方已生成
              </v-chip>
            </div>
          </v-list-item-title>
          
          <v-list-item-subtitle class="text-body-2 text-truncate conversation-message">
            {{ conversation.lastMessage || '无消息内容' }}
          </v-list-item-subtitle>
          
          <template v-slot:append>
            <div class="d-flex flex-column align-end">
              <div class="text-caption text-medium-emphasis time-badge mb-1">
                {{ formatTime(conversation.updateTime) }}
              </div>
              <v-chip
                size="x-small"
                :color="getAgentColor(conversation.agentId)"
                variant="flat"
              >
                {{ conversation.directionName || '未知科室' }}
              </v-chip>
            </div>
          </template>
        </v-list-item>
        
        <!-- 查看更多按钮 -->
        <v-list-item
          class="view-more-item"
          :to="{ name: 'patientConversations' }"
          prepend-icon="mdi-history"
        >
          <v-list-item-title class="text-center text-primary">
            查看全部聊天记录
          </v-list-item-title>
        </v-list-item>
      </v-list>
      
      <!-- 无数据提示 -->
      <div 
        v-if="!loading && conversations.length === 0"
        class="pa-4 d-flex flex-column justify-center align-center h-100 empty-state"
      >
        <v-avatar color="grey-lighten-3" size="80" class="mb-3">
          <v-icon icon="mdi-chat-remove" size="40" color="grey-lighten-1"></v-icon>
        </v-avatar>
        <div class="text-body-1 text-medium-emphasis">暂无历史对话</div>
        <div class="text-caption text-medium-emphasis mt-2">选择左侧智能助手开始咨询</div>
      </div>
    </v-card-text>
  </div>
</template>

<style scoped>
.conversations-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.conversation-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.conversation-list {
  overflow-y: auto;
  flex: 1;
}

.conversation-item {
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
  margin-bottom: 2px;
}

.conversation-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.05);
  transform: translateX(2px);
}

.conversation-finished {
  border-left-color: var(--v-theme-success);
}

.conversation-title {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-message {
  max-width: 100%;
  color: rgba(0, 0, 0, 0.6);
}

.time-badge {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
}

.empty-state {
  opacity: 0.8;
}

.view-more-item {
  background-color: rgba(0, 0, 0, 0.02);
  margin-top: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.view-more-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.08);
  transform: translateY(-2px);
}

.h-100 {
  height: 100%;
}
</style> 