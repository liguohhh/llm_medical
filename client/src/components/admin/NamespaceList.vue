<script setup>
import { ref, computed, onMounted } from 'vue'
import { vectorAdminApi } from '@/services/vectorAdminApi'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'
import DeleteConfirmDialog from '@/components/admin/DeleteConfirmDialog.vue'

// 状态变量
const namespaces = ref([])
const selectedNamespace = ref(null)
const loading = ref(false)
const error = ref('')
const search = ref('')

// 删除对话框
const deleteDialog = ref(false)
const deleteNamespaceValue = ref(null)
const deleteLoading = ref(false)

// 创建命名空间对话框
const createDialog = ref(false)
const newNamespace = ref('')
const createLoading = ref(false)

// 事件
const emit = defineEmits(['select-namespace', 'refresh-documents'])

// 组件挂载时加载数据
onMounted(() => {
  loadNamespaces()
})

// 加载命名空间列表
const loadNamespaces = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await vectorAdminApi.getNamespaces()
    if (response.code === 200) {
      namespaces.value = response.data || []
      
      // 如果有命名空间，默认选择第一个
      if (namespaces.value.length > 0 && !selectedNamespace.value) {
        selectNamespace(namespaces.value[0])
      } else if (namespaces.value.length === 0) {
        // 如果没有命名空间，清空选中
        selectedNamespace.value = null
        emit('select-namespace', null)
      }
    } else {
      error.value = response.message || '加载命名空间列表失败'
    }
  } catch (err) {
    console.error('加载命名空间列表错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 选择命名空间
const selectNamespace = (namespace) => {
  selectedNamespace.value = namespace
  emit('select-namespace', namespace)
}

// 打开删除确认对话框
const openDeleteDialog = (namespace) => {
  deleteNamespaceValue.value = namespace
  deleteDialog.value = true
}

// 删除命名空间
const deleteNamespace = async () => {
  deleteLoading.value = true
  
  try {
    const response = await vectorAdminApi.deleteNamespace(deleteNamespaceValue.value)
    if (response.code === 200) {
      // 重新加载数据
      loadNamespaces()
      // 如果删除的是当前选中的命名空间，清除选中
      if (deleteNamespaceValue.value === selectedNamespace.value) {
        selectedNamespace.value = null
        emit('select-namespace', null)
      }
      // 通知父组件刷新文档列表
      emit('refresh-documents')
      // 关闭对话框
      deleteDialog.value = false
    } else {
      error.value = response.message || '删除命名空间失败'
    }
  } catch (err) {
    console.error('删除命名空间错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
  }
}

// 创建命名空间
const createNamespace = async () => {
  if (!newNamespace.value.trim()) {
    error.value = '命名空间名称不能为空'
    return
  }
  
  createLoading.value = true
  error.value = ''
  
  try {
    const response = await vectorAdminApi.createNamespace(newNamespace.value.trim())
    if (response.code === 200) {
      // 重新加载数据
      await loadNamespaces()
      // 选中新创建的命名空间
      selectNamespace(newNamespace.value.trim())
      // 关闭对话框并清空输入
      createDialog.value = false
      newNamespace.value = ''
    } else {
      error.value = response.message || '创建命名空间失败'
    }
  } catch (err) {
    console.error('创建命名空间错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    createLoading.value = false
  }
}

// 筛选命名空间
const filteredNamespaces = computed(() => {
  if (!search.value) return namespaces.value
  
  const searchLower = search.value.toLowerCase()
  return namespaces.value.filter(namespace => 
    namespace.toLowerCase().includes(searchLower)
  )
})

// 暴露方法给父组件
defineExpose({
  loadNamespaces
})
</script>

<template>
  <v-card class="namespace-list-card h-100">
    <v-card-title class="d-flex justify-space-between align-center pa-4">
      <div class="text-h6">命名空间列表</div>
      <div class="d-flex">
        <v-btn
          icon="mdi-plus"
          variant="text"
          class="me-2"
          @click="createDialog = true"
          title="创建命名空间"
          color="success"
        ></v-btn>
        <v-btn
          icon="mdi-refresh"
          variant="text"
          @click="loadNamespaces"
          title="刷新列表"
        ></v-btn>
      </div>
    </v-card-title>
    
    <v-divider></v-divider>
    
    <v-card-text class="pa-0">
      <div class="px-4 pt-4">
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="搜索命名空间"
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
        
        <v-list class="namespace-list-items">
          <v-list-item
            v-for="namespace in filteredNamespaces"
            :key="namespace"
            :active="selectedNamespace === namespace"
            @click="selectNamespace(namespace)"
            class="namespace-list-item"
          >
            <template v-slot:prepend>
              <v-icon icon="mdi-database-outline"></v-icon>
            </template>
            
            <v-list-item-title>{{ namespace }}</v-list-item-title>
            
            <template v-slot:append>
              <v-btn
                icon="mdi-delete"
                variant="text"
                density="comfortable"
                size="small"
                color="error"
                @click.stop="openDeleteDialog(namespace)"
                title="删除命名空间"
              ></v-btn>
            </template>
          </v-list-item>
          
          <div v-if="filteredNamespaces.length === 0 && !loading" class="pa-4 text-center text-medium-emphasis">
            {{ namespaces.length === 0 ? '暂无命名空间数据' : '没有匹配的命名空间' }}
          </div>
        </v-list>
      </div>
    </v-card-text>
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      :model-value="deleteDialog"
      @update:model-value="deleteDialog = $event"
      title="确认删除命名空间"
      :message="`确定要删除命名空间『${deleteNamespaceValue}』吗？此操作将删除该命名空间下的所有文档，无法撤销。`"
      :loading="deleteLoading"
      @confirm="deleteNamespace"
    />
    
    <!-- 创建命名空间对话框 -->
    <v-dialog v-model="createDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5 pa-4">
          创建新命名空间
        </v-card-title>
        
        <v-card-text class="pa-4">
          <v-text-field
            v-model="newNamespace"
            label="命名空间名称"
            required
            autofocus
            :disabled="createLoading"
            @keyup.enter="createNamespace"
          ></v-text-field>
          
          <v-alert
            v-if="error && createDialog"
            type="error"
            variant="tonal"
            class="mt-2"
            density="compact"
          >
            {{ error }}
          </v-alert>
        </v-card-text>
        
        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn
            color="grey-darken-1"
            variant="text"
            @click="createDialog = false; newNamespace = ''; error = ''"
            :disabled="createLoading"
          >
            取消
          </v-btn>
          <v-btn
            color="success"
            variant="elevated"
            @click="createNamespace"
            :loading="createLoading"
          >
            创建
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<style scoped>
.namespace-list-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.namespace-list-items {
  max-height: 400px;
  overflow-y: auto;
}

.namespace-list-item {
  border-radius: 4px;
  margin: 4px;
}

.position-relative {
  position: relative;
}

.h-100 {
  height: 100%;
}
</style> 