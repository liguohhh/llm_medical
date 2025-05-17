<script setup>
import { ref, computed, watch } from 'vue'
import { vectorAdminApi } from '@/services/vectorAdminApi'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'
import DeleteConfirmDialog from '@/components/admin/DeleteConfirmDialog.vue'

// 属性定义
const props = defineProps({
  namespace: {
    type: String,
    default: null
  }
})

// 状态变量
const documents = ref([])
const loading = ref(false)
const error = ref('')
const search = ref('')

// 删除对话框
const deleteDialog = ref(false)
const deleteDocumentId = ref(null)
const deleteLoading = ref(false)

// 监听命名空间变化
watch(() => props.namespace, (newNamespace) => {
  if (newNamespace) {
    loadDocuments()
  } else {
    documents.value = []
  }
}, { immediate: true })

// 加载文档列表
const loadDocuments = async () => {
  if (!props.namespace) return
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await vectorAdminApi.getDocuments(props.namespace)
    if (response.code === 200) {
      documents.value = response.data || []
    } else {
      error.value = response.message || '加载文档列表失败'
    }
  } catch (err) {
    console.error('加载文档列表错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 打开删除确认对话框
const openDeleteDialog = (docId) => {
  deleteDocumentId.value = docId
  deleteDialog.value = true
}

// 删除文档
const deleteDocument = async () => {
  deleteLoading.value = true
  
  try {
    const response = await vectorAdminApi.deleteDocument(props.namespace, deleteDocumentId.value)
    if (response.code === 200) {
      // 重新加载数据
      loadDocuments()
      // 关闭对话框
      deleteDialog.value = false
    } else {
      error.value = response.message || '删除文档失败'
    }
  } catch (err) {
    console.error('删除文档错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
  }
}

// 筛选文档
const filteredDocuments = computed(() => {
  if (!search.value) return documents.value
  
  const searchLower = search.value.toLowerCase()
  return documents.value.filter(doc => 
    (doc.doc_id && doc.doc_id.toLowerCase().includes(searchLower)) || 
    (doc.source && doc.source.toLowerCase().includes(searchLower))
  )
})

// 暴露方法给父组件
defineExpose({
  loadDocuments
})
</script>

<template>
  <v-card class="document-list-card h-100">
    <v-card-title class="d-flex justify-space-between align-center pa-4">
      <div class="text-h6">
        文档列表
        <span v-if="props.namespace" class="text-subtitle-1 ml-2 text-medium-emphasis">
          ({{ props.namespace }})
        </span>
      </div>
      <div class="d-flex">
        <v-btn
          icon="mdi-refresh"
          variant="text"
          @click="loadDocuments"
          :disabled="!props.namespace"
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
          label="搜索文档"
          single-line
          hide-details
          density="compact"
          variant="outlined"
          bg-color="grey-lighten-5"
          :disabled="!props.namespace"
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
        
        <v-alert
          v-if="!props.namespace && !loading"
          type="info"
          variant="tonal"
          class="ma-4"
          density="compact"
        >
          请先选择一个命名空间查看文档
        </v-alert>
        
        <div v-if="props.namespace">
          <v-table class="document-table">
            <thead>
              <tr>
                <th>文档ID</th>
                <th>来源</th>
                <th class="text-center">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="doc in filteredDocuments" :key="doc.doc_id">
                <td class="text-no-wrap">{{ doc.doc_id }}</td>
                <td class="text-no-wrap">{{ doc.source || '未知' }}</td>
                <td class="text-center">
                  <v-btn
                    icon="mdi-delete"
                    variant="text"
                    density="comfortable"
                    size="small"
                    color="error"
                    @click="openDeleteDialog(doc.doc_id)"
                    title="删除文档"
                  ></v-btn>
                </td>
              </tr>
              <tr v-if="filteredDocuments.length === 0 && !loading && props.namespace">
                <td colspan="3" class="text-center py-4 text-medium-emphasis">
                  {{ documents.length === 0 ? '暂无文档数据' : '没有匹配的文档' }}
                </td>
              </tr>
            </tbody>
          </v-table>
        </div>
      </div>
    </v-card-text>
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      :model-value="deleteDialog"
      @update:model-value="deleteDialog = $event"
      title="确认删除文档"
      :message="`确定要删除文档『${deleteDocumentId}』吗？此操作无法撤销。`"
      :loading="deleteLoading"
      @confirm="deleteDocument"
    />
  </v-card>
</template>

<style scoped>
.document-list-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.document-table {
  font-size: 14px;
}

.position-relative {
  position: relative;
}

.h-100 {
  height: 100%;
}

.text-no-wrap {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
</style> 