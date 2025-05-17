<script setup>
// 定义props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '确认删除'
  },
  content: {
    type: String,
    default: '确定要删除此项吗？此操作不可恢复。'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['update:modelValue', 'confirm'])

// 关闭对话框
const closeDialog = () => {
  emit('update:modelValue', false)
}

// 确认删除
const confirmDelete = () => {
  emit('confirm')
}
</script>

<template>
  <v-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    max-width="500px"
    persistent
  >
    <v-card>
      <v-card-title class="text-error">{{ title }}</v-card-title>
      
      <v-card-text>
        <p>{{ content }}</p>
      </v-card-text>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="grey-darken-1"
          variant="text"
          @click="closeDialog"
          :disabled="loading"
        >
          取消
        </v-btn>
        <v-btn
          color="error"
          variant="tonal"
          @click="confirmDelete"
          :loading="loading"
        >
          确认删除
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template> 