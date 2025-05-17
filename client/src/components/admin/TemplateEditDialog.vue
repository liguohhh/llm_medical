<script setup>
import { ref, computed, watch } from 'vue'
import { templateAdminApi } from '@/services/templateAdminApi'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  templateId: {
    type: String,
    default: null
  },
  templateData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:model-value', 'saved'])

// 表单数据
const form = ref({
  templateId: '',
  description: '',
  subTemplates: {}
})

// 编辑模式
const isEditMode = computed(() => !!props.templateId)

// 对话框标题
const dialogTitle = computed(() => isEditMode.value ? '编辑模板' : '创建模板')

// 子模板编辑状态
const editingSubTemplate = ref(null)
const subTemplateForm = ref({
  name: '',
  description: '',
  template: '',
  parameters: [],
  order: 0
})
const newParameter = ref('')
const subTemplateDialogTitle = computed(() => 
  editingSubTemplate.value ? '编辑子模板' : '添加子模板'
)

// 错误和加载状态
const error = ref('')
const loading = ref(false)
const subTemplateDialog = ref(false)

// 监听props变化，初始化表单
watch(() => props.modelValue, (val) => {
  if (val) {
    initForm()
  }
})

// 获取已排序的子模板列表
const getSortedSubTemplates = () => {
  // 转换为数组并按order字段排序
  const sortedItems = Object.entries(form.value.subTemplates).map(([name, subTemplate]) => ({
    name,
    ...subTemplate
  })).sort((a, b) => (a.order || 999) - (b.order || 999));
  
  return sortedItems;
}

// 初始化表单数据
const initForm = () => {
  if (isEditMode.value && props.templateData) {
    // 编辑现有模板
    const subTemplates = JSON.parse(JSON.stringify(props.templateData.sub_templates || {}));
    
    // 确保每个子模板都有order字段
    let orderCounter = 1;
    Object.values(subTemplates).forEach(subTemplate => {
      if (!subTemplate.order) {
        subTemplate.order = orderCounter++;
      }
    });
    
    form.value = {
      templateId: props.templateId,
      description: props.templateData.description || '',
      subTemplates: subTemplates
    }
  } else {
    // 创建新模板
    form.value = {
      templateId: '',
      description: '',
      subTemplates: {}
    }
  }
  error.value = ''
}

// 关闭对话框
const closeDialog = () => {
  emit('update:model-value', false)
}

