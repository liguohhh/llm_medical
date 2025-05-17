<script setup>
import { ref, computed, watch } from 'vue'
import { directionAdminApi } from '@/services/directionAdminApi'

const props = defineProps({
  modelValue: Boolean,
  direction: Object
})

const emit = defineEmits(['update:modelValue', 'saved'])

// 表单参考
const formRef = ref(null)

// 本地表单数据
const formData = ref({
  id: null,
  name: '',
  description: ''
})

// 是否为编辑模式
const isEditMode = computed(() => formData.value.id !== null)

// 对话框标题
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑医疗方向' : '添加医疗方向'
})

// 状态
const loading = ref(false)
const error = ref('')

// 表单验证规则
const nameRules = [
  v => !!v || '名称不能为空',
  v => (v && v.length <= 50) || '名称不能超过50个字符'
]

const descriptionRules = [
  v => !!v || '描述不能为空',
  v => (v && v.length <= 200) || '描述不能超过200个字符'
]

// 当方向数据变化时，更新表单数据
watch(() => props.direction, (newDirection) => {
  if (newDirection) {
    formData.value = { ...newDirection }
  } else {
    resetForm()
  }
}, { deep: true })

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    name: '',
    description: ''
  }
  if (formRef.value) {
    formRef.value.resetValidation()
  }
}

// 关闭对话框
const closeDialog = () => {
  emit('update:modelValue', false)
  resetForm()
}

// 保存医疗方向
const saveDirection = async () => {
  if (!formRef.value) return
  
  const { valid } = await formRef.value.validate()
  if (!valid) return
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await directionAdminApi.saveDirection(formData.value)
    if (response.code === 200) {
      emit('saved')
      closeDialog()
    } else {
      error.value = response.message || '保存医疗方向失败'
    }
  } catch (err) {
    console.error('保存医疗方向错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" max-width="500" persistent>
    <v-card class="direction-edit-dialog">
      <v-card-title class="d-flex align-center py-4 px-6">
        <v-icon 
          :icon="isEditMode ? 'mdi-hospital-box' : 'mdi-hospital-box-outline'" 
          size="large" 
          class="me-2 text-primary"
        ></v-icon>
        {{ dialogTitle }}
      </v-card-title>
      
      <v-divider></v-divider>
      
      <v-card-text class="pa-6">
        <v-form ref="formRef">
          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="formData.name"
                label="医疗方向名称"
                :rules="nameRules"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12">
              <v-textarea
                v-model="formData.description"
                label="描述"
                :rules="descriptionRules"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
                rows="3"
                auto-grow
              ></v-textarea>
            </v-col>
          </v-row>
          
          <v-alert
            v-if="error"
            type="error"
            variant="tonal"
            class="mt-4"
            density="compact"
          >
            {{ error }}
          </v-alert>
        </v-form>
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
          color="primary" 
          @click="saveDirection"
          :loading="loading"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.direction-edit-dialog {
  border-radius: 12px;
}
</style> 