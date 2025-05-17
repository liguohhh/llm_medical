<script setup>
import { ref, computed, onMounted } from 'vue'
import { templateAdminApi } from '@/services/templateAdminApi'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'
import DeleteConfirmDialog from '@/components/admin/DeleteConfirmDialog.vue'

// 状态变量
const templates = ref([])
const selectedTemplateId = ref(null)
const templateDetail = ref(null)
const loading = ref(false)
const detailLoading = ref(false)
const error = ref('')
const search = ref('')

// 删除对话框
const deleteDialog = ref(false)
const deleteTemplateId = ref(null)
const deleteLoading = ref(false)

// 编辑事件
const emit = defineEmits(['edit'])

// 组件挂载时加载数据
onMounted(() => {
  loadTemplates()
})

// 加载模板列表
const loadTemplates = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await templateAdminApi.getTemplateIds()
    if (response.code === 200) {
      templates.value = response.data || []
      
      // 如果有模板，默认选择第一个
      if (templates.value.length > 0 && !selectedTemplateId.value) {
        selectTemplate(templates.value[0])
      }
    } else {
      error.value = response.message || '加载模板列表失败'
    }
  } catch (err) {
    console.error('加载模板列表错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 选择模板
const selectTemplate = async (templateId) => {
  selectedTemplateId.value = templateId
  await loadTemplateDetail(templateId)
}

// 加载模板详情
const loadTemplateDetail = async (templateId) => {
  detailLoading.value = true
  error.value = ''
  templateDetail.value = null
  
  try {
    const response = await templateAdminApi.getTemplateDetail(templateId)
    if (response.code === 200) {
      templateDetail.value = response.data
    } else {
      error.value = response.message || '加载模板详情失败'
    }
  } catch (err) {
    console.error('加载模板详情错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    detailLoading.value = false
  }
}

// 编辑模板
const editTemplate = () => {
  if (selectedTemplateId.value) {
    emit('edit', selectedTemplateId.value, templateDetail.value)
  }
}

// 创建新模板
const createTemplate = () => {
  emit('edit', null, null)
}

// 打开删除确认对话框
const openDeleteDialog = (templateId) => {
  deleteTemplateId.value = templateId
  deleteDialog.value = true
}

// 删除模板
const deleteTemplate = async () => {
  deleteLoading.value = true
  
  try {
    const response = await templateAdminApi.deleteTemplate(deleteTemplateId.value)
    if (response.code === 200) {
      // 重新加载数据
      loadTemplates()
      // 如果删除的是当前选中的模板，清除选中
      if (deleteTemplateId.value === selectedTemplateId.value) {
        selectedTemplateId.value = null
        templateDetail.value = null
      }
      // 关闭对话框
      deleteDialog.value = false
    } else {
      error.value = response.message || '删除模板失败'
    }
  } catch (err) {
    console.error('删除模板错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
  }
}

// 筛选模板数据
const filteredTemplates = computed(() => {
  if (!search.value) return templates.value
  
  const searchLower = search.value.toLowerCase()
  return templates.value.filter(templateId => 
    templateId.toLowerCase().includes(searchLower)
  )
})

// 获取子模板数量
const getSubTemplateCount = computed(() => {
  if (!templateDetail.value || !templateDetail.value.sub_templates) {
    return 0
  }
  return Object.keys(templateDetail.value.sub_templates).length
})

// 获取参数数量
const getParametersCount = computed(() => {
  if (!templateDetail.value || !templateDetail.value.sub_templates) {
    return 0
  }
  
  let count = 0
  Object.values(templateDetail.value.sub_templates).forEach(subTemplate => {
    if (subTemplate.parameters) {
      count += subTemplate.parameters.length
    }
  })
  return count
})
</script>

<template>
  <div class="template-list">
    <v-row>
      <v-col cols="12" md="4">
        <v-card class="template-list-card h-100">
          <v-card-title class="d-flex justify-space-between align-center pa-4">
            <div class="text-h6">模板列表</div>
            <v-btn
              color="primary"
              variant="text"
              icon="mdi-plus"
              @click="createTemplate"
              title="创建新模板"
            ></v-btn>
          </v-card-title>
          
          <v-divider></v-divider>
          
          <v-card-text class="pa-0">
            <div class="px-4 pt-4">
              <v-text-field
                v-model="search"
                prepend-inner-icon="mdi-magnify"
                label="搜索模板"
                single-line
                hide-details
                density="compact"
                variant="outlined"
                bg-color="grey-lighten-5"
              ></v-text-field>
            </div>
            
            <div class="position-relative">
              <loading-indicator :loading="loading" />
              
              <v-alert
                v-if="error"
                type="error"
                variant="tonal"
                class="ma-4"
                density="compact"
              >
                {{ error }}
              </v-alert>
              
              <v-list class="template-list-items">
                <v-list-item
                  v-for="templateId in filteredTemplates"
                  :key="templateId"
                  :active="selectedTemplateId === templateId"
                  @click="selectTemplate(templateId)"
                  class="template-list-item"
                >
                  <template v-slot:prepend>
                    <v-icon icon="mdi-file-document-outline"></v-icon>
                  </template>
                  
                  <v-list-item-title>{{ templateId }}</v-list-item-title>
                  
                  <template v-slot:append>
                    <div class="d-flex">
                      <v-btn
                        icon="mdi-pencil"
                        variant="text"
                        density="comfortable"
                        size="small"
                        color="primary"
                        @click.stop="editTemplate"
                        title="编辑"
                      ></v-btn>
                      
                      <v-btn
                        icon="mdi-delete"
                        variant="text"
                        density="comfortable"
                        size="small"
                        color="error"
                        @click.stop="openDeleteDialog(templateId)"
                        title="删除"
                      ></v-btn>
                    </div>
                  </template>
                </v-list-item>
                
                <div v-if="filteredTemplates.length === 0 && !loading" class="pa-4 text-center text-medium-emphasis">
                  {{ templates.length === 0 ? '暂无模板数据' : '没有匹配的模板' }}
                </div>
              </v-list>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" md="8">
        <v-card class="template-detail-card h-100">
          <v-card-title class="d-flex justify-space-between align-center pa-4">
            <div class="text-h6">模板详情</div>
            <v-btn
              v-if="selectedTemplateId"
              color="primary"
              prepend-icon="mdi-pencil"
              @click="editTemplate"
            >
              编辑模板
            </v-btn>
          </v-card-title>
          
          <v-divider></v-divider>
          
          <v-card-text class="pa-4">
            <div class="position-relative">
              <loading-indicator :loading="detailLoading" />
              
              <div v-if="!selectedTemplateId && !detailLoading" class="text-center py-8 text-medium-emphasis">
                请从左侧选择一个模板或创建新模板
              </div>
              
              <div v-else-if="templateDetail && !detailLoading" class="template-detail">
                <v-row>
                  <v-col cols="12" md="6">
                    <v-card variant="outlined" class="mb-4">
                      <v-card-title class="text-subtitle-1">基本信息</v-card-title>
                      <v-card-text>
                        <div class="d-flex mb-2">
                          <div class="font-weight-medium me-2">模板ID:</div>
                          <div>{{ selectedTemplateId }}</div>
                        </div>
                        <div class="d-flex mb-2">
                          <div class="font-weight-medium me-2">描述:</div>
                          <div>{{ templateDetail.description }}</div>
                        </div>
                        <div class="d-flex mb-2">
                          <div class="font-weight-medium me-2">子模板数量:</div>
                          <div>{{ getSubTemplateCount }}</div>
                        </div>
                        <div class="d-flex">
                          <div class="font-weight-medium me-2">参数数量:</div>
                          <div>{{ getParametersCount }}</div>
                        </div>
                      </v-card-text>
                    </v-card>
                  </v-col>
                  
                  <v-col cols="12">
                    <v-expansion-panels variant="accordion">
                      <v-expansion-panel
                        v-for="(subTemplate, name) in templateDetail.sub_templates"
                        :key="name"
                        class="mb-2"
                      >
                        <v-expansion-panel-title>
                          <div class="d-flex align-center">
                            <v-icon icon="mdi-file-outline" class="me-2"></v-icon>
                            {{ name }}
                            <v-chip
                              v-if="subTemplate.parameters && subTemplate.parameters.length > 0"
                              size="small"
                              color="primary"
                              class="ms-2"
                            >
                              {{ subTemplate.parameters.length }} 参数
                            </v-chip>
                          </div>
                        </v-expansion-panel-title>
                        
                        <v-expansion-panel-text>
                          <div class="mb-3">
                            <div class="font-weight-medium mb-1">描述:</div>
                            <div>{{ subTemplate.description }}</div>
                          </div>
                          
                          <div class="mb-3" v-if="subTemplate.parameters && subTemplate.parameters.length > 0">
                            <div class="font-weight-medium mb-1">参数:</div>
                            <v-chip
                              v-for="param in subTemplate.parameters"
                              :key="param"
                              size="small"
                              class="me-1 mb-1"
                              color="info"
                              variant="outlined"
                            >
                              {{ param }}
                            </v-chip>
                          </div>
                          
                          <div>
                            <div class="font-weight-medium mb-1">模板内容:</div>
                            <v-card variant="outlined" class="template-content">
                              <pre class="pa-3">{{ subTemplate.template }}</pre>
                            </v-card>
                          </div>
                        </v-expansion-panel-text>
                      </v-expansion-panel>
                    </v-expansion-panels>
                  </v-col>
                </v-row>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      :model-value="deleteDialog"
      @update:model-value="deleteDialog = $event"
      title="确认删除模板"
      :message="`确定要删除模板『${deleteTemplateId}』吗？此操作无法撤销。`"
      :loading="deleteLoading"
      @confirm="deleteTemplate"
    />
  </div>
</template>

<style scoped>
.template-list-card, .template-detail-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.template-list-items {
  max-height: 500px;
  overflow-y: auto;
}

.template-list-item {
  border-radius: 4px;
  margin: 4px;
}

.template-content {
  max-height: 300px;
  overflow-y: auto;
  background-color: #f5f5f5;
}

.template-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.9rem;
  margin: 0;
}

.position-relative {
  position: relative;
}

.h-100 {
  height: 100%;
}
</style> 