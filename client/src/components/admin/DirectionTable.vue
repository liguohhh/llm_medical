<script setup>
import { ref, computed, onMounted } from 'vue'
import { directionAdminApi } from '@/services/directionAdminApi'
import DirectionEditDialog from '@/components/admin/DirectionEditDialog.vue'
import DeleteConfirmDialog from '@/components/admin/DeleteConfirmDialog.vue'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'

// 状态变量
const directions = ref([])
const loading = ref(false)
const error = ref('')
const search = ref('')

// 对话框状态
const editDialog = ref(false)
const selectedDirection = ref(null)
const deleteDialog = ref(false)
const deleteDirectionId = ref(null)
const deleteDirectionName = ref('')
const deleteLoading = ref(false)

// 表格头部
const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'UID', key: 'uid', sortable: true },
  { title: '名称', key: 'name', sortable: true },
  { title: '描述', key: 'description', sortable: false },
  { title: '创建时间', key: 'createTime', sortable: true },
  { title: '操作', key: 'actions', sortable: false }
]

// 组件挂载时加载数据
onMounted(() => {
  loadDirections()
})

// 加载医疗方向数据
const loadDirections = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await directionAdminApi.getDirectionList()
    if (response.code === 200) {
      directions.value = response.data || []
    } else {
      error.value = response.message || '加载医疗方向数据失败'
    }
  } catch (err) {
    console.error('加载医疗方向数据错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEditDialog = (direction = null) => {
  selectedDirection.value = direction
  editDialog.value = true
}

// 打开删除确认对话框
const openDeleteDialog = (directionId, directionName) => {
  deleteDirectionId.value = directionId
  deleteDirectionName.value = directionName
  deleteDialog.value = true
}

// 删除医疗方向
const deleteDirection = async () => {
  deleteLoading.value = true
  
  try {
    const response = await directionAdminApi.deleteDirection(deleteDirectionId.value)
    if (response.code === 200) {
      // 重新加载数据
      loadDirections()
      // 关闭对话框
      deleteDialog.value = false
    } else {
      error.value = response.message || '删除医疗方向失败'
    }
  } catch (err) {
    console.error('删除医疗方向错误:', err)
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

// 医疗方向保存回调
const handleDirectionSaved = () => {
  loadDirections()
}

// 筛选医疗方向数据（本地搜索）
const filteredDirections = computed(() => {
  if (!search.value) return directions.value
  
  const searchLower = search.value.toLowerCase()
  return directions.value.filter(direction => 
    direction.name?.toLowerCase().includes(searchLower) ||
    direction.description?.toLowerCase().includes(searchLower) ||
    direction.uid?.toLowerCase().includes(searchLower)
  )
})
</script>

<template>
  <div class="direction-table">
    <div class="d-flex justify-space-between align-center mb-4">
      <h2 class="text-h5 font-weight-bold">医疗方向管理</h2>
      
      <div class="d-flex">
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="搜索医疗方向"
          single-line
          hide-details
          density="compact"
          variant="outlined"
          class="me-4"
          style="width: 240px"
        ></v-text-field>
        
        <v-btn
          color="primary"
          prepend-icon="mdi-hospital-box-outline"
          @click="openEditDialog()"
        >
          添加医疗方向
        </v-btn>
      </div>
    </div>
    
    <v-card class="direction-table-card">
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
          :items="filteredDirections"
          :loading="loading"
          class="direction-data-table"
        >
          <!-- 描述列 -->
          <template v-slot:item.description="{ item }">
            <div class="description-cell">{{ item.description }}</div>
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
              <p class="text-medium-emphasis">暂无医疗方向数据</p>
              <v-btn 
                color="primary" 
                variant="text" 
                @click="loadDirections" 
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
    <direction-edit-dialog
      :model-value="editDialog"
      @update:model-value="editDialog = $event"
      :direction="selectedDirection"
      @saved="handleDirectionSaved"
    />
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      :model-value="deleteDialog"
      @update:model-value="deleteDialog = $event"
      title="确认删除医疗方向"
      :message="`确定要删除医疗方向『${deleteDirectionName}』吗？此操作无法撤销。`"
      :loading="deleteLoading"
      @confirm="deleteDirection"
    />
  </div>
</template>

<style scoped>
.direction-table-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.direction-data-table {
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

.description-cell {
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style> 