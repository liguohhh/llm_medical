<script setup>
import { ref, computed, onMounted } from 'vue'
import { preciseAdminApi } from '@/services/preciseAdminApi'
import LoadingIndicator from '@/components/common/LoadingIndicator.vue'
import DeleteConfirmDialog from '@/components/admin/DeleteConfirmDialog.vue'

// 状态变量
const categories = ref([])
const selectedCategory = ref(null)
const loading = ref(false)
const error = ref('')
const search = ref('')

// 删除对话框
const deleteDialog = ref(false)
const deleteCategoryValue = ref(null)
const deleteLoading = ref(false)

// 事件
const emit = defineEmits(['select-category', 'edit-category', 'add-category'])

// 组件挂载时加载数据
onMounted(() => {
  loadCategories()
})

// 加载大类列表
const loadCategories = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await preciseAdminApi.getCategories()
    if (response.code === 200) {
      categories.value = response.data || []
      
      // 如果有大类，默认选择第一个
      if (categories.value.length > 0 && !selectedCategory.value) {
        selectCategory(categories.value[0])
      } else if (categories.value.length === 0) {
        // 如果没有大类，清空选中
        selectedCategory.value = null
        emit('select-category', null)
      }
    } else {
      error.value = response.message || '加载大类列表失败'
    }
  } catch (err) {
    console.error('加载大类列表错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 选择大类
const selectCategory = (category) => {
  selectedCategory.value = category
  emit('select-category', category)
}

// 打开删除确认对话框
const openDeleteDialog = (category) => {
  deleteCategoryValue.value = category
  deleteDialog.value = true
}

// 删除大类
const deleteCategory = async () => {
  deleteLoading.value = true
  
  try {
    const response = await preciseAdminApi.deleteCategory(deleteCategoryValue.value.uid)
    if (response.code === 200) {
      // 重新加载数据
      loadCategories()
      // 如果删除的是当前选中的大类，清除选中
      if (deleteCategoryValue.value.uid === selectedCategory.value?.uid) {
        selectedCategory.value = null
        emit('select-category', null)
      }
      // 关闭对话框
      deleteDialog.value = false
    } else {
      error.value = response.message || '删除大类失败'
    }
  } catch (err) {
    console.error('删除大类错误:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    deleteLoading.value = false
  }
}

// 编辑大类
const editCategory = (category) => {
  emit('edit-category', category)
}

// 添加大类
const addCategory = () => {
  emit('add-category')
}

// 筛选大类
const filteredCategories = computed(() => {
  if (!search.value) return categories.value
  
  const searchLower = search.value.toLowerCase()
  return categories.value.filter(category => 
    category.name.toLowerCase().includes(searchLower)
  )
})

// 暴露方法给父组件
defineExpose({
  loadCategories
})
</script>

<template>
  <v-card class="category-list-card h-100">
    <v-card-title class="d-flex justify-space-between align-center pa-4">
      <div class="text-h6">大类列表</div>
      <div class="d-flex">
        <v-btn
          icon="mdi-plus"
          variant="text"
          color="primary"
          @click="addCategory"
          title="添加大类"
          class="mr-2"
        ></v-btn>
        <v-btn
          icon="mdi-refresh"
          variant="text"
          @click="loadCategories"
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
          label="搜索大类"
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
        
        <v-list class="category-list-items">
          <v-list-item
            v-for="category in filteredCategories"
            :key="category.uid"
            :active="selectedCategory?.uid === category.uid"
            @click="selectCategory(category)"
            class="category-list-item"
          >
            <template v-slot:prepend>
              <v-icon icon="mdi-folder-outline"></v-icon>
            </template>
            
            <v-list-item-title>{{ category.name }}</v-list-item-title>
            
            <template v-slot:append>
              <div class="d-flex">
                <v-btn
                  icon="mdi-pencil"
                  variant="text"
                  density="comfortable"
                  size="small"
                  color="primary"
                  @click.stop="editCategory(category)"
                  title="编辑大类"
                  class="mr-1"
                ></v-btn>
                <v-btn
                  icon="mdi-delete"
                  variant="text"
                  density="comfortable"
                  size="small"
                  color="error"
                  @click.stop="openDeleteDialog(category)"
                  title="删除大类"
                ></v-btn>
              </div>
            </template>
          </v-list-item>
          
          <div v-if="filteredCategories.length === 0 && !loading" class="pa-4 text-center text-medium-emphasis">
            {{ categories.length === 0 ? '暂无大类数据' : '没有匹配的大类' }}
          </div>
        </v-list>
      </div>
    </v-card-text>
    
    <!-- 删除确认对话框 -->
    <delete-confirm-dialog
      :model-value="deleteDialog"
      @update:model-value="deleteDialog = $event"
      title="确认删除大类"
      :message="`确定要删除大类『${deleteCategoryValue?.name}』吗？此操作将删除该大类下的所有条目，无法撤销。`"
      :loading="deleteLoading"
      @confirm="deleteCategory"
    />
  </v-card>
</template>

<style scoped>
.category-list-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.category-list-items {
  max-height: 400px;
  overflow-y: auto;
}

.category-list-item {
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