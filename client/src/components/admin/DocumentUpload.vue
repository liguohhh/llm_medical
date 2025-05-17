<script setup>
import { ref, computed, watch } from 'vue'
import { vectorAdminApi } from '@/services/vectorAdminApi'

// 属性定义
const props = defineProps({
  namespace: {
    type: String,
    default: null
  }
})

// 事件
const emit = defineEmits(['upload-success'])

// 状态变量
const file = ref(null)
const chunkSize = ref(500) // 默认分块大小
const chunkOverlap = ref(50) // 默认分块重叠大小
const loading = ref(false)
const error = ref('')
const success = ref('')

// 是否显示表单
const showForm = computed(() => !!props.namespace)

// 重置表单
const resetForm = () => {
  file.value = null
  chunkSize.value = 500
  chunkOverlap.value = 50
  error.value = ''
  success.value = ''
}

// 处理文件选择
const handleFileChange = (e) => {
  const files = e.target.files
  if (files && files.length > 0) {
    file.value = files[0]
  }
}

// 上传文档
const uploadDocument = async () => {
  if (!props.namespace) {
    error.value = '请先选择一个命名空间'
    return
  }
  
  if (!file.value) {
    error.value = '请选择要上传的文件'
    return
  }
  
  // 验证分块大小和分块重叠大小
  if (chunkSize.value <= 0) {
    error.value = '分块大小必须大于0'
    return
  }
  
  if (chunkOverlap.value < 0) {
    error.value = '分块重叠大小不能小于0'
    return
  }
  
  if (chunkOverlap.value >= chunkSize.value) {
    error.value = '分块重叠大小必须小于分块大小'
    return
  }
  
  loading.value = true
  error.value = ''
  success.value = ''
  
  try {
    const formData = new FormData()
    formData.append('file', file.value)
    formData.append('namespace', props.namespace)
    
    // 添加分块大小和分块重叠大小参数
    if (chunkSize.value) {
      formData.append('chunkSize', chunkSize.value)
    }
    
    if (chunkOverlap.value) {
      formData.append('chunkOverlap', chunkOverlap.value)
    }
    
    const response = await vectorAdminApi.uploadDocument(formData)
    
    if (response.code === 200) {
      success.value = '文档上传成功'
      resetForm()
      emit('upload-success')
    } else {
      error.value = response.message || '文档上传失败'
    }
  } catch (err) {
    console.error('文档上传错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 监听命名空间变化
watch(() => props.namespace, () => {
  resetForm()
}, { immediate: true })
</script>

<template>
  <v-card class="upload-card">
    <v-card-title class="pa-4">
      <div class="text-h6">上传文档</div>
    </v-card-title>
    
    <v-divider></v-divider>
    
    <v-card-text>
      <v-alert
        v-if="!showForm"
        type="info"
        variant="tonal"
        class="mb-4"
        density="compact"
      >
        请先选择一个命名空间以上传文档
      </v-alert>
      
      <v-form v-if="showForm" @submit.prevent="uploadDocument">
        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          density="compact"
        >
          {{ error }}
        </v-alert>
        
        <v-alert
          v-if="success"
          type="success"
          variant="tonal"
          class="mb-4"
          density="compact"
        >
          {{ success }}
        </v-alert>
        
        <div class="mb-4">
          <label class="text-subtitle-2 d-block mb-2">选择文件</label>
          <v-file-input
            v-model="file"
            label="选择要上传的文件"
            variant="outlined"
            density="compact"
            hide-details
            @change="handleFileChange"
            :disabled="loading"
          ></v-file-input>
        </div>
        
        <div class="mb-4">
          <v-row>
            <v-col cols="12">
              <div class="text-body-2 text-medium-emphasis mb-2">
                <v-icon icon="mdi-information-outline" size="small" class="me-1"></v-icon>
                分块参数用于控制文档如何被切分并存入向量数据库，影响检索质量和性能
              </div>
            </v-col>
            <v-col cols="12" sm="6">
              <label class="text-subtitle-2 d-block mb-2">分块大小</label>
              <v-text-field
                v-model.number="chunkSize"
                label="文档分块大小"
                type="number"
                min="100"
                hint="默认值: 500，较大的值适合长文本语义理解"
                persistent-hint
                variant="outlined"
                density="compact"
                :disabled="loading"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12" sm="6">
              <label class="text-subtitle-2 d-block mb-2">分块重叠大小</label>
              <v-text-field
                v-model.number="chunkOverlap"
                label="分块重叠大小"
                type="number"
                min="0"
                hint="默认值: 50，增加重叠可提高上下文连贯性"
                persistent-hint
                variant="outlined"
                density="compact"
                :disabled="loading"
              ></v-text-field>
            </v-col>
          </v-row>
        </div>
        
        <div class="d-flex justify-end">
          <v-btn
            color="primary"
            type="submit"
            :loading="loading"
            :disabled="!props.namespace"
          >
            上传文档
          </v-btn>
        </div>
      </v-form>
    </v-card-text>
  </v-card>
</template>

<style scoped>
.upload-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
</style> 