<script setup>
import { ref, computed, watch } from 'vue'
import { preciseAdminApi } from '@/services/preciseAdminApi'

// 属性定义
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  category: {
    type: Object,
    default: null
  }
})

// 事件
const emit = defineEmits(['update:modelValue', 'saved'])

// 表单数据
const form = ref({
  name: ''
})

// 表单验证规则
const nameRules = [
  v => !!v || '名称不能为空',
  v => (v && v.length <= 50) || '名称不能超过50个字符'
]

// 状态变量
const loading = ref(false)
const error = ref('')

// 是否为编辑模式
const isEditMode = computed(() => !!props.category)

// 对话框标题
const dialogTitle = computed(() => isEditMode.value ? '编辑大类' : '添加大类')

// 重置表单
const resetForm = () => {
  form.value = {
    name: ''
  }
  error.value = ''
}

// 监听category变化，更新表单数据
watch(() => props.category, (newCategory) => {
  if (newCategory) {
    form.value.name = newCategory.name
  } else {
    resetForm()
  }
}, { immediate: true })

// 监听对话框状态
watch(() => props.modelValue, (newValue) => {
  if (!newValue) {
    resetForm()
  }
})

// 关闭对话框
const closeDialog = () => {
  emit('update:modelValue', false)
}

// 保存大类
const saveCategory = async () => {
  loading.value = true
  error.value = ''
  
  try {
    let response
    
    if (isEditMode.value) {
      // 更新大类
      response = await preciseAdminApi.updateCategory(props.category.uid, {
        name: form.value.name
      })
    } else {
      // 创建大类
      response = await preciseAdminApi.createCategory({
        name: form.value.name
      })
    }
    
    if (response.code === 200) {
      emit('saved')
      closeDialog()
    } else {
      error.value = response.message || '保存大类失败'
    }
  } catch (err) {
    console.error('保存大类错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-dialog
    v-model="props.modelValue"
    max-width="500"
    persistent
    @update:modelValue="emit('update:modelValue', $event)"
  >
    <v-card>
      <v-card-title class="text-h6 pa-4">
        {{ dialogTitle }}
      </v-card-title>
      
      <v-divider></v-divider>
      
      <v-card-text class="pa-4">
        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          density="compact"
        >
          {{ error }}
        </v-alert>
        
        <v-form @submit.prevent="saveCategory">
          <v-text-field
            v-model="form.name"
            label="大类名称"
            variant="outlined"
            :rules="nameRules"
            :disabled="loading"
            required
            class="mb-2"
          ></v-text-field>
        </v-form>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions class="pa-4">
        <v-spacer></v-spacer>
        <v-btn
          variant="text"
          @click="closeDialog"
          :disabled="loading"
        >
          取消
        </v-btn>
        <v-btn
          color="primary"
          @click="saveCategory"
          :loading="loading"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template> 