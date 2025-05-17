<script setup>
import { ref, defineProps, onMounted, defineEmits, watch } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  agentName: {
    type: String,
    default: '智能医疗助手'
  },
  sending: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['prescription-detected'])

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  
  return `${hours}:${minutes}`
}

// 判断消息类型
const isUserMessage = (message) => {
  return message.type === 0
}

// 判断是否为系统通知
const isSystemNotification = (message) => {
  return message.type === 'info' || message.type === 'success' || message.type === 'error'
}

// 获取通知图标
const getNotificationIcon = (message) => {
  switch (message.type) {
    case 'info':
      return 'mdi-information'
    case 'success':
      return 'mdi-check-circle'
    case 'error':
      return 'mdi-alert-circle'
    default:
      return 'mdi-bell'
  }
}

// 获取通知颜色
const getNotificationColor = (message) => {
  switch (message.type) {
    case 'info':
      return 'info'
    case 'success':
      return 'success'
    case 'error':
      return 'error'
    default:
      return 'primary'
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
    
    // 处理处方标签，将内容替换为打码符号
    if (tagName === '处方') {
      const maskedContent = '***** 处方内容需经医生审核后可见 *****'
      tags.push({
        name: tagName,
        content: maskedContent,
        fullTag,
        isMasked: true
      })
    } else {
      tags.push({
        name: tagName,
        content: tagContent,
        fullTag,
        isMasked: false
      })
    }
    
    // 从显示内容中移除标签
    processedContent = processedContent.replace(fullTag, '')
  }
  
  return {
    text: processedContent.trim(),
    tags
  }
}

// 检查是否包含处方标签
const checkForPrescription = (message) => {
  if (!message || isUserMessage(message)) return false
  
  const prescriptionRegex = /<处方>([\s\S]*?)<\/处方>/
  return prescriptionRegex.test(message.content)
}

// 监听消息变化，检查处方
watch(() => props.messages, (newMessages) => {
  if (!newMessages || newMessages.length === 0) return
  
  // 检查最新的系统消息是否包含处方标签
  const latestSystemMessages = [...newMessages]
    .reverse()
    .filter(msg => !isUserMessage(msg))
  
  if (latestSystemMessages.length > 0) {
    const latestMessage = latestSystemMessages[0]
    if (checkForPrescription(latestMessage)) {
      // 提取处方内容
      const match = latestMessage.content.match(/<处方>([\s\S]*?)<\/处方>/)
      const prescriptionContent = match ? match[1] : ''
      
      // 发送事件通知父组件
      emit('prescription-detected', {
        messageId: latestMessage.id,
        content: prescriptionContent
      })
    }
  }
}, { deep: true })

// 渲染Markdown内容
const renderMarkdown = (content) => {
  try {
    return DOMPurify.sanitize(marked(content))
  } catch (e) {
    console.error('Markdown渲染失败:', e)
    return content
  }
}

// 初始化DOMPurify和marked配置
onMounted(() => {
  // 配置marked选项
  marked.setOptions({
    gfm: true,
    breaks: true,
    smartLists: true,
    smartypants: true
  })
})

// 控制标签展开/折叠状态
const expandedTags = ref({})

// 切换标签展开状态
const toggleTagExpand = (messageId) => {
  expandedTags.value[messageId] = !expandedTags.value[messageId]
}

// 检查消息是否有标签
const hasMessageTags = (message) => {
  if (isUserMessage(message)) return false
  const { tags } = processMessageContent(message.content)
  return tags.length > 0
}
</script>

