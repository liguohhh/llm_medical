<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { patientApi } from '@/services/patientApi'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'

const router = useRouter()
const conversations = ref([])
const loading = ref(true)
const error = ref('')
const showError = ref(false)

// 分页设置
const totalItems = ref(0)
const itemsPerPage = ref(10)
const page = ref(1)

// 过滤和排序
const searchQuery = ref('')
const sortBy = ref('updateTime')
const sortDesc = ref(true)

// 智能体颜色映射
const agentColors = [
  'primary', 'success', 'info', 'error', 'warning', 
  'purple', 'pink', 'indigo', 'cyan', 'teal', 'orange', 'deep-orange'
]

// 根据智能体ID获取颜色
const getAgentColor = (agentId) => {
  const colorIndex = agentId % agentColors.length
  return agentColors[colorIndex]
}

// 获取聊天记录
const fetchConversations = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await patientApi.getConversations()
    if (response.code === 200) {
      const allConversations = response.data || []
      totalItems.value = allConversations.length
      
      // 应用排序
      const sorted = [...allConversations].sort((a, b) => {
        const aValue = a[sortBy.value]
        const bValue = b[sortBy.value]
        
        const compareResult = sortDesc.value ? 
          (aValue > bValue ? -1 : 1) : 
          (aValue < bValue ? -1 : 1)
          
        return compareResult
      })
      
      // 应用搜索过滤
      let filtered = sorted
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = sorted.filter(conversation => 
          conversation.agentName.toLowerCase().includes(query) || 
          (conversation.lastMessage && conversation.lastMessage.toLowerCase().includes(query))
        )
        totalItems.value = filtered.length
      }
      
      // 应用分页
      const start = (page.value - 1) * itemsPerPage.value
      const end = start + itemsPerPage.value
      conversations.value = filtered.slice(start, end)
    } else {
      error.value = response.message || '获取对话列表失败'
      showError.value = true
    }
  } catch (err) {
    console.error('获取对话列表错误:', err)
    error.value = '网络错误，请稍后重试'
    showError.value = true
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  page.value = 1 // 重置到第一页
  fetchConversations()
}

// 处理排序变更
const toggleSort = (field) => {
  if (sortBy.value === field) {
    sortDesc.value = !sortDesc.value
  } else {
    sortBy.value = field
    sortDesc.value = true
  }
  fetchConversations()
}

// 处理分页变更
const onPageChange = (newPage) => {
  page.value = newPage
  fetchConversations()
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
          
          // 今天显示小时和分钟
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
    
    // 今天显示小时和分钟
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

// 计算总页数
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value))

