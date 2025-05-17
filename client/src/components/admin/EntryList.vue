<script setup>
import { ref, computed, watch } from 'vue'
import { preciseAdminApi } from '@/services/preciseAdminApi'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'
import DeleteConfirmDialog from '@/components/admin/DeleteConfirmDialog.vue'

// 属性定义
const props = defineProps({
  category: {
    type: Object,
    default: null
  }
})

// 状态变量
const entries = ref([])
const loading = ref(false)
const error = ref('')
const search = ref('')

// 删除对话框
const deleteDialog = ref(false)
const deleteEntryValue = ref(null)
const deleteLoading = ref(false)

// 事件
const emit = defineEmits(['edit-entry', 'add-entry'])

// 监听选中的大类变化
watch(() => props.category, (newCategory) => {
  if (newCategory) {
    loadEntries()
  } else {
    entries.value = []
  }
}, { immediate: true })

// 加载条目列表
const loadEntries = async () => {
  if (!props.category) return
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await preciseAdminApi.getEntries(props.category.uid)
    if (response.code === 200) {
      entries.value = response.data?.entries || []
    } else {
      error.value = response.message || '加载条目列表失败'
    }
  } catch (err) {
    console.error('加载条目列表错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 打开删除确认对话框
const openDeleteDialog = (entry) => {
  deleteEntryValue.value = entry
  deleteDialog.value = true
}

// 删除条目
const deleteEntry = async () => {
  deleteLoading.value = true
  
  try {
    const response = await preciseAdminApi.deleteEntry(deleteEntryValue.value.uid)
    if (response.code === 200) {
      // 重新加载数据
      loadEntries()
      // 关闭对话框
      deleteDialog.value = false
    } else {
      error.value = response.message || '删除条目失败'
    }
  } catch (err) {
    console.error('删除条目错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
  }
}

// 编辑条目
const editEntry = (entry) => {
  emit('edit-entry', entry)
}

// 添加条目
const addEntry = () => {
  emit('add-entry')
}

// 筛选条目
const filteredEntries = computed(() => {
  if (!search.value) return entries.value
  
  const searchLower = search.value.toLowerCase()
  return entries.value.filter(entry => 
    entry.description.toLowerCase().includes(searchLower) || 
    entry.keywords.some(keyword => keyword.toLowerCase().includes(searchLower))
  )
})

// 格式化关键词
const formatKeywords = (keywords) => {
  return keywords.join(', ')
}

// 暴露方法给父组件
defineExpose({
  loadEntries
})
</script>

<template>
  <v-card class="entry-list-card h-100">
    <v-card-title class="d-flex justify-space-between align-center pa-4">
      <div class="text-h6">
        条目列表
        <span v-if="props.category" class="text-subtitle-1 ml-2 text-medium-emphasis">
          ({{ props.category.name }})
        </span>
      </div>
      <div class="d-flex">
        <v-btn
          icon="mdi-plus"
          variant="text"
          color="primary"
          @click="addEntry"
          :disabled="!props.category"
          title="添加条目"
          class="mr-2"
        ></v-btn>
        <v-btn
          icon="mdi-refresh"
          variant="text"
          @click="loadEntries"
          :disabled="!props.category"
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
          label="搜索条目"
          single-line
          hide-details
          density="compact"
          variant="outlined"
          bg-color="grey-lighten-5"
          :disabled="!props.category"
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
          v-if="!props.category && !loading"
          type="info"
          variant="tonal"
          class="ma-4"
          density="compact"
        >
          请先选择一个大类查看条目
        </v-alert>
        
        <div v-if="props.category">
          <v-table class="entry-table">
            <thead>
              <tr>
                <th>描述</th>
                <th>权重</th>
                <th>关键词</th>
                <th>状态</th>
                <th class="text-center">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="entry in filteredEntries" :key="entry.uid">
                <td class="text-no-wrap">{{ entry.description }}</td>
                <td>{{ entry.weight }}</td>
                <td>
                  <div class="keywords-container">
                    {{ formatKeywords(entry.keywords) }}
                  </div>
                </td>
                <td>
                  <v-chip
                    :color="entry.is_enabled ? 'success' : 'grey'"
                    size="small"
                    variant="tonal"
                  >
                    {{ entry.is_enabled ? '启用' : '禁用' }}
                  </v-chip>
                </td>
                <td class="text-center">
                  <div class="d-flex justify-center">
                    <v-btn
                      icon="mdi-pencil"
                      variant="text"
                      density="comfortable"
                      size="small"
                      color="primary"
                      @click="editEntry(entry)"
                      title="编辑条目"
                      class="mr-1"
                    ></v-btn>
                    <v-btn
                      icon="mdi-delete"
                      variant="text"
                      density="comfortable"
                      size="small"
                      color="error"
                      @click="openDeleteDialog(entry)"
                      title="删除条目"
                    ></v-btn>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredEntries.length === 0 && !loading && props.category">
                <td colspan="5" class="text-center py-4 text-medium-emphasis">
                  {{ entries.length === 0 ? '暂无条目数据' : '没有匹配的条目' }}
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
      title="确认删除条目"
      :message="`确定要删除条目『${deleteEntryValue?.description}』吗？此操作无法撤销。`"
      :loading="deleteLoading"
      @confirm="deleteEntry"
    />
  </v-card>
</template>

<style scoped>
.entry-list-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.entry-table {
  font-size: 14px;
}

.keywords-container {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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