<template>
  <div class="message-list pa-4">
    <!-- 对话开始提示 -->
    <div class="text-center my-4">
      <div class="text-caption text-medium-emphasis">
        对话开始，您正在与 {{ agentName }} 沟通
      </div>
    </div>
    
    <!-- 消息列表 -->
    <div 
      v-for="(message, index) in messages" 
      :key="message.id"
      class="message-container"
      :class="{ 
        'user-message': isUserMessage(message), 
        'agent-message': !isUserMessage(message) && !isSystemNotification(message),
        'system-notification': isSystemNotification(message)
      }"
    >
      <div class="message-content" :class="{ 'notification-content': isSystemNotification(message) }">
        <div class="message-header" v-if="!isSystemNotification(message)">
          <div class="message-sender font-weight-medium">
            {{ isUserMessage(message) ? '我' : agentName }}
          </div>
          <div class="message-time text-caption text-medium-emphasis">
            {{ formatTime(message.timestamp) }}
          </div>
        </div>
        <div 
          v-if="!isUserMessage(message) && !isSystemNotification(message)" 
          class="message-body markdown-body" 
          v-html="renderMarkdown(processMessageContent(message.content).text)"
        ></div>
        <div v-else-if="isUserMessage(message)" class="message-body">
          <!-- 用户消息不需要Markdown渲染 -->
          <p v-for="(line, i) in message.content.split('\n')" :key="i">
            {{ line || '&nbsp;' }}
          </p>
        </div>
        <div v-else class="notification-body">
          <!-- 系统通知 -->
          <v-icon 
            :color="getNotificationColor(message)" 
            size="small" 
            class="mr-2"
          >
            {{ getNotificationIcon(message) }}
          </v-icon>
          <span>{{ message.content }}</span>
        </div>
        
        <!-- 标签折叠区域 -->
        <div v-if="hasMessageTags(message)" class="message-tags-container">
          <div 
            class="message-tags-toggle"
            @click="toggleTagExpand(message.id)"
          >
            <v-icon size="small" class="mr-1">
              {{ expandedTags[message.id] ? 'mdi-chevron-up' : 'mdi-chevron-down' }}
            </v-icon>
            <span>{{ expandedTags[message.id] ? '收起标签信息' : '查看标签信息' }}</span>
            <span class="tag-count ml-1">({{ processMessageContent(message.content).tags.length }})</span>
          </div>
          
          <v-expand-transition>
            <div v-if="expandedTags[message.id]" class="message-tags-list">
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
                  <v-chip
                    v-if="tag.isMasked"
                    size="x-small"
                    color="warning"
                    text-color="white"
                    class="ml-2"
                  >
                    待审核
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
    
    <!-- 发送中状态 -->
    <div v-if="sending" class="message-container agent-message">
      <div class="message-content">
        <div class="message-header">
          <div class="message-sender font-weight-medium">
            {{ agentName }}
          </div>
          <div class="message-time text-caption text-medium-emphasis">
            等待回复...
          </div>
        </div>
        <div class="message-body">
          <v-progress-linear indeterminate color="primary" class="mt-2"></v-progress-linear>
        </div>
      </div>
    </div>
    
    <!-- 没有消息的提示 -->
    <div v-if="messages.length === 0 && !sending" class="text-center my-8">
      <v-icon size="64" color="grey lighten-1" class="mb-4">mdi-message-outline</v-icon>
      <div class="text-body-1 text-medium-emphasis">
        暂无消息，开始发送第一条消息吧
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-list {
  display: flex;
  flex-direction: column;
  max-width: 100%;
}

.message-container {
  display: flex;
  margin-bottom: 16px;
  max-width: 80%;
}

.user-message {
  align-self: flex-end;
}

.agent-message {
  align-self: flex-start;
}

.system-notification {
  align-self: center;
  max-width: 90%;
}

.message-content {
  border-radius: 12px;
  padding: 10px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.notification-content {
  background-color: rgba(0, 0, 0, 0.03);
  border: 1px dashed rgba(0, 0, 0, 0.1);
  box-shadow: none;
}

.user-message .message-content {
  background-color: #e3f2fd;
  border-bottom-right-radius: 4px;
}

.agent-message .message-content {
  background-color: white;
  border-bottom-left-radius: 4px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.message-body {
  white-space: pre-wrap;
  word-break: break-word;
}

.notification-body {
  display: flex;
  align-items: center;
  font-size: 0.9rem;
  color: rgba(0, 0, 0, 0.7);
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

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
}

.markdown-body h1 {
  font-size: 1.5em;
}

.markdown-body h2 {
  font-size: 1.3em;
}

.markdown-body h3 {
  font-size: 1.2em;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 20px;
  margin-bottom: 8px;
}

.markdown-body code {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

.markdown-body pre {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
  margin-bottom: 8px;
}

.markdown-body pre code {
  background-color: transparent;
  padding: 0;
}

.markdown-body table {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 8px;
}

.markdown-body table th,
.markdown-body table td {
  border: 1px solid #ddd;
  padding: 6px 8px;
}

.markdown-body table th {
  background-color: rgba(0, 0, 0, 0.05);
}

.markdown-body blockquote {
  border-left: 4px solid #ddd;
  padding-left: 8px;
  color: #666;
  margin-left: 0;
  margin-right: 0;
}

.markdown-body img {
  max-width: 100%;
}

.markdown-body a {
  color: #1976d2;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}
</style> 