<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { doctorApi } from '@/services/doctorApi'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'
import DeleteConfirmDialog from '@/components/common/DeleteConfirmDialog.vue'

// 定义props
const props = defineProps({
  category: {
    type: Object,
    default: null
  }
})

// 状态变量
const entries = ref([])
const loading = ref(false)
const error = ref(null)
const search = ref('')

// 删除对话框状态
const deleteDialog = ref(false)
const deleteEntryUid = ref(null)
const deleteLoading = ref(false)

// 加载条目列表
const loadEntries = async () => {
  if (!props.category) {
    entries.value = []
    return
  }
  
  loading.value = true
  error.value = null
  
  try {
    const response = await doctorApi.getPreciseEntries(props.category.uid)
    if (response.code === 200) {
      entries.value = response.data?.entries || []
    } else {
      error.value = response.message || '获取条目列表失败'
    }
  } catch (err) {
    console.error('加载条目列表出错:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 打开删除确认对话框
const openDeleteDialog = (entryUid) => {
  deleteEntryUid.value = entryUid
  deleteDialog.value = true
}

// 删除条目
const deleteEntry = async () => {
  if (!deleteEntryUid.value) return
  
  deleteLoading.value = true
  
  try {
    const response = await doctorApi.deletePreciseEntry(deleteEntryUid.value)
    if (response.code === 200) {
      // 重新加载条目列表
      await loadEntries()
    } else {
      error.value = response.message || '删除条目失败'
    }
  } catch (err) {
    console.error('删除条目出错:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
    deleteDialog.value = false
  }
}

// 添加条目
const handleAddEntry = () => {
  emit('add-entry')
}

// 编辑条目
const handleEditEntry = (entry) => {
  emit('edit-entry', entry)
}

// 过滤后的条目列表
const filteredEntries = computed(() => {
  if (!search.value) return entries.value
  
  const searchLower = search.value.toLowerCase()
  return entries.value.filter(entry => 
    entry.description.toLowerCase().includes(searchLower) || 
    entry.content.toLowerCase().includes(searchLower) ||
    (entry.keywords && entry.keywords.some(kw => kw.toLowerCase().includes(searchLower)))
  )
})

// 监听category变化，重新加载条目
watch(() => props.category, (newVal) => {
  if (newVal) {
    loadEntries()
  } else {
    entries.value = []
  }
}, { immediate: true })

// 定义事件
const emit = defineEmits(['add-entry', 'edit-entry'])

// 暴露方法给父组件
defineExpose({
  loadEntries
})
</script>

<template>
  <v-card>
    <v-card-title class="d-flex justify-space-between align-center">
      <span>
        {{ props.category ? `${props.category.name} - 条目列表` : '条目列表' }}
      </span>
      <div>
        <v-btn
          color="primary"
          size="small"
          prepend-icon="mdi-plus"
          @click="handleAddEntry"
          :disabled="!props.category"
          class="me-2"
        >
          添加条目
        </v-btn>
        <v-btn
          color="secondary"
          size="small"
          prepend-icon="mdi-refresh"
          @click="loadEntries"
          :disabled="!props.category"
          :loading="loading"
        >
          刷新
        </v-btn>
      </div>
    </v-card-title>
    
    <v-card-text>
      <!-- 无大类选择提示 -->
      <v-alert
        v-if="!props.category"
        type="info"
        variant="tonal"
        class="mb-3"
      >
        请先选择左侧的大类
      </v-alert>
      
      <template v-else>
        <!-- 搜索框 -->
        <v-text-field
          v-model="search"
          label="搜索条目"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          hide-details
          class="mb-4"
        ></v-text-field>
        
        <!-- 加载指示器 -->
        <loading-indicator v-if="loading" />
        
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
        
        <!-- 条目列表 -->
        <div v-if="!loading && filteredEntries.length > 0">
          <v-card
            v-for="entry in filteredEntries"
            :key="entry.uid"
            class="mb-4 entry-card"
            :class="{ 'disabled-entry': !entry.isEnabled }"
            variant="outlined"
          >
            <v-card-item>
              <template v-slot:prepend>
                <v-chip
                  :color="entry.isEnabled ? 'success' : 'grey'"
                  size="small"
                  class="me-2"
                >
                  {{ entry.isEnabled ? '启用' : '禁用' }}
                </v-chip>
              </template>
              
              <v-card-title>{{ entry.description }}</v-card-title>
              
              <template v-slot:append>
                <div class="d-flex">
                  <v-btn
                    icon="mdi-pencil"
                    variant="text"
                    size="small"
                    color="primary"
                    @click="handleEditEntry(entry)"
                    class="me-1"
                  ></v-btn>
                  <v-btn
                    icon="mdi-delete"
                    variant="text"
                    size="small"
                    color="error"
                    @click="openDeleteDialog(entry.uid)"
                  ></v-btn>
                </div>
              </template>
            </v-card-item>
            
            <v-card-text>
              <div class="text-subtitle-2 mb-1">内容:</div>
              <div class="entry-content mb-3">{{ entry.content }}</div>
              
              <div class="d-flex align-center">
                <div class="text-subtitle-2 me-2">关键词:</div>
                <div>
                  <v-chip
                    v-for="(keyword, index) in entry.keywords"
                    :key="index"
                    size="x-small"
                    class="me-1 mb-1"
                    color="primary"
                    variant="outlined"
                  >
                    {{ keyword }}
                  </v-chip>
                </div>
              </div>
              
              <div class="d-flex align-center mt-2">
                <div class="text-subtitle-2 me-2">权重:</div>
                <v-rating
                  :model-value="entry.weight"
                  density="compact"
                  size="small"
                  readonly
                  color="amber"
                  half-increments
                  :length="5"
                ></v-rating>
                <span class="text-body-2 ms-2">({{ entry.weight }})</span>
              </div>
            </v-card-text>
          </v-card>
        </div>
        
        <!-- 空状态 -->
        <v-alert
          v-else-if="!loading && filteredEntries.length === 0"
          type="info"
          variant="tonal"
          class="mb-3"
        >
          {{ search ? '没有找到匹配的条目' : '暂无条目，请添加' }}
        </v-alert>
      </template>
    </v-card-text>
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      v-model="deleteDialog"
      title="删除条目"
      :content="`确定要删除此条目吗？此操作不可恢复。`"
      :loading="deleteLoading"
      @confirm="deleteEntry"
    />
  </v-card>
</template>

<style scoped>
.entry-card {
  transition: all 0.2s ease;
}

.entry-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.disabled-entry {
  opacity: 0.7;
  background-color: rgba(0, 0, 0, 0.02);
}

.entry-content {
  white-space: pre-wrap;
  max-height: 100px;
  overflow-y: auto;
  padding: 8px;
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}
</style> 