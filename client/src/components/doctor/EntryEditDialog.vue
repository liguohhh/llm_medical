<script setup>
import { ref, computed, watch } from 'vue'
import { doctorApi } from '@/services/doctorApi'

// 定义props
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

// 状态变量
const description = ref('')
const content = ref('')
const keywords = ref([])
const weight = ref(1)
const isEnabled = ref(true)
const keywordInput = ref('')
const loading = ref(false)
const error = ref(null)

// 计算属性：对话框标题
const dialogTitle = computed(() => {
  return props.entry ? '编辑条目' : '添加条目'
})

// 监听对话框打开，初始化表单
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    // 如果是编辑模式，填充表单
    if (props.entry) {
      description.value = props.entry.description
      content.value = props.entry.content
      keywords.value = [...(props.entry.keywords || [])]
      weight.value = props.entry.weight || 1
      isEnabled.value = props.entry.isEnabled !== false
    } else {
      // 如果是添加模式，重置表单
      description.value = ''
      content.value = ''
      keywords.value = []
      weight.value = 1
      isEnabled.value = true
    }
    keywordInput.value = ''
    error.value = null
  }
})

// 添加关键词
const addKeyword = () => {
  const trimmedKeyword = keywordInput.value.trim()
  if (trimmedKeyword && !keywords.value.includes(trimmedKeyword)) {
    keywords.value.push(trimmedKeyword)
  }
  keywordInput.value = ''
}

// 删除关键词
const removeKeyword = (index) => {
  keywords.value.splice(index, 1)
}

// 保存条目
const saveEntry = async () => {
  // 表单验证
  if (!description.value.trim()) {
    error.value = '条目描述不能为空'
    return
  }
  
  if (!content.value.trim()) {
    error.value = '条目内容不能为空'
    return
  }
  
  loading.value = true
  error.value = null
  
  try {
    const entryData = {
      description: description.value.trim(),
      content: content.value.trim(),
      keywords: keywords.value,
      weight: weight.value,
      isEnabled: isEnabled.value
    }
    
    let response
    
    if (props.entry) {
      // 更新条目
      response = await doctorApi.updatePreciseEntry(props.entry.uid, entryData)
    } else {
      // 创建条目
      if (!props.category) {
        error.value = '未选择大类'
        loading.value = false
        return
      }
      response = await doctorApi.createPreciseEntry(props.category.uid, entryData)
    }
    
    if (response.code === 200) {
      // 关闭对话框并通知父组件保存成功
      emit('update:modelValue', false)
      emit('saved')
    } else {
      error.value = response.message || '保存失败'
    }
  } catch (err) {
    console.error('保存条目出错:', err)
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
    max-width="700px"
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
        <v-form @submit.prevent="saveEntry">
          <v-text-field
            v-model="description"
            label="条目描述"
            variant="outlined"
            :disabled="loading"
            required
            autofocus
            :error-messages="error && !description.trim() ? ['条目描述不能为空'] : []"
            class="mb-3"
          ></v-text-field>
          
          <v-textarea
            v-model="content"
            label="条目内容"
            variant="outlined"
            :disabled="loading"
            required
            rows="5"
            :error-messages="error && !content.trim() ? ['条目内容不能为空'] : []"
            class="mb-3"
          ></v-textarea>
          
          <div class="mb-3">
            <label class="text-subtitle-2 mb-1 d-block">关键词</label>
            <div class="d-flex align-center mb-2">
              <v-text-field
                v-model="keywordInput"
                label="添加关键词"
                variant="outlined"
                density="compact"
                hide-details
                :disabled="loading"
                class="flex-grow-1 me-2"
                @keyup.enter="addKeyword"
              ></v-text-field>
              <v-btn
                color="primary"
                variant="tonal"
                :disabled="!keywordInput.trim() || loading"
                @click="addKeyword"
              >
                添加
              </v-btn>
            </div>
            
            <div v-if="keywords.length > 0" class="keywords-container">
              <v-chip
                v-for="(keyword, index) in keywords"
                :key="index"
                closable
                @click:close="removeKeyword(index)"
                :disabled="loading"
                class="ma-1"
              >
                {{ keyword }}
              </v-chip>
            </div>
            <div v-else class="text-caption text-grey">
              暂无关键词，请添加
            </div>
          </div>
          
          <div class="mb-3">
            <label class="text-subtitle-2 mb-1 d-block">权重 (1-5)</label>
            <div class="d-flex align-center">
              <v-slider
                v-model="weight"
                :min="1"
                :max="5"
                :step="1"
                :disabled="loading"
                color="primary"
                thumb-label
                class="flex-grow-1 me-2"
              ></v-slider>
              <v-text-field
                v-model.number="weight"
                type="number"
                :min="1"
                :max="5"
                variant="outlined"
                density="compact"
                hide-details
                :disabled="loading"
                style="width: 80px"
              ></v-text-field>
            </div>
          </div>
          
          <v-switch
            v-model="isEnabled"
            label="启用条目"
            color="success"
            :disabled="loading"
            hide-details
          ></v-switch>
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
          @click="saveEntry"
          :loading="loading"
          :disabled="!description.trim() || !content.trim()"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.keywords-container {
  min-height: 40px;
  padding: 4px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  background-color: rgba(0, 0, 0, 0.02);
}
</style> 