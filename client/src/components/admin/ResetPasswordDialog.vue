<script setup>
import { ref } from 'vue'
import { userAdminApi } from '@/services/userAdminApi'
import { validationRules } from '@/components/FormValidation'

const props = defineProps({
  modelValue: Boolean,
  userId: Number,
  userName: String
})

const emit = defineEmits(['update:modelValue', 'completed'])

// 表单引用
const formRef = ref(null)

// 新密码
const newPassword = ref('')
const confirmPassword = ref('')

// 状态
const loading = ref(false)
const error = ref('')

// 密码确认验证规则
const confirmRules = [
  v => !!v || '确认密码不能为空',
  v => v === newPassword.value || '两次输入的密码不一致'
]

// 关闭对话框
const closeDialog = () => {
  emit('update:modelValue', false)
  resetForm()
}

// 重置表单
const resetForm = () => {
  newPassword.value = ''
  confirmPassword.value = ''
  error.value = ''
  if (formRef.value) {
    formRef.value.resetValidation()
  }
}

// 提交重置密码
const submitReset = async () => {
  if (!formRef.value) return
  
  const { valid } = await formRef.value.validate()
  if (!valid) return
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await userAdminApi.resetUserPassword(props.userId, newPassword.value)
    if (response.code === 200) {
      emit('completed')
      closeDialog()
    } else {
      error.value = response.message || '重置密码失败'
    }
  } catch (err) {
    console.error('重置密码错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" max-width="500" persistent>
    <v-card class="reset-password-dialog">
      <v-card-title class="d-flex align-center py-4 px-6">
        <v-icon icon="mdi-lock-reset" size="large" class="me-2 text-warning"></v-icon>
        重置密码
      </v-card-title>
      
      <v-divider></v-divider>
      
      <v-card-text class="pa-6">
        <p class="mb-4">
          您正在为用户 <strong class="text-primary">{{ userName }}</strong> 重置密码。
        </p>
        
        <v-form ref="formRef">
          <v-text-field
            v-model="newPassword"
            label="新密码"
            type="password"
            :rules="validationRules.password"
            variant="outlined"
            density="comfortable"
            hide-details="auto"
            class="mb-4"
          ></v-text-field>
          
          <v-text-field
            v-model="confirmPassword"
            label="确认密码"
            type="password"
            :rules="confirmRules"
            variant="outlined"
            density="comfortable"
            hide-details="auto"
          ></v-text-field>
          
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
          color="warning" 
          @click="submitReset"
          :loading="loading"
        >
          重置密码
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.reset-password-dialog {
  border-radius: 12px;
}
</style> 