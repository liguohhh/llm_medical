<script setup>
import { ref, watch, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { patientApi } from '@/services/patientApi'
import MessageList from '@/components/chat/MessageList.vue'
import MessageInput from '@/components/chat/MessageInput.vue'
import ChatSettings from '@/components/chat/ChatSettings.vue'

const route = useRoute()
const router = useRouter()
const conversationId = ref(null)
const loading = ref(false)
const error = ref(null)
const conversation = ref(null)
const userMessage = ref('')
const sending = ref(false)
const showSettings = ref(false)
const useStreamMode = ref(true) // 是否使用流式对话
const chatSettings = ref({
  vectorResults: 3,
  preciseResults: 3,
  searchDepth: 2,
  vectorHistoryCount: 1,
  preciseHistoryCount: 1,
  templateParams: {}
})
const agentSettings = ref(null)
const messageEndRef = ref(null)
const eventSource = ref(null)
const showInfoCard = ref(true) // 控制信息卡片的展开/折叠状态

// 滚动到最新消息
const scrollToBottom = () => {
  setTimeout(() => {
    if (messageEndRef.value) {
      messageEndRef.value.scrollIntoView({ behavior: 'smooth' })
    }
  }, 100)
}

// 加载对话内容
const loadConversation = async () => {
  if (!conversationId.value) return;
  
  loading.value = true;
  error.value = null;
  
  try {
    const response = await patientApi.getConversation(conversationId.value);
    if (response.code === 200 && response.data) {
      conversation.value = response.data;
      
      // 如果对话已结束，标记处方已检测
      if (conversation.value.isFinished === 1) {
        conversation.value.prescriptionDetected = true;
      } else {
        conversation.value.prescriptionDetected = false;
      }
      
      console.log('加载对话成功:', conversation.value);
      scrollToBottom();
      
      // 如果有agentId，加载聊天设置
      if (conversation.value.agentId) {
        loadAgentSettings(conversation.value.agentId);
      }
    } else {
      error.value = response.message || '加载对话失败';
      // 清除可能存在的临时对话
      conversation.value = null;
    }
  } catch (err) {
    console.error('加载对话错误:', err);
    error.value = '网络错误，请稍后重试';
    // 清除可能存在的临时对话
    conversation.value = null;
  } finally {
    loading.value = false;
  }
}

// 加载智能体聊天设置
const loadAgentSettings = async (agentId) => {
  try {
    const response = await patientApi.getChatSettings(agentId)
    if (response.code === 200 && response.data) {
      agentSettings.value = response.data
      
      // 初始化模板参数
      if (agentSettings.value.templateParams && 
          Array.isArray(agentSettings.value.templateParams) && 
          agentSettings.value.templateParams.length > 0) {
        const params = {}
        agentSettings.value.templateParams.forEach(param => {
          if (param && param.name && param.defaultValue) {
            params[param.name] = param.defaultValue
          }
        })
        chatSettings.value.templateParams = params
      } else {
        // 确保templateParams存在
        chatSettings.value.templateParams = {}
      }
    } else {
      console.error('获取聊天设置失败:', response.message || '未知错误')
    }
  } catch (err) {
    console.error('加载智能体设置错误:', err)
    // 确保即使出错也有默认值
    chatSettings.value.templateParams = {}
  }
}

// 创建新对话
const createNewConversation = async (agentId) => {
  loading.value = true;
  error.value = null;
  
  // 先创建一个临时对话对象，用于乐观更新UI
  const initialMessage = "您好，我想咨询一些健康问题。";
  const tempConversation = {
    id: "temp_" + Date.now(),
    agentId: agentId,
    agentName: "智能医疗助手",
    isFinished: 0,
    prescriptionDetected: false,
    messages: [
      {
        id: "user_" + Date.now(),
        content: initialMessage,
        type: 0, // 用户消息
        timestamp: new Date().toISOString()
      },
      {
        id: "system_" + Date.now(),
        content: "等待回复...",
        type: 1, // 系统消息
        timestamp: new Date().toISOString()
      }
    ]
  };
  
  // 立即显示临时对话
  conversation.value = tempConversation;
  
  // 加载智能体设置
  loadAgentSettings(agentId);
  
  try {
    const response = await patientApi.createConversation(agentId, initialMessage);
    
    if (response.code === 200 && response.data) {
      // 获取真实的对话ID
      const realConversationId = response.data.id;
      
      // 更新URL，但不触发路由变化
      window.history.replaceState(
        null,
        '',
        router.resolve({
          name: 'patientChat',
          params: { conversationId: realConversationId }
        }).href
      );
      
      // 更新conversationId
      conversationId.value = realConversationId;
      
      // 更新对话内容
      conversation.value = response.data;
      scrollToBottom();
    } else {
      error.value = response.message || '创建对话失败';
    }
  } catch (err) {
    console.error('创建对话错误:', err);
    error.value = '网络错误，请稍后重试';
  } finally {
    loading.value = false;
  }
}

// 发送消息
const sendMessage = async () => {
  if (!userMessage.value.trim() || !conversationId.value || sending.value) return
  
  sending.value = true
  const message = userMessage.value
  userMessage.value = ''
  
  try {
    // 构建请求数据
    const requestData = {
      conversationId: conversationId.value,
      message: message,
      ...chatSettings.value
    }
    
    // 向对话添加用户消息（乐观更新）
    if (!conversation.value) {
      conversation.value = {
        id: conversationId.value,
        messages: []
      }
    }
    
    if (!conversation.value.messages) {
      conversation.value.messages = []
    }
    
    const userMessageObj = {
      id: Date.now().toString(),
      content: message,
      type: 0, // 用户消息
      timestamp: new Date().toISOString()
    }
    
    conversation.value.messages.push(userMessageObj)
    scrollToBottom()
    
    // 暂时禁用流式对话，只使用标准方式
    await standardMessage(requestData)
  } catch (err) {
    console.error('发送消息错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    sending.value = false
  }
}

// 标准消息发送
const standardMessage = async (requestData) => {
  const response = await patientApi.sendMessage(requestData)
  
  if (response.code === 200 && response.data) {
    // 更新对话内容
    conversation.value = response.data
    scrollToBottom()
  } else {
    error.value = response.message || '发送消息失败'
  }
}

// 流式消息处理
const streamMessage = async (requestData) => {
  // 准备接收回复的消息对象
  const responseMessageId = Date.now().toString() + '-response'
  const responseMessage = {
    id: responseMessageId,
    content: '',
    type: 1, // 系统消息
    timestamp: new Date().toISOString()
  }
  
  // 添加到消息列表
  conversation.value.messages.push(responseMessage)
  
  // 创建或关闭现有EventSource
  if (eventSource.value) {
    try {
      if (typeof eventSource.value.close === 'function') {
        eventSource.value.close()
      } else if (eventSource.value.eventSource && typeof eventSource.value.eventSource.close === 'function') {
        eventSource.value.eventSource.close()
      }
    } catch (e) {
      console.error('关闭EventSource时出错:', e)
    }
    eventSource.value = null
  }
  
  // 处理流式事件
  const onMessage = (data) => {
    try {
      // 更新消息内容
      responseMessage.content += data
      scrollToBottom()
    } catch (e) {
      console.error('处理消息时出错:', e)
    }
  }
  
  const onError = (event) => {
    console.error('流式消息错误:', event)
    
    // 如果消息内容为空，设置错误信息
    if (!responseMessage.content) {
      responseMessage.content = '处理您的请求时出错，请稍后重试。'
    }
    
    // 关闭连接后重新加载对话
    eventSource.value = null
    sending.value = false
    
    // 延迟加载对话，给用户一些时间看到错误消息
    setTimeout(() => {
      loadConversation()
    }, 2000)
  }
  
  const onComplete = () => {
    console.log('流式消息完成')
    eventSource.value = null
    sending.value = false
    
    // 延迟加载对话，确保所有内容都已保存
    setTimeout(() => {
      loadConversation()
    }, 500)
  }
  
  try {
    // 先使用标准方式发送消息
    const response = await patientApi.sendMessage(requestData)
    
    if (response.code === 200 && response.data) {
      // 更新对话内容
      conversation.value = response.data
      scrollToBottom()
      sending.value = false
      return
    }
    
    // 如果标准方式失败，显示错误
    console.error('标准消息发送失败，错误码:', response.code)
    responseMessage.content = response.message || '发送消息失败，请稍后重试。'
    sending.value = false
  } catch (error) {
    console.error('创建流式连接失败:', error)
    onError(error)
  }
}

// 处理键盘输入
const handleKeyDown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 生成处方
const generatePrescription = async () => {
  if (!conversationId.value) return
  
  loading.value = true
  
  try {
    const response = await patientApi.generatePrescription(conversationId.value)
    if (response.code === 200 && response.data) {
      // 更新对话为已完成
      loadConversation()
      
      // 提示生成成功
      alert('处方已生成，请在处方列表中查看')
    } else {
      error.value = response.message || '生成处方失败'
    }
  } catch (err) {
    console.error('生成处方错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 切换流式/标准模式
const toggleStreamMode = () => {
  useStreamMode.value = !useStreamMode.value
}

// 监听路由参数变化
watch(
  () => [route.params.conversationId, route.query.agentId],
  ([newConversationId, newAgentId]) => {
    // 避免重复处理
    if (loading.value) return;
    
    if (newConversationId) {
      // 如果已经有conversationId，则加载对话
      conversationId.value = newConversationId;
      loadConversation();
    } else if (newAgentId && !conversation.value) {
      // 只有当没有对话时，才创建新对话
      createNewConversation(newAgentId);
    }
  },
  { immediate: true }
)

// 计算对话是否已结束
const isConversationFinished = computed(() => {
  return conversation.value && conversation.value.isFinished === 1
})

// 组件销毁时关闭EventSource
onUnmounted(() => {
  if (eventSource.value) {
    if (typeof eventSource.value.close === 'function') {
      eventSource.value.close()
    } else if (eventSource.value.eventSource && typeof eventSource.value.eventSource.close === 'function') {
      eventSource.value.eventSource.close()
    }
    eventSource.value = null
  }
})

// 处理检测到处方
const handlePrescriptionDetected = async (prescriptionData) => {
  if (!conversationId.value || isConversationFinished.value) return
  
  // 标记处方已检测
  if (conversation.value) {
    conversation.value.prescriptionDetected = true
  }
  
  // 显示处方生成中的提示
  const notificationId = 'prescription-generating'
  const notification = {
    id: notificationId,
    content: '系统已检测到处方信息，正在生成处方...',
    type: 'info',
    timestamp: new Date().toISOString()
  }
  
  // 添加系统通知
  if (conversation.value && conversation.value.messages) {
    conversation.value.messages.push(notification)
    scrollToBottom()
  }
  
  // 调用生成处方API
  try {
    loading.value = true
    
    // 使用新的API提取处方
    const response = await patientApi.extractPrescription(
      conversationId.value, 
      prescriptionData.content
    )
    
    if (response.code === 200 && response.data) {
      // 更新对话为已完成
      conversation.value.isFinished = 1
      conversation.value.prescriptionId = response.data.id
      
      // 替换通知为成功消息
      const successNotification = {
        id: notificationId,
        content: '处方已成功生成并提交，请等待医生审核。处方内容将在医生审核通过后可见。',
        type: 'success',
        timestamp: new Date().toISOString()
      }
      
      // 更新通知消息
      const notificationIndex = conversation.value.messages.findIndex(msg => msg.id === notificationId)
      if (notificationIndex !== -1) {
        conversation.value.messages[notificationIndex] = successNotification
      }
      
      // 添加对话结束提示
      conversation.value.messages.push({
        id: 'conversation-end-' + Date.now(),
        content: '对话已结束，您可以在"处方列表"中查看该处方的审核状态。',
        type: 1, // 系统消息
        timestamp: new Date().toISOString()
      })
      
      scrollToBottom()
    } else {
      error.value = response.message || '生成处方失败'
      
      // 替换通知为错误消息
      const errorNotification = {
        id: notificationId,
        content: `处方生成失败：${error.value}`,
        type: 'error',
        timestamp: new Date().toISOString()
      }
      
      // 更新通知消息
      const notificationIndex = conversation.value.messages.findIndex(msg => msg.id === notificationId)
      if (notificationIndex !== -1) {
        conversation.value.messages[notificationIndex] = errorNotification
      }
      
      // 如果处方生成失败，取消处方检测状态
      conversation.value.prescriptionDetected = false
    }
  } catch (err) {
    console.error('生成处方错误:', err)
    error.value = '网络错误，请稍后重试'
    
    // 替换通知为错误消息
    const errorNotification = {
      id: notificationId,
      content: `处方生成失败：${error.value}`,
      type: 'error',
      timestamp: new Date().toISOString()
    }
    
    // 更新通知消息
    const notificationIndex = conversation.value.messages.findIndex(msg => msg.id === notificationId)
    if (notificationIndex !== -1) {
      conversation.value.messages[notificationIndex] = errorNotification
    }
    
    // 如果处方生成失败，取消处方检测状态
    conversation.value.prescriptionDetected = false
  } finally {
    loading.value = false
  }
}

// 切换信息卡片展开状态
const toggleInfoCard = () => {
  showInfoCard.value = !showInfoCard.value
}
</script>

<template>
  <v-container fluid class="chat-container pa-0">
    <v-card class="chat-card">
      <v-app-bar density="compact" color="primary" class="chat-header">
        <v-btn
          icon="mdi-arrow-left"
          variant="text"
          class="mr-2"
          :to="{ name: 'patientHome' }"
          color="white"
        ></v-btn>
        <v-app-bar-title>
          <span class="text-white">{{ conversation ? conversation.agentName : '对话加载中...' }}</span>
          <span v-if="isConversationFinished" class="text-caption ml-2 text-white">(已结束)</span>
        </v-app-bar-title>
        <template v-slot:append>
          <!-- 暂时隐藏流式对话切换按钮 -->
          <!-- <v-btn
            v-if="conversation && !isConversationFinished"
            :icon="useStreamMode ? 'mdi-lightning-bolt' : 'mdi-lightning-bolt-outline'"
            variant="text"
            @click="toggleStreamMode"
            :title="useStreamMode ? '已开启流式对话' : '已关闭流式对话'"
            color="white"
            class="mr-2"
          ></v-btn> -->
          <v-btn
            v-if="conversation && !isConversationFinished"
            icon="mdi-tune"
            variant="text"
            @click="showSettings = !showSettings"
            color="white"
          ></v-btn>
        </template>
      </v-app-bar>
      
      <!-- 加载中 -->
      <v-card-text v-if="loading && !conversation" class="text-center py-16">
        <v-progress-circular indeterminate color="primary" size="64" class="mb-4"></v-progress-circular>
        <div class="text-h6">{{ conversationId ? '加载对话内容...' : '创建新对话...' }}</div>
      </v-card-text>
      
      <!-- 错误提示 -->
      <v-card-text v-else-if="error && !conversation" class="text-center py-16">
        <v-icon size="64" color="error" class="mb-4">mdi-alert-circle</v-icon>
        <div class="text-h6 mb-2">出错了</div>
        <div class="text-body-1 text-medium-emphasis mb-4">
          {{ error }}
        </div>
        <v-btn color="primary" :to="{ name: 'patientHome' }">
          返回首页
        </v-btn>
      </v-card-text>
      
      <!-- 对话内容 -->
      <v-card-text v-else-if="conversation" class="chat-content pa-0">
        <!-- 对话信息卡片 -->
        <div class="conversation-info pa-4">
          <v-card variant="outlined" class="info-card">
            <v-card-item>
              <v-card-title class="text-subtitle-1 font-weight-bold">
                <v-icon color="primary" class="mr-2">mdi-information</v-icon>
                对话信息
                <v-spacer></v-spacer>
                <v-btn
                  icon="mdi-chevron-down"
                  variant="text"
                  density="compact"
                  :class="{ 'rotate-icon': showInfoCard }"
                  @click="toggleInfoCard"
                ></v-btn>
              </v-card-title>
            </v-card-item>
            
            <v-expand-transition>
              <v-card-text v-show="showInfoCard" class="pt-0">
                <v-list density="compact" class="bg-grey-lighten-4 rounded">
                  <v-list-item>
                    <template v-slot:prepend>
                      <v-icon color="primary" class="mr-2">mdi-robot</v-icon>
                    </template>
                    <v-list-item-title>智能助手</v-list-item-title>
                    <v-list-item-subtitle>{{ conversation.agentName || '未知' }}</v-list-item-subtitle>
                  </v-list-item>
                  
                  <v-list-item v-if="conversation.directionName">
                    <template v-slot:prepend>
                      <v-icon color="primary" class="mr-2">mdi-medical-bag</v-icon>
                    </template>
                    <v-list-item-title>医疗方向</v-list-item-title>
                    <v-list-item-subtitle>{{ conversation.directionName }}</v-list-item-subtitle>
                  </v-list-item>
                  
                  <v-list-item>
                    <template v-slot:prepend>
                      <v-icon color="primary" class="mr-2">mdi-calendar</v-icon>
                    </template>
                    <v-list-item-title>开始时间</v-list-item-title>
                    <v-list-item-subtitle>{{ new Date(conversation.createTime).toLocaleString('zh-CN') }}</v-list-item-subtitle>
                  </v-list-item>
                  
                  <v-list-item v-if="conversation.messages">
                    <template v-slot:prepend>
                      <v-icon color="primary" class="mr-2">mdi-message-text</v-icon>
                    </template>
                    <v-list-item-title>消息数量</v-list-item-title>
                    <v-list-item-subtitle>{{ conversation.messages.length }}</v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-card-text>
            </v-expand-transition>
          </v-card>
        </div>
        
        <message-list 
          :messages="conversation.messages" 
          :agent-name="conversation.agentName"
          :sending="sending || loading"
          :user-name="'我'"
          @prescription-detected="handlePrescriptionDetected"
        />
        <div ref="messageEndRef"></div>
      </v-card-text>
      
      <!-- 空白状态 -->
      <v-card-text v-else class="text-center py-16">
        <v-icon size="64" color="primary" class="mb-4">mdi-chat-processing</v-icon>
        <div class="text-h5 mb-2">无法加载对话内容</div>
        <div class="text-body-1 text-medium-emphasis">
          请返回首页重新选择智能助手
        </div>
      </v-card-text>
      
      <!-- 聊天输入区域 -->
      <v-card-actions v-if="conversation && !isConversationFinished" class="chat-input pa-4">
        <v-row no-gutters>
          <v-col cols="12">
            <message-input
              v-model="userMessage"
              :sending="sending || loading"
              :disabled="conversation.prescriptionDetected"
              @send="sendMessage"
              @keydown="handleKeyDown"
            />
          </v-col>
        </v-row>
        <v-row v-if="conversation.prescriptionDetected" no-gutters class="mt-2">
          <v-col cols="12">
            <div class="text-caption text-center text-warning">
              <v-icon size="small" color="warning" class="mr-1">mdi-information</v-icon>
              处方已生成，对话已结束。请等待医生审核。
            </div>
          </v-col>
        </v-row>
      </v-card-actions>
      
      <!-- 对话已结束提示 -->
      <v-card-actions v-if="conversation && isConversationFinished" class="chat-input pa-4">
        <v-row no-gutters justify="center">
          <v-col cols="12" md="8" class="text-center">
            <div class="text-body-1 text-medium-emphasis mb-3">
              对话已结束，您可以查看处方或返回首页
            </div>
            <v-btn 
              v-if="conversation.prescriptionId"
              color="primary" 
              class="mr-2" 
              :to="{ name: 'patientPrescription', params: { prescriptionId: conversation.prescriptionId } }"
            >
              查看处方
            </v-btn>
            <v-btn 
              v-else
              color="primary"
              class="mr-2" 
              @click="generatePrescription"
              :disabled="loading"
              :loading="loading"
            >
              生成处方
            </v-btn>
            <v-btn color="secondary" class="mr-2" :to="{ name: 'patientHome' }">
              返回首页
            </v-btn>
            <v-btn color="info" :to="{ name: 'patientPrescriptions' }">
              处方列表
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
      
      <!-- 设置抽屉 -->
      <v-navigation-drawer
        v-model="showSettings"
        location="right"
        temporary
        width="300"
      >
        <chat-settings 
          v-model:settings="chatSettings"
          :agent-settings="agentSettings"
        />
      </v-navigation-drawer>
    </v-card>
  </v-container>
</template>

<style scoped>
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 0;
}

.chat-header {
  z-index: 10;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f5f5;
}

.chat-input {
  border-top: 1px solid rgba(0, 0, 0, 0.12);
  background-color: white;
}

.conversation-info {
  background-color: #f5f5f5;
  padding: 16px;
}

.info-card {
  border-radius: 12px;
  background-color: white;
  transition: transform 0.2s, box-shadow 0.2s;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.rotate-icon {
  transform: rotate(180deg);
  transition: transform 0.3s ease;
}
</style> 