// 保存模板
const saveTemplate = async () => {
  if (!validateForm()) return
  
  loading.value = true
  error.value = ''
  
  try {
    // 确保每个子模板都有正确的order值
    const orderedSubTemplates = {};
    getSortedSubTemplates().forEach((item, index) => {
      const { name, ...subTemplate } = item;
      subTemplate.order = index + 1; // 重置order从1开始
      orderedSubTemplates[name] = subTemplate;
    });
    
    const templateData = {
      templateId: form.value.templateId,
      description: form.value.description,
      subTemplates: orderedSubTemplates
    }
    
    const response = await templateAdminApi.saveTemplate(templateData)
    if (response.code === 200) {
      emit('saved')
      closeDialog()
    } else {
      error.value = response.message || '保存模板失败'
    }
  } catch (err) {
    console.error('保存模板错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 处理拖拽排序
const handleDragStart = (event, item) => {
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', JSON.stringify(item))
  event.target.classList.add('dragging')
}

const handleDragOver = (event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'move'
}

const handleDragEnter = (event) => {
  event.target.closest('tr')?.classList.add('drag-over')
}

const handleDragLeave = (event) => {
  event.target.closest('tr')?.classList.remove('drag-over')
}

const handleDrop = (event, targetItem) => {
  event.preventDefault()
  const sourceItemData = event.dataTransfer.getData('text/plain')
  try {
    const sourceItem = JSON.parse(sourceItemData)
    
    // 如果拖放到自己上面，不做任何操作
    if (sourceItem.name === targetItem.name) return
    
    // 获取排序值
    const sourceOrder = sourceItem.order || 999
    const targetOrder = targetItem.order || 999
    
    // 更新排序
    const subTemplates = form.value.subTemplates
    
    // 遍历所有子模板，调整order值
    Object.entries(subTemplates).forEach(([name, template]) => {
      if (name === sourceItem.name) {
        // 移动的元素
        template.order = targetOrder
      } else if (sourceOrder < targetOrder) {
        // 当源在目标前面时，将所有在源和目标之间的元素order减1
        if (template.order > sourceOrder && template.order <= targetOrder) {
          template.order -= 1
        }
      } else if (sourceOrder > targetOrder) {
        // 当源在目标后面时，将所有在目标和源之间的元素order加1
        if (template.order >= targetOrder && template.order < sourceOrder) {
          template.order += 1
        }
      }
    })
    
    // 触发响应式更新
    form.value.subTemplates = { ...form.value.subTemplates }
  } catch (e) {
    console.error('拖拽处理错误:', e)
  }
  
  event.target.closest('tr')?.classList.remove('drag-over')
}

const handleDragEnd = (event) => {
  event.target.classList.remove('dragging')
}

// 验证表单
const validateForm = () => {
  if (!form.value.templateId) {
    error.value = '请输入模板ID'
    return false
  }
  
  if (!form.value.description) {
    error.value = '请输入模板描述'
    return false
  }
  
  if (Object.keys(form.value.subTemplates).length === 0) {
    error.value = '请至少添加一个子模板'
    return false
  }
  
  return true
}

// 打开子模板编辑对话框
const openSubTemplateDialog = (item = null) => {
  if (item) {
    // 编辑现有子模板
    editingSubTemplate.value = item.name
    subTemplateForm.value = {
      name: item.name,
      description: item.description || '',
      template: item.template || '',
      parameters: [...(item.parameters || [])],
      order: item.order || 0
    }
  } else {
    // 创建新子模板
    editingSubTemplate.value = null
    
    // 为新子模板创建一个新的order值 (最大值+1)
    let maxOrder = 0
    Object.values(form.value.subTemplates).forEach(template => {
      if (template.order && template.order > maxOrder) {
        maxOrder = template.order
      }
    })
    
    subTemplateForm.value = {
      name: '',
      description: '',
      template: '',
      parameters: [],
      order: maxOrder + 1
    }
  }
  
  newParameter.value = ''
  subTemplateDialog.value = true
}

// 保存子模板
const saveSubTemplate = () => {
  if (!validateSubTemplateForm()) return
  
  const name = subTemplateForm.value.name
  const oldName = editingSubTemplate.value
  
  // 如果是重命名子模板，需要删除旧的
  if (oldName && oldName !== name) {
    const oldOrder = form.value.subTemplates[oldName].order
    delete form.value.subTemplates[oldName]
    
    // 保存子模板并保留原来的排序
    form.value.subTemplates[name] = {
      description: subTemplateForm.value.description,
      template: subTemplateForm.value.template,
      parameters: [...subTemplateForm.value.parameters],
      order: oldOrder
    }
  } else if (!oldName) {
    // 如果是新增子模板，使用新的order值
    form.value.subTemplates[name] = {
      description: subTemplateForm.value.description,
      template: subTemplateForm.value.template,
      parameters: [...subTemplateForm.value.parameters],
      order: subTemplateForm.value.order
    }
  } else {
    // 如果是编辑现有子模板（无改名）
    form.value.subTemplates[name] = {
      description: subTemplateForm.value.description,
      template: subTemplateForm.value.template,
      parameters: [...subTemplateForm.value.parameters],
      order: form.value.subTemplates[name].order // 保留原来的排序
    }
  }
  
  subTemplateDialog.value = false
}

// 验证子模板表单
const validateSubTemplateForm = () => {
  if (!subTemplateForm.value.name) {
    error.value = '请输入子模板名称'
    return false
  }
  
  if (!subTemplateForm.value.description) {
    error.value = '请输入子模板描述'
    return false
  }
  
  if (!subTemplateForm.value.template) {
    error.value = '请输入子模板内容'
    return false
  }
  
  return true
}

// 删除子模板
const deleteSubTemplate = (item) => {
  const name = item.name;
  const deletedOrder = item.order;
  
  if (form.value.subTemplates[name]) {
    delete form.value.subTemplates[name]
    
    // 更新其他子模板的排序
    Object.values(form.value.subTemplates).forEach(template => {
      if (template.order > deletedOrder) {
        template.order -= 1
      }
    })
    
    form.value = { ...form.value } // 触发响应式更新
  }
}

// 添加参数
const addParameter = () => {
  if (newParameter.value && !subTemplateForm.value.parameters.includes(newParameter.value)) {
    subTemplateForm.value.parameters.push(newParameter.value)
    newParameter.value = ''
  }
}

// 删除参数
const removeParameter = (param) => {
  const index = subTemplateForm.value.parameters.indexOf(param)
  if (index !== -1) {
    subTemplateForm.value.parameters.splice(index, 1)
  }
}

// 从模板内容中提取参数
const extractParameters = () => {
  const template = subTemplateForm.value.template
  const paramRegex = /\{([^{}]+)\}/g
  const matches = [...template.matchAll(paramRegex)]
  const extractedParams = matches.map(match => match[1])
  
  // 添加提取的参数（去重）
  extractedParams.forEach(param => {
    if (!subTemplateForm.value.parameters.includes(param)) {
      subTemplateForm.value.parameters.push(param)
    }
  })
}
</script>

<template>
  <v-dialog
    v-model="props.modelValue"
    @update:model-value="emit('update:model-value', $event)"
    max-width="900"
    persistent
  >
    <v-card class="template-edit-dialog">
      <v-card-title class="d-flex align-center pa-4">
        <span class="text-h6">{{ dialogTitle }}</span>
        <v-spacer></v-spacer>
        <v-btn
          icon="mdi-close"
          variant="text"
          @click="closeDialog"
        ></v-btn>
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
        
        <v-form @submit.prevent="saveTemplate">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.templateId"
                label="模板ID"
                :disabled="isEditMode"
                :hint="isEditMode ? '编辑模式下不可修改模板ID' : '请使用有意义的ID，如medical_template'"
                persistent-hint
                required
              ></v-text-field>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.description"
                label="模板描述"
                hint="简要描述模板的用途"
                persistent-hint
                required
              ></v-text-field>
            </v-col>
          </v-row>
          
          <div class="d-flex align-center mb-2">
            <h3 class="text-subtitle-1 font-weight-bold">子模板列表</h3>
            <v-spacer></v-spacer>
            <v-btn
              color="primary"
              size="small"
              prepend-icon="mdi-plus"
              @click="openSubTemplateDialog()"
            >
              添加子模板
            </v-btn>
          </div>
          
          <v-table v-if="Object.keys(form.subTemplates).length > 0" class="mb-4">
            <thead>
              <tr>
                <th width="40" class="text-center">#</th>
                <th>名称</th>
                <th>描述</th>
                <th>参数数量</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="item in getSortedSubTemplates()" 
                :key="item.name"
                draggable="true"
                @dragstart="handleDragStart($event, item)"
                @dragover="handleDragOver"
                @dragenter="handleDragEnter"
                @dragleave="handleDragLeave"
                @drop="handleDrop($event, item)"
                @dragend="handleDragEnd"
                class="sub-template-row"
              >
                <td class="text-center">
                  <v-icon icon="mdi-drag" size="small" class="drag-handle"></v-icon>
                </td>
                <td>{{ item.name }}</td>
                <td>{{ item.description }}</td>
                <td>{{ item.parameters ? item.parameters.length : 0 }}</td>
                <td>
                  <v-btn
                    icon="mdi-pencil"
                    variant="text"
                    density="comfortable"
                    size="small"
                    color="primary"
                    @click="openSubTemplateDialog(item)"
                    class="me-1"
                  ></v-btn>
                  
                  <v-btn
                    icon="mdi-delete"
                    variant="text"
                    density="comfortable"
                    size="small"
                    color="error"
                    @click="deleteSubTemplate(item)"
                  ></v-btn>
                </td>
              </tr>
            </tbody>
          </v-table>
          
          <div v-else class="text-center pa-4 bg-grey-lighten-4 rounded mb-4">
            <p class="text-medium-emphasis mb-0">暂无子模板，请点击"添加子模板"按钮创建</p>
          </div>
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
          @click="saveTemplate"
          :loading="loading"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  
  <!-- 子模板编辑对话框 -->
  <v-dialog
    v-model="subTemplateDialog"
    max-width="800"
    persistent
  >
    <v-card>
      <v-card-title class="d-flex align-center pa-4">
        <span class="text-h6">{{ subTemplateDialogTitle }}</span>
        <v-spacer></v-spacer>
        <v-btn
          icon="mdi-close"
          variant="text"
          @click="subTemplateDialog = false"
        ></v-btn>
      </v-card-title>
      
      <v-divider></v-divider>
      
      <v-card-text class="pa-4">
        <v-form @submit.prevent="saveSubTemplate">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="subTemplateForm.name"
                label="子模板名称"
                hint="如：基础信息、参考内容等"
                persistent-hint
                required
              ></v-text-field>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-text-field
                v-model="subTemplateForm.description"
                label="子模板描述"
                hint="简要描述子模板的用途"
                persistent-hint
                required
              ></v-text-field>
            </v-col>
          </v-row>
          
          <v-textarea
            v-model="subTemplateForm.template"
            label="模板内容"
            hint="使用{parameter}格式表示参数"
            persistent-hint
            rows="8"
            auto-grow
            class="mb-2"
            required
          ></v-textarea>
          
          <div class="d-flex align-center mb-2">
            <v-btn
              size="small"
              variant="text"
              color="primary"
              @click="extractParameters"
              prepend-icon="mdi-refresh"
            >
              从内容提取参数
            </v-btn>
          </div>
          
          <div class="d-flex align-center mb-2">
            <h3 class="text-subtitle-1 font-weight-bold">参数列表</h3>
          </div>
          
          <div class="d-flex mb-4">
            <v-text-field
              v-model="newParameter"
              label="参数名称"
              hide-details
              density="compact"
              class="me-2"
              @keyup.enter="addParameter"
            ></v-text-field>
            
            <v-btn
              color="primary"
              @click="addParameter"
            >
              添加
            </v-btn>
          </div>
          
          <div v-if="subTemplateForm.parameters.length > 0" class="parameters-container mb-4">
            <v-chip
              v-for="param in subTemplateForm.parameters"
              :key="param"
              class="ma-1"
              closable
              @click:close="removeParameter(param)"
            >
              {{ param }}
            </v-chip>
          </div>
          
          <div v-else class="text-center pa-4 bg-grey-lighten-4 rounded mb-4">
            <p class="text-medium-emphasis mb-0">暂无参数，可以手动添加或从模板内容提取</p>
          </div>
        </v-form>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions class="pa-4">
        <v-spacer></v-spacer>
        <v-btn
          variant="text"
          @click="subTemplateDialog = false"
        >
          取消
        </v-btn>
        <v-btn
          color="primary"
          @click="saveSubTemplate"
        >
          保存
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.template-edit-dialog {
  overflow: hidden;
}

.sub-template-row {
  cursor: grab;
  transition: background-color 0.2s;
}

.sub-template-row:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

.sub-template-row.dragging {
  opacity: 0.5;
  background-color: rgba(0, 0, 0, 0.05);
}

.sub-template-row.drag-over {
  background-color: rgba(25, 118, 210, 0.1);
  border-top: 2px dashed #1976D2;
}

.drag-handle {
  cursor: grab;
  opacity: 0.5;
}

.sub-template-row:hover .drag-handle {
  opacity: 1;
}

.parameters-container {
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 4px;
  padding: 8px;
  min-height: 60px;
}
</style> 