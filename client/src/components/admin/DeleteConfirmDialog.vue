<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  title: {
    type: String,
    default: '确认删除'
  },
  message: {
    type: String,
    default: '确定要删除此项吗？此操作无法撤销。'
  },
  loading: Boolean
})

const emit = defineEmits(['update:modelValue', 'confirm'])

// 关闭对话框
const closeDialog = () => {
  emit('update:modelValue', false)
}

// 确认操作
const confirmAction = () => {
  emit('confirm')
}
</script>

<template>
  <v-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" max-width="400">
    <v-card class="delete-confirm-dialog">
      <v-card-title class="d-flex align-center py-4 px-6">
        <v-icon icon="mdi-alert-circle" size="large" class="me-2 text-error"></v-icon>
        {{ title }}
      </v-card-title>
      
      <v-divider></v-divider>
      
      <v-card-text class="pa-6">
        <p>{{ message }}</p>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions class="pa-4">
        <v-spacer></v-spacer>
        <v-btn 
          variant="text" 
          @click="closeDialog"
        >
          取消
        </v-btn>
        <v-btn 
          color="error" 
          @click="confirmAction"
          :loading="loading"
        >
          确认删除
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.delete-confirm-dialog {
  border-radius: 12px;
}
</style> 