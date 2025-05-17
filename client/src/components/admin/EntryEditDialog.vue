<script setup>
import { ref, computed, watch } from 'vue'
import { preciseAdminApi } from '@/services/preciseAdminApi'

// 属性定义
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  entry: {
    type: Object,
    default: null
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
  description: '',
  content: '',
  keywords: [],
  weight: 50,
  isEnabled: true
})

// 关键词输入
const keywordInput = ref('')

// 表单验证规则
const descriptionRules = [
  v => !!v || '描述不能为空',
  v => (v && v.length <= 50) || '描述不能超过50个字符'
]

const contentRules = [
  v => !!v || '内容不能为空',
  v => (v && v.length <= 2000) || '内容不能超过2000个字符'
]

const weightRules = [
  v => v !== null || '权重不能为空',
  v => (v >= 0 && v <= 100) || '权重必须在0-100之间'
]

// 状态变量
const loading = ref(false)
const error = ref('')

// 是否为编辑模式
const isEditMode = computed(() => !!props.entry)

// 对话框标题
const dialogTitle = computed(() => isEditMode.value ? '编辑条目' : '添加条目')

// 重置表单
const resetForm = () => {
  form.value = {
    description: '',
    content: '',
    keywords: [],
    weight: 50,
    isEnabled: true
  }
  keywordInput.value = ''
  error.value = ''
}

// 监听entry变化，更新表单数据
watch(() => props.entry, (newEntry) => {
  if (newEntry) {
    form.value = {
      description: newEntry.description || '',
      content: newEntry.content || '',
      keywords: [...(newEntry.keywords || [])],
      weight: newEntry.weight || 50,
      isEnabled: newEntry.is_enabled !== undefined ? newEntry.is_enabled : true
    }
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

// 添加关键词
const addKeyword = () => {
  if (keywordInput.value && !form.value.keywords.includes(keywordInput.value)) {
    form.value.keywords.push(keywordInput.value)
    keywordInput.value = ''
  }
}

// 删除关键词
const removeKeyword = (keyword) => {
  form.value.keywords = form.value.keywords.filter(k => k !== keyword)
}

// 保存条目
const saveEntry = async () => {
  if (form.value.keywords.length === 0) {
    error.value = '请至少添加一个关键词'
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    let response
    
    if (isEditMode.value) {
      // 更新条目
      response = await preciseAdminApi.updateEntry(props.entry.uid, {
        description: form.value.description,
        content: form.value.content,
        keywords: form.value.keywords,
        weight: form.value.weight,
        isEnabled: form.value.isEnabled
      })
    } else {
      // 创建条目
      response = await preciseAdminApi.createEntry(props.category.uid, {
        description: form.value.description,
        content: form.value.content,
        keywords: form.value.keywords,
        weight: form.value.weight,
        isEnabled: form.value.isEnabled
      })
    }
    
    if (response.code === 200) {
      emit('saved')
      closeDialog()
    } else {
      error.value = response.message || '保存条目失败'
    }
  } catch (err) {
    console.error('保存条目错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-dialog
    v-model="props.modelValue"
    max-width="700"
    persistent
    scrollable
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
        
        <v-form @submit.prevent="saveEntry">
          <v-text-field
            v-model="form.description"
            label="条目描述"
            variant="outlined"
            :rules="descriptionRules"
            :disabled="loading"
            required
            class="mb-4"
          ></v-text-field>
          
          <v-textarea
            v-model="form.content"
            label="条目内容"
            variant="outlined"
            :rules="contentRules"
            :disabled="loading"
            required
            rows="6"
            class="mb-4"
          ></v-textarea>
          
          <div class="mb-4">
            <label class="text-subtitle-2 d-block mb-2">关键词</label>
            <div class="d-flex align-center mb-2">
              <v-text-field
                v-model="keywordInput"
                label="添加关键词"
                variant="outlined"
                density="compact"
                :disabled="loading"
                hide-details
                class="flex-grow-1 mr-2"
                @keyup.enter="addKeyword"
              ></v-text-field>
              <v-btn
                color="primary"
                variant="tonal"
                @click="addKeyword"
                :disabled="!keywordInput || loading"
              >
                添加
              </v-btn>
            </div>
            
            <div class="keywords-container mt-2">
              <v-chip
                v-for="keyword in form.keywords"
                :key="keyword"
                class="ma-1"
                closable
                @click:close="removeKeyword(keyword)"
              >
                {{ keyword }}
              </v-chip>
              <div v-if="form.keywords.length === 0" class="text-medium-emphasis text-caption pa-2">
                请添加至少一个关键词
              </div>
            </div>
          </div>
          
          <v-slider
            v-model="form.weight"
            label="权重"
            :rules="weightRules"
            :disabled="loading"
            min="0"
            max="100"
            step="1"
            thumb-label
            class="mb-4"
          >
            <template v-slot:append>
              <v-text-field
                v-model="form.weight"
                type="number"
                style="width: 70px"
                density="compact"
                hide-details
                :disabled="loading"
                variant="outlined"
              ></v-text-field>
            </template>
          </v-slider>
          
          <v-switch
            v-model="form.isEnabled"
            label="启用条目"
            color="success"
            :disabled="loading"
            hide-details
            class="mb-2"
          ></v-switch>
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
          @click="saveEntry"
          :loading="loading"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.keywords-container {
  min-height: 50px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  padding: 8px;
  background-color: #f5f5f5;
}
</style> 