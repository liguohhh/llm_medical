<script setup>
import { ref, computed, watch } from 'vue'
import { doctorApi } from '@/services/doctorApi'

// 定义props
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

// 状态变量
const name = ref('')
const loading = ref(false)
const error = ref(null)

// 计算属性：对话框标题
const dialogTitle = computed(() => {
  return props.category ? '编辑大类' : '添加大类'
})

// 监听对话框打开，初始化表单
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    // 如果是编辑模式，填充表单
    if (props.category) {
      name.value = props.category.name
    } else {
      // 如果是添加模式，重置表单
      name.value = ''
    }
    error.value = null
  }
})

// 保存大类
const saveCategory = async () => {
  // 表单验证
  if (!name.value.trim()) {
    error.value = '大类名称不能为空'
    return
  }
  
  loading.value = true
  error.value = null
  
  try {
    let response
    
    if (props.category) {
      // 更新大类
      response = await doctorApi.updatePreciseCategory(props.category.uid, {
        name: name.value.trim()
      })
    } else {
      // 创建大类
      response = await doctorApi.createPreciseCategory({
        name: name.value.trim()
      })
    }
    
    if (response.code === 200) {
      // 关闭对话框并通知父组件保存成功
      emit('update:modelValue', false)
      emit('saved')
    } else {
      error.value = response.message || '保存失败'
    }
  } catch (err) {
    console.error('保存大类出错:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const closeDialog = () => {
  emit('update:modelValue', false)
}

// 定义事件
const emit = defineEmits(['update:modelValue', 'saved'])
</script>

<template>
  <v-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    max-width="500px"
    persistent
  >
    <v-card>
      <v-card-title>{{ dialogTitle }}</v-card-title>
      
      <v-card-text>
        <!-- 错误提示 -->
        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          closable
          class="mb-3"
          @click:close="error = null"
        >
          {{ error }}
        </v-alert>
        
        <!-- 表单 -->
        <v-form @submit.prevent="saveCategory">
          <v-text-field
            v-model="name"
            label="大类名称"
            variant="outlined"
            :disabled="loading"
            required
            autofocus
            :error-messages="error && !name.trim() ? ['大类名称不能为空'] : []"
          ></v-text-field>
        </v-form>
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
          color="primary"
          variant="tonal"
          @click="saveCategory"
          :loading="loading"
          :disabled="!name.trim()"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template> 