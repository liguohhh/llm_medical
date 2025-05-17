<script setup>
import { ref, computed, onMounted } from 'vue'
import { agentAdminApi } from '@/services/agentAdminApi'
import AgentEditDialog from '@/components/admin/AgentEditDialog.vue'
import DeleteConfirmDialog from '@/components/admin/DeleteConfirmDialog.vue'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'

// 状态变量
const agents = ref([])
const loading = ref(false)
const error = ref('')
const search = ref('')
const directions = ref([])
const filterDirectionId = ref(null)

// 对话框状态
const editDialog = ref(false)
const selectedAgent = ref(null)
const deleteDialog = ref(false)
const deleteAgentId = ref(null)
const deleteAgentName = ref('')
const deleteLoading = ref(false)

// 表格头部
const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: '名称', key: 'name', sortable: true },
  { title: '医疗方向', key: 'direction.name', sortable: true },
  { title: '模型', key: 'modelName', sortable: true },
  { title: '模板', key: 'templateId', sortable: true },
  { title: '创建时间', key: 'createTime', sortable: true },
  { title: '操作', key: 'actions', sortable: false }
]

// 组件挂载时加载数据
onMounted(async () => {
  await loadDirections()
  loadAgents()
})

// 加载医疗方向数据
const loadDirections = async () => {
  try {
    const response = await agentAdminApi.getDirectionList()
    if (response.code === 200) {
      directions.value = response.data || []
    } else {
      console.error('加载医疗方向失败:', response.message)
    }
  } catch (err) {
    console.error('加载医疗方向错误:', err)
  }
}

// 加载智能体数据
const loadAgents = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await agentAdminApi.getAgentList(filterDirectionId.value)
    if (response.code === 200) {
      agents.value = response.data || []
    } else {
      error.value = response.message || '加载智能体数据失败'
    }
  } catch (err) {
    console.error('加载智能体数据错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEditDialog = (agent = null) => {
  selectedAgent.value = agent
  editDialog.value = true
}

// 打开删除确认对话框
const openDeleteDialog = (agentId, agentName) => {
  deleteAgentId.value = agentId
  deleteAgentName.value = agentName
  deleteDialog.value = true
}

// 删除智能体
const deleteAgent = async () => {
  deleteLoading.value = true
  
  try {
    const response = await agentAdminApi.deleteAgent(deleteAgentId.value)
    if (response.code === 200) {
      // 重新加载数据
      loadAgents()
      // 关闭对话框
      deleteDialog.value = false
    } else {
      error.value = response.message || '删除智能体失败'
    }
  } catch (err) {
    console.error('删除智能体错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19).replace(/-/g, '/')
}

// 智能体保存回调
const handleAgentSaved = () => {
  loadAgents()
}

// 筛选智能体数据（本地搜索）
const filteredAgents = computed(() => {
  if (!search.value) return agents.value
  
  const searchLower = search.value.toLowerCase()
  return agents.value.filter(agent => 
    agent.name?.toLowerCase().includes(searchLower) ||
    agent.modelName?.toLowerCase().includes(searchLower) ||
    agent.templateId?.toLowerCase().includes(searchLower) ||
    agent.direction?.name?.toLowerCase().includes(searchLower)
  )
})

// 过滤方向变化时重新加载数据
const handleDirectionFilterChange = () => {
  // 确保当filterDirectionId为null时正确处理
  loadAgents()
}
</script>

<template>
  <div class="agent-table">
    <div class="d-flex justify-space-between align-center mb-4">
      <h2 class="text-h5 font-weight-bold">智能体管理</h2>
      
      <div class="d-flex">
        <v-select
          v-model="filterDirectionId"
          label="按医疗方向筛选"
          :items="directions"
          item-title="name"
          item-value="id"
          clearable
          variant="outlined"
          density="compact"
          hide-details
          class="me-4"
          style="width: 200px"
          @update:model-value="handleDirectionFilterChange"
        >
          <template v-slot:prepend-inner>
            <v-icon size="small">mdi-filter-variant</v-icon>
          </template>
        </v-select>
        
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="搜索智能体"
          single-line
          hide-details
          density="compact"
          variant="outlined"
          class="me-4"
          style="width: 240px"
        ></v-text-field>
        
        <v-btn
          color="primary"
          prepend-icon="mdi-robot-outline"
          @click="openEditDialog()"
        >
          添加智能体
        </v-btn>
      </div>
    </div>
    
    <v-card class="agent-table-card">
      <div class="position-relative">
        <loading-indicator :loading="loading" />
        
        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          density="compact"
        >
          {{ error }}
        </v-alert>
        
        <v-data-table
          :headers="headers"
          :items="filteredAgents"
          :loading="loading"
          class="agent-data-table"
        >
          <!-- 医疗方向列 -->
          <template v-slot:item.direction.name="{ item }">
            <v-chip
              :color="item.direction ? 'success' : 'grey'"
              size="small"
              variant="tonal"
              class="font-weight-medium"
            >
              {{ item.direction?.name || '未分配' }}
            </v-chip>
          </template>
          
          <!-- 模型列 -->
          <template v-slot:item.modelName="{ item }">
            <div class="d-flex align-center">
              <v-icon 
                :icon="item.modelName.includes('gpt') ? 'mdi-chat-processing' : 'mdi-brain'"
                size="small"
                class="me-1"
                :color="item.modelName.includes('gpt') ? 'success' : 'primary'"
              ></v-icon>
              {{ item.modelName }}
            </div>
          </template>
          
          <!-- 创建时间列 -->
          <template v-slot:item.createTime="{ item }">
            {{ formatDate(item.createTime) }}
          </template>
          
          <!-- 操作列 -->
          <template v-slot:item.actions="{ item }">
            <div class="d-flex">
              <v-icon 
                icon="mdi-pencil" 
                size="small" 
                color="primary" 
                class="me-2 action-icon"
                @click="openEditDialog(item)"
                title="编辑"
              ></v-icon>
              
              <v-icon 
                icon="mdi-delete" 
                size="small" 
                color="error" 
                class="action-icon"
                @click="openDeleteDialog(item.id, item.name)"
                title="删除"
              ></v-icon>
            </div>
          </template>
          
          <!-- 空状态 -->
          <template v-slot:no-data>
            <div class="text-center pa-4">
              <p class="text-medium-emphasis">暂无智能体数据</p>
              <v-btn 
                color="primary" 
                variant="text" 
                @click="loadAgents" 
                class="mt-2"
              >
                刷新
              </v-btn>
            </div>
          </template>
        </v-data-table>
      </div>
    </v-card>
    
    <!-- 编辑对话框 -->
    <agent-edit-dialog
      :model-value="editDialog"
      @update:model-value="editDialog = $event"
      :agent="selectedAgent"
      @saved="handleAgentSaved"
    />
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      :model-value="deleteDialog"
      @update:model-value="deleteDialog = $event"
      title="确认删除智能体"
      :message="`确定要删除智能体『${deleteAgentName}』吗？此操作无法撤销。`"
      :loading="deleteLoading"
      @confirm="deleteAgent"
    />
  </div>
</template>

<style scoped>
.agent-table-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.agent-data-table {
  width: 100%;
}

.position-relative {
  position: relative;
}

.action-icon {
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.2s ease;
}

.action-icon:hover {
  opacity: 1;
  transform: scale(1.2);
}
</style>