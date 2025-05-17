<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { doctorApi } from '@/services/doctorApi'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const router = useRouter()
const conversationId = ref(route.params.conversationId)
const conversation = ref(null)
const messages = ref([])
const loading = ref(true)
const error = ref(null)
const expandedTags = ref({}) // 控制标签展开/折叠状态
const showInfoCard = ref(true) // 控制信息卡片的展开/折叠状态

// 加载对话记录
const loadConversation = async () => {
  if (!conversationId.value) return
  
  loading.value = true
  error.value = null
  
  try {
    const response = await doctorApi.getConversationDetail(conversationId.value)
    
    if (response.code === 200 && response.data) {
      conversation.value = response.data
      messages.value = response.data.messages || []
      
      // 确保消息对象格式正确
      messages.value.forEach(msg => {
        // 确保每条消息都有时间戳
        if (!msg.timestamp && msg.createTime) {
          msg.timestamp = msg.createTime
        } else if (!msg.timestamp) {
          msg.timestamp = new Date().toISOString()
        }
      })
      
      console.log('加载的消息:', messages.value)
    } else {
      error.value = response.message || '加载对话记录失败'
    }
  } catch (err) {
    console.error('加载对话记录错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 返回处方详情
const goToPrescription = () => {
  if (conversation.value && conversation.value.prescriptionId) {
    router.push({
      name: 'doctorPrescriptionDetail',
      params: { prescriptionId: conversation.value.prescriptionId }
    })
  } else {
    goToPrescriptions()
  }
}

// 返回处方列表
const goToPrescriptions = () => {
  router.push({ name: 'doctorPrescriptions' })
}

// 返回首页
const goToHome = () => {
  router.push({ name: 'doctorHome' })
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '未知时间'
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化时间（仅显示时:分）
const formatTime = (dateTimeStr) => {
  if (!dateTimeStr) return '未知时间'
  try {
    const date = new Date(dateTimeStr)
    if (isNaN(date.getTime())) return '未知时间'
    
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  } catch (e) {
    console.error('时间格式化错误:', e, dateTimeStr)
    return '未知时间'
  }
}

// 处理消息内容，提取标签
const processMessageContent = (content) => {
  if (!content) return { text: '', tags: [] }
  
  const tagRegex = /<([^>]+)>([^<]*)<\/\1>/g
  const tags = []
  let match
  
  // 复制原始内容用于提取标签后的展示
  let processedContent = content
  
  // 提取所有标签
  while ((match = tagRegex.exec(content)) !== null) {
    const tagName = match[1]
    const tagContent = match[2]
    const fullTag = match[0]
    
    // 处理处方标签，医生界面可以直接查看处方内容
    tags.push({
      name: tagName,
      content: tagContent,
      fullTag,
      isMasked: false
    })
    
    // 从显示内容中移除标签
    processedContent = processedContent.replace(fullTag, '')
  }
  
  return {
    text: processedContent.trim(),
    tags
  }
}

// 渲染Markdown内容
const renderMarkdown = (content) => {
  if (!content) return ''
  try {
    const rawHtml = marked(content)
    return DOMPurify.sanitize(rawHtml)
  } catch (e) {
    console.error('Markdown渲染失败:', e)
    return content
  }
}

// 判断是否为用户消息
const isUserMessage = (message) => {
  // 后端API中，消息类型 type=0 表示用户消息，type=1 表示系统/智能体消息
  // 同时也兼容 isFromUser 字段
  return message && (message.type === 0 || message.isFromUser === true)
}

// 获取消息发送者类型
const getSenderType = (message) => {
  if (!message) return 'unknown'
  if (isUserMessage(message)) return 'user'
  return 'agent'
}

// 获取消息发送者名称
const getSenderName = (message) => {
  if (!message) return '未知'
  if (isUserMessage(message)) return conversation.value?.userName || '患者'
  return conversation.value?.agentName || '智能助手'
}

// 获取消息发送者头像
const getSenderAvatar = (message) => {
  if (!message) return ''
  if (isUserMessage(message)) return 'mdi-account'
  return 'mdi-robot'
}

// 获取消息发送者头像颜色
const getSenderAvatarColor = (message) => {
  if (!message) return 'grey'
  if (isUserMessage(message)) return 'primary'
  return 'secondary'
}

// 切换标签展开状态
const toggleTagExpand = (messageId) => {
  expandedTags.value[messageId] = !expandedTags.value[messageId]
}

// 检查消息是否有标签
const hasMessageTags = (message) => {
  if (!message || isUserMessage(message)) return false
  const { tags } = processMessageContent(message.content)
  return tags.length > 0
}

// 切换信息卡片展开状态
const toggleInfoCard = () => {
  showInfoCard.value = !showInfoCard.value
}

// 组件挂载时加载对话记录
onMounted(() => {
  // 配置marked选项
  marked.setOptions({
    gfm: true,
    breaks: true,
    smartLists: true,
    smartypants: true
  })
  
  loadConversation()
})
</script>

<template>
  <v-container fluid class="conversation-container pa-0">
    <v-card class="conversation-card">
      <v-app-bar density="compact" color="primary" class="conversation-header">
        <v-btn
          icon="mdi-arrow-left"
          variant="text"
          class="mr-2"
          @click="goToPrescription"
          color="white"
        ></v-btn>
        <v-app-bar-title>
          <span class="text-white">对话记录</span>
          <span v-if="conversation" class="text-caption ml-2 text-white">
            (ID: {{ conversation.id }})
          </span>
        </v-app-bar-title>
      </v-app-bar>
      
      <div class="conversation-content">
        <!-- 加载中 -->
        <div v-if="loading" class="text-center py-8">
          <v-progress-circular indeterminate color="primary" size="64" class="mb-4"></v-progress-circular>
          <div class="text-h6">加载对话记录中...</div>
        </div>
        
        <!-- 错误提示 -->
        <div v-else-if="error" class="text-center py-8">
          <v-icon size="64" color="error" class="mb-4">mdi-alert-circle</v-icon>
          <div class="text-h6 mb-2">出错了</div>
          <div class="text-body-1 text-medium-emphasis mb-4">
            {{ error }}
          </div>
          <v-btn color="primary" @click="goToHome">
            返回首页
          </v-btn>
        </div>
        
        <!-- 对话内容 -->
        <template v-else-if="conversation">
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
                        <v-icon color="primary" class="mr-2">mdi-account</v-icon>
                      </template>
                      <v-list-item-title>患者姓名</v-list-item-title>
                      <v-list-item-subtitle>{{ conversation.userName || '未知' }}</v-list-item-subtitle>
                    </v-list-item>
                    
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary" class="mr-2">mdi-robot</v-icon>
                      </template>
                      <v-list-item-title>智能助手</v-list-item-title>
                      <v-list-item-subtitle>{{ conversation.agentName || '未知' }}</v-list-item-subtitle>
                    </v-list-item>
                    
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary" class="mr-2">mdi-medical-bag</v-icon>
                      </template>
                      <v-list-item-title>医疗方向</v-list-item-title>
                      <v-list-item-subtitle>{{ conversation.directionName || '未知' }}</v-list-item-subtitle>
                    </v-list-item>
                    
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary" class="mr-2">mdi-calendar</v-icon>
                      </template>
                      <v-list-item-title>开始时间</v-list-item-title>
                      <v-list-item-subtitle>{{ formatDateTime(conversation.createTime) }}</v-list-item-subtitle>
                    </v-list-item>
                    
                    <v-list-item>
                      <template v-slot:prepend>
                        <v-icon color="primary" class="mr-2">mdi-message-text</v-icon>
                      </template>
                      <v-list-item-title>消息数量</v-list-item-title>
                      <v-list-item-subtitle>{{ messages.length }}</v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                </v-card-text>
              </v-expand-transition>
            </v-card>
          </div>
          
          <!-- 消息列表 -->
          <div class="conversation-messages pa-4">
            <div class="messages-container">
              <div v-if="messages.length === 0" class="text-center py-8">
                <v-icon size="64" color="grey" class="mb-4">mdi-chat-outline</v-icon>
                <div class="text-h6 mb-2">暂无对话消息</div>
                <div class="text-body-1 text-medium-emphasis">
                  该对话暂未包含任何消息记录
                </div>
              </div>
              
              <template v-else>
                <div
                  v-for="(message, index) in messages"
                  :key="message.id || index"
                  class="message-item"
                  :class="{
                    'message-user': isUserMessage(message),
                    'message-agent': !isUserMessage(message)
                  }"
                >
                  <div class="message-header">
                    <div class="sender-info">
                      <v-avatar :color="getSenderAvatarColor(message)" size="32" class="mr-2">
                        <v-icon :icon="getSenderAvatar(message)" color="white"></v-icon>
                      </v-avatar>
                      <span class="sender-name">{{ getSenderName(message) }}</span>
                    </div>
                    <div class="message-time">
                      {{ formatTime(message.timestamp) }}
                    </div>
                  </div>
                  
                  <div class="message-content">
                    <div v-if="!isUserMessage(message)" class="markdown-body" v-html="renderMarkdown(processMessageContent(message.content).text)"></div>
                    <div v-else class="message-body">
                      <!-- 用户消息不需要Markdown渲染 -->
                      <p v-for="(line, i) in message.content.split('\n')" :key="i">
                        {{ line || '&nbsp;' }}
                      </p>
                    </div>
                    
                    <!-- 标签折叠区域 -->
                    <div v-if="hasMessageTags(message)" class="message-tags-container">
                      <div 
                        class="message-tags-toggle"
                        @click="toggleTagExpand(message.id || index)"
                      >
                        <v-icon size="small" class="mr-1">
                          {{ expandedTags[message.id || index] ? 'mdi-chevron-up' : 'mdi-chevron-down' }}
                        </v-icon>
                        <span>{{ expandedTags[message.id || index] ? '收起标签信息' : '查看标签信息' }}</span>
                        <span class="tag-count ml-1">({{ processMessageContent(message.content).tags.length }})</span>
                      </div>
                      
                      <v-expand-transition>
                        <div v-if="expandedTags[message.id || index]" class="message-tags-list">
                          <div 
                            v-for="(tag, tagIndex) in processMessageContent(message.content).tags" 
                            :key="tagIndex"
                            class="message-tag"
                            :class="{ 
                              'prescription-tag': tag.name === '处方',
                              'masked-tag': tag.isMasked
                            }"
                          >
                            <div class="message-tag-header">
                              <v-chip
                                size="small"
                                :class="'tag-chip-' + tag.name"
                                text-color="white"
                                class="font-weight-medium"
                              >
                                {{ tag.name }}
                              </v-chip>
                            </div>
                            <div class="message-tag-content" :class="{ 'masked-content': tag.isMasked }">
                              {{ tag.content }}
                            </div>
                          </div>
                        </div>
                      </v-expand-transition>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
          
          <!-- 底部操作按钮 -->
          <div class="conversation-actions pa-4">
            <v-btn
              v-if="conversation.prescriptionId"
              color="secondary"
              variant="outlined"
              class="mr-2"
              @click="goToPrescription"
              prepend-icon="mdi-file-document"
            >
              查看处方
            </v-btn>
            
            <v-spacer></v-spacer>
            
            <v-btn color="primary" variant="outlined" class="mr-2" @click="goToPrescriptions" prepend-icon="mdi-format-list-bulleted">
              返回列表
            </v-btn>
            <v-btn color="primary" @click="goToHome" prepend-icon="mdi-home">
              返回首页
            </v-btn>
          </div>
        </template>
        
        <!-- 空白状态 -->
        <div v-else class="text-center py-8">
          <v-icon size="64" color="primary" class="mb-4">mdi-chat-outline</v-icon>
          <div class="text-h6 mb-2">未找到对话记录</div>
          <div class="text-body-1 text-medium-emphasis mb-4">
            该对话记录可能不存在或已被删除
          </div>
          <v-btn color="primary" @click="goToHome">
            返回首页
          </v-btn>
        </div>
      </div>
    </v-card>
  </v-container>
</template>

<style scoped>
.conversation-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.conversation-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 0;
}

.conversation-header {
  z-index: 10;
}

.conversation-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.conversation-messages {
  flex: 1;
  overflow-y: auto;
  background-color: #f5f5f5;
  padding: 16px;
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  flex-direction: column;
  max-width: 85%;
  border-radius: 12px;
  overflow: hidden;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-user {
  align-self: flex-end;
  background-color: #e3f2fd;
}

.message-agent {
  align-self: flex-start;
  background-color: white;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background-color: rgba(0, 0, 0, 0.03);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.sender-info {
  display: flex;
  align-items: center;
}

.sender-name {
  font-weight: 500;
}

.message-time {
  font-size: 0.75rem;
  color: rgba(0, 0, 0, 0.6);
}

.message-content {
  padding: 16px;
}

.message-body {
  white-space: pre-wrap;
  word-break: break-word;
}

.message-body p {
  margin: 0;
  line-height: 1.5;
}

.message-body p:not(:last-child) {
  margin-bottom: 8px;
}

/* 标签样式 */
.message-tags-container {
  margin-top: 8px;
  border-top: 1px dashed rgba(0, 0, 0, 0.12);
  padding-top: 6px;
}

.message-tags-toggle {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #1976d2;
  font-size: 0.85rem;
  user-select: none;
}

.message-tags-toggle:hover {
  text-decoration: underline;
}

.tag-count {
  color: rgba(0, 0, 0, 0.6);
}

.message-tags-list {
  margin-top: 8px;
}

.message-tag {
  background-color: rgba(25, 118, 210, 0.05);
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 8px;
}

/* 特殊处理处方标签 */
.message-tag.prescription-tag {
  background-color: rgba(76, 175, 80, 0.08);
  border-left: 3px solid #4caf50;
}

/* 打码标签样式 */
.message-tag.masked-tag {
  background-color: rgba(255, 152, 0, 0.08);
  border-left: 3px solid #ff9800;
}

.masked-content {
  color: #ff9800;
  font-style: italic;
  text-align: center;
}

.message-tag:last-child {
  margin-bottom: 0;
}

.message-tag-header {
  margin-bottom: 4px;
}

/* 为不同类型的标签设置不同颜色 */
.tag-chip-病症 {
  background-color: #ff5722 !important;
}

.tag-chip-处方 {
  background-color: #4caf50 !important;
}

.tag-chip-诊断 {
  background-color: #2196f3 !important;
}

.tag-chip-用药 {
  background-color: #9c27b0 !important;
}

.tag-chip-建议 {
  background-color: #ff9800 !important;
}

.message-tag-content {
  font-size: 0.9rem;
  color: rgba(0, 0, 0, 0.87);
  white-space: pre-wrap;
  word-break: break-word;
}

/* Markdown样式 */
.markdown-body {
  white-space: normal;
}

.markdown-body :deep(p) {
  margin-bottom: 1em;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(ul), .markdown-body :deep(ol) {
  padding-left: 1.5em;
  margin-bottom: 1em;
}

.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3),
.markdown-body :deep(h4), .markdown-body :deep(h5), .markdown-body :deep(h6) {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: 600;
}

.markdown-body :deep(code) {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 1em;
  border-radius: 4px;
  overflow-x: auto;
  margin-bottom: 1em;
}

.markdown-body :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid rgba(0, 0, 0, 0.1);
  padding-left: 1em;
  margin-left: 0;
  margin-right: 0;
  color: rgba(0, 0, 0, 0.7);
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 1em;
}

.markdown-body :deep(th), .markdown-body :deep(td) {
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0.5em;
  text-align: left;
}

.markdown-body :deep(th) {
  background-color: rgba(0, 0, 0, 0.05);
}

.markdown-body :deep(a) {
  color: #1976d2;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(img) {
  max-width: 100%;
}

.conversation-actions {
  background-color: #f5f5f5;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.rotate-icon {
  transform: rotate(180deg);
  transition: transform 0.3s ease;
}
</style> 