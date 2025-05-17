<script setup>
import { ref, computed, watch } from 'vue'
import { userAdminApi } from '@/services/userAdminApi'
import { validationRules } from '@/components/FormValidation'

const props = defineProps({
  modelValue: Boolean,
  user: Object,
  userType: Number,
})

const emit = defineEmits(['update:modelValue', 'saved'])

// 表单参考
const formRef = ref(null)

// 本地表单数据
const formData = ref({
  id: null,
  username: '',
  realName: '',
  gender: 1,
  age: 18,
  userType: 0,
  directionId: null
})

// 是否为编辑模式
const isEditMode = computed(() => formData.value.id !== null)

// 对话框标题
const dialogTitle = computed(() => {
  const typeText = userTypeText.value
  return isEditMode.value ? `编辑${typeText}` : `添加${typeText}`
})

// 用户类型文本
const userTypeText = computed(() => {
  const typeMap = {
    0: '病人',
    1: '医生'
  }
  return typeMap[props.userType] || '用户'
})

// 医疗方向列表
const directions = ref([])
const loading = ref(false)
const error = ref('')

// 表单验证规则
const requiredRule = [v => !!v || '该字段不能为空']

// 当用户数据变化时，更新表单数据
watch(() => props.user, (newUser) => {
  if (newUser) {
    formData.value = { ...newUser }
  } else {
    resetForm()
  }
}, { deep: true })

// 当对话框打开时，如果是医生类型，则加载医疗方向
watch(() => props.modelValue, async (newVal) => {
  if (newVal && props.userType === 1) {
    await loadDirections()
  }
})

// 加载医疗方向列表
const loadDirections = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await userAdminApi.getDirectionList()
    if (response.code === 200) {
      directions.value = response.data
    } else {
      error.value = response.message || '加载医疗方向失败'
    }
  } catch (err) {
    console.error('加载医疗方向错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    username: '',
    realName: '',
    gender: 1,
    age: 18,
    userType: props.userType,
    directionId: null
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

// 保存用户
const saveUser = async () => {
  if (!formRef.value) return
  
  const { valid } = await formRef.value.validate()
  if (!valid) return
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await userAdminApi.saveUser(formData.value)
    if (response.code === 200) {
      emit('saved')
      closeDialog()
    } else {
      error.value = response.message || '保存用户失败'
    }
  } catch (err) {
    console.error('保存用户错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" max-width="500" persistent>
    <v-card class="user-edit-dialog">
      <v-card-title class="d-flex align-center py-4 px-6">
        <v-icon 
          :icon="isEditMode ? 'mdi-account-edit' : 'mdi-account-plus'" 
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
                v-model="formData.username"
                label="用户名"
                :disabled="isEditMode"
                :rules="validationRules.username"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12">
              <v-text-field
                v-model="formData.realName"
                label="真实姓名"
                :rules="requiredRule"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12" sm="6">
              <v-select
                v-model="formData.gender"
                label="性别"
                :items="[
                  { title: '男', value: 1 },
                  { title: '女', value: 2 }
                ]"
                item-title="title"
                item-value="value"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-select>
            </v-col>
            
            <v-col cols="12" sm="6">
              <v-text-field
                v-model.number="formData.age"
                label="年龄"
                type="number"
                :rules="validationRules.age"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12" v-if="userType === 1">
              <v-select
                v-model="formData.directionId"
                label="医疗方向"
                :items="directions"
                item-title="name"
                item-value="id"
                :rules="requiredRule"
                :loading="loading"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-select>
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
          @click="saveUser"
          :loading="loading"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.user-edit-dialog {
  border-radius: 12px;
}
</style> 