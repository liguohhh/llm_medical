<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { agentAdminApi } from '@/services/agentAdminApi'
import { templateAdminApi } from '@/services/templateAdminApi'
import { vectorAdminApi } from '@/services/vectorAdminApi'
import { preciseAdminApi } from '@/services/preciseAdminApi'

const props = defineProps({
  modelValue: Boolean,
  agent: Object
})

const emit = defineEmits(['update:modelValue', 'saved'])

// 表单参考
const formRef = ref(null)

// 本地表单数据
const formData = ref({
  id: null,
  name: '',
  directionId: null,
  modelName: 'gpt-3.5-turbo',
  modelUrl: 'https://api.openai.com/v1',
  apiKey: '',
  templateId: '',
  templateDescription: '',
  templateParameters: '{}',
  vectorNamespaces: [],
  preciseDbName: '',
  preciseDbUids: []
})

// 是否为编辑模式
const isEditMode = computed(() => formData.value.id !== null)

// 对话框标题
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑智能体' : '添加智能体'
})

// 状态
const loading = ref(false)
const error = ref('')
const directions = ref([])
const loadingDirections = ref(false)

// 模板、向量数据库和精确查找数据库数据
const templates = ref([])
const loadingTemplates = ref(false)
const vectorNamespaces = ref([])
const loadingVectorNamespaces = ref(false)
const preciseCategories = ref([])
const loadingPreciseCategories = ref(false)

// 表单验证规则
const requiredRule = [v => !!v || '该字段不能为空']
const jsonRule = [
  v => {
    if (!v) return true
    try {
      JSON.parse(v)
      return true
    } catch (e) {
      return 'JSON格式不正确'
    }
  }
]

// 处理向量命名空间和精确数据库UID的转换
const processVectorNamespaces = (value) => {
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch (e) {
      return []
    }
  }
  return value || []
}

// 处理精确数据库UID的转换
const processPreciseDbUids = (value) => {
  if (typeof value === 'string') {
    try {
      // 将字符串格式的UID数组转换为对象数组
      const uids = JSON.parse(value);
      // 对每个UID，找到对应的分类对象
      return uids.map(uid => {
        const category = preciseCategories.value.find(cat => cat.uid === uid);
        return category || uid; // 如果找不到对应的分类，则保留原始UID
      }).filter(item => item); // 过滤掉可能的null或undefined
    } catch (e) {
      return [];
    }
  } else if (Array.isArray(value)) {
    // 如果已经是数组，检查是否是对象数组
    if (value.length > 0 && typeof value[0] === 'object') {
      return value; // 已经是对象数组，直接返回
    } else {
      // 是UID数组，转换为对象数组
      return value.map(uid => {
        const category = preciseCategories.value.find(cat => cat.uid === uid);
        return category || uid;
      }).filter(item => item);
    }
  }
  return [];
}

// 监听对话框打开状态，确保在打开时正确初始化表单数据
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    if (props.agent) {
      // 编辑模式：使用传入的智能体数据
      // 注意：不要直接修改props.agent，创建一个副本
      const agent = { ...props.agent }
      agent.vectorNamespaces = processVectorNamespaces(agent.vectorNamespaces)
      
      // 处理精确查找大类数据
      if (agent.preciseDbUids) {
        const uids = processPreciseDbUids(agent.preciseDbUids)
        // 如果preciseCategories已加载，将uid转换为对象
        if (preciseCategories.value.length > 0) {
          agent.preciseDbUids = uids.map(uid => {
            // 如果已经是对象，直接返回
            if (typeof uid === 'object' && uid !== null) return uid
            // 否则查找对应的分类对象
            const category = preciseCategories.value.find(cat => cat.uid === uid)
            return category || null
          }).filter(Boolean)
        } else {
          agent.preciseDbUids = uids
        }
      } else {
        agent.preciseDbUids = []
      }
      
      formData.value = agent
    } else {
      // 添加模式：重置表单
      resetForm()
    }
  }
})