// 组件挂载时获取数据
onMounted(() => {
  fetchConversations()
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
                <v-icon icon="mdi-message-text" size="large" color="white" class="me-3"></v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1 d-inline-block">我的聊天记录</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  查看与智能医疗助手的所有对话历史
                </p>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 搜索区域 -->
    <v-row class="mb-4">
      <v-col cols="12">
        <v-card>
          <v-card-text>
            <v-row align="center">
              <v-col cols="12" sm="6" md="4">
                <v-text-field
                  v-model="searchQuery"
                  density="compact"
                  variant="outlined"
                  label="搜索聊天记录"
                  prepend-inner-icon="mdi-magnify"
                  single-line
                  hide-details
                  clearable
                  @keyup.enter="handleSearch"
                  @click:clear="handleSearch"
                ></v-text-field>
              </v-col>
              
              <v-col cols="12" sm="6" md="4">
                <v-select
                  v-model="itemsPerPage"
                  :items="[5, 10, 20, 50]"
                  density="compact"
                  variant="outlined"
                  label="每页显示"
                  hide-details
                  @update:model-value="page = 1; fetchConversations()"
                ></v-select>
              </v-col>
              
              <v-spacer></v-spacer>
              
              <v-col cols="12" sm="6" md="4" class="d-flex justify-end">
                <v-btn 
                  color="primary" 
                  prepend-icon="mdi-refresh"
                  @click="fetchConversations"
                  :loading="loading"
                >
                  刷新
                </v-btn>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 主要内容 -->
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center py-4 px-6">
            <v-icon icon="mdi-history" class="mr-2"></v-icon>
            聊天记录
            <v-chip class="ml-2" color="primary" size="small">{{ totalItems }} 条记录</v-chip>
            <v-spacer></v-spacer>
            <div class="d-flex align-center">
              <span class="text-subtitle-2 mr-2">排序:</span>
              <v-btn-group variant="tonal" density="comfortable">
                <v-btn 
                  :color="sortBy === 'updateTime' ? 'primary' : ''" 
                  @click="toggleSort('updateTime')"
                >
                  时间
                  <v-icon v-if="sortBy === 'updateTime'" size="small" class="ml-1">
                    {{ sortDesc ? 'mdi-arrow-down' : 'mdi-arrow-up' }}
                  </v-icon>
                </v-btn>
                <v-btn 
                  :color="sortBy === 'agentName' ? 'primary' : ''" 
                  @click="toggleSort('agentName')"
                >
                  助手
                  <v-icon v-if="sortBy === 'agentName'" size="small" class="ml-1">
                    {{ sortDesc ? 'mdi-arrow-down' : 'mdi-arrow-up' }}
                  </v-icon>
                </v-btn>
              </v-btn-group>
            </div>
          </v-card-title>
          
          <v-divider></v-divider>
          
          <div class="position-relative">
            <loading-indicator :loading="loading" />
            
            <v-alert
              v-if="showError"
              type="error"
              variant="tonal"
              closable
              class="ma-4"
              @click:close="showError = false"
            >
              {{ error }}
            </v-alert>
            
            <!-- 对话列表 -->
            <v-list
              v-if="!loading && conversations.length > 0"
              class="conversation-list"
            >
              <v-list-item
                v-for="conversation in conversations"
                :key="conversation.id"
                @click="goToConversation(conversation)"
                class="conversation-item py-3"
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
                  {{ conversation.agentName }}
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
                </v-list-item-title>
                
                <v-list-item-subtitle class="text-body-2 text-truncate conversation-message">
                  {{ conversation.lastMessage || '无消息内容' }}
                </v-list-item-subtitle>
                
                <template v-slot:append>
                  <div class="d-flex flex-column align-end">
                    <div class="time-badge mb-1">
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
              
              <v-divider></v-divider>
            </v-list>
            
            <!-- 无数据提示 -->
            <div 
              v-if="!loading && conversations.length === 0"
              class="pa-12 d-flex flex-column justify-center align-center empty-state"
            >
              <v-avatar color="grey-lighten-3" size="80" class="mb-4">
                <v-icon icon="mdi-chat-remove" size="40" color="grey-lighten-1"></v-icon>
              </v-avatar>
              <div class="text-h6 text-medium-emphasis mb-2">暂无聊天记录</div>
              <div class="text-body-1 text-medium-emphasis mb-4">开始与智能医疗助手的对话，获取专业健康咨询</div>
              <v-btn 
                color="primary" 
                prepend-icon="mdi-robot" 
                :to="{ name: 'patientHome' }"
              >
                选择医疗助手
              </v-btn>
            </div>
            
            <!-- 分页控件 -->
            <div v-if="conversations.length > 0" class="d-flex justify-center pa-4">
              <v-pagination
                v-model="page"
                :length="totalPages"
                :total-visible="7"
                @update:model-value="onPageChange"
                rounded
              ></v-pagination>
            </div>
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

.conversation-item {
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.conversation-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.05);
}

.conversation-finished {
  border-left-color: var(--v-theme-success);
}

.conversation-message {
  color: rgba(0, 0, 0, 0.6);
  max-width: 80%;
}

.time-badge {
  font-size: 0.75rem;
  color: rgba(0, 0, 0, 0.6);
  background-color: rgba(0, 0, 0, 0.05);
  padding: 2px 8px;
  border-radius: 12px;
}

.empty-state {
  text-align: center;
}
</style> 