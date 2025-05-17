<script setup>
import { defineProps, defineEmits, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  sending: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '输入消息...'
  }
})

const emit = defineEmits(['update:modelValue', 'send', 'keydown'])

const message = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 发送消息
const sendMessage = () => {
  if (props.sending || props.disabled) return
  if (message.value.trim()) {
    emit('send')
  }
}

// 处理键盘事件
const handleKeyDown = (event) => {
  emit('keydown', event)
}
</script>

<template>
  <div class="message-input-container">
    <v-textarea
      v-model="message"
      @keydown="handleKeyDown"
      :disabled="sending || disabled"
      :placeholder="disabled ? '对话已结束，无法发送消息' : placeholder"
      variant="outlined"
      rows="2"
      auto-grow
      hide-details
      bg-color="white"
      class="message-textarea"
    ></v-textarea>
    <v-btn
      icon="mdi-send"
      class="send-button ml-2"
      :disabled="!message.trim() || sending || disabled"
      @click="sendMessage"
      color="primary"
    ></v-btn>
  </div>
</template>

<style scoped>
.message-input-container {
  display: flex;
  align-items: flex-end;
}

.message-textarea {
  flex: 1;
}

.send-button {
  margin-bottom: 8px;
}

/* 覆盖textarea默认样式 */
:deep(.v-field__outline) {
  --v-field-border-width: 1px !important;
}
</style> 