// 当精确查找大类数据加载完成后，处理preciseDbUids
watch(() => preciseCategories.value, (newCategories) => {
  if (newCategories.length > 0 && formData.value.preciseDbUids.length > 0) {
    // 如果preciseDbUids是字符串数组，将其转换为对象数组
    if (typeof formData.value.preciseDbUids[0] === 'string') {
      formData.value.preciseDbUids = formData.value.preciseDbUids.map(uid => {
        const category = newCategories.find(cat => cat.uid === uid);
        return category || null;
      }).filter(Boolean);
    }
  }
}, { immediate: true });

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    name: '',
    directionId: null,
    modelName: 'gpt-3.5-turbo',
    modelUrl: 'https://api.openai.com/v1',
    apiKey: '',
    templateId: '',
    templateDescription: '',
    templateParameters: '{}',
    vectorNamespaces: [],
    preciseDbName: '',
    preciseDbUids: []
  }
  if (formRef.value) {
    formRef.value.resetValidation()
  }
}

// 关闭对话框
const closeDialog = () => {
  emit('update:modelValue', false)
  // 不要在这里重置表单，因为可能会再次打开同一个智能体的编辑窗口
  // resetForm()
}

// 加载医疗方向列表
const loadDirections = async () => {
  loadingDirections.value = true
  
  try {
    const response = await agentAdminApi.getDirectionList()
    if (response.code === 200) {
      directions.value = response.data || []
    } else {
      console.error('加载医疗方向失败:', response.message)
    }
  } catch (err) {
    console.error('加载医疗方向错误:', err)
  } finally {
    loadingDirections.value = false
  }
}

// 加载模板列表
const loadTemplates = async () => {
  loadingTemplates.value = true
  
  try {
    const response = await templateAdminApi.getTemplateIds()
    if (response.code === 200) {
      templates.value = response.data || []
      
      // 如果有选中的模板，获取其详情
      if (formData.value.templateId && !formData.value.templateDescription) {
        loadTemplateDetail(formData.value.templateId)
      }
    } else {
      console.error('加载模板列表失败:', response.message)
    }
  } catch (err) {
    console.error('加载模板列表错误:', err)
  } finally {
    loadingTemplates.value = false
  }
}

// 加载模板详情
const loadTemplateDetail = async (templateId) => {
  if (!templateId) return
  
  try {
    const response = await templateAdminApi.getTemplateDetail(templateId)
    if (response.code === 200 && response.data) {
      formData.value.templateDescription = response.data.description || ''
    }
  } catch (err) {
    console.error('加载模板详情错误:', err)
  }
}

// 模板选择变更时
const handleTemplateChange = (templateId) => {
  loadTemplateDetail(templateId)
}

// 加载向量数据库命名空间
const loadVectorNamespaces = async () => {
  loadingVectorNamespaces.value = true
  
  try {
    const response = await vectorAdminApi.getNamespaces()
    if (response.code === 200) {
      vectorNamespaces.value = response.data || []
    } else {
      console.error('加载向量命名空间失败:', response.message)
    }
  } catch (err) {
    console.error('加载向量命名空间错误:', err)
  } finally {
    loadingVectorNamespaces.value = false
  }
}

// 加载精确查找大类
const loadPreciseCategories = async () => {
  loadingPreciseCategories.value = true
  
  try {
    const response = await preciseAdminApi.getCategories()
    if (response.code === 200) {
      preciseCategories.value = response.data || []
    } else {
      console.error('加载精确查找大类失败:', response.message)
    }
  } catch (err) {
    console.error('加载精确查找大类错误:', err)
  } finally {
    loadingPreciseCategories.value = false
  }
}

// 保存智能体
const saveAgent = async () => {
  if (!formRef.value) return
  
  const { valid } = await formRef.value.validate()
  if (!valid) return
  
  loading.value = true
  error.value = ''
  
  try {
    // 创建要提交的数据对象
    const agentData = { ...formData.value }
    
    // 确保向量命名空间是数组
    if (!Array.isArray(agentData.vectorNamespaces)) {
      agentData.vectorNamespaces = []
    }
    
    // 处理精确查找大类数据
    if (Array.isArray(agentData.preciseDbUids)) {
      // 如果是对象数组，提取uid
      if (agentData.preciseDbUids.length > 0 && typeof agentData.preciseDbUids[0] === 'object') {
        // 提取所选分类的名称，用于设置preciseDbName
        const selectedCategoryNames = agentData.preciseDbUids
          .map(category => category.name)
          .filter(Boolean)
          .join(', ')
        
        // 设置精确数据库名称
        agentData.preciseDbName = selectedCategoryNames || ''
        
        // 提取uid数组
        agentData.preciseDbUids = agentData.preciseDbUids.map(category => category.uid)
      } else if (agentData.preciseDbUids.length > 0) {
        // 如果是uid数组，获取对应的名称
        const selectedCategoryNames = agentData.preciseDbUids
          .map(uid => {
            const category = preciseCategories.value.find(cat => cat.uid === uid)
            return category ? category.name : null
          })
          .filter(Boolean)
          .join(', ')
        
        // 设置精确数据库名称
        agentData.preciseDbName = selectedCategoryNames || ''
      } else {
        // 如果数组为空，清空名称
        agentData.preciseDbName = ''
      }
    } else {
      // 如果不是数组，设置为空数组
      agentData.preciseDbUids = []
      agentData.preciseDbName = ''
    }
    
    const response = await agentAdminApi.saveAgent(agentData)
    if (response.code === 200) {
      emit('saved')
      closeDialog()
    } else {
      error.value = response.message || '保存智能体失败'
    }
  } catch (err) {
    console.error('保存智能体错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载所有必要数据
onMounted(() => {
  loadDirections()
  loadTemplates()
  loadVectorNamespaces()
  loadPreciseCategories()
})
</script>

<template>
  <v-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" max-width="800" persistent>
    <v-card class="agent-edit-dialog">
      <v-card-title class="d-flex align-center py-4 px-6">
        <v-icon 
          :icon="isEditMode ? 'mdi-robot' : 'mdi-robot-outline'" 
          size="large" 
          class="me-2 text-primary"
        ></v-icon>
        {{ dialogTitle }}
      </v-card-title>
      
      <v-divider></v-divider>
      
      <v-card-text class="pa-6">
        <v-form ref="formRef">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="formData.name"
                label="智能体名称"
                :rules="requiredRule"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-select
                v-model="formData.directionId"
                label="医疗方向"
                :items="directions"
                item-title="name"
                item-value="id"
                :rules="requiredRule"
                :loading="loadingDirections"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-select>
            </v-col>
            
            <v-col cols="12">
              <v-divider class="my-3"></v-divider>
              <p class="text-subtitle-1 font-weight-medium mb-2">模型配置</p>
            </v-col>
            
            <v-col cols="12" md="4">
              <v-text-field
                v-model="formData.modelName"
                label="模型名称"
                :rules="requiredRule"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12" md="4">
              <v-text-field
                v-model="formData.modelUrl"
                label="模型URL"
                :rules="requiredRule"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12" md="4">
              <v-text-field
                v-model="formData.apiKey"
                label="API密钥"
                :rules="requiredRule"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
                type="password"
              ></v-text-field>
            </v-col>
            
            <v-col cols="12">
              <v-divider class="my-3"></v-divider>
              <p class="text-subtitle-1 font-weight-medium mb-2">模板配置</p>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-select
                v-model="formData.templateId"
                label="模板ID"
                :items="templates"
                :rules="requiredRule"
                :loading="loadingTemplates"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
                @update:model-value="handleTemplateChange"
              ></v-select>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-text-field
                v-model="formData.templateDescription"
                label="模板描述"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
                disabled
              ></v-text-field>
            </v-col>
            
            <v-col cols="12">
              <v-divider class="my-3"></v-divider>
              <p class="text-subtitle-1 font-weight-medium mb-2">知识库配置</p>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-select
                v-model="formData.vectorNamespaces"
                label="向量命名空间"
                :items="vectorNamespaces"
                :loading="loadingVectorNamespaces"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
                multiple
                chips
                closable-chips
              ></v-select>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-select
                v-model="formData.preciseDbUids"
                label="精确查找大类"
                :items="preciseCategories"
                item-title="name"
                item-value="uid"
                :loading="loadingPreciseCategories"
                variant="outlined"
                density="comfortable"
                hide-details="auto"
                multiple
                chips
                closable-chips
                return-object
              >
                <template v-slot:selection="{ item }">
                  <v-chip
                    closable
                    @click:close="
                      formData.preciseDbUids = formData.preciseDbUids.filter(
                        category => category.uid !== item.raw.uid
                      )
                    "
                  >
                    {{ item.raw.name }}
                  </v-chip>
                </template>
              </v-select>
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
          @click="saveAgent"
          :loading="loading"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.agent-edit-dialog {
  border-radius: 12px;
}
</style> 